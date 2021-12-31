package haas.olivier.comptes.dao.mysql;

import static org.junit.Assert.*;

import java.awt.Color;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

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
import haas.olivier.comptes.TypeCompte;
import haas.olivier.comptes.dao.CompteDAO;
import haas.olivier.comptes.dao.EcritureDAO;
import haas.olivier.comptes.dao.cache.CacheDAOFactory;

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

	/** Objet testé. */
	private MySqlDAO dao;
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		try (Connection connection = DATASOURCE.getConnection(USERNAME, PASSWORD);
				Statement statement = connection.createStatement()) {
			statement.execute("DROP DATABASE IF EXISTS " + DATABASE);
			statement.execute("CREATE DATABASE " + DATABASE);
		}
		
		compte1 = new Compte(1, TypeCompte.COMPTE_EPARGNE);
		compte1.setNom("Le compte 1");
		compte1.setNumero(57931L);
		compte1.setOuverture(new Date(35468L));
		compte1.setColor(Color.CYAN);
		
		compte2 = new Compte(7, TypeCompte.RECETTES_EN_EPARGNE);
		compte2.setNom("Le compte n°7");
		compte2.setOuverture(new Date(24963479L));
		compte2.setCloture(new Date(3696347895263L));
		compte2.setColor(Color.ORANGE);
		
		ecriture1 = new Ecriture(
				3,
				new Date(2482148L),
				new Date(8941476L),
				compte1,
				compte2,
				BigDecimal.ONE,
				"libellé 1",
				null,
				null);
		
		ecriture2 = new Ecriture(
				31,
				new Date(932487L),
				new Date(3147826L),
				compte2,
				compte1,
				BigDecimal.TEN,
				null,
				"tiers 2",
				3544782);
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
		
		CacheDAOFactory cache = mock(CacheDAOFactory.class);
		when(cache.getCompteDAO()).thenReturn(compteDAO);
		when(cache.getEcritureDAO()).thenReturn(ecritureDAO);
		
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

	@Test
	public void testGetBanques() {
		fail("Not yet implemented");
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
		
		List<Ecriture> ecritures = new ArrayList<>(2);
		ecritures.add(ecriture1);
		ecritures.add(ecriture2);
		
		while (ecrituresIterator.hasNext()) {
			assertTrue(ecritures.remove(ecrituresIterator.next()));
		}
		assertTrue(ecritures.isEmpty());
	}
	
	@Test
	public void testGetPermanents() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetHistoriques() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetSoldesAVue() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetMoyennes() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetProperties() {
		fail("Not yet implemented");
	}

	@Test
	public void testCanBeSaved() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetName() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetSource() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetSourceFullName() {
		fail("Not yet implemented");
	}

	@Test
	public void testMySqlDAO() {
		fail("Not yet implemented");
	}
	
	@Test
	public void testClose() {
		fail("Not yet implemented");
	}

}
