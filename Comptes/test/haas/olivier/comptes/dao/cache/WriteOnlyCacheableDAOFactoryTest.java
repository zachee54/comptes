package haas.olivier.comptes.dao.cache;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.io.IOException;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import haas.olivier.comptes.dao.CompteDAO;
import haas.olivier.comptes.dao.DAOFactory;

public class WriteOnlyCacheableDAOFactoryTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	/** Un mock de la sous-couche du modèle. */
	private final CacheableDAOFactory delegate =
			mock(CacheableDAOFactory.class);
	
	/** Objet testé. */
	private WriteOnlyCacheableDAOFactory writeOnly;
	
	@Before
	public void setUp() throws Exception {
		RuntimeException exception = new UnsupportedOperationException();
		when(delegate.getBanques()).thenThrow(exception);
		when(delegate.getComptes()).thenThrow(exception);
		when(delegate.getEcritures((CompteDAO) any())).thenThrow(exception);
		when(delegate.getPermanents((CachePermanentDAO) any(), (CompteDAO) any()))
		.thenThrow(exception);
		when(delegate.getProperties()).thenThrow(exception);
		when(delegate.getHistorique()).thenThrow(exception);
		when(delegate.getMoyennes()).thenThrow(exception);
		when(delegate.getSoldesAVue()).thenThrow(exception);
		
		writeOnly = new WriteOnlyCacheableDAOFactory(delegate);
	}// setUp

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testWriteOnlyCacheableDAOFactory() {
		verifyZeroInteractions(delegate);
	}// testWriteOnlyCacheableDAOFactory

	@Test
	public void testGetBanques() throws IOException {
		assertFalse(writeOnly.getBanques().hasNext());
	}// testGetBanques

	@Test
	public void testGetComptes() throws IOException {
		assertFalse(writeOnly.getComptes().hasNext());
	}// testGetComptes

	@Test
	public void testGetEcritures() throws IOException {
		assertFalse(writeOnly.getEcritures(mock(CompteDAO.class)).hasNext());
	}// testGetEcritures

	@Test
	public void testGetPermanents() throws IOException {
		assertFalse(writeOnly.getPermanents(
				mock(CachePermanentDAO.class), mock(CompteDAO.class))
				.hasNext());
	}// testGetPermanents

	@Test
	public void testGetHistorique() throws IOException {
		assertFalse(writeOnly.getHistorique().hasNext());
	}// testGetHistorique

	@Test
	public void testGetSoldesAVue() throws IOException {
		assertFalse(writeOnly.getSoldesAVue().hasNext());
	}// testGetSoldesAVue

	@Test
	public void testGetMoyennes() throws IOException {
		assertFalse(writeOnly.getMoyennes().hasNext());
	}// testGetMoyennes

	@Test
	public void testGetProperties() throws IOException {
		assertTrue(writeOnly.getProperties().getDiagramProperties().isEmpty());
	}// testGetProperties

	@Test
	public void testSave() throws IOException {
		DAOFactory factory = mock(DAOFactory.class);
		writeOnly.save(factory);
		verify(delegate).save(factory);
	}// testSave

	@Test
	public void testGetName() {
		String name = "test name";
		when(delegate.getName()).thenReturn(name);
		assertEquals(name, writeOnly.getName());
	}// testGetName

	@Test
	public void testGetSource() {
		String source = "test source name";
		when(delegate.getSource()).thenReturn(source);
		assertEquals(source, writeOnly.getSource());
	}// testGetSource

	@Test
	public void testGetSourceFullName() {
		String name = "test source full name";
		when(delegate.getSourceFullName()).thenReturn(name);
		assertEquals(name, writeOnly.getSourceFullName());
	}// testGetSourceFullName

	@Test
	public void testClose() throws IOException {
		writeOnly.close();
		verify(delegate).close();
	}// testClose

	@Test
	public void testCanBeSaved() {
		assertFalse(writeOnly.canBeSaved());
		
		when(delegate.canBeSaved()).thenReturn(true);
		assertTrue(writeOnly.canBeSaved());
	}// testCanBeSaved

}
