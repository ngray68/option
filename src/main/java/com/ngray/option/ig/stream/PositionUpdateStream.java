package com.ngray.option.ig.stream;

import com.ngray.option.service.ServiceDataPublisher;
import com.ngray.option.service.ServiceDataSource;

import java.util.HashMap;
import java.util.Map;

import com.lightstreamer.client.ItemUpdate;
import com.lightstreamer.client.LightstreamerClient;
import com.lightstreamer.client.Subscription;
import com.lightstreamer.client.SubscriptionListener;
import com.ngray.option.Log;
import com.ngray.option.ig.position.IGPositionUpdate;


public class PositionUpdateStream implements ServiceDataSource<String, IGPositionUpdate> {

	private static final String TRADE_PATTERN = "TRADE:{accountId}";
	private static final String[] POSITION_UPDATE_FIELDS = { "OPU" };
	
	private final LightstreamerClient lsClient;
	
	private Map<String, Subscription> subscriptions;
	
	public PositionUpdateStream(LightstreamerClient lsClient) {	
		this.lsClient = lsClient;
		this.subscriptions = new HashMap<>();
	}
		
	@Override
	public void addSubscription(String accountId, ServiceDataPublisher<String, IGPositionUpdate> publisher) {
		
		String[] group = { TRADE_PATTERN.replace("{accountId}", accountId) };
		Subscription subscription = new Subscription("DISTINCT", group, POSITION_UPDATE_FIELDS);
		
		subscription.addListener(new SubscriptionListener() {

			@Override
			public void onClearSnapshot(String itemName, int itemPos) {
				// unimplemented
			}

			@Override
			public void onCommandSecondLevelItemLostUpdates(int lostUpdates, String key) {
				// unimplemented
			}

			@Override
			public void onCommandSecondLevelSubscriptionError(int code, String message, String key) {
				// unimplemented
			}

			@Override
			public void onEndOfSnapshot(String itemName, int itemPos) {
				// unimplemented
			}

			@Override
			public void onItemLostUpdates(String itemName, int itemPos, int lostUpdates) {
				// unimplemented
			}

			@Override
			public void onItemUpdate(ItemUpdate itemUpdate) {
				Log.getLogger().debug("PositionUpdateDataSource: onItemUpdate called");
				IGPositionUpdate positionUpdate = IGPositionUpdate.fromJson(itemUpdate.getValue("OPU"));
				publisher.publish(accountId, positionUpdate);
			}

			@Override
			public void onListenEnd(Subscription subscription) {
				// unimplemented
			}

			@Override
			public void onListenStart(Subscription subscription) {
				// unimplemented
			}

			@Override
			public void onSubscription() {
				// unimplemented
			}

			@Override
			public void onSubscriptionError(int code, String message) {
				// unimplemented
			}

			@Override
			public void onUnsubscription() {
				// unimplemented
				
			}});
		
		subscriptions.put(accountId, subscription);
		lsClient.subscribe(subscription);
		
	}

	@Override
	public void removeSubscription(String accountId) {
		if (subscriptions.containsKey(accountId)) {
			Subscription subscription = subscriptions.get(accountId);
			lsClient.unsubscribe(subscription);
			subscriptions.remove(accountId);
		}		
	}

	@Override
	public void start() {
		// Nothing to do in this case - lsClient handles all threading
	}

	@Override
	public void shutdown() {
		lsClient.disconnect();
		subscriptions.clear();	
	}

}
