/*
 * Copyright (c) 2018 Olivier HAAS - Tous droits réservés
 */
package haas.olivier.gui.table;

import java.awt.Component;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.MouseEvent;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

/** Une table qui affiche un tool tip lorsque la cellule est trop petite pour
 * afficher le texte complet.
 *
 * @author Olivier HAAS
 */
public class SmartTable extends JTable {
	private static final long serialVersionUID = -356739582024144451L;
	
	/** Construit une table avec des tool tips qui s'affichent si nécessaire. */
	public SmartTable() {
		// Déclaré uniquement pour autoriser l'usage du constructeur par défaut
	}// constructeur simple

	/** Construit une table avec des tool tips qui s'affichent si nécessaire. */
	public SmartTable(TableModel model) {
		super(model);
	}// constructeur TableModel

	/** Construit une table avec des tool tips qui s'affichent si nécessaire. */
	public SmartTable(TableModel model, TableColumnModel cm) {
		super(model, cm);
	}// constructeur TableModel et ColumnModel

	/** Renvoie la largeur minimale d'une <code>TableColumn</code> pour que le
	 * texte de l'en-tête et de chaque cellule puisse être affiché en entier.
	 * 
	 * @param table	La table dans laquelle se trouve la colonne à examiner.
	 * 
	 * @param col	L'index de la colonne à examiner (index de la vue).
	 */
	public static int getLargeurUtileColonne(JTable table, int col) {
		TableModel model = table.getModel();				// Modèle de table
		TableColumn column =								// Colonne concernée
				table.getColumnModel().getColumn(col);
		
		int largeurUtileCol = 0;							// Résultat
		
		// Commencer par la largeur utile de l'en-tête de colonne
		JTableHeader header = table.getTableHeader();		// En-tête de table
		if (header != null) {
			TableCellRenderer headerRenderer = column.getHeaderRenderer();
			if (headerRenderer == null)						// Si null, Default
				headerRenderer = header.getDefaultRenderer();
			
			// Calculer la largeur de l'en-tête
			largeurUtileCol = headerRenderer.getTableCellRendererComponent(
					table, column.getHeaderValue(), false, false, -1, col)
					.getPreferredSize().width;				// Largeur préférée
		}// if header
		
		// Comparer avec la largeur préférée de chaque ligne
		for (int row = 0; row < model.getRowCount(); row++) {
			
			// Le composant graphique de la cellule
			Component comp = table.getCellRenderer(row, col)// Renderer
					.getTableCellRendererComponent(
							table,							// Table
							model.getValueAt(				// Sinon valeur
									row,// (pas besoin de convert:toutes lignes)
									table.convertColumnIndexToModel(col)),
							false,							// Pas de sélection
							false,							// Pas de focus
							row,							// Index de ligne
							col);							// Index de colonne
			
			// Garder la largeur la plus grande
			largeurUtileCol = Math.max(
					largeurUtileCol,
					comp.getPreferredSize().width);
		}// for lignes
		
		return largeurUtileCol + table.getIntercellSpacing().width;
	}// getLargeurUtileColonne
	
	/** Renvoie la largeur minimale de l'étiquette pour que le texte puisse être
	 * affiché en entier.
	 * 
	 * @param label	Une étiquette.
	 * @return		La largeur minimale sans couper le texte, en pixels.
	 */
	public static int getLargeurUtile(JLabel label) {
		Insets insets = label.getInsets();
		String text = label.getText();
		if (text == null) {
			return insets.left + insets.right;
		} else {
			return label.getFontMetrics(label.getFont())
					.stringWidth(label.getText())
					+ insets.left + insets.right;
		}// if text null
	}// getLargeurUtile
	
	/** Renvoie le texte de la cellule comme tool tip si la cellule est petite
	 * pour afficher le texte en entier.<br>
	 * 
	 * @return	Le texte de la cellule si le RendererComponent est un
	 * 			<code>JLabel</code> trop étroit, ou <code>null</code> sinon.
	 */
	@Override
	public String getToolTipText(MouseEvent e) {
		String tooltip = null;							// Valeur par défaut
		
		// Trouver les coordonnées de la cellule survolée
		Point p = e.getPoint();
		int viewRowIndex = rowAtPoint(p);				// Ligne visible
		int viewColIndex = columnAtPoint(p);			// Colonne visible
		if (viewRowIndex == -1 || viewColIndex == -1)	// Si hors champ
			return null;								// Arrêter
		int rowIndex = convertRowIndexToModel(viewRowIndex);	//Ligne modèle
		int colIndex = convertColumnIndexToModel(viewColIndex);	//Colonne modèle
		
		// Récupérer le composant de rendu de la cellule
		Component comp = getCellRenderer(rowIndex, colIndex)
				.getTableCellRendererComponent(this,
						dataModel.getValueAt(rowIndex, colIndex),
						false, false, rowIndex, colIndex);
		
		// Vérifier que c'est un JLabel
		if (comp instanceof JComponent
				&& (tooltip = ((JComponent) comp).getToolTipText()) != null) {
			/* Rien à faire : on a déjà affecté tooltip dans le test.
			 * Il sera renvoyé à la fin de la méthode.
			 */
		
		} else if (comp instanceof JLabel) {
			JLabel label = (JLabel) comp;
			
			// Largeur de la cellule
			int width = columnModel.getColumn(colIndex).getWidth()
					- getIntercellSpacing().width;		// Moins la grille
			
			// Si le texte est trop long pour la largeur disponible
			if (getLargeurUtile(label) > width)
				tooltip = label.getText();				// Le texte en tool tip
		}// if JLabel
		
		// Dans tous les cas, renvoyer le tool tip, qu'il soit null ou défini
		return tooltip;
	}// getToolTipText
	
	/** Renvoie un en-tête de colonnes qui affiche des tool tips. */
	@SuppressWarnings("serial")
	@Override
	protected JTableHeader createDefaultTableHeader() {
		return new JTableHeader(columnModel) {
			
			@Override
			public String getToolTipText(MouseEvent e) {
				
				// L'index de la colonne survolée
				int columnIndex = columnModel.getColumnIndexAtX(e.getPoint().x);
				
				// Le nom de la colonne
                return columnIndex < 0
                		? null							// Pas de colonne
                		: table.getModel().getColumnName(// Le nom de la colonne
                		columnIndex);
            }// getToolTipText
			
		};// classe anonyme JTableHeader
	}// createDefaultTableHeader
}
