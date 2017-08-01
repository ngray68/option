package com.ngray.option.ui;

import java.awt.Component;
import java.awt.GridLayout;
import java.beans.PropertyVetoException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JInternalFrame;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.InternalFrameListener;

import com.ngray.option.Log;
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
	
	private final MainUI parentUI;
	private JInternalFrame parentInternalFrame;
	private final List<Scenario> scenarios;
	
	public ScenarioView(MainUI parentUI, List<Scenario> scenarios) {
		this.parentUI = parentUI;
		this.scenarios = new ArrayList<>(scenarios);
	}
	
	public void show() {
		createParentInternalFrame();
		createScenarioTables();
		parentUI.getDesktopPane().add(parentInternalFrame);
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
	
	private void createScenarioTables() {
		scenarios.forEach(
			scenario -> createScenarioTable(scenario)
			);
	}
	
	private void createScenarioTable(Scenario scenario) {
		
		JTabbedPane tabbedPane = new JTabbedPane();
		for (RiskMeasure riskMeasure : RiskMeasure.values()) {
			ScenarioTableModel model = new ScenarioTableModel(scenario, riskMeasure);
			JTable table = new JTable(model);
			tabbedPane.add(riskMeasure.toString(), new JScrollPane(table));
		}
		
		String scenarioName = "Test";
		InternalFrameListener listener = new InternalFrameAdapter() {
			@Override
			public void internalFrameClosed(InternalFrameEvent e) {
				// TODO - clean up
				e.getInternalFrame().removeInternalFrameListener(this);;
			}
		};
		JInternalFrame frame = Frames.createJInternalFrame(scenarioName, listener, tabbedPane);
		parentInternalFrame.add(frame);
	}

	private void onClose() {
		Log.getLogger().debug("Closing scenario view....");
		for (Component component : parentInternalFrame.getContentPane().getComponents()) {
			try {
				if (component instanceof JInternalFrame) {
					Log.getLogger().debug("Closing " + ((JInternalFrame)component).getTitle());
					((JInternalFrame)component).setClosed(true);
				}
			} catch (PropertyVetoException e) {
				Log.getLogger().warn(e.getMessage(), true);
			}
		}
		
	}
}
