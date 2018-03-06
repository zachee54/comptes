package haas.olivier.comptes.dao.csv;

import static org.junit.Assert.*;

import haas.olivier.util.Month;
import haas.olivier.comptes.dao.csv.CsvSuiviDAO;

import java.io.BufferedReader;
import java.io.CharArrayWriter;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class CsvSuiviDAOTest {

	private static final String HISTOFILENAME = "test/testhisto.csv";
	private static char[] testContent;	// Contenu du fichier test
	private static final DateFormat df = new SimpleDateFormat("dd/MM/yy");
	private static Month month1, month2, month3, month4;
	private static BigDecimal montant, montant2;
	private Map<Month, Map<Integer, BigDecimal>> map;
	private CsvSuiviDAO dao;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		month1 = new Month(df.parse("01/11/12"));
		month2 = new Month(df.parse("01/12/12"));
		month3 = new Month(df.parse("01/01/13"));
		month4 = new Month(df.parse("01/03/13"));
		montant = new BigDecimal("85871.53");
		montant2 = new BigDecimal("624");
		
		// Contenu du fichier test
		Reader in = new BufferedReader(new FileReader(new File(HISTOFILENAME)));
		CharArrayWriter writer = new CharArrayWriter();
		int c;
		while ((c = in.read()) != -1) {
			writer.write(c);
		}
		in.close();
		writer.close();
		testContent = writer.toCharArray();
	}// setUpBeforeClass

	@Before
	public void setUp() throws Exception {
		/* On a besoin de passer par une classe anonyme pour imposer un contenu
		 * pendant le test sans passer par la fabrique CsvDAO.
		 */
		dao = new CsvSuiviDAO() {
			CsvSuiviDAO init() {
				setContent(testContent);
				return this;
			}// init
		}.init();
		map = new HashMap<Month, Map<Integer, BigDecimal>>();
	}// setUp

	@Test
	public void testSaveDelete() throws IOException {
		// Supprimer depuis month2
		dao.save(month2, map);
		
		// Tester la suppression de month2 et month3, mais pas month1
		Map<Month,Map<Integer,BigDecimal>> all = dao.getAll();
		assertTrue(all.containsKey(month1));
		assertFalse(all.containsKey(month2));
		assertFalse(all.containsKey(month3));
	}

	@Test
	public void testSaveAddMonth() throws IOException {
		// Tester l'ajout d'un mois
		map.put(month4, new HashMap<Integer, BigDecimal>());
		map.get(month4).put(1, montant);
		dao.save(month4.getNext(), map);
		Map<Month,Map<Integer,BigDecimal>> all = dao.getAll();
		
		// Tester la nouvelle valeur
		assertEquals(0, montant.compareTo(all.get(month4).get(1)));

		// Tester l'absence de changement sur les autres mois
		assertEquals(0, new BigDecimal("-666.5").compareTo(all.get(month1).get(2)));
		assertEquals(0, new BigDecimal("463.26").compareTo(all.get(month2).get(2)));
		assertEquals(0, new BigDecimal("-214.11").compareTo(all.get(month3).get(1)));
	}

	@Test
	public void testSaveAddCompte() throws IOException {
		// Tester l'ajout d'un compte
		map.put(month2, new HashMap<Integer, BigDecimal>());
		map.get(month2).put(3, montant);
		dao.save(month2, map);
		Map<Month,Map<Integer,BigDecimal>> all = dao.getAll();

		// Tester la nouvelle valeur
		assertEquals(0, montant.compareTo(all.get(month2).get(3)));
		
		// Tester l'effacement des données du mois et des mois suivants
		assertNull(all.get(month2).get(2));
		assertNull(all.get(month3));
		
		// Tester l'absence de changement sur les autres comptes
		assertEquals(0, new BigDecimal("845.03").compareTo(all.get(month1).get(1)));
	}

	@Test
	public void testSaveAddMonthAndCompte1() throws IOException {
		// Tester l'ajout d'un compte et d'un mois
		map.put(month3, new HashMap<Integer, BigDecimal>());
		map.get(month3).put(3, montant);
		map.put(month4, new HashMap<Integer, BigDecimal>());
		map.get(month4).put(2, montant2);
		dao.save(month3, map);
		Map<Month,Map<Integer,BigDecimal>> all = dao.getAll();

		assertEquals(0, montant.compareTo(all.get(month3).get(3)));
		assertEquals(0, montant2.compareTo(all.get(month4).get(2)));
	}

	@Test
	public void testSaveAddMonthAndCompte2() throws IOException {
		// Tester l'ajout d'un compte sur un nouveau mois
		map.put(month4, new HashMap<Integer, BigDecimal>());
		map.get(month4).put(3, montant);
		dao.save(month4, map);
		Map<Month,Map<Integer,BigDecimal>> all = dao.getAll();

		assertEquals(0, montant.compareTo(all.get(month4).get(3)));
	}

	@Test
	public void testSaveTwoLines() throws IOException {
		// Changer deux lignes
		map.put(month2, new HashMap<Integer, BigDecimal>());
		map.get(month2).put(2, montant);
		map.put(month3, new HashMap<Integer, BigDecimal>());
		map.get(month3).put(1, montant2);
		dao.save(month2, map);
		Map<Month,Map<Integer,BigDecimal>> all = dao.getAll();

		assertEquals(0, montant.compareTo(all.get(month2).get(2)));
		assertEquals(0, montant2.compareTo(all.get(month3).get(1)));
		assertNull(all.get(month2).get(1));
		assertNull(all.get(month3).get(2));
	}
}
