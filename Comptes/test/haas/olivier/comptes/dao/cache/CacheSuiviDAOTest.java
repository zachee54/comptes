package haas.olivier.comptes.dao.cache;

import static org.junit.Assert.*;

import haas.olivier.util.Month;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class CacheSuiviDAOTest {

	/**
	 * Des mois.
	 */
	private static final Month month2 = new Month(),
			month1 = month2.getPrevious(),
			month3 = month2.getNext();
	
	/**
	 * Des suivis fictifs.
	 */
	private static final Map<Month, Map<Integer, BigDecimal>> suivis =
			new HashMap<>();
	
	/**
	 * Objet testé.
	 */
	private CacheSuiviDAO dao;
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		
		// Valeurs mois 1
		Map<Integer, BigDecimal> values = new HashMap<>();
		values.put(1, BigDecimal.TEN);
		values.put(2, BigDecimal.ONE);
		suivis.put(month1, values);
		
		// Valeurs mois 2
		values = new HashMap<>();
		values.put(2, BigDecimal.TEN.negate());
		values.put(3, new BigDecimal("-895.23"));
		suivis.put(month2, values);
		
		// Valeurs mois 3
		values = new HashMap<>();
		values.put(1, BigDecimal.ZERO);
		values.put(2, new BigDecimal("200"));
		values.put(3, BigDecimal.ONE.negate());
		suivis.put(month3, values);
	}

	@Before
	public void setUp() throws Exception {
		
		// Énumération des valeurs (ordre aléatoire)
		Set<Entry<Month, Entry<Integer, BigDecimal>>> entries = new HashSet<>();
		entries.add(createEntry(month1, 1, BigDecimal.TEN));
		entries.add(createEntry(month1, 2, BigDecimal.ONE));
		entries.add(createEntry(month2, 2, BigDecimal.TEN.negate()));
		entries.add(createEntry(month2, 3, new BigDecimal("-895.23")));
		entries.add(createEntry(month3, 1, BigDecimal.ZERO));
		entries.add(createEntry(month3, 2, new BigDecimal("200")));
		entries.add(createEntry(month3, 3, BigDecimal.ONE.negate()));
		
		// Objet testé
		dao = new CacheSuiviDAO(entries.iterator());
	}

	
	/**
	 * Méthode de confort pour créer facilement des entrées sur lesquelles
	 * itérer lors de l'instanciation de l'objet testé.
	 * 
	 * @param m	Le mois.
	 * @param i	L'identifiant.
	 * @param d	Le montant.
	 * @return	Une entrée contenant <code>m</code>, <code>i</code> et
	 * 			<code>d</code>.
	 */
	private static Entry<Month, Entry<Integer, BigDecimal>> createEntry(Month m,
			Integer i, BigDecimal d) {
		return new SimpleImmutableEntry<Month, Entry<Integer, BigDecimal>>(
				m,
				new SimpleImmutableEntry<Integer, BigDecimal>(
						i,
						d));
	}

	@Test
	public void testGetAll() throws IOException {
		assertEquals(suivis, dao.getAll());
	}

	@Test
	public void testGet() {
		
		// Tester toutes les valeurs
		assertEquals(BigDecimal.TEN, dao.get(1, month1));
		assertEquals(BigDecimal.ONE, dao.get(2, month1));
		assertEquals(BigDecimal.TEN.negate(), dao.get(2, month2));
		assertEquals(new BigDecimal("-895.23"), dao.get(3, month2));
		assertEquals(BigDecimal.ZERO, dao.get(1, month3));
		assertEquals(new BigDecimal("200"), dao.get(2, month3));
		assertEquals(BigDecimal.ONE.negate(), dao.get(3, month3));
		
		// Valeurs non définies
		assertNull(dao.get(4, month1));		// Compte inexistant
		assertNull(dao.get(1, month2));		// Compte inexistant pour ce mois
		assertNull(dao.get(3,
				month1.getPrevious()));		// Mois inexistant (antérieur)
		assertNull(dao.get(3,
				month3.getNext()));			// Mois inexistant (postérieur)
	}

	@Test
	public void testSet() {
		
		// Définition d'un montant auparavant non défini
		BigDecimal b = new BigDecimal("2016");
		dao.set(1, month2, b);
		assertEquals(b, dao.get(1, month2));
		// Vérifier l'absence d'interaction
		assertEquals(BigDecimal.TEN, dao.get(1, month1));
		assertEquals(BigDecimal.ZERO, dao.get(1, month3));
		assertEquals(BigDecimal.TEN.negate(), dao.get(2, month2));
		assertEquals(new BigDecimal("-895.23"), dao.get(3, month2));
		
		// Redéfinition d'un montant existant
		b = new BigDecimal("-7.1");
		dao.set(3, month2, b);
		assertEquals(b, dao.get(3, month2));
		// Vérifier l'absence d'interaction
		assertEquals(BigDecimal.ONE.negate(), dao.get(3, month3));
		assertNull(dao.get(3, month1));
	}

	@Test
	public void testRemoveFrom() {
		dao.removeFrom(month2);						// Méthode testée
		assertNull(dao.get(1, month2));
		assertNull(dao.get(2, month2));
		assertNull(dao.get(3, month2));
		assertNull(dao.get(1, month3));
		assertNull(dao.get(2, month3));
		assertNull(dao.get(3, month3));
		assertEquals(BigDecimal.TEN, dao.get(1, month1));
		assertEquals(BigDecimal.ONE, dao.get(2, month1));
	}

	@Test
	public void testErase() throws IOException {
		dao.erase();								// Méthode testée
		
		// Vérifier qu'il n'y a plus rien dans getAll()
		for (Map<Integer, BigDecimal> map : dao.getAll().values())
			/*
			 * Normalement getAll est vide donc le code ci-dessous ne devrait
			 * même pas s'exécuter. S'il y a des entrées, il faut que les
			 * valeurs soient des Map vides.
			 */
			assertEquals(0, map.size());
	}

}
