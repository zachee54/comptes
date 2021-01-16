/*
 * Copyright (c) 2018 DGFiP - Tous droits réservés
 * 
 */
package haas.olivier.util;

import static org.junit.Assert.*;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class PoolTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}
	
	/**
	 * Objet testé.
	 */
	private final Pool pool = new Pool();
	
	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@SuppressWarnings("deprecation")
	@Test
	public void testGet() {
		Object a = new Integer(12);
		Object b = new Integer(12);
		Object c = Integer.valueOf(7);
		assertNotSame(a, b);
		
		assertSame(a, pool.get(a));		// Première insertion de a
		assertSame(c, pool.get(c));		// Première insertion de c
		assertSame(a, pool.get(b));		// Insertion de b : renvoyer a égal à b
	}

}
