package com.ngray.option.ui;

import java.awt.Component;
import java.awt.GridLayout;
import java.beans.PropertyVetoException;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import javax.swing.JInternalFrame;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.InternalFrameListener;

import com.ngray.option.Log;
import com.ngray.option.analysis.OptionRiskLadder;
import com.ngray.option.financialinstrument.EuropeanOption;
import com.ngray.option.financialinstrument.EuropeanOption.Type;
import com.ngray.option.financialinstrument.Security;

/**
 * Class encapsulating the UI components for viewing option risk ladders
 * @author nigelgray
 *
 */
public class OptionRiskLadderView {
	
	private final MainUI parentUI;
	
	private JInternalFrame parentInternalFrame;
	
	private OptionRiskLadder callRiskLadder;
	private OptionRiskLadder putRiskLadder;
	private Security underlying;
	private LocalDate optionExpiry;
	
	/**
	 * Constructor
	 * @param parentUI
	 * @param underlying
	 * @param optionExpiry
	 * @param options
	 */
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
			
		String underlyingName = underlying.getName() + " " + underlying.getIGMarket().getExpiry();
		JInternalFrame callFrame = createFrame(underlyingName + " CALLS Expiring " + optionExpiry, callRiskLadder);
		JInternalFrame putFrame = createFrame(underlyingName + " PUTS Expiring " + optionExpiry, putRiskLadder);
		//show(callFrame);
		//show(putFrame);
		
		InternalFrameListener listener = new InternalFrameAdapter() {
			@Override
			public void internalFrameClosed(InternalFrameEvent e) {
				onClose();
				parentInternalFrame.removeInternalFrameListener(this);
			}
		};
		
		parentInternalFrame = Frames.createJInternalFrame("Option Risk Ladder", listener, callFrame, putFrame);
		parentInternalFrame.setLayout(new GridLayout(1,2));
		parentInternalFrame.pack();
		parentUI.getDesktopPane().add(parentInternalFrame);
	}
	
	private JInternalFrame createFrame(String title, OptionRiskLadder riskLadder) {
		OptionRiskLadderTableModel model = new OptionRiskLadderTableModel(riskLadder);
		JTable table = new JTable(model);
		JScrollPane pane = new JScrollPane(table);
		
		InternalFrameListener listener = new InternalFrameAdapter() {
			@Override
			public void internalFrameClosed(InternalFrameEvent e) {
				Log.getLogger().debug("OptionRiskLadderView::InternalFrame close event received");
				riskLadder.dispose();
			}
		};
		
		JInternalFrame frame = Frames.createJInternalFrame(title, listener, pane);
		return frame;
	}
	
	private void onClose() {
		Log.getLogger().debug("Closing option risk ladder view....");
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
}
