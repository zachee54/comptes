/*
 * Copyright 2013-2018 Olivier HAAS. All rights reserved.
 */
package haas.olivier.diagram;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.Iterator;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class EchelleTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}
	
	/** Des valeurs en abscisses. */
	private final Object[] xValues = {new Object(), new Object()};
	
	/** Un modèle de données simpliste. */
	private final DiagramModel model = new SimpleDiagramModel(xValues);

	/** L'objet testé. */
	private Echelle echelle;
	
	/** Des séries mockées. */
	private Serie serie1 = mock(Serie.class), serie2 = mock(Serie.class);
	
	@Before
	public void setUp() throws Exception {
		when(serie1.isScaled()).thenReturn(true);
		when(serie1.get(xValues[0])).thenReturn(-50);
		when(serie1.get(xValues[1])).thenReturn(23900);
		
		when(serie2.isScaled()).thenReturn(false);	// Série à ignorer
		when(serie2.get(xValues[0])).thenReturn(150000);
		when(serie2.get(xValues[1])).thenReturn(-30000);
		
		model.add(serie1);
		model.add(serie2);
		
		echelle = new Echelle(model);
	}// setUp

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testUpdateGraduations() {
		when(serie2.isScaled()).thenReturn(true);
		
		// Méthode testée
		echelle.updateGraduations();
		
		assertTrue((int) echelle.getMax() >= 150000);
		assertTrue((int) echelle.getMin() <= -30000);
	}// dataChanged

	@Test
	public void testGetMin() {
		int min = (int) echelle.getMin();
		assertTrue(min <= -50);
		assertTrue(min > -30000);
	}// testGetMin

	@Test
	public void testGetMax() {
		int max = (int) echelle.getMax();
		assertTrue(max >= 24000);
		assertTrue(max < 150000);
	}// testGetMax

	@Test
	public void testGetGraduations() {
		Iterator<BigDecimal> it = echelle.getGraduations().iterator();
		
		// Vérifier le minimum
		assertTrue(it.hasNext());
		assertTrue((int) it.next().doubleValue() == 0);		// La plus petite
		
		// Vérifier le nombre et le maximum
		int count = 1;
		int next = 0;
		while (it.hasNext()) {
			next = (int) it.next().doubleValue();
			count++;
		}
		assertTrue(count >= 4 && count <= 10);				//4 à 10 graduations
		assertTrue(next <= 25000);							// La plus grande
	}// testGetGraduations

	@Test
	public void testGetYFromValueNumberInt() {
		BigDecimal min = new BigDecimal(echelle.getMin()),
				max = new BigDecimal(echelle.getMax());
		
		assertEquals(999, echelle.getYFromValue(min, 1000));
		assertEquals(0, echelle.getYFromValue(max, 1000));
		assertEquals(499, echelle.getYFromValue(
				min.add(max).divide(new BigDecimal("2")),
				1000));
	}// testGetYFromValueNumberInt

	@Test
	public void testGetYFromValueDoubleInt() {
		double min = echelle.getMin(), max = echelle.getMax();
		
		// Dernier pixel visible = y + height - 1, soit ici 999
		assertEquals(999, echelle.getYFromValue(min, 1000));
		assertEquals(0, echelle.getYFromValue(max, 1000));
		assertEquals(499, echelle.getYFromValue((min+max)/2, 1000));
	}// getYFromValueDoubleInt
}
