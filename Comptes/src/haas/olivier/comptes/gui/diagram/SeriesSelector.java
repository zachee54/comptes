package haas.olivier.comptes.gui.diagram;

import haas.olivier.comptes.dao.DAOFactory;
import haas.olivier.diagram.DiagramModel;
import haas.olivier.diagram.Serie;
import haas.olivier.diagram.SeriesOrdener;
import haas.olivier.gui.util.ReverseRowSorter;
import haas.olivier.gui.util.SmartTable;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableModel;

/**
 * Un panneau de sélection et gestion des séries de données.
 * <p>
 * Il permet de masquer certaines séries et de changer l'ordre des séries.
 *
 * @author Olivier HAAS
 */
@SuppressWarnings("serial")
class SeriesSelector extends JPanel {
	
	/**
	 * Le nom unique du diagramme, pour l'identifier lors du chargement et de la
	 * mise à jour des propriétés.
	 */
	private final String name;
	
	/**
	 * La table affichant les séries.
	 */
	private final JTable table;

	/**
	 * L'ordonnateur des séries dans le modèle de diagramme.
	 */
	private final SeriesOrdener ordener;
	
	/**
	 * Construit un panneau de sélection et gestion des séries.
	 * 
	 * @param model	Le modèle du diagramme.
	 */
	public SeriesSelector(final DiagramModel model) {
		super(new BorderLayout());
		this.ordener = model.getOrdener();
		this.name = ordener.getMemento().getName();
		
		// Définir le composant principal : une table contenant les séries
		TableModel tableModel = new SeriesSelectorModel();
		table = new JTable(tableModel);
		table.setRowSorter(new ReverseRowSorter(tableModel));// Inverser lignes
		table.setTableHeader(null);							// Pas d'en-têtes
		table.getColumnModel().getColumn(0).setMaxWidth(15);// Larg checkboxes
		table.getColumnModel().getColumn(1).setMinWidth(	// Largeur du texte
				SmartTable.getLargeurUtileColonne(table, 1) + 2);
		table.setOpaque(false);								// Fond transparent
		
		// Rendre les cellules transparentes, sauf la cellule sélectionnée
		table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
			
			@Override
			public Component getTableCellRendererComponent(JTable table,
					Object value, boolean isSelected, boolean hasFocus,
					int row, int column) {
				setOpaque(isSelected);				// Opaque ssi sélectionnée
				return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
			}
			
		});// classe anonyme DefaultTableCellRenderer
		
		// Ajouter dans un scroll pane
		JScrollPane scrollPane = new JScrollPane(table);	// Le scroll pane
		scrollPane.setPreferredSize(						// Ajuster la taille
				table.getPreferredSize());
		scrollPane.setBorder(null);							// Pas de bordure
		add(scrollPane);									// Ajouter		
		
		// Les boutons pour changer l'ordre des séries
		JPanel ordre = new JPanel();						// Le panneau
		JButton haut = new JButton("\u25b2"),				// Bouton haut
				bas = new JButton("\u25bc");				// Bouton bas
		haut.addActionListener(								// Action haut
				new OrdreActionListener(false));
		bas.addActionListener(								// Action bas
				new OrdreActionListener(true));
		ordre.add(haut);									// Ajouter bouton H
		ordre.add(bas);										// Ajouter bouton B
		add(ordre, BorderLayout.NORTH);						// Ajouter panneau
	}
	
	/**
	 * Met à jour les propriétés du diagramme dans le modèle de données.
	 */
	private void notifyDAO() {
		DAOFactory.getFactory().getPropertiesDAO().setDiagramProperties(name,
				ordener.getMemento());
		// TODO Avertir la vue que le modèle doit être sauvegardé
	}
	
	/**
	 * Un <code>ActionListener</code> pour décaler une série dans la liste.
	 * 
	 * @author Olivier HAAS
	 */
	private class OrdreActionListener implements ActionListener {

		/**
		 * Le sens dans lequel déplacer la série : <code>true</code> pour la
		 * déplacer vers le haut de la table (la fin de la liste),
		 * <code>false</code> pour la déplacer vers le bas de la table (le début
		 * de la liste).
		 */
		private final boolean forward;
		
		/**
		 * Construit un objet qui décale une série dans la liste.
		 * 
		 * @param delta	Le sens dans lequel déplacer la série :
		 * 				<code>true</code> pour la déplacer vers le début de la
		 * 				liste, <code>false</code> pour la déplacer vers la fin.
		 */
		private OrdreActionListener(boolean forward) {
			this.forward = forward;
		}
		
		@Override
		public void actionPerformed(ActionEvent e) {
			int index = table.convertRowIndexToModel(table.getSelectedRow());
			if (forward) {
				ordener.moveForward(index);
			} else {
				ordener.moveBack(index);
			}
			
			// Mettre à jour dans le modèle
			notifyDAO();
			
			// Mettre à jour la vue de la table
			int newIndex = table.convertRowIndexToView(
					index + (forward ? -1 : 1));
			
			// Décaler la sélection dans la table graphique
			table.getSelectionModel().setSelectionInterval(newIndex, newIndex);
		}
	}// classe OrdreActionListener

	/**
	 * Un modèle pour la table constituant le composant principal du sélecteur
	 * de séries de données.
	 * <p>
	 * Les séries sont présentées dans l'ordre inverse du
	 * <code>SeriesOrdener</code>, de façon à ce que les premières séries se
	 * trouvent visuellement en bas, et les dernières en haut.
	 *
	 * @author Olivier HAAS
	 */
	private class SeriesSelectorModel extends AbstractTableModel {

		@Override
		public int getRowCount() {
			return ordener.getSeriesCount();
		}

		@Override
		public int getColumnCount() {
			return 2;
		}

		@Override
		public Class<?> getColumnClass(int columnIndex) {
			return columnIndex == 0 ? Boolean.class : Object.class;
		}

		@Override
		public boolean isCellEditable(int rowIndex, int columnIndex) {
			return columnIndex == 0;
		}

		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			Serie serie = getSerieForRow(rowIndex);
			switch (columnIndex) {
			case 0:		return !ordener.isHidden(serie);
			case 1:		return serie;
			default:	return null;
			}
		}

		@Override
		public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
			
			// Seules les cases à cocher (colonne 0) peuvent être éditées
			if (columnIndex == 0 && aValue instanceof Boolean) {
				ordener.setHidden(
						getSerieForRow(rowIndex),
						!((boolean) aValue));
				notifyDAO();
			}
		}
		
		/**
		 * Renvoie la série se trouvant à l'index de ligne spécifié.
		 * 
		 * @param rowIndex	L'index de ligne.
		 * @return			La série à présenter à cette ligne.
		 */
		private Serie getSerieForRow(int rowIndex) {
			return ordener.getSerieAt(rowIndex);
		}
	}// class SeriesSelectorModel
}// class SeriesSelector