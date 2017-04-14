package com.ngray.option.model;

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
	
	public Risk(double value, double delta, double gamma, double vega, double theta, double rho, double impliedVolatility) {
		this.value = value;
		this.delta = delta;
		this.gamma = gamma;
		this.vega = vega;
		this.theta = theta;
		this.rho = rho;
		this.impliedVolatility = impliedVolatility;
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
		String s = "--Risk-------------------------"  +
				   "\nValue:\t" + getValue() +
				   "\nIV:\t" + getImpliedVolatility() +
				   "\nDelta:\t" + getDelta() +
				   "\nGamma:\t" + getGamma() +
				   "\nVega:\t" + getVega() +
				   "\nTheta:\t" + getTheta() +
				   "\nRho:\t" + getRho() + 
				   "\n-------------------------------";
		return s;
				   
	}
}
