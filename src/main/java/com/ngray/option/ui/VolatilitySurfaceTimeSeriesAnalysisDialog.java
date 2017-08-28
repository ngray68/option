package com.ngray.option.ui;

import com.ngray.option.Log;
import com.ngray.option.mongo.Mongo;
import com.ngray.option.mongo.MongoConstants;
import com.ngray.option.mongo.Price.SnapshotType;
import com.ngray.option.ui.components.DocumentAdapter;
import com.ngray.option.ui.components.Wizard;
import com.ngray.option.ui.components.WizardController;
import com.ngray.option.ui.components.WizardPanel;
import com.ngray.option.ui.volsurfacetimeseries.AnalysisOptionsWizardModel;
import com.ngray.option.ui.volsurfacetimeseries.VolatilitySurfaceChoiceReviewWizardModel;
import com.ngray.option.ui.volsurfacetimeseries.VolatilitySurfaceChooserWizardModel;
import com.ngray.option.volatilitysurface.VolatilitySurface;
import java.text.DateFormat;
import java.text.ParseException;
import java.time.LocalDate;
import java.util.Calendar;
import java.util.Properties;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField.AbstractFormatter;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;

import org.jdatepicker.DateModel;
import org.jdatepicker.impl.JDatePanelImpl;
import org.jdatepicker.impl.JDatePickerImpl;
import org.jdatepicker.impl.UtilDateModel;

import com.mongodb.client.DistinctIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import net.miginfocom.swing.MigLayout;

public class VolatilitySurfaceTimeSeriesAnalysisDialog {
	
	private final MainUI parentUI;
	private Wizard wizard;
	
	private JPanel volSurfaceChooserPanel;
	private JComboBox<String> volSurfaceNameChooser;
	private JComboBox<SnapshotType> snapshotTypeChooser;
	private JDatePickerImpl fromDatePicker;
	private JDatePickerImpl toDatePicker;
	private VolatilitySurfaceChooserWizardModel volSurfaceChooserModel;
	
	private JPanel volSurfaceChoiceReviewPanel;
	private VolatilitySurfaceChoiceReviewWizardModel volSurfaceChoiceReviewModel;
	
	
	private JPanel analysisPanel;
	private JCheckBox analyzeATM;
	private JCheckBox analyzeOTM;
	private JCheckBox analyzeITM;
	private JTextField otmOffset;
	private JTextField itmOffset;
	private JTextField daysToExpiry;
	private JCheckBox minValue;
	private JCheckBox maxValue;
	private JCheckBox average;
	private JCheckBox fiveDay;
	private JCheckBox thirtyDay;
	private JCheckBox ninetyDay;
	private AnalysisOptionsWizardModel analysisOptionsModel;
	
	
	
	
	public VolatilitySurfaceTimeSeriesAnalysisDialog(MainUI parentUI) {
		this.parentUI = parentUI;
		initialize();
	}
	
	public void show() {
		wizard.show();
	}
	
	private void initialize() {
		createVolSurfaceChooserPanel();
		createVolSurfaceChoiceReviewPanel();
		createAnalysisPanel();
		
		WizardPanel volSurfaceChooserWizardPanel = new WizardPanel(volSurfaceChooserPanel, "FIRST", volSurfaceChooserModel);
		WizardPanel volSurfaceChoiceReviewWizardPanel = new WizardPanel(volSurfaceChoiceReviewPanel, "SECOND", volSurfaceChoiceReviewModel);
		WizardPanel analysisWizardPanel = new WizardPanel(analysisPanel, "THIRD", analysisOptionsModel);
		volSurfaceChooserWizardPanel.setNext(volSurfaceChoiceReviewWizardPanel)
									.setNext(analysisWizardPanel)
									.setPrevious(volSurfaceChoiceReviewWizardPanel)
									.setPrevious(volSurfaceChooserWizardPanel);
		WizardController controller = new WizardController(volSurfaceChooserWizardPanel);
		wizard = new Wizard(parentUI.getParentFrame(), "Time Series Analysis", controller);
	}
	
