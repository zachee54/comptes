package haas.olivier.gui.table;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class VerticalCompositeTableModelTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	/**
	 * L'objet testé.
	 */
	private VerticalCompositeTableModel model;
	
	/**
	 * Des sous-modèles mockés.
	 */
	@Mock
	private TableModel model1, model2, model4;
	
	/**
	 * Un vrai sous-modèle.
	 */
	private final TableModel model3 = new DefaultTableModel(5, 6);
	
	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		when(model1.getRowCount()).thenReturn(1);
		when(model2.getRowCount()).thenReturn(3);
		/* Pour mémoire, model3 a 5 lignes entre model2 et model4 */
		when(model4.getRowCount()).thenReturn(7);
		
		model = new VerticalCompositeTableModel(model1, model2, model3, model4);
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testGetRowCount() {
		assertEquals(16, model.getRowCount());
	}

	@Test
	public void testGetColumnCount() {
		when(model1.getColumnCount()).thenReturn(6);
		when(model2.getColumnCount()).thenReturn(12);
		when(model4.getColumnCount()).thenReturn(7);
		assertEquals(12, model.getColumnCount());
	}

	@Test
	public void testGetValueAt() {
	    Object a = new Object(), b = new Object(), c = new Object();
	    
	    when(model1.getValueAt(0, 0)).thenReturn(a);
	    when(model1.getColumnCount()).thenReturn(4);
	    
	    when(model2.getValueAt(2, 4)).thenReturn(b);
	    when(model2.getColumnCount()).thenReturn(17);
	    
	    when(model4.getValueAt(0, 7)).thenReturn(c);
	    when(model4.getColumnCount()).thenReturn(8);
	    
	    assertSame(a, model.getValueAt(0, 0));
	    assertNull(model.getValueAt(0, 6));				// Pas assez de colonnes
	    assertSame(b, model.getValueAt(3, 4));
	    assertSame(c, model.getValueAt(9, 7));
	    assertNull(model.getValueAt(0, 1));				// Témoin
	}

	@Test
	public void testGetColumnName() {
		when(model1.getColumnName(2)).thenReturn("Nom de la colonne");
		assertEquals("Nom de la colonne", model.getColumnName(2));
	}

	@Test
	public void testGetColumnClass() {
		doReturn(Double.class).when(model1).getColumnClass(5);
	}

	@Test
	public void testIsCellEditable() {
		when(model1.isCellEditable(0, 3)).thenReturn(true);
		when(model1.getColumnCount()).thenReturn(12);
		
		when(model2.isCellEditable(1, 1)).thenReturn(true);
		when(model2.getColumnCount()).thenReturn(1);	// Pas assez de colonnes
		
		when(model4.isCellEditable(6, 4)).thenReturn(true);
		when(model4.getColumnCount()).thenReturn(5);
		
		assertTrue(model.isCellEditable(0, 3));
		assertFalse(model.isCellEditable(2, 1));		// Pas assez de colonnes
		assertTrue(model.isCellEditable(15, 4));
		assertFalse(model.isCellEditable(1, 2));		// Témoin
	}

	@Test
	public void testSetValueAt() {
	    Object a = new Object(), b = new Object(), c = new Object();
	    
	    model.setValueAt(a, 0, 5);
	    model.setValueAt(b, 1, 8);
	    model.setValueAt(c, 13, 9);
	    
	    verify(model1).setValueAt(a, 0, 5);
	    verify(model2).setValueAt(b, 0, 8);
	    verify(model4).setValueAt(c, 4, 9);
	}

	@Test
	public void testAddTableModelListener() {
	    TableModelListener listener = mock(TableModelListener.class);
	    model.addTableModelListener(listener);
	    model3.setValueAt(0, 0, 0);
	    verify(listener).tableChanged(any(TableModelEvent.class));
	}

	@Test
	public void testRemoveTableModelListener() {
	    TableModelListener listener = mock(TableModelListener.class);
	    model.addTableModelListener(listener);
	    model.removeTableModelListener(listener);
	    model3.setValueAt(0, 0, 0);
	    verify(listener, never()).tableChanged(any(TableModelEvent.class));
	}

}
