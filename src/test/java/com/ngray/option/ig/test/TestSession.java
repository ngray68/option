package com.ngray.option.ig.test;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

import com.ngray.option.ig.Session;
import com.ngray.option.ig.SessionException;
import com.ngray.option.ig.SessionLoginDetails;
import com.ngray.option.ig.position.IGPositionList;

public class TestSession {

	private Session session = null;
	private String loginDetailsFile = "/Users/nigelgray/Documents/details.txt";
	
	private String readFile(String fileName) throws IOException {
		String result = "";
		try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
			String nextLine = null;
			while ((nextLine = reader.readLine()) != null) {
				result += nextLine;
			}
		}
		return result;
	}
	
	@Before
	public void setUp() throws SessionException, IOException {
		boolean isLive = false;
		String json = readFile(loginDetailsFile);
		SessionLoginDetails loginDetails = SessionLoginDetails.fromJson(json);
		session = Session.login(loginDetails, isLive);		
	}
	@Test
	public void testLogin() throws SessionException {
		assertTrue(session != null);
	}

	@Test
	public void testLogout() throws SessionException {
		assertTrue(session != null);
		session.logout();
	}
	
	@Test
	public void testGetPosition() throws SessionException {
		assertTrue(session != null);
		IGPositionList posList = session.getPositions();
		assertTrue(posList != null);
	}
}
