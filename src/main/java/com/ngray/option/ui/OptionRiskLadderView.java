package com.ngray.option.ui;

import java.awt.EventQueue;
import java.awt.GridLayout;
import java.awt.HeadlessException;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import javax.swing.JInternalFrame;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import com.ngray.option.Log;
import com.ngray.option.analysis.OptionRiskLadder;
import com.ngray.option.financialinstrument.EuropeanOption;
import com.ngray.option.financialinstrument.EuropeanOption.Type;
import com.ngray.option.financialinstrument.Security;

public class OptionRiskLadderView {
	
	private final MainUI parentUI;
	
	private OptionRiskLadder callRiskLadder;
	private OptionRiskLadder putRiskLadder;
	private Security underlying;
	private LocalDate optionExpiry;
	
	public OptionRiskLadderView(MainUI parentUI, Security underlying, LocalDate optionExpiry, List<EuropeanOption> options) {
		Log.getLogger().debug("Constructing OptionRiskLadderView");
		this.parentUI = parentUI;
		this.underlying = underlying;
		this.optionExpiry = optionExpiry;
		add(options);
		createRiskTables();
	}
	
	private void add(List<EuropeanOption> options) {
		Log.getLogger().info("OptionRiskLadderView: adding options to view");
		List<EuropeanOption> calls = options.stream()
											.filter(option -> option.getType()==Type.CALL)
											.collect(Collectors.toList());
		
		List<EuropeanOption> puts = options.stream()
				.filter(option -> option.getType()==Type.PUT)
				.collect(Collectors.toList());
		
		callRiskLadder = new OptionRiskLadder(calls);
		putRiskLadder = new OptionRiskLadder(puts);
	}
	
	private void createRiskTables() {
		
		OptionRiskLadderTableModel callModel = new OptionRiskLadderTableModel(callRiskLadder);
		OptionRiskLadderTableModel putModel = new OptionRiskLadderTableModel(putRiskLadder);
		
		JTable callTable = new JTable(callModel);
		JTable putTable = new JTable(putModel);
		
		JScrollPane callPane = new JScrollPane(callTable);
		JScrollPane putPane = new JScrollPane(putTable);
		
		parentUI.setLayout(new GridLayout(1,2));
		
		String underlyingName = underlying.getName() + " " + underlying.getIGMarket().getExpiry();
		JInternalFrame callFrame = createFrame(underlyingName + " CALLS Expiring " + optionExpiry, callPane);
		JInternalFrame putFrame = createFrame(underlyingName + " PUTS Expiring " + optionExpiry, putPane);
		show(callFrame);
		show(putFrame);
		parentUI.setFrameTitle("Option Risk Ladder");	
		parentUI.getDesktopPane().add(callFrame);
		parentUI.getDesktopPane().add(putFrame);
	}
	
	private JInternalFrame createFrame(String title, JScrollPane pane) {
		JInternalFrame frame = new JInternalFrame();
		frame.add(pane);
		frame.setTitle(title);
		frame.setMaximizable(true);
		frame.setResizable(true);
		frame.setClosable(true);
		frame.setIconifiable(true);
		frame.pack();
		return frame;
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
