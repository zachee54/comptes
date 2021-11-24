package haas.olivier.comptes.dao.mysql;

import static org.junit.Assert.*;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class MySqlDAOTest {
	
	private static final String DATABASE = "comptes_mysqldao_test";
	
	private static final MySqlDAO STATIC_DAO =
			new MySqlDAO("localhost", 3306, null, "comptes_mysqldao_test", "dummypassword");

	/** Objet testé. */
	private MySqlDAO dao;
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		try (Connection connection = STATIC_DAO.getConnection();
				Statement statement = connection.createStatement()) {
			statement.execute("DROP DATABASE IF EXISTS " + DATABASE);
			statement.execute("CREATE DATABASE " + DATABASE);
		}
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
	public void testGetBanqueDAO() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetCompteDAO() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetEcritureDAO() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetPermanentDAO() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetHistoriqueDAO() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetSoldeAVueDAO() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetMoyenneDAO() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetPropertiesDAO() {
		fail("Not yet implemented");
	}

	@Test
	public void testCanBeSaved() {
		fail("Not yet implemented");
	}

	@Test
	public void testMustBeSaved() {
		fail("Not yet implemented");
	}

	@Test
	public void testSave() {
		fail("Not yet implemented");
	}

	@Test
	public void testErase() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetDebut() {
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

}
