package haas.olivier.comptes.dao.cache.hibernate;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Iterator;
import haas.olivier.comptes.Compte;
import haas.olivier.comptes.TypeCompte;
import haas.olivier.comptes.dao.CompteDAO;
import haas.olivier.comptes.dao.cache.CacheDAOFactory;
import haas.olivier.comptes.dao.cache.CacheableDAOFactory;
import haas.olivier.comptes.dao.cache.hibernate.HibernateCacheableDAO;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class HibernateCacheableDAOTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	/**
	 * Objet testé.
	 */
	private CacheableDAOFactory factory;
	
	@Before
	public void setUp() throws Exception {
		factory = new HibernateCacheableDAO(
				"jdbc:hsqldb:mem:test", "org.hsqldb.jdbc.JDBCDriver");
	}

	@After
	public void tearDown() throws Exception {
		factory.close();
	}
	
	@Test
	public void testGetComptes() throws IOException {
		assertFalse(factory.getComptes().hasNext());
	}
	
	@Test
	public void testSave() throws IOException {
		Compte compte1 = new Compte(0, TypeCompte.COMPTE_CARTE);
		
		CompteDAO cacheCompteDAO = mock(CompteDAO.class);
		when(cacheCompteDAO.getAll()).thenReturn(Collections.singleton(compte1));
		
		CacheDAOFactory cacheDAO = mock(CacheDAOFactory.class);
		when(cacheDAO.getCompteDAO()).thenReturn(cacheCompteDAO);
		
		// Méthode testée (1ère passe)
		factory.save(cacheDAO);
		
		Iterator<Compte> iterator = factory.getComptes();
		assertTrue(iterator.hasNext());
		assertSame(compte1, iterator.next());
		assertFalse(iterator.hasNext());
		
		// 2ème phase
		
		compte1.setNom("mon compte 1");
		
		Compte compte2 = new Compte(0, TypeCompte.RECETTES);
		when(cacheCompteDAO.getAll()).thenReturn(
				Arrays.asList(new Compte[] {compte1, compte2}));
		
		// Méthode testée (2ème passe avec 1 nouveau compte + 1 modifié)
		factory.save(cacheDAO);
		
		IdentityHashMap<Compte, Void> result = new IdentityHashMap<>();
		iterator = factory.getComptes();
		while (iterator.hasNext()) {
			result.put(iterator.next(), null);
		}
		assertTrue(result.containsKey(compte1));
		assertTrue(result.containsKey(compte2));
	}

}
