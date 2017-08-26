package com.ngray.option.ui.components;

import java.awt.CardLayout;
import java.awt.EventQueue;
import java.awt.HeadlessException;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;

import com.ngray.option.Log;
import net.miginfocom.swing.MigLayout;

public class Wizard {
	
	private final JDialog dialog;
	private final WizardController controller;
	private final JPanel panel;
	private JButton back;
	private JButton next;
	private JButton finish;
	private JButton cancel;
	
	public Wizard(JFrame parentFrame, String title, WizardController controller) {
		this.dialog = new JDialog(parentFrame, title);
		this.controller = controller;
		panel = new JPanel(new CardLayout());
		back = new JButton("Back");
		next = new JButton("Next");
		finish = new JButton("Finish");
		cancel = new JButton("Cancel");
		initDialog();
		addListeners();
	}
	
	public void show() {
		EventQueue.invokeLater(()-> {
			try {
				dialog.setVisible(true);
			} catch (HeadlessException e) {
				Log.getLogger().error(e.getMessage(), e);
			}
		});
	}
	
	private void initDialog() {
		back.setEnabled(false);
		next.setEnabled(true);
		finish.setEnabled(false);
		cancel.setEnabled(true);
		dialog.setLayout(new MigLayout("", "[][][][]", "[][]"));
		
		WizardPanel thisPanel = controller.getRootPanel();
		while (thisPanel != null) {
			panel.add(thisPanel.getPanel(), thisPanel.getName());
			thisPanel = thisPanel.getNext();
		}
		 
		((CardLayout)panel.getLayout()).show(panel, controller.getRootPanel().getName());
		dialog.add(panel, "cell 0 0, span, grow");
		dialog.add(back, "cell 0 1,grow");
		dialog.add(next, "cell 1 1,grow");
		dialog.add(finish, "cell 2 1,grow");
		dialog.add(cancel, "cell 3 1,grow");
		dialog.pack();
	}
	
    private void addListeners() {
    	back.addActionListener(event -> showPanel(controller.getPreviousPanel()));
    	next.addActionListener(event -> showPanel(controller.getNextPanel()));
    	
    	cancel.addActionListener(event -> dialog.dispose());
    }
    
    private void showPanel(WizardPanel newPanel) {
    	if (newPanel == null) return;
    	
    	if (newPanel.getNext() == null) {
    		next.setEnabled(false);
    	} else {
    		next.setEnabled(true);
    	}
    	if (newPanel.getPrevious() == null) {
    		back.setEnabled(false);
    	} else {
    		back.setEnabled(true);
    	}
    	
    	controller.setCurrentPanel(newPanel);
    	((CardLayout)panel.getLayout()).show(panel, newPanel.getName());
    }
}
