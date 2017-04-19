package com.ngray.option.position.test;

import static org.junit.Assert.*;

import java.time.LocalDate;

import org.junit.Test;

import com.ngray.option.financialinstrument.EuropeanOption;
import com.ngray.option.financialinstrument.EuropeanOption.Type;
import com.ngray.option.financialinstrument.Security;
import com.ngray.option.marketdata.MarketDataService;
import com.ngray.option.position.Position;
import com.ngray.option.risk.RiskService;

public class TestPosition {

	private MarketDataService marketDataService;
	private RiskService riskService;
	
	//private List<Position> testPositions;
	
	public TestPosition() {
		marketDataService = new MarketDataService("Test");
		riskService = new RiskService("Test", marketDataService, LocalDate.now());
		
		//testPositions = new ArrayList<>();
	}
	/*
	@Test
	public void testUpdatePositionRiskAndPnL() {
		fail("Not yet implemented");
	}
*/
	@Test
	public void testSubscribeToRiskService() {
		
		Security security = new Security("SomeStock");
		Position testPosition = new Position("1", security, 500.0, 0.5);
		testPosition.subscribeToRiskService(riskService);
		
		EuropeanOption option = new EuropeanOption("SomeOption", security, 0.5, LocalDate.of(2017, 6, 16), Type.CALL);
		Position optionPosition = new Position("2", option, 1000.0, 0.1);
		optionPosition.subscribeToRiskService(riskService);
		while(true) { }
	}
/*
	@Test
	public void testUnsubscribeFromRiskService() {
		fail("Not yet implemented");
	}
*/
}
