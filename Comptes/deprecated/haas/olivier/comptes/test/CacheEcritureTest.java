package haas.olivier.comptes.test;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TreeSet;

import haas.olivier.comptes.Compte;
import haas.olivier.comptes.Ecriture;
import haas.olivier.comptes.dao.AbstractEcritureDAO;
import haas.olivier.comptes.dao.util.CacheEcriture;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mock;

public class CacheEcritureTest {

	private static Ecriture e1, e2;
	private static SimpleDateFormat df = new SimpleDateFormat("dd/MM/yy");
	private static Date date1, date2;
	private static Compte c1 = mock(Compte.class), c2 = mock(Compte.class);
	private static BigDecimal m = new BigDecimal("10");
	private static TreeSet<Ecriture> ecritures = new TreeSet<Ecriture>();
	@Mock private AbstractEcritureDAO dao;
	private CacheEcriture cache;
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		// Données pêle-mêle
		date1 = df.parse("01/01/2014");
		date2 = df.parse("01/02/2014");
		e1 = new Ecriture(1, date1, null, c1, c2, m, null, null, null);
		e2 = new Ecriture(2, date2, null, c2, c1, m, null, null, null);
		ecritures.add(e1);
		ecritures.add(e2);
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
		
		// Le Mock
		dao = mock(AbstractEcritureDAO.class);
		when(dao.getAll()).thenReturn(ecritures);
		
		// Objet testé
		cache = new CacheEcriture(dao);
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testInitMaxId() throws IOException {
		assertEquals(3, cache.getNextId());
	}

	@Test
	public void testCacheEcritureAbstractEcritureDAO() throws IOException {
		// Le bon cache sans modif (donc trié par ordre naturel)
		assertSame(ecritures, cache.getCache());
	}

	@Test
	public void testCacheEcritureCacheEcriture() throws IOException {
		// Un cache "pointages"
		CacheEcriture cachePointages = new CacheEcriture(cache);
		// On récupère ses données
		TreeSet<Ecriture> pointages = cachePointages.getCache();
		
		// Vérifier que le contenu est le même
		assertEquals(pointages, ecritures);
		
		// Vérifier que le deuxième est bien trié par pointages
		assertTrue(pointages.comparator() instanceof Ecriture.SortPointages);
	}

	@Test
	public void testGetCloneCache() throws IOException {
		TreeSet<Ecriture> clone = cache.getCloneCache();
		
		// Mêmes données
		assertEquals(clone, ecritures);
		
		// Pas la même instance
		assertNotSame(clone, ecritures);
	}

	@Test
	public void testGetCache() throws IOException {
		assertSame(ecritures, cache.getCache());
	}
}
