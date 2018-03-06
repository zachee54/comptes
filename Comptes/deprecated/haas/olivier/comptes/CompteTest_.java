package haas.olivier.comptes;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import haas.olivier.comptes.Compte;
import haas.olivier.comptes.CompteBancaire;
import haas.olivier.comptes.CompteBudget;
import haas.olivier.comptes.Ecriture;
import haas.olivier.util.Month;
import haas.olivier.comptes.TypeCompte;
import haas.olivier.comptes.dao.CompteDAO;
import haas.olivier.comptes.dao.DAOFactory;
import haas.olivier.comptes.dao.EcritureDAO;
import haas.olivier.comptes.dao.SuiviDAO;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.NavigableSet;
import java.util.TreeSet;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class CompteTest_ {

	private static BigDecimal zero, a, b;
	private static Month month1, month2, month3, avril, mai;
	private static DateFormat parser;
	private static Date date1, date2, date3, date4;
	private Ecriture e1, e2, e3, e4, e5;
	private Compte c1, c2;
	private CompteBancaire banque;
	private Compte budget;
	private DAOFactory factory;
	private CompteDAO cDAO;
	private SuiviDAO hDAO, sDAO, mDAO;
	private EcritureDAO eDAO;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		parser = new SimpleDateFormat("dd/MM/yy");

		// Des données pêle-mêle
		month1 = new Month(parser.parse("26/10/04"));
		month2 = new Month(parser.parse("11/11/04"));
		month3 = new Month(parser.parse("13/12/04"));
		a = new BigDecimal("14595.03");
		b = new BigDecimal("-487.5");
		zero = new BigDecimal("0");

		date1 = parser.parse("01/05/13");
		date2 = parser.parse("04/05/13");
		date3 = parser.parse("07/05/13");
		date4 = parser.parse("31/05/13");
		mai = new Month(date1);
		avril = mai.getPrevious();
	}// setUpBeforeClass

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
		mock(SuiviDAO.class);
		eDAO = mock(EcritureDAO.class);

		// DAOFactory mockée
		factory = mock(DAOFactory.class);
		when(factory.getCompteDAO()).thenReturn(cDAO);
		when(factory.getHistoriqueDAO()).thenReturn(hDAO);
		when(factory.getSoldeAVueDAO()).thenReturn(sDAO);
		when(factory.getMoyenneDAO()).thenReturn(mDAO);
		when(factory.getEcritureDAO()).thenReturn(eDAO);
		when(factory.getDebut()).thenReturn(new Month(parser.parse("01/09/2001")));

		DAOFactory.setFactory(factory, false);

		// Comptes testés
		budget = new CompteBudget(54, "Compte budget", TypeCompte.RECETTES);
		banque = new CompteBancaire(100, "Compte bancaire", 1L,
				TypeCompte.EMPRUNT);
		
