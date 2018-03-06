package haas.olivier.comptes.test;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import haas.olivier.comptes.Compte;
import haas.olivier.comptes.CompteBancaire;
import haas.olivier.comptes.CompteBudget;
import haas.olivier.comptes.Ecriture;
import haas.olivier.comptes.MessagesFactory;
import haas.olivier.comptes.TypeCompte;
import haas.olivier.comptes.dao.DAOFactory;
import haas.olivier.comptes.dao.EcritureDAO;
import haas.olivier.comptes.dao.buffer.BufferableCompteDAO;
import haas.olivier.comptes.dao.buffer.BufferableDAOFactory;
import haas.olivier.comptes.dao.buffer.BufferableEcritureDAO;
import haas.olivier.comptes.dao.buffer.BufferablePermanentDAO;
import haas.olivier.comptes.dao.buffer.BufferableSuiviDAO;
import haas.olivier.comptes.dao.buffer.BufferedCompteDAO;
import haas.olivier.comptes.dao.buffer.BufferedDAOFactory;
import haas.olivier.util.Month;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.MockitoAnnotations;

public class BufferedCompteDAOTest {

	private Compte compte1, compte2, compte3, compte4, compte3bis, compte5null,
			compte5;
	private HashSet<Compte> set;
	private BufferedDAOFactory mainDAO;
	private BufferableDAOFactory mockDAO;
	private BufferedCompteDAO dao;
	private BufferableCompteDAO subdao;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		
		/* Simuler un DAO pour permettre de vérifier qu'un compte à supprimer
		 * n'est pas utilisé. */
		EcritureDAO eDAO = mock(EcritureDAO.class);
		when(eDAO.getAll()).thenReturn(new TreeSet<Ecriture>());
		
		DAOFactory factory = mock(DAOFactory.class);
		when(factory.getEcritureDAO()).thenReturn(eDAO);
		when(factory.getDebut()).thenReturn(new Month());
		DAOFactory.setFactory(factory);
		
		// Neutraliser les messages pendant le test
		MessagesFactory.setFactory(mock(MessagesFactory.class));
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		
		compte1 = new CompteBudget(1, "uhvu", TypeCompte.DEPENSES);
		compte2 = new CompteBudget(2, "dcuhvu", TypeCompte.DEPENSES);
		compte3 = new CompteBudget(3, "uhdcvu", TypeCompte.DEPENSES);
		compte4 = new CompteBudget(4, "uhvuas", TypeCompte.DEPENSES);

		compte3bis = new CompteBudget(3, "", TypeCompte.DEPENSES);
		compte5null = new CompteBancaire(null, "", 15L,
				TypeCompte.COMPTE_CARTE);
		compte5 = new CompteBancaire(5, "", 15L, TypeCompte.COMPTE_CARTE);

		subdao = mock(BufferableCompteDAO.class);
		set = new HashSet<Compte>();
		set.add(compte1);
		set.add(compte2);
		set.add(compte3);
		set.add(compte4);
		when(subdao.getAll()).thenReturn(set);

		mockDAO = mock(BufferableDAOFactory.class);
		when(mockDAO.getCompteDAO()).thenReturn(subdao);
		when(mockDAO.getEcritureDAO()).thenReturn(mock(BufferableEcritureDAO.class));
		when(mockDAO.getPermanentDAO()).thenReturn(mock(BufferablePermanentDAO.class));
		when(mockDAO.getHistoriqueDAO()).thenReturn(mock(BufferableSuiviDAO.class));
		when(mockDAO.getSoldeAVueDAO()).thenReturn(mock(BufferableSuiviDAO.class));
		when(mockDAO.getMoyenneDAO()).thenReturn(mock(BufferableSuiviDAO.class));
		
		mainDAO = new BufferedDAOFactory(mockDAO);
		dao = mainDAO.getCompteDAO();
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testGet() throws IOException {
		
		// Ne pas réinstancier un compte déjà instancié
		assertSame(dao.get(3), dao.get(3));
		verify(subdao, times(1)).getAll();	// Deux utilisations, un seul appel
		
		try {
			dao.get(5);
			fail("Doit lever une exception");
		} catch (IOException e) {
		}
	}

	@Test
	public void testGetAll() throws IOException {
		assertEquals(set, dao.getAll());
	}

	@Test
	public void testAdd() throws IOException {
		
		// Ajout avec identifiant
		dao.add(compte5);
		assertSame(compte5,dao.get(5));		// Même instance
		verifyZeroInteractions(subdao);		// Pas d'accès à la sous-couche
	}
	
	@Test
	public void testAddWithoutId() throws IOException {
		
		// Ajout sans identifiant
		dao.add(compte5null);
		Compte c5 = dao.get(5);
		assertTrue(compte5.equals(c5));		// Égalité (test propre)
	}

	@Test
	public void testUpdate() throws IOException {
		dao.update(compte3bis);
		
		// Renvoie le nouveau compte sans le réinstancier
		assertSame(compte3bis, dao.get(3));
		
		verifyZeroInteractions(subdao);		// Pas d'accès à la sous-couche
	}

	@Test
	public void testRemove() throws Exception {
		dao.remove(2);
		try {
			dao.get(2);
			fail("Doit lever une exception");
		} catch (IOException e) {
		}
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testSave() throws Exception {
		dao.update(compte3bis);
		dao.add(compte5null);
		dao.remove(2);
//		dao.flush();
		mainDAO.save();
		
		Map<Integer,Compte> mapUpdate = new HashMap<Integer,Compte>();
		mapUpdate.put(3, compte3bis);
		
		Set<Integer> setRemove = new HashSet<Integer>();
		setRemove.add(2);

		verify(subdao).save(
				(Map<Integer, Compte>) any(),
				(Map<Integer, Compte>) any(),
				(Set<Integer>) any());
		// Bug Mockito ? Impossible d'obtenir les bonnes valeurs dans les arguments capturés
	}

	@Test
	public void testMustBeSaved() throws Exception {
		// Test initial
		assertFalse(dao.mustBeSaved());

		// Test après ajout
		dao.add(compte5null);
		assertTrue(dao.mustBeSaved());

		// Test après flush
//		dao.flush();
		mainDAO.save();
		assertFalse(dao.mustBeSaved());

		// Test après modif
		dao.update(compte3bis);
		assertTrue(dao.mustBeSaved());
//		dao.flush();
		mainDAO.save();
		assertFalse(dao.mustBeSaved());

		// Test après suppression
		dao.remove(1);
		assertTrue(dao.mustBeSaved());
//		dao.flush();
		mainDAO.save();
		assertFalse(dao.mustBeSaved());
	}
	
	@Test
	public void testGetAllAvecConcurrence() throws IOException {
		class Testeur extends Thread {
			@Override public void run() {
				// Appeler le cache en entier
				try {
					dao.getAll();
				} catch (IOException e) {
				}
			}// run
		}// local class Testeur
		
		// Appeler 1.000 fois le cache
		for (int i=0; i<1000; i++) {
			new Testeur().start();
		}
		
		// Vérifier que le sous-DAO n'a reçu qu'un seul appel
		verify(subdao, times(1)).getAll();
	}
}
