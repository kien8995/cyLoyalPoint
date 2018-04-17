package com.cyloyalpoint.internal;

import java.awt.event.ActionEvent;
import java.util.Properties;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.swing.AbstractCyAction;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.application.swing.CytoPanel;
import org.cytoscape.application.swing.CytoPanelComponent;
import org.cytoscape.application.swing.CytoPanelName;
import org.cytoscape.application.swing.CytoPanelState;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.work.TaskManager;

public class LoyalPointPanelAction extends AbstractCyAction {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private final CySwingApplication cySwingApplication;
	private final CyServiceRegistrar cyServiceRegistrar;
	private final CytoPanel cytoPanelSouth;
	private LoyalPointPanel loyalPointPanel;

	public LoyalPointPanelAction(CyApplicationManager cyApplicationManager, CySwingApplication cySwingApplication,
			CyServiceRegistrar cyServiceRegistrar, TaskManager<?, ?> taskManager, String parentMenu, String panelName) {
		super(panelName);
		setPreferredMenu(parentMenu);

		this.cySwingApplication = cySwingApplication;
		this.cyServiceRegistrar = cyServiceRegistrar;
		this.cytoPanelSouth = this.cySwingApplication.getCytoPanel(CytoPanelName.SOUTH);
		this.loyalPointPanel = new LoyalPointPanel(cyApplicationManager, cySwingApplication, taskManager);
	}

	public void actionPerformed(ActionEvent e) {
		cyServiceRegistrar.registerService(loyalPointPanel, CytoPanelComponent.class, new Properties());

		App.services.put(loyalPointPanel, CytoPanelComponent.class);
		
		if (cytoPanelSouth.getState() == CytoPanelState.HIDE) {
			cytoPanelSouth.setState(CytoPanelState.DOCK);
		}

		int index = cytoPanelSouth.indexOfComponent(loyalPointPanel);
		if (index == -1) {
			return;
		}

		cytoPanelSouth.setSelectedIndex(index);
	}

}
