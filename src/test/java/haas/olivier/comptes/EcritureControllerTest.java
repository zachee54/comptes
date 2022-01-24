/*
 * Copyright 2013-2018 Olivier HAAS. All rights reserved.
 */
package haas.olivier.comptes;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.TreeSet;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.InOrder;

import haas.olivier.comptes.ctrl.EcritureController;
import haas.olivier.comptes.dao.CompteDAO;
import haas.olivier.comptes.dao.DAOFactory;
import haas.olivier.comptes.dao.EcritureDAO;
import haas.olivier.comptes.dao.SuiviDAO;
import haas.olivier.util.Month;

public class EcritureControllerTest {

	private static DateFormat parser;
	private static Month septembre2011, octobre2011, fevrier2012, aout2012;
	
	private Ecriture e1, e2, e3;
	private Iterable<Ecriture> ecritures;
	
	private EcritureDAO eDAO = mock(EcritureDAO.class);
	private SuiviDAO hDAO = mock(SuiviDAO.class),
			sDAO = mock(SuiviDAO.class),
			mDAO = mock(SuiviDAO.class);
	
	/**
	 * Un compte bancaire qui n'est pas un compte d'epargne
	 */
	private Compte c1 = mock(Compte.class);
	
	/**
	 * Un compte budgétaire qui est un compte d'épargne.
	 */
	private Compte c2 = mock(Compte.class);
	
	/**
	 * Un compte budgétaire qui est un compte d'épargne.
	 */
	private Compte c3 = mock(Compte.class);
	
	private BigDecimal zero = BigDecimal.ZERO,
			x = new BigDecimal("178.50"),
			y = new BigDecimal("-4500.32"),
			z = new BigDecimal("56.00");
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		parser = new SimpleDateFormat("dd/MM/yy");
		septembre2011 = Month.getInstance(parser.parse("01/09/11"));
		octobre2011 = Month.getInstance(parser.parse("01/10/11"));
		fevrier2012 = Month.getInstance(parser.parse("01/02/12"));
		aout2012 = Month.getInstance(parser.parse("01/08/12"));
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
		
		// Écritures
		e1 = new Ecriture(1, parser.parse("25/09/11"),
				parser.parse("26/10/11"), c1, c2, x, null, null, null);
		e2 = new Ecriture(2, parser.parse("12/10/11"), null, c2, c1,
				y, null, null, null);
		e3 = new Ecriture(3, parser.parse("29/02/12"),
				parser.parse("15/08/12"), c3, c1, z, null, null, null);
		ecritures = Arrays.asList(new Ecriture[] {e1, e2, e3});
		
		// Comportement des comptes
		when(c1.getId()).thenReturn(1);
		when(c2.getId()).thenReturn(2);
		when(c3.getId()).thenReturn(3);
		when(c1.getType()).thenReturn(TypeCompte.COMPTE_COURANT);
		when(c2.getType()).thenReturn(TypeCompte.RECETTES_EN_EPARGNE);
		when(c3.getType()).thenReturn(TypeCompte.DEPENSES_EN_EPARGNE);
		
		// Créer un modèle mocké complet
		DAOFactory dao = mock(DAOFactory.class);
		CompteDAO cDAO = mock(CompteDAO.class);

		when(dao.getCompteDAO()).thenReturn(cDAO);
		when(dao.getEcritureDAO()).thenReturn(eDAO);
		when(dao.getHistoriqueDAO()).thenReturn(hDAO);
		when(dao.getSoldeAVueDAO()).thenReturn(sDAO);
		when(dao.getMoyenneDAO()).thenReturn(mDAO);
		when(dao.canSaveSuivis()).thenReturn(true);
		
		// Comportement du mock : liste complète des comptes
		when(cDAO.getAll()).thenReturn(
				Arrays.asList(new Compte[] {c1, c2, c3}));
		
		// Comportement du mock : listes d'écritures
		when(eDAO.getAllSince((Month) any())).thenReturn(
				Collections.<Ecriture>emptyList());
		when(eDAO.getPointagesSince((Month) any())).thenReturn(
				Collections.<Ecriture>emptyList());
		
		// Historiques pour éviter une NPE lors du recalcul de la moyenne
		when(c1.getHistorique(any(Month.class))).thenReturn(BigDecimal.ZERO);
		when(c2.getHistorique(any(Month.class))).thenReturn(BigDecimal.ZERO);
		when(c3.getHistorique(any(Month.class))).thenReturn(BigDecimal.ZERO);
		
