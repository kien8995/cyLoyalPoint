package com.cyloyalpoint.algorithm;

import org.cytoscape.opencl.cycl.CyCL;
import org.cytoscape.opencl.cycl.CyCLDevice;

public class ParallelLoyalPoint2 {

	private final CyCLDevice device;

	public ParallelLoyalPoint2() {
		try {
			this.device = CyCL.getDevices().get(0);
		} catch (Exception e) {
			throw new RuntimeException("No OpenCL devices found, cannot run program.");
		}
	}

	public long run() {
		return device.bestWarpSize;
	}
}