	private void createAnalysisPanel() {
		analysisPanel = new JPanel(new MigLayout("", "[][grow][grow]", "[][][][][][][]"));
	
		analyzeATM = new JCheckBox("ATM");
		analyzeOTM = new JCheckBox("OTM");
		analyzeITM = new JCheckBox("ITM");
		analysisPanel.add(new JLabel("Analysis Options"), "cell 0 0");
		analysisPanel.add(analyzeATM, "cell 0 1");
		analysisPanel.add(analyzeOTM, "cell 0 2");
		analysisPanel.add(analyzeITM, "cell 0 3");
		
		JLabel otmOffsetLabel = new JLabel("OTM Offset");
		JLabel itmOffsetLabel = new JLabel("ITM Offset");
		otmOffset = new JTextField();
		otmOffset.setEnabled(false);
		itmOffset = new JTextField();
		itmOffset.setEnabled(false);
		analysisPanel.add(otmOffsetLabel, "cell 1 2");
		analysisPanel.add(itmOffsetLabel, "cell 1 3");
		analysisPanel.add(otmOffset, "cell 2 2,grow");
		analysisPanel.add(itmOffset, "cell 2 3,grow");
		
		JLabel daysToExpiryLabel = new JLabel("Days To Expiry");
		daysToExpiry = new JTextField();
		analysisPanel.add(daysToExpiryLabel, "cell 1 4");
		analysisPanel.add(daysToExpiry, "cell 2 4, grow");
		
		minValue = new JCheckBox("Min IV");
		maxValue = new JCheckBox("Max IV");
		average = new JCheckBox("Mean IV");
		analysisPanel.add(minValue, "cell 0 5");
		analysisPanel.add(maxValue, "cell 1 5");
		analysisPanel.add(average, "cell 2 5");
		
		analysisPanel.add(new JLabel("Moving Averages"), "cell 0 6");
		fiveDay = new JCheckBox("5-day");
		thirtyDay = new JCheckBox("30-day");
		ninetyDay = new JCheckBox("90-day");
		analysisPanel.add(fiveDay, "cell 0 7");
		analysisPanel.add(thirtyDay, "cell 1 7");
		analysisPanel.add(ninetyDay, "cell 2 7");
		
		analysisOptionsModel = new AnalysisOptionsWizardModel(volSurfaceChooserModel);
		addAnalysisPanelListeners();
	}

	private void createVolSurfaceChoiceReviewPanel() {
		volSurfaceChoiceReviewPanel = new JPanel();	
		volSurfaceChoiceReviewModel = new VolatilitySurfaceChoiceReviewWizardModel(volSurfaceChoiceReviewPanel);
	}

	private void createVolSurfaceChooserPanel() {
		volSurfaceChooserPanel = new JPanel(new MigLayout("", "[][]", "[][][][]"));
		volSurfaceChooserModel = new VolatilitySurfaceChooserWizardModel();
		
		JLabel volSurfaceNameChooserTitle = new JLabel("Volatility Surface: ");
		volSurfaceNameChooser = new JComboBox<>();
		volSurfaceChooserPanel.add(volSurfaceNameChooserTitle, "cell 0 0");
		volSurfaceChooserPanel.add(volSurfaceNameChooser, "cell 1 0,span,grow");
		
		JLabel fromDateLabel = new JLabel("From: ");
		fromDatePicker = createDatePicker();
		volSurfaceChooserPanel.add(fromDateLabel, "cell 0 1");
		volSurfaceChooserPanel.add(fromDatePicker, "cell 1 1,span,grow");
		
		JLabel toDateLabel = new JLabel("To: ");
		toDatePicker = createDatePicker();
		volSurfaceChooserPanel.add(toDateLabel, "cell 0 2");
		volSurfaceChooserPanel.add(toDatePicker, "cell 1 2,span,grow");
		
		JLabel snapshotTypeLabel = new JLabel("Snapshot Type: ");
		snapshotTypeChooser = new JComboBox<>();
		for (SnapshotType type: SnapshotType.values()) {
			snapshotTypeChooser.addItem(type);
		}
		volSurfaceChooserPanel.add(snapshotTypeLabel);
		volSurfaceChooserPanel.add(snapshotTypeChooser, "cell 1 3, span,grow");
		
		MongoCollection<VolatilitySurface> collection = Mongo.getMongoDatabase(MongoConstants.DATABASE_NAME).getCollection(MongoConstants.VOLATILITY_SURFACE_COLLECTION, VolatilitySurface.class);
		DistinctIterable<String>  result = collection.distinct(VolatilitySurface.NAME_COL, String.class);
		MongoCursor<String> iter = result.iterator();
		while (iter.hasNext()) {
			volSurfaceNameChooser.addItem(iter.next());
		}
		
		addVolSurfaceChooserPanelListeners();
	}
	
	private JDatePickerImpl createDatePicker() {
		UtilDateModel model = new UtilDateModel();
		Properties p = new Properties();
		p.put("text.today", "Today");
		p.put("text.month", "Month");
		p.put("text.year", "Year");
		JDatePanelImpl datePanel = new JDatePanelImpl(model, p);
		@SuppressWarnings("serial")
		JDatePickerImpl datePicker = new JDatePickerImpl(datePanel, new AbstractFormatter() {

			@Override
			public Object stringToValue(String text) throws ParseException {
				return null;
			}

			@Override
			public String valueToString(Object value) throws ParseException {
				if (value == null || !(value instanceof Calendar)) return null;
				return DateFormat.getInstance().format(((Calendar)value).getTime());
			}});
	
		return datePicker;
	}
	
	
	private void addAnalysisPanelListeners() {
		analyzeATM.addActionListener(
				e -> setATMOffset(analyzeATM.isSelected())
			);
		
		analyzeOTM.addActionListener(
				e -> setOTMOffsetField(analyzeOTM.isSelected())
			);
		
		analyzeITM.addActionListener(
				e -> setITMOffsetField(analyzeITM.isSelected())
			);
		
		daysToExpiry.getDocument().addDocumentListener(
				new DocumentAdapter() {

					@Override
					public void update(DocumentEvent e) {
						setDaysToExpiry(daysToExpiry.getText());
					}}
			);
		
		maxValue.addActionListener(
				e -> setMaxValue(maxValue.isSelected())
			);
		
		minValue.addActionListener(
				e -> setMinValue(minValue.isSelected())
			);
		
		average.addActionListener(
				e -> setMean(average.isSelected())
			);
		
		fiveDay.addActionListener(
				e -> setFiveDayMovingAverage(fiveDay.isSelected())
			);
		
		thirtyDay.addActionListener(
				e -> setThirtyDayMovingAverage(thirtyDay.isSelected())
			);
		
		ninetyDay.addActionListener(
				e -> setNinetyDayMovingAverage(ninetyDay.isSelected())
			);
		
		// TODO the OTM/ITM offset fields
	
	}

