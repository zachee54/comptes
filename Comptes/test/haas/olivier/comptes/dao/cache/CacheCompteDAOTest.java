package haas.olivier.comptes.dao.cache;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Map.Entry;
import haas.olivier.comptes.Banque;
import haas.olivier.comptes.Compte;
import haas.olivier.comptes.CompteBancaire;
import haas.olivier.comptes.CompteBudget;
import haas.olivier.comptes.Ecriture;
import haas.olivier.comptes.Permanent;
import haas.olivier.comptes.TypeCompte;
import haas.olivier.comptes.dao.CompteDAO;
import haas.olivier.comptes.dao.DAOFactory;
import haas.olivier.comptes.dao.EcritureDAO;
import haas.olivier.util.Month;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/** La classe
 * {@link haas.olivier.comptes.dao.cache.CacheDAOFactory.CacheCompteDAO} étant
 * une classe interne de <code>CacheDAOFactory</code>, on doit instancier
 * complètement celle-ci pour pouvoir tester celle-là.
 *
 * @author Olivier HAAS
 */
public class CacheCompteDAOTest {

	/** Des comptes. */
	private static final Collection<Compte> comptes = new HashSet<>();
	private static final Compte c1 =
			new CompteBancaire(1, "compte1", 124L, TypeCompte.COMPTE_CARTE),
			c2 = new CompteBudget(2, "compte2", TypeCompte.RECETTES_EN_EPARGNE),
			c3 = new CompteBancaire(3, "compte3", 56L, TypeCompte.COMPTE_EPARGNE);
	
	/** Source de données mockée. */
	private static final CacheableDAOFactory cacheable =
			mock(CacheableDAOFactory.class);
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		
		// Collection des comptes utilisés pour le test
		comptes.add(c1);
		comptes.add(c2);
		comptes.add(c3);
		
		// Définir le comportement de la source mockée
		when(cacheable.getBanques()).thenReturn(
				Collections.<Banque>emptyIterator());
		when(cacheable.getHistorique()).thenReturn(
				Collections.<Entry<Month, Entry<Integer, BigDecimal>>>emptyIterator());
		when(cacheable.getMoyennes()).thenReturn(
				Collections.<Entry<Month, Entry<Integer, BigDecimal>>>emptyIterator());
		when(cacheable.getSoldesAVue()).thenReturn(
				Collections.<Entry<Month, Entry<Integer, BigDecimal>>>emptyIterator());
		when(cacheable.getPermanents(
				(CachePermanentDAO) any(), (CompteDAO) any())).thenReturn(
						Collections.<Permanent>emptyIterator());
		when(cacheable.getProperties()).thenReturn(
				mock(CacheablePropertiesDAO.class));
	}// setUpBeforeClass

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}
	
	/** La fabrique. */
	private DAOFactory factory;
	
	/** Objet testé. */
	private CompteDAO dao;

	@Before
	public void setUp() throws Exception {
		
		Collection<Ecriture> ecritures = new ArrayList<>();
		ecritures.add(new Ecriture(1, new Date(), null, c1, c2, BigDecimal.ONE, null, null, null));
		ecritures.add(new Ecriture(2, new Date(), null, c2, c3, BigDecimal.TEN, null, null, null));
		when(cacheable.getEcritures((CompteDAO) any())).thenReturn(
				ecritures.iterator());
		
		/* Utiliser un nouvel itérateur des comptes à chaque test (sinon, il ne
		 * marche que pour le premier test et ensuite il ne renvoie plus rien !)
		 */
		when(cacheable.getComptes()).thenReturn(comptes.iterator());
		
		// Fabrique
		factory = new CacheDAOFactory(cacheable);
		
		// Objet testé
		dao = factory.getCompteDAO();
	}// setUp

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testGetAll() throws IOException {
		Collection<Compte> result = dao.getAll();
		assertEquals(3, result.size());
		for (Compte c : result)
			assertTrue(comptes.contains(c));
	}// testGetAll
	
	@Test
	public void testGet() throws IOException {
		assertEquals(c1, dao.get(1));
		assertEquals(c2, dao.get(2));
		assertEquals(c3, dao.get(3));
		assertNull(dao.get(4));
	}// testGet
	
	@Test
	public void testAddCompteBudget() throws IOException {
		Compte c4sansId = new CompteBudget(null, "compte4", TypeCompte.DEPENSES),
				c4 = new CompteBudget(4, "compte4", TypeCompte.DEPENSES);
		
		// Méthode testée
		dao.add(c4sansId);
		
		assertEquals(c4, dao.get(4));
		assertEquals(4, dao.getAll().size());
	}// testAddCompte Budget
	
	@Test
	public void testAddCompteBancaire() throws IOException {
		Compte c4sansId = new CompteBancaire(null, "compte5", 47L, TypeCompte.COMPTE_EPARGNE),
				c4 = new CompteBancaire(4, "compte5", 47L, TypeCompte.COMPTE_EPARGNE);
		
		// Méthode testée
		dao.add(c4sansId);
		
		assertEquals(c4, dao.get(4));
		assertEquals(4, dao.getAll().size());
	}// testAddCompteBancaire
	
	@Test
	public void testUpdate() throws IOException {
		Compte c2bis = new CompteBancaire(2, "compte2bis", 0L, TypeCompte.EMPRUNT);
		
		// Méthode testée
		dao.update(c2bis);
		
		assertEquals(c2bis, dao.get(2));
		assertEquals(3, dao.getAll().size());
		
		EcritureDAO eDAO = factory.getEcritureDAO();
		assertSame(c2bis, eDAO.get(1).credit);
		assertSame(c2bis, eDAO.get(2).debit);
	}// testUpdate
	
	@Test
	public void testRemove() throws IOException {
		dao.remove(2);
		assertNull(dao.get(2));
		assertEquals(2, dao.getAll().size());
	}// testRemove

}
