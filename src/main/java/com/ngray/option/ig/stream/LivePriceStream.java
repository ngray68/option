package com.ngray.option.ig.stream;

import java.util.HashMap;
import java.util.Map;

import com.lightstreamer.client.ItemUpdate;
import com.lightstreamer.client.LightstreamerClient;
import com.lightstreamer.client.Subscription;
import com.lightstreamer.client.SubscriptionListener;
import com.ngray.option.Log;
import com.ngray.option.financialinstrument.FinancialInstrument;
import com.ngray.option.ig.market.Market;
import com.ngray.option.marketdata.MarketData;
import com.ngray.option.marketdata.MarketData.Type;
import com.ngray.option.service.ServiceDataPublisher;
import com.ngray.option.service.ServiceDataSource;

public class LivePriceStream implements ServiceDataSource<FinancialInstrument, MarketData> {

	private final static String MARKET_PATTERN = "MARKET:{epic}";
	private final static String[] MARKET_FIELDS = { "BID", "OFFER" };
	
	private final LightstreamerClient client;
	
	private Map<FinancialInstrument, Subscription> subscriptions;
	
	public LivePriceStream(LightstreamerClient client) {
		this.client = client;
		this.subscriptions = new HashMap<>();
	}

	@Override
	public void addSubscription(FinancialInstrument instrument, ServiceDataPublisher<FinancialInstrument, MarketData> publisher) {
		Log.getLogger().info("Adding LivePriceStream subscription: " + instrument.getIdentifier());
		if (instrument == null || publisher == null) {
			Log.getLogger().error("LivePriceStream::addSubscription called with null parameter (FinancialInstrument or MarketDataPubliser) ignored");
			return;
		}
		
		String[] group = new String[1];
		Market market = instrument.getIGMarket();
		if (market == null) {
			Log.getLogger().warn("LivePriceStream::addSubscription - the instrument for which the subscription is being added has no IG market information");
			Log.getLogger().warn("LivePriceStream::addSubscription - using instrument ID to subscribe, this will only work if the ID is identical the IG epic");
			group[0] = MARKET_PATTERN.replace("{epic}", instrument.getIdentifier());
		}	
		else {
			group[0] = MARKET_PATTERN.replace("{epic}", market.getEpic());
		}
		
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
				publisher.publish(instrument, new MarketData(instrument.getIdentifier(), bidVal, offerVal, Type.PRICE));
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
		
		subscriptions.put(instrument, subs);
		subs.addListener(subsListener);
		client.subscribe(subs);
		
	}

	@Override
	public void removeSubscription(FinancialInstrument instrument) {
		if (subscriptions.containsKey(instrument)) {
			Subscription subscription = subscriptions.get(instrument);
			client.unsubscribe(subscription);
			subscriptions.remove(instrument);
		}	
	}

	@Override
	public void start() {
		// nothing to do here
	}

	@Override
	public void shutdown() {
		Log.getLogger().info("Shutting down LivePriceStream...");
		subscriptions.forEach((instrument, subscription) -> client.unsubscribe(subscription));
		subscriptions.clear();	
		// don't disconnect the lightstreamer client as it isn't owned by this
	}

}
