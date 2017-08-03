package com.ngray.option.ui;

import java.awt.EventQueue;
import java.awt.HeadlessException;
import java.time.LocalDate;
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
import com.ngray.option.analysis.scenario.Scenario;
import com.ngray.option.analysis.scenario.ScenarioDefinition;
import com.ngray.option.analysis.scenario.ScenarioDefinition.Type;
import com.ngray.option.analysis.scenario.UnderlyingPriceScenario;
import com.ngray.option.financialinstrument.FinancialInstrument;
import net.miginfocom.swing.MigLayout;

/**
 * Dialog box in which users can define and run scenarios over their positions.
 * eg. by specifying an underlying, a central price, price range and price increments,
 * you can see the effect of underlying price change on the pnl and risk for all
 * your  open positions in that underlying
 * @author nigelgray
 *
 */
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
	
	private ScenarioDefinition scenarioDefinition;
	
	private final static double PRICE_INCREMENT_DEFAULT = 10.0; 
	private final static double PRICE_RANGE_DEFAULT = 100.0;
	
	public ScenarioDefinitionDialog(MainUI parentUI) {
		this.parentUI = parentUI;
		scenarioDefinition = new ScenarioDefinition(null, Type.UNDERLYING, PRICE_INCREMENT_DEFAULT, 0, PRICE_RANGE_DEFAULT);
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
		run.setEnabled(false);
		
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
		scenarioDefinition.setInstrument(((NameAdapter)underlying.getSelectedItem()).getInstrument());
		validate();
	}
	
	private void onSelectScenarioType() {
		Log.getLogger().debug("ScenarioDefinitionDialog::onSelectScenarioType");
		scenarioDefinition.setType((Type)scenarioType.getSelectedItem());
		validate();
	}
	
	private void onEditBaseCase() {
		Log.getLogger().debug("ScenarioDefinitionDialog::onEditBaseCase");
		double baseCaseValue = Double.parseDouble(baseCase.getText());
		scenarioDefinition.setBaseValue(baseCaseValue);
		validate();
	}
	
	private void onEditIncrement() {
		Log.getLogger().debug("ScenarioDefinitionDialog::onEditIncrement");
		double incValue = Double.parseDouble(increment.getText());
		scenarioDefinition.setIncrement(incValue);		validate();
	}
	
	private void onEditRange() {
		Log.getLogger().debug("ScenarioDefinitionDialog::onEditRange");
		double rangeValue = Double.parseDouble(range.getText());
		scenarioDefinition.setRange(rangeValue);
		validate();
	}

	private void onRun() {
		Log.getLogger().debug("ScenarioDefinitionDialog::onRun");
		runScenario();
		dialog.setVisible(false);
	}

	private void onCancel() {
		Log.getLogger().debug("ScenarioDefinitionDialog::onCancel");
		dialog.setVisible(false);
	}
	
	private void validate() {
		if (scenarioDefinition.validate()) {
			run.setEnabled(true);
		} else {
			run.setEnabled(false);
		}
	}

	
	private void runScenario() {
		// run the scenario over each position in the selected underlying
		Scenario scenario = null;
		if (scenarioDefinition.getType() == Type.UNDERLYING) {
			// the scenario definition is owned by this dialog so we pass a copy to the actual scenario
			scenario = new UnderlyingPriceScenario(scenarioDefinition.copy(), LocalDate.now());
			scenario.evaluate();
			// display the results
			if (parentUI.getScenarioView() == null) {
				parentUI.setScenarioView(new ScenarioView(parentUI));	
			}
			parentUI.getScenarioView().addScenario(scenario);
			parentUI.getScenarioView().show();
		} else {
			// TODO - IMPLIED VOL Scenario
			// for now we just return
			return;
		}
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
