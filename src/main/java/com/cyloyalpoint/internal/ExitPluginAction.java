package com.cyloyalpoint.internal;

import java.awt.event.ActionEvent;

import org.cytoscape.application.swing.AbstractCyAction;
import org.cytoscape.application.swing.CytoPanelComponent;
import org.cytoscape.service.util.CyServiceRegistrar;

public class ExitPluginAction extends AbstractCyAction {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private CyServiceRegistrar cyServiceRegistrar;

	public ExitPluginAction(CyServiceRegistrar cyServiceRegistrar, String parentMenu, String menuName) {
		super(menuName);
		setPreferredMenu(parentMenu);

		this.cyServiceRegistrar = cyServiceRegistrar;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		cyServiceRegistrar.unregisterService(cyServiceRegistrar.getService(LoyalPointPanel.class), CytoPanelComponent.class);
	}
}
