package haas.olivier.comptes.dao.cache;

import static org.junit.Assert.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;

import haas.olivier.comptes.Compte;
import haas.olivier.comptes.Ecriture;
import haas.olivier.comptes.EcritureMissingArgumentException;
import haas.olivier.comptes.InconsistentArgumentsException;
import haas.olivier.comptes.TypeCompte;
import haas.olivier.util.Month;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class CacheEcritureDAOTest {

	/**
	 * Des comptes.
	 */
	private static final Compte c1 =
			new CompteBancaire(1, "compte1", 0L, TypeCompte.COMPTE_COURANT),
			c2 = new CompteBudget(2, "compte2", TypeCompte.DEPENSES);
	
	/**
	 * Des dates.
	 */
	private static Date date1, date2, date3;
	
	/**
	 * Des mois correspondant aux dates.
	 */
	private static Month month1, month2, month3;
	
	/**
	 * Des écritures.
	 */
	private static Ecriture e1, e1bis, e2, e3;
	
	/**
	 * Objet testé.
	 */
	private CacheEcritureDAO dao;
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		
		// Obtenir la date du 1er jour du mois, sans l'heure
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.DAY_OF_MONTH, 1);
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		date3 = cal.getTime();
		
		// Une date dans le passé
		cal.add(Calendar.MONTH, -2);
		date2 = cal.getTime();
		
		// Une date encore avant
		cal.add(Calendar.MONTH, -1);
		date1 = cal.getTime();
		
		// Les mois
		month1 = new Month(date1);		// Mois de la date 1
		month2 = new Month(date2);		// Mois de la date 2
		month3 = new Month(date3);		// Mois de la date 3
		
		// Définition des écritures utilisées
		e1 = new Ecriture(1, date1, date2, c1, c2, BigDecimal.ONE, "libelle1", "tiers1", null);
		e1bis = new Ecriture(0, date1, null, c1, c2, BigDecimal.ONE, "libelle1", "tiers1", null);
		e2 = new Ecriture(2, date2, date3, c2, c1, BigDecimal.TEN, "libelle2", "tiers2", 3);
		e3 = new Ecriture(3, date3, null, c1, c2, BigDecimal.TEN, "libelle3", "tiers3", null);
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
		
		// Préparer l'itérateur censé provenir de la sous-couche
		Collection<Ecriture> ecritures = new ArrayList<>();
		ecritures.add(e1);
		ecritures.add(e1bis);
		ecritures.add(e2);
		ecritures.add(e3);
		
		// Instancier l'objet testé
		dao = new CacheEcritureDAO(ecritures.iterator());
	}

	@After
	public void tearDown() throws Exception {
	}

	/** 
	 * Vérifie que l'itérable spécifié contient exactement les écritures
	 * spécifiées, dans cet ordre.
	 * 
	 * @param result	Le résultat.
	 * @param expected	Les écritures attendues.
	 */
	private void check(Iterable<Ecriture> result, Ecriture... expected) {
		
		// Itérateur du résultat
		Iterator<Ecriture> it = result.iterator();
		
		// Vérifier le contenu
		for (int i=0; i<expected.length; i++) {		// Chaque valeur attendue
			assertTrue(it.hasNext());				// Il y a une valeur
			assertSame(expected[i], it.next());		// Et c'est la bonne
		}// for
		
		// Vérifier qu'il n'y a rien de plus
		assertFalse(it.hasNext());
	}
	
	@Test
	public void testGet() {
		assertSame(e1bis, dao.get(0));
		assertSame(e1, dao.get(1));
		assertSame(e2, dao.get(2));
		assertSame(e3, dao.get(3));
		assertNull(dao.get(4));
	}
	
	@Test
	public void testGetAll() {
		check(dao.getAll(), e3, e2, e1bis, e1);
	}

	@Test
	public void testGetAllBetween() {
		
		// Vérifier qu'on a toutes les écritures entre deux bornes très larges
		check(dao.getAllBetween(month1.getPrevious(), month3.getNext()),
				e3, e2, e1bis, e1);
		
		// Vérifier la borne inférieure
		check(dao.getAllBetween(month2, month3.getNext()),
				e3, e2);
		
		// Vérifier la borne supérieure
		check(dao.getAllBetween(month1.getPrevious(), month2),
				e2, e1bis, e1);
	}

	@Test
	public void testGetAllSince() {
		check(dao.getAllSince(month2),
				e2, e3);
	}

	@Test
	public void testGetPointagesSince() {
		check(dao.getPointagesSince(month2),
				e1, e2, e1bis, e3);
		check(dao.getPointagesSince(month3),
				e2, e1bis, e3);
	}

	@Test
	public void testGetAllTo() {
		check(dao.getAllTo(month2),
				e2, e1bis, e1);
	}

	@Test
	public void testGetPointagesTo() {
		
		// Pointages jusqu'au mois en cours : cela inclut les non pointées
		check(dao.getPointagesTo(month3),
				e3, e1bis, e2, e1);
		
		// Pointages jusqu'à un mois passé : pas les non pointées,ni pointées après
		check(dao.getPointagesTo(month2),
				e1);
	}

	@Test
	public void testAdd() throws EcritureMissingArgumentException, InconsistentArgumentsException {
		
		// Ajouter une écriture avec identifiant
		Ecriture e5 = new Ecriture(5, date2, date3, c2, c1, BigDecimal.TEN, "libellé5", "tiers5", 9);
		dao.add(e5);
		
		// Ajouter une écriture dans identifiant
		Ecriture e6 = new Ecriture(null, date3, null, c1, c2, BigDecimal.TEN, "libellé6", "tiers6", null);
		dao.add(e6);
		
		// Récupérer toutes les écritures
		Iterator<Ecriture> it = dao.getAll().iterator();
		Collection<Ecriture> all = new ArrayList<>();
		while (it.hasNext())
			all.add(it.next());
		
		// Vérifier qu'on a deux écritures de plus qu'avant
		assertEquals(6, all.size());
		
		// Vérifier que e5 est dedans
		assertTrue(all.contains(e5));
		
		// Vérifier qu'on a numéroté une écriture 6
		for (Ecriture e : all) {
			if (e.id.equals(6)) {
				return;			// Tout le test est fini
			}
		}
		fail("Ecriture ajoutée non numérotée 6");
	}

	@Test
	public void testRemove() {
		dao.remove(2);
		check(dao.getAll(), e3, e1bis, e1);
	}

	@Test
	public void testUpdate() throws EcritureMissingArgumentException, InconsistentArgumentsException {
		
		// Une écriture numérotée 2 mais chronologiquement avant les autres
		Ecriture e2bis = new Ecriture(2, date1, date1, c2, c1, BigDecimal.TEN, "libelle2", "tiers2", null);
		dao.update(e2bis);								// Méthode testée
		check(dao.getAll(), e3, e1bis, e1, e2bis);		// A remplacé e2
	}

	@Test
	public void testErase() {
		dao.erase();
		assertFalse(dao.getAll().iterator().hasNext());	// Plus rien
	}// testErase

	@Test
	public void testConstructCommentIndex() {
		
		// Méthode testée
		Map<String, Integer> map = dao.constructCommentIndex();
		
		// Vérifier chaque chaîne
		assertEquals(2, (int) map.get(e1.libelle));
		assertEquals(2, (int) map.get(e1.tiers));
		assertEquals(1, (int) map.get(e2.libelle));
		assertEquals(1, (int) map.get(e2.tiers));
		assertEquals(1, (int) map.get(e3.libelle));
		assertEquals(1, (int) map.get(e3.tiers));
	}

	@Test
	public void testMustBeSaved() throws EcritureMissingArgumentException, InconsistentArgumentsException {
		
		// Au départ
		assertFalse(dao.mustBeSaved());
		
		// Après simple consultation
		dao.getAll();
		dao.getAllBetween(month1, month3);
		dao.getAllSince(month2);
		dao.getAllTo(month3);
		dao.getPointagesSince(month1);
		dao.getPointagesTo(month1);
		assertFalse(dao.mustBeSaved());
		
		// Après modification
		dao.add(new Ecriture(null, date3, null, c1, c2, BigDecimal.ONE, null, null, null));
		assertTrue(dao.mustBeSaved());
		
		// Simuler une sauvegarde
		dao.setSaved();
		assertFalse(dao.mustBeSaved());
		
		// Après mise à jour
		dao.update(new Ecriture(3, date2, null, c1, c2, BigDecimal.ONE, null, null, null));
		assertTrue(dao.mustBeSaved());
		
		// Après suppression
		dao.setSaved();					// Remise à "blanc"
		assertFalse(dao.mustBeSaved());	// Vérifier la remise à blanc
		dao.remove(3);
		assertTrue(dao.mustBeSaved());
	}
}
