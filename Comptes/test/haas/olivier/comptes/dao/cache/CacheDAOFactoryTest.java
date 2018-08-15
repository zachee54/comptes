package haas.olivier.comptes.dao.cache;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import haas.olivier.comptes.Banque;
import haas.olivier.comptes.Compte;
import haas.olivier.comptes.Ecriture;
import haas.olivier.comptes.Permanent;
import haas.olivier.comptes.PermanentFixe;
import haas.olivier.comptes.TypeCompte;
import haas.olivier.diagram.DiagramMemento;
import haas.olivier.util.Month;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class CacheDAOFactoryTest {

	/**
	 * Source de données mockée.
	 */
	private static final CacheableDAOFactory cacheable =
			mock(CacheableDAOFactory.class);
	
	/**
	 * Une banque mockée.
	 */
	private static final Banque BANQUE = mock(Banque.class);
	
	private static final Compte COMPTE1 = new Compte(1, TypeCompte.DEPENSES);
	private static final Compte COMPTE2 =
			new Compte(2, TypeCompte.COMPTE_COURANT);
	
	private static Ecriture e;
	
	private static final Permanent PERMANENT =
			new PermanentFixe(1, "permanent", COMPTE1, COMPTE2, "permanent", "tiers", false, new HashMap<Month, Integer>(), new HashMap<Month, BigDecimal>());
	
	private static final Month MONTH = Month.getInstance();
	
	private static final Solde HISTO = new Solde(
			MONTH.getPrevious(), COMPTE2, BigDecimal.TEN);
	private static final Solde SOLDE_A_VUE = new Solde(
			MONTH.getNext(), mock(Compte.class), BigDecimal.TEN.negate());
	private static final Solde MOYENNE = new Solde(
			MONTH, COMPTE1, BigDecimal.ONE);
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		e = new Ecriture(1, new Date(), null, COMPTE1, COMPTE2, BigDecimal.ONE, "ecriture", "tiers", null);
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}
	
	/**
	 * Objet testé.
	 */
	private CacheDAOFactory factory;
	
	/**
	 * Un DAO de bas niveau pour les propriétés.
	 */
	private final CacheablePropertiesDAO propsDAO =
			mock(CacheablePropertiesDAO.class);
	
	/**
	 * Un mock de memento de diagramme.
	 */
	private final DiagramMemento memento = mock(DiagramMemento.class);

	@Before
	public void setUp() throws Exception {
		when(cacheable.getBanques()).thenReturn(
				Collections.singleton(BANQUE).iterator());
		when(cacheable.getComptes()).thenReturn(
				Arrays.asList(COMPTE1, COMPTE2).iterator());
		when(cacheable.getEcritures()).thenReturn(
				Collections.singleton(e).iterator());
		when(cacheable.getPermanents(any(CachePermanentDAO.class))).thenReturn(
				Collections.singleton(PERMANENT).iterator());
		when(cacheable.getHistorique()).thenReturn(
				Collections.singleton(HISTO).iterator());
		when(cacheable.getSoldesAVue()).thenReturn(
				Collections.singleton(SOLDE_A_VUE).iterator());
		when(cacheable.getMoyennes()).thenReturn(
				Collections.singleton(MOYENNE).iterator());
		when(cacheable.getProperties()).thenReturn(propsDAO);
		
		Map<String, DiagramMemento> map = new HashMap<>();
		map.put("diagram", memento);
		when(propsDAO.getDiagramProperties()).thenReturn(map);
		
		// Objet testé
		factory = new CacheDAOFactory(cacheable);
	}

	@After
	public void tearDown() throws Exception {
	}

	/**
	 * Teste une instanciation à vide (aucune donnée source).
	 */
	@Test
	public void testCacheDAOFactory() throws IOException {
		factory = new CacheDAOFactory(null);
		assertFalse(factory.canBeSaved());
		assertFalse(factory.getBanqueDAO().getAll().iterator().hasNext());
		assertTrue(factory.getCompteDAO().getAll().isEmpty());
		assertFalse(factory.getEcritureDAO().getAll().iterator().hasNext());
		assertFalse(factory.getHistoriqueDAO().getAll().hasNext());
		assertFalse(factory.getMoyenneDAO().getAll().hasNext());
		assertFalse(factory.getSoldeAVueDAO().getAll().hasNext());
		assertFalse(factory.getPermanentDAO().getAll().iterator().hasNext());
		assertFalse(factory.getPropertiesDAO().getDiagramNames().iterator().hasNext());
		assertEquals("indéfini", factory.getName());
	}
	
	@Test
	public void testGetBanqueDAO() {
		
		// Méthode testée
		Iterator<Banque> banques = factory.getBanqueDAO().getAll().iterator();
		
		// Vérifier qu'il y a un élément unique : le bon
		assertTrue(banques.hasNext());
		assertSame(BANQUE, banques.next());
		assertFalse(banques.hasNext());
	}

	@Test
	public void testGetCompteDAO() throws IOException {
		
		// Méthode testée
		Iterator<Compte> comptes = factory.getCompteDAO().getAll().iterator();
		
		// Vérifier qu'il y a exactement les deux comptes
		Collection<Compte> liste = new ArrayList<Compte>(2);
		while (comptes.hasNext())
			liste.add(comptes.next());
		assertEquals(Arrays.asList(COMPTE1, COMPTE2), liste);
	}

	@Test
	public void testGetEcritureDAO() throws IOException {
		
		// Méthode testée
		Iterator<Ecriture> ecritures =
				factory.getEcritureDAO().getAll().iterator();
		
		// Vérifier qu'il n'y a qu'une écriture : la bonne
		assertTrue(ecritures.hasNext());
		assertEquals(e, ecritures.next());
		assertFalse(ecritures.hasNext());
	}

	@Test
	public void testGetPermanentDAO() throws IOException {
		
		// Méthode testée
		Iterator<Permanent> permanents =
				factory.getPermanentDAO().getAll().iterator();
		
		// Vérifier qu'il n'y a qu'une opération permanente : la bonne
		assertTrue(permanents.hasNext());
		assertEquals(PERMANENT, permanents.next());
		assertFalse(permanents.hasNext());
	}

	@Test
	public void testGetHistoriqueDAO() {
		assertEquals(HISTO.montant,
				factory.getHistoriqueDAO().get(HISTO.compte, HISTO.month));
	}

	@Test
	public void testGetSoldeAVueDAO() {
		assertEquals(
				SOLDE_A_VUE.montant,
				factory.getSoldeAVueDAO().get(
						SOLDE_A_VUE.compte, SOLDE_A_VUE.month));
	}

	@Test
	public void testGetMoyenneDAO() {
		assertEquals(MOYENNE.montant,
				factory.getMoyenneDAO().get(MOYENNE.compte, MOYENNE.month));
	}

	@Test
	public void testGetPropertiesDAO() {
		assertSame(memento,
				factory.getPropertiesDAO().getDiagramProperties("diagram"));
	}

	@Test
	public void testMustBeSaved() throws IOException {
		
		// Situation de départ
		assertFalse(factory.mustBeSaved());
		
		// Modification des banques
		Banque banqueMock = mock(Banque.class);
		when(banqueMock.getBytes()).thenReturn(new byte[] {0});
		factory.getBanqueDAO().add(banqueMock);
		checkMustBeSaved();
		
		// Modification des écritures
		factory.getEcritureDAO().update(e);
		checkMustBeSaved();
		
		// Modification des opérations permanentes
		factory.getPermanentDAO().update(PERMANENT);
		checkMustBeSaved();
		
		// Modification des propriétés
		factory.getPropertiesDAO().setDiagramProperties("diagram1", memento);
		checkMustBeSaved();
		
		/*
		 * NB: il n'est pas nécessaire de sauvegarder les suivis, puisqu'ils
		 * peuvent être recalculés par l'application à partir des écritures.
		 */
	}
	
	private void checkMustBeSaved() throws IOException {
		assertTrue(factory.mustBeSaved());
		factory.save();
		assertFalse(factory.mustBeSaved());
	}
	
	@Test
	public void testSave() throws IOException {
		factory.save();
		verify(cacheable).save(factory);
	}

	@Test
	public void testErase() throws IOException {
		
		// Méthode testée
		factory.erase();
		
		// Vérification
		assertFalse(factory.getBanqueDAO().getAll().iterator().hasNext());
		assertTrue(factory.getCompteDAO().getAll().isEmpty());
		assertFalse(factory.getEcritureDAO().getAll().iterator().hasNext());
		assertFalse(factory.getPermanentDAO().getAll().iterator().hasNext());
		assertFalse(factory.getHistoriqueDAO().getAll().hasNext());
		assertFalse(factory.getSoldeAVueDAO().getAll().hasNext());
		assertFalse(factory.getMoyenneDAO().getAll().hasNext());
		assertNotSame(memento,
				factory.getPropertiesDAO()
				.getDiagramProperties("diagram").getSeries());
	}

	@Test
	public void testGetDebut() {
		assertEquals(MONTH, factory.getDebut());
	}

	@Test
	public void testGetDebutNull() throws IOException {
		when(cacheable.getEcritures()).thenReturn(
				Collections.<Ecriture>emptyIterator());
		assertNotNull(new CacheDAOFactory(cacheable).getDebut());
	}
	
	@Test
	public void testGetSource() {
		String result = "source name test";
		when(cacheable.getSource()).thenReturn(result);
		assertEquals(result, factory.getSource());
	}
	
	@Test
	public void testGetSourceFullName() {
		String result = "source full name test";
		when(cacheable.getSourceFullName()).thenReturn(result);
		assertEquals(result, factory.getSourceFullName());
	}
}
