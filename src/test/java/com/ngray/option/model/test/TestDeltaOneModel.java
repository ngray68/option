package com.ngray.option.model.test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;

import org.junit.Test;

import com.ngray.option.financialinstrument.Security;
import com.ngray.option.marketdata.MarketData;
import com.ngray.option.marketdata.MarketDataCollection;
import com.ngray.option.marketdata.MarketDataException;
import com.ngray.option.model.DeltaOneModel;
import com.ngray.option.model.ModelException;
import com.ngray.option.risk.Risk;

public class TestDeltaOneModel {

	@Test
	public void testCalculateRisk() throws MarketDataException, ModelException {
		System.out.println("Testing DeltaOneModel...");
		Security security = mock(Security.class);
		when(security.getIdentifier()).thenReturn("Underlying");
		when(security.getModel()).thenReturn(new DeltaOneModel());
		
		MarketDataCollection marketData = mock(MarketDataCollection.class);
		when(marketData.getMarketData(security)).thenReturn(new MarketData(7275.0, MarketData.Type.PRICE));
	
		
		LocalDate valueDate = LocalDate.of(2017, 05, 16);
		Risk risk = security.getModel().calculateRisk(security, marketData, valueDate);
		System.out.println(risk);
		
		verify(marketData).getMarketData(security);

			
	}

}
