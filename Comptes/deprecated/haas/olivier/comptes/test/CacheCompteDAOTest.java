package haas.olivier.comptes.test;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.util.HashSet;
import java.util.TreeSet;

import haas.olivier.comptes.Compte;
import haas.olivier.comptes.CompteBancaire;
import haas.olivier.comptes.CompteBudget;
import haas.olivier.comptes.Ecriture;
import haas.olivier.comptes.MessagesFactory;
import haas.olivier.comptes.TypeCompte;
import haas.olivier.comptes.dao.DAOFactory;
import haas.olivier.comptes.dao.EcritureDAO;
import haas.olivier.comptes.dao.cache.CacheCompteDAO;
import haas.olivier.comptes.dao.cache.CacheableCompteDAO;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class CacheCompteDAOTest {

	private static Compte c1, c2, c2b, c3;
	private HashSet<Compte> comptes = new HashSet<Compte>();
	private CacheCompteDAO dao;
	private CacheableCompteDAO subdao;
	private HashSet<Compte> comptes2 = new HashSet<Compte>();
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		// Des comptes
		c1 = new CompteBancaire(1, "un", 0L, TypeCompte.COMPTE_CARTE);
		c2 = new CompteBudget(2, "deux", TypeCompte.DEPENSES_EN_EPARGNE);
		c2b = new CompteBancaire(2, "deuxbis", 2L, TypeCompte.COMPTE_EPARGNE);
		c3 = new CompteBancaire(3, "trois", 1L, TypeCompte.EMPRUNT);
		
		// Créer un DAOFactory fictif pour les Ecritures
		EcritureDAO eDAO = mock(EcritureDAO.class);
		when(eDAO.getAll()).thenReturn(new TreeSet<Ecriture>());// du vide
		DAOFactory factory = mock(DAOFactory.class);
		when(factory.getEcritureDAO()).thenReturn(eDAO);
		DAOFactory.setFactory(factory);
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
		// Neutraliser les messages intempestifs
		MessagesFactory.setFactory(mock(MessagesFactory.class));
		
		// La collection des Comptes
		comptes.add(c1);
		comptes.add(c2);
		
		// Comportement du Mock
		subdao = mock(CacheableCompteDAO.class);
		when(subdao.getAll()).thenReturn(comptes);
		
		// Objet testé
		dao = new CacheCompteDAO(subdao);
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testGetAll() throws IOException {
		assertEquals(comptes, dao.getAll());
	}

	@Test
	public void testGet() throws IOException {
		assertSame(c1, dao.get(1));
		assertSame(c2, dao.get(2));
		try {
			dao.get(3);
			fail("Doit lever une exception");
		} catch (IOException e) {
		}
	}// testGet

	@Test
	public void testAdd() throws IOException {
		
		// Méthode testée
		dao.add(c3);
		
		// Vérifier qu'on a accès à tous les bons comptes
		assertSame(c1, dao.get(1));
		assertSame(c2, dao.get(2));
		assertSame(c3, dao.get(3));
	}// testAdd

	@Test
	public void testUpdate() throws IOException {
		// Méthode testée
		dao.update(c2b);
		
		// Vérifier qu'on obtient le nouveau compte
		assertSame(c2b, dao.get(2));
	}// testUpdate

	@Test
	public void testRemove() throws IOException, Exception {
		// Méthode testée
		dao.remove(1);
		
		// Vérifier que le compte n'est plus accessible
		try {
			dao.get(1);
			fail("Doit lever une exception");
		} catch (IOException e) {
		}
	}// testRemove

	@Test
	public void testSaveNormal() throws IOException {
		dao.save();
		verify(subdao).save(eq(comptes));	// Pas de changement de données
	}
	
	@Test
	public void testSaveAdd() throws IOException {
		dao.add(c3);
		dao.save();
		
		// Nouvelle collection
		comptes2.addAll(comptes);
		comptes2.add(c3);
		
		// Vérifier que le résultat concorde
		verify(subdao).save(eq(comptes2));
	}// testSaveAdd
	
	@Test
	public void testSaveUpdate() throws IOException {
		dao.update(c2b);
		dao.save();
		
		// Nouvelle collection
		comptes2.add(c1);
		comptes2.add(c2b);
		
		verify(subdao).save(eq(comptes2));
	}// testSaveUpdate
	
	@Test
	public void testSaveRemove() throws Exception {
		dao.remove(1);
		dao.save();
		
		// Nouvelle collection
		comptes2.add(c2);
		
		verify(subdao).save(eq(comptes2));
	}// testSaveRemove
	
	@Test
	public void testSaveAllInOne() throws Exception {
		dao.remove(1);
		dao.update(c2b);
		dao.add(c3);
		dao.save();
		
		// Nouvelle collection
		comptes2.add(c2b);
		comptes2.add(c3);
		
		verify(subdao).save(eq(comptes2));
	}// testSaveAllInOne
}
