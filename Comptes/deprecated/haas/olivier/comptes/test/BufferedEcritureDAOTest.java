package haas.olivier.comptes.test;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import haas.olivier.comptes.Compte;
import haas.olivier.comptes.CompteBancaire;
import haas.olivier.comptes.CompteBudget;
import haas.olivier.comptes.Ecriture;
import haas.olivier.comptes.MessagesFactory;
import haas.olivier.util.Month;
import haas.olivier.comptes.TypeCompte;
import haas.olivier.comptes.dao.buffer.BufferableCompteDAO;
import haas.olivier.comptes.dao.buffer.BufferableDAOFactory;
import haas.olivier.comptes.dao.buffer.BufferableEcritureDAO;
import haas.olivier.comptes.dao.buffer.BufferablePermanentDAO;
import haas.olivier.comptes.dao.buffer.BufferableSuiviDAO;
import haas.olivier.comptes.dao.buffer.BufferedDAOFactory;
import haas.olivier.comptes.dao.buffer.BufferedEcritureDAO;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class BufferedEcritureDAOTest {

	private static final DateFormat df = new SimpleDateFormat("dd/MM/yy");
	private static final Compte compte1 = new CompteBancaire(1, "C/C",
			16856740L, TypeCompte.COMPTE_COURANT);
	private static final Compte compte2 = new CompteBancaire(2, "CB",
			16856741L, TypeCompte.COMPTE_CARTE);
	private static final Compte compte3 = new CompteBudget(3, "Sécu",
			TypeCompte.RECETTES);
	private static final Compte compte4 = new CompteBudget(4, "Courses",
			TypeCompte.DEPENSES);
	private static Ecriture e1, e2, e2bis, e3, e4, e4null, e4bis, e7, e7bis,
			e7ter;
	private BufferedDAOFactory mainDAO;
	private BufferableDAOFactory mockDAO;
	private BufferedEcritureDAO dao;
	private BufferableEcritureDAO subdao;
	private TreeSet<Ecriture> all;

	private void compareList(Collection<Ecriture> list1, Collection<Ecriture> list2) {
		assertEquals(list1.size(), list2.size());
		Iterator<Ecriture> it1 = list1.iterator();
		Iterator<Ecriture> it2 = list2.iterator();

		while (it1.hasNext()) {
			assertEquals(it1.next(), it2.next());
		}
	}

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		// Définir les propriétés des comptes utiles
		compte1.setOuverture(df.parse("01/09/02"));
		compte1.setCloture(df.parse("15/08/04"));
		compte2.setCloture(df.parse("25/12/11"));

		// Définir les écritures utiles
		e1 = new Ecriture(1, df.parse("09/01/10"), null, compte1, compte3,
				new BigDecimal("89.22"), "e1", "Kiné", 2086504);
		e2 = new Ecriture(2, df.parse("24/08/10"), df.parse("27/10/10"),
				compte1, compte4, new BigDecimal("49.31"), "e2", "Lidl",
				2142058);
		e3 = new Ecriture(3, df.parse("25/08/10"), df.parse("25/08/10"),
				compte1, compte4, new BigDecimal("50.08"), "e3", "Rabo d'or",
				2142059);
		e4 = new Ecriture(4, df.parse("26/08/10"), df.parse("26/09/10"),
				compte2, compte1, new BigDecimal("49"), "e4", "Dr Courtois",
				null);
		e2bis = new Ecriture(2, df.parse("08/02/03"), df.parse("10/02/11"),
				compte1, compte2, new BigDecimal("3518.1"), "e2bis",
				"quelqu'un", 6152);
		e4null = new Ecriture(null, df.parse("26/08/10"), df.parse("26/09/10"),
				compte2, compte1, new BigDecimal("49"), "e4", "Dr Courtois",
				null);
		e4bis = new Ecriture(4, df.parse("26/08/10"), null, compte2, compte1,
				new BigDecimal("49"), "e4", "Dr Courtois", null);
		e7 = new Ecriture(7, df.parse("08/02/11"), df.parse("10/02/13"),
				compte1, compte2, new BigDecimal("3518.1"), "e7", "quelqu'un",
				6152);
		e7bis = new Ecriture(7, df.parse("04/08/10"), df.parse("04/09/12"),
				compte3, compte1, new BigDecimal("563"), "e7bis", null, 65123);
		e7ter = new Ecriture(7, df.parse("08/02/01"), df.parse("10/02/03"),
				compte1, compte2, new BigDecimal("3518.1"), "e7", "quelqu'un",
				6152);

		// Neutraliser les messages intempestifs
		MessagesFactory.setFactory(mock(MessagesFactory.class));
	}

	@Before
	public void setUp() throws Exception {

		all = new TreeSet<Ecriture>();
		all.add(e3);
		all.add(e2);
		all.add(e1);

		// Mocker le DAO sous-jacent
		subdao = mock(BufferableEcritureDAO.class);
		when(subdao.getAll()).thenReturn(all);

		mockDAO = mock(BufferableDAOFactory.class);
		when(mockDAO.getCompteDAO()).thenReturn(mock(BufferableCompteDAO.class));
		when(mockDAO.getEcritureDAO()).thenReturn(subdao);
		when(mockDAO.getPermanentDAO()).thenReturn(mock(BufferablePermanentDAO.class));
		when(mockDAO.getHistoriqueDAO()).thenReturn(mock(BufferableSuiviDAO.class));
		when(mockDAO.getSoldeAVueDAO()).thenReturn(mock(BufferableSuiviDAO.class));
		when(mockDAO.getMoyenneDAO()).thenReturn(mock(BufferableSuiviDAO.class));
		
		mainDAO = new BufferedDAOFactory(mockDAO);
		dao = mainDAO.getEcritureDAO();
	}

	@Test
	public void testAdd() throws IOException, ParseException {

		// Test simple
		dao.add(e4null);
		assertEquals(e4, dao.get(4));

		// Test avec identifiant imposé
		dao.add(e7);
		assertEquals(e7, dao.get(7));
	}

	@Test
	public void testUpdate() throws IOException, ParseException {

		// Update simple
		dao.update(e2bis);
		assertSame(e2bis, dao.get(2));

		// Add suivi de update
		dao.add(e7);
		dao.update(e7bis);
		assertSame(e7bis, dao.get(7));
	}

	@Test
	public void testRemove() throws IOException {

		// Test simple
		dao.remove(3);
		try {
			dao.get(3);
			fail("Doit lever une exception");
		} catch (IOException e) {
		}

		// Add puis remove
		dao.add(e7);
		dao.remove(7);
		try {
			dao.get(7);
			fail("Doit lever une exception");
		} catch (IOException e) {
		}

		// Update puis remove
		dao.update(e2bis);
		dao.remove(2);
		try {
			dao.get(2);
			fail("Doit lever une exception");
		} catch (IOException e) {
		}
	}

	@Test
	public void testGet() throws IOException {
		assertSame(e1, dao.get(1));
		assertSame(e3, dao.get(3));
	}

	@Test
	public void testGetAll() throws IOException {
		// Test simple
		compareList(all, dao.getAll());

		ArrayList<Ecriture> list = new ArrayList<Ecriture>(all);
		
		// Add et GetAll
		list.add(0, e4);
		dao.add(e4null);
		compareList(list, dao.getAll());

		// Update et GetAll
		list.remove(e2);
		list.add(e2bis);
		dao.update(e2bis);
		compareList(list, dao.getAll());

		// Remove et GetAll
		list.remove(e3);
		dao.remove(3);
		compareList(list, dao.getAll());
	}

	@Test
	public void testGetAllSince() throws ParseException, IOException {
		Month mois = new Month(df.parse("01/08/10"));
//		all.remove(e1);	
		List<Ecriture> list = new ArrayList<Ecriture>(all);

		// Test simple
		list.remove(e1);
		compareList(list, dao.getAllSince(mois));

		// Add null puis GetAllSince
		list.add(0, e4);
		dao.add(e4null);
		compareList(list, dao.getAllSince(mois));

		// Add avec id puis GetAllSince
		list.add(0, e7);
		dao.add(e7);
		compareList(list, dao.getAllSince(mois));

		// Update (à enlever) puis GetAllSince
		list.remove(e2);
		dao.update(e2bis);
		compareList(list, dao.getAllSince(mois));

		// Update (à mettre à jour) puis GetAllSince
		list.remove(e7);
		list.add(e7bis);
		dao.update(e7bis);
		compareList(list, dao.getAllSince(mois));

		// Remove puis getAllSince
		list.remove(e3);
		dao.remove(3);
		compareList(list, dao.getAllSince(mois));
	}

	@Test
	public void testGetPointagesSince() throws ParseException, IOException {
		Month mois = new Month(df.parse("31/08/10"));

		ArrayList<Ecriture> list = new ArrayList<Ecriture>();
		list.add(e1);
		list.add(e2);
		list.add(e3);

		// Test simple
		compareList(list, dao.getPointagesSince(mois));

		// Add null puis getPointages
		list.add(2, e4);
		dao.add(e4null);
		compareList(list, dao.getPointagesSince(mois));

		// Add hors champ puis getPointages
		dao.add(e7ter);
		compareList(list, dao.getPointagesSince(mois));

		// Update puis getPointages
		list.remove(e2);
		list.add(1, e2bis);
		dao.update(e2bis);
		compareList(list, dao.getPointagesSince(mois));

		// Update qui fait passer dans la liste puis getPointages
		list.remove(e7ter);
		list.add(1, e7bis);
		dao.update(e7bis);
		compareList(list, dao.getPointagesSince(mois));

		// Remove puis getPointages
		list.remove(e3);
		dao.remove(3);
		compareList(list, dao.getPointagesSince(mois));

		// Update sans pointage
		list.remove(e4);
		list.add(0, e4bis); // Ajouter au début
		dao.update(e4bis);
//		compareSet(list, new HashSet<Ecriture>(dao.getPointagesSince(mois)));
		compareList(list, dao.getPointagesSince(mois));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testSave() throws IOException {
//		dao.flush();
		mainDAO.save();

		verify(subdao).save((Map<Integer, Ecriture>) any(),
				(Map<Integer, Ecriture>) any(), (Set<Integer>) any());
	}

	@Test
	public void testMustBeSaved() throws IOException {
		// Test initial
		assertFalse(dao.mustBeSaved());

		// Test après un ajout
		dao.add(e7);
		assertTrue(dao.mustBeSaved());

		// Test après flush
//		dao.flush();
		mainDAO.save();
		assertFalse(dao.mustBeSaved());

		// Test après mise à jour
		dao.update(e2bis);
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
			@Override
			public void run() {
				 try {
					// Appeler tout le cache
					dao.getAll();
				} catch (IOException e) {
				}
			}
		}// local class Testeur
		
		
		// Appeler 10.000 fois le cache de manière concurrente
		for (int i=0; i<1000; i++) {
			new Testeur().start();
		}
		
		// Vérifier que le DAO sous-jacent n'a reçu qu'un appel
		verify(subdao, times(1)).getAll();
	}
}
