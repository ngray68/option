package com.ngray.option;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDate;

import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.ngray.option.analysis.scenario.ScenarioDataSource;
import com.ngray.option.analysis.scenario.ScenarioService;
import com.ngray.option.data.HistoricalPriceCache;
import com.ngray.option.ig.Session;
import com.ngray.option.ig.SessionException;
import com.ngray.option.ig.SessionLoginDetails;
import com.ngray.option.ig.refdata.OptionReferenceDataMap;
import com.ngray.option.ig.stream.StreamManager;
import com.ngray.option.marketdata.MarketDataService;
import com.ngray.option.mongo.Mongo;
import com.ngray.option.mongo.PriceCodec;
import com.ngray.option.mongo.VolatilitySurfaceDefinitionCodec;
import com.ngray.option.position.PositionService;
import com.ngray.option.position.PositionUpdateService;
import com.ngray.option.risk.RiskService;
import com.ngray.option.ui.MainUI;

/**
 * This class provides the entry-point for the live risk engine program.
 * @author nigelgray
 *
 */
public class RiskEngine {
	
	private static Session session = null;
	private static MarketDataService marketDataService = null;
	private static RiskService riskService = null;
	private static PositionService positionService = null;
	private static Object waitLock = new Object();
	private static PositionUpdateService positionUpdateService = null;
	private static ScenarioService scenarioService = null;
	//private static MongoClient mongoClient = null;
	private static HistoricalPriceCache priceCache = null;
	
	private static String readFile(String fileName) throws IOException {
		String result = "";
		try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
			String nextLine = null;
			while ((nextLine = reader.readLine()) != null) {
				result += nextLine;
			}
		}
		return result;
	}
	
	/**
	 * Entry-point. Arguments are:
	 * 1. filename of file containing json login details and api key
	 * 2. flag to indicate whether the account is live or demo (default = demo)
	 * @param args
	 */
	public static void main(String[] args) {
		
		String loginFileName = null;
		String refDataFileName = null;
		boolean isLive = false;
		boolean fromResource = true;
		
		if (args.length != 2 && args.length != 3) {
			Log.getLogger().fatal("Usage: <logindetailsfile> [<optionrefdatafile>] DEMO|LIVE");
			return;
		}
		
		if (args.length == 3) {
			loginFileName = args[0];
			refDataFileName = args[1];
			isLive = args[2].equals("LIVE") ? true : false;
			fromResource = false;
		}
		else if (args.length == 2) {
			loginFileName = args[0];
			// NG will load all files matching the pattern below that are resources in the classpath
			refDataFileName = "/OptionReferenceData*.csv";
			isLive = args[1].equals("LIVE") ? true : false;
		}
		
		SessionLoginDetails loginDetails = null;
		try {
			loginDetails = SessionLoginDetails.fromJson(readFile(loginFileName));
		} catch (IOException e) {
			Log.getLogger().fatal("Error reading login details: " + e.getMessage(), e);
			return;
		}
		
		try {
			session = Session.login(loginDetails, isLive);
			// static data initialization
			OptionReferenceDataMap.init(refDataFileName, session, fromResource);

			String activeAccountId = session.getSessionInfo().getCurrentAccountId();
			String lightStreamerEndpoint = session.getSessionInfo().getLightStreamerEndpoint();
			String xst = session.getXSecurityToken();
			String cst = session.getClientSecurityToken();
			
			priceCache = new HistoricalPriceCache("RiskEngine");
			
			StreamManager streamManager = new StreamManager(lightStreamerEndpoint, activeAccountId, cst, xst);
			marketDataService = new MarketDataService("MarketData-LIVE", streamManager.getLivePriceStream());
			positionUpdateService = new PositionUpdateService("PositionUpdate-LIVE", streamManager.getPositionUpdateStream());
			riskService = new RiskService("LIVE", marketDataService, LocalDate.now());
			
			positionService = new PositionService("LIVE", session, riskService, marketDataService, positionUpdateService);
			positionService.initialize();
			
			// Create without a data source for now - will add when we do live updates
			scenarioService = new ScenarioService("LIVE", new ScenarioDataSource("ScenarioDataSource-LIVE", positionService));
			
			Mongo.initialize();
			// TODO mongo authentication credentials
			/*
			CodecRegistry codecRegistry = CodecRegistries.fromRegistries(
					MongoClient.getDefaultCodecRegistry(),
					CodecRegistries.fromCodecs(new VolatilitySurfaceDefinitionCodec(MongoClient.getDefaultCodecRegistry())),
					CodecRegistries.fromCodecs(new PriceCodec(MongoClient.getDefaultCodecRegistry()))
				);
			 MongoClientOptions options = MongoClientOptions.builder().codecRegistry(codecRegistry)
			            .build();

			mongoClient = new MongoClient("localhost:27017", options);
			*/
			
			new MainUI().show();
			Runtime.getRuntime().addShutdownHook(new Thread() {

				@Override
				public void run() {
					shutdown();
				}});
			
			// wait forever
			synchronized(waitLock) {
				waitLock.wait();
			}
		} catch (SessionException | InterruptedException e) {
			Log.getLogger().fatal(e.getMessage(), e);
		}		
	}
	
	/**
	 * Shutdown the risk engine
	 */
	public static void shutdown() {
		Log.getLogger().info("RiskEngine shutdown initiated...");
		if (marketDataService != null) marketDataService.shutdown();
		if (riskService != null) riskService.shutdown();
		if (positionService != null) positionService.shutdown();
		try {
			if (session != null) session.logout();
		} catch (SessionException e) {
			Log.getLogger().error("Error logging out of IG Session during shutdown", e);
		}
	}

	/**
	 * Get the position service
	 * @return
	 */
	public static PositionService getPositionService() {
		return positionService;
	}

	/**
	 * Get the market data service
	 * @return
	 */
	public static MarketDataService getMarketDataService() {
		return marketDataService;
	}
	
	/**
	 * Get the scenario service
	 * @return
	 */
	public static ScenarioService getScenarioService() {
		return scenarioService;
	}
	
	/**
	 * Get the mongoclient instance
	 * @return
	 */
	/*
	public static MongoClient getMongoClient() {
		return mongoClient;
	}*/
	
	/**
	 * Get the IG session
	 * @return
	 */
	public static Session getSession() {
		return session;
	}
	
	/**
	 * Get the historical price cache
	 * @return
	 */
	public static HistoricalPriceCache getHistoricalPriceCache() {
		return priceCache ;
	}
	
	// The following methods are used to inject test instances of RiskEngine resources for use in
	// JUnit tests where a full RiskEngine instance isn't required and PowerMockito doesn't work with
	// eg HTTP or Mongo.
	/**
	 * Set the test IG session
	 * @param session
	 */
	public static void setTestSession(Session thisSession) {
		session = thisSession;
	}
	
	/**
	 * Set the test mongo client instance
	 * @param thisClient
	 */
	/*
	public static void setTestMongoClient(MongoClient thisClient) {
		mongoClient = thisClient;
	}*/

	/**
	 * Set the test price cache
	 * @param cache
	 */
	public static void setHistoricalPriceCache(HistoricalPriceCache cache) {
		priceCache = cache;
	}
}
