package haas.olivier.comptes.test;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import haas.olivier.comptes.Compte;
import haas.olivier.comptes.dao.util.AbstractCache;

import java.io.IOException;
import java.util.HashSet;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class AbstractCacheTest {

	private AbstractCache<Cloneable> cache;
	@Mock private MyTest test;// N'importe quelle classe fait l'affaire
	@Mock private HashSet<Compte> donnees;
	
	private class MyTest implements Cloneable {
		public Cloneable get() {
			return null;
		}
	}
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		
		// Comportement du Mock
		when(test.get()).thenReturn(donnees);
		
		// Instancier une implémentation bidon
		cache = new AbstractCache<Cloneable>() {
			@Override
			protected Cloneable load() throws IOException {
				return test.get();	// Des données clonables mockées
			}

			@Override
			protected void initMaxId() throws IOException {
				maxId = 50;
			}

			@Override
			public Cloneable getCloneCache() throws IOException {
				return null;
			}
		};// classe anonyme AbstractCache
	}// setUp

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testGetCache() throws IOException {
		// Générer des appels concurrents
		for (int i = 0; i < 1000 ; i++) {
			new Thread() {
				@Override public void run() {
					try {
						cache.getCache();
					} catch (IOException e) {
					}// try
				}// run
			}.run();// Thread anonyme
		}// for
		
		// Vérifier que les données n'ont été chargées qu'une fois
		verify(test, times(1)).get();
		
		// Vérifier le résultat
		assertSame(donnees, cache.getCache());
	}

	@Test
	public void testClear() throws IOException {
		// 1er appel
		cache.getCache();
		verify(test).get();
		
		// Méthode testée
		cache.clear();
		
		// 2ème appel
		assertSame(donnees, cache.getCache());	// Bonnes données
		verify(test, times(2)).get();		// 2ème chargement (objet du test)
	}

	@Test
	public void testGetNextId() throws IOException {
		assertEquals(51, cache.getNextId());	// n+1
		assertEquals(52, cache.getNextId());	// n+2
	}

}
