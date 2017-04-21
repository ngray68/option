package com.ngray.option.ig.test;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Before;
import org.junit.Test;

import com.ngray.option.Log;
import com.ngray.option.financialinstrument.FinancialInstrument;
import com.ngray.option.ig.Session;
import com.ngray.option.ig.SessionException;
import com.ngray.option.ig.SessionLoginDetails;
import com.ngray.option.ig.position.IGPositionList;
import com.ngray.option.ig.refdata.MissingReferenceDataException;
import com.ngray.option.ig.stream.LivePriceStream;

public class TestLivePriceStream {

	Session session = null;
	private String loginDetailsFile = "/Users/nigelgray/Documents/details.txt";
	
	private String readFile(String fileName) throws IOException {
		String result = "";
		try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
			String nextLine = null;
			while ((nextLine = reader.readLine()) != null) {
				result += nextLine;
			}
		}
		return result;
	}

	
	@Before
	public void setUp() throws Exception {
		boolean isLive = false;
		String json = readFile(loginDetailsFile);
		session = Session.login(SessionLoginDetails.fromJson(json), isLive);
	}
	
	@Test
	public void testCreateLivePriceStream() {
		Log.getLogger().info("Testng create LivePriceStream...");
		String activeAccountId = session.getSessionInfo().getCurrentAccountId();
		String lightStreamerEndpoint = session.getSessionInfo().getLightStreamerEndpoint();
		String xst = session.getXSecurityToken();
		String cst = session.getClientSecurityToken();
		LivePriceStream stream = new LivePriceStream(lightStreamerEndpoint, activeAccountId, cst, xst);
		assertTrue(stream != null);
		stream.disconnect();
	}

	@Test
	public void testAddSubscription() throws SessionException, InterruptedException {
		
		Log.getLogger().info("Testng LivePriceStream::addSubscription...");
		String activeAccountId = session.getSessionInfo().getCurrentAccountId();
		String lightStreamerEndpoint = session.getSessionInfo().getLightStreamerEndpoint();
		String xst = session.getXSecurityToken();
		String cst = session.getClientSecurityToken();
		LivePriceStream stream = new LivePriceStream(lightStreamerEndpoint, activeAccountId, cst, xst);
		assertTrue(stream != null);
		
		// keep track of message  count
		AtomicInteger count = new AtomicInteger(0);
		IGPositionList positions = session.getPositions();
		positions.getPositions().forEach((position)->{
			try {
				stream.addSubscription(
						FinancialInstrument.fromIGMarket(position.getMarket()), 
						(instr, marketData) -> { 
								System.out.println("Instrument: " + instr.getIdentifier() + " Mid-price: " + marketData.getValue()); 
								count.incrementAndGet();
							} 
						);
			} catch (MissingReferenceDataException e) {
				e.printStackTrace();
			} });
		
		// sleep for a while to allow time to receive some messages
		Thread.sleep(30000);
		Log.getLogger().info("Received " + count.get() + " messages");
		assertTrue(count.get() > 0);
		stream.disconnect();
	}
}
