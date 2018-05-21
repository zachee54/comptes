package haas.olivier.comptes;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.TreeSet;

import haas.olivier.comptes.Compte;
import haas.olivier.comptes.Ecriture;
import haas.olivier.util.Month;
import haas.olivier.comptes.TypeCompte;
import haas.olivier.comptes.dao.DAOFactory;
import haas.olivier.comptes.dao.EcritureDAO;
import haas.olivier.comptes.gui.actions.CompteObservable;
import haas.olivier.comptes.gui.actions.DataObservable;
import haas.olivier.comptes.gui.actions.MonthObservable;
import haas.olivier.comptes.gui.table.EcrituresTableModel;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class EcrituresTableModelTest {

	private static Compte compte, compte2, compte3;
	private static Ecriture e0, e1, e2, e3, e4;
	private static TreeSet<Ecriture> ecrituresToutes, ecrituresMai;
	private static BigDecimal a, b, c;
	private static Month mai11, juin10;
	private static DataObservable dataObservable = mock(DataObservable.class);
	private static CompteObservable compteObservable = mock(CompteObservable.class);
	private static MonthObservable monthObservable = new MonthObservable();
	private static SimpleDateFormat df = new SimpleDateFormat("dd/MM/yy");
//	private static Integer[] disposition = { EcrituresTableModel.IDENTIFIANT,
//			EcrituresTableModel.DATE, EcrituresTableModel.DATE_POINTAGE,
//			EcrituresTableModel.POINTAGE, EcrituresTableModel.TIERS,
//			EcrituresTableModel.LIBELLE, EcrituresTableModel.CHEQUE,
//			EcrituresTableModel.MONTANT, EcrituresTableModel.COMPTE };

	private DAOFactory dao = mock(DAOFactory.class);
	private EcritureDAO eDAO = mock(EcritureDAO.class);

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		// Des données
		compte = new Compte(1, TypeCompte.COMPTE_CARTE);
		compte2 = new Compte(2, TypeCompte.DEPENSES);
		compte3 = new Compte(3, TypeCompte.RECETTES);

		a = new BigDecimal("657.13");
		b = new BigDecimal("18.20");
		c = new BigDecimal("-6.00");

		e0 = new Ecriture(0, df.parse("31/03/10"), null, compte, compte3, c,
				null, null, null);
		e1 = new Ecriture(1, df.parse("04/06/10"), null, compte, compte2, a,
				"libelleun", null, 1234567);
		e2 = new Ecriture(2, df.parse("06/05/11"), df.parse("30/05/11"),
				compte2, compte, b, "libelledeux", "tiersdeux", null);
		e3 = new Ecriture(3, df.parse("17/05/11"), df.parse("05/12/11"),
				compte3, compte, c, "libelletrois", "tierstrois", null);
		e4 = new Ecriture(4, df.parse("31/05/11"), null, compte2, compte, a,
				null, null, null);

		mai11 = new Month(df.parse("01/05/11"));
		juin10 = new Month(df.parse("01/06/10"));

		// Simuler en permanence la sélection du compte
		when(compteObservable.getCompte()).thenReturn(compte);
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
		// Des listes pour les mocks
		ecrituresToutes = new TreeSet<Ecriture>();
		ecrituresToutes.add(e0);
		ecrituresToutes.add(e1);
		ecrituresToutes.add(e2);
		ecrituresToutes.add(e3);
		ecrituresToutes.add(e4);
		
		ecrituresMai = new TreeSet<Ecriture>();
		ecrituresMai.add(e1);
		ecrituresMai.add(e2);
		ecrituresMai.add(e3);
		ecrituresMai.add(e4);

		// Les mocks DAO sont recrées à chaque test
		DAOFactory.setFactory(dao, false);
		when(dao.getEcritureDAO()).thenReturn(eDAO);

		when(eDAO.getAllSince(juin10)).thenReturn(ecrituresMai);
		when(eDAO.getAllTo(mai11)).thenReturn(ecrituresToutes.descendingSet());
	}// setUp

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testGetMontantAt() {
		monthObservable.setMonth(mai11);
		EcrituresTableModel model = new EcrituresTableModel(monthObservable,
				compteObservable, dataObservable);
		model.update();
		assertEquals(0, BigDecimal.ZERO.compareTo(model.getMontantAt(0)));
		assertEquals(0, a.compareTo(model.getMontantAt(1)));
		assertEquals(0, c.compareTo(model.getMontantAt(2)));
		assertEquals(0, b.compareTo(model.getMontantAt(3)));
		assertEquals(0, a.negate().compareTo(model.getMontantAt(4)));
	}
}
