package haas.olivier.comptes.test;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TreeSet;

import haas.olivier.comptes.Compte;
import haas.olivier.comptes.CompteBancaire;
import haas.olivier.comptes.CompteBudget;
import haas.olivier.comptes.Ecriture;
import haas.olivier.util.Month;
import haas.olivier.comptes.TypeCompte;
import haas.olivier.comptes.dao.CompteDAO;
import haas.olivier.comptes.dao.DAOFactory;
import haas.olivier.comptes.dao.cache.CacheEcritureDAO;
import haas.olivier.comptes.dao.cache.CacheableEcritureDAO;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class CacheEcritureDAOTest {

	private static Ecriture e1, e2, e3, e2b;
	private static Compte c1, c2, c1b;
	private static Date date1, date2, date3;
	private TreeSet<Ecriture> ecritures = new TreeSet<Ecriture>();
	private CacheableEcritureDAO subdao = mock(CacheableEcritureDAO.class);
	private CacheEcritureDAO dao;
	private TreeSet<Ecriture> ecritures2 = new TreeSet<Ecriture>();
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		// Des dates
		SimpleDateFormat df = new SimpleDateFormat("dd/M/yy");
		date1 = df.parse("01/01/2012");
		date2 = df.parse("01/02/2012");
		date3 = df.parse("01/03/2012");
		BigDecimal m = new BigDecimal("1");
		
		// Des comptes
		c1 = new CompteBancaire(1, "un", 0L, TypeCompte.COMPTE_CARTE);
		c1b = new CompteBudget(1, "un bis", TypeCompte.RECETTES);
		c2 = new CompteBudget(2, "deux", TypeCompte.DEPENSES);
		
		// Des écritures
		e1 = new Ecriture(1, date1, date3, c1, c2, m, "un", null, null);
		e2 = new Ecriture(2, date2, date2, c2, c1, m, "deux", null, null);
		e2b = new Ecriture(2, date1, null, c1, c2, m, "deux bis", null, 1);
		e3 = new Ecriture(3, date2, null, c1, c2, m, "trois", null, null);
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
		// Collection de toutes les écritures
		ecritures.add(e1);
		ecritures.add(e2);
		
		// Comportement du Mock
		when(subdao.getAll()).thenReturn(ecritures);
//		when(subdao.getAllPointage()).thenReturn(ecritures);
		
		// Objet testé
		dao = new CacheEcritureDAO(subdao);
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testGetAll() throws IOException {
		assertEquals(ecritures, dao.getAll());
	}

	@Test
	public void testAdd() throws IOException {
		dao.add(e3);
		assertSame(e3, dao.get(3));
	}

	@Test
	public void testUpdate() throws IOException {
		dao.update(e2b);
		assertSame(e2b, dao.get(2));
	}

	@Test
	public void testRemove() throws IOException {
		dao.remove(1);
		try {
			dao.get(1);
			fail("Doit lever une exception");
		} catch (IOException e) {
		}
	}// testRemove

	@Test
	public void testGet() throws IOException {
		assertSame(e1, dao.get(1));
		assertSame(e2, dao.get(2));
		try {
			dao.get(3);
			fail("Doit lever une exception");
		} catch (IOException e) {
		}
	}// testGet

	@Test
	public void testGetAllSince() throws IOException {
		ecritures2.add(e2);
		assertEquals(ecritures2, dao.getAllSince(new Month(date2)));
	}

	@Test
	public void testGetPointagesSince() throws IOException {
		ecritures2.add(e1);
		assertEquals(ecritures2, dao.getPointagesSince(new Month(date3)));
	}

	@Test
	public void testRefreshCompte() throws Exception {
		// Comportement du Mock CompteDAO, rendu accessible par DAOFactory
		CompteDAO cDAO = mock(CompteDAO.class);
		when(cDAO.get(1)).thenReturn(c1b);	// Le nouveau compte ("rafraîchi")
		when(cDAO.get(2)).thenReturn(c2);
		DAOFactory factory = mock(DAOFactory.class);
		when(factory.getCompteDAO()).thenReturn(cDAO);
		DAOFactory.setFactory(factory);
		
		// Méthode testée
		dao.refreshCompte(c1b);
		assertSame(c1b, dao.get(1).debit);
		assertSame(c1b, dao.get(2).credit);
		
		// Vérifier aussi que l'autre compte n'a pas été changé
		assertSame(c2, dao.get(1).credit);
	}// testRefreshCompte

	@Test
	public void testSave() throws IOException {
		dao.save();
		verify(subdao).save(eq(ecritures));
	}// testSave
	
	@Test
	public void testSaveAdd() throws IOException {
		dao.add(e3);
		dao.save();
		
		// Nouvelle collection
		ecritures2.addAll(ecritures);
		ecritures2.add(e3);
		
		verify(subdao).save(eq(ecritures2));
	}// testSaveAdd
	
	@Test 
	public void testSaveUpdate() throws IOException {
		dao.update(e2b);
		dao.save();
		
		// Nouvelle collection
		ecritures2.add(e1);
		ecritures2.add(e2b);
		
		verify(subdao).save(eq(ecritures2));
	}// testSaveUpdate
	
	@Test
	public void testSaveRemove() throws IOException {
		dao.remove(1);
		dao.save();
		
		// Nouvelle collection
		ecritures2.add(e2);
		
		verify(subdao).save(eq(ecritures2));
	}// testSaveRemove
	
	@Test
	public void testSaveAllInOne() throws IOException {
		dao.add(e3);
		dao.update(e2b);
		dao.remove(1);
		dao.save();
		
		// Nouvelle collection
		ecritures2.add(e2b);
		ecritures2.add(e3);
		
		verify(subdao).save(eq(ecritures2));
	}// testSaveAllInOne
}
