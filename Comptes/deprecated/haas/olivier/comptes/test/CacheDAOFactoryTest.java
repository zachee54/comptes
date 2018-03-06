package haas.olivier.comptes.test;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import haas.olivier.comptes.Compte;
import haas.olivier.comptes.Ecriture;
import haas.olivier.util.Month;
import haas.olivier.comptes.Permanent;
import haas.olivier.comptes.dao.DAOFactory;
import haas.olivier.comptes.dao.cache.CacheDAOFactory;
import haas.olivier.comptes.dao.cache.CacheableCompteDAO;
import haas.olivier.comptes.dao.cache.CacheableDAOFactory;
import haas.olivier.comptes.dao.cache.CacheableEcritureDAO;
import haas.olivier.comptes.dao.cache.CacheablePermanentDAO;
import haas.olivier.comptes.dao.cache.CacheableSuiviDAO;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;


public class CacheDAOFactoryTest {

	private CacheDAOFactory dao;
	@Mock private CacheableDAOFactory subDAO;
	@Mock private CacheableCompteDAO subCDAO;
	@Mock private CacheableEcritureDAO subEDAO;
	@Mock private CacheablePermanentDAO subPDAO;
	@Mock private CacheableSuiviDAO subHDAO, subSDAO, subMDAO;
	@Mock private HashMap<Month,Map<Integer,BigDecimal>> histo, soldes, moyennes;
	private Set<Compte> comptes = new HashSet<Compte>();
	private TreeSet<Ecriture> ecritures = new TreeSet<Ecriture>();
	private Set<Permanent> permanents = new HashSet<Permanent>();
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}// setUpBeforeClass

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
		
		// Initialiser les Mocks
		MockitoAnnotations.initMocks(this);
		
		// Comportement du Mock DAOFactory
		when(subDAO.getCompteDAO()).thenReturn(subCDAO);
		when(subDAO.getEcritureDAO()).thenReturn(subEDAO);
		when(subDAO.getPermanentDAO()).thenReturn(subPDAO);
		when(subDAO.getHistoriqueDAO()).thenReturn(subHDAO);
		when(subDAO.getSoldeAVueDAO()).thenReturn(subSDAO);
		when(subDAO.getMoyenneDAO()).thenReturn(subMDAO);
		when(subDAO.getName()).thenReturn("nom du Mock");
		when(subDAO.getSource()).thenReturn("source du Mock");
		when(subDAO.getSourceFullName()).thenReturn("source longue du Mock");
		
		// Comportement des DAOs
		when(subCDAO.getAll()).thenReturn(comptes);
		when(subEDAO.getAll()).thenReturn(ecritures);
		when(subPDAO.getAll()).thenReturn(permanents);
		when(subHDAO.getAll()).thenReturn(histo);
		when(subSDAO.getAll()).thenReturn(soldes);
		when(subMDAO.getAll()).thenReturn(moyennes);
		
		// Empêcher l'accès aux données (pas d'appel en phase d'instantiation)
		DAOFactory.setFactory(mock(DAOFactory.class));
		
		// Object testé
		dao = new CacheDAOFactory(subDAO);
	}// setUp

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testGetCompteDAO() throws IOException {
		dao.getCompteDAO().getAll();
		verify(subCDAO).getAll();
	}

	@Test
	public void testGetEcritureDAO() throws IOException {
		dao.getEcritureDAO().getAll();
		verify(subEDAO).getAll();
	}

	@Test
	public void testGetPermanentDAO() throws IOException {
		dao.getPermanentDAO().getAll();
		verify(subPDAO).getAll();
	}

	@Test
	public void testGetHistoriqueDAO() throws IOException {
		dao.getHistoriqueDAO().getAll();
		verify(subHDAO).getAll();
	}

	@Test
	public void testGetSoldeAVueDAO() throws IOException {
		dao.getSoldeAVueDAO().getAll();
		verify(subSDAO).getAll();
	}

	@Test
	public void testGetMoyenneDAO() throws IOException {
		dao.getMoyenneDAO().getAll();
		verify(subMDAO).getAll();
	}

	@Test
	public void testGetName() throws IOException {
		assertEquals("Cache nom du Mock", dao.getName());
	}

	@Test
	public void testGetSource() {
		assertEquals("source du Mock", dao.getSource());
	}

	@Test
	public void testGetSourceFullName() {
		assertEquals("source longue du Mock", dao.getSourceFullName());
	}

	@Test
	public void testSave() throws IOException {
		dao.save();
		
		/* Les DAO annexes peuvent avoir été sauvegardés dans n'importe quel
		 * ordre, en revanche flush doit être appelé en dernier.
		 */
		
		// Vérifier le flush après les comptes
		InOrder inOrderC = inOrder(subCDAO, subDAO);
		inOrderC.verify(subCDAO).save(eq(comptes));
		inOrderC.verify(subDAO).flush();
		
		// Flush après les écritures
		InOrder inOrderE = inOrder(subEDAO, subDAO);
		inOrderE.verify(subEDAO).save(eq(ecritures));
		inOrderE.verify(subDAO).flush();
		
		// Flush après les permanents
		InOrder inOrderP = inOrder(subPDAO, subDAO);
		inOrderP.verify(subPDAO).save(eq(permanents));
		inOrderP.verify(subDAO).flush();
		
		// Flush après les historiques
		InOrder inOrderH = inOrder(subHDAO, subDAO);
		inOrderH.verify(subHDAO).save(eq(histo));	// Marche bien avec un Mock de HashMap uniquement (sinon subHDAO renvoie une autre collection)
		inOrderH.verify(subDAO).flush();
		
		// Flush après les soldes à vue
		InOrder inOrderS = inOrder(subSDAO, subDAO);
		inOrderS.verify(subSDAO).save(eq(soldes));
		inOrderS.verify(subDAO).flush();
		
		// Flush après les moyennes
		InOrder inOrderM = inOrder(subMDAO, subDAO);
		inOrderM.verify(subMDAO).save(eq(moyennes));
		inOrderM.verify(subDAO).flush();
	}// testSave
}
