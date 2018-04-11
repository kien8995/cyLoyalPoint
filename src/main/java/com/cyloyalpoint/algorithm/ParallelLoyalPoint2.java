package com.cyloyalpoint.algorithm;

import java.util.HashMap;
import java.util.Map;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.opencl.cycl.CyCL;
import org.cytoscape.opencl.cycl.CyCLBuffer;
import org.cytoscape.opencl.cycl.CyCLDevice;
import org.cytoscape.opencl.cycl.CyCLProgram;

import com.cyloyalpoint.util.NetworkUtil;

public class ParallelLoyalPoint2 {

	private final CyCLDevice device;
	private final CyCLProgram program;

	private CyNetwork network;

	public ParallelLoyalPoint2(CyNetwork network) {
		try {
			this.device = CyCL.getDevices().get(0);
		} catch (Exception e) {
			throw new RuntimeException("No OpenCL devices found, cannot run program.");
		}

		String[] kernelNames = new String[] { "InitLoyalPoint", "InitResult", "ComputeLoyalNodesOfLeader" };

		CyCLProgram tryProgram;
		try {
			tryProgram = device.forceAddProgram("LoyalPoint2", getClass().getResource("/LoyalPoint2.cl"), kernelNames,
					null, false);
		} catch (Exception exc) {
			throw new RuntimeException("Could not load and compile OpenCL program.");
		}
		this.program = tryProgram;

		this.network = network;
	}

	public String getDeviceInfo() {
		String info = "";
		info += "Address Bits: " + device.addressBits + "\n";
		info += "Benchmark Score: " + device.benchmarkScore + "\n";
		info += "Best Block Size: " + device.bestBlockSize + "\n";
		info += "Best Warp Size: " + device.bestWarpSize + "\n";
		info += "Clock Frequency: " + device.clockFrequency + "\n";
		info += "Compute Units: " + device.computeUnits + "\n";
		info += "Global Mem Size: " + device.globalMemSize + "\n";
		info += "Local Mem Size: " + device.localMemSize + "\n";
		info += "Local Mem Type: " + device.localMemType + "\n";
		info += "Max Const Buffer Size: " + device.maxConstBufferSize + "\n";
		info += "Max Malloc Size: " + device.maxMallocSize + "\n";
		info += "Max Read Image Args: " + device.maxReadImageArgs + "\n";
		info += "Max Work Group Size: " + device.maxWorkGroupSize + "\n";
		info += "Max Write Image Args: " + device.maxWriteImageArgs + "\n";
		info += "Name: " + device.name + "\n";
		info += "Platform Name: " + device.platformName + "\n";
		info += "Pref Width Char: " + device.prefWidthChar + "\n";
		info += "Pref Width Double: " + device.prefWidthDouble + "\n";
		info += "Pref Width Float: " + device.prefWidthFloat + "\n";
		info += "Pref Width Int: " + device.prefWidthInt + "\n";
		info += "Pref Width Long: " + device.prefWidthLong + "\n";
		info += "Pref Width Short: " + device.prefWidthShort + "\n";
		info += "Vendor: " + device.vendor + "\n";
		info += "Version: " + device.version + "\n";
		info += "Work Item Dimensions: " + device.workItemDimensions + "\n";
		info += "Supports ECC: " + device.supportsECC + "\n";
		info += "Supports Images: " + device.supportsImages + "\n";
		return info;
	}

	public Map<Integer, Float> compute(int leader) {
		LoyalPoint lp = new LoyalPoint(network);
		return lp.compute(leader);
	}

	private class LoyalPoint {

		private int nodeCount;
		private float E;
		private int[] unDirectedAdjacentList;
		private int[] outDirectedAdjacentList;
		private int[] inDirectedAdjacentList;
		private int[] normalNode;
		private float[] loyalPoint;
		private float[] result;

		// CyBuffers
		private CyCLBuffer bufferUnDirectedAdjacentList;
		private CyCLBuffer bufferInDirectedAdjacentList;
		private CyCLBuffer bufferOutDirectedAdjacentList;
		private CyCLBuffer bufferNormalNode;
		private CyCLBuffer bufferLoyalPoint;
		private CyCLBuffer bufferResult;

		private boolean buffersInitialized = false;

		public LoyalPoint(CyNetwork network) {
			this.nodeCount = network.getNodeCount();

			this.inDirectedAdjacentList = NetworkUtil.extractNetworkInDirectedAdjacentList(network);
			this.outDirectedAdjacentList = NetworkUtil.extractNetworkOutDirectedAdjacentList(network);
			this.unDirectedAdjacentList = NetworkUtil.extractNetworkUnDirectedAdjacentList(network);

			this.normalNode = new int[nodeCount - 1];
			this.loyalPoint = new float[(nodeCount * nodeCount)];
			this.result = new float[nodeCount + 1];

			initialize();
		}

		private void initialize() {
			int maxOutDegMixing = 0;
			for (int node = 0; node < nodeCount; node++) {
				int sum = getNumberOfAdjacent(outDirectedAdjacentList, node)
						+ getNumberOfAdjacent(unDirectedAdjacentList, node);
				if (sum > maxOutDegMixing) {
					maxOutDegMixing = sum;
				}
			}

			E = 1.0f / maxOutDegMixing;
		}

		private int getNumberOfAdjacent(int[] adjList, int node) {
			if (adjList[0] == -1) {
				return 0;
			}
			return adjList[node + 1] - adjList[node];
		}

		private void initializeBuffers() {

			bufferInDirectedAdjacentList = device.createBuffer(inDirectedAdjacentList);
			bufferOutDirectedAdjacentList = device.createBuffer(outDirectedAdjacentList);
			bufferUnDirectedAdjacentList = device.createBuffer(unDirectedAdjacentList);
			bufferNormalNode = device.createBuffer(normalNode);
			bufferLoyalPoint = device.createBuffer(loyalPoint);
			bufferResult = device.createBuffer(result);

			buffersInitialized = true;
		}

		private void freeBuffers() {
			if (!buffersInitialized) {
				return;
			}

			bufferInDirectedAdjacentList.free();
			bufferOutDirectedAdjacentList.free();
			bufferUnDirectedAdjacentList.free();
			bufferNormalNode.free();
			bufferLoyalPoint.free();
			bufferResult.free();

			buffersInitialized = false;
		}

		public Map<Integer, Float> compute(int leader) {

			int againstLeader = nodeCount;

			int normalNodeIndex = 0;
			for (int node = 0; node < nodeCount; node++) {
				if (node != leader) {
					normalNode[normalNodeIndex++] = node;
				}	
			}

			initializeBuffers();
			
			program.getKernel("InitLoyalPoint").execute(new long[] { (nodeCount * nodeCount) }, null, bufferLoyalPoint);
			
			program.getKernel("InitResult").execute(new long[] { nodeCount + 1 }, null, bufferResult);

			program.getKernel("ComputeLoyalNodesOfLeader").execute(new long[] { nodeCount - 1 }, null,
					bufferInDirectedAdjacentList, bufferUnDirectedAdjacentList, bufferNormalNode, bufferLoyalPoint,
					bufferResult, leader, againstLeader, nodeCount, E, nodeCount - 1);

			bufferResult.getFromDevice(result);

			Map<Integer, Float> ans = new HashMap<>();
			
			for (int node = 0; node < result.length; node++) {
				if (result[node] != -2.0f) {
					ans.put(node, result[node]);
				}
			}

			freeBuffers();

			return ans;
		}
	}
}
