package com.ngray.option.ui.components;

public class WizardController {
	
	
	private final WizardPanel rootPanel;
	private WizardPanel currentPanel;
	
	public WizardController(WizardPanel rootPanel) {
		this.rootPanel = rootPanel;
		this.currentPanel = rootPanel;
	}
	
	public WizardPanel getNextPanel() {
		return currentPanel.getNext();
	}
	
	public WizardPanel getPreviousPanel() {
		return currentPanel.getPrevious();
	}

	public WizardPanel getRootPanel() {
		return rootPanel;
	}
	
	public WizardPanel getCurrentPanel() {
		return currentPanel;
	}
	
	public void setCurrentPanel(WizardPanel panel) {
		this.currentPanel = panel;
	}
	
	
	

}
