package haas.olivier.autocompletion;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.Collection;


import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class CacheCompletionModelTest {

	@Mock private CompletionModel<Object> delegate;			// Objet délégué
	@Mock private CacheCompletionModel.ContextProvider cp;	// Contexte
	@Mock private ItemFilter<Object> filter;				// Filtre
	@Mock private Collection<Object> v1, v2;				// Valeurs
	private CacheCompletionModel<Object> ccm;				// Objet testé
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
		
		// Initialiser les mocks
		MockitoAnnotations.initMocks(this);
		
		// Comportement des mocks
		when(delegate.getValues()).thenReturn(v1);
		when(delegate.getItemFilter()).thenReturn(filter);
		
		// Objet testé
		ccm = new CacheCompletionModel<Object>(cp, delegate);
	}// setUp

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testCheckOne() {
		when(cp.getContext()).thenReturn(new Object());
		ccm.check();
		ccm.check();
		ccm.check();
		verify(delegate, times(1)).load();	// Un seul chargement
	}
	
	@Test
	public void testCheckTwice() {
		// 1ère série de tests
		when(cp.getContext()).thenReturn(new Object());
		ccm.check();
		ccm.check();
		ccm.check();
		
		// 2ème série
		when(cp.getContext()).thenReturn(new Object());
		ccm.check();
		ccm.check();
		
		// Vérifier
		verify(delegate, times(2)).load();	// Deux chargements en tout
	}

	@Test
	public void testLoad() {
		ccm.load();
		verify(delegate).load();
	}

	@Test
	public void testGetValues() {
		assertSame(v1, ccm.getValues());
	}

	@Test
	public void testGetItemFilter() {
		assertSame(filter, ccm.getItemFilter());
	}

	@Test
	public void testSetItemFilter() {
		ccm.setItemFilter(filter);
		verify(delegate).setItemFilter(filter);
	}

	@Test
	public void testFilter() {
		String text = "coucou";
		ccm.filter(text);
		verify(delegate).filter(text);
	}

}
