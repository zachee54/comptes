/*
 * Copyright 2013-2018 Olivier HAAS. All rights reserved.
 */
package haas.olivier.diagram;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.Iterator;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class SimpleDiagramModelTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	/** Des valeurs quelconques pour servir d'étiquettes des abscisses. */
	private final Object a = new Object(),
			b = new Object(),
			c = new Object();
	
	/** L'objet testé. */
	private final SimpleDiagramModel model =
			new SimpleDiagramModel(new Object[] {a, b, c});
	
	/** Des séries mockées. */
	private final Serie  serie1 = mock(Serie.class), serie2 = mock(Serie.class);
	
	/** Un mock observateur de l'objet testé. */
	private final DiagramModelObserver observer =
			mock(DiagramModelObserver.class);
	
	@Before
	public void setUp() throws Exception {
		model.add(serie1);
		model.add(serie2);
		model.getObservable().addObserver(observer);
	}// setUp

	@After
	public void tearDown() throws Exception {
	}
	
	@Test
	public void testGetSeries() {
		Iterator<Serie> it = model.getSeries().iterator();
		assertTrue(it.hasNext());
		assertSame(serie1, it.next());
		assertTrue(it.hasNext());
		assertSame(serie2, it.next());
		assertFalse(it.hasNext());
	}// testGetSeries

	@Test
	public void testRemove() {
		model.remove(serie1);
		Iterator<Serie> it = model.getSeries().iterator();
		assertTrue(it.hasNext());
		assertSame(serie2, it.next());
		assertFalse(it.hasNext());
		
		verify(observer).diagramChanged();
	}// testRemove
	
	/** Vérifie l'absence d'exception si on supprime une série inexistante. */
	@Test
	public void testRemoveInexistent() {
		model.remove(mock(Serie.class));
	}// testRemoveInexistent

	@Test
	public void testSetXValues() {
		Object[] objects = {new Object(), new Object()};
		model.setXValues(objects);
		assertEquals((Object) objects, model.getXValues());
		
		verify(observer).diagramChanged();
	}// setXValues

	@Test
	public void testGetXValues() {
		Object[] xValues = model.getXValues();
		assertEquals(3, xValues.length);
		assertSame(a, xValues[0]);
		assertSame(b, xValues[1]);
		assertSame(c, xValues[2]);
	}// testGetXValues

	@Test
	public void testGetOrdener() {
		assertEquals(2, model.getOrdener().getSeriesCount());
	}// testGetOrdener

}
