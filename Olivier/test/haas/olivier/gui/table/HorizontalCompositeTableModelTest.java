package haas.olivier.gui.table;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import haas.olivier.gui.table.HorizontalCompositeTableModel;

import java.math.BigDecimal;

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

public class HorizontalCompositeTableModelTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}
	
	/**
	 * L'objet testé.
	 */
	private HorizontalCompositeTableModel model;

	/**
	 * Des sous-modèles mockés.
	 */
	@Mock
	private TableModel model1, model2, model4;
	
	/**
	 * Un vrai sous-modèle.
	 */
	private final TableModel model3 = new DefaultTableModel(6, 5);
	
	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		when(model1.getColumnCount()).thenReturn(1);
		when(model2.getColumnCount()).thenReturn(3);
		/* Pour mémoire, model3 a 5 colonnes entre model2 et model4 */
		when(model4.getColumnCount()).thenReturn(7);
		
		model = new HorizontalCompositeTableModel(
		    model1, model2, model3, model4);
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testIsCellEditable() {
		when(model1.isCellEditable(3, 0)).thenReturn(true);
		when(model1.getRowCount()).thenReturn(12);
		
		when(model2.isCellEditable(1, 1)).thenReturn(true);
		when(model2.getRowCount()).thenReturn(1);		// Pas assez de lignes
		
		when(model4.isCellEditable(4, 6)).thenReturn(true);
		when(model4.getRowCount()).thenReturn(5);
		
		assertTrue(model.isCellEditable(3, 0));
		assertFalse(model.isCellEditable(1, 2));		// Pas assez de lignes
		assertTrue(model.isCellEditable(4, 15));
		assertFalse(model.isCellEditable(2, 1));		// Témoin
	}

	@Test
	public void testGetRowCount() {
	    when(model1.getRowCount()).thenReturn(6);
	    when(model2.getRowCount()).thenReturn(12);
	    when(model4.getRowCount()).thenReturn(7);
	    assertEquals(12, model.getRowCount());
	}

	@Test
	public void testGetColumnCount() {
	    assertEquals(16, model.getColumnCount());
	}

	@Test
	public void testGetValueAt() {
	    Object a = new Object(), b = new Object(), c = new Object();
	    
	    when(model1.getValueAt(0, 0)).thenReturn(a);
	    when(model1.getRowCount()).thenReturn(4);
	    
	    when(model2.getValueAt(4, 2)).thenReturn(b);
	    when(model2.getRowCount()).thenReturn(17);
	    
	    when(model4.getValueAt(7, 0)).thenReturn(c);
	    when(model4.getRowCount()).thenReturn(8);
	    
	    assertSame(a, model.getValueAt(0, 0));
	    assertNull(model.getValueAt(6, 0));				// Pas assez de lignes
	    assertSame(b, model.getValueAt(4, 3));
	    assertSame(c, model.getValueAt(7, 9));
	    assertNull(model.getValueAt(1, 0));				// Témoin
	}

	@Test
	public void testGetColumnName() {
	    when(model1.getColumnName(0)).thenReturn("colonne 1");
	    when(model2.getColumnName(1)).thenReturn("colonne 3");
	    when(model4.getColumnName(4)).thenReturn("colonne 14");
	    
	    assertEquals("colonne 1", model.getColumnName(0));
	    assertEquals("colonne 3", model.getColumnName(2));
	    assertEquals("colonne 14", model.getColumnName(13));
	    assertNull(model.getColumnName(12));			// Témoin
	}

	@Test
	public void testGetColumnClass() {
	    doReturn(Integer.class).when(model1).getColumnClass(0);
	    doReturn(String.class).when(model2).getColumnClass(2);
	    doReturn(BigDecimal.class).when(model4).getColumnClass(1);
	    
	    assertSame(Integer.class, model.getColumnClass(0));
	    assertSame(String.class, model.getColumnClass(3));
	    assertSame(BigDecimal.class, model.getColumnClass(10));
	    assertSame(Object.class, model.getColumnClass(6)); // Témoin
	}

	@Test
	public void testSetValueAt() {
	    Object a = new Object(), b = new Object(), c = new Object();
	    
	    model.setValueAt(a, 5, 0);
	    model.setValueAt(b, 8, 1);
	    model.setValueAt(c, 9, 13);
	    
	    verify(model1).setValueAt(a, 5, 0);
	    verify(model2).setValueAt(b, 8, 0);
	    verify(model4).setValueAt(c, 9, 4);
	}

	@Test
	public void testAddTableModelListener() {
	    TableModelListener listener = mock(TableModelListener.class);
	    model.addTableModelListener(listener);
	    model3.setValueAt(0, 0, 0);
	    verify(listener).tableChanged((TableModelEvent) any());
	}

	@Test
	public void testRemoveTableModelListener() {
	    TableModelListener listener = mock(TableModelListener.class);
	    model.addTableModelListener(listener);
	    model.removeTableModelListener(listener);
	    model3.setValueAt(0, 0, 0);
	    verify(listener, never()).tableChanged((TableModelEvent) any()); 
	}
}
