package com.ngray.option.position;

public interface PositionListener {

	public void onPositionRiskUpdate(Position position);
	
	public void onPositionPnLUpdate(Position position);
	
	public void onOpenPosition(Position position);
	
	public void onDeletePosition(Position position);
	
	public void onUpdatePosition(Position position);
}
