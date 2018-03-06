package haas.olivier.comptes;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import haas.olivier.comptes.CompteBudget;
import haas.olivier.util.Month;
import haas.olivier.comptes.TypeCompte;
import haas.olivier.comptes.dao.CompteDAO;
import haas.olivier.comptes.dao.DAOFactory;
import haas.olivier.comptes.dao.SuiviDAO;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class CompteBudgetTest_ {

	private static BigDecimal zero, douze, a, b;
	private static Month month1, month2;
	private static DateFormat parser;
	private CompteBudget compte;
	private DAOFactory factory;
	private CompteDAO cDAO;
	private SuiviDAO hDAO, sDAO, mDAO;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		parser = new SimpleDateFormat("dd/MM/yy");

		// Des données pêle-mêle
		month1 = new Month(parser.parse("26/10/04"));
		month2 = new Month(parser.parse("11/11/04"));
		a = new BigDecimal("14595.03");
		b = new BigDecimal("-487.50");
		zero = new BigDecimal("0");
		douze = new BigDecimal("12");
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {

		// DAO mockés
		cDAO = mock(CompteDAO.class);
		hDAO = mock(SuiviDAO.class);
		sDAO = mock(SuiviDAO.class);
		mDAO = mock(SuiviDAO.class);

		// DAOFactory mockée
		factory = mock(DAOFactory.class);
		when(factory.getCompteDAO()).thenReturn(cDAO);
		when(factory.getHistoriqueDAO()).thenReturn(hDAO);
		when(factory.getSoldeAVueDAO()).thenReturn(sDAO);
		when(factory.getMoyenneDAO()).thenReturn(mDAO);
		when(factory.getDebut()).thenReturn(month1);
		DAOFactory.setFactory(factory, false);

		// Compte testé
		compte = new CompteBudget(7, "Compte de test", TypeCompte.RECETTES);
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testAddHistorique() throws IOException {
		/*
		 * La méthode addHistorique doit 1° aller chercher le solde actuel 2°
		 * enregistrer le nouveau solde 3° mettre à jour la moyenne Pour cela,
		 * on a besoin de mocker un objet CompteBudgetDAO qui va, pour chaque
		 * appel de getHistorique() sur ce mois, renvoyer d'abord null (étape
		 * 1°), puis le solde censé avoir été stocké (pour l'étape 3°).
		 */
		when(hDAO.get(7, month1)).thenReturn(a);

		// Méthode à tester
		compte.addHistorique(month1, b);

		/*
		 * Vérifier la mise à jour du solde (attention: CompteBudget prend
		 * l'opposé !)
		 */
		verify(hDAO).set(7, month1, a.add(b.negate()));
	}

	@Test
	public void testGetType() {
		assertEquals(compte.getType(), TypeCompte.RECETTES);
	}

	@Test
	public void testGetMoyenne() throws ParseException {
		BigDecimal moy = compte.getMoyenne(month1);

		assertNotNull("Moyenne non null si pas d'opération", moy);
		assertEquals("Moyenne zéro si pas d'opération", 0, zero.compareTo(moy));

		// Vérifier l'appel de la fonction dans la couche données
		verify(mDAO).get(7, month1);
	}
	
	@Test
	public void testUpdateMoyennes() throws ParseException, IOException {
		
		// Mocker une base de données avec des historiques
		when(hDAO.get(7, month1)).thenReturn(a);
		when(hDAO.get(7, month2)).thenReturn(b);
		
		// Méthode testée
		compte.updateMoyennes(month1);
		
		/* Vérifier la mise à jour de chaque moyennes.
		 * ATTENTION il faut que month1 et month2 correspondent bien à deux mois
		 * consécutifs, sinon la vérification n'a pas de sens.
		 */
		// 1er mois: a/12
		verify(mDAO).set(7, month1, a.divide(douze, RoundingMode.HALF_UP));
		// Du 2ème au 12ème mois: (a+b)/12
		for (int m = 1; m < 12; m++) {
			verify(mDAO).set(7, month1.getTranslated(m), b.add(a).divide(douze, RoundingMode.HALF_UP));
		}
		// 13ème mois: b/12
		verify(mDAO).set(7, month1.getTranslated(12), b.divide(douze, RoundingMode.HALF_UP));
	}// testUpdateMoyennes
}
