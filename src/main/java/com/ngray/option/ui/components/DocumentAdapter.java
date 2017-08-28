package com.ngray.option.ui.components;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

//Adapter to simplify listeners for text fields
public abstract class DocumentAdapter implements DocumentListener {

		public abstract void update(DocumentEvent e);
		
		@Override
		public void insertUpdate(DocumentEvent e) {
			update(e);
		}

		@Override
		public void removeUpdate(DocumentEvent e) {
			update(e);
		}

		@Override
		public void changedUpdate(DocumentEvent e) {
			update(e);
		}
		
	}