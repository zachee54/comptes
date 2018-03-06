package haas.olivier.comptes.test;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

import haas.olivier.util.Month;
import haas.olivier.comptes.dao.buffer.BufferableCompteDAO;
import haas.olivier.comptes.dao.buffer.BufferableDAOFactory;
import haas.olivier.comptes.dao.buffer.BufferableEcritureDAO;
import haas.olivier.comptes.dao.buffer.BufferablePermanentDAO;
import haas.olivier.comptes.dao.buffer.BufferableSuiviDAO;
import haas.olivier.comptes.dao.buffer.BufferedDAOFactory;
import haas.olivier.comptes.dao.buffer.BufferedSuiviDAO;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class BufferedSuiviDAOTest {

	private static SimpleDateFormat df;
	private static Month month1, month2, month3;
	private static BigDecimal v11, v12, v21, v22, v31, v32;
	private BufferedDAOFactory mainDAO;
	private BufferableDAOFactory mockDAO;
	private BufferableSuiviDAO suivi;
	private BufferedSuiviDAO dao;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		df = new SimpleDateFormat("dd/MM/yy");
		month1 = new Month(df.parse("01/11/12"));
		month2 = new Month(df.parse("01/12/12"));
		month3 = new Month(df.parse("01/01/13"));
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
		suivi = mock(BufferableSuiviDAO.class);
		mockDAO = mock(BufferableDAOFactory.class);
		when(mockDAO.getCompteDAO()).thenReturn(mock(BufferableCompteDAO.class));
		when(mockDAO.getEcritureDAO()).thenReturn(mock(BufferableEcritureDAO.class));
		when(mockDAO.getPermanentDAO()).thenReturn(mock(BufferablePermanentDAO.class));
		when(mockDAO.getHistoriqueDAO()).thenReturn(suivi);
		when(mockDAO.getSoldeAVueDAO()).thenReturn(mock(BufferableSuiviDAO.class));
		when(mockDAO.getMoyenneDAO()).thenReturn(mock(BufferableSuiviDAO.class));
		mainDAO = new BufferedDAOFactory(mockDAO);
		dao = mainDAO.getHistoriqueDAO();

		v11 = new BigDecimal("845.03");
		v12 = new BigDecimal("-666.5");
		v21 = new BigDecimal("-390.08");
		v22 = new BigDecimal("463.26");
		v31 = new BigDecimal("-214.11");
		v32 = new BigDecimal("549.56");

		Map<Month, Map<Integer, BigDecimal>> map, map2, map3;

		map = new HashMap<Month, Map<Integer, BigDecimal>>();
		Map<Integer, BigDecimal> submap1, submap2, submap3;
		submap1 = new HashMap<Integer, BigDecimal>();
		submap2 = new HashMap<Integer, BigDecimal>();
		submap3 = new HashMap<Integer, BigDecimal>();

		submap1.put(1, v11);
		submap1.put(2, v12);
		map.put(month1, submap1);

		submap2.put(1, v21);
		submap2.put(2, v22);
		map.put(month2, submap2);

		submap3.put(1, v31);
		submap3.put(2, v32);
		map.put(month3, submap3);

		map2 = new HashMap<Month, Map<Integer, BigDecimal>>(map);
		map2.remove(month1);

		map3 = new HashMap<Month, Map<Integer, BigDecimal>>(map2);
		map3.remove(month2);
		
		when(suivi.getAll()).thenReturn(map);
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testGetAll() throws IOException {
		// vérifier qu'il renvoie le même cache
		assertEquals(suivi.getAll(), dao.getAll());
	}
	
	@Test
	public void testGet() throws IOException {
		assertEquals(0, v21.compareTo(dao.get(1, month2)));
		assertEquals(0, v32.compareTo(dao.get(2, month3)));
	}

	@Test
	public void testSet() throws IOException {
		BigDecimal valeur = new BigDecimal("-5410596");

		// Test simple
		dao.set(2, month2, valeur);
		assertEquals(0, valeur.compareTo(dao.get(2, month2)));

		// Test complémentaire: ne modifie pas les autres valeurs
		assertEquals(0, v21.compareTo(dao.get(1, month2)));
		assertEquals(0, v32.compareTo(dao.get(2, month3)));
	}

	@Test
	public void testRemoveFrom() throws IOException {
		dao.set(1, month3, new BigDecimal("654.53"));
		dao.removeFrom(month2);
		assertEquals(0, v11.compareTo(dao.get(1, month1)));			// Inchangé
		assertNull(dao.get(2, month2));
		assertNull(dao.get(1, month3));
	}

	@Test
	public void testRemoveSuiviCompte() throws IOException {
		dao.removeSuiviCompte(1);
		verify(suivi, never()).removeSuiviCompte(1);
//		dao.flush();
		mainDAO.save();
		verify(suivi).removeSuiviCompte(1);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testSave() throws IOException {
		BigDecimal valeur1 = new BigDecimal("1789");
		BigDecimal valeur2 = new BigDecimal("1515.15");

		// Des changements
		dao.removeFrom(month3);
		dao.set(1, month2, valeur1);
		dao.set(2, month3, valeur2);

		Map<Month, Map<Integer, BigDecimal>> expected = new HashMap<Month, Map<Integer, BigDecimal>>();
		expected.put(month2, new HashMap<Integer, BigDecimal>());
		expected.put(month3, new HashMap<Integer, BigDecimal>());
		expected.get(month2).put(1, valeur1);
		expected.get(month3).put(2, valeur2);

		// Test
//		dao.flush();
		mainDAO.save();
		verify(suivi).save(eq(month3),
				(Map<Month, Map<Integer, BigDecimal>>) any());
		/*
		 * Il semble qu'il y ait un bug Mockito: le débogage montre une Map
		 * remplie alors que Mockito intercepte une Map vide... ?? --> Confirmé
		 * par un test "en dur".
		 */
	}

	@Test
	public void testMustBeSaved() throws IOException {
		// Test initial
		assertFalse(dao.mustBeSaved());

		// Test après une modif
		dao.set(1, month1, v12);
		assertTrue(dao.mustBeSaved());

		// Test après flush
//		dao.flush();
		mainDAO.save();
		assertFalse(dao.mustBeSaved());

		// Test après suppression
		dao.removeFrom(month3);
		assertTrue(dao.mustBeSaved());
//		dao.flush();
		mainDAO.save();
		assertFalse(dao.mustBeSaved());

		// Test après suppression d'un compte
		dao.removeSuiviCompte(2);
		assertTrue(dao.mustBeSaved());
//		dao.flush();
		mainDAO.save();
		assertFalse(dao.mustBeSaved());
	}
	
	@Test
	public void testGetAllAvecConcurrence() throws IOException {
		class Testeur extends Thread {
			@Override public void run() {
				// Appeler le cache
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
		
		// Vérifier que le sous-DAO n'a reçu qu'un appel
		verify(suivi, times(1)).getAll();
	}
}
