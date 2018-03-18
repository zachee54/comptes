package haas.olivier.comptes.gui;

import haas.olivier.comptes.Compte;
import haas.olivier.comptes.ctrl.SituationCritique;
import haas.olivier.util.Month;
import haas.olivier.comptes.gui.actions.CompteObservable;
import haas.olivier.comptes.gui.actions.CompteObserver;
import haas.olivier.comptes.gui.actions.MonthObservable;
import haas.olivier.comptes.gui.actions.MonthObserver;
import haas.olivier.comptes.gui.actions.SoldesObservable;
import haas.olivier.comptes.gui.actions.SoldesObserver;

import java.awt.Color;
import java.awt.GridLayout;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

/**
 * Un panel présentant les soldes utiles.
 * <p>
 * La classe propose d'instancier deux types de classes dérivées, selon que l'on
 * souhaite suivre un compte bancaire ou un compte budgétaire.
 * 
 * @author Olivier HAAS
 */
@SuppressWarnings("serial")
public abstract class SoldesPanel extends JPanel implements MonthObserver,
		CompteObserver, SoldesObserver {

	/**
	 * La couleur du solde théorique.
	 */
	protected static final Color COLOR_THEORIQUE = new Color(0, 0, 127);
	
	/**
	 * La couleur du solde à vue.
	 */
	protected static final Color COLOR_A_VUE = new Color(0, 127, 0);
	
	/**
	 * La couleur de la situation critique.
	 */
	protected static final Color COLOR_CRITIQUE = new Color(127, 63, 0);

	/**
	 * Le format monétaire.
	 */
	protected static final DecimalFormat NF = new DecimalFormat(
			"#,##0.00 €;- #,##0.00 €", new DecimalFormatSymbols(Locale.FRANCE));

	/**
	 * Renvoie une instance du type souhaité.
	 * 
	 * @param bancaires
	 * 			Indique s'il faut adapter l'affichage pour des comptes bancaires
	 * 			(true) ou budgétaires (false).
	 * @param monthObservable
	 * 			Objet à observer pour les changements de mois.
	 * @param compteObservable
	 *			Objet à observer pour les changements de compte
	 * @param dataObservable
	 * 			Objet à observer pour les changements de données
	 */
	public static SoldesPanel getInstance(boolean bancaires,
			MonthObservable monthObservable, CompteObservable compteObservable,
			SoldesObservable soldesObservable) {

		if (bancaires) {
			// Retourner un panel des soldes pour les comptes bancaires
			return new SoldesBancairesPanel(
					monthObservable, compteObservable, soldesObservable);
			
		} else {
			// Retourner un panel des soldes pour les comptes budgétaires
			return new SoldesBudgetPanel(monthObservable, compteObservable,
					soldesObservable);
		}
	}

	/**
	 * Le compte sélectionné.
	 */
	protected Compte compte;

	/**
	 * Constructeur privé ne contenant que les dispositions communes.
	 */
	protected SoldesPanel(MonthObservable monthObservable,
			CompteObservable compteObservable,
			SoldesObservable soldesObservable) {

		// Écouter les changements
		monthObservable.addObserver(this);	// de mois/dates
		compteObservable.addObserver(this);	// de comptes
		soldesObservable.addObserver(this);	// de soldes

		// Mémoriser le compte (pas d'accès statique dans CompteObservable)
		compte = compteObservable.getCompte();

		// Laisser les instances se mettre à jour après s'être initialisées.
	}

	/**
	 * Affiche le solde théorique et solde à vue ou la moyenne à la fin du mois
	 * spécifié.
	 */
	protected abstract void setSoldesToMonth(Month month);

	/**
	 * Affiche le solde théorique et le solde à vue ou la moyenne à la date
	 * spécifiée.
	 * 
	 * @throws IOException
	 */
	protected abstract void setSoldesToDate(Date date) throws IOException;

	/**
	 * Met à jour tous les soldes affichés.
	 * 
	 * @throws IOException
	 */
	public void update() throws IOException {
		// Mettre à jour solde théorique et solde à vue au mois ou à la date
		if (MonthObservable.getDate() == null) {
			setSoldesToMonth(MonthObservable.getMonth());	// Mois sélectionné
		} else {
			setSoldesToDate(MonthObservable.getDate());	// Ou Date sélectionnée
		}
	}

	@Override
	public void monthChanged(Month month) {
		// Écrire les soldes à ce mois
		if (compte != null)			// Sauf s'il n'y a pas de compte
			setSoldesToMonth(month);
	}

	@Override
	public void dateChanged(Date date) throws IOException {
		setSoldesToDate(date);		// Écrire les soldes à cette date
	}

	@Override
	public void compteChanged(Compte compte) throws IOException {
		this.compte = compte;		// Modifier le compte
		update();					// Mettre à jour
	}

	@Override
	public void soldesChanged() throws IOException {
		update();					// Si les soldesont changé, recalculer tout
	}
}// class SoldesPanel