	private void setDaysToExpiry(String text) {
		if (text == null) return;
		try {
	        double value = Double.parseDouble(text);
	        analysisOptionsModel.setDaysToExpiry(value);
	    } catch (NumberFormatException e) {
	        // we can throw this away silently
	    	// will occur if user enters non-numeric data - which we can ignore
	    }
	}

	private void setNinetyDayMovingAverage(boolean selected) {
		if (selected) {
			analysisOptionsModel.addMovingAverage(90);
		} else {
			analysisOptionsModel.removeMovingAverage(90);
		}
		wizard.setFinishEnabled(analysisOptionsModel.validate());
	}

	private void setThirtyDayMovingAverage(boolean selected) {
		if (selected) {
			analysisOptionsModel.addMovingAverage(30);
		} else {
			analysisOptionsModel.removeMovingAverage(30);
		}
		wizard.setFinishEnabled(analysisOptionsModel.validate());	}

	private void setFiveDayMovingAverage(boolean selected) {
		if (selected) {
			analysisOptionsModel.addMovingAverage(5);
		} else {
			analysisOptionsModel.removeMovingAverage(5);
		}
		wizard.setFinishEnabled(analysisOptionsModel.validate());
	}

	private void setMean(boolean selected) {
		analysisOptionsModel.setCalcMean(selected);
		wizard.setFinishEnabled(analysisOptionsModel.validate());
	}

	private void setMinValue(boolean selected) {
		analysisOptionsModel.setCalcMinValue(selected);
		wizard.setFinishEnabled(analysisOptionsModel.validate());
	}

	private void setMaxValue(boolean selected) {
		analysisOptionsModel.setCalcMaxValue(selected);
		wizard.setFinishEnabled(analysisOptionsModel.validate());
	}

	private void setITMOffsetField(boolean selected) {
		itmOffset.setEnabled(selected);
		wizard.setFinishEnabled(analysisOptionsModel.validate());
	}

	private void setOTMOffsetField(boolean selected) {
		otmOffset.setEnabled(selected);
		wizard.setFinishEnabled(analysisOptionsModel.validate());
	}

	private void setATMOffset(boolean set) {
		if (set) {
			analysisOptionsModel.addAtmOffset(0.0);
		} else {
			analysisOptionsModel.removeAtmOffset(0.0);
		}
		wizard.setFinishEnabled(analysisOptionsModel.validate());
	}

	private void addVolSurfaceChooserPanelListeners() {
		DateModel<?> fromDateModel = fromDatePicker.getModel();
		DateModel<?> toDateModel = toDatePicker.getModel();
		volSurfaceNameChooser.addActionListener(
				e -> setVolSurfaceName(volSurfaceNameChooser.getSelectedItem().toString())	
			);
		
		fromDatePicker.addActionListener(
			    e -> setFromDate( LocalDate.of(fromDateModel.getYear(), fromDateModel.getMonth() + 1, fromDateModel.getDay()))
			);
		
		toDatePicker.addActionListener(
			    e -> setToDate( LocalDate.of(toDateModel.getYear(), toDateModel.getMonth() + 1, toDateModel.getDay()))
			);
		
		snapshotTypeChooser.addActionListener(
				e -> setSnapshotType((SnapshotType)snapshotTypeChooser.getSelectedItem())
			);			
	}

	private void setSnapshotType(SnapshotType snapshotType) {
		volSurfaceChooserModel.setSnapshotType(snapshotType);
		wizard.setNextEnabledIfNextExists(volSurfaceChooserModel.validate());
	}

	private void setToDate(LocalDate date) {
		volSurfaceChooserModel.setToDate(date);
		wizard.setNextEnabledIfNextExists(volSurfaceChooserModel.validate());	
	}

	private void setFromDate(LocalDate date) {
		volSurfaceChooserModel.setFromDate(date);
		wizard.setNextEnabledIfNextExists(volSurfaceChooserModel.validate());
	}

	private void setVolSurfaceName(String string) {
		volSurfaceChooserModel.setVolSurfaceName(string);
		wizard.setNextEnabledIfNextExists(volSurfaceChooserModel.validate());
	}
}
