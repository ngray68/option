package com.ngray.option;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.ngray.option.financialinstrument.EuropeanOption.Type;
import com.ngray.option.ig.refdata.OptionReferenceData;
import com.ngray.option.ig.refdata.OptionReferenceDataWriter;

/**
 * Utility class which generates a .csv file containing a list of option reference data
 * eg.
 * OptionEpic,UnderlyingEpic,Strike,Expiry,CallOrPut,DividendYield,RiskFreeRate
 * OP.D.FTSE6.7325P.IP,IX.D.FTSE.MONTH1.IP,7325,2017-06-16,PUT,0,0.0031
 * OP.D.FTSE6.7350C.IP,IX.D.FTSE.MONTH1.IP,7350,2017-06-16,CALL,0,0.0031
 * OP.D.FTSE6.7400P.IP,IX.D.FTSE.MONTH1.IP,7400,2017-06-16,PUT,0,0.0031
 * OP.D.FTSE6.7475P.IP,IX.D.FTSE.MONTH1.IP,7475,2017-06-16,PUT,0,0.0031
 *
 * given an underlying epic eg. IX.D.FTSE.MONTH1.IP, an option epic form
 * eg. OP.D.FTSE6.{STRIKE}{CALLORPUT}.IP, and an expiry date eg. 2017-06-16, a 
 * dividend yield, and a risk free rate to use in Black Scholes formula.
 * @author nigelgray
 *
 */
public class OptionReferenceDataGenerator {

	private String underlyingEpic;
	private String optionEpicForm;
	private LocalDate expiryDate;
	private double dividendYield;
	private double riskFreeRate;
	private double minStrike;
	private double maxStrike;
	private double strikeIncrement;
	
	public static void main(String[] args) {		
			try {
				// expect 5 arguments - underlying, optionEpicForm, expiryDate and div yield,and riskFreeRate, 
				// minStrike, maxStrike, strikeIncrement in that order
				if (args.length != 8) throw new Exception("Missing argument");
				
				OptionReferenceDataGenerator generator = new OptionReferenceDataGenerator();
				generator.setUnderlyingEpic(args[0]);
				generator.setOptionEpicForm(args[1]);
				generator.setExpiryDate(LocalDate.parse(args[2]));
				generator.setDividendYield(Double.parseDouble(args[3]));
				generator.setRiskFreeRate(Double.parseDouble(args[4]));
				generator.setMinStrike(Double.parseDouble(args[5]));
				generator.setMaxStrike(Double.parseDouble(args[6]));
				generator.setStrikeIncrement(Double.parseDouble(args[7]));
				
				// rudimentary checks
				if (Double.compare(generator.getMinStrike(), generator.getMaxStrike()) >= 0) {
					throw new Exception("Mininum strike must be less than maximum strike");
				}
				
				if(Double.compare(generator.getDividendYield(), 0.0) < 0) {
					throw new Exception("Dividend yield must be zero or positive");
				}
				
				// we can have negative interest rates... so let's not do this
				/*
				if(Double.compare(generator.getRiskFreeRate(), 0.0) < 0) {
					throw new Exception("Dividend yield must be zero or positive");
				}*/
				
				// now generate the option ref data
				List<OptionReferenceData> refDataList = new ArrayList<OptionReferenceData>();
				for (double strike = generator.getMinStrike(); strike <= generator.getMaxStrike(); strike += generator.getStrikeIncrement()) {
					String optionEpicForm = generator.getOptionEpicForm();
					String callOptionEpic = 
							optionEpicForm.replace("{STRIKE}", Integer.toString((int)strike))
										  .replace("{CALLORPUT}", "C");			
					refDataList.add(new OptionReferenceData(callOptionEpic, generator.getUnderlyingEpic(), strike, generator.getExpiryDate(), Type.CALL, generator.getDividendYield(), generator.getRiskFreeRate()));
					
					String putOptionEpic = 
							optionEpicForm.replace("{STRIKE}", Integer.toString((int)strike))
										  .replace("{CALLORPUT}", "P");
					refDataList.add(new OptionReferenceData(putOptionEpic, generator.getUnderlyingEpic(), strike, generator.getExpiryDate(), Type.PUT, generator.getDividendYield(), generator.getRiskFreeRate()));
				}
				
				String filename = "OptionReferenceData-" + generator.getUnderlyingEpic() + ".csv";
				OptionReferenceDataWriter.writeToResource(filename, refDataList);
				
				
			} catch (Exception e) {
				Log.getLogger().fatal(e.getMessage(), true);
			}
	}

	public String getUnderlyingEpic() {
		return underlyingEpic;
	}

	public void setUnderlyingEpic(String underlyingEpic) {
		this.underlyingEpic = underlyingEpic;
	}

	public String getOptionEpicForm() {
		return optionEpicForm;
	}

	public void setOptionEpicForm(String optionEpicForm) {
		this.optionEpicForm = optionEpicForm;
	}

	public LocalDate getExpiryDate() {
		return expiryDate;
	}

	public void setExpiryDate(LocalDate expiryDate) {
		this.expiryDate = expiryDate;
	}

	public double getDividendYield() {
		return dividendYield;
	}

	public void setDividendYield(double dividendYield) {
		this.dividendYield = dividendYield;
	}

	public double getRiskFreeRate() {
		return riskFreeRate;
	}

	public void setRiskFreeRate(double riskFreeRate) {
		this.riskFreeRate = riskFreeRate;
	}

	public double getMinStrike() {
		return minStrike;
	}

	public void setMinStrike(double minStrike) {
		this.minStrike = minStrike;
	}

	public double getMaxStrike() {
		return maxStrike;
	}

	public void setMaxStrike(double maxStrike) {
		this.maxStrike = maxStrike;
	}

	public double getStrikeIncrement() {
		return strikeIncrement;
	}

	public void setStrikeIncrement(double strikeIncrement) {
		this.strikeIncrement = strikeIncrement;
	}
	
	

}
