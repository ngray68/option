package com.ngray.option.ig.rest;

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.impl.client.HttpClientBuilder;

import com.ngray.option.Log;
import com.ngray.option.ig.Session;
import com.ngray.option.ig.SessionConstants;
import com.ngray.option.ig.SessionException;

public class RestAPIDelete implements RestAPIAction {

	private final String request;
	
	public RestAPIDelete(String request) {
		this.request = request;
	}

	@Override
	public RestAPIResponse execute(Session session) throws SessionException {
		try {
			Log.getLogger().info("Executing HttpDelete request " + getUrl(session.getIsLive()));
			final HttpDelete delete = new HttpDelete(getUrl(session.getIsLive()));		
			session.getSessionHeaders().forEach(delete::addHeader);				
			final HttpClient client = HttpClientBuilder.create().build();
			final HttpResponse response = client.execute(delete);
			
			StatusLine statusLine = response.getStatusLine();
			switch (statusLine.getStatusCode()) {
				case HttpStatus.SC_OK:				// 200	
					Log.getLogger().info("Request " + getUrl(session.getIsLive()) + " successful");
					return new RestAPIResponse(response);
				case HttpStatus.SC_NO_CONTENT:				
					Log.getLogger().info("Request " + getUrl(session.getIsLive()) + " successful");
					return new RestAPIResponse(response);
				default:
					throw new SessionException("Request " + getUrl(session.getIsLive()) + " failed: " + statusLine.toString());
			}
		} catch (IOException e) {
			throw new SessionException(e);
		}
	}
	
	private String getUrl(boolean isLive) {
		return SessionConstants.getProtocol() + SessionConstants.getGateway(isLive) + request;
	}

}
