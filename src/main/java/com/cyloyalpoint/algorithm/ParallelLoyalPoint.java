package com.cyloyalpoint.algorithm;

import org.cytoscape.opencl.cycl.CyCL;
import org.cytoscape.opencl.cycl.CyCLBuffer;
import org.cytoscape.opencl.cycl.CyCLDevice;
import org.cytoscape.opencl.cycl.CyCLProgram;

public class ParallelLoyalPoint {

	private final CyCLDevice device;
	private final CyCLProgram program;

	// buffer
	private CyCLBuffer bufferArrayA;
	private CyCLBuffer bufferArrayB;
	private CyCLBuffer bufferArrayC;
	private boolean buffersInitialized = false;

	int n = 3;
	int[] a = new int[] { 1, 2, 3 };
	int[] b = new int[] { 9, 8, 7 };
	int[] c = new int[n];

	public ParallelLoyalPoint() {
		try {
			device = CyCL.getDevices().get(0);
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
		program = tryProgram;
	}

	private void initializeBuffers() {

		bufferArrayA = device.createBuffer(a);
		bufferArrayB = device.createBuffer(b);
		bufferArrayC = device.createBuffer(c);

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

	public int[] run() {
		initializeBuffers();

		program.getKernel("Init").execute(new long[] { n }, null, bufferArrayA, bufferArrayB, bufferArrayC, n);
		bufferArrayC.getFromDevice(c);

		freeBuffers();

		return c;
	}
}