/**
 * Un <code>SoldesPanel</code> pour les comptes bancaires.
 * <p>
 * Affiche le solde théorique, le solde à vue et la situation critique du
 * compte.
 * 
 * @author Olivier HAAS
 */
@SuppressWarnings("serial")
class SoldesBancairesPanel extends SoldesPanel {

	/**
	 * L'étiquette contenant le solde théorique.
	 */
	private JLabel theo;
	
	/**
	 * L'étiquette contenant le solde à vue.
	 */
	private JLabel aVue;
	
	/**
	 * L'étiquette contenant le montant critique.
	 */
	private JLabel crit;
	
	/**
	 * L'étiquette contenant la date critique.
	 */
	private JLabel date;
	
	/**
	 * Le format de date.
	 */
	private final DateFormat dateFormat = new SimpleDateFormat("d MMM");

	/**
	 * Construit un panneau des soldes pour les comptes bancaires.
	 * 
	 * @param monthObservable	L'observable de mois.
	 * @param compteObservable	L'observable de compte.
	 * @param soldesObservable	L'observable de soldes.
	 */
	protected SoldesBancairesPanel(MonthObservable monthObservable,
			CompteObservable compteObservable,
			SoldesObservable soldesObservable) {
		super(monthObservable, compteObservable, soldesObservable);

		// Créer un panel
		setLayout(new GridLayout(2, 3, 5, 2));

		// Définir les textes
		JLabel labelTheo = new JLabel("Solde théorique", SwingConstants.CENTER);
		JLabel labelAVue = new JLabel("Solde à vue", SwingConstants.CENTER);
		JLabel labelCritique = new JLabel("Solde critique",
				SwingConstants.CENTER);

		// Définir les JLabels contenant les montants
		theo = new JLabel();
		aVue = new JLabel();
		crit = new JLabel();
		date = new JLabel();

		// Aligner les montants
		theo.setHorizontalAlignment(SwingConstants.CENTER);
		aVue.setHorizontalAlignment(SwingConstants.CENTER);
		crit.setHorizontalAlignment(SwingConstants.CENTER);
		date.setHorizontalAlignment(SwingConstants.CENTER);

		// Coloriser
		theo.setForeground(COLOR_THEORIQUE);
		aVue.setForeground(COLOR_A_VUE);
		crit.setForeground(COLOR_CRITIQUE);
		date.setForeground(COLOR_CRITIQUE);

		// Insérer dans le panel des soldes
		add(labelAVue);		// Libellé solde à vue
		add(labelTheo);		// Libellé solde théorique
		add(labelCritique);	// Libellé solde critique
		add(new JLabel());	// Un vide

		add(aVue);			// Solde à vue
		add(theo);			// Solde théorique
		add(crit);			// Solde critique
		add(date);			// Date critique
	}

	@Override
	protected void setSoldesToMonth(Month month) {
		theo.setText(NF.format(compte.getHistorique(month)));
		aVue.setText(NF.format(compte.getSoldeAVue(month)));
		updateSoldeCritique();
	}

