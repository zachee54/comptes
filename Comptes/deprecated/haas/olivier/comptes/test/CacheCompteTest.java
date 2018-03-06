package haas.olivier.comptes.test;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import haas.olivier.comptes.Compte;
import haas.olivier.comptes.CompteBancaire;
import haas.olivier.comptes.CompteBudget;
import haas.olivier.comptes.TypeCompte;
import haas.olivier.comptes.dao.AbstractCompteDAO;
import haas.olivier.comptes.dao.util.CacheCompte;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class CacheCompteTest {

	private static Compte compte1, compte2;
	@Mock private AbstractCompteDAO dao;
	private Set<Compte> comptes = new HashSet<Compte>();
	private CacheCompte cache;
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		compte1 = new CompteBancaire(1, "un", 0L, TypeCompte.COMPTE_CARTE);
		compte2 = new CompteBudget(2, "deux", TypeCompte.DEPENSES);
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
		// Initialiser les Mocks
		MockitoAnnotations.initMocks(this);
		when(dao.getAll()).thenReturn(comptes);
		
		// Remplir la collection des comptes
		comptes.add(compte1);
		comptes.add(compte2);
		
		// Instancier le cache
		cache = new CacheCompte(dao);
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testCacheCompte() {
		
		// Pas d'appel au DAO à l'instantiation
		verifyZeroInteractions(dao);
	}

	@Test
	public void testGetCloneCache() throws IOException {
		Set<Compte> cloneCache = cache.getCloneCache();
		assertEquals(comptes, cloneCache);	// Les mêmes données
		assertNotSame(comptes, cloneCache);	// dans une autre instance de Set
	}

	@Test
	public void testGetCache() throws IOException {
		// Le cache sans clonage
		assertSame(comptes, cache.getCache());
	}
	
	@Test
	public void testInitMaxId() throws IOException {
		assertEquals(3, cache.getNextId());	// 1er identifiant non utilisé
	}
}
