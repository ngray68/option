package com.ngray.option.ig.rest;

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.HttpClientBuilder;

import com.ngray.option.Log;
import com.ngray.option.ig.Session;
import com.ngray.option.ig.SessionConstants;
import com.ngray.option.ig.SessionException;

public class RestAPIPost implements RestAPIAction {

	private final String request;
	private final String messageBody;
	
	public RestAPIPost(String request, String messageBody) {
		this.request = request;
		this.messageBody = messageBody;
	}

	@Override
	public RestAPIResponse execute(Session session) throws SessionException {
		try {
			Log.getLogger().info("Executing HttpPost request " + getUrl(session.getIsLive()));
			final HttpPost post = new HttpPost(getUrl(session.getIsLive()));		
			session.getSessionHeaders().forEach(post::addHeader);				
			final HttpClient client = HttpClientBuilder.create().build();
			post.setEntity(new ByteArrayEntity(messageBody.getBytes("UTF-8")));
			final HttpResponse response = client.execute(post);
			StatusLine statusLine = response.getStatusLine();
			switch (statusLine.getStatusCode()) {
				case HttpStatus.SC_OK:				// 200	
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
