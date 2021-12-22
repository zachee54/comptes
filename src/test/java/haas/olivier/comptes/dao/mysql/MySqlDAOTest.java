package haas.olivier.comptes.dao.mysql;

import static org.junit.Assert.*;

import java.awt.Color;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.mockito.Mockito.*;

import haas.olivier.comptes.Compte;
import haas.olivier.comptes.TypeCompte;
import haas.olivier.comptes.dao.CompteDAO;
import haas.olivier.comptes.dao.cache.CacheDAOFactory;

public class MySqlDAOTest {
	
	private static final String DATABASE = "comptes_mysqldao_test";
	
	private static final MySqlDAO STATIC_DAO =
			new MySqlDAO("localhost", 3306, null, "comptes_mysqldao_test", "dummypassword");
	
	private static Compte compte1, compte2;

	/** Objet testé. */
	private MySqlDAO dao;
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		try (Connection connection = STATIC_DAO.getConnection();
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
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		try (Connection connection = STATIC_DAO.getConnection();
				Statement statement = connection.createStatement()) {
			statement.execute("DROP DATABASE IF EXISTS " + DATABASE);
		}
	}

	@Before
	public void setUp() throws Exception {
		dao = new MySqlDAO("localhost", 3306, DATABASE, "comptes", "dummypassword");
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testGetBanques() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetComptes() throws IOException {
		List<Compte> comptes = new ArrayList<>();
		comptes.add(compte1);
		comptes.add(compte2);
		
		CompteDAO cacheCompteDAO = mock(CompteDAO.class);
		when(cacheCompteDAO.getAll()).thenReturn(comptes);
		
		CacheDAOFactory cache = mock(CacheDAOFactory.class);
		when(cache.getCompteDAO()).thenReturn(cacheCompteDAO);
		
		dao.save(cache);
		
		// Méthode testée
		Iterator<Compte> comptesIt = dao.getComptes();
		
		while (comptesIt.hasNext()) {
			assertTrue(comptes.remove(comptesIt.next()));
		}
		assertTrue(comptes.isEmpty());
	}

	@Test
	public void testGetEcritures() {
		fail("Not yet implemented");
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
	public void testGetConnection() throws SQLException {
		try (Connection connection = dao.getConnection()) {
			assertFalse(connection.isClosed());
		}
	}
	
	@Test
	public void testClose() {
		fail("Not yet implemented");
	}

}
