package haas.olivier.comptes.dao.cache;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import haas.olivier.comptes.Compte;
import haas.olivier.comptes.Permanent;
import haas.olivier.comptes.PermanentFixe;
import haas.olivier.comptes.PermanentProport;
import haas.olivier.comptes.PermanentSoldeur;
import haas.olivier.comptes.dao.CompteDAO;
import haas.olivier.util.Month;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class CachePermanentDAOTest {

	/**
	 * Des opérations permanentes.
	 */
	private static Permanent p1, p2, p3;
	
	/**
	 * Des comptes.
	 */
	private static final Compte c1 = mock(Compte.class),
			c2 = mock(Compte.class);
	
	/**
	 * L'ensembles des opérations permanentes au départ.
	 */
	private static final Set<Permanent> all = new HashSet<Permanent>();
	
	/**
	 * Objet testé.
	 */
	private CachePermanentDAO dao;
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		
		// Définir les opérations permanentes
		p1 = new PermanentFixe(1, "permanent1", c1, c2, "libellé1", "tiers1", false, new HashMap<Month, Integer>(), new HashMap<Month, BigDecimal>());
		p2 = new PermanentSoldeur(2, "permanent2", mock(Compte.class), c2, "libellé3", "tiers3", true, new HashMap<Month, Integer>());
		p3 = new PermanentProport(3, "permanent3", c2, c1, "libellé2", "tiers2", true, new HashMap<Month, Integer>(), p2, new BigDecimal("0.2"));
		
		// Les ajouter à la collection
		all.add(p1);
		all.add(p2);
		all.add(p3);
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
		
		// Un mock pour le DAO des comptes (nécessaire pour l'objet testé)
		CompteDAO cDAO = mock(CompteDAO.class);
		
		// Des mocks pour la sous-couche
		CacheableDAOFactory factory = mock(CacheableDAOFactory.class);
		when(factory.getPermanents((CachePermanentDAO) any()))
		.thenReturn(all.iterator());
		
		// Objet testé
		dao = new CachePermanentDAO(factory, cDAO);
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testGetAll() throws IOException {
		
		// Méthode testée
		Iterator<Permanent> it = dao.getAll().iterator();
		
		// Colllecter le résultat
		Set<Permanent> result = new HashSet<>();
		while (it.hasNext())
			result.add(it.next());
		
		// Vérifier
		assertTrue(all.equals(result));
	}

	@Test
	public void testGet() {
		
		// Les instances existantes
		assertSame(p1, dao.get(1));
		assertSame(p2, dao.get(2));
		assertSame(p3, dao.get(3));
		
		// Comportement avec un identifiant inexistant
		assertNull(dao.get(5));
	}

	@Test
	public void testAdd() {
		
		// Nouvelle opération
		Permanent p6 = new PermanentFixe(6, "permanent6", c2, c1, "libellé6", "tiers6", false, new HashMap<Month, Integer>(), new HashMap<Month, BigDecimal>());
		
		// Méthode testée
		dao.add(p6);
		assertSame(p6, dao.get(6));
		
		// Tester en même temps le statut de sauvegarde
		assertTrue(dao.mustBeSaved());
		dao.setSaved();
		assertFalse(dao.mustBeSaved());
	}

	@Test
	public void testUpdate() {
		
		// Opération ayant un identifiant pré-existant
		Permanent p2bis = new PermanentSoldeur(2, "permanent2bis", mock(Compte.class), c1, "libellé2bis", "tiers2bis", false, new HashMap<Month, Integer>());
		
		// Méthode testée
		dao.update(p2bis);
		assertSame(p2bis, dao.get(2));

		// Vérifier que cela n'a pas changé le reste
		assertSame(p1, dao.get(1));
		assertSame(p3, dao.get(3));
		
		// Tester en même temps le statut de sauvegarde
		assertTrue(dao.mustBeSaved());
		dao.setSaved();
		assertFalse(dao.mustBeSaved());
	}

	@Test
	public void testRemove() throws IOException {
		
		// Enlever une opération
		dao.remove(1);
		assertNull(dao.get(1));
		assertSame(p2, dao.get(2));	// Vérifier la conservation des autres
		assertSame(p3, dao.get(3));
		
		// Enlever une deuxième
		dao.remove(3);
		assertNull(dao.get(3));
		assertSame(p2, dao.get(2));	// Vérifier la conservation des autres
		assertNull(dao.get(1));
		
		// Vérifier le contenu global
		Iterator<Permanent> it = dao.getAll().iterator();
		assertTrue(it.hasNext());
		assertSame(p2, it.next());
		assertFalse(it.hasNext());
	}

	@Test
	public void testErase() throws IOException {
		
		// Méthode testée
		dao.erase();
		
		// Vérification globale
		assertFalse(dao.getAll().iterator().hasNext());
		
		// Vérification par l'accès par identifiant
		for (int i=0; i<4; i++)
			assertNull(dao.get(i));
	}

	@Test
	public void testMustBeSaved() {
		
		// Situation de départ
		assertFalse(dao.mustBeSaved());
		
		// Après de simples consultations
		dao.get(1);
		assertFalse(dao.mustBeSaved());
	}
}
