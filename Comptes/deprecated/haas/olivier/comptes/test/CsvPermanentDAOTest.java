package haas.olivier.comptes.test;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import haas.olivier.comptes.Compte;
import haas.olivier.comptes.CompteBancaire;
import haas.olivier.comptes.CompteBudget;
import haas.olivier.util.Month;
import haas.olivier.comptes.Permanent;
import haas.olivier.comptes.TypeCompte;
import haas.olivier.comptes.dao.CompteDAO;
import haas.olivier.comptes.dao.DAOFactory;
import haas.olivier.comptes.dao0.csv.CsvPermanentDAO;

import java.io.BufferedReader;
import java.io.CharArrayWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.math.BigDecimal;
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

public class CsvPermanentDAOTest {

	private static final String FILENAME = "test/testpermanents.csv";
	private static final DateFormat df = new SimpleDateFormat("dd/MM/yy");
	private CsvPermanentDAO dao;
	private static CompteDAO cDAO;
	private static Compte compte1, compte2, compte3;
	private static CompteBancaire compteBancaire4;
	private static Map<Month, Integer> dates1, dates3;
	private static Map<Month, BigDecimal> montants1, montants2;
	private Permanent permanent1, permanent2, permanent3, permanent4;
	private Set<Integer> remove;
	private Map<Integer, Permanent> add;
	private Map<Integer, Permanent> update;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		// Des données
		Month janvier = new Month(df.parse("01/01/13"));
		Month fevrier = new Month(df.parse("01/02/13"));
		Month mars = new Month(df.parse("01/03/13"));
		Month avril = new Month(df.parse("01/04/13"));

		dates1 = new HashMap<Month, Integer>();
		dates1.put(janvier, 5);
		dates1.put(fevrier, 17);
		dates1.put(avril, 23);

		dates3 = new HashMap<Month, Integer>();
		dates3.put(new Month(df.parse("01/12/14")), -1);

		montants1 = new HashMap<Month, BigDecimal>();
		montants1.put(janvier, new BigDecimal("45"));
		montants1.put(mars, new BigDecimal("659.39"));
		montants1.put(avril, new BigDecimal("9837.2"));

		montants2 = new HashMap<Month, BigDecimal>();
		montants2.put(new Month(df.parse("01/10/11")), new BigDecimal("-8.35"));

		// De vrais comptes avec de vrais id
		compte1 = new CompteBudget(1, "", TypeCompte.RECETTES);
		compte2 = new CompteBudget(2, "", TypeCompte.RECETTES);
		compte3 = new CompteBudget(3, "", TypeCompte.RECETTES);
		compteBancaire4 = new CompteBancaire(4, "", 0L, TypeCompte.COMPTE_COURANT);

		cDAO = mock(CompteDAO.class);
		when(cDAO.get(1)).thenReturn(compte1);
		when(cDAO.get(2)).thenReturn(compte2);
		when(cDAO.get(3)).thenReturn(compte3);
		when(cDAO.get(4)).thenReturn(compteBancaire4);

