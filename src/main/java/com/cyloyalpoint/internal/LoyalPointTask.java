package com.cyloyalpoint.internal;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;

import com.cyloyalpoint.view.View;

public class LoyalPointTask extends AbstractTask {

	private CyNetwork network;

	public LoyalPointTask(CyNetwork network) {
		this.network = network;
	}

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {

		new View(network);

	}

}
