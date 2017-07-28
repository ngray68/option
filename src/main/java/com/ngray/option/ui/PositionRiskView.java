package com.ngray.option.ui;

import java.awt.EventQueue;
import java.awt.GridLayout;
import java.awt.HeadlessException;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyVetoException;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.InternalFrameListener;

import com.ngray.option.Log;
import com.ngray.option.RiskEngine;
import com.ngray.option.financialinstrument.FinancialInstrument;
import com.ngray.option.position.Position;

/**
 * This class manages the viewing of open positions
 * @author nigelgray
 *
 */
public class PositionRiskView {
	
	/**
	 * The parent UI which supplies the frame and content pane
	 * on which the positions will be displayed
	 */
	private MainUI parentUI;
	
	/**
	 * Constructor
	 * @param parentUI
	 */
	public PositionRiskView(MainUI parentUI) {
		this.parentUI = parentUI;
	}
	
	/**
	 * Show all open positions in separate windows for each underlying
	 */
	public void showPositions() {
		Log.getLogger().debug("Showing open positions");
		RiskEngine.getPositionService().setView(this);
		
		parentUI.setLayout(new GridLayout(0,1));
		if (createPositionTables() == 0) {
			Log.getLogger().debug("No open positions");
			JDialog noPositions = new JDialog(parentUI.getParentFrame());
			noPositions.setModalityType(JDialog.DEFAULT_MODALITY_TYPE);
			noPositions.setLayout(new GridLayout(0,1));
			JButton ok = new JButton("OK");
			ok.addActionListener(event -> noPositions.dispose());
			noPositions.add(new JLabel("You have no open positions at present"));
			noPositions.add(ok);
			noPositions.pack();
			noPositions.setVisible(true);
			return;
		}
		
		parentUI.getParentFrame().addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				hidePositions();
			}
		});
		parentUI.setFrameTitle("Open Positions");
	}
	
	/**
	 * Hide the positions - effectively makes the position risk view invisible
	 */
	public void hidePositions() {
		Log.getLogger().debug("Hiding open positions");
		for (JInternalFrame frame : parentUI.getDesktopPane().getAllFrames()) {
			try {
				frame.setClosed(true);
			} catch (PropertyVetoException e) {
				Log.getLogger().warn(e.getMessage(), true);
			}
		}
		
		parentUI.setFrameTitle("");
		parentUI.show();
	}
	
	/**
	 * Creates a new risk window when a position on a new underlying is detected
	 * @param position
	 */
	public void onPositionInNewUnderlying(Position position) {
		//TODO: check that a model/JInternalFrame for the underlying doesn't already exist
		Log.getLogger().debug("Creating position table for new underlying");
		createPositionTable(position.getUnderlying());
	}
	
	/**
	 * Create position windows for each underlying on initialization
	 * @return the number of position tables created
	 */
	private int createPositionTables() {
		Log.getLogger().debug("Creating position tables...");
		Set<FinancialInstrument> underlyings = RiskEngine.getPositionService().getUnderlyings();			
		underlyings.forEach(underlying -> {
			createPositionTable(underlying);
		});
		return underlyings.size();
	}
	
	/**
	 * Create a single position table for the given underlying
	 * @param underlying
	 */
	private void createPositionTable(FinancialInstrument underlying) {
		Log.getLogger().debug("Creating position table for underlying " + underlying);
		PositionRiskTableModel model = new PositionRiskTableModel(RiskEngine.getPositionService().getPositions(underlying));
		JTable table = new JTable(model);
		JScrollPane pane = new JScrollPane(table);
		
		JInternalFrame frame = new JInternalFrame();
		frame.add(pane);
		frame.setTitle(underlying.getName() + " " + underlying.getIGMarket().getExpiry());
		frame.setMaximizable(true);
		frame.setResizable(true);
		frame.setClosable(true);
		frame.setIconifiable(true);
		frame.pack();
		frame.addInternalFrameListener(new InternalFrameListener() {

			@Override
			public void internalFrameOpened(InternalFrameEvent e) {
				// unimplemented
			}

			@Override
			public void internalFrameClosing(InternalFrameEvent e) {
				// unimplemented
			}

			@Override
			public void internalFrameClosed(InternalFrameEvent e) {
				Log.getLogger().debug("JInternalFrame closed event received");
				RiskEngine.getPositionService().removeListener(underlying, model);
			}

			@Override
			public void internalFrameIconified(InternalFrameEvent e) {
				// unimplemented				
			}

			@Override
			public void internalFrameDeiconified(InternalFrameEvent e) {
				// unimplemented
			}

			@Override
			public void internalFrameActivated(InternalFrameEvent e) {
				// unimplemented
			}

			@Override
			public void internalFrameDeactivated(InternalFrameEvent e) {
				// unimplemented
			}		
		});
	
		show(frame);
		parentUI.getDesktopPane().add(frame);
		RiskEngine.getPositionService().addListener(underlying, model);
	}

	/**
	 * Show the JInternalFrame
	 * @param frame
	 */
	private void show(JInternalFrame frame) {
		EventQueue.invokeLater(()-> {
			try {
				frame.setVisible(true);
			} catch (HeadlessException e) {
				Log.getLogger().error(e.getMessage(), e);
			}
		});		
	}
}
