package com.ngray.option;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import com.ngray.option.financialinstrument.EuropeanOption;
import com.ngray.option.financialinstrument.EuropeanOption.Type;
import com.ngray.option.financialinstrument.FinancialInstrument;
import com.ngray.option.financialinstrument.Security;
import com.ngray.option.marketdata.MarketData;
import com.ngray.option.marketdata.MarketDataCollection;
import com.ngray.option.model.ModelException;
import com.ngray.option.risk.Risk;

/**
 * SimpleOptionPricer - simple command line option pricing
 * @author nigelgray
 *
 */
public class SimpleOptionPricer {

	/**
	 * Entry point - supply the following args to price an option
	 * -p <optionPrice> -u <underlyingPrice>
	 * The implied volatility (IV) and option greeks will be calculated
	 * and output to console, or;
	 * -v <volatility> -u <underlyingPrice>
	 * The theoretical option value and greeks will be calculated and
	 * output to console
	 * The following arguments must be supplied in all cases:
	 * -s <strikePrice>
	 * -e <expiryDate>
	 * -t <PUT|CALL>
	 * -d <valueDate>
	 * 
	 * @param args
	 */
	public static void main(String[] args) {	
		try {
			Config config = parseArgs(args);
			Security underlying = new Security("Underlying");
			EuropeanOption option = new EuropeanOption("Option", underlying, config.strike, config.expiryDate, config.putOrCall);
			Map<FinancialInstrument, MarketData> prices = new HashMap<>();
			prices.put(underlying, new MarketData(config.underlyingPrice, MarketData.Type.PRICE));
			prices.put(option, new MarketData(config.optionPriceOrVolatility,  config.isVolatility ? MarketData.Type.VOLATILITY : MarketData.Type.PRICE));
			MarketDataCollection marketData = new MarketDataCollection(prices);
			Risk risk = option.getModel().calculateRisk(option, marketData, config.valueDate);
			System.out.println(risk);
		} catch (ParseException | ModelException e) {
			e.printStackTrace();
		}

	}
	
	public static class Config {
		public double optionPriceOrVolatility = Double.NaN;
		public double underlyingPrice = Double.NaN;
		public double strike = Double.NaN;
		public LocalDate expiryDate = LocalDate.MIN;
		public LocalDate valueDate = LocalDate.MIN;;
		public EuropeanOption.Type putOrCall;
		public boolean isVolatility;
	}
	
	private static Config parseArgs(String[] args) throws ParseException {
		Config config = new Config();
		Options options = new Options();
		options.addOption("p", true, "-p <optionPrice>");
		options.addOption("v", true, "-v <volatility>");
		options.addOption("u", true, "-u <underlyingPrice");
		options.addOption("s", true, "-s <strikePrice>");
		options.addOption("e", true, "-e <expiryDate>");
		options.addOption("t", true,  "-t <PUT|CALL>");
		options.addOption("d", true,  "-d <valueDate>");
		
		DefaultParser parser = new DefaultParser();
		CommandLine cmdLine = parser.parse(options, args);
		cmdLine.iterator().forEachRemaining( 
				option -> { try {
					setOption(config, option);
				} catch (ParseException e) {
					System.out.println(e.getMessage());
					e.printStackTrace();
				} });
		
		checkValidity(config);
		return config;
	}

	private static void checkValidity(Config config) throws ParseException {
		if (Double.isNaN(config.optionPriceOrVolatility)) {
			 throw new ParseException("Missing option price or volatility");
		} else if (Double.isNaN(config.underlyingPrice)) {
			 throw new ParseException("Missing underlying price");	
		} else if (Double.isNaN(config.strike)) {
			 throw new ParseException("Missing strike price");
		} else if (config.expiryDate.equals(LocalDate.MIN)) {
			 throw new ParseException("Missing expiry date");
		} else if (config.valueDate.equals(LocalDate.MIN)) {
			 throw new ParseException("Missing value date");
		}
		
	}

	private static void setOption(Config config, Option option) throws ParseException {
		switch (option.getOpt()) {
		case "p":
			config.optionPriceOrVolatility = Double.parseDouble(option.getValue());
			config.isVolatility = false;
			break;
		case "u":
			config.underlyingPrice = Double.parseDouble(option.getValue());
			break;
		case "v":
			config.optionPriceOrVolatility = Double.parseDouble(option.getValue());
			config.isVolatility = true;
			break;
		case "s":
			config.strike = Double.parseDouble(option.getValue());
			break;
		case "e":
			config.expiryDate = LocalDate.parse(option.getValue());
			break;
		case "t":
			if (option.getValue().equals("CALL")) {
				config.putOrCall = Type.CALL;
			} else if (option.getValue().equals("PUT")) {
				config.putOrCall = Type.PUT;
			} else {
				throw new ParseException("Unrecognised option type - should be PUT or CALL");
			}
			break;
		case "d":
			config.valueDate = LocalDate.parse(option.getValue());
			break;
		default:
			throw new ParseException("Unrecognised command line option " + option.getOpt());
		}
	}
}
