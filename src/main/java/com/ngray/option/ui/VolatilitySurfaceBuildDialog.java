package com.ngray.option.ui;

import java.awt.EventQueue;
import java.awt.GridLayout;
import java.awt.HeadlessException;
import java.time.LocalDate;
import java.util.Properties;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;

import org.jdatepicker.impl.JDatePanelImpl;
import org.jdatepicker.impl.JDatePickerImpl;
import org.jdatepicker.impl.UtilDateModel;
import org.apache.commons.math3.analysis.interpolation.BicubicInterpolator;
import org.jdatepicker.DateModel;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.ngray.option.Log;
import com.ngray.option.mongo.Mongo;
import com.ngray.option.mongo.MongoCache;
import com.ngray.option.mongo.MongoCacheRegistry;
import com.ngray.option.mongo.MongoCacheRegistryException;
import com.ngray.option.mongo.MongoConstants;
import com.ngray.option.mongo.Price.SnapshotType;

import net.miginfocom.swing.MigLayout;

import com.ngray.option.mongo.VolatilitySurfaceDefinition;
import com.ngray.option.volatilitysurface.VolatilitySurface;
import com.ngray.option.volatilitysurface.VolatilitySurfaceDataSet;
import com.ngray.option.volatilitysurface.VolatilitySurfaceDataSetBuilder;
import com.ngray.option.volatilitysurface.VolatilitySurfaceException;


public class VolatilitySurfaceBuildDialog {
	
	private final MainUI parentUI;
	private JDialog dialog;
	
	private JComboBox<VolatilitySurfaceDefinition> volSurfaceDefinitionBox;
	private JComboBox<SnapshotType> snapshotTypeBox;
	private JDatePickerImpl valueDatePicker;
	private JDatePanelImpl valueDatePanel;
	
	private VolatilitySurfaceDefinition definition;
	private LocalDate valueDate;
	private SnapshotType snapshotType;
	
	public VolatilitySurfaceBuildDialog(MainUI parentUI) {
		this.parentUI = parentUI;
		initialize();
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
	
	private void initialize() {
		createDialog();
		createVolSurfaceDefinitionBox();
		createDatePicker();
		createSnapshotTypeBox();
		createBuildCancelButtons();
		dialog.pack();
	}

	private void createBuildCancelButtons() {
		JButton build = new JButton("Build...");
		JButton cancel = new JButton("Cancel");
		dialog.add(build, "cell 0 6");
		dialog.add(cancel, "cell 1 6");	
		cancel.addActionListener(e -> dialog.dispose());
		build.addActionListener(e -> buildVolatilitySurface());
	}

	private void buildVolatilitySurface() {
		if (validateChoice()) {
			try {
				VolatilitySurfaceDataSet dataSet = new VolatilitySurfaceDataSetBuilder(definition).build(valueDate, snapshotType);
				VolatilitySurface surface = new VolatilitySurface(dataSet, snapshotType, new BicubicInterpolator());
				MongoCache<VolatilitySurface> cache = MongoCacheRegistry.get(VolatilitySurface.class);
				cache.put(surface);
				new VolatilitySurfaceViewer(parentUI, surface).create();
				dialog.dispose();
			} catch (VolatilitySurfaceException | MongoCacheRegistryException e) {
				Log.getLogger().error(e.getMessage(), e);
				createErrorDialog(e);
			}
		}
	}

	private boolean validateChoice() {
		// TODO Auto-generated method stub
		return true;
	}
	
	/**
	 * Show a dialog if there is there is a vol surface exception
	 */
	private void createErrorDialog(Exception e) {
		JDialog dialog = new JDialog(parentUI.getParentFrame());
		dialog.setModalityType(JDialog.DEFAULT_MODALITY_TYPE);
		dialog.setLayout(new GridLayout(0,1));
		JButton ok = new JButton("OK");
		ok.addActionListener(event -> dialog.dispose());
		dialog.add(new JLabel(e.getMessage()));
		dialog.add(ok);
		dialog.pack();
		dialog.setVisible(true);
	}

	private void createSnapshotTypeBox() {
		JLabel label = new JLabel("Snapshot Type:");
		snapshotTypeBox = new JComboBox<>();
		dialog.add(label,"cell 0 2,span,grow");
		dialog.add(snapshotTypeBox, "cell 0 3,span,grow");
		fillSnapshotTypeBox();
		addSnapshotTypeListener();
	}

	private void addSnapshotTypeListener() {
		snapshotTypeBox.addActionListener(e -> snapshotType = (SnapshotType)snapshotTypeBox.getSelectedItem());
	}

	private void fillSnapshotTypeBox() {
		for (SnapshotType type : SnapshotType.values()) {
			snapshotTypeBox.addItem(type);
		}
	}

	private void createDatePicker() {
		JLabel label = new JLabel("Value Date:");
		UtilDateModel model = new UtilDateModel();
		//model.setDate(20,04,2014);
		// Need this...
		Properties p = new Properties();
		p.put("text.today", "Today");
		p.put("text.month", "Month");
		p.put("text.year", "Year");
		valueDatePanel = new JDatePanelImpl(model, p);
		valueDatePicker = new JDatePickerImpl(valueDatePanel, null);
		dialog.add(label, "cell 0 4,span,grow");
		dialog.add(valueDatePanel, "cell 0 5,span,grow");
		addDatePickerListener();
	}

	private void addDatePickerListener() {
		DateModel<?> model = valueDatePicker.getModel();
		// model uses months 0 to 11
		valueDatePicker.addActionListener(
			e -> valueDate = LocalDate.of(model.getYear(), model.getMonth() + 1, model.getDay())
			);
	}

	private void createVolSurfaceDefinitionBox() {
		JLabel label = new JLabel("Volatility Surface Definition:");
		volSurfaceDefinitionBox = new JComboBox<>();
		dialog.add(label,"cell 0 0,span,grow");
		dialog.add(volSurfaceDefinitionBox, "cell 0 1,span,grow");
		fillVolSurfaceDefinitionBox();
		addVolSurfaceDefinitionBoxListener();
	}

	private void addVolSurfaceDefinitionBoxListener() {
		volSurfaceDefinitionBox.addActionListener(e -> definition = (VolatilitySurfaceDefinition)volSurfaceDefinitionBox.getSelectedItem());		
	}
	
	private void fillVolSurfaceDefinitionBox() {
		// TODO - fill from MongoCache
		MongoDatabase db = Mongo.getMongoDatabase(MongoConstants.DATABASE_NAME);
		//RiskEngine.getMongoClient().getDatabase("optiondb");
		MongoCollection<VolatilitySurfaceDefinition> collection = db.getCollection("volatility_surface_definition", VolatilitySurfaceDefinition.class);
		MongoCursor<VolatilitySurfaceDefinition> docs = collection.find().iterator();
		while (docs.hasNext()) {
			VolatilitySurfaceDefinition volSurfaceDef = docs.next();	
			volSurfaceDefinitionBox.addItem(volSurfaceDef);
		}
	}

	private void createDialog() {
		dialog = new JDialog(parentUI.getParentFrame(), "Build Volatility Surface");
		dialog.setModalityType(JDialog.DEFAULT_MODALITY_TYPE);
		dialog.setResizable(false);
		
		dialog.getContentPane().setLayout(new MigLayout("", "[grow][grow]", "[][][][][][][]"));
	}
	
	


}
