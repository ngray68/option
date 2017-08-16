package com.ngray.option.ui;

import java.awt.Component;
import java.awt.EventQueue;
import java.awt.HeadlessException;
import javax.swing.JInternalFrame;
import javax.swing.event.InternalFrameListener;

import com.ngray.option.Log;

public class Frames {
	
	public static JInternalFrame createJInternalFrame(String title, InternalFrameListener listener, Component... components) {
		JInternalFrame frame = new JInternalFrame();
		for (Component component : components) {
			frame.add(component);
		}
		frame.setTitle(title);
		frame.setMaximizable(true);
		frame.setResizable(true);
		frame.setClosable(true);
		frame.setIconifiable(true);
		frame.pack();
		if (listener != null) {
			frame.addInternalFrameListener(listener);
		}
		show(frame);
		return frame;
	}

	private static void show(JInternalFrame frame) {
		EventQueue.invokeLater(()-> {
			try {
				frame.setVisible(true);
			} catch (HeadlessException e) {
				Log.getLogger().error(e.getMessage(), e);
			}
		});	
	}

}