		DAOFactory.setFactory(dao, false);
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testInsertMoreRecent() throws EcritureMissingArgumentException, InconsistentArgumentsException, ParseException, IOException {
		when(eDAO.getAllSince(septembre2011)).thenReturn(ecritures);
		
		// Écriture existante
		when(eDAO.get(1)).thenReturn(e1);
		
		// Écriture mise à jour (la date change)
		Ecriture e1bis = new Ecriture(1, parser.parse("25/10/11"),
				parser.parse("26/10/11"), c1, c2, x, null, null, null);
		
		// Méthode testée
		EcritureController.insert(e1bis);
		
		// Vérifier la mise à jour de l'écriture
		verify(eDAO).update(e1bis);
		
		// Vérifier la mise à jour des suivis depuis septembre 2011
		verify(c2).addHistorique(septembre2011, x);
	}

	@Test
	public void testInsertOlder() throws EcritureMissingArgumentException, InconsistentArgumentsException, ParseException, IOException {
		
		// Écriture existante
		when(eDAO.get(3)).thenReturn(e3);
		
		// Écriture mise à jour (la date change)
		Ecriture e3bis = new Ecriture(3, parser.parse("29/10/11"),
				parser.parse("15/08/12"), c3, c1, z, null, null, null);
		
		// Renvoyer la nouvelle écriture lors de la mise à jour des suivis
		when(eDAO.getAllSince(octobre2011)).thenReturn(
				Collections.singleton(e3bis));
		
		// Méthode testée
		EcritureController.insert(e3bis);
		
		// Vérifier la mise à jour de l'écriture
		verify(eDAO).update(e3bis);
		
		// Vérifier la mise à jour des suivis depuis septembre 2011
		verify(c1).addHistorique(octobre2011, z);
	}
	
	@Test
	public void testInsertNoId() throws EcritureMissingArgumentException, InconsistentArgumentsException, ParseException, IOException {

		// Nouvelle écriture
		Ecriture e = new Ecriture(null, parser.parse("29/02/12"),
				parser.parse("15/08/12"), c3, c1, z, null, null, null);
		
		// Renvoyer celle-ci pour la mise à jour des suivis après insertion
		when(eDAO.getAllSince(fevrier2012)).thenReturn(
				Collections.singleton(e));
		
		// Méthode testée
		EcritureController.insert(e);
		
		// Vérifier l'insertion comme nouvelle écriture
		verify(eDAO).add(e);
		
		// Vérifier la mise à jour des suivis
		verify(c1).addHistorique(fevrier2012, z);
	}

	@Test
	public void testAdd() throws EcritureMissingArgumentException, InconsistentArgumentsException, ParseException, IOException {

		/*
		 * Faire comme si les écritures étaient déjà dans le modèle.
		 * Ça ne sert pas au moment de leur insertion, mais ça sert au moment de
		 * la mise à jour des suivis : l'application reprend les écritures du
		 * modèle à compter du mois choisi pour tout recalculer.
		 */
		when(eDAO.getAllSince(septembre2011)).thenReturn(ecritures);
		
		// Méthode testée
		EcritureController.add(ecritures);
		
		// Vérifier que les écritures ont été insérées
		verify(eDAO).add(e1);
		verify(eDAO).add(e2);
		verify(eDAO).add(e3);
		
		// Vérifier que les suivis ont été mis à jour
		// Au moins le suivi de c2 au mois de septembre 2011 (le plus ancien)
		verify(c2).addHistorique(septembre2011, x);
	}

	@Test
	public void testRemove() throws IOException {
		when(eDAO.get(1)).thenReturn(e1);
		
		/*
		 * Simuler des écritures depuis septembre pour vérifier la mise à jour
		 * de leur historique.
		 */
		when(eDAO.getAllSince(septembre2011)).thenReturn(ecritures);
		
		// Méthode testée
		EcritureController.remove(1);
		
		// Vérifier qu'elle a été supprimée
		verify(eDAO).remove(1);
		
		// Vérifier que les suivis ont été mis à jour
		verify(c2).addHistorique(septembre2011, x);
	}

