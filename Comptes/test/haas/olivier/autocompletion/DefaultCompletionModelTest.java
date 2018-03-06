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

public class DefaultCompletionModelTest {

	// Des mocks
	@Mock private Collection<Object> coll;
	@Mock private ItemFilter<Object> filter;
	
	// Objet testé
	private DefaultCompletionModel<Object> model =
			new DefaultCompletionModel<Object>();
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testDefaultCompletionModelCollectionOfT() {
		model = new DefaultCompletionModel<Object>(coll);
		assertSame(coll, model.getValues());
	}

	@Test
	public void testGetValues() {
		// Pas de valeur à renvoyer à l'instanciation
		Collection<Object> values = model.getValues();	// Les valeurs
		assertNotNull(values);						// Une collection non nulle
		for (@SuppressWarnings("unused") Object o : values) {
			fail("Collection non vide au départ");	// Ne contenant aucune valeur
		}
	}// testGetValues

	@Test
	public void testSetValues() {
		model.setValues(coll);
		assertSame(coll, model.getValues());
	}

	@Test
	public void testGetItemFilter() {
		assertNotNull(model.getItemFilter());
	}

	@Test
	public void testSetItemFilter() {
		model.setItemFilter(filter);
		assertSame(filter, model.getItemFilter());
	}

	@Test
	public void testFilter() {
		model.setItemFilter(filter);
		model.setValues(coll);
		String text = "coucou";
		model.filter(text);
		verify(filter).filter(coll, text);
	}// testFilter

	@Test
	public void testCheck() {
		model.check();	// Pas d'exception
	}

	@Test
	public void testLoad() {
		model.load();	// Pas d'exception
	}

}