//		// Définir la liste intégrale des comptes
//		HashSet<Compte> comptes = new HashSet<Compte>();
//		comptes.add(banque);
//		comptes.add(budget);
//		when(cDAO.getAll()).thenReturn(comptes);

		// Comptes mockés
		c1 = mock(Compte.class);
		c2 = mock(Compte.class);

		// Ecritures
		e1 = new Ecriture(null, date1, date2, banque, c1, new BigDecimal("1"),
				null, null, null);
		e2 = new Ecriture(null, date2, date2, c1, c2, new BigDecimal("-2"),
				null, null, null);
		e3 = new Ecriture(null, date2, date4, c2, banque, new BigDecimal("4"),
				null, null, null);
		e4 = new Ecriture(null, date3, null, banque, c2, new BigDecimal("-8"),
				null, null, null);
		e5 = new Ecriture(null, date4, null, c2, banque, new BigDecimal("16"),
				null, null, null);

		// Créer la liste des écritures depuis date1
		NavigableSet<Ecriture> ecritures = new TreeSet<>();
		ecritures.add(e5);
		ecritures.add(e4);
		ecritures.add(e3);
		ecritures.add(e2);
		ecritures.add(e1);
		when(eDAO.getAllSince(mai)).thenReturn(ecritures);

		// Créer la liste des écritures pointées depuis date1
		NavigableSet<Ecriture> pointees = new TreeSet<>(new Ecriture.SortPointages());
		pointees.add(e3);
		pointees.add(e2);
		pointees.add(e1);
		when(eDAO.getPointagesSince(mai)).thenReturn(pointees);
		
		// Créer la liste des écritures jusqu'à date3
		NavigableSet<Ecriture> toDate3 = new TreeSet<>();
		toDate3.add(e4);
		toDate3.add(e3);
		toDate3.add(e1);
		toDate3.add(e4);
		when(eDAO.getAllTo(mai)).thenReturn(toDate3);

		// Créer la liste des écritures pointées jusqu'à date3
		NavigableSet<Ecriture> pointagesDate3 = new TreeSet<>();
		pointagesDate3.add(e2);
		pointagesDate3.add(e1);
		when(eDAO.getPointagesTo(mai)).thenReturn(pointagesDate3);
	}// setUp

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testGetHistorique() {
		// CompteBudget ne redéfinit pas cette méthode

		BigDecimal solde = budget.getHistorique(month1);

		// Appel de la bonne méthode
		verify(hDAO).get(54, month1);

		// Le mock renvoie toujours null, donc on devrait avoir un solde zéro
		assertEquals("Solde nul si pas d'opération", 0, zero.compareTo(solde));
	}// testGetHistorique

	@Test
	public void testGetHistoriqueInPast() throws ParseException, IOException {
		// CompteBudget ne redéfinit pas cette méthode

		budget.addHistorique(month1, a);
		BigDecimal result = budget.getHistorique(month2);

		assertEquals("Solde non reporté sur les mois suivants", 0,
				zero.compareTo(result));
	}// testGetHistoriqueInPast

	@Test
	public void testAddHistorique() throws IOException {
		// CompteBancaire ne redéfinit pas cette méthode

		// Vérifier une bonne affectation à partir de rien
		banque.addHistorique(month1, a);
		verify(hDAO).set(100, month1, a);

		// Vérifier l'addition correcte d'une deuxième valeur
		when(hDAO.get(100, month1)).thenReturn(a);
		banque.addHistorique(month1, b);
		verify(hDAO).set(100, month1, a.add(b));

		// Vérifier que l'appel null ne lève pas d'exception
		banque.addHistorique(month1, null);

		/*
		 * Vérifier qu'il n'y a pas eu d'appel sur les autres mois, ni sur les
		 * appels null et zéro
		 */
		banque.addHistorique(month1, new BigDecimal("0"));
		verify(hDAO, times(2)).set(eq(100), (Month) anyObject(),
				(BigDecimal) anyObject());
	}// testAddHistorique

	@Test
	public void testRemoveHistoriqueFrom() throws IOException {

		// Mocker 2 valeurs
		when(hDAO.get(54, month1)).thenReturn(a);
		when(hDAO.get(54, month2)).thenReturn(b);

		// Méthode à tester
		Compte.removeSuiviFrom(month3);

		/*
		 * Vérifier qu'après l'effacement des données, les moyennes ont été
		 * redéfinies en tenant compte des historiques avant month3
		 */
//		InOrder inOrder = inOrder(hDAO, mDAO);
//		inOrder.verify(hDAO).removeFrom(month3);
//		inOrder.verify(mDAO).set(eq(54), eq(month3),
//				eq(new BigDecimal("1175.63")));
//
//		// Vérifier aussi le mois suivant...
//		verify(mDAO, atLeast(1)).set(eq(54), eq(month3.getNext()),
//				eq(new BigDecimal("1175.63")));
//
//		// ...le dernier pour la route...
//		verify(mDAO, atLeast(1)).set(eq(54), eq(month3.getTranslated(10)),
//				eq(new BigDecimal("-40.63")));
//
//		// ... et un trop loin pour être concerné
//		verify(mDAO, never()).set(eq(54), eq(month3.getTranslated(11)),
//				(BigDecimal) any());
	}// testRemoveHistoriqueFrom

	@Test
	public void testGetSoldeAVue() {
		// CompteBudget ne redéfinit pas cette méthode

		budget.getSoldeAVue(month1);
		verify(hDAO).get(54, month1);
	}// testGetSoldeAVue

	@Test
	public void testAddPointages() throws IOException {
		// CompteBudget ne redéfinit pas cette méthode

		budget.addPointages(month1, a);
		verifyNoMoreInteractions(cDAO);
	}// testAddPointages

	@Test
	public void testGetType() {
		assertEquals(TypeCompte.RECETTES, budget.getType());
		assertEquals(TypeCompte.EMPRUNT, banque.getType());
	}// testGetType

	@Test
	public void testGetImpactOf() {

		// Tester au crédit
		assertEquals(0, new BigDecimal("16").compareTo(banque.getImpactOf(e5)));

		// Tester au débit
		assertEquals(0, new BigDecimal("-1").compareTo(banque.getImpactOf(e1)));

		// Tester sans débit ni crédit
		assertEquals(0, zero.compareTo(banque.getImpactOf(e2)));
	}// testGetImpactOf

	@Test
	public void testGetHistoriqueAt() {
		// Prévoir le solde en début de mois (fin du mois précédent)
		when(hDAO.get(eq(100), eq(avril))).thenReturn(new BigDecimal("-32"));

		// Donner un solde bidon pour le mois suivant (pour vérifier que l'objet
		// va bien chercher le mois précédent)
		when(hDAO.get(eq(100), eq(mai))).thenReturn(new BigDecimal("64"));

		// Trouver le solde à date3
		assertEquals(0,
				new BigDecimal("-21").compareTo(banque.getHistoriqueAt(date3)));
	}// testGetHistoriqueAt

	@Test
	public void testGetSoldeAVueAt() {
		// Prévoir le solde à vue en début de mois (fin du mois précédent)
		when(sDAO.get(eq(100), eq(avril))).thenReturn(new BigDecimal("-64"));

		// Donner un solde à vue bidon pour le mois suivant (pour vérifier que
		// l'objet va bien chercher le mois précédent)
		when(sDAO.get(eq(100), eq(mai))).thenReturn(new BigDecimal("128"));

		// Trouver le solde à vue à date3
		assertEquals(0,
				new BigDecimal("-65").compareTo(banque.getSoldeAVueAt(date3)));
	}// testGetSoldeAVueAt
}
