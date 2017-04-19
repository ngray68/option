package com.ngray.option.ig.test;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import com.ngray.option.ig.Session;
import com.ngray.option.ig.SessionException;
import com.ngray.option.ig.position.IGPositionList;

public class TestSession {

	private Session session = null;
	
	@Before
	public void setUp() throws SessionException {
		boolean isLive = false;
		boolean encrypted = false;
		String username = "dummyuser";
		String password = "dummypassword";
		session = Session.login(username, password, encrypted, isLive);		
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
