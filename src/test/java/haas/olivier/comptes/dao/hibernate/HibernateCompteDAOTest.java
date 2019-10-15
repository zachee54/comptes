package haas.olivier.comptes.dao.hibernate;

import static org.junit.Assert.*;
import haas.olivier.comptes.Compte;
import haas.olivier.comptes.TypeCompte;
import haas.olivier.comptes.dao.CompteDAO;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class HibernateCompteDAOTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	/**
	 * Contexte JPA.
	 */
	private HibernateDAO factory;
	
	/**
	 * Objet testé.
	 */
	private CompteDAO dao;
	
	@Before
	public void setUp() throws Exception {
		factory = new HibernateDAO(
				"jdbc:hsqldb:mem:test", "org.hsqldb.jdbc.JDBCDriver");
		dao = factory.getCompteDAO();
	}

	@After
	public void tearDown() throws Exception {
		factory.close();
	}

	@Test
	public void testGetAll() {
//		fail("Not yet implemented");
	}

	@Test
	public void testAdd() {
		Compte compte = new Compte(0, TypeCompte.COMPTE_CARTE);
		dao.add(compte);
	}

	@Test
	public void testCreateAndAdd() {
//		fail("Not yet implemented");
	}

	@Test
	public void testRemove() {
//		fail("Not yet implemented");
	}

	@Test
	public void testErase() {
//		fail("Not yet implemented");
	}

}
