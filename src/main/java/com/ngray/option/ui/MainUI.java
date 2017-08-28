package com.ngray.option.ui;

import java.awt.CardLayout;
import java.awt.EventQueue;
import java.awt.HeadlessException;
import java.awt.LayoutManager;

import javax.swing.JButton;
import javax.swing.JDesktopPane;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;

import com.ngray.option.Log;
import com.ngray.option.ui.components.Wizard;
import com.ngray.option.ui.components.WizardController;
import com.ngray.option.ui.components.WizardPanel;

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
	private ScenarioView scenarioView;
	
	// keep references to these so we can remember settings from previous uses
	// in the same session
	private OptionLadderDialog optionRiskLadderDialog;
	private ScenarioDefinitionDialog scenarioDefinitionDialog;
	private VolatilitySurfaceBuildDialog volatilitySurfaceBuildDialog;
	
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
	
	public void setScenarioView(ScenarioView view) {
		scenarioView = view;
	}
	
	public ScenarioView getScenarioView() {
		return scenarioView;
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
		JMenuItem buildVolatilitySurface = new JMenuItem("Build Volatility Surface...");
		analysisMenu.add(buildVolatilitySurface);
		JMenuItem timeSeriesAnalysis = new JMenuItem("Time Series Analysis...");
		analysisMenu.add(timeSeriesAnalysis);
		
		optionLadder.addActionListener(event -> showOptionLadderDialog());	
		buildVolatilitySurface.addActionListener(event -> showVolatilitySurfaceBuildDialog());
		timeSeriesAnalysis.addActionListener(event -> showTimeSeriesAnalysisWizard());
		
		riskMenu = new JMenu("Positions");
		JMenuItem showPositions = new JMenuItem("View Positions");
		riskMenu.add(showPositions);
		
		showPositions.addActionListener(
				(event) -> { showOpenPositions(); }
				);
		
		JMenuItem runScenario = new JMenuItem("New Scenario...");
		riskMenu.add(runScenario);
		
		runScenario.addActionListener(
				(event) -> showRunScenarioDialog()
		);
		
		JMenuItem viewScenarios = new JMenuItem("View Scenarios");
		riskMenu.add(viewScenarios);
		
		viewScenarios.addActionListener( 
			(event) -> showScenarios()
		);
		
		mainMenu.add(analysisMenu);
		mainMenu.add(riskMenu);
		parentFrame.setJMenuBar(mainMenu);
	}

	private void showTimeSeriesAnalysisWizard() {
		/*
		// test panels
		JPanel first = new JPanel();
		first.add(new  JButton("FIRST"));
		JPanel second = new JPanel();
		second.add(new  JButton("SECOND"));
		JPanel third = new JPanel();
		third.add(new  JButton("THIRD"));
		JPanel fourth = new JPanel();
		fourth.add(new  JButton("FOURTH"));
		WizardPanel firstPanel = new WizardPanel(first, "FIRST");
		WizardPanel secondPanel = new WizardPanel(second, "SECOND");
		WizardPanel thirdPanel = new WizardPanel(third, "THIRD");
		WizardPanel fourthPanel = new WizardPanel(fourth, "FOURTH");
		firstPanel.setNext(secondPanel).setNext(thirdPanel).setNext(fourthPanel);
		fourthPanel.setPrevious(thirdPanel).setPrevious(secondPanel).setPrevious(firstPanel);   
		WizardController model = new WizardController(firstPanel);
		new Wizard(parentFrame, "Volatility Surface Time Series", model).show();
		*/
		new VolatilitySurfaceTimeSeriesAnalysisDialog(this).show();
	}

	private void showVolatilitySurfaceBuildDialog() {
		Log.getLogger().debug("Opening VolatilitySurfaceBuildDialog");
		if (volatilitySurfaceBuildDialog == null) {
			volatilitySurfaceBuildDialog = new VolatilitySurfaceBuildDialog(this);
		}
		volatilitySurfaceBuildDialog.show();
	}

	private void showScenarios() {
		Log.getLogger().debug("Showing scenarios");
		if (scenarioView == null) {
			scenarioView = new ScenarioView(this);
		}
		scenarioView.show();
	}

	private void showOpenPositions() {
		Log.getLogger().debug("Showing open positions");
		if (positionRiskView == null) {
			positionRiskView = new PositionRiskView(this);
		}
		positionRiskView.showPositions();
	}
		
	private void showOptionLadderDialog() {
		if (optionRiskLadderDialog == null) {
			optionRiskLadderDialog = new OptionLadderDialog(this);
		}
		optionRiskLadderDialog.show();
	}
	
	private void showRunScenarioDialog() {
		if (scenarioDefinitionDialog == null) {
			scenarioDefinitionDialog = new ScenarioDefinitionDialog(this);
		}
		scenarioDefinitionDialog.show();
	}
}
