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
import com.ngray.option.financialinstrument.Security;
import com.ngray.option.ig.Session;
import com.ngray.option.ig.SessionException;
import com.ngray.option.ig.SessionLoginDetails;
import com.ngray.option.ig.position.IGPositionList;
import com.ngray.option.ig.refdata.MissingReferenceDataException;
import com.ngray.option.ig.stream.LivePriceStream;
import com.ngray.option.ig.stream.StreamManager;

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
		StreamManager streamManager = new StreamManager(lightStreamerEndpoint, activeAccountId, cst, xst);
		LivePriceStream stream = streamManager.getLivePriceStream();//LivePriceStream stream = new LivePriceStream(lightStreamerEndpoint, activeAccountId, cst, xst);
		assertTrue(stream != null);
		streamManager.shutdown();
	}

	@Test
	public void testAddSubscription() throws SessionException, InterruptedException {
		
		Log.getLogger().info("Testng LivePriceStream::addSubscription...");
		String activeAccountId = session.getSessionInfo().getCurrentAccountId();
		String lightStreamerEndpoint = session.getSessionInfo().getLightStreamerEndpoint();
		String xst = session.getXSecurityToken();
		String cst = session.getClientSecurityToken();
		StreamManager streamManager = new StreamManager(lightStreamerEndpoint, activeAccountId, cst, xst);
		LivePriceStream stream = streamManager.getLivePriceStream();// new LivePriceStream(lightStreamerEndpoint, activeAccountId, cst, xst);
		assertTrue(stream != null);
		
		// keep track of message  count
		AtomicInteger count = new AtomicInteger(0);
		FinancialInstrument instrument = new Security("IX.D.FTSE.DAILY.IP");
		stream.addSubscription(
				instrument, 
				(instr, marketData) -> { 
						System.out.println("Instrument: " + instr.getIdentifier() + " Mid-price: " + marketData.getMid()); 
						count.incrementAndGet();
					} 
				);
		
		// sleep for a while to allow time to receive some messages
		Thread.sleep(30000);
		Log.getLogger().info("Received " + count.get() + " messages");
		assertTrue(count.get() > 0);
		streamManager.shutdown();
	}
}