	@Test
	public void testUpdateSuivis() throws ParseException, EcritureMissingArgumentException, InconsistentArgumentsException, IOException {

		// Mocks servant à faire le test
		when(c1.isEpargne()).thenReturn(false);
		when(c2.isEpargne()).thenReturn(true);
		when(c3.isEpargne()).thenReturn(true);

		// 3 écritures
		Ecriture e1 = new Ecriture(1, parser.parse("25/09/11"),
				parser.parse("26/10/11"), c1, c2, x, null, null, null);
		Ecriture e2 = new Ecriture(2, parser.parse("12/10/11"), null, c2, c1,
				y, null, null, null);
		Ecriture e3 = new Ecriture(3, parser.parse("29/02/12"),
				parser.parse("15/08/12"), c3, c1, z, null, null, null);

		TreeSet<Ecriture> list = new TreeSet<Ecriture>();
		list.add(e2);
		list.add(e3);

		TreeSet<Ecriture> pointees = new TreeSet<Ecriture>(new Ecriture.SortPointages());
		pointees.add(e1);
		pointees.add(e2); // Non pointée
		pointees.add(e3);

		// Mocker une couche DAO (pas de données réelles à enregistrer)

		when(eDAO.getAllSince(octobre2011)).thenReturn(list);
		when(eDAO.getPointagesSince(octobre2011)).thenReturn(pointees);

		// Comportement du mock: types de comptes (épargne ou pas)
		when(c1.getType()).thenReturn(TypeCompte.RECETTES);
		when(c2.getType()).thenReturn(TypeCompte.COMPTE_EPARGNE);
		when(c3.getType()).thenReturn(TypeCompte.DEPENSES_EN_EPARGNE);

		// Comportement du mock: historique renvoyée pour chaque compte
		when(c1.getHistorique(septembre2011)).thenReturn(zero);
		when(c1.getHistorique(octobre2011)).thenReturn(x.negate());
		when(c1.getHistorique(fevrier2012)).thenReturn(y.subtract(x));

		when(c2.getHistorique(septembre2011)).thenReturn(zero);
		when(c2.getHistorique(octobre2011)).thenReturn(x);

		when(c3.getHistorique(fevrier2012)).thenReturn(zero);

		// Comportement du mock: soldes à vue renvoyés pour chaque compte
		when(c1.getSoldeAVue(octobre2011)).thenReturn(zero);
		when(c1.getSoldeAVue(aout2012)).thenReturn(x.negate());

		when(c3.getSoldeAVue(aout2012)).thenReturn(zero);

		// Comportement du mock à l'impact des écritures: utiliser un vrai objet
		when(c1.getImpactOf((Ecriture) any())).thenCallRealMethod();
		
		// Méthode testée
		EcritureController.updateSuivis(octobre2011);

		// Vérification de l'effacement des historiques
		verify(hDAO).removeFrom(octobre2011);
		verify(sDAO).removeFrom(octobre2011);
		verify(mDAO).removeFrom(octobre2011);

		// Vérification des historiques par compte dans l'ordre chronologique
		InOrder inOrder1 = inOrder(c1);
		inOrder1.verify(c1).addHistorique(octobre2011, y);
		inOrder1.verify(c1).addHistorique(fevrier2012, z);

		verify(c2).addHistorique(octobre2011, y.negate());
		verify(c3).addHistorique(fevrier2012, z.negate());

		// Vérification des pointages par compte dans l'ordre chronologique
		InOrder inOrder1bis = inOrder(c1);
		inOrder1bis.verify(c1).addPointages(octobre2011, x.negate());
		inOrder1bis.verify(c1).addPointages(aout2012, z);

		verify(c3).addPointages(aout2012, z.negate());

		verify(c2).addPointages(octobre2011, x);
		verify(c2, never()).addPointages((Month) any(), eq(y)); 
		verify(c2, never()).addPointages((Month) any(), eq(y.negate())); 
		verify(c2, atLeast(1)).isEpargne();
		
		// Vérification de l'actualisation des moyennes des comptes budgétaires
//		verify((CompteBudget) c1).updateMoyennes(octobre);
//		verify((CompteBudget) c2).updateMoyennes(octobre);

		// Vérification des épargnes
		verify(hDAO).set(Compte.COMPTE_EPARGNE, octobre2011, y.negate());
		verify(hDAO).set(Compte.COMPTE_EPARGNE, fevrier2012, z.negate());
	}

}
