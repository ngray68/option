package com.ngray.option.model;

import static java.lang.Math.*;
import org.apache.commons.math3.distribution.NormalDistribution;

/**
 * Static class encapsulating a simple Black-Scholes model for
 * European option pricing and risk
 * @author nigelgray
 *
 */
public class BlackScholesModel {
	
	/**
	 * Calculate the theoretical price of a European call option
	 * @param spot
	 * @param strike
	 * @param volatility
	 * @param timeToExpiry
	 * @param riskFreeRate
	 * @param dividendYield
	 * @return
	 */
	public static double calcCallOptionPrice(
						double spot,
						double strike,
						double volatility,
						double timeToExpiry,
						double riskFreeRate,
						double dividendYield) {
		
		double dOne = calcDOne(spot, strike, volatility, timeToExpiry, riskFreeRate, dividendYield);
		double dTwo = calcDTwo(dOne, volatility, timeToExpiry);
		return spot * exp(-dividendYield * timeToExpiry) * cumulativeDistribution(dOne) - strike * exp(-riskFreeRate * timeToExpiry) * cumulativeDistribution(dTwo);	
	}
	
	/**
	 * Calculate the theoretical value of a European put option
	 * @param spot
	 * @param strike
	 * @param volatility
	 * @param timeToExpiry
	 * @param riskFreeRate
	 * @param dividendYield
	 * @return
	 */
	public static double calcPutOptionPrice(
						double spot,
						double strike,
						double volatility,
						double timeToExpiry,
						double riskFreeRate,
						double dividendYield) {
		
		double dOne = calcDOne(spot, strike, volatility, timeToExpiry, riskFreeRate, dividendYield);
		double dTwo = calcDTwo(dOne, volatility, timeToExpiry);
		return strike * exp(-riskFreeRate * timeToExpiry) * cumulativeDistribution(-dTwo) - spot * exp(-dividendYield * timeToExpiry) * cumulativeDistribution(-dOne);	
	}

	
	/**
	 * Calculate the implied volatility of a European call option
	 * @param spot
	 * @param strike
	 * @param optionPrice
	 * @param timeToExpiry
	 * @param riskFreeRate
	 * @param dividendYield
	 * @return
	 */
	public static double calcCallOptionImpliedVol(
						double spot,
						double strike,
						double optionPrice,
						double timeToExpiry,
						double riskFreeRate,
						double dividendYield) {
		
		double highVol = 5.0;
		double lowVol = 0.0;
		
		while (highVol - lowVol > 0.0001) {
			double volGuess = (highVol+lowVol)/2;
			double error = calcCallOptionPrice(spot, strike, volGuess, timeToExpiry, riskFreeRate, dividendYield) - optionPrice;
			if (error > 0.0) {
				highVol = volGuess;
			} else {
				lowVol = volGuess;
			}
		}
		
		return (highVol + lowVol)/2;
	}
	
	/**
	 * Calculate the implied volatility of a European put option
	 * @param spot
	 * @param strike
	 * @param optionPrice
	 * @param timeToExpiry
	 * @param riskFreeRate
	 * @param dividendYield
	 * @return
	 */
	public static double calcPutOptionImpliedVol(
					double spot,
					double strike,
					double optionPrice,
					double timeToExpiry,
					double riskFreeRate,
					double dividendYield) {

		double highVol = 5.0;
		double lowVol = 0.0;
		
		while (highVol - lowVol > 0.0001) {
			double volGuess = (highVol+lowVol)/2;
			double error = calcPutOptionPrice(spot, strike, volGuess, timeToExpiry, riskFreeRate, dividendYield) - optionPrice;
			if (error > 0.0) {
				highVol = volGuess;
			} else {
				lowVol = volGuess;
			}
		}
		
		return (highVol + lowVol)/2;
	}
	
	/**
	 * Calculate the delta of a European call option
	 * @param spot
	 * @param strike
	 * @param volatility
	 * @param timeToExpiry
	 * @param riskFreeRate
	 * @param dividendYield
	 * @return
	 */
	public static double calcCallOptionDelta(
					double spot,
					double strike,
					double volatility,
					double timeToExpiry,
					double riskFreeRate,
					double dividendYield) {
		
		double dOne = calcDOne(spot, strike, volatility, timeToExpiry, riskFreeRate, dividendYield);
		return cumulativeDistribution(dOne) * exp(-dividendYield * timeToExpiry);
		
	}

	/**
	 * Calculate the delta of a European put option
	 * @param spot
	 * @param strike
	 * @param volatility
	 * @param timeToExpiry
	 * @param riskFreeRate
	 * @param dividendYield
	 * @return
	 */
	public static double calcPutOptionDelta(
					double spot,
					double strike,
					double volatility,
					double timeToExpiry,
					double riskFreeRate,
					double dividendYield) {
		double dOne = calcDOne(spot, strike, volatility, timeToExpiry, riskFreeRate, dividendYield);
		return (cumulativeDistribution(dOne) - 1) * exp(-dividendYield * timeToExpiry);
	}
	
