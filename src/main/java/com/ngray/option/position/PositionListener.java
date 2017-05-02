package com.ngray.option.position;

public interface PositionListener {

	public void onPositionRiskUpdate(Position position);
	
	public void onPositionPnLUpdate(Position position);
}
