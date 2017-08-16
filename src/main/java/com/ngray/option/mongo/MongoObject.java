package com.ngray.option.mongo;

public interface MongoObject {
	
	/**
	 * Override this method to return a unique id for the object. For example,
	 * this could be a combination of attributes that is unique for each object
	 * @return
	 */
	public String getUniqueId();
	
	/**
	 * Override this method in implementations to return the MongoCollection name
	 * for the object type
	 * @return
	 */
	public default String getCollectionName() {
		return null;
	}
	
	public static String getUniqueIdKey() {
		return "UniqueId";
	}

}
