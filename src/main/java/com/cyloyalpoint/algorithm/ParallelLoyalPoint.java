package com.cyloyalpoint.algorithm;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.opencl.cycl.CyCL;
import org.cytoscape.opencl.cycl.CyCLBuffer;
import org.cytoscape.opencl.cycl.CyCLDevice;
import org.cytoscape.opencl.cycl.CyCLProgram;

import com.cyloyalpoint.util.NetworkUtil;

public class ParallelLoyalPoint {

	private final CyCLDevice device;
	private final CyCLProgram program;

	private CyNetwork network;

	public ParallelLoyalPoint(CyNetwork network) {
		try {
			this.device = CyCL.getDevices().get(0);
		} catch (Exception e) {
			throw new RuntimeException("No OpenCL devices found, cannot run program.");
		}

		String[] kernelNames = new String[] { "Init" };

		CyCLProgram tryProgram;
		try {
			tryProgram = device.addProgram("LoyalPoint", getClass().getResource("/LoyalPoint.cl"), kernelNames, null,
					false);
		} catch (Exception exc) {
			throw new RuntimeException("Could not load and compile OpenCL program.");
		}
		this.program = tryProgram;

		this.network = network;
	}

	private class LoyalPoint {

		private int nodeCount;
		private float E;
		private final float EPS = 1e-7f;
		private int[] unDirectedAdjacentList;
		private int[] outDirectedAdjacentList;
		private int[] inDirectedAdjacentList;

		private CyCLBuffer bufferArrayA;
		private CyCLBuffer bufferArrayB;
		private CyCLBuffer bufferArrayC;
		private boolean buffersInitialized = false;

		public LoyalPoint(CyNetwork network) {
			this.nodeCount = network.getNodeCount();

			this.inDirectedAdjacentList = NetworkUtil.extractNetworkInDirectedAdjacentList(network);
			this.outDirectedAdjacentList = NetworkUtil.extractNetworkOutDirectedAdjacentList(network);
			this.unDirectedAdjacentList = NetworkUtil.extractNetworkUnDirectedAdjacentList(network);

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
			if (adjList == null)
				return 0;
			return adjList[node + 1] - adjList[node];
		}

		private int getCurrentIndex(int[] adjList, int node, int index) {
			if (adjList == null)
				return 0;
			return adjList[adjList[node] + index];
		}

		private void initializeBuffers() {

			// bufferArrayA = device.createBuffer(a);
			// bufferArrayB = device.createBuffer(b);
			// bufferArrayC = device.createBuffer(c);

			buffersInitialized = true;
		}

		private void freeBuffers() {
			if (!buffersInitialized) {
				return;
			}

			bufferArrayA.free();
			bufferArrayB.free();
			bufferArrayC.free();

			buffersInitialized = false;
		}

		public void run() {
			initializeBuffers();

			// program.getKernel("Init").execute(new long[] { n }, null, bufferArrayA,
			// bufferArrayB, bufferArrayC, n);
			// bufferArrayC.getFromDevice(c);

			freeBuffers();
		}
	}
}
