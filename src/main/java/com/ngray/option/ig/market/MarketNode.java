package com.ngray.option.ig.market;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


import com.google.gson.Gson;

import com.ngray.option.ig.rest.RestAPIGet;
import com.ngray.option.ig.rest.RestAPIResponse;
import com.ngray.option.Log;
import com.ngray.option.ig.Session;
import com.ngray.option.ig.SessionException;


/**
 * Class represents a grouping of Markets and/or sub-instances of itself.
 * Instances naturally form a Tree structure
 * @author nigelgray
 *
 */
public final class MarketNode {
	
	/**
	 * Unique Id for the node
	 */
	private String id;
	
	/**
	 * Descriptive name for the node
	 */
	private String name;
	
	/**
	 * List of sub-nodes
	 */
	private List<MarketNode> nodes;
	
	/**
	 * List of markets owned by this node
	 */
	private List<Market> markets;
		
	/**
	 * Empty constructor required by Gson
	 * Id and name are by default initialized to those for the root node
	 */
	public MarketNode() {
		id = "";
		name = "Markets";
		nodes = new ArrayList<MarketNode>();
		markets = new ArrayList<Market>();	
	}
	
	/**
	 * Construct a MarketNode with the given id and name
	 * with empty markets and sub-nodes lists
	 * @param id
	 * @param name
	 */
	public MarketNode(String id, String name) {
		this.id = id;
		this.name = name;
		nodes = new ArrayList<MarketNode>();
		markets = new ArrayList<Market>();	
	}
	
	/**
	 * Get the root node of the MarketNode tree ie. call /marketnavigation
	 * Will throw a NullPointer exception if session is null, or a SessionException
	 * if the request fails (eg if the session has expired)
	 * @param session
	 * @return
	 * @throws SessionException, NullPointerException
	 */
	public static MarketNode getRootNode(Session session) throws SessionException {			
		RestAPIGet request = new RestAPIGet("/marketnavigation");
		RestAPIResponse response = request.execute(session);
		return fromJson(response.getResponseBodyAsJson());			
	}
	
	/**
	 * Return a MarketNode object constructed from the supplied Json string
	 * @param json
	 * @return
	 */
	public static MarketNode fromJson(String json) {
		Gson gson = new Gson();
		return gson.fromJson(json, MarketNode.class);
	}
	
	/**
	 * Return a string representing this object as Json
	 * @return
	 */
	public String asJson() {
		Gson gson = new Gson();
		return gson.toJson(this);
	}
	
	/**
	 * Get the Id of the node
	 * @return
	 */
	public String getId() {
		return id;
	}
	
	/**
	 * Get the name of the node
	 * @return
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Return a list of the nodes owned by this node
	 * Returns an empty list if there are no nodes or if called before getSubNodesAndMarkets()
	 * @return
	 */
	public List<MarketNode> getSubNodes() {		
		return nodes == null ? new ArrayList<>() : Collections.unmodifiableList(nodes);
	}
	
	/**
	 * Return a list of the markets owned by this node
	 * Returns an empty list if there are no markets or if called before getSubNodesAndMarkets()
	 * @return
	 */
	public List<Market> getMarkets() {
		return markets == null ? new ArrayList<>() :Collections.unmodifiableList(markets);
	}
	
	/**
	 * This method calls the /marketnavigation/{id} request and fills out the 
	 * nodes and markets lists with the returned objects.
	 * This object may have non-empty markets and nodes lists after this method
	 * has executed
	 * Will throw a NullPointerException if the session is null
	 * @param session
	 * @throws NullPointerException
	 */
	public void getSubNodesAndMarkets(Session session) {
		try {
			RestAPIGet request = new RestAPIGet("/marketnavigation/" + getId());
			RestAPIResponse response = request.execute(session);
			MarketNode newNode = fromJson(response.getResponseBodyAsJson());
			this.nodes = newNode.getSubNodes();
			this.markets = newNode.getMarkets();
		} catch (SessionException e) {
			Log.getLogger().fatal(e.getMessage(), e);
		}			
	}
	
	@Override
	/**
	 * toString() returns the name of the node - this is what will be displayed in the Nodes tree
	 */
	public String toString() {
		return name;
	}
}
