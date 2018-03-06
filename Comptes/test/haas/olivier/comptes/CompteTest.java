package haas.olivier.comptes;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.awt.Color;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map.Entry;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import haas.olivier.comptes.dao.DAOFactory;
import haas.olivier.comptes.dao.EcritureDAO;
import haas.olivier.comptes.dao.SuiviDAO;
import haas.olivier.util.Month;

public class CompteTest {

	/**
	 * Un parseur de dates.
	 */
	private static final DateFormat DF = new SimpleDateFormat("dd/MM/yy");
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Mock
	private DAOFactory factory;
	
	@Mock
	private SuiviDAO hDAO, sDAO, mDAO;
	
	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		
		when(factory.getHistoriqueDAO()).thenReturn(hDAO);
		when(factory.getSoldeAVueDAO()).thenReturn(sDAO);
		when(factory.getMoyenneDAO()).thenReturn(mDAO);
		
		DAOFactory.setFactory(factory, false);
	}

	@After
	public void tearDown() throws Exception {
	}

	/**
	 * Vérifie que la date d'ouverture, la date exacte de clôture et la couleur
	 * sont sans influcence sur le hashcode.
	 * 
	 * @throws ParseException
	 */
	@Test
	public void testHashCode() throws ParseException {
		Compte c1 = new Compte(TypeCompte.COMPTE_COURANT);
		Compte c2 = new Compte(TypeCompte.COMPTE_COURANT);
		c1.setOuverture(DF.parse("01/01/00"));
		c2.setOuverture(DF.parse("14/07/89"));
		c1.setCloture(DF.parse("25/12/07"));
		c2.setCloture(DF.parse("31/12/10"));
		c1.setColor(Color.BLACK);
		c2.setColor(Color.RED);
		
		assertEquals(c1.hashCode(), c2.hashCode());
	}

	@Test
	public void testRemoveSuiviFrom() throws IOException {
		Month month = new Month();
		
		Compte.removeSuiviFrom(month);
		verify(hDAO).removeFrom(month);
		verify(sDAO).removeFrom(month);
		verify(mDAO).removeFrom(month);
	}

	@Test
	public void testCompte() {
		Month month = new Month();
		when(factory.getDebut()).thenReturn(month);
		
		Compte c = new Compte(TypeCompte.ENFANTS);
		assertSame(TypeCompte.ENFANTS, c.getType());	// Type demandé
		assertEquals(month, c.getOuverture());			// Date par défaut
		assertNull(c.getCloture());						// Pas de clôture
		assertNotNull(c.getColor());					// Couleur quelconque
	}

	@Test
	public void testSetNom() {
		Compte c = new Compte(TypeCompte.DEPENSES_EN_EPARGNE);
		
		c.setNom("un nom");
		assertEquals("un nom", c.getNom());
	}

	@Test
	public void testSetNumeroBancaire() {
		Compte c = new Compte(TypeCompte.COMPTE_CARTE);
		
		c.setNumero(258L);
		assertEquals(Long.valueOf(258L), c.getNumero());
	}
	
	/**
	 * Les comptes budgétaires ignorent les numéros.
	 */
	@Test
	public void testSetNumeroBudget() {
		Compte c = new Compte(TypeCompte.RECETTES);
		c.setNumero(684L);
		assertNull(c.getNumero());
	}

	@Test
	public void testSetTypeBudget() {
		Compte c = new Compte(TypeCompte.EMPRUNT);
		c.setNumero(89L);
		
		c.setType(TypeCompte.DEPENSES_EN_EPARGNE);
		assertSame(TypeCompte.DEPENSES_EN_EPARGNE, c.getType());
		assertNull(c.getNumero());
	}
	
	@Test
	public void testSetTypeBancaire() {
		Compte c = new Compte(TypeCompte.RECETTES);
		c.setType(TypeCompte.ENFANTS);
		assertSame(TypeCompte.ENFANTS, c.getType());
		
		assertNull(c.getNumero());
		c.setNumero(3L);
		assertEquals(Long.valueOf(3L), c.getNumero());
	}

	@Test
	public void testSetOuverture() throws ParseException {
		Compte c = new Compte(TypeCompte.COMPTE_COURANT);
		Date date = DF.parse("05/12/78");
		
		c.setOuverture(date);
		assertEquals(date, c.getOuverture());
	}
	
	@Test
	public void testSetOuvertureNull() {
		Compte c = new Compte(TypeCompte.COMPTE_EPARGNE);
		try {
			c.setOuverture(null);
			fail("Devrait lever une exception");
		} catch (IllegalArgumentException e) {
		}
	}

	@Test
	public void testSetCloture() throws ParseException {
		Compte c = new Compte(TypeCompte.EMPRUNT);
		Date date = DF.parse("26/10/74");
		c.setCloture(date);
		assertEquals(date, c.getCloture());
	}

	@Test
	public void testSetColor() {
		Compte c = new Compte(TypeCompte.RECETTES);
		c.setColor(Color.GREEN);
		assertEquals(Color.GREEN, c.getColor());
	}

	@Test
	public void testGetHistoriqueBancaire() {
		Compte c = new Compte(TypeCompte.COMPTE_EPARGNE);
		Month month = new Month();
		Month past = month.getTranslated(-3);		// 3 mois plus tôt
		
		when(hDAO.get(c, past)).thenReturn(BigDecimal.TEN);
		assertEquals(0, BigDecimal.TEN.compareTo(c.getHistorique(month)));
		assertEquals(0, BigDecimal.TEN.compareTo(c.getHistorique(past)));
		assertEquals(0,
				BigDecimal.ZERO.compareTo(c.getHistorique(past.getPrevious())));
	}
	
	@Test
	public void testGetHistoriqueBudget() {
		Compte c = new Compte(TypeCompte.DEPENSES);
		Month month = new Month();
		
		when(hDAO.get(c, month)).thenReturn(BigDecimal.ONE);
		assertEquals(0, BigDecimal.ONE.compareTo(c.getHistorique(month)));
		assertEquals(0,
				BigDecimal.ZERO.compareTo(c.getHistorique(month.getNext())));
	}

	@Test
	public void testGetHistoriqueIn() throws ParseException, EcritureMissingArgumentException, InconsistentArgumentsException, IOException {
		Compte c = new Compte(TypeCompte.COMPTE_CARTE);
		Month month = new Month(DF.parse("01/02/2016"));	// Février 2016
		
		// Solde en fin de mois
		BigDecimal histo3 = new BigDecimal("415.2");
		when(hDAO.get(c, month)).thenReturn(histo3);
		
		// Des écritures
		EcritureDAO eDAO = mock(EcritureDAO.class);
		when(factory.getEcritureDAO()).thenReturn(eDAO);
		Compte c2 = mock(Compte.class);
		List<Ecriture> ecritures = new ArrayList<>();
		Date date2 = DF.parse("27/02/16");
		Date date1 = DF.parse("14/02/16");
		ecritures.add(new Ecriture(null, date2, null, c2, c, new BigDecimal("2.31"), null, null, null));
		ecritures.add(new Ecriture(null, date1, null, c, c2, new BigDecimal("89.7"), null, null, null));
		ecritures.add(new Ecriture(null, date1, null, c2, c, new BigDecimal("15"), null, null, null));
		ecritures.add(new Ecriture(null, DF.parse("31/01/16"), null, c, c2, BigDecimal.ONE, null, null, null));// Écriture à ignorer
		when(eDAO.getAllTo(month)).thenReturn(ecritures);
		
		// Vérifier
		BigDecimal histo2 = new BigDecimal("412.89");	// du 15 au 27/02
		BigDecimal histo1 = new BigDecimal("487.59");	// du 1er au 14/02
		for (Entry<Date, BigDecimal> dateSolde : c.getHistoriqueIn(month)) {
			Date date = dateSolde.getKey();
			if (date.before(date1)) {
				assertEquals(0, histo1.compareTo(dateSolde.getValue()));
			} else if (date.before(date2)) {
				assertEquals(0, histo2.compareTo(dateSolde.getValue()));
			} else {
				assertEquals(0, histo3.compareTo(dateSolde.getValue()));
			}
		}
	}

	@Test
	public void testGetSoldeAVue() {
		Compte c = new Compte(TypeCompte.COMPTE_COURANT);
		Month month = new Month();
		
		when(sDAO.get(c, month)).thenReturn(BigDecimal.ONE);
		assertEquals(0,
				BigDecimal.ZERO.compareTo(c.getSoldeAVue(month.getPrevious())));
		assertEquals(0,
				BigDecimal.ONE.compareTo(c.getSoldeAVue(month)));
		assertEquals(0,
				BigDecimal.ONE.compareTo(c.getSoldeAVue(month.getNext())));
	}

	@Test
	public void testGetSoldeAVueIn() throws ParseException, IOException, EcritureMissingArgumentException, InconsistentArgumentsException {
		Compte c = new Compte(TypeCompte.COMPTE_CARTE);
		Month month = new Month(DF.parse("01/02/2016"));	// Février 2016
		
		// Solde en fin de mois
		BigDecimal histo3 = new BigDecimal("415.2");
		when(sDAO.get(c, month)).thenReturn(histo3);
		
		// Des écritures
		EcritureDAO eDAO = mock(EcritureDAO.class);
		when(factory.getEcritureDAO()).thenReturn(eDAO);
		Compte c2 = mock(Compte.class);
		List<Ecriture> ecritures = new ArrayList<>();
		Date date2 = DF.parse("27/02/16");
		Date date1 = DF.parse("14/02/16");
		Date date0 = DF.parse("31/01/16");
		ecritures.add(new Ecriture(null, date1, date2, c2, c, new BigDecimal("2.31"), null, null, null));
		ecritures.add(new Ecriture(null, date1, date1, c, c2, new BigDecimal("89.7"), null, null, null));
		ecritures.add(new Ecriture(null, date0, date1, c2, c, new BigDecimal("15"), null, null, null));
		ecritures.add(new Ecriture(null, date0, date0, c, c2, BigDecimal.ONE, null, null, null));// Écriture à ignorer
		when(eDAO.getAllTo(month)).thenReturn(ecritures);
		
		// Vérifier
		BigDecimal histo2 = new BigDecimal("412.89");	// du 15 au 27/02
		BigDecimal histo1 = new BigDecimal("487.59");	// du 1er au 14/02
		for (Entry<Date, BigDecimal> dateSolde : c.getSoldeAVueIn(month)) {
			Date date = dateSolde.getKey();
			if (date.before(date1)) {
				assertEquals(0, histo1.compareTo(dateSolde.getValue()));
			} else if (date.before(date2)) {
				assertEquals(0, histo2.compareTo(dateSolde.getValue()));
			} else {
				assertEquals(0, histo3.compareTo(dateSolde.getValue()));
			}
		}
	}

	@Test
	public void testGetMoyenne() {
		Compte c = new Compte(TypeCompte.RECETTES);
		Month month = new Month();
		when(mDAO.get(c, month)).thenReturn(BigDecimal.TEN);
		assertEquals(0, BigDecimal.TEN.compareTo(c.getMoyenne(month)));
	}
	
	/**
	 * Ajoute un montant au solde bancaire théorique alors qu'il n'y avait pas
	 * encore de suivi pour ce compte sur ce mois.
	 * 
	 * @throws IOException
	 */
	@Test
	public void testAddHistoriqueBancaireFromNone() throws IOException {
		Compte c = new Compte(TypeCompte.COMPTE_EPARGNE);
		Month month = new Month();
		
		c.addHistorique(month, BigDecimal.ONE);
		verify(hDAO).set(c, month, BigDecimal.ONE);
	}

	/**
	 * Teste l'ajout d'un montant à un historique existant pour un compte
	 * bancaire.
	 */
	@Test
	public void testAddHistoriqueBancaire() throws IOException {
		testAddHistorique(new Compte(TypeCompte.ENFANTS));
	}
	
	/**
	 * Teste l'ajout d'un montant à un historique existant pour un compte
	 * budgétaire.
	 * <p>
	 * Comme pour les comptes bancaires, mais il existe une duplication de code
	 * donc on double le test.
	 * 
	 * @throws IOException
	 */
	@Test
	public void testAddHistoriqueBudget() throws IOException {
		testAddHistorique(new Compte(TypeCompte.RECETTES_EN_EPARGNE));
	}
	
	/**
	 * Teste l'ajout d'un montant à un historique existant.
	 * 
	 * @param c	Le compte à tester.
	 * 
	 * @throws IOException
	 */
	private void testAddHistorique(Compte c) throws IOException {
		Month month = new Month();
		when(hDAO.get(c, month)).thenReturn(new BigDecimal("15"));
		
		c.addHistorique(month, new BigDecimal("-25"));
		/* Attention parce que cette vérification utilise BigDecimal.equals(),
		 * qui exige non seulement l'égalité en valeur mais également en échelle
		 * des BigDecimal. Il faut donc être sûr que le nombre calculé par
		 * l'objet testé et le nombre attendu aient la même échelle... ici 0.
		 */
		verify(hDAO).set(eq(c), eq(month), eq(new BigDecimal("-10")));
	}
	
	@Test
	public void testAddHistoriqueNull() throws IOException {
		Compte c = new Compte(TypeCompte.COMPTE_CARTE);
		Month month = new Month();
		c.addHistorique(month, null);
		verifyZeroInteractions(hDAO);
	}

	@Test
	public void testAddHistoriqueZero() throws IOException {
		Compte c = new Compte(TypeCompte.COMPTE_COURANT);
		Month month = new Month();
		c.addHistorique(month, BigDecimal.ZERO);
		verifyZeroInteractions(hDAO);
	}
	
	@Test
	public void testAddPointagesBancaire() throws IOException {
		Compte c = new Compte(TypeCompte.COMPTE_COURANT);
		Month month = new Month();
		when(sDAO.get(c, month)).thenReturn(new BigDecimal("15"));
		
		c.addPointages(month, new BigDecimal("-25"));
		/* Attention parce que cette vérification utilise BigDecimal.equals(),
		 * qui exige non seulement l'égalité en valeur mais également en échelle
		 * des BigDecimal. Il faut donc être sûr que le nombre calculé par
		 * l'objet testé et le nombre attendu aient la même échelle... ici 0.
		 */
		verify(sDAO).set(eq(c), eq(month), eq(new BigDecimal("-10")));
	}
	
	@Test
	public void testAddPointagesBudget() throws IOException {
		new Compte(TypeCompte.DEPENSES_EN_EPARGNE).addPointages(
				new Month(), BigDecimal.ONE);
		verifyZeroInteractions(sDAO);
	}
	
	@Test
	public void testAddPointagesNull() throws IOException {
		Compte c = new Compte(TypeCompte.COMPTE_CARTE);
		Month month = new Month();
		c.addPointages(month, null);
		verifyZeroInteractions(sDAO);
	}
	
	@Test
	public void testAddPointagesZero() throws IOException {
		Compte c = new Compte(TypeCompte.COMPTE_COURANT);
		Month month = new Month();
		c.addPointages(month, BigDecimal.ZERO);
		verifyZeroInteractions(sDAO);
	}

	@Test
	public void testGetViewSignBancaire() {
		Compte bancaire = new Compte(TypeCompte.COMPTE_EPARGNE);
		Compte budget = new Compte(TypeCompte.DEPENSES);
		
		// Vu depuis le compte bancaire
		assertEquals(1, bancaire.getViewSign(budget, bancaire));
		assertEquals(-1, bancaire.getViewSign(bancaire, budget));
		assertEquals(0, bancaire.getViewSign(budget, budget));
		
		// Vu depuis le compte budgétaire
		assertEquals(1, budget.getViewSign(budget, bancaire));
		assertEquals(-1, budget.getViewSign(bancaire, budget));
		assertEquals(0, budget.getViewSign(bancaire, bancaire));
	}

	@Test
	public void testGetImpactOf() throws EcritureMissingArgumentException, InconsistentArgumentsException {
		Compte bancaire = new Compte(TypeCompte.COMPTE_CARTE);
		Compte budget = new Compte(TypeCompte.RECETTES);
		Compte cNeutre1 = new Compte(TypeCompte.DEPENSES);
		Compte cNeutre2 = new Compte(TypeCompte.ENFANTS);
		Date date = mock(Date.class);
		BigDecimal montant = new BigDecimal("8");
		BigDecimal inverse = montant.negate();
		Ecriture e1 = new Ecriture(null, date, null, budget, bancaire, montant, null, null, null);
		Ecriture e2 = new Ecriture(null, date, null, bancaire, budget, montant, null, null, null);
		Ecriture eNeutre = new Ecriture(null, date, null, cNeutre1, cNeutre2, BigDecimal.ONE, null, null, null);
		
		// Vu depuis le compte bancaire
		assertEquals(0, montant.compareTo(bancaire.getImpactOf(e1)));
		assertEquals(0, inverse.compareTo(bancaire.getImpactOf(e2)));
		assertEquals(0, bancaire.getImpactOf(eNeutre).signum());
		
		// Vu depuis le compte budgétaire (même chose...)
		assertEquals(0, montant.compareTo(budget.getImpactOf(e1)));
		assertEquals(0, inverse.compareTo(budget.getImpactOf(e2)));
		assertEquals(0, budget.getImpactOf(eNeutre).signum());
	}

	@Test
	public void testIsEpargne() {
		assertFalse(new Compte(TypeCompte.COMPTE_COURANT).isEpargne());
		assertTrue(new Compte(TypeCompte.COMPTE_EPARGNE).isEpargne());
		assertFalse(new Compte(TypeCompte.DEPENSES).isEpargne());
		assertTrue(new Compte(TypeCompte.RECETTES_EN_EPARGNE).isEpargne());
	}

	@Test
	public void testGetSituationCritique() {
		fail("Not yet implemented");
	}

	@Test
	public void testCompareTo() {
		fail("Not yet implemented");
	}

	@Test
	public void testEqualsObject() throws ParseException {
		Compte c = createReference();
		Compte c2;
		
		// Type différent
		c2 = createReference();
		c2.setType(TypeCompte.COMPTE_CARTE);
		assertFalse(c.equals(c2));
		
		// Nom différent
		c2 = createReference();
		c2.setNom("un autre nom");
		assertFalse(c.equals(c2));
		
		// Numéro différent
		c2 = createReference();
		c2.setNumero(15L);
		assertFalse(c.equals(c2));

		// Numéro null
		c2 = createReference();
		c2.setNumero(null);
		assertFalse(c.equals(c2));
		assertFalse(c2.equals(c));
		
		// Date d'ouverture différente
		c2 = createReference();
		c2.setOuverture(DF.parse("25/12/14"));
		assertFalse(c.equals(c2));
		
		// Date de clôture différente
		c2 = createReference();
		c2.setCloture(DF.parse("01/09/19"));
		assertTrue(c.equals(c2));
		
		// Pas de date de clôture
		c2 = createReference();
		c2.setCloture(null);
		assertFalse(c.equals(c2));
		assertFalse(c2.equals(c));
		
		// Couleur différente
		c2 = createReference();
		c2.setColor(Color.GREEN);
		assertFalse(c.equals(c2));
	}

	/**
	 * Crée un compte de référence pour les comparaisons.
	 * 
	 * @throws ParseException
	 */
	private Compte createReference() throws ParseException {
		Compte c = new Compte(TypeCompte.COMPTE_COURANT);
		c.setCloture(DF.parse("15/08/17"));
		c.setNom("mon compte");
		c.setNumero(64L);
		c.setColor(Color.BLUE);
		return c;
	}
	
	@Test
	public void testToStringBancaire() {
		Compte c = new Compte(TypeCompte.ENFANTS);
		c.setNom("un nom");
		c.setNumero(1789L);
		String s = c.toString();
		assertTrue(s.contains("un nom"));
		assertTrue((s.contains("1789")));
	}
	
	@Test
	public void testToStringBudget() {
		Compte c = new Compte(TypeCompte.DEPENSES_EN_EPARGNE);
		c.setNom("un nom");
		assertEquals("un nom", c.toString());
	}

}
