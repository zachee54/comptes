package haas.olivier.comptes;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import haas.olivier.comptes.Compte;
import haas.olivier.comptes.CompteBancaire;
import haas.olivier.comptes.CompteBudget;
import haas.olivier.comptes.Ecriture;
import haas.olivier.comptes.TypeCompte;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class EcritureTest {
	
	private static DateFormat parser;
	private Compte c1, c2, c3;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		parser = new SimpleDateFormat("dd/MM/yy");
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testCompareTo()
			throws ParseException, EcritureMissingArgumentException, InconsistentArgumentsException {

		// Mocks servant à faire le test
		c1 = mock(Compte.class);
		c2 = mock(Compte.class);
		c3 = mock(Compte.class);
		when(c1.isEpargne()).thenReturn(false);
		when(c2.isEpargne()).thenReturn(false);
		when(c3.isEpargne()).thenReturn(false);

		Ecriture e1, e2, e2bis, e3, e3bis, e3ter;
		e1 = new Ecriture(1, parser.parse("14/04/12"), null, c1, c2,
				new BigDecimal("14000.3"), null, null, null);
		e2 = new Ecriture(2, parser.parse("15/08/12"), null, c2, c3,
				new BigDecimal("-2500"), null, null, null);
		e2bis = new Ecriture(2, parser.parse("15/08/12"), null, c2, c3,
				new BigDecimal("-2500"), null, null, 8);
		e3 = new Ecriture(3, parser.parse("15/08/12"), null, c3, c1, new
				BigDecimal("1.25"), null, null, null);
		e3bis = new Ecriture(3, parser.parse("15/08/12"), null, c1, c2, new
				BigDecimal("475"), null, null, 12);
		e3ter = new Ecriture(3, parser.parse("15/08/12"), null, c3, c1, new
				BigDecimal("1.25"), null, null, 12);

		assertTrue("Comparaison sur la date", e1.compareTo(e2) < 0);
		assertTrue("Comparaison sur la date (inverse)", e2.compareTo(e1) > 0);
		assertTrue("Comparaison sur l'id sans chèque", e2.compareTo(e3) < 0);
		assertTrue("Comparaison sur l'id sans chèque", e3.compareTo(e2) > 0);
		assertTrue("Comparaison sur l'id avec un seul chèque",
				e2.compareTo(e3bis) < 0);
		assertTrue("Comparaison sur l'id avec un seul chèque",
				e3bis.compareTo(e2) > 0);
		assertTrue("Comparaison sur le chèque", e2bis.compareTo(e3bis) < 0);
		assertTrue("Comparaison sur le chèque", e3bis.compareTo(e2bis) > 0);
		assertEquals("Egalité parfaite", 0, e3bis.compareTo(e3ter));
	}// testCompareTo

	@Test
	public void testEquals()
			throws ParseException, EcritureMissingArgumentException, InconsistentArgumentsException {
		Date date1 = parser.parse("15/08/04");
		c1 = new CompteBancaire(1, "compte 1", 0, TypeCompte.COMPTE_CARTE);
		c2 = new CompteBudget(2, "compte 2", TypeCompte.RECETTES_EN_EPARGNE);
		c3 = new CompteBudget(3, "compte 3", TypeCompte.DEPENSES);

		// Ecriture à tester
		Ecriture e = new Ecriture(5, date1, null, c1, c2, new BigDecimal(
				"546.3"), null, null, null);

		// Tester l'égalité simple (avec un petit changement sur le montant !)
		Ecriture e1 = new Ecriture(5, parser.parse("15/08/04"), null, c1, c2,
				new BigDecimal("546.30"), null, null, null);
		assertTrue(e.equals(e1));
		assertTrue(e1.equals(e));

		// Enlever l'id
		e1 = new Ecriture(null, date1, null, c1, c2, new BigDecimal("546.30"),
				null, null, null);
		assertFalse(e.equals(e1));
		assertFalse(e1.equals(e));
		// Autre id
		e1 = new Ecriture(19, date1, null, c1, c2, new BigDecimal("546.30"),
				null, null, null);
		assertFalse(e.equals(e1));

		// Changer la date
		e1 = new Ecriture(5, parser.parse("19/09/12"), null, c1, c2,
				new BigDecimal("546.30"), null, null, null);
		assertFalse(e.equals(e1));

		// Changer le compte débité
//		e1 = new Ecriture(5, date1, null, c3, c2, new BigDecimal("546.30"),
//				null, null, null);
//		assertFalse(e.equals(e1));

		// Changer le compte crédité
//		e1 = new Ecriture(5, date1, null, c1, c3, new BigDecimal("546.30"),
//				null, null, null);
//		assertFalse(e.equals(e1));

		// Changer le montant
//		e1 = new Ecriture(5, date1, null, c1, c2, new BigDecimal("1237"), null,
//				null, null);
//		assertFalse(e.equals(e1));

		// Ajouter un pointage
		// e1 = new Ecriture(5, date1, null, c1, c2, new BigDecimal("546.30"),
		// null, null, null);
		// e1.pointage = parser.parse("04/07/06");
		// assertFalse(e.equals(e1));
		// assertFalse(e1.equals(e));
		// // Autre pointage
		// e.pointage = parser.parse("05/03/07");
		// assertFalse(e.equals(e1));
		// // Même pointage
		// e1.pointage = parser.parse("05/03/07");
		// assertTrue(e.equals(e1));
		//
		// // Ajouter un tiers
		// e1.tiers = "papa";
		// assertFalse(e.equals(e1));
		// assertFalse(e1.equals(e));
		// // Autre tiers
		// e.tiers = "maman";
		// assertFalse(e.equals(e1));
		// // Même tiers
		// e1.tiers = "maman";
		// assertTrue(e.equals(e1));
		//
		// // Ajouter un libellé
		// e1.libelle = "pourquoi";
		// assertFalse(e.equals(e1));
		// assertFalse(e1.equals(e));
		// // Autre libellé
		// e.libelle = "parce que";
		// assertFalse(e.equals(e1));
		// // Même libellé
		// e1.libelle = "parce que";
		// assertTrue(e.equals(e1));
		//
		// // Ajouter un chèque
		// e1.cheque = 123456;
		// assertFalse(e.equals(e1));
		// assertFalse(e1.equals(e));
		// // Autre chèque
		// e.cheque = 98765;
		// assertFalse(e.equals(e1));
		// // Même chèque
		// e1.cheque = 98765;
		// assertTrue(e.equals(e1));
	}
}
