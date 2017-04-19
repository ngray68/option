package com.ngray.option.ig.rest;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.http.Header;
import org.apache.http.HttpResponse;

import com.ngray.option.ig.SessionException;

/**
 * Class encapsulates a response to a RestAPIAction
 * Provides wrapper methods to access the response headers
 * and the Json response body from the raw HttpResponse
 * @author nigelgray
 *
 */
public final class RestAPIResponse {
	
	/**
	 * The raw Http response object
	 */
	private final HttpResponse response;
	
	/**
	 * Construct from the raw HttpResponse parameter
	 * @param response
	 */
	public RestAPIResponse(HttpResponse response) {
		this.response = response;
	}
	
	/**
	 * Get the raw HttpResponse
	 * @return
	 */
	public HttpResponse getRawHttpResponse() {
		return response;
	}
	
	/**
	 * Get a list of the response headers
	 * @return
	 */
	public List<Header> getHeaders() {
		assert (response != null);
		return Collections.unmodifiableList(Arrays.asList(response.getAllHeaders()));
	}
	
	/**
	 * Get the response body as a Json string
	 * @return
	 * @throws SessionException
	 */
	public String getResponseBodyAsJson() throws SessionException {
		assert (response != null);
		try {
			String json = "";
			InputStream stream = response.getEntity().getContent();
			
			try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream))) {
				String line = null;
				while ((line = reader.readLine()) != null) {
					json += line;
				}
			}
			return json;
		} catch (UnsupportedOperationException |IOException e) {
			throw new SessionException(e);
		}
	}
}
