package com.ngray.option.ui;

import java.awt.EventQueue;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.HeadlessException;
import java.awt.LayoutManager;
import java.util.Set;

import javax.swing.JDesktopPane;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;

import org.apache.logging.log4j.core.Layout;

import com.ngray.option.Log;
import com.ngray.option.financialinstrument.FinancialInstrument;
import com.ngray.option.position.Position;
import com.ngray.option.position.PositionService;

public class PositionUI {

	private final PositionService positionService;
	private final JFrame parentFrame;
	private final JDesktopPane desktopPane;
	
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

	public PositionService getPositionService() {
		return positionService;
	}
	
	public void onPositionInNewUnderlying(Position position) {
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

	public JFrame getParentFrame() {
		return parentFrame;
	}

}
