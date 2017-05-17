package com.ngray.option;

import java.awt.EventQueue;
import java.awt.HeadlessException;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDate;
import java.util.Set;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import com.ngray.option.financialinstrument.FinancialInstrument;
import com.ngray.option.ig.Session;
import com.ngray.option.ig.SessionException;
import com.ngray.option.ig.SessionLoginDetails;
import com.ngray.option.ig.refdata.OptionReferenceDataMap;
import com.ngray.option.ig.stream.LivePriceStream;
import com.ngray.option.marketdata.MarketDataService;
import com.ngray.option.position.PositionService;
import com.ngray.option.risk.RiskService;
import com.ngray.option.ui.PositionRiskTableModel;

/**
 * This class provides the entry-point for the live risk engine program.
 * @author nigelgray
 *
 */
public class RiskEngine {
	
	private static Session session = null;
	private static LivePriceStream livePriceStream = null;
	private static MarketDataService marketDataService = null;
	private static RiskService riskService = null;
	private static PositionService positionService = null;
	private static Object waitLock = new Object();

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
			refDataFileName = "/OptionReferenceData.csv";
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
			
			livePriceStream = new LivePriceStream(lightStreamerEndpoint, activeAccountId, cst, xst);
			marketDataService = new MarketDataService("LIVE", livePriceStream);
			riskService = new RiskService("LIVE", marketDataService, LocalDate.now());
			
			positionService = new PositionService("LIVE");
			positionService.initialize(session);
			positionService.subscribeAllToMarketDataService(marketDataService);
			positionService.subscribeAllToRiskService(riskService);
		
			Set<FinancialInstrument> underlyings = positionService.getUnderlyings();			
			underlyings.forEach(underlying -> {
				PositionRiskTableModel model = new PositionRiskTableModel(positionService.getPositions(underlying));
				JTable table = new JTable(model);
				JScrollPane pane = new JScrollPane(table);
				JFrame frame = new JFrame();
				frame.add(pane);
				frame.pack();
				frame.setTitle(underlying.getName());
				
				EventQueue.invokeLater(()-> {
					try {
						frame.setVisible(true);
					} catch (HeadlessException e) {
						Log.getLogger().error(e.getMessage(), e);
					}
				});
				
				positionService.getPositions(underlying).forEach(position->positionService.addListener(position, model));
			});
		
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
}
