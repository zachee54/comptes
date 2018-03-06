package haas.olivier.comptes.gui.diagram;

import java.util.ArrayList;
import java.util.List;

import haas.olivier.util.Month;
import haas.olivier.comptes.dao.DAOFactory;
import haas.olivier.diagram.DiagramModel;

import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/** Un sélecteur de la période à afficher.
 * <p>
 * Il s'agit d'un curseur permettant de déplacer la date de début du graphique,
 * sachant que la période actuelle est toujours affichée.
 *
 * @author Olivier HAAS
 */
@SuppressWarnings("serial")
class TimeSelector extends JSlider implements ChangeListener {

	/** Le modèle du diagramme. */
	private final DiagramModel model;
	
	/** Les mois d'origine. */
	private final List<Month> months = new ArrayList<>();
	
	/** Construit un sélecteur de période.
	 * 
	 * @param model	Le modèle de diagramme à modifier en fonction de la
	 * 				sélection.
	 */
	TimeSelector(DiagramModel model) {
		this.model = model;
		
		// Créer la liste des mois possibles
		Month startMonth = DAOFactory.getFactory().getDebut();
		Month actual = new Month();
		for (Month month = startMonth;
				!month.after(actual);
				month = month.getNext()) {
			months.add(month);
		}// for
		
		/* Les bornes du sélecteur.
		 * On utilise comme bornes intermédiaires le mois de chaque année égal
		 * au mois calendaire actuel (par exemple octobre). On aura donc des
		 * bornes [oct N1, oct N2], càd avec length % 12 == 1.
		 * Seul le tout premier mois est susceptible de concerner un autre mois
		 * calendaire.
		 * Si ce tout premier mois est octobre ou novembre, alors
		 * months.size() % 12 est égal à 1 ou 0 et il suffit de faire la
		 * division euclidienne par 12 pour obtenir le nombre de périodes
		 * différentes qu'on peut choisir. Sinon, il faut ajouter un bout
		 * d'année en plus 
		 */
		int maximum = actual.getYear() - 1;
		int nbAnnees = months.size() / 12;
		if (months.size() % 12 > 1)
			nbAnnees++;
		setMaximum(maximum);
		setMinimum(maximum - nbAnnees + 1);
		
		// Écouter les changements
		addChangeListener(this);
		
		// Valeur par défaut : avant-dernière valeur (s'il y en a au moins deux)
		setValue(Math.max(getMinimum(), getMaximum()-1));
		
		// Apparence
		setMajorTickSpacing(1);
		setPaintLabels(true);
		setPaintTicks(true);
		createStandardLabels(1);
	}// constructeur

	@Override
	public void stateChanged(ChangeEvent e) {
		int nbAnnees = getMaximum() - getValue() + 1;	// Nb d'années à voir
		model.setXValues(
				months.subList(
						Math.max(0, months.size() - nbAnnees * 12 - 1),
						months.size())
				.toArray());
	}// stateChanged
}
