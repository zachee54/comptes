package haas.olivier.comptes.test;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import haas.olivier.comptes.Compte;
import haas.olivier.comptes.CompteBancaire;
import haas.olivier.comptes.MessagesFactory;
import haas.olivier.util.Month;
import haas.olivier.comptes.Permanent;
import haas.olivier.comptes.dao.buffer.BufferableCompteDAO;
import haas.olivier.comptes.dao.buffer.BufferableDAOFactory;
import haas.olivier.comptes.dao.buffer.BufferableEcritureDAO;
import haas.olivier.comptes.dao.buffer.BufferablePermanentDAO;
import haas.olivier.comptes.dao.buffer.BufferableSuiviDAO;
import haas.olivier.comptes.dao.buffer.BufferedDAOFactory;
import haas.olivier.comptes.dao.buffer.BufferedPermanentDAO;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class BufferedPermanentDAOTest {

	private Permanent perm1, perm2, perm3, perm4, perm5, perm5null, perm3bis;
	private HashSet<Permanent> set;
	private BufferedDAOFactory mainDAO;
	private BufferableDAOFactory mockDAO;
	private BufferedPermanentDAO dao;
	private BufferablePermanentDAO subdao;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		// Neutraliser les messages pendant le test
		MessagesFactory.setFactory(mock(MessagesFactory.class));
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
		HashMap<Month, Integer> jours = new HashMap<Month, Integer>();
		perm1 = new Permanent(1, "oubgwd", mock(CompteBancaire.class),
				mock(Compte.class), jours);
		perm2 = new Permanent(2, "sef", mock(CompteBancaire.class),
				mock(Compte.class), jours);
		perm3 = new Permanent(3, "scyjhcg:li", mock(CompteBancaire.class),
				mock(Compte.class), jours);
		perm4 = new Permanent(4, "mlk,lhb", mock(CompteBancaire.class),
				mock(Compte.class), jours);

		// Définitions nécessaires pour comparer perm5 et perm5null
		CompteBancaire debit = mock(CompteBancaire.class);
		Compte credit = mock(Compte.class);
		
		perm3bis = new Permanent(3, "", mock(CompteBancaire.class),
				mock(Compte.class), jours);
		perm5null = new Permanent(null, "qkjby", debit, credit, jours);
		perm5 = new Permanent(5, "qkjby", debit, credit, jours);

		subdao = mock(BufferablePermanentDAO.class);
		set = new HashSet<Permanent>();
		set.add(perm1);
		set.add(perm2);
		set.add(perm3);
		set.add(perm4);
		when(subdao.getAll()).thenReturn(set);

		mockDAO = mock(BufferableDAOFactory.class);
		when(mockDAO.getCompteDAO()).thenReturn(mock(BufferableCompteDAO.class));
		when(mockDAO.getEcritureDAO()).thenReturn(mock(BufferableEcritureDAO.class));
		when(mockDAO.getPermanentDAO()).thenReturn(subdao);
		when(mockDAO.getHistoriqueDAO()).thenReturn(mock(BufferableSuiviDAO.class));
		when(mockDAO.getSoldeAVueDAO()).thenReturn(mock(BufferableSuiviDAO.class));
		when(mockDAO.getMoyenneDAO()).thenReturn(mock(BufferableSuiviDAO.class));
		
		mainDAO = new BufferedDAOFactory(mockDAO);
		dao = mainDAO.getPermanentDAO();
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testGet() throws IOException {
		assertSame(perm3, dao.get(3));
	}

	@Test
	public void testGetAll() throws IOException {
		assertEquals(set, dao.getAll());
	}

	@Test
	public void testAdd() throws IOException {
		dao.add(perm5null);
		assertTrue(perm5.equals(dao.get(5)));
	}

	@Test
	public void testUpdate() throws IOException {
		dao.update(perm3bis);
		assertSame(perm3bis, dao.get(3));
	}

	@Test
	public void testRemove() throws IOException {
		dao.remove(2);
		try {
			dao.get(2);
			fail("Doit lever une exception");
		} catch (IOException e) {
		}
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testSave() throws IOException {
		dao.update(perm3bis);
		dao.add(perm5);
		dao.remove(2);
//		dao.flush();
		mainDAO.save();

		verify(subdao).save((Map<Integer, Permanent>) any(),
				(Map<Integer, Permanent>) any(), (Set<Integer>) any());
	}

	@Test
	public void testMustBeSaved() throws IOException {
		// Test initial
		assertFalse(dao.mustBeSaved());

		// Test après ajout
		dao.add(perm5null);
		assertTrue(dao.mustBeSaved());

		// Test après flush
//		dao.flush();
		mainDAO.save();
		assertFalse(dao.mustBeSaved());

		// Test après modif
		dao.update(perm3bis);
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
