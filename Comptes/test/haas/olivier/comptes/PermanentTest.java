package haas.olivier.comptes;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import haas.olivier.comptes.Compte;
import haas.olivier.comptes.Ecriture;
import haas.olivier.util.Month;
import haas.olivier.comptes.Permanent;
import java.math.BigDecimal;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class PermanentTest {

	private static SimpleDateFormat df;
	private static Month avril, mai, juin, juillet, aout;
	private static Map<Month, Integer> jours;
	private static Compte debit, credit;
	private static String tiers, libelle;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		df = new SimpleDateFormat("dd/MM/yy");
		avril = new Month(df.parse("01/04/12"));
		mai = avril.getNext();
		juin = mai.getNext();
		juillet = juin.getNext();
		aout = juillet.getNext();

		// Les dates
		jours = new HashMap<Month, Integer>();
		jours.put(avril, 15);
		jours.put(mai, 16);
		jours.put(juin, 32);
		jours.put(juillet, 18);

		// Divers
		debit = mock(Compte.class);
		credit = mock(Compte.class);
		tiers = "un tiers";
		libelle = "un libelle";
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
	public void testCreateEcritureMontantsPredefinis() throws Exception {
		
		// Les montants
		Map<Month, BigDecimal> montants = new HashMap<Month, BigDecimal>();
		montants.put(avril, new BigDecimal("69"));
		montants.put(mai, new BigDecimal("23"));
		montants.put(juillet, new BigDecimal("-523.1"));

		Ecriture e = new Ecriture(null, df.parse("02/07/12"), null, debit,
				credit, new BigDecimal("23"), libelle, tiers, null);

		Permanent perm = new PermanentFixe(7, "", debit, credit, libelle, tiers,
				false, jours, montants);
		assertEquals(e, perm.createEcriture(juin));
	}

	@Test
	public void testCreateEcritureCompteASolder() throws Exception {
		Compte compteASolder = mock(Compte.class);
		BigDecimal montant = new BigDecimal("456.78");
		when(compteASolder.getSoldeAVue(aout)).thenReturn(montant);

		Ecriture e = new Ecriture(null, df.parse("18/09/12"), null,
				compteASolder, credit, montant, libelle, tiers, null);

		Permanent perm = new PermanentSoldeur(7, "", compteASolder, credit,
				libelle, tiers, false, jours);
		assertEquals(e, perm.createEcriture(aout.getNext()));
	}

	@Test
	public void testCreateEcritureDependance() throws Exception {
		Permanent dependance = mock(Permanent.class);
		BigDecimal montant = new BigDecimal("15031.84");
		Ecriture ecritureDepend = new Ecriture(null, mock(Date.class), null,
				mock(Compte.class), mock(Compte.class), montant, null, null,
				null);
		when(dependance.createEcriture(avril)).thenReturn(ecritureDepend);
		when(dependance.getMontant(avril)).thenReturn(montant);

		Ecriture e = new Ecriture(null, df.parse("15/04/12"), null, debit,
				credit, new BigDecimal("302.14"), libelle, tiers, null);

		// Créer une dépendance avec un taux de 2,01 %
		Permanent perm = new PermanentProport(7, "", debit, credit, libelle,
				tiers, false, jours, dependance, new BigDecimal("2.01"));
		assertEquals(e, perm.createEcriture(avril));
	}

	@Test
	public void testCreateEcritureThrowException() {
		PermanentFixe perm = new PermanentFixe(7, "", debit, credit, libelle,
				tiers, false, jours, new HashMap<Month, BigDecimal>());

		// Aucun montant
		perm.montants.put(mai, new BigDecimal("5"));
		try {
			perm.createEcriture(avril);
			fail("Doit lever une exception");
		} catch (Exception e2) {
		}

		// Aucune date
		try {
			perm.createEcriture(avril.getPrevious());
			fail("Doit lever une exception");
		} catch (Exception e1) {
		}
	}
}
