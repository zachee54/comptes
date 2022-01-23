package haas.olivier.comptes.dao.mysql;

import static org.junit.Assert.*;

import java.awt.Color;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.sql.DataSource;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mariadb.jdbc.MariaDbDataSource;
import static org.mockito.Mockito.*;

import haas.olivier.comptes.Compte;
import haas.olivier.comptes.Ecriture;
import haas.olivier.comptes.EcritureMissingArgumentException;
import haas.olivier.comptes.InconsistentArgumentsException;
import haas.olivier.comptes.Permanent;
import haas.olivier.comptes.PermanentFixe;
import haas.olivier.comptes.PermanentProport;
import haas.olivier.comptes.PermanentSoldeur;
import haas.olivier.comptes.TypeCompte;
import haas.olivier.comptes.dao.CompteDAO;
import haas.olivier.comptes.dao.DAOFactory;
import haas.olivier.comptes.dao.EcritureDAO;
import haas.olivier.comptes.dao.PropertiesDAO;
import haas.olivier.comptes.dao.cache.CacheDAOFactory;
import haas.olivier.comptes.dao.cache.CachePermanentDAO;
import haas.olivier.comptes.dao.cache.CacheSuiviDAO;
import haas.olivier.comptes.dao.cache.CacheablePropertiesDAO;
import haas.olivier.diagram.DiagramMemento;
import haas.olivier.util.Month;

public class MySqlDAOTest {
	
	private static final String DATABASE = "comptes_mysqldao_test";
	
	/**
	 * Source de données utilisée uniquement pour créer et supprimer la base de
	 * tests.
	 */
	private static final DataSource DATASOURCE =
			new MariaDbDataSource("localhost", 3306, null);
	
	private static final String USERNAME = "comptes_mysqldao_test";
	private static final String PASSWORD = "dummypassword";
	
	private static Compte compte1, compte2;
	private static Ecriture ecriture1, ecriture2;
	private static Permanent permanent1, permanent2, permanent3;
	private static DiagramMemento memento1, memento2, memento3;
	private static String diagram1 = "diagramme n°1",
			diagram2 = "diagramme n°2",
			diagram3 = "Diagramme n°3";

	/** Objet testé. */
	private MySqlDAO dao;
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		try (Connection connection = DATASOURCE.getConnection(USERNAME, PASSWORD);
				Statement statement = connection.createStatement()) {
			statement.execute("DROP DATABASE IF EXISTS " + DATABASE);
		}
		
		DateFormat df = new SimpleDateFormat("dd/MM/yy");
		Date date1 = df.parse("14/07/2001");
		Date date2 = df.parse("06/05/2004");
		Date date3 = df.parse("21/04/2006");
		Date date4 = df.parse("25/11/2006");
		Date date5 = df.parse("29/12/2006");
		
		compte1 = new Compte(0, TypeCompte.COMPTE_EPARGNE);
		compte1.setNom("Le compte 1");
		compte1.setNumero(57931L);
		compte1.setOuverture(date1);
		compte1.setColor(Color.CYAN);
		
		compte2 = new Compte(7, TypeCompte.RECETTES_EN_EPARGNE);
		compte2.setNom("Le compte n°7");
		compte2.setOuverture(date2);
		compte2.setCloture(date5);
		compte2.setColor(Color.ORANGE);
		
		ecriture1 = new Ecriture(
				3,
				date3,
				date4,
				compte1,
				compte2,
				BigDecimal.ONE,
				"libellé 1",
				null,
				null);
		
		ecriture2 = new Ecriture(
				31,
				date4,
				null,
				compte2,
				compte1,
				BigDecimal.TEN,
				null,
				"tiers 2",
				3544782);
		
		Map<Month, Integer> jours1 = new HashMap<>();
		jours1.put(Month.getInstance(2004, 7), 15);
		jours1.put(Month.getInstance(2008, 12), -2);
		permanent1 = new Permanent(
				3,
				"Permanent n°3",
				compte1,
				compte2,
				"Libellé permanent 3",
				null,
				false,
				jours1);
		Map<Month, BigDecimal> montants = new HashMap<>();
		montants.put(Month.getInstance(2010, 9), new BigDecimal("11.3"));
		montants.put(Month.getInstance(2010, 10), BigDecimal.TEN);
		permanent1.setState(new PermanentFixe(montants));
		
		Map<Month, Integer> jours2 = new HashMap<>();
		jours2.put(Month.getInstance(2012, 1), 0);
		jours2.put(Month.getInstance(2012, 2), 1);
		permanent2 = new Permanent(
				4,
				"Opération permanente n°4",
				compte2,
				compte1,
				null,
				"Tiers du n°4",
				true,
				jours2);
		permanent2.setState(new PermanentSoldeur(permanent2));
		
