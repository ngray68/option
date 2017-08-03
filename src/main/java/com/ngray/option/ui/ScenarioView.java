package com.ngray.option.ui;

import java.awt.Dimension;
import java.awt.GridLayout;
import javax.swing.JInternalFrame;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.InternalFrameListener;

import com.ngray.option.Log;
import com.ngray.option.RiskEngine;
import com.ngray.option.analysis.scenario.Scenario;

public class ScenarioView {
	
	public enum RiskMeasure {
		PNL("PnL"),
		DELTA("Delta"),
		GAMMA("Gamma"),
		VEGA("Vega"),
		THETA("Theta"),
		RHO("Rho");
		
		private String string;
		
		private RiskMeasure(String s) {
			string = s;
		}

		@Override
		public String toString() {
			return string;
		}
	};
	
	public static final int DEFAULT_WIDTH = 1000;
	public static final int DEFAULT_HEIGHT = 200;
	
	private final MainUI parentUI;
	private JInternalFrame parentInternalFrame;
	private boolean closed;
	
	private JInternalFrame lastInternalFrameCreated;
	
	public ScenarioView(MainUI parentUI) {
		this.parentUI = parentUI;
		createParentInternalFrame();
		closed = true;
	}
	
	public void show() {
		if (isClosed()) {	
			parentInternalFrame.setVisible(true);
			parentUI.getDesktopPane().add(parentInternalFrame);
			closed = false;
		}
	}
	
	private boolean isClosed() {
		return closed;
	}

	public void addScenario(Scenario scenario) {
		addToScenarioService(scenario);
		createScenarioTable(scenario);	
	}

	private void createParentInternalFrame() {
		InternalFrameListener listener = new InternalFrameAdapter() {
			@Override
			public void internalFrameClosed(InternalFrameEvent e) {
				onClose();
				e.getInternalFrame().removeInternalFrameListener(this);
			}
		};
		parentInternalFrame = Frames.createJInternalFrame("Scenarios", listener);
		parentInternalFrame.setLayout(new GridLayout(0,1));
	}

	private void createScenarioTable(Scenario scenario) {
		
		JTabbedPane tabbedPane = new JTabbedPane();
		for (RiskMeasure riskMeasure : RiskMeasure.values()) {
			ScenarioTableModel model = new ScenarioTableModel(scenario, riskMeasure);
			JTable table = new JTable(model);
			tabbedPane.add(riskMeasure.toString(), new JScrollPane(table));
			RiskEngine.getScenarioService().addListener(scenario.getName(), model);
		}
		
		String scenarioName = scenario.getName();
		InternalFrameListener listener = new InternalFrameAdapter() {
			@Override
			public void internalFrameClosed(InternalFrameEvent e) {
				e.getInternalFrame().removeInternalFrameListener(this);
				parentInternalFrame.remove(e.getInternalFrame());	
			}
		};
		JInternalFrame frame = Frames.createJInternalFrame(scenarioName, listener, tabbedPane);
		
		// TOOD - this is crude, needs revisiting
		if (lastInternalFrameCreated != null) {		
			frame.setPreferredSize(lastInternalFrameCreated.getSize());
		} else {
			frame.setPreferredSize(new Dimension(DEFAULT_WIDTH, DEFAULT_HEIGHT));
		}
		lastInternalFrameCreated = frame;
		
		parentInternalFrame.add(frame);
		parentInternalFrame.pack();
	}

	private void onClose() {
		Log.getLogger().debug("Closing scenario view....");
		parentUI.getDesktopPane().remove(parentInternalFrame);
		closed = true;
		// we don't remove the scenarios - they are remembered unless explicitly closed
	}
	
	private void addToScenarioService(Scenario scenario) {
		RiskEngine.getScenarioService().addScenario(scenario.getName(), scenario);
	}
	
	private void removeFromScenarioService(Scenario scenario) {
		RiskEngine.getScenarioService().removeScenario(scenario.getName());
	}
}
