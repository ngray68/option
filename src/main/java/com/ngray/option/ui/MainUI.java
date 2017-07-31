package com.ngray.option.ui;

import java.awt.EventQueue;
import java.awt.HeadlessException;
import java.awt.LayoutManager;
import javax.swing.JDesktopPane;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import com.ngray.option.Log;

/**
 * This is the main UI window of the application
 * @author nigelgray
 *
 */
public class MainUI {
	
	private JFrame parentFrame;
	private JDesktopPane desktopPane;
	private JMenuBar mainMenu;
	private JMenu analysisMenu;
	private JMenu riskMenu;
	
	private PositionRiskView positionRiskView;
	
	
	public MainUI() {
		initialize();
	}
	
	public JFrame getParentFrame() {
		return parentFrame;
	}
	
	public JDesktopPane getDesktopPane() {
		return desktopPane;
	}
	
	public JMenuBar getMenuBar() {
		return mainMenu;
	}
	
	public void setLayout(LayoutManager layout) {
		desktopPane.setLayout(layout);
	}

	public void setFrameTitle(String title) {
		parentFrame.setTitle(title);
	}
	
	
	public void show() {
		EventQueue.invokeLater(()-> {
			try {
				parentFrame.setVisible(true);
			} catch (HeadlessException e) {
				Log.getLogger().error(e.getMessage(), e);
			}
		});
	}
	

	// Private helper methods	
	private void initialize() {
		parentFrame = new JFrame();
		parentFrame.setExtendedState(JFrame.MAXIMIZED_BOTH);
		desktopPane = new JDesktopPane();
		parentFrame.setContentPane(desktopPane);
		createMenuBar();
	}
	
	private void createMenuBar() {
		System.setProperty("apple.laf.useScreenMenuBar", "true");
		mainMenu = new JMenuBar();
		analysisMenu = new JMenu("Analysis");
		JMenuItem optionLadder = new JMenuItem("Option Ladder...");
		analysisMenu.add(optionLadder);
		
		optionLadder.addActionListener(
				(event) -> { showOptionLadderDialog(); }
				);
		
		
		riskMenu = new JMenu("Risk");
		JMenuItem showPositions = new JMenuItem("Show Positions");
		riskMenu.add(showPositions);
		
		showPositions.addActionListener(
				(event) -> { showOpenPositions(); }
				);
		
		JMenuItem runScenario = new JMenuItem("Run Scenario...");
		riskMenu.add(runScenario);
		
		runScenario.addActionListener(
				(event) -> runScenario()
		);
		
		mainMenu.add(analysisMenu);
		mainMenu.add(riskMenu);
		parentFrame.setJMenuBar(mainMenu);
	}

	private void showOpenPositions() {
		Log.getLogger().debug("Showing open positions");
		if (positionRiskView == null) {
			positionRiskView = new PositionRiskView(this);
		}
		positionRiskView.showPositions();
	}
		
	private void showOptionLadderDialog() {
		new OptionLadderDialog(this).show();
	}
	
	private void runScenario() {
		new ScenarioDefinitionDialog(this).show();
	}

}
