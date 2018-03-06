package haas.olivier.comptes.test;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import haas.olivier.comptes.Compte;
import haas.olivier.comptes.CompteBancaire;
import haas.olivier.comptes.CompteBudget;
import haas.olivier.comptes.Ecriture;
import haas.olivier.util.Month;
import haas.olivier.comptes.Permanent;
import haas.olivier.comptes.TypeCompte;
import haas.olivier.comptes.dao.serialize.SerializeDAOFactory;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class SerializeDAOFactoryTest {
	private final String FILENAME = "test/serializetest.cpt";
	private final File FILE = new File(FILENAME);
	private SerializeDAOFactory serial;
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
		// Objet testé
		serial = new SerializeDAOFactory(FILE);
	}

	@After
	public void tearDown() throws Exception {
		// Nettoyer
		if (FILE.exists()) {
			FILE.delete();
		}
	}

	@Test
	public void testSerializeDAOFactory() throws IOException {
		
		// Si le fichier n'existe pas, en créer un vide
		File file = new File("non-exist");
		SerializeDAOFactory vide = new SerializeDAOFactory(file);
		vide.load();
		assertEquals(0, vide.getEcritureDAO().getAll().size());
		
		// Remettre à blanc si besoin
		if (file.exists()) {
			file.delete();
		}
	}// testSerializeDAOFactory

	@Test
	public void testMustBeSaved() throws IOException {
		assertFalse(serial.mustBeSaved());
	}// testMustBeSaved

	@Test
	public void testGetName() {
		assertTrue("Serialize".equals(serial.getName()));
	}

	@Test
	public void testGetSource() {
		assertEquals(FILE.getName(), serial.getSource());
	}

	@Test
	public void testGetSourceFullName() {
		assertTrue(FILE.getAbsolutePath().equals(serial.getSourceFullName()));
	}

	@Test
	public void testFlush() throws IOException {
		
		// Des données en vrac
		Compte c1 = new CompteBancaire(1, "un", 0L, TypeCompte.COMPTE_EPARGNE);
		Compte c2 = new CompteBudget(2, "deux", TypeCompte.DEPENSES);
		Set<Compte> comptes = new HashSet<Compte>();
		comptes.add(c1);
		comptes.add(c2);
		
		Ecriture e = new Ecriture(3, new Date(), null, c1, c2, new BigDecimal("1"), "libelle", "tiers", 45);
		TreeSet<Ecriture> ecritures = new TreeSet<Ecriture>();
		ecritures.add(e);
		
		Map<Month,BigDecimal> montants = new HashMap<Month,BigDecimal>();
		Map<Month,Integer> jours = new HashMap<Month,Integer>();
		Month mois = new Month();
		montants.put(mois, new BigDecimal("24"));
		jours.put(mois, 31);
		Permanent p = new Permanent(4, "quatre", c2, c2, jours, montants);
		Set<Permanent> permanents = new HashSet<Permanent>();
		permanents.add(p);
		
		Map<Month,Map<Integer,BigDecimal>>
		histo = new HashMap<Month,Map<Integer,BigDecimal>>(),
		soldes = new HashMap<Month,Map<Integer,BigDecimal>>(),
		moy = new HashMap<Month,Map<Integer,BigDecimal>>();
		histo.put(mois, new HashMap<Integer,BigDecimal>());
		soldes.put(mois.getNext(), new HashMap<Integer,BigDecimal>());
		moy.put(mois.getPrevious(), new HashMap<Integer,BigDecimal>());
		histo.get(mois).put(5, new BigDecimal("420"));
		soldes.get(mois.getNext()).put(6, new BigDecimal("37.4"));
		moy.get(mois.getPrevious()).put(7, new BigDecimal("-19.2"));
		
		// Insérer les données
		serial.getCompteDAO().save(comptes);
		serial.getEcritureDAO().save(ecritures);
		serial.getPermanentDAO().save(permanents);
		serial.getHistoriqueDAO().save(histo);
		serial.getSoldeAVueDAO().save(soldes);
		serial.getMoyenneDAO().save(moy);
		
		// Méthode testée
		serial.flush();
		
		// Récupérer les données
		SerializeDAOFactory serial2 = new SerializeDAOFactory(FILE);
		serial2.load();
		assertEquals(comptes, serial2.getCompteDAO().getAll());
		assertEquals(ecritures, serial2.getEcritureDAO().getAll());
		assertEquals(permanents, serial2.getPermanentDAO().getAll());
		assertEquals(histo, serial2.getHistoriqueDAO().getAll());
		assertEquals(soldes, serial2.getSoldeAVueDAO().getAll());
		assertEquals(moy, serial2.getMoyenneDAO().getAll());
	}// testFlush
}
