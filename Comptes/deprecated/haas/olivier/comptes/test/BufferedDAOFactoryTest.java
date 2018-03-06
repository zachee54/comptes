package haas.olivier.comptes.test;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.lang.Integer;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import haas.olivier.comptes.Compte;
import haas.olivier.comptes.CompteBancaire;
import haas.olivier.comptes.CompteBudget;
import haas.olivier.comptes.Ecriture;
import haas.olivier.comptes.MessagesFactory;
import haas.olivier.util.Month;
import haas.olivier.comptes.Permanent;
import haas.olivier.comptes.TypeCompte;
import haas.olivier.comptes.dao.buffer.BufferableCompteDAO;
import haas.olivier.comptes.dao.buffer.BufferableDAOFactory;
import haas.olivier.comptes.dao.buffer.BufferableEcritureDAO;
import haas.olivier.comptes.dao.buffer.BufferablePermanentDAO;
import haas.olivier.comptes.dao.buffer.BufferableSuiviDAO;
import haas.olivier.comptes.dao.buffer.BufferedDAOFactory;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class BufferedDAOFactoryTest {

	private BufferedDAOFactory dao;
	private BufferableDAOFactory subdao;
	private BufferableCompteDAO cDAO;
	private BufferableEcritureDAO eDAO;
	private BufferablePermanentDAO pDAO;
	private BufferableSuiviDAO sDAO;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		// Désactiver les messages intempestifs
		MessagesFactory.setFactory(mock(MessagesFactory.class));
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
		cDAO = mock(BufferableCompteDAO.class);
		eDAO = mock(BufferableEcritureDAO.class);
		pDAO = mock(BufferablePermanentDAO.class);
		sDAO = mock(BufferableSuiviDAO.class);
		subdao = mock(BufferableDAOFactory.class);
		when(subdao.getCompteDAO()).thenReturn(cDAO);
		when(subdao.getEcritureDAO()).thenReturn(eDAO);
		when(subdao.getPermanentDAO()).thenReturn(pDAO);
		when(subdao.getHistoriqueDAO()).thenReturn(sDAO);
		when(subdao.getSoldeAVueDAO()).thenReturn(sDAO);
		when(subdao.getMoyenneDAO()).thenReturn(sDAO);

		dao = new BufferedDAOFactory(subdao);
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testMustBeSaved() throws IOException {
		// Test de départ
		assertFalse(dao.mustBeSaved());

		// Test après une modif
		Compte compte = new CompteBudget(1, "a", TypeCompte.DEPENSES);
		dao.getCompteDAO().add(compte);
		assertTrue(dao.mustBeSaved());

		// Test après flush
		dao.save();
		assertFalse(dao.mustBeSaved());

		// Test après autre modif
		Ecriture ecriture = new Ecriture(1, new Date(), new Date(),
				mock(Compte.class), mock(Compte.class), new BigDecimal(1), "",
				"", 1);
		dao.getEcritureDAO().add(ecriture);
		assertTrue(dao.mustBeSaved());
		dao.save();
		assertFalse(dao.mustBeSaved());

		// Test après autre modif
		dao.getHistoriqueDAO().set(1, new Month(), new BigDecimal("1"));
		assertTrue(dao.mustBeSaved());
		dao.save();
		assertFalse(dao.mustBeSaved());

		// Test après autre modif
		Permanent permanent = new Permanent(2, "iojub",
				mock(CompteBancaire.class), mock(Compte.class),
				new HashMap<Month, Integer>());
		dao.getPermanentDAO().add(permanent);
		assertTrue(dao.mustBeSaved());
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testSave() throws IOException {
		// Neutraliser les messages
		MessagesFactory.setFactory(mock(MessagesFactory.class));
		
		dao.save();

		verify(cDAO).save((Map<Integer, Compte>) any(),
				(Map<Integer, Compte>) any(), (Set<Integer>) any());
		verify(eDAO).save((Map<Integer, Ecriture>) any(),
				(Map<Integer, Ecriture>) any(), (Set<Integer>) any());
		verify(pDAO).save((Map<Integer, Permanent>) any(),
				(Map<Integer, Permanent>) any(), (Set<Integer>) any());
		verify(sDAO, times(3)).save((Month) any(),
				(Map<Month, Map<Integer, BigDecimal>>) any());
	}
}