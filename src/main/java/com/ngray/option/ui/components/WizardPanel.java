package com.ngray.option.ui.components;

import javax.swing.JPanel;

public class WizardPanel {
	
	private final JPanel panel;
	private WizardPanel previous;
	private WizardPanel next;
	private String name;
	
	public WizardPanel(JPanel panel, String name) {
		this.panel = panel;
		this.previous = null;
		this.next = null;
		this.name = name;
	}
	
	public WizardPanel setNext(WizardPanel next) {
		this.next = next;
		return next;
	}
	
	public WizardPanel setPrevious(WizardPanel previous) {
		this.previous = previous;
		return previous;
	}

	public JPanel getPanel() {
		return panel;
	}

	public WizardPanel getPrevious() {
		return previous;
	}

	public WizardPanel getNext() {
		return next;
	}

	public String getName() {
		return name;
	}
}
