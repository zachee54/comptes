package haas.olivier.comptes.dao.cache;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Collection;
import haas.olivier.comptes.Banque;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class CacheBanqueDAOTest {

//	/** Une image vide. */
//	private static final BufferedImage image =
//			new BufferedImage(1, 1, BufferedImage.TYPE_3BYTE_BGR);
	
	/** Des banques. */
	private static Banque banque1, banque2;
	
	/** Objet testé. */
	private CacheBanqueDAO dao;
	
	private static byte[] bytes1, bytes2;
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		
		// Générer des données binaires insignifiantes
		bytes1 = new byte[256];
		for (int i=0; i<bytes1.length; i++)
			bytes1[i] = (byte) i;
		
		bytes2 = new byte[512];
		for (int i=0; i<256; i++) {
			bytes2[2*i] = (byte) i;
			bytes2[2*i+1] = (byte) -i;
		}// for
		
		// Instancier des objets banques
		banque1 = new Banque(1, "un", bytes1);
		banque2 = new Banque(2, "deux", bytes2);
	}// setUpBeforeClass

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
		
		// Préparer l'itérateur de banques censé provenir de la sous-couche
		Collection<Banque> banques = new ArrayList<Banque>();
		banques.add(banque1);
		banques.add(banque2);
		
		// Instancier l'objet testé
		dao = new CacheBanqueDAO(banques.iterator());
	}// setUp

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testGetAll() {
		
		// Collecter le résultat
		Collection<Banque> banques = new ArrayList<Banque>();
		for (Banque banque : dao.getAll())
			banques.add(banque);
		
		// Vérifier le contenu
		assertEquals(2, banques.size());
		assertTrue(banques.contains(banque1));
		assertTrue(banques.contains(banque2));
	}// testGetAll

	@Test
	public void testGet() {
		
		// Renvoie les bonnes instances
		assertSame(banque1, dao.get(1));
		assertSame(banque2, dao.get(2));
		
		// Comportement en cas d'argument incohérent
		assertNull(dao.get(0));
		assertNull(dao.get(3));
	}// testGet

	@Test
	public void testAdd() {
		
		// Ajouter une banque avec identifiant
		Banque banque4 = new Banque(4, "quatre", bytes1);	// Nouvelle banque
		dao.add(banque4);									// Méthode testée
		assertSame(banque4, dao.get(4));					// Test
		
		// Ajouter une banque sans identifiant
		Banque banque5 = new Banque(null, "banque5", bytes2);
		dao.add(banque5);
		assertEquals(banque5.nom, dao.get(5).nom);			// La même banque
		assertEquals(5, (int) dao.get(5).id);				// Avec id == 5
		
		// Vérifier que cela n'a pas modifié le reste du comportement
		assertSame(banque1, dao.get(1));
		assertSame(banque4, dao.get(4));
	}// testAdd

	@Test
	public void testErase() {
		dao.erase();										// Méthode testée
		
		// Vérifier que les données ont été effacées
		assertNull(dao.get(1));
		assertNull(dao.get(2));
	}// testErase

	@Test
	public void testMustBeSaved() {
		
		// Au départ
		assertFalse(dao.mustBeSaved());
		
		// Après simple consultation
		dao.get(0);
		dao.get(1);
		assertFalse(dao.mustBeSaved());
		
		// Après modification
		dao.add(new Banque(3, "trois", bytes2));
		assertTrue(dao.mustBeSaved());
		
		// Marquer comme sauvegardé
		dao.setSaved();
		assertFalse(dao.mustBeSaved());
	}// testMustBeSaved
}
