package com.cyloyalpoint.internal;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.task.NetworkTaskFactory;
import org.cytoscape.work.TaskIterator;


public class LoyalPointTaskFactory implements NetworkTaskFactory {

	@Override
	public TaskIterator createTaskIterator(CyNetwork network) {
		return new TaskIterator(new LoyalPointTask(network));
	}

	@Override
	public boolean isReady(CyNetwork network) {
		return network != null;
	}
	
}