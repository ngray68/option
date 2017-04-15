package com.ngray.option;

import org.apache.logging.log4j.*;
/*
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
*/
/**
 * Simple utility wrapper around the standard java logger
 * @author nigelgray
 *
 */
public final class Log {

	private static final Logger logger = LogManager.getRootLogger();
	
	public static Logger getLogger() {
		return logger;	
	}
	
	
}