	/**
	 * Calculate the gamma of a European option
	 * @param spot
	 * @param strike
	 * @param volatility
	 * @param timeToExpiry
	 * @param riskFreeRate
	 * @param dividendYield
	 * @return
	 */
	public static double calcOptionGamma(
					double spot,
					double strike,
					double volatility,
					double timeToExpiry,
					double riskFreeRate,
					double dividendYield) {
		
		double dOne = calcDOne(spot, strike, volatility, timeToExpiry, riskFreeRate, dividendYield);	
		return normalProbabilityDensity(dOne) * exp(-dividendYield * timeToExpiry)/(spot * volatility * sqrt(timeToExpiry));
	}
	
	/**
	 * Calculate the vega of a European call option
	 * @param spot
	 * @param strike
	 * @param volatility
	 * @param timeToExpiry
	 * @param riskFreeRate
	 * @param dividendYield
	 * @return
	 */
	public static double calcOptionVega(
					double spot,
					double strike,
					double volatility,
					double timeToExpiry,
					double riskFreeRate,
					double dividendYield) {
		
		double dOne = calcDOne(spot, strike, volatility, timeToExpiry, riskFreeRate, dividendYield);	
		return 0.01 * spot * exp(-dividendYield * timeToExpiry) * sqrt(timeToExpiry) * normalProbabilityDensity(dOne);
	}
	
	/**
	 * Calculate the theta of a European call option
	 * @param spot
	 * @param strike
	 * @param volatility
	 * @param timeToExpiry
	 * @param riskFreeRate
	 * @param dividendYield
	 * @return
	 */
	public static double calcCallOptionTheta(
					double spot,
					double strike,
					double volatility,
					double timeToExpiry,
					double riskFreeRate,
					double dividendYield) {
		
		double dOne = calcDOne(spot, strike, volatility, timeToExpiry, riskFreeRate, dividendYield);
		double dTwo = calcDTwo(dOne, volatility, timeToExpiry);
		
		double termOne = spot * volatility * normalProbabilityDensity(dOne)/(2 * sqrt(timeToExpiry));
		double termTwo = riskFreeRate * strike * exp(-riskFreeRate * timeToExpiry) * cumulativeDistribution(dTwo) - dividendYield * spot * exp(-dividendYield * timeToExpiry) * cumulativeDistribution(dOne);
		return -(termOne + termTwo)/365;
	}
	
	/**
	 * Calculate the theta of a European put option
	 * @param spot
	 * @param strike
	 * @param volatility
	 * @param timeToExpiry
	 * @param riskFreeRate
	 * @param dividendYield
	 * @return
	 */
	public static double calcPutOptionTheta(
			double spot,
			double strike,
			double volatility,
			double timeToExpiry,
			double riskFreeRate,
			double dividendYield) {
		double dOne = calcDOne(spot, strike, volatility, timeToExpiry, riskFreeRate, dividendYield);
		double dTwo = calcDTwo(dOne, volatility, timeToExpiry);
		
		double termOne = spot * volatility * normalProbabilityDensity(dOne)/(2 * sqrt(timeToExpiry));
		double termTwo = riskFreeRate * strike * exp(-riskFreeRate * timeToExpiry) * cumulativeDistribution(-dTwo) - dividendYield * spot * exp(-dividendYield * timeToExpiry) * cumulativeDistribution(-dOne);
		return (termTwo - termOne)/365;
	}
	
	/**
	 * Calculate the rho of a European call option
	 * @param spot
	 * @param strike
	 * @param volatility
	 * @param timeToExpiry
	 * @param riskFreeRate
	 * @param dividendYield
	 * @return
	 */
	public static double calcCallOptionRho(
			double spot,
			double strike,
			double volatility,
			double timeToExpiry,
			double riskFreeRate,
			double dividendYield) {
		
		double dOne = calcDOne(spot, strike, volatility, timeToExpiry, riskFreeRate, dividendYield);
		double dTwo = calcDTwo(dOne, volatility, timeToExpiry);
		return 0.01 * strike * timeToExpiry * exp(-riskFreeRate * timeToExpiry) * cumulativeDistribution(dTwo);
	}
	
	/**
	 * Calculate the rho of a European put option
	 * @param spot
	 * @param strike
	 * @param volatility
	 * @param timeToExpiry
	 * @param riskFreeRate
	 * @param dividendYield
	 * @return
	 */
	public static double calcPutOptionRho(
			double spot,
			double strike,
			double volatility,
			double timeToExpiry,
			double riskFreeRate,
			double dividendYield) {
		
		double dOne = calcDOne(spot, strike, volatility, timeToExpiry, riskFreeRate, dividendYield);
		double dTwo = calcDTwo(dOne, volatility, timeToExpiry);
		return -0.01 * strike * timeToExpiry * exp(-riskFreeRate * timeToExpiry) * cumulativeDistribution(-dTwo);
		
	}
	
	private static double cumulativeDistribution(double x) {		
		return new NormalDistribution().cumulativeProbability(x);
	}
	
	private static double normalProbabilityDensity(double x) {
		return new NormalDistribution().density(x);
	}
	
	private static double calcDOne(
					double spot,
					double strike,
					double volatility,
					double timeToExpiry,
					double riskFreeRate,
					double dividendYield	
					) {
		return 
			(log(spot/strike) + (riskFreeRate - dividendYield + pow(volatility, 2)/2) * timeToExpiry)
					/(volatility * sqrt(timeToExpiry));
	}
	
	private static double calcDTwo(double dOne, double volatility, double timeToExpiry) {
		return dOne - volatility * sqrt(timeToExpiry);
	}
}

	
