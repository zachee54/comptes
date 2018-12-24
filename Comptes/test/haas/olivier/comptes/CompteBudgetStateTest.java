/*
 * Copyright 2013-2018 Olivier HAAS. All rights reserved.
 */
package haas.olivier.comptes;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.math.BigDecimal;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import haas.olivier.comptes.dao.DAOFactory;
import haas.olivier.comptes.dao.SuiviDAO;
import haas.olivier.util.Month;

public class CompteBudgetStateTest {

	/**
	 * Un mois quelconque.
	 */
	private static final Month month = Month.getInstance();
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	/**
	 * Un compte mocké.
	 */
	private final Compte compte = mock(Compte.class);
	
	/**
	 * Un suivi mocké.
	 */
	private final SuiviDAO suivi = mock(SuiviDAO.class);
	
	/**
	 * Un DAO mocké.
	 */
	private final DAOFactory factory = mock(DAOFactory.class);
	
	/**
	 * L'objet testé.
	 */
	private final CompteBudgetState state =
			new CompteBudgetState(TypeCompte.DEPENSES);
	
	@Before
	public void setUp() throws Exception {
		DAOFactory.setFactory(factory);
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testGetType() {
		assertSame(TypeCompte.DEPENSES, state);
	}

	@Test
	public void testGetNumero() {
		assertNull(state.getNumero());
	}

	@Test
	public void testSetNumero() {
		state.setNumero(Long.valueOf(7L));
		assertNull(state.getNumero());
	}

	@Test
	public void testGetSuivi() {
		when(suivi.get(compte, month)).thenReturn(BigDecimal.TEN);
		
		assertEquals(0, BigDecimal.TEN.compareTo(
				state.getSuivi(compte, suivi, month)));
	}

	@Test
	public void testAddHistoriquePreviousNull() throws IOException {
		when(factory.getHistoriqueDAO()).thenReturn(suivi);
		
		state.addHistorique(compte, month, BigDecimal.TEN);
		
		/* Égalité ambigue entre BigDecimals */
		verify(suivi).set(eq(compte), eq(month), eq(new BigDecimal("-10")));
	}
	
	@Test
	public void testAddHistorique() throws IOException {
		when(factory.getHistoriqueDAO()).thenReturn(suivi);
		when(suivi.get(compte, month)).thenReturn(new BigDecimal("45"));
		
		state.addHistorique(compte, month, BigDecimal.TEN);
		
		/* Égalité ambigue entre BigDecimals */
		verify(suivi).set(eq(compte), eq(month), eq(new BigDecimal("35")));
	}
	
	@Test
	public void testAddPointagePreviousNull() throws IOException {
		when(factory.getSoldeAVueDAO()).thenReturn(suivi);
		
		state.addPointage(compte, month, BigDecimal.TEN);
		
		/* Égalité ambigue entre BigDecimals */
		verify(suivi).set(eq(compte), eq(month), eq(new BigDecimal("-10")));
	}
	
	@Test
	public void testAddPointage() throws IOException {
		when(factory.getSoldeAVueDAO()).thenReturn(suivi);
		when(suivi.get(compte, month)).thenReturn(new BigDecimal("45"));
		
		state.addPointage(compte, month, BigDecimal.TEN);
		
		/* Égalité ambigue entre BigDecimals */
		verify(suivi).set(eq(compte), eq(month), eq(new BigDecimal("35")));
	}

	@Test
	public void testGetViewSign() {
		Compte compte2 = mock(Compte.class);
		
		assertEquals(1, state.getViewSign(compte, compte, compte2));
		assertEquals(-1, state.getViewSign(compte, compte2, compte));
		assertEquals(0, state.getViewSign(compte, compte2, compte2));
	}

	@Test
	public void testEqualsObject() {
		assertEquals(state, new CompteBudgetState(TypeCompte.DEPENSES));
		assertNotEquals(state, new CompteBudgetState(TypeCompte.RECETTES));
	}

}
