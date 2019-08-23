/*
 * Copyright 2013-2018 Olivier HAAS. All rights reserved.
 */
package haas.olivier.diagram;

import java.awt.Component;
import java.awt.LayoutManager;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;

/** Un composant graphique affichant des étiquettes des ordonnées.
 * <p>
 * Les sous-composants sont des étiquettes créées à chaque changement de
 * l'échelle.<br>
 * Leur disposition est gérée par un {@link YLabelsLayout}.
 * <p>
 * Les sous-composants sont toujours dans l'ordre des graduations de l'échelle,
 * ce qui garantit une synchronisation avec <code>DiagramYLabelsLayout</code>.
 * 
 * @author Olivier Haas
 */
class YLabels extends JComponent {
	private static final long serialVersionUID = -9061985087818204928L;

	/** L'échelle de l'axe des ordonnées. */
	private final Echelle echelle;
	
	/** Le format numérique à utiliser. */
	private NumberFormat format = new DecimalFormat("#,##0.##");
	
	/** Construit un composant contenant les étiquettes des ordonnées.
	 * 
	 * @param echelle	L'échelle donnant les graduations à afficher.
	 */
	public YLabels(Echelle echelle) {
		this.echelle = echelle;
		setLayout(new YLabelsLayout(echelle));
		reloadGraduations();
	}// constructeur
	
	/** Crée une nouvelle étiquette.
	 * 
	 * @param text	Le texte de la nouvelle étiquette.
	 * @return		Une nouvelle étiquette.
	 */
	private static Component createLabel(String text) {
		JLabel label = new JLabel(text);
		label.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
		return label;
	}// createLabel

	@Override
	public void setLayout(LayoutManager layout) {
		if (layout instanceof YLabelsLayout) {
			super.setLayout(layout);
		} else {
			throw new IllegalArgumentException(
					"DiagramYLabels n'accepte que des DiagramYLabelsLayout");
		}// if
	}// setLayout

	/** Supprime les étiquettes actuelles et crée une nouvelle
	 * <code>JLabel</code> pour chaque graduation de l'échelle.
	 */
	public void reloadGraduations() {
		removeAll();
		echelle.updateGraduations();
		for (BigDecimal graduation : echelle.getGraduations())
			add(createLabel(getGraduationText(graduation)));
	}// reloadGraduations
	
	/** Renvoie le texte à afficher pour cette graduation.<br>
	 * Le texte correspond au format numérique défini par {@link #format}.
	 * 
	 * @param graduation	La graduation à afficher.
	 */
	private String getGraduationText(Number graduation) {
		return format.format(graduation);
	}// getGraduationText
	
	/** Renvoie la hauteur dont les étiquettes dépassent par rapport aux bords
	 * inférieur et supérieur du diagramme.
	 * 
	 * @return	La moitié de la hauteur de la première étiquette arrondie par
	 * 			excès, ou <code>0</code> s'il n'y a aucune étiquette.
	 */
	public int getVerticalOverflow() {
		if (getComponentCount() == 0)
			return 0;
		return (getComponent(0).getPreferredSize().height + 1) / 2;
	}// getVerticalOverflow
}
