package com.ngray.option.ui;

import javax.swing.JInternalFrame;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import com.ngray.option.volatilitysurface.VolatilitySurface;

public class VolatilitySurfaceViewer {
	
	private final MainUI parentUI;
	private final VolatilitySurface volatilitySurface;
	
	public VolatilitySurfaceViewer(MainUI parentUI, VolatilitySurface volatilitySurface) {
		this.parentUI = parentUI;
		this.volatilitySurface = volatilitySurface;
	}
	
	public void create() {
		String title = volatilitySurface.getUniqueId();
		JInternalFrame frame = createFrame(title, volatilitySurface);
		parentUI.getDesktopPane().add(frame);
	}
	
	private JInternalFrame createFrame(String title, VolatilitySurface volatilitySurface) {
		VolatilitySurfaceTableModel model = new VolatilitySurfaceTableModel(volatilitySurface);
		JTable table = new JTable(model);
		JScrollPane pane = new JScrollPane(table);		
		JInternalFrame frame = Frames.createJInternalFrame(title, null, pane);
		return frame;
	}

}
