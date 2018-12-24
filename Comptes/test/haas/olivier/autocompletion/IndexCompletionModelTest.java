/*
 * Copyright 2013-2018 Olivier HAAS. All rights reserved.
 */
package haas.olivier.autocompletion;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map.Entry;


import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class IndexCompletionModelTest {

	// Objet délégué
	@Mock private CompletionModel<Entry<Object,Integer>> delegate;	
	
	// Valeurs diverses
	@Mock private ItemFilter<Object> filter;
	private Collection<Entry<Object,Integer>> v1, v2;
	private HashMap<Object,Integer>
			map1 = new HashMap<Object,Integer>(),
			map2 = new HashMap<Object,Integer>();
	private Object[] objets;
	
	// Objet testé
	private IndexCompletionModel<Object> icm;
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
		// Valeurs en vrac
		objets = new Object[5];					// Tableau d'objets indéterminés
		for (int i=0; i<objets.length; i++) {	// Remplir brutalement
			objets[i] = new Object();
		}
		map1.put(objets[0], 1);
		map1.put(objets[1], 1);
		map1.put(objets[2], 3);
		v1 = map1.entrySet();
		
		map2.put(objets[3], 1);
		map2.put(objets[4], 2);
		v2 = map2.entrySet();
		
		// Créer les mocks
		MockitoAnnotations.initMocks(this);
		
		// Comportement des mocks
		when(delegate.getValues()).thenReturn(v1);
		
		// Objet testé
		icm = new IndexCompletionModel<Object>(delegate);
	}// setUp

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testGetItemFilter() {
		assertNotNull(icm.getItemFilter());
	}

	@Test
	public void testSetItemFilter() {
		icm.setItemFilter(filter);
		assertSame(filter, icm.getItemFilter());
	}

	@Test
	public void testGetValues() {
		// Charger les données
		icm.check();
		
		// Le modèle ne doit renvoyer que le keySet
		assertEquals(map1.keySet(), icm.getValues());
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testFilter() {
		
		// Pas d'appel au délégué
		icm.filter("coucou");
		verify(delegate, never()).filter((String) any());
		
		// Mais un appel au filtre propre
		String text = "coucou2";
		icm.setItemFilter(filter);
		icm.filter(text);
		verify(filter).filter((Collection<Object>) any(), eq(text));
	}// testFilter

	@Test
	public void testCompare() {
		// Mettre à jour les données avant de commencer
		icm.check();
		
		// Vérifier le comparateur
		assertEquals(0, icm.compare(objets[0], objets[1]));
		assertEquals(0, icm.compare(objets[1], objets[0]));
		
		assertTrue(icm.compare(objets[0], objets[2]) > 0);
		assertTrue(icm.compare(objets[2], objets[0]) < 0);
		
		assertTrue(icm.compare(objets[2], objets[1]) < 0);
		assertTrue(icm.compare(objets[1], objets[2]) > 0);
		
		// Changer de collection
		when(delegate.getValues()).thenReturn(v2);
		icm.check();	// Recharger les données
		assertTrue(icm.compare(objets[3], objets[4]) > 0);
		assertTrue(icm.compare(objets[4], objets[3]) < 0);
	}

	@Test
	public void testCheck() {
		icm.check();
		InOrder inOrder = inOrder(delegate);
		inOrder.verify(delegate).check();		// Vérification du délégué
		inOrder.verify(delegate).getValues();	// Réactualisation des données (pour l'index)
	}

	@Test
	public void testLoad() {
		icm.load();
		InOrder inOrder = inOrder(delegate);
		inOrder.verify(delegate).load();		// Rechargement des données du délégué
		inOrder.verify(delegate).getValues();	// (re)chargement des données
	}

}
