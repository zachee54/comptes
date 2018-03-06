package haas.olivier.comptes.test;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import haas.olivier.comptes.Compte;
import haas.olivier.comptes.CompteBancaire;
import haas.olivier.util.Month;
import haas.olivier.comptes.Permanent;
import haas.olivier.comptes.dao.AbstractPermanentDAO;
import haas.olivier.comptes.dao.util.CachePermanent;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class CachePermanentTest {

	private static Permanent perm1, perm2, perm3;
	private static HashSet<Permanent> permanents = new HashSet<Permanent>();
	private AbstractPermanentDAO dao;
	private CachePermanent cache;
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		
		// Données pêle-mêle
		Compte compte = mock(Compte.class);
		HashMap<Month,Integer> jours = new HashMap<Month,Integer>();
		perm1 = new Permanent(1, "un", compte, compte, jours, new HashMap<Month,BigDecimal>());
		perm2 = new Permanent(2, "deux", mock(CompteBancaire.class), compte, jours);
		perm3 = new Permanent(3, "trois", compte, compte, jours, perm1, new BigDecimal("3.01"));
		
		permanents.add(perm1);
		permanents.add(perm2);
		permanents.add(perm3);
	}// setUpBeforeClass

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
		// Initialiser le Mock
		dao = mock(AbstractPermanentDAO.class);
		when(dao.getAll()).thenReturn(permanents);
		
		// Objet testé
		cache = new CachePermanent(dao);
	}// setUp

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testCachePermanent() {
		
		// Pas d'appel au DAO à l'instantiation
		verifyZeroInteractions(dao);
	}

	@Test
	public void testGetCloneCache() throws IOException {
		Set<Permanent> clone = cache.getCloneCache();
		assertEquals(permanents, clone);
		assertNotSame(permanents, clone);
	}

	@Test
	public void testGetCache() throws IOException {
		assertSame(permanents, cache.getCache());
	}

	@Test
	public void testInitMaxId() throws IOException {
		assertEquals(4, cache.getNextId());
	}

}
