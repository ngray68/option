package com.ngray.option.model.test;

import static org.junit.Assert.*;

import java.time.LocalDate;
import org.junit.Test;

import com.ngray.option.financialinstrument.EuropeanOption;
import com.ngray.option.financialinstrument.EuropeanOption.Type;
import com.ngray.option.financialinstrument.Security;
import com.ngray.option.marketdata.MarketData;
import com.ngray.option.marketdata.MarketDataCollection;
import com.ngray.option.marketdata.MarketDataException;
import com.ngray.option.model.EuropeanOptionModel;
import com.ngray.option.model.ModelException;
import com.ngray.option.risk.Risk;

import static org.mockito.Mockito.*;

public class TestEuropeanOptionModel {

	@Test
	public void testCalculateRiskForCall() throws MarketDataException, ModelException {
		System.out.println("Testing EuropeanOptionModel with Call...");
		Security underlying = mock(Security.class);
		when(underlying.getIdentifier()).thenReturn("Underlying");
		
		EuropeanOption option = mock(EuropeanOption.class);
		when(option.getExpiryDate()).thenReturn(LocalDate.of(2017, 06, 16));
		when(option.getStrike()).thenReturn(7275.0);
		when(option.getType()).thenReturn(Type.CALL);
		when(option.getUnderlying()).thenReturn(underlying);
		when(option.getIdentifier()).thenReturn("CallOption");
		when(option.getModel()).thenReturn(new EuropeanOptionModel());
		
		MarketDataCollection marketData = mock(MarketDataCollection.class);
		when(marketData.getMarketData(underlying)).thenReturn(new MarketData(7275.0, MarketData.Type.PRICE));
		when(marketData.getMarketData(option)).thenReturn(new MarketData(78.5080, MarketData.Type.PRICE));
		
		LocalDate valueDate = LocalDate.of(2017, 05, 16);
		Risk risk = option.getModel().calculateRisk(option, marketData, valueDate);
		System.out.println(risk);
		
		verify(marketData).getMarketData(underlying);
		verify(marketData).getMarketData(option);
		verify(option).getType();
		verify(option).getStrike();
		verify(option).getExpiryDate();
		verify(option, atLeast(1)).getUnderlying();		
	}
	
	@Test
	public void testCalculateRiskForPut() throws MarketDataException, ModelException {
		System.out.println("Testing EuropeanOptionModel with Put...");
		Security underlying = mock(Security.class);
		when(underlying.getIdentifier()).thenReturn("Underlying");
		
		EuropeanOption option = mock(EuropeanOption.class);
		when(option.getExpiryDate()).thenReturn(LocalDate.of(2017, 06, 16));
		when(option.getStrike()).thenReturn(7275.0);
		when(option.getType()).thenReturn(Type.PUT);
		when(option.getUnderlying()).thenReturn(underlying);
		when(option.getIdentifier()).thenReturn("PutOption");
		when(option.getModel()).thenReturn(new EuropeanOptionModel());
		
		MarketDataCollection marketData = mock(MarketDataCollection.class);
		when(marketData.getMarketData(underlying)).thenReturn(new MarketData(7275.0, MarketData.Type.PRICE));
		when(marketData.getMarketData(option)).thenReturn(new MarketData(78.5080, MarketData.Type.PRICE));
		
		
		LocalDate valueDate = LocalDate.of(2017, 05, 16);
		Risk risk = option.getModel().calculateRisk(option, marketData, valueDate);
		System.out.println(risk);
		
		verify(marketData).getMarketData(underlying);
		verify(marketData).getMarketData(option);
		verify(option).getType();
		verify(option).getStrike();
		verify(option).getExpiryDate();
		verify(option, atLeast(1)).getUnderlying();		
	}

}
