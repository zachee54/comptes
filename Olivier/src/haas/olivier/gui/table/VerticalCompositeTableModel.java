/*
 * Copyright (c) 2018 Olivier HAAS - Tous droits réservés
 * 
 */
package haas.olivier.gui.table;

import java.util.Collection;

import haas.olivier.util.Observable;

import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

/**
 * Un modèle de table composé à partir de plusieurs sous-modèles dont les tables
 * seraient disposées verticalement les unes à côté des autres.
 * 
 * @author Olivier HAAS
 */
public class VerticalCompositeTableModel
extends Observable<TableModelListener>
implements TableModel, TableModelListener {
	
	/**
	 * Les sous-modèles.
	 */
	private final TableModel[] models;
	
	/**
	 * Construit un modèle de table composé de plusieurs sous-modèles disposés
	 * verticalement.
	 * 
	 * @param models	Une collection des sous-modèles.
	 */
	public VerticalCompositeTableModel(
			Collection<? extends TableModel> models) {
		this(models.toArray(new TableModel[models.size()]));
	}
	
	/**
	 * Construit un modèle de table composé de plusieurs sous-modèles disposés
	 * verticalement.
	 * 
	 * @param models	Les sous-modèles.
	 */
	public VerticalCompositeTableModel(TableModel... models) {
		this.models = models;
		for (TableModel model : models)
			model.addTableModelListener(this);
	}
	
	@Override
	public int getRowCount() {
		int count = 0;
		for (TableModel model : models)
			count += model.getRowCount();
		return count;
	}

	@Override
	public int getColumnCount() {
		int count = 0;
		for (TableModel model : models)
			count = Math.max(count, model.getColumnCount());
		return count;
	}
	
	/**
	 * Renvoie le sous-modèle se trouvant à un index de ligne spécifique, et
	 * l'index de cette ligne dans le sous-modèle.
	 * 
	 * @param rowIndex	L'index de la ligne souhaitée.
	 * @return			Un memento du sous-modèle concerné et de l'index de la
	 * 					ligne dans ce sous-modèle.
	 */
	public SubModelAndIndex getSubModelAndIndex(int rowIndex) {
		int result = rowIndex;
		for (TableModel model : models) {
			int rowCount = model.getRowCount();
			if (result < rowCount)
				return new SubModelAndIndex(model, result);
			result -= rowCount;
		}
		throw new IllegalArgumentException(
				"rowIndex must be less than rows count");
	}

	/**
	 * @return	La valeur du sous-modèle auquel correspond cet index de ligne,
	 * 			ou <code>null</code> si <code>columnIndex</code> est au-delà de
	 * 			la dernière colonne de ce sous-modèle.
	 */
	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		SubModelAndIndex delegate = getSubModelAndIndex(rowIndex);
		TableModel model = delegate.model;
		return columnIndex < model.getColumnCount()
				? model.getValueAt(delegate.index, columnIndex)
				: null;
	}

	/**
	 * @return	Le nom de la colonne dans le premier sous-modèle.
	 */
	@Override
	public String getColumnName(int columnIndex) {
		return models[0].getColumnName(columnIndex);	//Non vide par hypothèse
	}

	/**
	 * @return	La classe de la colonne dans le premier sous-modèle.
	 */
	@Override
	public Class<?> getColumnClass(int columnIndex) {
		return models[0].getColumnClass(columnIndex);
	}

	/**
	 * @return	La valeur de {@link TableModel#isCellEditable(int, int)} pour le
	 * 			sous-modèle situé à cet index de ligne, ou <code>false</code>
	 * 			si <code>columnIndex</code> est au-delà de la dernière colonne
	 * 			de ce sous-modèle.
	 */
	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		SubModelAndIndex delegate = getSubModelAndIndex(rowIndex);
		TableModel model = delegate.model;
		return columnIndex < model.getColumnCount()
				? model.isCellEditable(delegate.index, columnIndex)
				: false;
	}

	@Override
	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
		SubModelAndIndex delegate = getSubModelAndIndex(rowIndex);
		delegate.model.setValueAt(aValue, delegate.index, columnIndex);
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