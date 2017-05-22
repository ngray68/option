package com.ngray.option.ig.stream;

import com.lightstreamer.client.ClientListener;
import com.lightstreamer.client.ItemUpdate;
import com.lightstreamer.client.LightstreamerClient;
import com.lightstreamer.client.Subscription;
import com.lightstreamer.client.SubscriptionListener;
import com.ngray.option.Log;
import com.ngray.option.financialinstrument.FinancialInstrument;
import com.ngray.option.ig.market.Market;
import com.ngray.option.marketdata.MarketData;
import com.ngray.option.marketdata.MarketData.Type;
import com.ngray.option.marketdata.MarketDataPublisher;

public class LivePriceStream {

	private final static String MARKET_PATTERN = "MARKET:{epic}";
	private final static String[] MARKET_FIELDS = { "BID", "OFFER" };
	private LightstreamerClient client;
	
	public LivePriceStream(String lightStreamerEndpoint, String activeAccountId, String clientToken, String accountToken) {
		this.client = new LightstreamerClient(lightStreamerEndpoint, null);
		this.client.connectionDetails.setUser(activeAccountId);
	
		String pwd = "CST-" + clientToken + "|XST-" + accountToken;
		this.client.connectionDetails.setPassword(pwd);
	
		this.client.addListener(new ClientListener() {

			@Override
			public void onListenEnd(LightstreamerClient client) {
				Log.getLogger().debug("onListenEnd");		
			}

			@Override
			public void onListenStart(LightstreamerClient client) {
				Log.getLogger().debug("onListenStart");
			}

			@Override
			public void onPropertyChange(String property) {
				Log.getLogger().debug("onPropertyChange: " + property);	
			}

			@Override
			public void onServerError(int errorCode, String errorMessage) {
				Log.getLogger().debug("onServerError: " + errorCode + ": " + errorMessage);		
			}

			@Override
			public void onStatusChange(String status) {
				Log.getLogger().debug("onStatusChange: " + status);	
			}});
		
		Log.getLogger().info("Connecting to Lightstreamer server");
		client.connect();
	}
	
	
	public LivePriceStream(LightstreamerClient client) {
		this.client = client;
	}


	/**
	 * Add a subscription to the lightstreamer client for the instrument.
	 * Use the  MarketDataPublisher to publish streamed prices to the outside world
	 * @param instrument
	 * @param publisher
	 */
	public void addSubscription(FinancialInstrument instrument, MarketDataPublisher publisher) {
		Log.getLogger().info("Adding LivePriceStream subscription: " + instrument.getIdentifier());
		if (instrument == null || publisher == null) {
			Log.getLogger().error("LivePriceStream::addSubscription called with null parameter (FinancialInstrument or MarketDataPubliser) ignored");
			return;
		}
		
		Market market = instrument.getIGMarket();
		if (market == null) {
			Log.getLogger().error("LivePriceStream::addSubscription - the instrument for which the subscription is being added has no IG market information - ignored");
			return;
		}
		
		String[] group = { MARKET_PATTERN.replace("{epic}", market.getEpic()) };
		Subscription subs = new Subscription("MERGE", group, MARKET_FIELDS);
		SubscriptionListener subsListener = new SubscriptionListener() {

			@Override
			public void onClearSnapshot(String itemName, int itemPos) {
				// not implemented
				
			}

			@Override
			public void onCommandSecondLevelItemLostUpdates(int lostUpdates, String key) {
				// not implemented
				
			}

			@Override
			public void onCommandSecondLevelSubscriptionError(int code, String message, String key) {
				// not implemented
				
			}

			@Override
			public void onEndOfSnapshot(String itemName, int itemPos) {
				// not implemented
				
			}

			@Override
			public void onItemLostUpdates(String itemName, int itemPos, int lostUpdates) {
				// not implemented
				
			}

			@Override
			public void onItemUpdate(ItemUpdate itemUpdate) {
				String bid = itemUpdate.getValue("BID");
				String offer = itemUpdate.getValue("OFFER");
				double bidVal = Double.parseDouble(bid);
				double offerVal = Double.parseDouble(offer);
				publisher.publishMarketData(instrument, new MarketData(instrument.getIdentifier(), bidVal, offerVal, Type.PRICE));
			}

			@Override
			public void onListenEnd(Subscription subscription) {
				// not implemented
				
			}

			@Override
			public void onListenStart(Subscription subscription) {
				// not implemented
				
			}

			@Override
			public void onSubscription() {
				// not implemented
			}

			@Override
			public void onSubscriptionError(int code, String message) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onUnsubscription() {
				// TODO Auto-generated method stub
				
			}
			
		};
		
		subs.addListener(subsListener);
		client.subscribe(subs);
	}

	/**
	 * Disconnect from the Lightstreamer server
	 */
	public void disconnect() {
		Log.getLogger().info("Disconnecting from Lightstreamer server");
		if (client != null) {
			client.disconnect();	
		}
	}
	
}
