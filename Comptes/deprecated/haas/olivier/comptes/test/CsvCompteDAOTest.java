package haas.olivier.comptes.test;

import static org.junit.Assert.*;
import haas.olivier.comptes.Compte;
import haas.olivier.comptes.CompteBancaire;
import haas.olivier.comptes.CompteBudget;
import haas.olivier.comptes.TypeCompte;
import haas.olivier.comptes.dao.csv.CsvCompteDAO;
import java.io.BufferedReader;
import java.io.CharArrayWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class CsvCompteDAOTest {

	private static final String FILENAME = "test/testcomptes.csv";
	private static final DateFormat df = new SimpleDateFormat("dd/MM/yy");
	private static CsvCompteDAO dao;
	private Compte compte1 = new CompteBancaire(1, "C/C", 16856740L,
			TypeCompte.COMPTE_COURANT);
	private Compte compte2 = new CompteBancaire(2, "CB", 16856741L,
			TypeCompte.COMPTE_CARTE);
	private Compte compte3 = new CompteBudget(3, "Sécu", TypeCompte.RECETTES);
	private Compte compte4 = new CompteBudget(4, "Courses",
			TypeCompte.DEPENSES);
	private Set<Integer> remove;
	private Map<Integer, Compte> add;
	private Map<Integer, Compte> update;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		// Récupérer le contenu du fichier test
		BufferedReader in = new BufferedReader(new FileReader(new File(FILENAME)));
		final CharArrayWriter writer = new CharArrayWriter();
		int c;
		while ((c = in.read()) != -1) {
			writer.write(c);
		}
		in.close();
		writer.close();
		
		/* Construire l'objet à tester.
		 * On utilise une classe anonyme pour permettre d'imposer un contenu
		 * sans passer par la fabrique CsvDAO.
		 */
		dao = new CsvCompteDAO() {
			CsvCompteDAO init() {
				setContent(writer.toCharArray());
				return this;
			}// init
		}.init();
	}// setUpBeforeClass

	@Before
	public void setUp() throws Exception {

		// Définir les comptes
		compte1.setOuverture(df.parse("01/09/02"));
		compte1.setCloture(df.parse("15/08/04"));
		compte2.setCloture(df.parse("25/12/11"));

		remove = new HashSet<Integer>();
		add = new HashMap<Integer, Compte>();
		update = new HashMap<Integer, Compte>();
	}

	@Test
	public void testGetAllNoReinstantiation() throws FileNotFoundException,
			IOException {
		// Appeler la méthode deux fois
		Set<Compte> set1 = dao.getAll();
		Set<Compte> set2 = dao.getAll();
		assertEquals(set1.size(), set2.size());

		// Voir si ce sont bien les mêmes objets
		for (Compte c1 : set1) {
			boolean trouve = false;
			for (Compte c2 : set2) {
				// Comparaison par référence
				trouve = trouve || (c1 == c2);
			}
			assertTrue(trouve);
		}
	}

	@Test
	public void testSaveDelete() throws IOException {
		// Supprimer deux comptes
		remove.add(2);
		remove.add(4);
		dao.save(add, update, remove);

		// Vérifier qu'ils ne sont plus dans le DAO
		Set<Compte> all = dao.getAll();
		for (Compte c : all) {
			assertFalse(c.equals(compte2));
			assertFalse(c.equals(compte4));
		}

		// Mais que les autres y sont
		assertEquals(2, all.size());
	}

	@Test
	public void testSaveAdd() throws IOException, ParseException {
		Compte c5 = new CompteBancaire(5, "test", 56L,
				TypeCompte.COMPTE_COURANT);
		Compte c6 = new CompteBudget(6, "test4", TypeCompte.DEPENSES);
		c5.setOuverture(df.parse("09/08/12"));
		c6.setOuverture(df.parse("31/10/08"));
		add.put(5, c5);
		add.put(6, c6);
		dao.save(add, update, remove);

		// Vérifier qu'ils y sont
		Set<Compte> all = dao.getAll();
		boolean trouve5 = false, trouve6 = false;
		for (Compte c : all) {
			if (c.equals(c5)) {
				trouve5 = true;
			} else if (c.equals(c6)) {
				trouve6 = true;
			}
		}
		assertTrue(trouve5);
		assertTrue(trouve6);
	}

	@Test
	public void testSaveUpdate() throws IOException {
		Compte c1 = new CompteBancaire(1, "nonuvea", 890L,
				TypeCompte.EMPRUNT);
		Compte c3 = new CompteBudget(3, "qsdhv", TypeCompte.RECETTES);
		update.put(1, c1);
		update.put(3, c3);

		dao.save(add, update, remove);

		// Vérifier qu'ils y sont et que les anciens n'y sont plus
		Set<Compte> all = dao.getAll();
		boolean trouve1 = false, trouve3 = false, trouveOld1 = false, trouveOld3 = false;
		for (Compte c : all) {
			if (c.equals(c1)) {
				trouve1 = true;
			} else if (c.equals(c3)) {
				trouve3 = true;
			} else if (c.equals(compte1)) {
				trouveOld1 = false;
			} else if (c.equals(compte3)) {
				trouveOld3 = false;
			}
		}
		assertTrue(trouve1);
		assertTrue(trouve3);
		assertFalse(trouveOld1);
		assertFalse(trouveOld3);
	}
}
