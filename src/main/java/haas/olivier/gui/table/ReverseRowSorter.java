package haas.olivier.comptes.gui.diagram;

import java.util.Collections;
import java.util.List;

import javax.swing.RowSorter;
import javax.swing.table.TableModel;

/**
 * Un trieur qui inverse l'ordre de tri des lignes dans une table.
 *
 * @author Olivier Haas
 */
class ReverseRowSorter extends RowSorter<TableModel> {

	/**
	 * Le modèle de table.
	 */
	private final TableModel model;
	
	public ReverseRowSorter(TableModel model) {
		this.model = model;
	}
	
	@Override
	public TableModel getModel() {
		return model;
	}

	/**
	 * Aucune implémentation.
	 */
	@Override
	public void toggleSortOrder(int column) {
		// Ce trieur est figé : aucune action à faire ici
	}

	@Override
	public int convertRowIndexToModel(int index) {
		return model.getRowCount() - 1 - index;
	}

	@Override
	public int convertRowIndexToView(int index) {
		return model.getRowCount() - 1 - index;
	}

	/**
	 * Aucune implémentation.
	 */
	@Override
	public void setSortKeys(List<? extends SortKey> keys) {
		// Ce trieur est figé : aucune action à faire ici
	}

	@Override
	public List<? extends SortKey> getSortKeys() {
		return Collections.emptyList();
	}

	@Override
	public int getViewRowCount() {
		return model.getRowCount();
	}

	@Override
	public int getModelRowCount() {
		return model.getRowCount();
	}

	/**
	 * Aucune implémentation.
	 */
	@Override
	public void modelStructureChanged() {
		// Ce trieur est figé : aucune action nécessaire ici
	}

	/**
	 * Aucune implémentation.
	 */
	@Override
	public void allRowsChanged() {
		// Ce trieur est figé : aucune action nécessaire ici
	}

	/**
	 * Aucune implémentation.
	 */
	@Override
	public void rowsInserted(int firstRow, int endRow) {
		// Ce trieur est figé : aucune action nécessaire ici
	}

	/**
	 * Aucune implémentation.
	 */
	@Override
	public void rowsDeleted(int firstRow, int endRow) {
		// Ce trieur est figé : aucune action nécessaire ici
	}

	/**
	 * Aucune implémentation.
	 */
	@Override
	public void rowsUpdated(int firstRow, int endRow) {
		// Ce trieur est figé : aucune action nécessaire ici
	}

	/**
	 * Aucune implémentation.
	 */
	@Override
	public void rowsUpdated(int firstRow, int endRow, int column) {
		// Ce trieur est figé : aucune action nécessaire ici
	}

}
