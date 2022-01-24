/*
 * Copyright 2013-2018 Olivier HAAS. All rights reserved.
 */
package haas.olivier.comptes;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import haas.olivier.comptes.ctrl.SituationCritique;
import haas.olivier.comptes.dao.DAOFactory;
import haas.olivier.comptes.dao.EcritureDAO;
import haas.olivier.comptes.dao.SuiviDAO;
import haas.olivier.util.Month;

public class SituationCritiqueTest {

	/**
	 * Le mois en cours.
	 */
	private static final Month MONTH = Month.getInstance();
	
	/**
	 * Le mois prochain.
	 */
	private static final Month MONTH_NEXT = MONTH.getNext();

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}
	
	@Mock
	private DAOFactory factory;
	
	@Mock
	private EcritureDAO eDAO;
	
	@Mock
	private SuiviDAO hDAO;
	
	/**
	 * Le compte principal utilisé pour les tests.
	 */
	private final Compte compte = new Compte(1, TypeCompte.COMPTE_COURANT);
	
	/**
	 * Un compte de contrepartie quelconque.
	 */
	private final Compte compte2 = mock(Compte.class);
	
	/**
	 * La date du jour.
	 */
	private Date today;
	
	/**
	 * Le 1er jour du mois suivant.
	 */
	private Date nextMonthDay1;
	
	/**
	 * Le 5ème jour du mois suivant.
	 */
	private Date nextMonthDay5;

	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		when(factory.canSaveSuivis()).thenReturn(true);
		
		DAOFactory.setFactory(factory);
		when(factory.getEcritureDAO()).thenReturn(eDAO);
		when(factory.getHistoriqueDAO()).thenReturn(hDAO);
		when(factory.getDebut()).thenReturn(
				Month.getInstance(2000, 1));				// 01/2000
		
		// Créer la date du jour sans heures/minutes/secondes
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		today = cal.getTime();
		cal.set(Calendar.DAY_OF_MONTH, 1);
		cal.add(Calendar.MONTH, 1);
		nextMonthDay1 = cal.getTime();
		cal.set(Calendar.DAY_OF_MONTH, 5);
		nextMonthDay5 = cal.getTime();
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testSituationCritiqueStable() throws IOException {
		when(hDAO.get(eq(compte), any())).thenReturn(BigDecimal.ONE);
		when(eDAO.getAllTo(any())).thenReturn(Collections.emptyList());
		
		// Méthode testée
		SituationCritique critique = compte.getSituationCritique();
		
		assertEquals(today, critique.getDateCritique());
		assertEquals(0, BigDecimal.ONE.compareTo(critique.getSoldeMini()));
	}

	@Test
	public void testSituationCritiqueCroissant() throws EcritureMissingArgumentException, InconsistentArgumentsException, IOException {
		
		// Deux écritures de crédit de 1€ et 10€ sur le mois suivant
		Ecriture e1 = new Ecriture(1, nextMonthDay1, null, compte2, compte, BigDecimal.ONE, null, null, null);
		Ecriture e2 = new Ecriture(2, nextMonthDay5, nextMonthDay5, compte2, compte, BigDecimal.TEN, null, null, null);
		
		// Les soldes de fin de mois
		when(hDAO.get(compte, MONTH)).thenReturn(BigDecimal.TEN);
		when(hDAO.get(compte, MONTH_NEXT)).thenReturn(new BigDecimal(21));
		
		// La liste des écritures pour chaque mois
		when(eDAO.getAllTo(MONTH)).thenReturn(Collections.emptyList());
		when(eDAO.getAllTo(MONTH_NEXT)).thenReturn(
				Arrays.asList(new Ecriture[] {e2, e1}));
		
		// Méthode testée
		SituationCritique critique = compte.getSituationCritique();
		
		assertEquals(today, critique.getDateCritique());
		assertEquals(0, BigDecimal.TEN.compareTo(critique.getSoldeMini()));
	}
	
	@Test
	public void testSituationCritiqueDecroissant() throws EcritureMissingArgumentException, InconsistentArgumentsException, IOException {
		
		// Deux écritures de débit de 1€ et 10€ sur le mois suivant
		Ecriture e1 = new Ecriture(1, nextMonthDay1, null, compte, compte2, BigDecimal.ONE, null, null, null);
		Ecriture e2 = new Ecriture(2, nextMonthDay5, nextMonthDay5, compte, compte2, BigDecimal.TEN, null, null, null);
		
		// Les soldes de fin de mois
		when(hDAO.get(compte, MONTH)).thenReturn(new BigDecimal(20));
		when(hDAO.get(compte, MONTH_NEXT)).thenReturn(new BigDecimal(9));
		
		// La liste des écritures pour chaque mois
		when(eDAO.getAllTo(MONTH)).thenReturn(Collections.emptyList());
		when(eDAO.getAllTo(MONTH_NEXT)).thenReturn(
				Arrays.asList(new Ecriture[] {e2, e1}));
		
		// Méthode testée
		SituationCritique critique = compte.getSituationCritique();
		
		assertEquals(nextMonthDay5, critique.getDateCritique());
		assertEquals(0, new BigDecimal(9).compareTo(critique.getSoldeMini()));
	}
	
	@Test
	public void testSituationCritiqueEnV() throws EcritureMissingArgumentException, InconsistentArgumentsException, IOException {
		
		// Deux écritures de débit de 10€ et crédit de 1€ sur le mois suivant
		Ecriture e1 = new Ecriture(1, nextMonthDay1, null, compte, compte2, BigDecimal.TEN, null, null, null);
		Ecriture e2 = new Ecriture(2, nextMonthDay5, nextMonthDay5, compte2, compte, BigDecimal.ONE, null, null, null);
		
		// Les soldes de fin de mois
		when(hDAO.get(compte, MONTH)).thenReturn(new BigDecimal(16));
		when(hDAO.get(compte, MONTH_NEXT)).thenReturn(new BigDecimal(7));
		
		// La liste des écritures pour chaque mois
		when(eDAO.getAllTo(MONTH)).thenReturn(Collections.emptyList());
		when(eDAO.getAllTo(MONTH_NEXT)).thenReturn(
				Arrays.asList(new Ecriture[] {e2, e1}));
		
		// Méthode testée
		SituationCritique critique = compte.getSituationCritique();
		
		assertEquals(nextMonthDay1, critique.getDateCritique());
		assertEquals(0, new BigDecimal(6).compareTo(critique.getSoldeMini()));
	}
	
	@Test
	public void testSituationCritiqueDecouvert() throws EcritureMissingArgumentException, InconsistentArgumentsException, IOException {
		
		// Deux écritures de débit de 10€ et 1€ sur le mois suivant
		Ecriture e1 = new Ecriture(1, nextMonthDay1, null, compte, compte2, BigDecimal.TEN, null, null, null);
		Ecriture e2 = new Ecriture(2, nextMonthDay5, nextMonthDay5, compte, compte2, BigDecimal.ONE, null, null, null);
		
		// Les soldes de fin de mois
		when(hDAO.get(compte, MONTH)).thenReturn(new BigDecimal(6));
		when(hDAO.get(compte, MONTH_NEXT)).thenReturn(new BigDecimal(-5));
		
		// La liste des écritures pour chaque mois
		when(eDAO.getAllTo(MONTH)).thenReturn(Collections.emptyList());
		when(eDAO.getAllTo(MONTH_NEXT)).thenReturn(
				Arrays.asList(new Ecriture[] {e2, e1}));
		
		// Méthode testée
		SituationCritique critique = compte.getSituationCritique();
		
		assertEquals("1er jour de découvert",
				nextMonthDay1, critique.getDateCritique());
		assertEquals("montant maximal du découvert",
				0, new BigDecimal(-5).compareTo(critique.getSoldeMini()));
	}
	
	@Test
	public void testSituationCritiqueDecouvertEnV() throws EcritureMissingArgumentException, InconsistentArgumentsException, IOException {
		
		// Deux écritures de crédit de 1€ et 10€ sur le mois suivant
		Ecriture e1 = new Ecriture(1, nextMonthDay1, null, compte2, compte, BigDecimal.ONE, null, null, null);
		Ecriture e2 = new Ecriture(2, nextMonthDay5, nextMonthDay5, compte2, compte, BigDecimal.TEN, null, null, null);
		
		// Les soldes de fin de mois
		when(hDAO.get(compte, MONTH)).thenReturn(new BigDecimal(-3));
		when(hDAO.get(compte, MONTH_NEXT)).thenReturn(new BigDecimal(8));
		
		// La liste des écritures pour chaque mois
		when(eDAO.getAllTo(MONTH)).thenReturn(Collections.emptyList());
		when(eDAO.getAllTo(MONTH_NEXT)).thenReturn(
				Arrays.asList(new Ecriture[] {e2, e1}));
		
		// Méthode testée
		SituationCritique critique = compte.getSituationCritique();
		
		assertEquals("Déjà à découvert aujourd'hui",
				today, critique.getDateCritique());
		assertEquals("montant maximal du découvert aujourd'hui",
				0, new BigDecimal(-3).compareTo(critique.getSoldeMini()));
	}
}
