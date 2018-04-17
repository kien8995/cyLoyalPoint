package com.cyloyalpoint.internal;

import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.swing.CyAction;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.service.util.AbstractCyActivator;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.work.ServiceProperties;
import org.cytoscape.work.TaskFactory;
import org.cytoscape.work.TaskManager;
import org.osgi.framework.BundleContext;

public class CyActivator extends AbstractCyActivator {

	private final String MENU_NAME = ".cyLoyalPoint";

	@Override
	public void start(BundleContext context) throws Exception {
		// Start plug-in in separate thread
		final ExecutorService service = Executors.newSingleThreadExecutor();
		service.submit(() -> {
			CyApplicationManager cyApplicationManager = getService(context, CyApplicationManager.class);
			CySwingApplication cySwingApplication = getService(context, CySwingApplication.class);
			CyServiceRegistrar cyServiceRegistrar = getService(context, CyServiceRegistrar.class);
			TaskManager<?, ?> taskManager = getService(context, TaskManager.class);

			// Loyal Point Panel
			LoyalPointPanelAction loyalPointPanelAction = new LoyalPointPanelAction(cyApplicationManager,
					cySwingApplication, cyServiceRegistrar, taskManager, ServiceProperties.APPS_MENU + MENU_NAME,
					"1) Compute Loyal Point");
			registerService(context, loyalPointPanelAction, CyAction.class, new Properties());

			// About Task
			AboutTaskFactory aboutTaskFactory = new AboutTaskFactory();
			Properties aboutTaskProperties = new Properties();
			aboutTaskProperties.put(ServiceProperties.PREFERRED_MENU, ServiceProperties.APPS_MENU + MENU_NAME);
			aboutTaskProperties.put(ServiceProperties.TITLE, "2) About");
			registerService(context, aboutTaskFactory, TaskFactory.class, aboutTaskProperties);

			// Exit Plugin
			ExitPluginAction exitPluginAction = new ExitPluginAction(cyServiceRegistrar,
					ServiceProperties.APPS_MENU + MENU_NAME, "3) Exit Plugin");
			registerService(context, exitPluginAction, CyAction.class, new Properties());
		});
	}
}
