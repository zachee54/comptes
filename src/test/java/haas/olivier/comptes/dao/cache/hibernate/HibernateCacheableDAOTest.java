package haas.olivier.comptes.dao.cache.hibernate;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.Map;

import haas.olivier.comptes.Compte;
import haas.olivier.comptes.Ecriture;
import haas.olivier.comptes.TypeCompte;
import haas.olivier.comptes.dao.CompteDAO;
import haas.olivier.comptes.dao.EcritureDAO;
import haas.olivier.comptes.dao.cache.CacheDAOFactory;
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
	private HibernateCacheableDAO factory;
	
	@Mock
	private CacheDAOFactory cacheDAO;
	
	@Mock
	private CompteDAO cacheCompteDAO;
	
	@Mock
	private EcritureDAO cacheEcritureDAO;
	
	// POJOs divers
	private Compte compte1, compte2;
	private Ecriture ecriture1, ecriture2;
	
	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		
		when(cacheCompteDAO.getAll()).thenReturn(Collections.emptyList());
		when(cacheEcritureDAO.getAll()).thenReturn(Collections.emptyList());
		
		when(cacheDAO.getCompteDAO()).thenReturn(cacheCompteDAO);
		when(cacheDAO.getEcritureDAO()).thenReturn(cacheEcritureDAO);
		
		factory = new HibernateCacheableDAO(
				"jdbc:hsqldb:mem:test", "org.hsqldb.jdbc.JDBCDriver");
		
		compte1 = new Compte(null, TypeCompte.COMPTE_CARTE);
		compte2 = new Compte(null, TypeCompte.RECETTES);
		compte2.setNom("compte 2");
		
		ecriture1 = new Ecriture(null, new Date(156L), new Date(192L),
				compte1, compte2, BigDecimal.ONE, "libellé 1", "tiers 1", 457);
		ecriture2 = new Ecriture(null, new Date(993156L),
				new Date(700000192L), compte2, compte1, BigDecimal.TEN,
				"libellé 2", "tiers 2", null);
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
	public void testReload() throws IOException {
		when(cacheCompteDAO.getAll()).thenReturn(
				Arrays.asList(new Compte[] {compte1, compte2}));
		when(cacheEcritureDAO.getAll()).thenReturn(
				Collections.singleton(ecriture1));
		factory.save(cacheDAO);
		
		compte1.setNumero(13L);
		compte2.setNom("nom modifié");
		ecriture1.setLibelle("libellé modifié");
		
		// Méthode testée
		factory.reload();
		
		Iterator<Compte> comptesIterator = factory.getComptes();
		Map<Integer, Compte> comptes = new HashMap<>();
		while (comptesIterator.hasNext()) {
			Compte compte = comptesIterator.next();
			comptes.put(compte.getId(), compte);
		}
		
		// Vérifier la réinitialisation du compte 1
		assertEquals(2, comptes.size());
		Integer id1 = compte1.getId();
		Compte compte1Reloaded = comptes.get(id1);
		assertNull(compte1Reloaded.getNumero());
		
		// Vérifier la réinitialisation du compte 2
		Integer id2 = compte2.getId();
		Compte compte2Reloaded = comptes.get(id2);
		assertEquals("compte 2", compte2Reloaded.getNom());
		
		// Vérifier la réinitialisation de l'écriture
		Iterator<Ecriture> ecrituresIterator = factory.getEcritures();
		assertTrue(ecrituresIterator.hasNext());
		assertEquals("libellé 1", ecrituresIterator.next().getLibelle());
		assertFalse(ecrituresIterator.hasNext());
	}
	
	@Test
	public void testSaveComptes() throws IOException {
		Compte[] comptes = {compte1, compte2};
		when(cacheCompteDAO.getAll()).thenReturn(Arrays.asList(comptes));
		
		// Méthode testée
		factory.save(cacheDAO);
		
		checkCollection(factory.getComptes(), comptes);
	}
	
	@Test
	public void testSaveAddCompte() throws IOException {
		when(cacheCompteDAO.getAll()).thenReturn(
				Collections.singleton(compte1));
		factory.save(cacheDAO);
		
		// Ajouter un deuxième compte
		Compte[] comptes = {compte1, compte2};
		when(cacheCompteDAO.getAll()).thenReturn(Arrays.asList(comptes));
		
		// Méthode testée
		factory.save(cacheDAO);
		
		checkCollection(factory.getComptes(), comptes);
	}
	
	@Test
	public void testSaveUpdateCompte() throws IOException {
		when(cacheCompteDAO.getAll()).thenReturn(Collections.singleton(compte1));
		factory.save(cacheDAO);
		
		compte1.setNom("nom modifié");
		
		// Méthode testée (mise à jour du compte 1)
		factory.save(cacheDAO);
		
		// Réinitialiser pour obliger la relecture depuis la BDD
		factory.reload();
		
		Iterator<Compte> comptesIterator = factory.getComptes();
		assertTrue(comptesIterator.hasNext());
		assertEquals("nom modifié", comptesIterator.next().getNom());
		assertFalse(comptesIterator.hasNext());
	}
	
	@Test
	public void testSaveDeleteCompte() throws IOException {
		when(cacheCompteDAO.getAll()).thenReturn(
				Arrays.asList(new Compte[] {compte1, compte2}));
		
		factory.save(cacheDAO);
		
		when(cacheCompteDAO.getAll()).thenReturn(
				Collections.singleton(compte2));
		
		// Méthode testée
		factory.save(cacheDAO);
		
		checkCollection(factory.getComptes(), new Compte[] {compte2});
	}
	
	@Test
	public void testSaveEcritures() throws IOException {
		Ecriture[] ecritures = {ecriture1, ecriture2};
		when(cacheEcritureDAO.getAll()).thenReturn(Arrays.asList(ecritures));
		
		// Méthode testée
		factory.save(cacheDAO);
		
		checkCollection(factory.getEcritures(), ecritures);
	}
	
	@Test
	public void testSaveAddEcriture() throws IOException {
		when(cacheEcritureDAO.getAll()).thenReturn(
				Collections.singleton(ecriture1));
		factory.save(cacheDAO);
		
		// Ajouter une deuxième écriture
		Ecriture[] ecritures = {ecriture1, ecriture2};
		when(cacheEcritureDAO.getAll()).thenReturn(Arrays.asList(ecritures));
		
		// Méthode testée
		factory.save(cacheDAO);
		
		checkCollection(factory.getEcritures(), ecritures);
	}
	
	@Test
	public void testSaveUpdateEcriture() throws IOException {
		when(cacheEcritureDAO.getAll()).thenReturn(
				Collections.singleton(ecriture1));
		factory.save(cacheDAO);
		
		ecriture1.setLibelle("nom modifié");
		
		// Méthode testée (mise à jour due l'écriture 1)
		factory.save(cacheDAO);
		
		// Réinitialiser pour obliger la relecture depuis la BDD
		factory.reload();
		
		Iterator<Ecriture> ecrituresIterator = factory.getEcritures();
		assertTrue(ecrituresIterator.hasNext());
		assertEquals("nom modifié", ecrituresIterator.next().getLibelle());
		assertFalse(ecrituresIterator.hasNext());
	}
	
	@Test
	public void testSaveDeleteEcriture() throws IOException {
		when(cacheEcritureDAO.getAll()).thenReturn(
				Arrays.asList(new Ecriture[] {ecriture1, ecriture2}));
		
		factory.save(cacheDAO);
		
		when(cacheEcritureDAO.getAll()).thenReturn(
				Collections.singleton(ecriture2));
		
		// Méthode testée
		factory.save(cacheDAO);
		
		checkCollection(factory.getEcritures(), new Ecriture[] {ecriture2});
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
