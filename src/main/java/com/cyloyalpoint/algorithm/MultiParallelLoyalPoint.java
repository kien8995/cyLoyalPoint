package com.cyloyalpoint.algorithm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.opencl.cycl.CyCLBuffer;
import org.cytoscape.opencl.cycl.CyCLDevice;
import org.cytoscape.opencl.cycl.CyCLProgram;

import com.cyloyalpoint.util.ArrayUtil;
import com.cyloyalpoint.util.MathUtil;
import com.cyloyalpoint.util.NetworkUtil;

public class MultiParallelLoyalPoint {

	private final List<CyCLDevice> devices;
	private final List<Double> benchMarks;
	private final List<CyCLProgram> programs;

	private final CyNetwork network;

	private final long[] GLOBAL_WORK_SIZE;
	private final long[] LOCAL_WORK_SIZE;

	public MultiParallelLoyalPoint(CyNetwork network, List<CyCLDevice> devices, List<Double> benchMarks) {
		this.network = network;
		this.devices = devices;
		this.benchMarks = benchMarks;
		this.programs = new ArrayList<>();
		this.GLOBAL_WORK_SIZE = new long[devices.size()];
		this.LOCAL_WORK_SIZE = new long[devices.size()];

		String[] kernelNames = new String[] { "InitLoyalPoint", "InitResult", "ComputeLoyalNodesOfLeader" };

		int index = 0;
		for (CyCLDevice device : devices) {
			CyCLProgram tryProgram;
			try {
				tryProgram = device.forceAddProgram("MultiParallelLoyalPoint",
						getClass().getResource("/LoyalPoint2.cl"), kernelNames, null, false);
			} catch (Exception exc) {
				throw new RuntimeException("Could not load and compile OpenCL program.");
			}
			this.programs.add(tryProgram);

			this.GLOBAL_WORK_SIZE[index] = device.bestWarpSize * device.computeUnits;
			this.LOCAL_WORK_SIZE[index] = device.bestWarpSize;

			index++;
		}
	}

	public Map<Integer, Float> compute(int leader) {
		int nodeCount = network.getNodeCount();
		int[] inDirectedAdjacentList = NetworkUtil.extractNetworkInDirectedAdjacentList(network);
		int[] outDirectedAdjacentList = NetworkUtil.extractNetworkOutDirectedAdjacentList(network);
		int[] unDirectedAdjacentList = NetworkUtil.extractNetworkUnDirectedAdjacentList(network);

		float E;
		int maxOutDegMixing = Integer.MIN_VALUE;
		for (int node = 0; node < nodeCount; node++) {
			int sum = (outDirectedAdjacentList[0] == -1 ? 0
					: outDirectedAdjacentList[node + 1] - outDirectedAdjacentList[node])
					+ (unDirectedAdjacentList[0] == -1 ? 0
							: unDirectedAdjacentList[node + 1] - unDirectedAdjacentList[node]);
			if (sum > maxOutDegMixing) {
				maxOutDegMixing = sum;
			}
		}
		E = 1.0f / maxOutDegMixing;

		int againstLeader = nodeCount;

		int[] normalNode = new int[nodeCount - 1];
		int normalNodeIndex = 0;
		for (int node = 0; node < nodeCount; node++) {
			if (node != leader) {
				normalNode[normalNodeIndex++] = node;
			}
		}

		NetworkCommon common = new NetworkCommon(inDirectedAdjacentList, outDirectedAdjacentList,
				unDirectedAdjacentList, E, leader, againstLeader, nodeCount);

		int numberNodePerChunk = (normalNode.length / devices.size())
				+ (normalNode.length % devices.size() == 0 ? 0 : 1);
		int[][] normalNodes = ArrayUtil.splitArrayIntoChunks(normalNode, numberNodePerChunk);
		
		Map<Integer, Float> result = new HashMap<>();

		for (int i = 0; i < devices.size(); i++) {
			LoyalPoint lp = new LoyalPoint(devices.get(i), programs.get(i), GLOBAL_WORK_SIZE[i], LOCAL_WORK_SIZE[i],
					normalNodes[i], common);

			result.putAll(lp.compute());
		}

		return result;
	}

	private class NetworkCommon {

