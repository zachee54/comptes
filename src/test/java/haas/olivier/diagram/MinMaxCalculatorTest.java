/*
 * Copyright 2013-2018 Olivier HAAS. All rights reserved.
 */
package haas.olivier.diagram;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class MinMaxCalculatorTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	/** Des valeurs en abscisses. */
	private final Object a = new Object(), b = new Object(), c = new Object();
	
	/** Des séries mockées. */
	@Mock
	private Serie serie1, serie2, serie3;
	
	/** Un modèle de données. */
	private final DiagramModel model =
			new SimpleDiagramModel(new Object[] {a, b, c});
	
	/** Objet testé. */
	private Extrema minMax;
	
	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		
		// Comportement des séries
		when(serie1.isScaled()).thenReturn(true);
		when(serie2.isScaled()).thenReturn(true);
		when(serie3.isScaled()).thenReturn(true);
		
		when(serie1.get(a)).thenReturn(0);
		when(serie1.get(b)).thenReturn(10000);				// Maximum
		when(serie2.get(a)).thenReturn(-50);
		when(serie2.get(c)).thenReturn(BigDecimal.TEN);
		when(serie3.get(b)).thenReturn(-200f);				// Minimum
		when(serie3.get(c)).thenReturn(BigDecimal.ONE);
		
		// Ajouter les séries au modèle
		model.add(serie1);
		model.add(serie2);
		model.add(serie3);
		
		minMax = new Extrema(model);
	}// setUp

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testMinMaxCalculator() {
		assertEquals(10000, (int) minMax.getMax());
		assertEquals(-200, (int) minMax.getMin());
	}// testMinMaxCalculator

	@Test
	public void testCalculateMinMax() {
		model.remove(serie1);
		
		// Méthode testée
		minMax.updateExtrema();
		
		assertEquals(10, (int) minMax.getMax());
	}// testCalculateMinMax
}
