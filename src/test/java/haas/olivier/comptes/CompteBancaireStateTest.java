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

public class CompteBancaireStateTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
		when(factory.canSaveSuivis()).thenReturn(true);
		DAOFactory.setFactory(factory);
		
		when(factory.getDebut()).thenReturn(month.getTranslated(-12));
	}

	@After
	public void tearDown() throws Exception {
	}
	
	private final DAOFactory factory = mock(DAOFactory.class);
	
	/**
	 * Un mois quelconque (celui en cours).
	 */
	private final Month month = Month.getInstance();
	
	/**
	 * Un compte mocké.
	 */
	private final Compte compte = mock(Compte.class);

	/**
	 * Vérifier que le code s'exécute sans mentionner d'état précédent.
	 */
	@Test
	public void testCompteBancaireStateOldNull() {
		new CompteBancaireState(TypeCompte.COMPTE_CARTE, null);
	}
	
	@Test
	public void testCompteBancaireStateOldBancaire() {
		CompteState old = mock(CompteState.class);
		when(old.getNumero()).thenReturn(15L);
		
		CompteBancaireState state =
				new CompteBancaireState(TypeCompte.COMPTE_COURANT, old);
		assertEquals(Long.valueOf(15L), state.getNumero());
	}
	
	@Test
	public void testCompteBancaireStateOldBudget() {
		CompteState old = new CompteBudgetState(TypeCompte.DEPENSES);
		
		CompteBancaireState state =
				new CompteBancaireState(TypeCompte.COMPTE_COURANT, old);
		assertNull(state.getNumero());
	}

	@Test
	public void testGetType() {
		CompteState old = mock(CompteState.class);
		when(old.getType()).thenReturn(TypeCompte.COMPTE_CARTE);
		
		CompteBancaireState state =
				new CompteBancaireState(TypeCompte.COMPTE_EPARGNE, old);
		assertEquals(TypeCompte.COMPTE_EPARGNE, state.getType());
	}

	@Test
	public void testSetNumero() {
		CompteBancaireState state =
				new CompteBancaireState(TypeCompte.EMPRUNT, null);
		state.setNumero(47L);
		assertEquals(Long.valueOf(47L), state.getNumero());
	}

	@Test
	public void testGetSuivi() {
		SuiviDAO suivi = mock(SuiviDAO.class);
		when(suivi.get(compte, month)).thenReturn(BigDecimal.TEN);
		
		CompteBancaireState state =
				new CompteBancaireState(TypeCompte.ENFANTS, null);
		assertEquals(0, BigDecimal.TEN.compareTo(
				state.getSuivi(compte, suivi, month)));
	}
	
	/**
	 * Teste qu'en cas d'absence de suivi, on aille chercher le dernier suivi
	 * précédent.
	 */
	@Test
	public void testGetSuiviPrevious() {
		Month past = month.getTranslated(-5);
		
		SuiviDAO suivi = mock(SuiviDAO.class);
		when(suivi.get(compte, past)).thenReturn(BigDecimal.ONE);
		
		CompteBancaireState state =
				new CompteBancaireState(TypeCompte.COMPTE_COURANT, null);
		assertEquals(0, BigDecimal.ONE.compareTo(
				state.getSuivi(compte, suivi, month)));
	}

	@Test
	public void testAddHistorique() throws IOException {
		SuiviDAO suivi = mock(SuiviDAO.class);
		when(suivi.get(compte, month)).thenReturn(BigDecimal.TEN);
		
		when(factory.getHistoriqueDAO()).thenReturn(suivi);
		
		CompteBancaireState state =
				new CompteBancaireState(TypeCompte.COMPTE_EPARGNE, null);
		state.addHistorique(compte, month, new BigDecimal("-12"));
		
		// Attention : l'égalité entre BigDecimals dépend aussi de l'échelle
		verify(suivi).set(eq(compte), eq(month), eq(new BigDecimal("-2")));
	}
	
	@Test
	public void testAddHistoriquePrevious() throws IOException {
		Month past = month.getTranslated(-7);
		
		SuiviDAO suivi = mock(SuiviDAO.class);
		when(suivi.get(compte, past)).thenReturn(BigDecimal.ONE);
		
		when(factory.getHistoriqueDAO()).thenReturn(suivi);
		
		CompteBancaireState state =
				new CompteBancaireState(TypeCompte.COMPTE_CARTE, null);
		state.addHistorique(compte, month, BigDecimal.TEN);
		
		// Attention à l'égalité entre BigDecimals
		verify(suivi).set(eq(compte), eq(month), eq(new BigDecimal("11")));
	}

	@Test
	public void testAddPointage() throws IOException {
		SuiviDAO suivi = mock(SuiviDAO.class);
		when(suivi.get(compte, month)).thenReturn(BigDecimal.TEN);
		
		when(factory.getSoldeAVueDAO()).thenReturn(suivi);
		
		CompteBancaireState state =
				new CompteBancaireState(TypeCompte.COMPTE_EPARGNE, null);
		state.addPointage(compte, month, new BigDecimal("-12"));
		
		// Attention : l'égalité entre BigDecimals dépend aussi de l'échelle
		verify(suivi).set(eq(compte), eq(month), eq(new BigDecimal("-2")));
	}
	
	@Test
	public void testAddPointagePrevious() throws IOException {
		Month past = month.getTranslated(-7);
		SuiviDAO suivi = mock(SuiviDAO.class);
		when(suivi.get(compte, past)).thenReturn(BigDecimal.ONE);
		
		when(factory.getSoldeAVueDAO()).thenReturn(suivi);
		
		CompteBancaireState state =
				new CompteBancaireState(TypeCompte.COMPTE_CARTE, null);
		state.addPointage(compte, month, BigDecimal.TEN);
		
		// Attention à l'égalité entre BigDecimals
		verify(suivi).set(eq(compte), eq(month), eq(new BigDecimal("11")));
	}


	@Test
	public void testGetViewSignDebit() {
		Compte compte2 = mock(Compte.class);
		
		CompteBancaireState state =
				new CompteBancaireState(TypeCompte.EMPRUNT, null);
		
		assertEquals(-1, state.getViewSign(compte, compte, compte2));
		assertEquals(1, state.getViewSign(compte, compte2, compte));
		assertEquals(0, state.getViewSign(compte, compte2, compte2));
	}

	@Test
	public void testEqualsObject() {
		CompteBancaireState state1 =
				new CompteBancaireState(TypeCompte.COMPTE_COURANT, null);
		CompteBancaireState state2 =
				new CompteBancaireState(TypeCompte.COMPTE_COURANT, null);
		
		assertTrue(state1.equals(state2));
		
		state1.setNumero(4L);
		assertFalse(state1.equals(state2));
		assertFalse(state2.equals(state1));
		
		state2.setNumero(4L);
		assertTrue(state1.equals(state2));
	}
	
	@Test
	public void testEqualsTypes() {
		CompteBancaireState state1 =
				new CompteBancaireState(TypeCompte.COMPTE_COURANT, null);
		CompteBancaireState state2 =
				new CompteBancaireState(TypeCompte.COMPTE_EPARGNE, null);
		assertFalse(state1.equals(state2));
	}

}
