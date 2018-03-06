/*
 * Copyright (c) 2018 Olivier HAAS - Tous droits réservés
 */
package haas.olivier.gui.table;

import haas.olivier.gui.util.VerticalLabel;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;

import javax.swing.BorderFactory;
import javax.swing.JTable;
import javax.swing.event.TableColumnModelEvent;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

/** Une table affichant verticalement le texte de ses en-têtes de colonnes. Cela
 * permet à la table de prendre moins de place en largeur, y compris si le texte
 * des en-têtes est long par rapport au contenu des cellules. 
 *
 * @author Olivier HAAS
 */
public class TableEtroite extends SmartTable {
	private static final long serialVersionUID = -8929939232334334244L;

	/** L'objet de rendu vertical des en-têtes de colonnes. <p>
	 * Il faut impérativement qu'il soit déclaré <code>static</code> pour être
	 * initialisé avant le constructeur de <code>JTable</code>, sinon le
	 * Renderer après l'instanciation sera <code>null</code>. */
	private static VerticalRenderer verticalRenderer = new VerticalRenderer();
	
	/** Attribue à la nouvelle colonne un Renderer présentant son texte
	 * verticalement. */
	@Override
	public void columnAdded(TableColumnModelEvent e) {
		super.columnAdded(e);
		TableColumn col = columnModel.getColumn(		// Nouvelle colonne
				e.getToIndex());
		col.setHeaderRenderer(verticalRenderer);		// Changer le Renderer
		col.setPreferredWidth(col.getMinWidth());		// Largeur minimale
	}// columnAdded
}// class TableEtroite

/** Un <code>TableCellRenderer</code> qui affiche le texte verticalement. <p>
 * Cet objet ne doit pas être défini comme <code>TableCellRenderer</code> par
 * défaut d'une table (ou d'un en-tête de table) dans son ensemble, mais
 * seulement pour une colonne.
 * <p>
 * En effet, d'une part, cette classe est un "wrap" d'un
 * <code>TableCellRenderer</code> sous-jacent, qui permet de conserver l'aspect
 * par défaut de la cellule mis à part l'orientation du texte.
 * <p>
 * D'autre part, le <code>TableCellRenderer</code> peut être modifié par Swing
 * lors d'un changement de Look&Feel. Cela entraînerait une instabilité de
 * l'apparence de la table, voire une exception liée au UI délégué car la classe
 * actuelle conserverait un lien vers le Renderer de l'ancien L&F.
 * <p>
 * Typiquement, cet objet est fait pour être utilisé avec une
 * <code>TableEtroite</code>.
 * 
 * @author Olivier HAAS
 */
class VerticalRenderer extends VerticalLabel
implements TableCellRenderer {
	private static final long serialVersionUID = 2132572047658201319L;
	
	/** Le composant de rendu enveloppé. Il est utilisé pour dessiner la cellule
	 * selon son aspect par défaut, hormis l'orientation du texte.
	 * Cet objet n'est pas défini une fois pour toutes : il est réévalué à
	 * chaque utilisation de la classe à partir du
	 * <code>TableCellRenderer</code> par défaut de la table. */
	private Component rendererComp;
	
	/** Construit </code>TableCellRenderer</code> qui affiche le texte
	 * verticalement. */
	public VerticalRenderer() {
		
		/* Forcer la transparence pour ne dessiner que le texte lui-même (le
		 * reste du dessin est délégué au composant de rendu par défaut). */
		setOpaque(false);
		
		/* Ajouter une bordure pour laisser une marge en bas avant le texte
		 * (donc à gauche, pour que ce soit en bas sur l'étiquette verticale) */
		setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 0));
		
		// Hauteur par défaut (75px, comme la largeur par défaut des colonnes)
		setPreferredSize(new Dimension(getPreferredSize().width, 75));
	}// constructeur
	
	/** Renvoie l'objet lui-même, avec le texte de la valeur de la cellule. */
	@Override
	public Component getTableCellRendererComponent(JTable table, Object value,
			boolean isSelected, boolean hasFocus, int row, int column) {
		
		// Récupérer le composant de rendu par défaut, mais sans texte
		rendererComp =
				table.getTableHeader().getDefaultRenderer()
				.getTableCellRendererComponent(
						table, null, isSelected, hasFocus, row, column);
		rendererComp.setPreferredSize(getPreferredSize());
		
		// Définir le texte à afficher verticalement
		String text = value == null ? "" : value.toString();// Le texte
		setText(text);										// Texte du Label
		setToolTipText(text);								// Tool tip
		return this;
	}// getTableCellRendererComponent
	
	/** Dessine le composant.
	 * En pratique, la méthode dessine le composant de rendu par défaut, sans le
	 * texte, puis dessine dans un deuxième temps le texte seul (c'est-à-dire
	 * l'objet actuel avec un fond transparent pour ne pas effacer le rendu par
	 * défaut) après rotation.
	 */
	@Override
	public void paint(Graphics g) {
		
		/* Dessiner le composant de rendu par défaut.
		 * Pour un rendu parfait on a besoin d'ajouter le composant dans
		 * l'arborescence des conteneurs. */
		Container parent = getParent();			// Le conteneur parent
		parent.add(rendererComp);				// Ajouter au parent
		rendererComp.setBounds(getBounds());	// Ajuster la taille 
		rendererComp.paint(g);					// Dessiner
		parent.remove(rendererComp);			// Retirer du conteneur
		
		// Dessiner le texte vertical (objet actuel avec transparence)
		super.paint(g);
	}// paint
}// class VerticalRenderer
