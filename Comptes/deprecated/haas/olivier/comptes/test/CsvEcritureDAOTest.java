package haas.olivier.comptes.test;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.CharArrayWriter;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import haas.olivier.comptes.Compte;
import haas.olivier.comptes.CompteBancaire;
import haas.olivier.comptes.CompteBudget;
import haas.olivier.comptes.Ecriture;
import haas.olivier.comptes.TypeCompte;
import haas.olivier.comptes.dao.CompteDAO;
import haas.olivier.comptes.dao.DAOFactory;
import haas.olivier.comptes.dao.buffer.BufferedCompteDAO;
import haas.olivier.comptes.dao.csv.CsvCompteDAO;
import haas.olivier.comptes.dao.csv.CsvEcritureDAO;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.mockito.Mockito.*;

public class CsvEcritureDAOTest {

	private static final File FILENAME = new File("test/test.csv");
	private static final DateFormat df = new SimpleDateFormat("dd/MM/yy");
	private CsvEcritureDAO dao;
	private static final Compte compte1 = new CompteBancaire(1, "C/C",
			16856740L, TypeCompte.COMPTE_COURANT);
	private static final Compte compte2 = new CompteBancaire(2, "CB",
			16856741L, TypeCompte.COMPTE_CARTE);
	private static final Compte compte3 = new CompteBudget(3, "Sécu",
			TypeCompte.RECETTES);
	private static final Compte compte4 = new CompteBudget(4, "Courses",
			TypeCompte.DEPENSES);
	private static Ecriture e1, e2, e3, e4;
	private Set<Integer> remove = new HashSet<Integer>();
	private Map<Integer, Ecriture> update = new HashMap<Integer, Ecriture>();
	private Map<Integer, Ecriture> add = new HashMap<Integer, Ecriture>();

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		// Définir les comptes utiles
		compte1.setOuverture(df.parse("01/09/02"));
		compte1.setCloture(df.parse("15/08/04"));
		compte2.setCloture(df.parse("25/12/11"));

		// Définir les écritures utiles
		e1 = new Ecriture(1, df.parse("09/01/10"), null, compte1, compte3,
				new BigDecimal("89.22"), "ouille", "Kiné", 2086504);
		e2 = new Ecriture(2, df.parse("24/08/10"), df.parse("27/10/10"),
				compte1, compte4, new BigDecimal("49.31"), "shopping", "Lidl",
				2142058);
		e3 = new Ecriture(3, df.parse("25/08/10"), df.parse("25/08/10"),
				compte1, compte4, new BigDecimal("50.08"), "", "Rabo d'Or",
				2142059);
		e4 = new Ecriture(4, df.parse("26/08/10"), df.parse("26/09/10"),
				compte2, compte3, new BigDecimal("49"), "malade",
				"Dr Courtois", null);
	}

	@Before
	public void setUp() throws Exception {
		// Récupérer le contenu du fichier test
		Reader in = new BufferedReader(new FileReader(FILENAME));
		final CharArrayWriter writer = new CharArrayWriter();
		int c;
		while ((c = in.read()) != -1) {
			writer.write(c);
		}
		in.close();
		writer.close();
		/* On a besoin d'utiliser une classe anonyme pour imposer le contenu
		 * sans passer par la fabrique CsvDAO.
		 */
		dao = new CsvEcritureDAO() {
			CsvEcritureDAO init() {
				setContent(writer.toCharArray());
				return this;
			}// init
		}.init();
		
		// Récupérer aussi le fichier test des comptes
		Reader in2 = new BufferedReader(new FileReader(new File("test/testcomptes.csv")));
		final CharArrayWriter writer2 = new CharArrayWriter();
		while ((c = in2.read()) != -1) {
			writer2.write(c);
		}
		in2.close();
		writer2.close();
		// ibidem
		CompteDAO cDAO = new BufferedCompteDAO(new CsvCompteDAO() {
			CsvCompteDAO init() {
				setContent(writer2.toCharArray());
				return this;
			}// init
		}.init());
		
		/*
		 * Mocker la DAOFactory pour que le DAO des comptes pointe vers le
		 * fichier test
		 */
		DAOFactory mockDAO = mock(DAOFactory.class);
		when(mockDAO.getCompteDAO()).thenReturn(cDAO);
		
		DAOFactory.setFactory(mockDAO);
	}// setUp

	@Test
	public void testGetAll() throws IOException {
		TreeSet<Ecriture> list = new TreeSet<Ecriture>();
		list.add(e4);
		list.add(e3);
		list.add(e2);
		list.add(e1);

		TreeSet<Ecriture> list2 = dao.getAll();
		assertEquals(4, list2.size());

		Iterator<Ecriture> it1 = list.iterator();
		Iterator<Ecriture> it2 = list2.iterator();
		while (it1.hasNext()) {
			assertTrue(it1.next().equals(it2.next()));
		}
	}

	@Test
	public void testSaveDelete() throws IOException {
		// Supprimer deux écritures
		remove.add(2);
		remove.add(3);
		dao.save(add, update, remove);

		// Vérifier qu'elles n'y sont plus
		TreeSet<Ecriture> all = dao.getAll();
		for (Ecriture e : all) {
			assertFalse(e.equals(e2));
			assertFalse(e.equals(e3));
		}
		
		// Mais que les autres y sont
		assertEquals(2, all.size());
	}

	@Test
	public void saveAdd() throws ParseException, IOException {
		Ecriture e5 = new Ecriture(5, df.parse("15/02/13"),
				df.parse("19/02/13"), compte2, compte3,
				new BigDecimal("-82.3"), "", "Duschmoll", null);
		Ecriture e6 = new Ecriture(6, df.parse("14/01/12"), null, compte3,
				compte1, new BigDecimal("563"), "Test", "", 65123);

		add.put(5, e5);
		add.put(6, e6);
		dao.save(add, update, remove);

		// Vérifier qu'elles y sont
		TreeSet<Ecriture> all = dao.getAll();
		assertTrue(all.contains(e5));
		assertTrue(all.contains(e6));
	}

	@Test
	public void testSaveUpdate() throws ParseException, IOException {
		Ecriture e3bis = new Ecriture(3, df.parse("15/02/13"),
				df.parse("19/02/13"), compte2, compte3,
				new BigDecimal("-82.3"), "", "Duschmoll", null);
		Ecriture e1bis = new Ecriture(1, df.parse("14/01/12"), null, compte3,
				compte1, new BigDecimal("563"), "Test", "", 65123);

		update.put(3, e3bis);
		update.put(1, e1bis);
		dao.save(add, update, remove);

		// Vérifier qu'elles y sont et que les anciennes n'y sont plus
		TreeSet<Ecriture> all = dao.getAll();
		assertTrue(all.contains(e3bis));
		assertTrue(all.contains(e1bis));
		assertFalse(all.contains(e1));
		assertFalse(all.contains(e3));
	}
}
