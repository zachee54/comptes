package haas.olivier.comptes.dao.cache;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import haas.olivier.comptes.Banque;
import haas.olivier.comptes.Compte;
import haas.olivier.comptes.CompteBancaire;
import haas.olivier.comptes.CompteBudget;
import haas.olivier.comptes.Ecriture;
import haas.olivier.comptes.Permanent;
import haas.olivier.comptes.PermanentFixe;
import haas.olivier.comptes.TypeCompte;
import haas.olivier.comptes.dao.CompteDAO;
import haas.olivier.diagram.DiagramMemento;
import haas.olivier.util.Month;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class CacheDAOFactoryTest {

	/** Source de données mockée. */
	private static final CacheableDAOFactory cacheable =
			mock(CacheableDAOFactory.class);
	
	/** Une banque. */
	private static final Banque b = mock(Banque.class);//new Banque(1, "banque", new BufferedImage(1, 1, BufferedImage.TYPE_3BYTE_BGR), null);
	
	/** Deux comptes. */
	private static final Compte c1 = new CompteBudget(1, "compte", TypeCompte.DEPENSES),
			c2 = new CompteBancaire(2, "compte2", 0L, TypeCompte.COMPTE_COURANT);
	
	/** Une écriture. */
	private static Ecriture e;
	
	/** Une opération permanente. */
	private static final Permanent p = new PermanentFixe(1, "permanent", c1, c2, "permanent", "tiers", false, new HashMap<Month, Integer>(), new HashMap<Month, BigDecimal>());
	
	/** Un mois. */
	private static final Month month = new Month();
	
	/** Des suivis minimalistes. */
	private static Entry<Month, Entry<Integer, BigDecimal>> h =
			new SimpleImmutableEntry<Month, Entry<Integer, BigDecimal>>(month.getPrevious(), 
					new SimpleImmutableEntry<Integer, BigDecimal>(2, BigDecimal.TEN)),
			s = new SimpleImmutableEntry<Month, Entry<Integer, BigDecimal>>(month.getNext(),
					new SimpleImmutableEntry<Integer, BigDecimal>(0, BigDecimal.TEN.negate())),
			m = new SimpleImmutableEntry<Month, Entry<Integer, BigDecimal>>(month, 
					new SimpleImmutableEntry<Integer, BigDecimal>(1, BigDecimal.ONE));
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		e = new Ecriture(1, new Date(), null, c1, c2, BigDecimal.ONE, "ecriture", "tiers", null);
	}// setUpBeforeClass

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}
	
	/** Objet testé. */
	private CacheDAOFactory factory;
	
	/** Un DAO de bas niveau pour les propriétés. */
	private final CacheablePropertiesDAO propsDAO =
			mock(CacheablePropertiesDAO.class);
	
	/** Un mock de memento de diagramme. */
	private final DiagramMemento memento = mock(DiagramMemento.class);

	@Before
	public void setUp() throws Exception {
		when(cacheable.getBanques()).thenReturn(
				Collections.singleton(b).iterator());
		when(cacheable.getComptes()).thenReturn(
				Arrays.asList(c1, c2).iterator());
		when(cacheable.getEcritures((CompteDAO) any())).thenReturn(
				Collections.singleton(e).iterator());
		when(cacheable.getPermanents((CachePermanentDAO) any(), (CompteDAO) any())).thenReturn(
				Collections.singleton(p).iterator());
		when(cacheable.getHistorique()).thenReturn(
				Collections.singleton(h).iterator());
		when(cacheable.getSoldesAVue()).thenReturn(
				Collections.singleton(s).iterator());
		when(cacheable.getMoyennes()).thenReturn(
				Collections.singleton(m).iterator());
		when(cacheable.getProperties()).thenReturn(propsDAO);
		
		Map<String, DiagramMemento> map = new HashMap<>();
		map.put("diagram", memento);
		when(propsDAO.getDiagramProperties()).thenReturn(map);
		
		// Objet testé
		factory = new CacheDAOFactory(cacheable);
	}// setUp

	@After
	public void tearDown() throws Exception {
	}

	/** Teste une instanciation à vide (aucune donnée source). */
	@Test
	public void testCacheDAOFactory() throws IOException {
		factory = new CacheDAOFactory(null);
		assertFalse(factory.canBeSaved());
		assertFalse(factory.getBanqueDAO().getAll().iterator().hasNext());
		assertTrue(factory.getCompteDAO().getAll().isEmpty());
		assertFalse(factory.getEcritureDAO().getAll().iterator().hasNext());
		assertTrue(factory.getHistoriqueDAO().getAll().isEmpty());
		assertTrue(factory.getMoyenneDAO().getAll().isEmpty());
		assertTrue(factory.getSoldeAVueDAO().getAll().isEmpty());
		assertFalse(factory.getPermanentDAO().getAll().iterator().hasNext());
		assertFalse(factory.getPropertiesDAO().getDiagramNames().iterator().hasNext());
		assertEquals("indéfini", factory.getName());
	}// testCacheDAOFactory
	
	@Test
	public void testGetBanqueDAO() {
		
		// Méthode testée
		Iterator<Banque> banques = factory.getBanqueDAO().getAll().iterator();
		
		// Vérifier qu'il y a un élément unique : le bon
		assertTrue(banques.hasNext());
		assertSame(b, banques.next());
		assertFalse(banques.hasNext());
	}// testGetBanqueDAO

	@Test
	public void testGetCompteDAO() throws IOException {
		
		// Méthode testée
		Iterator<Compte> comptes = factory.getCompteDAO().getAll().iterator();
		
		// Vérifier qu'il y a exactement les deux comptes
		Collection<Compte> liste = new ArrayList<Compte>(2);
		while (comptes.hasNext())
			liste.add(comptes.next());
		assertEquals(Arrays.asList(c1, c2), liste);
	}// testGetCompteDAO

	@Test
	public void testGetEcritureDAO() throws IOException {
		
		// Méthode testée
		Iterator<Ecriture> ecritures = factory.getEcritureDAO().getAll().iterator();
		
		// Vérifier qu'il n'y a qu'une écriture : la bonne
		assertTrue(ecritures.hasNext());
		assertEquals(e, ecritures.next());
		assertFalse(ecritures.hasNext());
	}// testGetEcritures

	@Test
	public void testGetPermanentDAO() throws IOException {
		
		// Méthode testée
		Iterator<Permanent> permanents = factory.getPermanentDAO().getAll().iterator();
		
		// Vérifier qu'il n'y a qu'une opération permanente : la bonne
		assertTrue(permanents.hasNext());
		assertEquals(p, permanents.next());
		assertFalse(permanents.hasNext());
	}// testGetPermanents

	@Test
	public void testGetHistoriqueDAO() {
		assertEquals(h.getValue().getValue(),
				factory.getHistoriqueDAO().get(
						h.getValue().getKey(),
						h.getKey()));
	}// testGetHistoriqueDAO

	@Test
	public void testGetSoldeAVueDAO() {
		assertEquals(s.getValue().getValue(),
				factory.getSoldeAVueDAO().get(
						s.getValue().getKey(),
						s.getKey()));
	}// testGetSoldeAVueDAO

	@Test
	public void testGetMoyenneDAO() {
		assertEquals(m.getValue().getValue(),
				factory.getMoyenneDAO().get(
						m.getValue().getKey(),
						m.getKey()));
	}// testGetMoyenneDAO

	@Test
	public void testGetPropertiesDAO() {
		assertSame(memento,
				factory.getPropertiesDAO().getDiagramProperties("diagram"));
	}// testGetPropertiesDAO

	@Test
	public void testMustBeSaved() throws IOException {
		
		// Situation de départ
		assertFalse(factory.mustBeSaved());
		
		// Modification des banques
		Banque banqueMock = mock(Banque.class);
		when(banqueMock.getBytes()).thenReturn(new byte[] {0});
		factory.getBanqueDAO().add(banqueMock);//new Banque(null, b.nom, b.image, b.bin));
		checkMustBeSaved();
		
		// Modification des comptes
		factory.getCompteDAO().update(c1);
		checkMustBeSaved();
		
		// Modification des écritures
		factory.getEcritureDAO().update(e);
		checkMustBeSaved();
		
		// Modification des opérations permanentes
		factory.getPermanentDAO().update(p);
		checkMustBeSaved();
		
		// Modification des propriétés
		factory.getPropertiesDAO().setDiagramProperties("diagram1", memento);
		checkMustBeSaved();
		
		/* NB: il n'est pas nécessaire de sauvegarder les suivis, puisqu'ils
		 * peuvent être recalculés par l'application à partir des écritures.
		 */
	}// testMustBeSaved
	
	private void checkMustBeSaved() throws IOException {
		assertTrue(factory.mustBeSaved());
		factory.save();
		assertFalse(factory.mustBeSaved());
	}// checkMustBeSaved
	
	@Test
	public void testSave() throws IOException {
		factory.save();
		verify(cacheable).save(factory);
	}// testSave

	@Test
	public void testErase() throws IOException {
		
		// Méthode testée
		factory.erase();
		
		// Vérification
		assertFalse(factory.getBanqueDAO().getAll().iterator().hasNext());
		assertTrue(factory.getCompteDAO().getAll().isEmpty());
		assertFalse(factory.getEcritureDAO().getAll().iterator().hasNext());
		assertFalse(factory.getPermanentDAO().getAll().iterator().hasNext());
		assertTrue(factory.getHistoriqueDAO().getAll().isEmpty());
		assertTrue(factory.getSoldeAVueDAO().getAll().isEmpty());
		assertTrue(factory.getMoyenneDAO().getAll().isEmpty());
		assertNotSame(memento,
				factory.getPropertiesDAO().getDiagramProperties("diagram").getSeries());
	}// testErase

	@Test
	public void testGetDebut() {
		assertEquals(month, factory.getDebut());
	}// testGetDebut

	@Test
	public void testGetDebutNull() throws IOException {
		when(cacheable.getEcritures((CompteDAO) any())).thenReturn(
				Collections.<Ecriture>emptyIterator());
		assertNotNull(new CacheDAOFactory(cacheable).getDebut());
	}// testGetDebutNull
	
	@Test
	public void testGetSource() {
		String result = "source name test";
		when(cacheable.getSource()).thenReturn(result);
		assertEquals(result, factory.getSource());
	}// testGetSource
	
	@Test
	public void testGetSourceFullName() {
		String result = "source full name test";
		when(cacheable.getSourceFullName()).thenReturn(result);
		assertEquals(result, factory.getSourceFullName());
	}// testGetSourceFullName
}
