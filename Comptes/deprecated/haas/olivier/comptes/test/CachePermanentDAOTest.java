package haas.olivier.comptes.test;

import static org.junit.Assert.*;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

import haas.olivier.comptes.Compte;
import haas.olivier.comptes.CompteBancaire;
import haas.olivier.util.Month;
import haas.olivier.comptes.Permanent;
import haas.olivier.comptes.dao.cache.CachePermanentDAO;
import haas.olivier.comptes.dao.cache.CacheablePermanentDAO;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Map;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class CachePermanentDAOTest {

	private HashSet<Permanent> permanents = new HashSet<Permanent>();
	private Permanent p1, p2, p2b, p3;
	@Mock private Compte c1, c2;
	@Mock private CompteBancaire cb = mock(CompteBancaire.class);
	@Mock private CacheablePermanentDAO subdao ;
	@Mock private Map<Month,Integer> jours;
	@Mock private Map<Month,BigDecimal> montants;
	private CachePermanentDAO dao;
	private HashSet<Permanent> permanents2 = new HashSet<Permanent>();
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		
		// La collection des Permanents
		p1 = new Permanent(1, "un", c1, c2, jours, montants);
		p2 = new Permanent(2, "deux", c1, c2, jours, p1, new BigDecimal("1"));
		p2b = new Permanent(2, "deux bis", c2, cb, jours, montants);
		p3 = new Permanent(3, "trois", cb, c1, jours);
		permanents.add(p1);
		permanents.add(p2);
		
		// Comportement du Mock
		when(subdao.getAll()).thenReturn(permanents);
		
		// Objet testé
		dao = new CachePermanentDAO(subdao);
	}// setUp

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testGetAll() throws IOException {
		assertEquals(permanents, dao.getAll());
	}

	@Test
	public void testAdd() throws IOException {
		dao.add(p3);
		assertSame(p3, dao.get(3));
	}

	@Test
	public void testUpdate() throws IOException {
		dao.update(p2b);
		assertSame(p2b, dao.get(2));
	}

	@Test
	public void testRemove() throws IOException {
		dao.remove(1);
		try {
			dao.get(1);
			fail("Doit lever une exception");
		} catch (IOException e) {
		}
	}// testRemove

	@Test
	public void testGet() throws IOException {
		assertSame(p1, dao.get(1));
		assertSame(p2, dao.get(2));
		try {
			dao.get(3);
			fail("Doit lever une exception");
		} catch (IOException e) {
		}
	}// testGet

	@Test
	public void testSave() throws IOException {
		dao.save();
		verify(subdao).save(eq(permanents));
	}// testSave

	@Test
	public void testSaveAdd() throws IOException {
		dao.add(p3);
		dao.save();
		
		// Nouvelle collection
		permanents2.addAll(permanents);
		permanents2.add(p3);
		
		// Vérifier que le résultat concorde
		verify(subdao).save(eq(permanents2));
	}// testSaveAdd
	
	@Test
	public void testSaveUpdate() throws IOException {
		dao.update(p2b);
		dao.save();
		
		// Nouvelle collection
		permanents2.add(p1);
		permanents2.add(p2b);
		
		verify(subdao).save(eq(permanents2));
	}// testSaveUpdate
	
	@Test
	public void testSaveRemove() throws Exception {
		dao.remove(1);
		dao.save();
		
		// Nouvelle collection
		permanents2.add(p2);
		
		verify(subdao).save(eq(permanents2));
	}// testSaveRemove
	
	@Test
	public void testSaveAllInOne() throws Exception {
		dao.remove(1);
		dao.update(p2b);
		dao.add(p3);
		dao.save();
		
		// Nouvelle collection
		permanents2.add(p2b);
		permanents2.add(p3);
		
		verify(subdao).save(eq(permanents2));
	}// testSaveAllInOne
}
