package haas.olivier.diagram;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.Iterator;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class SeriesOrdenerTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}
	
	/** L'objet testé. */
	private final SeriesOrdener ordener = new SeriesOrdener();
	
	/** Des séries mockées. */
	private final Serie serie1 = mock(Serie.class), serie2 = mock(Serie.class);
	
	/** Un mock d'observateur de modèle de diagramme. */
	private final DiagramModelObserver observer =
			mock(DiagramModelObserver.class);

	@Before
	public void setUp() throws Exception {
		ordener.add(serie1);
		ordener.add(serie2);
		ordener.getObservable().addObserver(observer);
	}// setUp

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testGetSeriesCount() {
		
		// Tester une instance à vide
		assertEquals(0, new SeriesOrdener().getSeriesCount());
		
		// Tester l'instance contenant des séries
		assertEquals(2, ordener.getSeriesCount());
		ordener.remove(serie1);
		assertEquals(1, ordener.getSeriesCount());
	}// testGetSeriesCount

	@Test
	public void testGetSerieAt() {
		assertSame(serie1, ordener.getSerieAt(0));
		assertSame(serie2, ordener.getSerieAt(1));
		
		ordener.remove(serie1);
		verify(observer).diagramChanged();
		assertSame(serie2, ordener.getSerieAt(0));
		try {
			ordener.getSerieAt(1);
			fail("Doit lever une exception");
		} catch (Exception e) {
		}// try
	}// testGetSerieAt

	@Test
	public void testMoveBack() {
		ordener.moveBack(0);
		assertSame(serie2, ordener.getSerieAt(0));
		assertSame(serie1, ordener.getSerieAt(1));
		verify(observer).diagramChanged();
		
		// Ne doit pas lever d'exception
		ordener.moveBack(1);
	}// testMoveBack

	@Test
	public void testMoveForward() {
		ordener.moveForward(1);
		assertSame(serie2, ordener.getSerieAt(0));
		assertSame(serie1, ordener.getSerieAt(1));
		verify(observer).diagramChanged();
		
		// Ne doit pas lever d'exception
		ordener.moveForward(0);
	}// testMoveForward

	/** Vérifie l'absence d'exception si on essaye de supprimer une série
	 * inexistante.
	 */
	@Test
	public void testRemove() {
		ordener.remove(mock(Serie.class));
	}// testRemove
	
	@Test
	public void testIsHidden() {
		assertFalse(ordener.isHidden(serie1));
		assertFalse(ordener.isHidden(serie2));
	}// testIsHidden

	@Test
	public void testSetHidden() {
		
		// Masquer une série
		ordener.setHidden(serie1, true);
		verify(observer).diagramChanged();
		assertTrue(ordener.isHidden(serie1));
		assertFalse(ordener.isHidden(serie2));
		
		// Masquer une série déjà masquée
		ordener.setHidden(serie1, true);
		assertTrue(ordener.isHidden(serie1));
		
		// Réafficher une série
		ordener.setHidden(serie1, false);
		verify(observer, atLeast(1)).diagramChanged();
		assertFalse(ordener.isHidden(serie1));
		
		// Afficher une série déjà affichée
		ordener.setHidden(serie2, false);
		assertFalse(ordener.isHidden(serie2));
	}// testSetHidden

	@Test
	public void testIterator() {
		Iterator<Serie> it = ordener.iterator();
		assertTrue(it.hasNext());
		assertSame(serie1, it.next());
		assertTrue(it.hasNext());
		assertSame(serie2, it.next());
		assertFalse(it.hasNext());
	}// testIterator

}
