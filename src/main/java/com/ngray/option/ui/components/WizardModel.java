package com.ngray.option.ui.components;

public interface WizardModel {
	
	public boolean validate();
	
	public void onBack();
	
	public void onNext();
	
	public void onShow();

	public void onFinish();
	
}
