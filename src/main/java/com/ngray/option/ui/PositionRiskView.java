package com.ngray.option.ui;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.beans.PropertyVetoException;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.event.InternalFrameAdapter;
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
	
	public static final int DEFAULT_WIDTH = 1000;
	public static final int DEFAULT_HEIGHT = 200;
	
	/**
	 * The parent UI which supplies the frame and content pane
	 * on which the positions will be displayed
	 */
	private MainUI parentUI;
	
	/**
	 * The frame in which all open position windows appear
	 */
	private JInternalFrame parentInternalFrame;
	
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
	
		InternalFrameListener listener = new InternalFrameAdapter() {
			@Override
			public void internalFrameClosed(InternalFrameEvent e) {
				Log.getLogger().debug("JInternalFrame closed event received");
				hidePositions();
				e.getInternalFrame().removeInternalFrameListener(this);
			}	
		};
		parentInternalFrame = Frames.createJInternalFrame("Open Positions", listener);
		parentInternalFrame.setLayout(new GridLayout(0,1));
		parentUI.getDesktopPane().add(parentInternalFrame);
		if (createPositionTables() == 0) {
			showNoPositionsDialog();
		}
	}
	
	/**
	 * Hide the positions - effectively makes the position risk view invisible
	 */
	public void hidePositions() {
		Log.getLogger().debug("Closing open positions view");	
		for (Component component : parentInternalFrame.getContentPane().getComponents()) {
			try {
				if (component instanceof JInternalFrame) {
					Log.getLogger().debug("Closing " + ((JInternalFrame)component).getTitle());
					((JInternalFrame)component).setClosed(true);
				}
			} catch (PropertyVetoException e) {
				Log.getLogger().warn(e.getMessage(), true);
			}
		}
	}
	
	/**
	 * Creates a new risk window when a position on a new underlying is detected
	 * @param position
	 */
	public void onPositionInNewUnderlying(Position position) {
		//TODO: check that a model/JInternalFrame for the underlying doesn't already exist
		Log.getLogger().debug("Creating position table for new underlying");
		parentInternalFrame.add(createPositionTable(position.getUnderlying()));
		parentInternalFrame.pack();
	}
	
	/**
	 * Create position windows for each underlying on initialization
	 * @return the number of position tables created
	 */
	private int createPositionTables() {
		Log.getLogger().debug("Creating position tables...");
		Set<FinancialInstrument> underlyings = RiskEngine.getPositionService().getUnderlyings();			
		underlyings.forEach(underlying -> {
			parentInternalFrame.add(createPositionTable(underlying));
		});
		parentInternalFrame.pack();
		return underlyings.size();
	}
	
	/**
	 * Create a single position table for the given underlying
	 * @param underlying
	 */
	private JInternalFrame createPositionTable(FinancialInstrument underlying) {
		Log.getLogger().debug("Creating position table for underlying " + underlying);
		PositionRiskTableModel model = new PositionRiskTableModel(RiskEngine.getPositionService().getPositions(underlying));
		RiskEngine.getPositionService().addListener(underlying, model);
		JTable table = new JTable(model);
		JScrollPane pane = new JScrollPane(table);

		InternalFrameListener listener = new InternalFrameAdapter() {
			@Override
			public void internalFrameClosed(InternalFrameEvent e) {
				Log.getLogger().debug("JInternalFrame closed event received");
				RiskEngine.getPositionService().removeListener(underlying, model);
				e.getInternalFrame().removeInternalFrameListener(this);
			}
		};
		JInternalFrame frame = Frames.createJInternalFrame(underlying.getName() + " " + underlying.getIGMarket().getExpiry(), listener, pane);
		frame.setPreferredSize(new Dimension(DEFAULT_WIDTH, DEFAULT_HEIGHT));
		return frame;
	}
	
	/**
	 * Show a dialog if there are no open positions
	 */
	private void showNoPositionsDialog() {
		Log.getLogger().debug("No open positions");
		JDialog noPositions = new JDialog(parentUI.getParentFrame());
		noPositions.setModalityType(JDialog.DEFAULT_MODALITY_TYPE);
		noPositions.setLayout(new GridLayout(0,1));
		JButton ok = new JButton("OK");
		ok.addActionListener(event -> noPositions.dispose());
		noPositions.add(new JLabel("No open positions"));
		noPositions.add(ok);
		noPositions.pack();
		noPositions.setVisible(true);
	}
}