		Map<Month, Integer> jours3 = new HashMap<>();
		jours3.put(Month.getInstance(2015, 12), 5);
		jours3.put(Month.getInstance(2016, 1), 31);
		jours3.put(Month.getInstance(2016, 2), 3);
		permanent3 = new Permanent(
				10,
				"Permanent 10",
				compte1,
				compte2,
				"Libellé du 10",
				"Tiers du 10",
				false,
				jours3);
		permanent3.setState(new PermanentProport(permanent1, new BigDecimal("0.5")));
		
		memento1 = new DiagramMemento(
				diagram1,
				List.of(4, 3, 2, 9, 11, 7),
				Set.of(2, 4, 7));
		memento2 = new DiagramMemento(
				diagram2,
				List.of(14, 5, 10, 7),
				Set.of());
		memento3 = new DiagramMemento(
				diagram3,
				List.of(54, 0, 42, 2, 15, 9),
				Set.of(15, 2, 0));
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		try (Connection connection = DATASOURCE.getConnection(USERNAME, PASSWORD);
				Statement statement = connection.createStatement()) {
			statement.execute("DROP DATABASE IF EXISTS " + DATABASE);
		}
	}

	@Before
	public void setUp() throws Exception {
		dao = new MySqlDAO("localhost", 3306, DATABASE, USERNAME, PASSWORD);
	}

	@After
	public void tearDown() throws Exception {
	}
	
	/**
	 * Crée un mock de CacheDAOFactory.
	 * 
	 * @return	Un mock complet.
	 * 
	 * @throws IOException
	 */
	private CacheDAOFactory createCacheDAO() throws IOException {
		CompteDAO compteDAO = createCompteDAO();
		EcritureDAO ecritureDAO = createEcritureDAO();
		CachePermanentDAO permanentDAO = createPermanentDAO();
		CacheSuiviDAO histoDAO = mock(CacheSuiviDAO.class);
		PropertiesDAO propertiesDAO = createPropertiesDAO();
		
		CacheDAOFactory cache = mock(CacheDAOFactory.class);
		when(cache.getCompteDAO()).thenReturn(compteDAO);
		when(cache.getEcritureDAO()).thenReturn(ecritureDAO);
		when(cache.getPermanentDAO()).thenReturn(permanentDAO);
		when(cache.getHistoriqueDAO()).thenReturn(histoDAO);
		when(cache.getPropertiesDAO()).thenReturn(propertiesDAO);
		
		return cache;
	}
	
	/**
	 * Crée un mock renvoyant les comptes {@link #compte1} et {@link #compte2}.
	 * 
	 * @return	Un Mock.
	 * 
	 * @throws IOException
	 */
	private static CompteDAO createCompteDAO() throws IOException {
		CompteDAO compteDAO = mock(CompteDAO.class);
		when(compteDAO.getAll()).thenReturn(
				List.of(compte1, compte2));
		return compteDAO;
	}
	
	/**
	 * Crée un mock renvoyant les écritures {@link #ecriture1} et
	 * {@link #ecriture2}.
	 * 
	 * @return	Un Mock.
	 * 
	 * @throws IOException
	 */
	private static EcritureDAO createEcritureDAO() throws IOException {
		EcritureDAO ecritureDAO = mock(EcritureDAO.class);
		when(ecritureDAO.getAll()).thenReturn(
				List.of(ecriture1, ecriture2));
		return ecritureDAO;
	}
	
	/**
	 * Crée un mock renvoyant des écritures permanentes.
	 * 
	 * @return	Un Mock.
	 * 
	 * @throws IOException
	 */
	private static CachePermanentDAO createPermanentDAO() throws IOException {
		CachePermanentDAO permanentDAO = mock(CachePermanentDAO.class);
		when(permanentDAO.getAll()).thenReturn(
				List.of(permanent1, permanent2, permanent3));
		return permanentDAO;
	}
	
	/**
	 * Crée un mock renvoyant des propriétés.
	 * 
	 * @return	Un Mock.
	 */
	private static PropertiesDAO createPropertiesDAO() {
		PropertiesDAO propertiesDAO = mock(PropertiesDAO.class);
		when(propertiesDAO.getDiagramNames()).thenReturn(
				List.of(diagram1, diagram2, diagram3));
		when(propertiesDAO.getDiagramProperties(diagram1)).thenReturn(memento1);
		when(propertiesDAO.getDiagramProperties(diagram2)).thenReturn(memento2);
		when(propertiesDAO.getDiagramProperties(diagram3)).thenReturn(memento3);
		return propertiesDAO;
	}

	@Test
	public void testGetBanques() throws IOException {
		assertFalse(dao.getBanques().hasNext());
	}

	@Test
	public void testGetComptes() throws IOException {
		CacheDAOFactory cache = createCacheDAO();
		
		List<Compte> comptes = new ArrayList<>();
		comptes.addAll(cache.getCompteDAO().getAll());
		
		dao.save(cache);
		
		// Méthode testée
		Iterator<Compte> comptesIt = dao.getComptes();
		
		while (comptesIt.hasNext()) {
			assertTrue(comptes.remove(comptesIt.next()));
		}
		assertTrue(comptes.isEmpty());
	}

	@Test
	public void testGetEcritures() throws EcritureMissingArgumentException, InconsistentArgumentsException, IOException {
		dao.save(createCacheDAO());
		
		// Méthode testée
		Iterator<Ecriture> ecrituresIterator = dao.getEcritures();
		
		checkOriginalEcritures(ecrituresIterator);
	}
	
	private void checkOriginalEcritures(Iterator<Ecriture> ecrituresIterator) {	
		List<Ecriture> ecritures = new ArrayList<>(2);
		ecritures.add(ecriture1);
		ecritures.add(ecriture2);
		
		while (ecrituresIterator.hasNext()) {
			assertTrue(ecritures.remove(ecrituresIterator.next()));
		}
		assertTrue(ecritures.isEmpty());
	}
	
	@Test
	public void testGetPermanents() throws IOException {
		CacheDAOFactory cache = createCacheDAO();
		dao.save(cache);
		
		// Méthode testée
		Iterator<Permanent> permanentsIterator =
				dao.getPermanents(cache.getPermanentDAO());
		
		List<Permanent> permanents =
				new ArrayList<>(cache.getPermanentDAO().getAll());
		while (permanentsIterator.hasNext()) {
			assertTrue(permanents.remove(permanentsIterator.next()));
		}
		assertTrue(permanents.isEmpty());
	}
	
	@Test
	public void testSave() throws IOException, EcritureMissingArgumentException, InconsistentArgumentsException {
		CacheDAOFactory cache = createCacheDAO();
		
		// Première sauvegarde banale (utilisée aussi dans les autres tests)
		dao.save(cache);
		
		// Créer une erreur de clé étrangère
		Compte compte = new Compte(5, TypeCompte.COMPTE_CARTE);	// Compte inconnu du DAO
		Ecriture ecriture = new Ecriture(
				8,
				ecriture2.date,
				ecriture2.pointage,
				ecriture2.debit,
				compte,	// Casse la clé étrangère dans la base
				ecriture2.montant,
				ecriture2.libelle,
				ecriture2.tiers,
				ecriture2.cheque);
		when(cache.getEcritureDAO().getAll()).thenReturn(
				List.of(ecriture1, ecriture2, ecriture));
		
		try {
			// Méthode testée
			dao.save(cache);
			
			fail("Foreign key constraint should fail");
			
		} catch (IOException e) {
			
			// Il doit toujours rester les deux écritures sauvegardées en premier
			checkOriginalEcritures(dao.getEcritures());
		}
	}

	@Test
	public void testGetHistoriques() throws IOException {
		assertFalse(dao.getBanques().hasNext());
	}

	@Test
	public void testGetSoldesAVue() throws IOException {
		assertFalse(dao.getBanques().hasNext());
	}

	@Test
	public void testGetMoyennes() throws IOException {
		assertFalse(dao.getBanques().hasNext());
	}

	@Test
	public void testGetProperties() throws IOException {
		CacheDAOFactory cache = createCacheDAO();
		dao.save(cache);
		
		// Méthode testée
		CacheablePropertiesDAO propertiesDAO = dao.getProperties();
		
		Map<String, DiagramMemento> mementos =
				propertiesDAO.getDiagramProperties();
		assertEquals(3, mementos.size());
		
		assertEquals(memento1, mementos.get(diagram1));
		assertEquals(memento2, mementos.get(diagram2));
		assertEquals(memento3, mementos.get(diagram3));
	}

	@Test
	public void testCanBeSaved() {
		assertTrue(dao.canBeSaved());
	}

	@Test
	public void testGetName() {
		assertTrue(dao.getName().toLowerCase().contains("sql"));
	}

	@Test
	public void testGetSource() {
		assertTrue(dao.getSource().contains("comptes_mysqldao_test"));
	}

	@Test
	public void testGetSourceFullName() {
		assertTrue(dao.getSource().contains("comptes_mysqldao_test"));
	}
	
	@Test
	public void testClose() throws IOException {
		dao.save(createCacheDAO());
		
		DAOFactory.setFactory(new CacheDAOFactory(dao));
		
		// Méthode testée
		dao.close();
		
		// Vérifier que les soldes ont été calculés
		BigDecimal solde = DAOFactory.getFactory().getHistoriqueDAO().get(
				compte1, Month.getInstance(2006, 11));
		assertEquals(0, new BigDecimal(9).compareTo(solde));
	}

}
