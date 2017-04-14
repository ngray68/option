package com.ngray.option.model.test;

import static org.junit.Assert.*;
import org.junit.Test;

import com.ngray.option.model.BlackScholesModel;

public class TestBlackScholesModel {

	private double atmSpot = 7275.0;
	private double strike = 7275.0;
	private double volatility = 0.1; // ie 10%
	private double timeToExpiry = 1/12.0;  // ie. approx 1 month
	private double riskFreeRate = 0.0025;
	private double dividendYield = 0.02;
	
	private double atmCallPrice = 78.5080;
	private double atmPutPrice = 89.1074;
	private double atmCallDelta = 0.4847;
	private double atmPutDelta = -0.5135;
	private double atmGamma = 0.001898;
	private double atmVega = 8.3588;
	private double atmCallTheta = -1.2067;
	private double atmPutTheta =  -1.5548;
	private double atmCallRho = 2.8736;
	private double atmPutRho = -3.1876;
	
	
	@Test
	public void testCalcCallOptionPrice() {
		System.out.println("\nTesting ATM call price...");
		double price = BlackScholesModel.calcCallOptionPrice(atmSpot, strike, volatility, timeToExpiry, riskFreeRate, dividendYield);
		System.out.println("Price = " + price);
		assertTrue(Double.compare(Math.abs(atmCallPrice - price), 0.0001) < 0);
		
		System.out.println("\nTesting OTM call price...");
		price = BlackScholesModel.calcCallOptionPrice(0.5 * atmSpot, strike, volatility, timeToExpiry, riskFreeRate, dividendYield);
		System.out.println("Price = " + price);
		assertTrue(Double.compare(price, 0.0) >= 0 && Double.compare(price, 0.0001) < 0);
		
		System.out.println("\nTesting ITM call price...");
		price = BlackScholesModel.calcCallOptionPrice(atmSpot + 200.0, strike, volatility, timeToExpiry, riskFreeRate, dividendYield);
		System.out.println("Price = " + price);
		assertTrue(Double.compare(price - 200.0, 0.0) >= 0);
	}

	@Test
	public void testCalcPutOptionPrice() {
		System.out.println("Testing ATM put price...");
		double price = BlackScholesModel.calcPutOptionPrice(atmSpot, strike, volatility, timeToExpiry, riskFreeRate, dividendYield);
		System.out.println("Price = " + price);
		assertTrue(Double.compare(Math.abs(atmPutPrice - price), 0.0001) < 0);
		
		System.out.println("\nTesting ITM put price...");
		price = BlackScholesModel.calcPutOptionPrice(atmSpot - 200.0, strike, volatility, timeToExpiry, riskFreeRate, dividendYield);
		System.out.println("Price = " + price);
		assertTrue(Double.compare(price - 200.0, 0.0) >= 0);
		
		
		System.out.println("\nTesting OTM put price...");
		price = BlackScholesModel.calcPutOptionPrice(1.2 * atmSpot, strike, volatility, timeToExpiry, riskFreeRate, dividendYield);
		System.out.println("Price = " + price);
		assertTrue(Double.compare(price, 0.0) >= 0 && Double.compare(price, 0.0001) < 0);
	}

	@Test
	public void testCalcCallOptionImpliedVol() {
		System.out.println("Testing ATM call implied vol..");
		double vol = BlackScholesModel.calcCallOptionImpliedVol(atmSpot, strike, atmCallPrice, timeToExpiry, riskFreeRate, dividendYield);
		System.out.println("Vol = " + vol);
		assertTrue(Double.compare(Math.abs(volatility - vol), 0.0001) < 0);
	}

	@Test
	public void testCalcPutOptionImpliedVol() {
		System.out.println("Testing ATM put implied vol..");
		double vol = BlackScholesModel.calcPutOptionImpliedVol(atmSpot, strike, atmPutPrice, timeToExpiry, riskFreeRate, dividendYield);
		System.out.println("Vol = " + vol);
		assertTrue(Double.compare(Math.abs(volatility - vol), 0.0001) < 0);
	}

	@Test
	public void testCalcCallOptionDelta() {
		System.out.println("\nTesting ATM call delta...");
		double delta = BlackScholesModel.calcCallOptionDelta(atmSpot, strike, volatility, timeToExpiry, riskFreeRate, dividendYield);
		System.out.println("Delta = " + delta);
		assertTrue(Double.compare(Math.abs(atmCallDelta - delta), 0.0001) < 0);
		
		System.out.println("\nTesting OTM call delta...");
		delta = BlackScholesModel.calcCallOptionDelta(atmSpot - 5000.0, strike, volatility, timeToExpiry, riskFreeRate, dividendYield);
		System.out.println("Delta = " + delta);
		assertTrue(Double.compare(delta, 0.0001) < 0);
		
		System.out.println("\nTesting OTM call delta...");
		delta = BlackScholesModel.calcCallOptionDelta(atmSpot + 5000.0, strike, volatility, timeToExpiry, riskFreeRate, dividendYield);
		System.out.println("Delta = " + delta);
		assertTrue(Double.compare(delta, 0.99) >= 0 && Double.compare(delta, 1.0) <=0);
	}

