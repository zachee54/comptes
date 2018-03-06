package haas.olivier.comptes.test;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import haas.olivier.util.Month;
import haas.olivier.comptes.dao.cache.CacheSuiviDAO;
import haas.olivier.comptes.dao.cache.CacheableSuiviDAO;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class CacheSuiviDAOTest {

	private Map<Month,Map<Integer,BigDecimal>> suivi;
	private Month month1 = new Month(), month2 = month1.getNext();
	private BigDecimal a = new BigDecimal("5");
	private CacheableSuiviDAO subdao = mock(CacheableSuiviDAO.class);
	private CacheSuiviDAO dao;
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
		// Données pêle-mêle
		suivi = new HashMap<Month,Map<Integer,BigDecimal>>();
		
		suivi.put(month1, new HashMap<Integer,BigDecimal>());
		suivi.put(month2, new HashMap<Integer,BigDecimal>());
		
		suivi.get(month1).put(1, new BigDecimal("101.01"));
		suivi.get(month2).put(2, new BigDecimal("202.02"));
		suivi.get(month2).put(1, new BigDecimal("303.03"));

		// Comportement du Mock
		when(subdao.getAll()).thenReturn(suivi);
		
		// Objet testé
		dao = new CacheSuiviDAO(subdao);
	}// setUp

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testGetAll() throws IOException {
		assertEquals(suivi, dao.getAll());
	}

	@Test
	public void testRemoveSuiviCompte() throws IOException {
		dao.removeSuiviCompte(1);
		
		// Vérifier que le compte 1 a disparu de month1
		assertNull(dao.get(1, month1));
		
		// Vérifier que le compte 1 a disparu de month2
		assertNull(dao.get(1, month2));
		
		// Vérifier que le compte 2 n'a pas disparu
		assertEquals(0, new BigDecimal("202.02").compareTo(dao.get(2, month2)));
	}// testRemoveSuiviCompte

	@Test
	public void testGet() {
		assertEquals(0, suivi.get(month1).get(1).compareTo(dao.get(1, month1)));
		assertEquals(0, suivi.get(month2).get(1).compareTo(dao.get(1, month2)));
		assertEquals(0, suivi.get(month2).get(2).compareTo(dao.get(2, month2)));
		assertNull(dao.get(2, month1));
	}// testGet

	@Test
	public void testSetNew() throws IOException {
		dao.set(2, month1, a);
		assertEquals(0, a.compareTo(dao.get(2, month1)));
	}// testSetNew
	
	@Test
	public void testSetReplace() throws IOException {
		dao.set(1, month2, a);
		assertEquals(0, a.compareTo(dao.get(1, month2)));
	}// testSetReplace

	@Test
	public void testRemoveFrom() throws IOException {
		// Ajouter une valeur dans un troisième mois
		Month month3 = month2.getNext();
		dao.set(2, month3, a);
		
		// Méthode testée
		dao.removeFrom(month2);
		
		// Vérifier qu'il n'y a plus rien dans month2 et month3
		assertNull(dao.get(2, month3));
		assertNull(dao.get(2, month2));
		assertNull(dao.get(1, month2));
		
		// Vérifier qu'on a toujours month1
		assertNotNull(dao.get(1, month1));
	}// testRemoveFrom

	@Test
	public void testSave() throws IOException {
		dao.save();
		verify(subdao).save(eq(suivi));
	}// testSave

}
