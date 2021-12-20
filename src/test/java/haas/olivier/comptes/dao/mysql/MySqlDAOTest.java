package haas.olivier.comptes.dao.mysql;

import static org.junit.Assert.*;

import java.io.IOException;
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
	public void testGetBanques() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetComptes() {
		fail("Not yet implemented");
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
	public void testSave() throws IOException {
		// TODO à compléter
		dao.save(null);
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
