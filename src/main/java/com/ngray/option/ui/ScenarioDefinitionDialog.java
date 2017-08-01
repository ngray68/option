package com.ngray.option.ui;

import java.awt.EventQueue;
import java.awt.HeadlessException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import com.ngray.option.Log;
import com.ngray.option.RiskEngine;
import com.ngray.option.analysis.scenario.InvalidScenarioDefinitionException;
import com.ngray.option.analysis.scenario.Scenario;
import com.ngray.option.analysis.scenario.ScenarioDefinition;
import com.ngray.option.analysis.scenario.ScenarioDefinition.Type;
import com.ngray.option.financialinstrument.FinancialInstrument;
import com.ngray.option.position.PositionService;

import net.miginfocom.swing.MigLayout;

public class ScenarioDefinitionDialog {
	
	private final MainUI parentUI;
	
	private JDialog dialog;
	
	private JComboBox<NameAdapter> underlying;
	private JComboBox<ScenarioDefinition.Type> scenarioType;
	private JTextField baseCase;
	private JTextField increment;
	private JTextField range;
	private JButton run;
	private JButton cancel;
	
	private FinancialInstrument selectedUnderlying;
	private ScenarioDefinition scenarioDefinition;
	
	private final static double PRICE_INCREMENT_DEFAULT = 10.0; 
	private final static double PRICE_RANGE_DEFAULT = 200.0;
	
	public ScenarioDefinitionDialog(MainUI parentUI) {
		this.parentUI = parentUI;
		selectedUnderlying = null;
		scenarioDefinition = new ScenarioDefinition(Type.UNDERLYING, PRICE_INCREMENT_DEFAULT, 0, PRICE_RANGE_DEFAULT);
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
		
		underlying.addActionListener(
				event -> onSelectUnderlying()
			);
		
		scenarioType.addActionListener(
				event -> onSelectScenarioType()
			);
		
		baseCase.getDocument().addDocumentListener(
				new DocumentAdapter() {
					@Override
					public void update(DocumentEvent e) {
						onEditBaseCase();
	
					}}
				);
		
		increment.getDocument().addDocumentListener(
				new DocumentAdapter() {
					@Override
					public void update(DocumentEvent e) {
						onEditIncrement();
	
					}}
				);
		
		range.getDocument().addDocumentListener(
				new DocumentAdapter() {
					@Override
					public void update(DocumentEvent e) {
						onEditRange();
	
					}}
				);
			
		run.addActionListener(
				event -> onRun()
			);
		
		cancel.addActionListener(
				event -> onCancel()
			);
		
	}

	private void createComponents() {
		dialog = new JDialog(parentUI.getParentFrame(), "Scenario");
		dialog.setModalityType(JDialog.DEFAULT_MODALITY_TYPE);
		
		JLabel underlyingLabel = new JLabel("Underlying:");
		Set<FinancialInstrument> underlyings = RiskEngine.getPositionService().getUnderlyings();
		underlying = new JComboBox<>();
		underlying.addItem(new NameAdapter()); // empty entry
		underlyings.forEach(instrument->underlying.addItem(new NameAdapter(instrument)));
		
		JLabel typeLabel = new JLabel("Scenario Type:");
		scenarioType = new JComboBox<>();
		scenarioType.addItem(Type.UNDERLYING);
		scenarioType.addItem(Type.IMPLIED_VOL);
		scenarioType.setSelectedItem(Type.UNDERLYING); // default
		
		JLabel baseCaseLabel = new JLabel("Base Case:");
		baseCase = new JTextField();
		
		JLabel incrementLabel = new JLabel("Increment:");
		increment = new JTextField();
		increment.setText(Double.toString(PRICE_INCREMENT_DEFAULT));
		
		JLabel rangeLabel = new JLabel("Range:");
		range = new JTextField();
		range.setText(Double.toString(PRICE_RANGE_DEFAULT));
		
		run = new JButton("Run");
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
		dialog.add(run, "Cell 1 6");
		dialog.add(cancel, "Cell 2 6");
		dialog.pack();
		
	}

	private void onSelectUnderlying() {
		Log.getLogger().debug("ScenarioDefinitionDialog::onSelectUnderlying");
		selectedUnderlying = ((NameAdapter)underlying.getSelectedItem()).getInstrument();
	}
	
	private void onSelectScenarioType() {
		Log.getLogger().debug("ScenarioDefinitionDialog::onSelectScenarioType");
		scenarioDefinition.setType((Type)scenarioType.getSelectedItem());
	}
	
	private void onEditBaseCase() {
		Log.getLogger().debug("ScenarioDefinitionDialog::onEditBaseCase");
		double baseCaseValue = Double.parseDouble(baseCase.getText());
		scenarioDefinition.setBaseValue(baseCaseValue);
	}
	
	private void onEditIncrement() {
		Log.getLogger().debug("ScenarioDefinitionDialog::onEditIncrement");
		double incValue = Double.parseDouble(increment.getText());
		scenarioDefinition.setBaseValue(incValue);
	}
	
	private void onEditRange() {
		Log.getLogger().debug("ScenarioDefinitionDialog::onEditRange");
		double rangeValue = Double.parseDouble(range.getText());
		scenarioDefinition.setBaseValue(rangeValue);
	}
	
	private void onRun() {
		Log.getLogger().debug("ScenarioDefinitionDialog::onRun");
		runScenario();
		dialog.dispose();
	}

	private void onCancel() {
		Log.getLogger().debug("ScenarioDefinitionDialog::onCancel");
		dialog.dispose();
	}
	
	private void runScenario() {
		// run the scenario over each position in the selected underlying
		List<Scenario> scenarios = new ArrayList<>();
		PositionService positionService = RiskEngine.getPositionService();
		positionService.getPositions(selectedUnderlying).forEach(
				position -> { scenarios.add(new Scenario(position.getId() + "-scenario", position, scenarioDefinition, LocalDate.now())); }
				);
		
		// evaluate the scenario
		scenarios.forEach(scenario -> {
			try {
				scenario.evaluate();
			} catch (InvalidScenarioDefinitionException e) {
				Log.getLogger().error(e.getMessage(), e);
			}
		});
		// register for position updates which may change the scenario results
		
		// display the results
		new ScenarioView(parentUI, scenarios).show();	
	}
	
	// Adapter to simplify listeners for text fields
	public abstract class DocumentAdapter implements DocumentListener {

		public abstract void update(DocumentEvent e);
		
		@Override
		public void insertUpdate(DocumentEvent e) {
			update(e);
		}

		@Override
		public void removeUpdate(DocumentEvent e) {
			update(e);
		}

		@Override
		public void changedUpdate(DocumentEvent e) {
			update(e);
		}
		
	}
	// Adapter to show a friendly name for securities in the underlyings dialog box
	public class NameAdapter {
			
			private final FinancialInstrument instrument;
			
			public NameAdapter() {
				instrument = null;
			}
			
			public NameAdapter(FinancialInstrument instrument) {
				this.instrument = instrument;
			}
			
			public FinancialInstrument getInstrument() {
				return instrument;
			}
			
			@Override
			public String toString() {
				if (instrument != null) {
					return instrument.getName() + " " + instrument.getIGMarket().getExpiry();
				}
				return "";
			}
		}

}
