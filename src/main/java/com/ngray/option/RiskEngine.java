package com.ngray.option;

import java.awt.EventQueue;
import java.awt.HeadlessException;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDate;


import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTable;

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
		boolean isLive = false;
		
		if (args.length != 1 && args.length != 2) {
			Log.getLogger().fatal("Usage: <logindetailsfile> [DEMO|LIVE (default DEMO]");
			return;
		}
		
		if (args.length == 1 && (args[0].equals("DEMO") || args[0].equals("LIVE"))) {
			Log.getLogger().fatal("Usage: <logindetailsfile> [DEMO|LIVE (default DEMO]");
			return;
		} else {
			loginFileName = args[0];
		}
		
		if (args.length == 2 && (args[0].equals("DEMO") || args[0].equals("LIVE"))) {
			loginFileName = args[1];
			isLive = args[0].equals("LIVE") ? true : false;		
		} else if (args.length == 2 && (args[1].equals("DEMO") || args[1].equals("LIVE"))) {
			loginFileName = args[0];
			isLive = args[1].equals("LIVE") ? true : false;	
		} else {
			Log.getLogger().fatal("Usage: <logindetailsfile> [DEMO|LIVE (default DEMO]");
			return;
		}
	
		
		
		SessionLoginDetails loginDetails = null;
		try {
			loginDetails = SessionLoginDetails.fromJson(readFile(loginFileName));
		} catch (IOException e) {
			Log.getLogger().fatal("Error reading login details: " + e.getMessage(), e);
			return;
		}
		
		Session session = null;
		try {
			session = Session.login(loginDetails, isLive);
			// static data initialization
			OptionReferenceDataMap.init(session);

			String activeAccountId = session.getSessionInfo().getCurrentAccountId();
			String lightStreamerEndpoint = session.getSessionInfo().getLightStreamerEndpoint();
			String xst = session.getXSecurityToken();
			String cst = session.getClientSecurityToken();
			
			LivePriceStream livePriceStream = new LivePriceStream(lightStreamerEndpoint, activeAccountId, cst, xst);
			MarketDataService marketDataService = new MarketDataService("LIVE", livePriceStream);
			RiskService riskService = new RiskService("LIVE", marketDataService, LocalDate.now());
			
			PositionService positionService = new PositionService("LIVE");
			positionService.initialize(session);
			positionService.subscribeAllToMarketDataService(marketDataService);
			positionService.subscribeAllToRiskService(riskService);
			
			PositionRiskTableModel model = new PositionRiskTableModel(positionService.getPositions());
			JTable table = new JTable(model);
			JScrollPane pane = new JScrollPane(table);
			JFrame frame = new JFrame();
			frame.add(pane);
			frame.pack();
			EventQueue.invokeLater(()-> {
				try {
					frame.setVisible(true);
				} catch (HeadlessException e) {
					Log.getLogger().error(e.getMessage(), e);
				}
			});
			
			positionService.getPositions().forEach(position->positionService.addListener(position, model));
			while (true) {}
			
		} catch (SessionException e) {
			Log.getLogger().fatal(e.getMessage(), e);
			return;
		}
		
		
		
		
		
		
	}

}
