package com.ngray.option.ui;

import java.awt.EventQueue;
import java.awt.GridLayout;
import java.awt.HeadlessException;
import java.util.Set;

import javax.swing.JDesktopPane;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.InternalFrameListener;

import com.ngray.option.Log;
import com.ngray.option.financialinstrument.FinancialInstrument;
import com.ngray.option.position.Position;
import com.ngray.option.position.PositionService;

/**
 * This class manages the windows which show risk on option/underlying positions
 * grouped by underlying security
 * @author nigelgray
 *
 */
public class PositionUI {

	/**
	 * The position service from which the UI groups positions
	 */
	private final PositionService positionService;
	
	/**
	 * The parent JFrame of the UI
	 */
	private final JFrame parentFrame;
	
	/**
	 * The desktop pane which contains the internal windows
	 * showing risk by underlying
	 */
	private final JDesktopPane desktopPane;
	
	/**
	 * Constructor
	 * @param positionService
	 */
	public PositionUI(PositionService positionService) {
		this.positionService = positionService;
		this.parentFrame = new JFrame();
		this.parentFrame.setTitle("Open Positions");
		this.desktopPane = new JDesktopPane();
		this.desktopPane.setLayout(new GridLayout(0,1));
		parentFrame.setContentPane(desktopPane);
		initialize();	
		parentFrame.setExtendedState(JFrame.MAXIMIZED_BOTH);
		show(parentFrame);
		this.positionService.setUI(this);
	}
	
	/**
	 * Callback to update the UI when a position in a new underlying is created
	 * @param position
	 */
	public void onPositionInNewUnderlying(Position position) {
		
		//TODO: check that a model/JInternalFrame for the underlying doesn't already exist
		createPositionTable(position.getUnderlying());
	}
	
	private void initialize() {
		Set<FinancialInstrument> underlyings = positionService.getUnderlyings();			
		underlyings.forEach(underlying -> {
			createPositionTable(underlying);
		});
	}
	
	private void createPositionTable(FinancialInstrument underlying) {
		PositionRiskTableModel model = new PositionRiskTableModel(positionService.getPositions(underlying));
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
				positionService.removeListener(underlying, model);
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
		desktopPane.add(frame);
		positionService.addListener(underlying, model);
	}
	
	private void show(JFrame frame) {
		EventQueue.invokeLater(()-> {
			try {
				frame.setVisible(true);
			} catch (HeadlessException e) {
				Log.getLogger().error(e.getMessage(), e);
			}
		});
	}
	
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
