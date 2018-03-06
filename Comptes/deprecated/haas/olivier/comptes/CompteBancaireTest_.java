package haas.olivier.comptes;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import haas.olivier.comptes.Compte;
import haas.olivier.comptes.CompteBancaire;
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
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Map.Entry;
import java.util.TreeSet;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class CompteBancaireTest_ {

	private static BigDecimal zero, a, b, c;
	private static Month month1, month2, month3, thisMonth;
	private static Date today, hier, demain, apDemain;
	private static int mToday, mHier, mDemain, mApDemain;	// N° de mois
	private static DateFormat parser;
	private static Compte compte2, compte3;
	private CompteBancaire compte;
	private DAOFactory factory;
	private CompteDAO cDAO;
	private EcritureDAO eDAO;
	private SuiviDAO sDAO, hDAO, mDAO; // Soldes, historiques, moyennes

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {

		parser = new SimpleDateFormat("dd/MM/yy");

		// Des données pêle-mêle
		month1 = new Month(parser.parse("26/10/04"));
		month2 = new Month(parser.parse("11/11/04"));
		month3 = new Month(parser.parse("25/12/08"));
		a = new BigDecimal("80");
		b = new BigDecimal("100");
		c = new BigDecimal("-170");
		zero = new BigDecimal("0");
		
		compte2 = mock(Compte.class);
		compte3 = mock(Compte.class);
		
		// Ce mois-ci
		thisMonth = new Month();
		
		// Aujourd'hui
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		mToday = cal.get(Calendar.MONTH);
		today = cal.getTime();
		
		// Hier
		cal.add(Calendar.DAY_OF_MONTH, -1);
		mHier = cal.get(Calendar.MONTH);
		hier = cal.getTime();
		
		// Demain
		cal.add(Calendar.DAY_OF_MONTH, 2);
		mDemain = cal.get(Calendar.MONTH);
		demain = cal.getTime();
		
		// Après-demain
		cal.add(Calendar.DAY_OF_MONTH, 1);
		mApDemain = cal.get(Calendar.MONTH);
		apDemain = cal.getTime();
	}// setUpBeforeClass

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {

		// DAO mockés
		cDAO = mock(CompteDAO.class);
		eDAO = mock(EcritureDAO.class);
		hDAO = mock(SuiviDAO.class);
		sDAO = mock(SuiviDAO.class);
		mDAO = mock(SuiviDAO.class);

		// DAOFactory mockée
		factory = mock(DAOFactory.class);
		when(factory.getCompteDAO()).thenReturn(cDAO);
		when(factory.getEcritureDAO()).thenReturn(eDAO);
		when(factory.getHistoriqueDAO()).thenReturn(hDAO);
		when(factory.getSoldeAVueDAO()).thenReturn(sDAO);
		when(factory.getMoyenneDAO()).thenReturn(mDAO);
		when(factory.getDebut()).thenReturn(new Month(parser.parse("01/09/2001")));
		DAOFactory.setFactory(factory);

		// Compte testé
		compte = new CompteBancaire(12, "Compte bancaire testé", 10016856740L,
				TypeCompte.COMPTE_CARTE);
	}// setUp

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testGetHistorique() {

		BigDecimal solde = compte.getHistorique(month1);

		// Appel de la bonne méthode
		verify(hDAO).get(12, month1);

		// Le mock renvoie toujours null, donc on devrait avoir un solde zéro
		assertEquals("Solde nul si pas d'opération", 0, zero.compareTo(solde));
	}// testGetHistorique

	@Test
	public void testGetHistoriqueInPast() {

		// Simuler un solde sur month2
		when(hDAO.get(12, month2)).thenReturn(a);

		// Méthode testée
		BigDecimal result = compte.getHistorique(month3);
		assertEquals("Solde reporté sur les mois suivants", 0,
				a.compareTo(result));
	}// testGetHistoriqueInPast

	@Test
	public void testGetSoldeAVue() {
		// Simuler un solde sur month1
		when(hDAO.get(12, month1)).thenReturn(a);

		// Simuler un pointage sur month2
		when(sDAO.get(12, month2)).thenReturn(b);

		// Pas de solde à vue si pas pointée
		assertEquals(0, zero.compareTo(compte.getSoldeAVue(month1)));

		// Un solde à vue si pointage, même sans solde théorique
		assertEquals(0, b.compareTo(compte.getSoldeAVue(month2)));

		// Report en avant du solde à vue
		assertEquals(0, b.compareTo(compte.getSoldeAVue(month3)));
	}// testGetSoldeAVue

	@Test
	public void testGetSoldeAVueVide() {
		assertNotNull(compte.getSoldeAVue(new Month()));
	}// testGetSoldeAVueVide

	@Test
	public void testAddPointages() throws IOException {
		compte.addPointages(month1, a);
		verify(sDAO).set(12, month1, a);
	}// testAddPointages

	@Test
	public void testAddPointagesTwice() throws IOException {

		// Simuler un solde à vue préexistant
		when(sDAO.get(12, month1)).thenReturn(a);

		// Méthode à tester
		compte.addPointages(month1, b);

		// Vérifier qu'on a fait la somme
		verify(sDAO).set(12, month1, a.add(b));
	}// testAddPointagesTwice

	@Test
	public void testAddPointagesHistoOK() throws IOException {
		compte.addHistorique(month1, a);
		compte.addPointages(month1, b);

		verify(sDAO, never()).set(12, month1, a);
		verify(sDAO).set(12, month1, b);
	}// testAddPointagesHistoOK

	@Test
	public void testGetNumero() {
		assertEquals(10016856740L, compte.getNumero());
	}

	@Test
	public void testSetNumeroLong() {
		long num = 8522046L;
		compte.setNumero(num);
		assertEquals(8522046L, compte.getNumero());
	}// testSetNumeroLong

//	@Test
//	public void testSetNumeroString() {
//		String num = "886521056";
//		compte.setNumero(num);
//		assertEquals(num, compte.getNumero());
//	}

	/** Après la date du jour, il y a un creux et ça remonte. */
	@Test
	public void testGetSituationCritiqueSimple()
			throws IOException, EcritureMissingArgumentException, InconsistentArgumentsException {
		
		// Vérification qu'on n'a pas changé de mois
		if (mDemain != mToday) {
			return;
		}
		
		// Solde de départ
		/* 190 */when(hDAO.get(12, thisMonth.getPrevious())).thenReturn(new BigDecimal("190"));
		
		// Écritures utilisées
		/*  -80 */Ecriture e1 = new Ecriture(1, demain, null, compte, compte2, a, null, null, null);
		/* +100 */Ecriture e2 = new Ecriture(2, apDemain, null, compte2, compte, b, null, null,null);
		/*    0 */Ecriture e3 = new Ecriture(3, apDemain, null, compte3, compte2, c, null, null, null);
		
		// Renvoyer ces valeurs
		TreeSet<Ecriture> all = new TreeSet<Ecriture>();
		all.add(e1);
		all.add(e2);
		all.add(e3);
		when(eDAO.getAllSince(eq(thisMonth))).thenReturn(all);
		
		// Méthode testée
		Entry<Date,BigDecimal> crit = compte.getSituationCritique(thisMonth);
		
		assertTrue(crit.getKey().equals(demain));	// Date critique demain
		assertEquals(0, new BigDecimal("110").compareTo(crit.getValue()));// Solde critique exact
	}// testGetSituationCritiqueSimple
	
	/** Après la date du jour, il y a deux creux et le deuxième est plus grand.
	 */ 
	@Test
	public void testGetSituationCritique2Seuils()
			throws IOException, EcritureMissingArgumentException, InconsistentArgumentsException {
		
		// Vérification qu'on n'a pas changé de mois
		if (mApDemain != mToday) {
			return;
		}
		
		// Solde de départ
		/* 190 */when(hDAO.get(12, thisMonth.getPrevious())).thenReturn(new BigDecimal("190"));
		
		// Écritures utilisées
		/*  -80 */Ecriture e1 = new Ecriture(1, demain, null, compte, compte2, a, null, null, null);
		/* +100 */Ecriture e2 = new Ecriture(2, apDemain, null, compte2, compte, b, null, null,null);
		/* -170 */Ecriture e3 = new Ecriture(3, apDemain, null, compte3, compte, c, null, null, null);
		
		// Renvoyer ces valeurs
		TreeSet<Ecriture> all = new TreeSet<Ecriture>();
		all.add(e1);
		all.add(e2);
		all.add(e3);
		when(eDAO.getAllSince(eq(thisMonth))).thenReturn(all);
		
		// Méthode testée
		Entry<Date,BigDecimal> crit = compte.getSituationCritique(thisMonth);
		
		assertTrue(crit.getKey().equals(apDemain));	// Date critique après-demain
		assertEquals(0, new BigDecimal("40").compareTo(crit.getValue()));// Solde critique exact
	}// testGetSituationCritique2Seuils
	
	/** Après la date du jour, il y a un découvert puis un deuxième creux plus
	 * grand. Il faut renvoyer la date du premier découvert, mais le montant du
	 * plus grand. 
	 */
	@Test
	public void testGetSituationCritiqueDecouvert()
			throws IOException, EcritureMissingArgumentException, InconsistentArgumentsException {
		
		// Vérification qu'on n'a pas changé de mois
		if (mApDemain != mToday) {
			return;
		}
		
		// Solde de départ
		/*   20 */when(hDAO.get(12, thisMonth.getPrevious())).thenReturn(new BigDecimal("20"));
		
		// Écritures utilisées
		/*  -80 */Ecriture e1 = new Ecriture(1, demain, null, compte, compte2, a, null, null, null);
		/* +100 */Ecriture e2 = new Ecriture(2, apDemain, null, compte2, compte, b, null, null,null);
		/* -170 */Ecriture e3 = new Ecriture(3, apDemain, null, compte3, compte, c, null, null, null);
		
		// Renvoyer ces valeurs
		TreeSet<Ecriture> all = new TreeSet<Ecriture>();
		all.add(e1);
		all.add(e2);
		all.add(e3);
		when(eDAO.getAllSince(eq(thisMonth))).thenReturn(all);
		
		// Méthode testée
		Entry<Date,BigDecimal> crit = compte.getSituationCritique(thisMonth);
		
		assertTrue(crit.getKey().equals(demain));	// Date critique demain
		assertEquals(0, new BigDecimal("-130").compareTo(crit.getValue()));// Solde critique exact
	}// testGetSituationCritiqueDecouvert
	
	/** La situation ne fait que s'améliorer: c'est aujourd'hui la date
	 * critique.
	 */
	@Test
	public void testGetSituationCritiqueAujourdHui()
			throws IOException, EcritureMissingArgumentException, InconsistentArgumentsException {
		
		// Vérification qu'on n'a pas changé de mois (pas terrible terrible...)
		if (mDemain != mHier) {
			return;
		}
		
		// Solde de départ
		/* 110 */when(hDAO.get(12, thisMonth.getPrevious())).thenReturn(new BigDecimal("110"));
		
		// Écritures utilisées
		/*  +80 */Ecriture e1 = new Ecriture(1, hier, null, compte2, compte, a, null, null, null);
		/* +100 */Ecriture e2 = new Ecriture(2, demain, null, compte2, compte, b, null, null,null);
		/*    0 */Ecriture e3 = new Ecriture(3, apDemain, null, compte3, compte2, c, null, null, null);
		
		// Renvoyer ces valeurs
		TreeSet<Ecriture> all = new TreeSet<Ecriture>();
		all.add(e1);
		all.add(e2);
		all.add(e3);
		when(eDAO.getAllSince(eq(thisMonth))).thenReturn(all);
		
		// Méthode testée
		Entry<Date,BigDecimal> crit = compte.getSituationCritique(thisMonth);
		
		Date date = crit.getKey();
		assertFalse(date.after(today));			// Date critique au plus tard aujourd'hui
		assertEquals(0,
				/* Selon que "hier" fait partie du mois en cours ou du mois
				 * précédent, le montant de l'écriture d'hier se rajoute (ou
				 * pas) au solde du mois précédent.
				 */
				new BigDecimal(mDemain == mHier ? "190" : "110")
		.compareTo(crit.getValue()));			// Solde critique exact
	}// testGetSituationCritiqueAujourdhui
	
	/** Découvert aujourd'hui, puis ça s'améliore, puis plus grand découvert
	 * ensuite. 
	 */
	@Test
	public void testGetSituationCritiqueMalPuisPire()
			throws IOException, EcritureMissingArgumentException, InconsistentArgumentsException {
		
		// Vérification qu'on n'a pas changé de mois
		if (mApDemain != mHier) {
			return;
		}
		
		// Solde de départ
		/*   60 */when(hDAO.get(12, thisMonth.getPrevious())).thenReturn(new BigDecimal("60"));
		
		// Écritures utilisées
		/*  -80 */Ecriture e1 = new Ecriture(1, hier, null, compte, compte2, a, null, null, null);
		/* +100 */Ecriture e2 = new Ecriture(2, demain, null, compte2, compte, b, null, null,null);
		/* -170 */Ecriture e3 = new Ecriture(3, apDemain, null, compte3, compte, c, null, null, null);
		
		// Renvoyer ces valeurs
		TreeSet<Ecriture> all = new TreeSet<Ecriture>();
		all.add(e1);
		all.add(e2);
		all.add(e3);
		when(eDAO.getAllSince(eq(thisMonth))).thenReturn(all);
		
		// Méthode testée
		Entry<Date,BigDecimal> crit = compte.getSituationCritique(thisMonth);
		
		Date date = crit.getKey();
		assertFalse(date.after(today));			// Date critique au plus tard aujourd'hui
		assertEquals(0, new BigDecimal("-90").compareTo(crit.getValue()));// Solde critique exact
	}// testGetSituationCritiqueMalPuisPire
	
	/** Aucune écriture après aujourd'hui. */
	@Test
	public void testGetSituationCritiqueStable()
			throws IOException, EcritureMissingArgumentException, InconsistentArgumentsException {
		
		// Vérification qu'on n'a pas changé de mois
		if (mToday != mHier) {
			return;
		}
		
		// Solde de départ
		/*  110 */when(hDAO.get(12, thisMonth.getPrevious())).thenReturn(new BigDecimal("110"));
		
		// Des dates pour les écritures
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.HOUR, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		cal.set(Calendar.DAY_OF_MONTH, 2);		// 2 du mois en cours
		Date date1 = cal.getTime();
		cal.set(Calendar.DAY_OF_MONTH, 4);		// 4 du mois en cours
		Date date2 = cal.getTime();
		
		// Écritures utilisées
		/*  -80 */Ecriture e1 = new Ecriture(1, date1, null, compte, compte2, a, null, null, null);
		/*    0 */Ecriture e2 = new Ecriture(2, date2, null, compte2, compte3, b, null, null,null);
		
		// Renvoyer ces valeurs
		TreeSet<Ecriture> all = new TreeSet<Ecriture>();
		all.add(e1);
		all.add(e2);
		when(eDAO.getAllSince(eq(thisMonth))).thenReturn(
				// Ordre chronologique
				all.descendingSet());
		
		// Méthode testée
		Entry<Date,BigDecimal> crit = compte.getSituationCritique(thisMonth);
		
		Date date = crit.getKey();
		assertFalse(date.after(today));			// Date critique au plus tard aujourd'hui
		assertEquals(0, new BigDecimal("30").compareTo(crit.getValue()));// Solde critique exact
	}// testGetSituationCritiqueStable
	
	/** Absolument aucune écriture. */
	@Test
	public void testGetSituationCritiqueAucuneEcriture() throws IOException {
		
		Month prochain = thisMonth.getNext();
		
		// Solde de départ
		when(hDAO.get(12, thisMonth)).thenReturn(new BigDecimal("10"));
		
		// Les écritures à renvoyer (aucune)
		TreeSet<Ecriture> none = new TreeSet<Ecriture>();
		when(eDAO.getAllSince(prochain)).thenReturn(none);
		
		// Méthode testée
		Entry<Date,BigDecimal> crit = compte.getSituationCritique(prochain);
		
		Date date = crit.getKey();
		assertFalse(prochain.before(date));		// Date critique au plus tard au début du mois suivant
		assertEquals(0, new BigDecimal("10").compareTo(crit.getValue()));// Solde critique exact
	}// testGetSituationCritiqueAucuneEcriture
}
