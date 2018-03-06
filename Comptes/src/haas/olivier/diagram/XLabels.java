package haas.olivier.diagram;

import java.awt.Component;
import java.awt.LayoutManager;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;

/** Un dessinateur des étiquettes des abscisses.
 *
 * @author Olivier HAAS
 */
class XLabels extends JComponent {
	private static final long serialVersionUID = -5013370123377218463L;

	/** Le modèle du diagramme dont on veut dessiner les étiquettes. */
	private DiagramModel model;
	
	/** Construit un dessinateur d'étiquettes des abscisses affichant les
	 * étiquettes en face des graduations.
	 * 
	 * @param model		Le modèle du diagramme à représenter.
	 * @param layout	L'agenceur d'étiquettes.
	 */
	public XLabels(DiagramModel model, LayoutManager labelsLayout) {
		this.model = model;
		setLayout(labelsLayout);
		reloadXValues();
	}// constructeur
	
	/** Crée de nouvelles étiquettes correspondant aux valeurs en abscisses. */
	public void reloadXValues() {
		removeAll();
		for (Object xValue : model.getXValues())
			add(createLabel(xValue.toString()));
	}// reloadXValues
	
	/** Crée une nouvelle étiquette.
	 * 
	 * @param text	Le texte de la nouvelle étiquette.
	 * @return		Une nouvelle étiquette.
	 */
	private Component createLabel(String text) {
		JLabel label = new JLabel(text);
		label.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
		return label;
	}// createLabel
	
	/** Renvoie la largeur dont dépasse la première étiquette par rapport au
	 * bord gauche du diagramme.
	 * 
	 * @return	La moitié de la largeur de la première étiquette arrondie à
	 * 			l'excès, ou <code>0</code> s'il n'y a pas d'étiquette.
	 */
	public int getLeftOverflow() {
		if (getComponentCount() == 0)
			return 0;
		return (getComponent(0).getPreferredSize().width + 1) / 2;
	}// getHorizontalOverflow
	
	/** Renvoie la largeur dont sont susceptibles de dépasser par rapport au
	 * bord droit du diagramme.
	 * 
	 * @return	La moitié de la largeur optimale de la plus grande étiquette.
	 */
	public int getRightOverflow() {
		int overflow = 0;
		for (Component comp : getComponents()) {
			int compOverflow = comp.getPreferredSize().width / 2;
			overflow = Math.max(overflow, compOverflow);
		}
		return overflow;
	}// getRightOverflow
}
