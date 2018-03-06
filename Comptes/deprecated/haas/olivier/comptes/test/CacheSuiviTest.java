package haas.olivier.comptes.test;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import haas.olivier.util.Month;
import haas.olivier.comptes.dao.AbstractSuiviDAO;
import haas.olivier.comptes.dao.util.CacheSuivi;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class CacheSuiviTest {

	private static HashMap<Month,Map<Integer,BigDecimal>> suivi;
	private AbstractSuiviDAO dao;
	private CacheSuivi cache;
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		
		// Données pêle-mêle
		suivi = new HashMap<Month,Map<Integer,BigDecimal>>();
		
		Month month1 = new Month(), month2 = month1.getNext();
		suivi.put(month1, new HashMap<Integer,BigDecimal>());
		suivi.put(month2, new HashMap<Integer,BigDecimal>());
		
		suivi.get(month1).put(1, new BigDecimal("101.01"));
		suivi.get(month2).put(2, new BigDecimal("202.02"));
		suivi.get(month2).put(1, new BigDecimal("303.03"));
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
		
		// Le Mock
		dao = mock(AbstractSuiviDAO.class);
		when(dao.getAll()).thenReturn(suivi);
		
		// Objet testé
		cache = new CacheSuivi(dao);
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testCacheSuivi() {
		
		// Pas d'interaction à l'instantiation
		verifyZeroInteractions(dao);
	}

	@Test
	public void testGetCloneCache() throws IOException {
		Map<Month,Map<Integer,BigDecimal>> clone = cache.getCloneCache();
		
		// Les mêmes données
		assertEquals(suivi, clone);
		
		// Pas la même instance
		assertNotSame(suivi, clone);
	}

	@Test
	public void testGetCache() throws IOException {
		assertSame(suivi, cache.getCache());
	}
}
