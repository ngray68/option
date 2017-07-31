package com.ngray.option.ui;

import java.awt.EventQueue;
import java.awt.HeadlessException;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JTextField;
import com.ngray.option.Log;
import com.ngray.option.financialinstrument.FinancialInstrument;

import net.miginfocom.swing.MigLayout;

public class ScenarioDefinitionDialog {
	
	private final MainUI parentUI;
	
	private JDialog dialog;
	
	private JComboBox<FinancialInstrument> underlying;
	private JComboBox<FinancialInstrument> scenarioType;
	private JTextField baseCase;
	private JTextField increment;
	private JTextField range;
	private JButton ok;
	private JButton cancel;
	
	public ScenarioDefinitionDialog(MainUI parentUI) {
		this.parentUI = parentUI;
		createComponents();
		addListeners();
	}
	
	/**
	 * Show this dialog box
	 */
	public void show() {
		EventQueue.invokeLater(()-> {
			try {
				dialog.setVisible(true);
			} catch (HeadlessException e) {
				Log.getLogger().error(e.getMessage(), e);
			}
		});
	}
	
	private void addListeners() {
		// TODO Auto-generated method stub
		
	}

	private void createComponents() {
		dialog = new JDialog(parentUI.getParentFrame(), "Scenario");
		dialog.setModalityType(JDialog.DEFAULT_MODALITY_TYPE);
		
		JLabel underlyingLabel = new JLabel("Underlying:");
		underlying = new JComboBox<>();
		
		JLabel typeLabel = new JLabel("Scenario Type:");
		scenarioType = new JComboBox<>();
		
		JLabel baseCaseLabel = new JLabel("Base Case:");
		baseCase = new JTextField();
		
		JLabel incrementLabel = new JLabel("Increment:");
		increment = new JTextField();
		
		JLabel rangeLabel = new JLabel("Range:");
		range = new JTextField();
		
		ok = new JButton("OK");
		cancel = new JButton("Cancel");
		
		
		dialog.setLayout(new MigLayout("", "[][][]", "[][][][][][][]"));
		dialog.add(underlyingLabel, "Cell 0 0,span,grow");
		dialog.add(underlying, "Cell 0 1,span,grow");
		dialog.add(typeLabel, "Cell 0 2,span,grow");
		dialog.add(scenarioType, "Cell 0 3,span,grow");
		dialog.add(baseCaseLabel, "Cell 0 4");
		dialog.add(incrementLabel, "Cell 1 4");
		dialog.add(rangeLabel, "Cell 2 4");
		dialog.add(baseCase, "Cell 0 5,grow");
		dialog.add(increment, "Cell 1 5,grow");
		dialog.add(range, "Cell 2 5,grow");
		dialog.add(ok, "Cell 1 6");
		dialog.add(cancel, "Cell 2 6");
		dialog.pack();
		
	}

	private void onOK() {
		
	}
	
	private void onCancel() {
		
	}

}
