package haas.olivier.comptes.dao.cache.hibernate;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.IdentityHashMap;
import java.util.Iterator;
import haas.olivier.comptes.Compte;
import haas.olivier.comptes.Ecriture;
import haas.olivier.comptes.EcritureMissingArgumentException;
import haas.olivier.comptes.InconsistentArgumentsException;
import haas.olivier.comptes.TypeCompte;
import haas.olivier.comptes.dao.CompteDAO;
import haas.olivier.comptes.dao.EcritureDAO;
import haas.olivier.comptes.dao.cache.CacheDAOFactory;
import haas.olivier.comptes.dao.cache.CacheableDAOFactory;
import haas.olivier.comptes.dao.cache.hibernate.HibernateCacheableDAO;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class HibernateCacheableDAOTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	/**
	 * Objet testé.
	 */
	private CacheableDAOFactory factory;
	
	@Mock
	private CacheDAOFactory cacheDAO;
	
	@Mock
	private CompteDAO cacheCompteDAO;
	
	@Mock
	private EcritureDAO cacheEcritureDAO;
	
	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		
		when(cacheCompteDAO.getAll()).thenReturn(Collections.emptyList());
		when(cacheEcritureDAO.getAll()).thenReturn(Collections.emptyList());
		
		when(cacheDAO.getCompteDAO()).thenReturn(cacheCompteDAO);
		when(cacheDAO.getEcritureDAO()).thenReturn(cacheEcritureDAO);
		
		factory = new HibernateCacheableDAO(
				"jdbc:hsqldb:mem:test", "org.hsqldb.jdbc.JDBCDriver");
	}

	@After
	public void tearDown() throws Exception {
		factory.close();
	}
	
	@Test
	public void testGetComptes() throws IOException {
		assertFalse(factory.getComptes().hasNext());
	}
	
	@Test
	public void testGetEcritures() throws IOException {
		assertFalse(factory.getEcritures().hasNext());
	}
	
	@Test
	public void testSaveComptes() throws IOException {
		Compte compte1 = new Compte(0, TypeCompte.COMPTE_CARTE);
		when(cacheCompteDAO.getAll()).thenReturn(Collections.singleton(compte1));
		
		// Méthode testée (1ère passe)
		factory.save(cacheDAO);
		
		Iterator<Compte> iterator = factory.getComptes();
		assertTrue(iterator.hasNext());
		assertSame(compte1, iterator.next());
		assertFalse(iterator.hasNext());
		
		// 2ème phase
		
		compte1.setNom("mon compte 1");
		
		Compte compte2 = new Compte(0, TypeCompte.RECETTES);
		when(cacheCompteDAO.getAll()).thenReturn(
				Arrays.asList(new Compte[] {compte1, compte2}));
		
		// Méthode testée (2ème passe avec 1 nouveau compte + 1 modifié)
		factory.save(cacheDAO);
		
		checkCollection(factory.getComptes(),
				new Compte[] {compte1, compte2});
	}
	
	@Test
	public void testSaveEcritures() throws EcritureMissingArgumentException, InconsistentArgumentsException, IOException {
		Compte compte1 = new Compte(1, TypeCompte.COMPTE_COURANT);
		Compte compte2 = new Compte(2, TypeCompte.DEPENSES_EN_EPARGNE);
		when(cacheCompteDAO.getAll()).thenReturn(
				Arrays.asList(new Compte[] {compte1, compte2}));
		
		Ecriture ecriture1 = new Ecriture(null, new Date(156L), new Date(192L),
				compte1, compte2, BigDecimal.ONE, "libellé 1", "tiers 1", 457);
		when(cacheEcritureDAO.getAll()).thenReturn(
				Collections.singleton(ecriture1));
		
		// Méthode testée (1ère phase)
		factory.save(cacheDAO);
		
		Iterator<Ecriture> iterator = factory.getEcritures();
		assertTrue(iterator.hasNext());
		assertSame(ecriture1, iterator.next());
		assertFalse(iterator.hasNext());
		
		// 2ème phase
		
		ecriture1.setLibelle("Libellé modifié");
		ecriture1.setDate(new Date(4632186L));
		ecriture1.setCredit(new Compte(3, TypeCompte.ENFANTS));
		
		Ecriture ecriture2 = new Ecriture(null, new Date(993156L),
				new Date(700000192L), compte2, compte1, BigDecimal.TEN,
				"libellé 2", "tiers 2", null);
		
		when(cacheEcritureDAO.getAll()).thenReturn(
				Arrays.asList(new Ecriture[] {ecriture1, ecriture2}));

		// Méthode testée (2ème passe avec 1 nouvelle écriture + 1 modifiée)
		factory.save(cacheDAO);
		
		checkCollection(factory.getEcritures(),
				new Ecriture[] {ecriture1, ecriture2});
	}
	
	/**
	 * Vérifie que l'itérateur spécifié contient exactement les éléments
	 * attendus. L'ordre n'a pas d'importance.
	 * 
	 * @param <T>		Le type des éléments recherchés
	 * @param iterator	L'itérateur à vérifier.
	 * @param expected	Les éléments attendus.
	 */
	private <T> void checkCollection(Iterator<T> iterator, T[] expected) {
		IdentityHashMap<T, Void> result = new IdentityHashMap<>();
		while (iterator.hasNext()) {
			result.put(iterator.next(), null);
		}
		
		for (T element : expected) {
			assertTrue(result.containsKey(element));
		}
		
		assertEquals(expected.length, result.size());
	}
}