		int[] inDirectedAdjacentList;
		int[] outDirectedAdjacentList;
		int[] unDirectedAdjacentList;
		float E;
		int leader;
		int againstLeader;
		int nodeCount;

		public NetworkCommon(int[] inDirectedAdjacentList, int[] outDirectedAdjacentList, int[] unDirectedAdjacentList,
				float E, int leader, int againstLeader, int nodeCount) {
			this.inDirectedAdjacentList = inDirectedAdjacentList;
			this.outDirectedAdjacentList = outDirectedAdjacentList;
			this.unDirectedAdjacentList = unDirectedAdjacentList;
			this.E = E;
			this.leader = leader;
			this.againstLeader = againstLeader;
			this.nodeCount = nodeCount;
		}
	}

	private class LoyalPoint {

		private final float EPS = 1e-5f;
		private final int MAX_ITERATION = 500;
		private CyCLDevice device;
		private CyCLProgram program;
		private long globalWorkSize;
		private long localWorkSize;
		private int[] normalNode;
		private NetworkCommon common;
		private float[] loyalPoint;
		private int[] resultIndex;
		private float[] resultValue;

		// CyBuffers
		private CyCLBuffer bufferUnDirectedAdjacentList;
		private CyCLBuffer bufferInDirectedAdjacentList;
		private CyCLBuffer bufferOutDirectedAdjacentList;
		private CyCLBuffer bufferNormalNode;
		private CyCLBuffer bufferLoyalPoint;
		private CyCLBuffer bufferResultIndex;
		private CyCLBuffer bufferResultValue;

		private boolean buffersInitialized = false;

		public LoyalPoint(CyCLDevice device, CyCLProgram program, long globalWorkSize, long localWorkSize,
				int[] normalNode, NetworkCommon common) {
			this.device = device;
			this.program = program;
			this.globalWorkSize = globalWorkSize;
			this.localWorkSize = localWorkSize;
			this.normalNode = normalNode;
			this.common = common;
		}

		private void initializeBuffers() {

			bufferInDirectedAdjacentList = device.createBuffer(common.inDirectedAdjacentList);
			bufferOutDirectedAdjacentList = device.createBuffer(common.outDirectedAdjacentList);
			bufferUnDirectedAdjacentList = device.createBuffer(common.unDirectedAdjacentList);
			bufferNormalNode = device.createBuffer(normalNode);
			bufferLoyalPoint = device.createBuffer(loyalPoint);
			bufferResultIndex = device.createBuffer(resultIndex);
			bufferResultValue = device.createBuffer(resultValue);

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
			bufferResultIndex.free();
			bufferResultValue.free();

			buffersInitialized = false;
		}

		public Map<Integer, Float> compute() {

			Map<Integer, Float> ans = new HashMap<>();

			int[][] chunks = ArrayUtil.splitArrayIntoChunks(normalNode, (int) globalWorkSize);

			for (int[] chunk : chunks) {

				loyalPoint = new float[(common.nodeCount + 1) * chunk.length];
				resultIndex = new int[chunk.length];
				resultValue = new float[chunk.length];

				normalNode = chunk;

				initializeBuffers();

				program.getKernel("InitLoyalPoint").execute(new long[] { (common.nodeCount + 1) * chunk.length }, null,
						bufferLoyalPoint);

				program.getKernel("InitResult").execute(new long[] { chunk.length }, null, bufferResultIndex,
						bufferResultValue);

				program.getKernel("ComputeLoyalNodesOfLeader").execute(new long[] { globalWorkSize },
						new long[] { localWorkSize }, bufferInDirectedAdjacentList, bufferUnDirectedAdjacentList,
						bufferNormalNode, bufferLoyalPoint, bufferResultIndex, bufferResultValue, common.leader,
						common.againstLeader, common.nodeCount, common.E, EPS, MAX_ITERATION, chunk.length);

				bufferResultIndex.getFromDevice(resultIndex);
				bufferResultValue.getFromDevice(resultValue);

				for (int index = 0; index < chunk.length; index++) {
					if (resultValue[index] != -9999.0f) {
						ans.put(resultIndex[index], MathUtil.zero(resultValue[index], EPS));
					}
				}

				freeBuffers();
			}

			return ans;
		}
	}
}
