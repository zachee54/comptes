/*
 * Copyright 2013-2018 Olivier HAAS. All rights reserved.
 */
package haas.olivier.comptes.dao.xml;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import haas.olivier.comptes.dao.PropertiesDAO;
import haas.olivier.diagram.DiagramMemento;

public class JaxbPropertiesDAOTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	/** Un objet d'accès aux données fictives. */
	private PropertiesDAO props = mock(PropertiesDAO.class);
	
	/** Des propriétés de diagrammes fictives. */
	private final Map<String, DiagramMemento> diagProps = new HashMap<>();
	
	@Before
	public void setUp() throws Exception {
		List<Integer> series1 = new ArrayList<>(),
				series2 = new ArrayList<>();
		Set<Integer> hidden1 = new HashSet<>(),
				hidden2 = new HashSet<>();
		
		// Memento 1
		series1.add(1);
		series1.add(7);
		series1.add(4);
		hidden1.add(1);
		hidden1.add(7);
		DiagramMemento memento1 = new DiagramMemento("un", series1, hidden1);
		
		// Memento 2
		series2.add(2);
		series2.add(11);
		series2.add(24);
		hidden2.add(24);
		DiagramMemento memento2 = new DiagramMemento("deux", series2, hidden2);
		
		diagProps.put(memento1.getName(), memento1);
		diagProps.put(memento2.getName(), memento2);
		
		when(props.getDiagramNames()).thenReturn(diagProps.keySet());
		when(props.getDiagramProperties("un")).thenReturn(memento1);
		when(props.getDiagramProperties("deux")).thenReturn(memento2);
	}// setUp

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testJaxbPropertiesDAO() throws IOException {
		byte[] bytes = save();
		
		JaxbPropertiesDAO jaxbProps =
				new JaxbPropertiesDAO(new ByteArrayInputStream(bytes));
		assertEquals(diagProps, jaxbProps.getDiagramProperties());
	}// testJaxbPropertiesDAO

	/** Sauvegarde les données fictives au format XML dans un byte[]. */
	private byte[] save() throws IOException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		JaxbPropertiesDAO.save(props, out);
		return out.toByteArray();
	}// save
}
