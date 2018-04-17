package com.cyloyalpoint.internal;

import javax.swing.JFrame;
import javax.swing.JLabel;

import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;

public class AboutTask extends AbstractTask {

	public AboutTask() {
		JFrame frame = new JFrame();

		frame.setSize(400, 500);
		frame.setLayout(null);
		frame.setVisible(true);
		frame.setLocationRelativeTo(null);
		
		JLabel label = new JLabel("Author: Kien Tran");
		label.setBounds(50, 50, 200, 30);

		frame.add(label);
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
	}

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		taskMonitor.setTitle("About");
	}

}
