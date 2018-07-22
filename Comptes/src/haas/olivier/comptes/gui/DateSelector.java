package haas.olivier.comptes.gui;

import haas.olivier.util.Month;
import haas.olivier.comptes.gui.actions.MonthObservable;
import haas.olivier.comptes.gui.actions.MonthObserver;

import java.awt.Color;
import java.awt.Component;
import java.awt.GridLayout;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Dictionary;
import java.util.Hashtable;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * Un sélecteur de date.
 * <p>
 * Il s'agit d'une interface graphique de choix des dates sous la forme de trois
 * "sliders", respectivement pour l'année, le mois et le jour du mois.<br>
 * Un changement sur les sliders du mois ou de l'année entraîne la diffusion
 * d'un <code>PropertyChangeEvent</code> pour la propriété "mois", ainsi que la
 * remise du slider jour au 31 du mois.<br>
 * Un changement sur le slider du jour du mois entraîne la diffusion d'un
 * <code>PropertyChangeEvent pour la propriété "jour". Pour le slider jour, le
 * 31 équivaut au dernier jour du mois, même si le mois a moins de 31 jours.
 */
public class DateSelector
implements ChangeListener, MonthObserver, MouseWheelListener {

	/**
	 * Le composant graphique.
	 */
	private final JPanel panel = new JPanel();
	
	/**
	 * Objet observable gérant les mois et jours.
	 */
	private MonthObservable observable;

	/**
	 * Le slider définissant l'année.
	 */
	private JSlider sliderAnnee;
	
	/**
	 * Le slider définissant le mois.
	 */
	private JSlider sliderMois;
	
	/**
	 * Le slider définissant le jour.
	 */
	private JSlider sliderJour;

	/**
	 * Détermine s'il faut ou non réagir aux événements (utile pendant les
	 * phases d'ajustement).
	 */
	private boolean ignoreEvent = false;

	/**
	 * Construit un sélecteur de date.
	 * 
	 * @param debut			Le mois le plus ancien à proposer.
	 * @param observable	L'observable à notifier en cas de changement.
	 */
	public DateSelector(Month debut, MonthObservable observable) {

		// S'enregistrer mutuellement auprès de l'observable
		this.observable = observable;
		observable.addObserver(this);
		
		// Mois par défaut à l'instantiation
		Month initial = MonthObservable.getMonth();

		// Définir un panel pour le choix des dates
		panel.setLayout(new GridLayout(1, 3, 15, 0));

		// Ajouter le slider année au panel
		createSliderAnnee(debut, initial);
		panel.add(sliderAnnee);

		// Ajouter le slider mois au panel
		createSliderMois(initial);
		panel.add(sliderMois);

		// Ajouter le slider jour au panel
		createSliderJour();
		panel.add(sliderJour);

		// Créer une bordure invisible pour faire la marge
		panel.setBorder(
				BorderFactory.createLineBorder(new Color(0, 0, 0, 0), 10));

		// Ecouter les mouvements de la molette
		panel.addMouseWheelListener(this);
		
		// Enregistrer cet objet pour écouter les changements
		sliderAnnee.addChangeListener(this);
		sliderMois.addChangeListener(this);
		sliderJour.addChangeListener(this);
	}

	/**
	 * Crée un slider permettant de choisir l'année.
	 * 
	 * @param debut		Le mois le plus ancien à proposer. Seule son année nous
	 * 					intéresse ici.
	 * 
	 * @param initial	Le mois à sélectionner par défaut. Seule son année nous
	 * 					intéresse ici.
	 */
	private void createSliderAnnee(Month debut, Month initial) {

		// Définir les dates de début et de fin
		int anneeDebut = debut.getYear();
		int anneeFin = new Month().getYear() + 1;
		int anneeInitial = initial.getYear();

		// Définir un slider pour les années, du début à la fin
		sliderAnnee = new JSlider(JSlider.VERTICAL, anneeDebut, anneeFin,
				anneeInitial);

		// Définir les étiquettes du slider année
		Dictionary<Integer, JLabel> annees = new Hashtable<>();
		for (int annee = anneeDebut; annee <= anneeFin; annee++) {
			annees.put(annee, new JLabel("" + annee)); // Ajouter une étiquette
		}
		sliderAnnee.setLabelTable(annees);	// Attribuer le dico d'étiquettes
		sliderAnnee.setPaintLabels(true);	// Peindre les étiquettes

		// L'aspect du slider année
		sliderAnnee.setSnapToTicks(true);	// Ajuster le curseur aux marques
	}

	/**
	 * Crée un slider permettant de choisir le mois. L'échelle va de décembre
	 * N-1 à janvier N+1. Janvier ayant l'index 0, le slider est gradué de -1 à
	 * 12.
	 * 
	 * @param initial	Le mois à sélectionner par défaut.
	 */
	private void createSliderMois(Month initial) {

		// Définir un slider pour les mois, de décembre N-1 à janvier N+1
		sliderMois = new JSlider(JSlider.VERTICAL, 0, 13,
				initial.getNumInYear());						// Mois en cours

		// Partir d'un mois de décembre
		Month unMois = new Month(2000, 12);

		// Définir un dictionnaire pour les étiquettes du slider mois
		Dictionary<Integer, JLabel> tableMois = new Hashtable<>();

		// Ecrire les mois
		DateFormat moisFormatter = new SimpleDateFormat("MMM");
		for (int i=sliderMois.getMinimum(); i<=sliderMois.getMaximum(); i++) {
			tableMois.put(i,
					new JLabel(moisFormatter.format(unMois.getFirstDay())));
			unMois = unMois.getNext();						// Mois suivant
		}

		// Attribuer ces étiquettes au slider mois
		sliderMois.setLabelTable(tableMois);
		sliderMois.setPaintLabels(true);

		// L'aspect du slider mois
		sliderMois.setSnapToTicks(true); // Ajuster le curseur aux marques
	}

	/**
	 * Crée un slider de 1 à 31 pour choisir le jour.
	 */
	private void createSliderJour() {

		// Créer le slider
		sliderJour = new JSlider(JSlider.VERTICAL, 1, 31, 31);

		// Les labels
		Dictionary<Integer, JLabel> labels = new Hashtable<>();
		for (int n = 1; n <= 31; n++) {
			JLabel label = new JLabel("" + n);	// Etiquette du jour n
			label.setHorizontalAlignment(SwingConstants.RIGHT); // Alignement
			labels.put(n, label);				// Insérer cette étiquette
		}
		sliderJour.setLabelTable(labels);		// Attribuer les étiquettes
		sliderJour.setPaintLabels(true);		// Peindre les étiquettes

		// Ne pas s'arrêter entre les marques
		sliderJour.setSnapToTicks(true);
	}

	/**
	 * Calcule le nouveau mois défini par les sliders et le transmet.
	 */
	@Override
	public void stateChanged(ChangeEvent e) {

		// S'il faut ignorer les événements, on arrête
		if (ignoreEvent)
			return;

		// Un calendrier
		Calendar cal = Calendar.getInstance();

		// Mettre au 1er du mois pour éviter les erreurs ensuite
		cal.set(Calendar.DAY_OF_MONTH, 1);
		
		// Définir la date correspondante
		cal.set(Calendar.YEAR, sliderAnnee.getValue());		// Régler l'année
		cal.set(Calendar.MONTH, sliderMois.getValue() - 1);	// Régler le mois
		cal.set(Calendar.DAY_OF_MONTH, sliderJour.getValue());
		
		// Voir s'il faut changer d'année
		if (!sliderMois.getValueIsAdjusting()	// Pas déjà en train de changer
				&& (sliderMois.getValue() == 0		// Décembre N-1
				|| sliderMois.getValue() == 13)) {	// ou janvier N+1

			// Empêcher la gestion d'un autre ChangeEvent
			ignoreEvent = true;

			// Régler au bon numéro de mois et d'année
			sliderAnnee.setValue(cal.get(Calendar.YEAR));
			sliderMois.setValue(cal.get(Calendar.MONTH) + 1);

			// Autoriser à nouveau la gestion des événements
			ignoreEvent = false;
		}

		/*
		 * Plafonner la date au dernier jour du mois (ex:"31 avril" => 30 avril)
		 * Concrètement: si le jour du mois n'est plus le même que celui qu'on
		 * vient de définir (1er mai au lieu de "31 avril")
		 */
		if (cal.get(Calendar.DAY_OF_MONTH) < sliderJour.getValue()) {

			// Fixer la date au dernier jour du mois précédent
			cal.set(Calendar.DAY_OF_MONTH, 0);
		}

		// Réglages actuels
		Month actualMonth = MonthObservable.getMonth();
		Date actualDate = MonthObservable.getDate();

		// Nouvelles valeurs
		Month newMonth = new Month(cal.getTime());		// Nouveau mois
		Date newDate; // Nouvelle date
		if (cal.get(Calendar.DAY_OF_MONTH) == cal
				.getActualMaximum(Calendar.DAY_OF_MONTH)// dernier jour
				&& actualDate == null) {				// pas de date précise

			/*
			 * On est au dernier jour du mois et l'utilisateur se servait
			 * uniquement du mois jusqu'à présent.
			 */
			newDate = null;				// Garder le mois seul
		} else {
			newDate = cal.getTime();	// Configurer la date
		}

		// Le mois a-t-il changé ?
		if (!newMonth.equals(actualMonth)) {
			observable.setMonth(newMonth);	// Oui: notifier l'observable

			// Sinon, a-t-on une date ? A-t-elle changé ?
		} else if (newDate != null && !newDate.equals(actualDate)) {
			observable.setDate(newDate);	// Oui: notifier l'observable
		}
	}

	@Override
	public void monthChanged(Month month) {
		ignoreEvent = true;				// Bloquer les événements intempestifs
		sliderAnnee.setValue(month.getYear());				// Régler l'année
		sliderMois.setValue(month.getNumInYear());			// Régler le mois
		sliderJour.setValue(sliderJour.getMaximum());		// Régler au 31
		ignoreEvent = false;			// Rétablir la gestion des événements
	}// monthChanged

	@Override
	public void dateChanged(Date date) {
		ignoreEvent = true;				// Bloquer les événements intempestifs
		Calendar cal = Calendar.getInstance();				// Un calendrier...
		cal.setTime(date);									// ...à régler
		sliderAnnee.setValue(cal.get(Calendar.YEAR));		// Régler l'année
		sliderMois.setValue(cal.get(Calendar.MONTH) + 1);	// Régler le mois
		sliderJour.setValue(cal.get(Calendar.DAY_OF_MONTH));// Régler le jour
		ignoreEvent = false;			// Rétablir la gestion des événements
	}

	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {
		Component component = panel.getComponentAt(e.getPoint());
		if (component instanceof JSlider) {
			JSlider slider = (JSlider) component;
			slider.setValue(slider.getValue() - e.getWheelRotation());
		}
	}
	
	/**
	 * Renvoie le composant graphique du sélecteur de date.
	 * @return
	 */
	public Component getComponent() {
		return panel;
	}
}