		DAOFactory factory = mock(DAOFactory.class);
		when(factory.getCompteDAO()).thenReturn(cDAO);
		DAOFactory.setFactory(factory);
	}// setUpBeforeClass

	@Before
	public void setUp() throws Exception {
		
		// Récupérer le contenu du fichier test
		Reader in = new BufferedReader(new FileReader(new File(FILENAME)));
		final CharArrayWriter writer = new CharArrayWriter();
		int c;
		while ((c = in.read()) != -1) {
			writer.write(c);
		}
		in.close();
		writer.close();
		
		/* Objet testé.
		 * On a besoin de passer par une classe anonyme pour imposer un contenu
		 * pendant le test sans passer par la fabrique CsvDAO.
		 */
		dao = new CsvPermanentDAO() {
			CsvPermanentDAO init() {
				setContent(writer.toCharArray());
				return this;
			}// init
		}.init();
		
		remove = new HashSet<Integer>();
		add = new HashMap<Integer, Permanent>();
		update = new HashMap<Integer, Permanent>();

		// Définir les permanents
		permanent1 = new Permanent(1, "un", compte1, compte2, dates1, montants1);
		permanent2 = new Permanent(2, "deux", compte2, compte3, dates1,
				montants2);
		permanent3 = new Permanent(3, "trois", compte1, compte3, dates3,
				permanent1, new BigDecimal("5"));
		permanent4 = new Permanent(4, "quatre", compteBancaire4, compte1,
				dates3);

		permanent1.libelle = "libelleun";
		permanent2.libelle = "libelledeux";
		permanent3.libelle = "libelletrois";
		permanent4.libelle = "libellequatre";

		permanent1.tiers = "tiersun";
		permanent2.tiers = "tiersdeux";
		permanent3.tiers = "tierstrois";
		permanent4.tiers = "tiersquatre";
		permanent2.pointer = true;
	}

	@Test
	public void testGetAll() throws FileNotFoundException, IOException {
		// Compliqué de comparer des Set par valeur !!
		Set<Permanent> set1 = new HashSet<Permanent>();
		set1.add(permanent1);
		set1.add(permanent2);
		set1.add(permanent3);
		set1.add(permanent4);

		Set<Permanent> set2 = dao.getAll();
		assertEquals(4, set2.size());

		for (Permanent c1 : set1) {
			boolean trouve = false;
			for (Permanent c2 : set2) {
				// Comparaison par référence pour les permanents
				trouve = trouve || (c1 == c2);
			}
			assertTrue(trouve);
		}
	}

	@Test
	public void testGetAllForceInstanciation() throws FileNotFoundException,
			IOException {
		// Même chose mais en supprimant la liste des instances
		Permanent.instances = new HashMap<Integer, Permanent>();

		Set<Permanent> set1 = new HashSet<Permanent>();
		set1.add(permanent1);
		set1.add(permanent2);
		set1.add(permanent3);
		set1.add(permanent4);

		Set<Permanent> set2 = dao.getAll();
		assertEquals(4, set2.size());

		for (Permanent c1 : set1) {
			boolean trouve = false;
			for (Permanent c2 : set2) {
				// Egalité par valeur seulement
				trouve = trouve || c1.equals(c2);
			}
			assertTrue(trouve);
		}
	}

	@Test
	public void testSaveDelete() throws IOException {
		// Supprimer deux permanents
		remove.add(2);
		remove.add(4);
		dao.save(add, update, remove);
		Permanent.instances.remove(2);
		Permanent.instances.remove(4);

		// Vérifier qu'ils n'y sont plus
		Set<Permanent> all = dao.getAll();
		assertFalse(all.contains(permanent2));
		assertFalse(all.contains(permanent4));
		
		// Mais que les autres y sont toujours
		assertEquals(2, all.size());
	}

	@Test
	public void testSaveAdd() throws IOException, ParseException {
		Permanent p5 = new Permanent(5, "cinq", compte3, compte2, dates1,
				montants2);
		Permanent p6 = new Permanent(6, "six", compte1, compte3, dates3, p5,
				new BigDecimal("3.21"));
		Permanent p7 = new Permanent(7, "sept", compteBancaire4, compte1,
				dates1);
		p5.libelle = "libellecinq";
		p6.libelle = "libellesix";
		p7.libelle = "libellesept";
		p5.tiers = "tierscinq";
		p6.tiers = "tierssix";
		p7.tiers = "tierssept";
		p6.pointer = true;
		add.put(5, p5);
		add.put(6, p6);
		add.put(7, p7);
		dao.save(add, update, remove);

		// Vérifier qu'ils y sont
		Set<Permanent> all = dao.getAll();
		assertTrue(all.contains(p5));
		assertTrue(all.contains(p6));
		assertTrue(all.contains(p7));
	}

	@Test
	public void testSaveUpdate() throws IOException {
		Permanent p1 = new Permanent(1, "unbis", compte3, compte1, dates3,
				permanent2, new BigDecimal("1"));
		Permanent p3 = new Permanent(3, "troisbis", compteBancaire4, compte2,
				dates1, montants2);
		update.put(1, p1);
		update.put(3, p3);

		dao.save(add, update, remove);

		// Vérifier qu'ils y sont et que les anciens n'y sont plus
		Set<Permanent> all = dao.getAll();
		assertTrue(all.contains(p1));
		assertTrue(all.contains(p3));
		assertFalse(all.contains(permanent1));
		assertFalse(all.contains(permanent3));
	}
}
