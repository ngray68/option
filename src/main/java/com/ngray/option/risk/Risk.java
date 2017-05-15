package com.ngray.option.risk;


/**
 * Class encapsulating a consisent set of value/risk numbers
 * Objects of this type are immutable
 * @author nigelgray
 *
 */
public class Risk {

	private final double value;
	private final double delta;
	private final double gamma;
	private final double vega;
	private final double theta;
	private final double rho;
	private final double impliedVolatility;
	
	// data used to calculate this risk
	private final double price;
	private final double underlyingPrice;
	
	public Risk(double value, double delta, double gamma, double vega, double theta, double rho, double impliedVolatility) {
		this.value = value;
		this.delta = delta;
		this.gamma = gamma;
		this.vega = vega;
		this.theta = theta;
		this.rho = rho;
		this.impliedVolatility = impliedVolatility;
		this.price = Double.NaN;
		this.underlyingPrice = Double.NaN;
	}
	
	public Risk(double value, double delta, double gamma, double vega, double theta, double rho, double impliedVolatility, double price, double underlyingPrice) {
		this.value = value;
		this.delta = delta;
		this.gamma = gamma;
		this.vega = vega;
		this.theta = theta;
		this.rho = rho;
		this.impliedVolatility = impliedVolatility;
		this.price = price;
		this.underlyingPrice = underlyingPrice;
	}

	public Risk() {
		this.value = Double.NaN;
		this.delta = Double.NaN;
		this.gamma = Double.NaN;
		this.vega = Double.NaN;
		this.theta = Double.NaN;
		this.rho = Double.NaN;
		this.impliedVolatility = Double.NaN;
		this.price = Double.NaN;
		this.underlyingPrice = Double.NaN;
	}
	
	public Risk multiply(double N) {
		double value = getValue() * N;
		double delta = getDelta() * N;
		double gamma = getGamma() * N;
		double vega = getVega() * N;
		double rho = getRho() * N;
		double theta = getTheta() * N;
		// imp vol and prices don't scale with position size
		return new Risk(value, delta, gamma, vega, theta, rho, getImpliedVolatility(), getPrice(), getUnderlyingPrice());
	}

	public double getValue() {
		return value;
	}

	public double getDelta() {
		return delta;
	}

	public double getGamma() {
		return gamma;
	}

	public double getVega() {
		return vega;
	}

	public double getTheta() {
		return theta;
	}

	public double getRho() {
		return rho;
	}

	public double getImpliedVolatility() {
		return impliedVolatility;
	}
	
	@Override
	public String toString() {
		String s = "\n\nRisk"  +
	               "\n====================================" +
				   "\nValue:\t" + getValue() +
				   "\nIV:\t" + getImpliedVolatility() +
				   "\nDelta:\t" + getDelta() +
				   "\nGamma:\t" + getGamma() +
				   "\nVega:\t" + getVega() +
				   "\nTheta:\t" + getTheta() +
				   "\nRho:\t" + getRho() + 
				   "\n====================================\n\n";
		return s;
				   
	}

	public double getPrice() {
		return price;
	}

	public double getUnderlyingPrice() {
		return underlyingPrice;
	}

}