	@Override
	protected void setSoldesToDate(Date date) throws IOException {
		Month month = new Month(date);
		theo.setText(NF.format(compte.getHistoriqueIn(month).getSoldeAt(date)));
		aVue.setText(NF.format(compte.getSoldeAVueIn(month).getSoldeAt(date)));
	}

	@Override
	public void update() throws IOException {
		super.update();
		updateSoldeCritique();
	}
	
	/**
	 * Met à jour les libellés "solde critique...le..." en fontion de la
	 * situation du compte au cours du mois spécifié. 
	 * <p>
	 * Si le compte n'est pas un compte bancaire (ce qui ne devrait jamais
	 * arriver), la méthode ne fait rien.
	 */
	private void updateSoldeCritique() {
		if (compte.getType().isBancaire()) {
			try {
				SituationCritique situationCritique =
						compte.getSituationCritique();
				Date dateCritique =						// Date de la situation
						situationCritique.getDateCritique();
				BigDecimal soldeCritique =				// Montant critique
						situationCritique.getSoldeMini();

				// Convertir au format texte
				crit.setText(NF.format(soldeCritique));
				date.setText("le " + dateFormat.format(dateCritique));
				
			} catch (IOException e) {
				crit.setText(null);
				date.setText(null);
				Logger.getLogger(getClass().getName()).log(Level.SEVERE,
						"Erreur de lecture des données pour calculer la situation critique",
						e);
			}
		}
	}
}// class SoldesBancairesPanel

/**
 * Un <code>SoldesPanel</code> pour les comptes budgétaires.
 * <p>
 * Affiche le solde théorique (opérations du mois) et la moyenne.
 * 
 * @author Olivier Haas
 */
@SuppressWarnings("serial")
class SoldesBudgetPanel extends SoldesPanel {

	/**
	 * L'étiquette contenant le solde théorique.
	 */
	private JLabel theo;
	
	/**
	 * L'étiquette contenant la moyenne.
	 */
	private JLabel moy;

	/**
	 * Construit un panneau de soldes pour les comptes budgétaires.
	 * 
	 * @param monthObservable	L'observable de mois.
	 * @param compteObservable	L'observable de compte.
	 * @param soldesObservable	L'observable de soldes.
	 */
	protected SoldesBudgetPanel(MonthObservable monthObservable,
			CompteObservable compteObservable,
			SoldesObservable soldesObservable) {
		super(monthObservable, compteObservable, soldesObservable);

		// Créer un panel
		setLayout(new GridLayout(2, 2, 5, 2));

		// Définir les textes
		JLabel labelTheo = new JLabel("Solde théorique", SwingConstants.CENTER);
		JLabel labelAVue = new JLabel("Moyenne", SwingConstants.CENTER);

		// Définir les JLabels contenant les montants
		theo = new JLabel();
		moy = new JLabel();

		// Aligner les montants
		theo.setHorizontalAlignment(SwingConstants.CENTER);
		moy.setHorizontalAlignment(SwingConstants.CENTER);

		// Coloriser
		theo.setForeground(COLOR_THEORIQUE);
		moy.setForeground(COLOR_A_VUE);

		// Insérer dans le panel des soldes
		add(labelTheo);								// Libellé solde théorique
		add(labelAVue);								// Libellé moyenne

		add(theo);									// Solde théorique
		add(moy);									// Moyenne
	}

	@Override
	protected void setSoldesToMonth(Month month) {
		theo.setText(NF.format(compte.getHistorique(month)));
		moy.setText(NF.format(compte.getMoyenne(month)));
	}

	@Override
	protected void setSoldesToDate(Date date) throws IOException {
		Month month = new Month(date);
		theo.setText(NF.format(compte.getHistoriqueIn(month).getSoldeAt(date)));
		moy.setText(NF.format(compte.getMoyenne(month)));
	}
}// class SoldesBudgetPanel