	@Test
	public void testCalcPutOptionDelta() {
		System.out.println("\nTesting ATM put delta...");
		double delta = BlackScholesModel.calcPutOptionDelta(atmSpot, strike, volatility, timeToExpiry, riskFreeRate, dividendYield);
		System.out.println("Delta = " + delta);
		assertTrue(Double.compare(Math.abs(atmPutDelta - delta), 0.0001) < 0);
		
		System.out.println("\nTesting OTM put delta...");
		delta = BlackScholesModel.calcPutOptionDelta(atmSpot + 5000.0, strike, volatility, timeToExpiry, riskFreeRate, dividendYield);
		System.out.println("Delta = " + delta);
		assertTrue(Double.compare(delta, 0.0001) < 0);
		
		System.out.println("\nTesting ITM put delta...");
		delta = BlackScholesModel.calcPutOptionDelta(atmSpot - 5000.0, strike, volatility, timeToExpiry, riskFreeRate, dividendYield);
		System.out.println("Delta = " + delta);
		assertTrue(Double.compare(delta, -0.99) <= 0 && Double.compare(delta, -1.0) >= 0);
	}

	@Test
	public void testCalcOptionGamma() {
		System.out.println("\nTesting ATM gamma...");
		double gamma = BlackScholesModel.calcOptionGamma(atmSpot, strike, volatility, timeToExpiry, riskFreeRate, dividendYield);
		System.out.println("Gamma = " + gamma);
		assertTrue(Double.compare(Math.abs(atmGamma - gamma), 0.0001) < 0);	
		
		System.out.println("\nTesting OTM/ITM gamma...");
		gamma = BlackScholesModel.calcOptionGamma(atmSpot + 5000.0, strike, volatility, timeToExpiry, riskFreeRate, dividendYield);
		System.out.println("Gamma = " + gamma);
		assertTrue(Double.compare(gamma, 0.0001) < 0 && Double.compare(gamma, 0.0) >= 0);
		
		System.out.println("\nTesting OTM/ITM gamma...");
		gamma = BlackScholesModel.calcOptionGamma(atmSpot -5000.0, strike, volatility, timeToExpiry, riskFreeRate, dividendYield);
		System.out.println("Gamma = " + gamma);
		assertTrue(Double.compare(gamma, 0.0001) < 0 && Double.compare(gamma, 0.0) >= 0);
	}

	@Test
	public void testCalcOptionVega() {
		System.out.println("\nTesting ATM vega...");
		double vega = BlackScholesModel.calcOptionVega(atmSpot, strike, volatility, timeToExpiry, riskFreeRate, dividendYield);
		System.out.println("Vega = " + vega);
		assertTrue(Double.compare(Math.abs(atmVega - vega), 0.0001) < 0);
	}

	@Test
	public void testCalcCallOptionTheta() {
		System.out.println("\nTesting ATM call theta...");
		double theta = BlackScholesModel.calcCallOptionTheta(atmSpot, strike, volatility, timeToExpiry, riskFreeRate, dividendYield);
		System.out.println("Theta = " + theta);
		assertTrue(Double.compare(Math.abs(atmCallTheta - theta), 0.0001) < 0);
	}

	@Test
	public void testCalcPutOptionTheta() {
		System.out.println("\nTesting ATM put theta...");
		double theta = BlackScholesModel.calcPutOptionTheta(atmSpot, strike, volatility, timeToExpiry, riskFreeRate, dividendYield);
		System.out.println("Theta = " + theta);
		assertTrue(Double.compare(Math.abs(atmPutTheta - theta), 0.0001) < 0);
	}

	@Test
	public void testCalcCallOptionRho() {
		System.out.println("\nTesting ATM call rho...");
		double rho = BlackScholesModel.calcCallOptionRho(atmSpot, strike, volatility, timeToExpiry, riskFreeRate, dividendYield);
		System.out.println("Rho = " + rho);
		assertTrue(Double.compare(Math.abs(atmCallRho - rho), 0.0001) < 0);
	}

	@Test
	public void testCalcPutOptionRho() {
		System.out.println("\nTesting ATM put rho...");
		double rho = BlackScholesModel.calcPutOptionRho(atmSpot, strike, volatility, timeToExpiry, riskFreeRate, dividendYield);
		System.out.println("Rho = " + rho);
		assertTrue(Math.abs(atmPutRho - rho) < 0.0001);
	}

}
