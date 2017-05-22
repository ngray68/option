package com.ngray.option.ig.stream;

import com.lightstreamer.client.ClientListener;
import com.lightstreamer.client.LightstreamerClient;
import com.ngray.option.Log;

public class StreamManager {

	private final LightstreamerClient client;
	
	private final PositionUpdateStream positionUpdateStream;
	private final LivePriceStream livePriceStream;
	
	public StreamManager(String lightStreamerEndpoint, String activeAccountId, String clientToken, String accountToken) {
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
		
		positionUpdateStream = new PositionUpdateStream(client);
		livePriceStream = new LivePriceStream(client);
	}

	public PositionUpdateStream getPositionUpdateStream() {
		return positionUpdateStream;
	}

	public LivePriceStream getLivePriceStream() {
		return livePriceStream;
	}

}
