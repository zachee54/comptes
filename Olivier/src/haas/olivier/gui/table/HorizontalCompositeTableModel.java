/*
 * Copyright (c) 2018 Olivier HAAS - Tous droits réservés
 */
package haas.olivier.gui.table;

import haas.olivier.util.Observable;

import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

/**
 * Un modèle de table composé à partir de plusieurs sous-modèles dont les tables
 * seraient disposées horizontalement les unes à côté des autres.
 * 
 * @author Olivier HAAS
 */
public class HorizontalCompositeTableModel
extends Observable<TableModelListener>
implements TableModel, TableModelListener {
	
	/**
	 * Les sous-modèles.
	 */
	private final TableModel[] models;
	
	/**
	 * Construit un modèle de table composé de plusieurs sous-modèles disposés
	 * horizontalement.
	 * 
	 * @param models	Les sous-modèles.
	 */
	public HorizontalCompositeTableModel(TableModel... models) {
		this.models = models;
		for (TableModel model : models)
			model.addTableModelListener(this);
	}
	
	@Override
	public int getRowCount() {
		int count = 0;
		for (TableModel model : models)
			count = Math.max(count, model.getRowCount());
		return count;
	}

	@Override
	public int getColumnCount() {
		int count = 0;
		for (TableModel model : models)
			count += model.getColumnCount();
		return count;
	}
	
	/**
	 * Renvoie le sous-modèle se trouvant à un index de colonne spécifique, et
	 * l'index de cette colonne dans le sous-modèle.
	 * 
	 * @param columnIndex	L'index de la colonne souhaitée.
	 * @return				Un memento du sous-modèle concerné et de l'index de
	 * 						la colonne dans ce sous-modèle.
	 */
	private SubModelAndIndex getSubModelAndIndex(int columnIndex) {
		int result = columnIndex;
		for (TableModel model : models) {
			int colCount = model.getColumnCount();
			if (result < colCount)
				return new SubModelAndIndex(model, result);
			result -= colCount;
		}
		throw new IllegalArgumentException(
				"columnIndex must be less than columns count");
	}

	/**
	 * @return	La valeur du sous-modèle auquel correspond cet index de colonne,
	 * 			ou <code>null</code> si <code>rowIndex</code> est au-delà de la
	 * 			dernière ligne de ce sous-modèle.
	 */
	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		SubModelAndIndex delegate = getSubModelAndIndex(columnIndex);
		TableModel model = delegate.model;
		return rowIndex < model.getRowCount()
				? model.getValueAt(rowIndex, delegate.index)
				: null;
	}

	@Override
	public String getColumnName(int columnIndex) {
		SubModelAndIndex delegate = getSubModelAndIndex(columnIndex);
		return delegate.model.getColumnName(delegate.index);
	}

	@Override
	public Class<?> getColumnClass(int columnIndex) {
		SubModelAndIndex delegate = getSubModelAndIndex(columnIndex);
		return delegate.model.getColumnClass(delegate.index);
	}

	/**
	 * @return	La valeur de {@link TableModel#isCellEditable(int, int)} pour le
	 * 			sous-modèle situé à cet index de colonne, ou <code>false</code>
	 * 			si <code>rowIndex</code> est au-delà de la dernière ligne de ce
	 * 			sous-modèle.
	 */
	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		SubModelAndIndex delegate = getSubModelAndIndex(columnIndex);
		TableModel model = delegate.model;
		return rowIndex < model.getRowCount()
				? model.isCellEditable(rowIndex, delegate.index)
				: false;
	}

	@Override
	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
		SubModelAndIndex delegate = getSubModelAndIndex(columnIndex);
		delegate.model.setValueAt(aValue, rowIndex, delegate.index);
	}

	/**
	 * Fait suivre aux listeners un événement qui a eu lieu sur un sous-modèle.
	 */
	@Override
	public void tableChanged(TableModelEvent e) {
		for (TableModelListener l : observers)
			l.tableChanged(e);
	}

	@Override
	public void addTableModelListener(TableModelListener l) {
		addObserver(l);
	}

	@Override
	public void removeTableModelListener(TableModelListener l) {
		removeObserver(l);
	}
}
