package haas.olivier.comptes.dao.cache;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

import haas.olivier.comptes.Compte;
import haas.olivier.util.Month;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class CacheSuiviDAOTest {

	private static final Month MONTH2 = new Month();
	private static final Month MONTH1 = MONTH2.getPrevious();
	private static final Month MONTH3 = MONTH2.getNext();
	
	/**
	 * Un compte mocké.
	 */
	private static final Compte COMPTE1 = mock(Compte.class);
	
	/**
	 * Un compte mocké.
	 */
	private static final Compte COMPTE2 = mock(Compte.class);
	
	/**
	 * Un compte mocké.
	 */
	private static final Compte COMPTE3 = mock(Compte.class);
	
	/**
	 * Des suivis fictifs.
	 */
	private static final Set<Solde> SUIVIS = new HashSet<>();
	
	/**
	 * Objet testé.
	 */
	private CacheSuiviDAO dao;
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		SUIVIS.add(new Solde(MONTH1, COMPTE1, BigDecimal.TEN));
		SUIVIS.add(new Solde(MONTH1, COMPTE2, BigDecimal.ONE));
		SUIVIS.add(new Solde(MONTH2, COMPTE2, BigDecimal.TEN.negate()));
		SUIVIS.add(new Solde(MONTH2, COMPTE3, new BigDecimal("-895.23")));
		SUIVIS.add(new Solde(MONTH3, COMPTE1, BigDecimal.ZERO));
		SUIVIS.add(new Solde(MONTH3, COMPTE2, new BigDecimal("200")));
		SUIVIS.add(new Solde(MONTH3, COMPTE3, BigDecimal.ONE.negate()));
	}

	@Before
	public void setUp() throws Exception {
		
		// Énumération des valeurs (ordre aléatoire)
		Set<Solde> entries = new HashSet<>();
		entries.add(new Solde(MONTH1, COMPTE1, BigDecimal.TEN));
		entries.add(new Solde(MONTH1, COMPTE2, BigDecimal.ONE));
		entries.add(new Solde(MONTH2, COMPTE2, BigDecimal.TEN.negate()));
		entries.add(new Solde(MONTH2, COMPTE3, new BigDecimal("-895.23")));
		entries.add(new Solde(MONTH3, COMPTE1, BigDecimal.ZERO));
		entries.add(new Solde(MONTH3, COMPTE2, new BigDecimal("200")));
		entries.add(new Solde(MONTH3, COMPTE3, BigDecimal.ONE.negate()));
		
		// Objet testé
		dao = new CacheSuiviDAO(entries.iterator());
	}

	@Test
	public void testGetAll() throws IOException {
		Iterator<Solde> suivisIterator = dao.getAll();
		Set<Solde> daoSuivis = new HashSet<>();
		while (suivisIterator.hasNext())
			daoSuivis.add(suivisIterator.next());
		
		assertEquals(SUIVIS, daoSuivis);
	}

	@Test
	public void testGet() {
		
		// Tester toutes les valeurs
		assertEquals(BigDecimal.TEN, dao.get(COMPTE1, MONTH1));
		assertEquals(BigDecimal.ONE, dao.get(COMPTE2, MONTH1));
		assertEquals(BigDecimal.TEN.negate(), dao.get(COMPTE2, MONTH2));
		assertEquals(new BigDecimal("-895.23"), dao.get(COMPTE3, MONTH2));
		assertEquals(BigDecimal.ZERO, dao.get(COMPTE1, MONTH3));
		assertEquals(new BigDecimal("200"), dao.get(COMPTE2, MONTH3));
		assertEquals(BigDecimal.ONE.negate(), dao.get(COMPTE3, MONTH3));
		
		// Valeurs non définies
		assertNull(dao.get(
				mock(Compte.class),			// Compte inexistant
				MONTH1));
		assertNull(dao.get(COMPTE1,
				MONTH2));					// Compte inexistant pour ce mois
		assertNull(dao.get(COMPTE3,
				MONTH1.getPrevious()));		// Mois inexistant (antérieur)
		assertNull(dao.get(COMPTE3,
				MONTH3.getNext()));			// Mois inexistant (postérieur)
	}

	@Test
	public void testSet() {
		
		// Définition d'un montant auparavant non défini
		BigDecimal b = new BigDecimal("2016");
		dao.set(COMPTE1, MONTH2, b);
		assertEquals(b, dao.get(COMPTE1, MONTH2));
		
		// Vérifier l'absence d'interaction
		assertEquals(BigDecimal.TEN, dao.get(COMPTE1, MONTH1));
		assertEquals(BigDecimal.ZERO, dao.get(COMPTE1, MONTH3));
		assertEquals(BigDecimal.TEN.negate(), dao.get(COMPTE2, MONTH2));
		assertEquals(new BigDecimal("-895.23"), dao.get(COMPTE3, MONTH2));
		
		// Redéfinition d'un montant existant
		b = new BigDecimal("-7.1");
		dao.set(COMPTE3, MONTH2, b);
		assertEquals(b, dao.get(COMPTE3, MONTH2));
		
		// Vérifier l'absence d'interaction
		assertEquals(BigDecimal.ONE.negate(), dao.get(COMPTE3, MONTH3));
		assertNull(dao.get(COMPTE3, MONTH1));
	}

	@Test
	public void testRemoveFrom() {
		dao.removeFrom(MONTH2);						// Méthode testée
		assertNull(dao.get(COMPTE1, MONTH2));
		assertNull(dao.get(COMPTE2, MONTH2));
		assertNull(dao.get(COMPTE3, MONTH2));
		assertNull(dao.get(COMPTE1, MONTH3));
		assertNull(dao.get(COMPTE2, MONTH3));
		assertNull(dao.get(COMPTE3, MONTH3));
		assertEquals(BigDecimal.TEN, dao.get(COMPTE1, MONTH1));
		assertEquals(BigDecimal.ONE, dao.get(COMPTE2, MONTH1));
	}

	@Test
	public void testErase() throws IOException {
		dao.erase();								// Méthode testée
		assertFalse(dao.getAll().hasNext());
	}

}
