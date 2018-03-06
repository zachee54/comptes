package haas.olivier.comptes.gui;

import haas.olivier.comptes.Compte;
import haas.olivier.comptes.CompteBancaire;
import haas.olivier.comptes.CompteBudget;
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
import java.util.Map.Entry;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

@SuppressWarnings("serial")
/** Un panel présentant les soldes utiles.
 * La classe propose d'instancier deux types de classes dérivées, selon que l'on
 * souhaite suivre un compte bancaire ou un compte budgétaire.
 * @author Olivier HAAS
 */
public abstract class SoldesPanel extends JPanel implements MonthObserver,
		CompteObserver, SoldesObserver {

	// Les couleurs de soldes
	protected static final Color COLOR1 = new Color(0, 0, 127);
	protected static final Color COLOR2 = new Color(0, 127, 0);
	protected static final Color COLOR3 = new Color(127, 63, 0);

	/** Format monétaire */
	protected static final DecimalFormat NF = new DecimalFormat(
			"#,##0.00 €;- #,##0.00 €", new DecimalFormatSymbols(Locale.FRANCE));

	/** Le format de date pour l'affichage de la date critique. */
	protected static final DateFormat DF = new SimpleDateFormat("d MMM");

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
	}// getInstance

	/** Le compte sélectionné. */
	protected Compte compte;

	/** Constructeur privé ne contenant que les dispositions communes. */
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
	}// constructeur

	/**
	 * Affiche le solde théorique et solde à vue ou la moyenne à la fin du mois
	 * spécifié.
	 */
	protected abstract void setSoldesToMonth(Month month);

	/**
	 * Affiche le solde théorique et le solde à vue ou la moyenne à la date
	 * spécifiée.
	 */
	protected abstract void setSoldesToDate(Date date);

	/** Met à jour tous les soldes affichés. */
	public void update() {
		// Mettre à jour solde théorique et solde à vue au mois ou à la date
		if (MonthObservable.getDate() == null) {
			setSoldesToMonth(MonthObservable.getMonth());	// Mois sélectionné
		} else {
			setSoldesToDate(MonthObservable.getDate());	// Ou Date sélectionnée
		}
	}// update

	@Override
	public void monthChanged(Month month) {
		// Écrire les soldes à ce mois
		if (compte != null)			// Sauf s'il n'y a pas de compte
			setSoldesToMonth(month);
	}// monthChanged

	@Override
	public void dateChanged(Date date) {
		setSoldesToDate(date);		// Écrire les soldes à cette date
	}

	@Override
	public void compteChanged(Compte compte) {
		this.compte = compte;		// Modifier le compte
		update();					// Mettre à jour
	}

	@Override
	public void soldesChanged() {
		update();					// Si les soldesont changé, recalculer tout
	}
}// class SoldesPanel

@SuppressWarnings("serial")
/** Un SoldesPanel pour les comptes bancaires.
 * Affiche le solde théorique, le solde à vue et la situation critique du
 * compte.
 * @author Olivier HAAS
 */
class SoldesBancairesPanel extends SoldesPanel {

	// Les étiquettes contenant les montants (modifiables)
	private JLabel theo, aVue, crit, date;

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
		theo.setForeground(COLOR1);
		aVue.setForeground(COLOR2);
		crit.setForeground(COLOR3);
		date.setForeground(COLOR3);

		// Insérer dans le panel des soldes
		add(labelAVue);		// Libellé solde à vue
		add(labelTheo);		// Libellé solde théorique
		add(labelCritique);	// Libellé solde critique
		add(new JLabel());	// Un vide

		add(aVue); // Solde à vue
		add(theo); // Solde théorique
		add(crit); // Solde critique
		add(date); // Date critique
	}// constructeur

	@Override
	protected void setSoldesToMonth(Month month) {
		theo.setText(NF.format(compte.getHistorique(month)));
		aVue.setText(NF.format(compte.getSoldeAVue(month)));
		setSoldeCritique(month);
	}

	@Override
	protected void setSoldesToDate(Date date) {
		theo.setText(NF.format(compte.getHistoriqueAt(date)));
		aVue.setText(NF.format(compte.getSoldeAVueAt(date)));
	}

	@Override
	public void update() {
		super.update();
		setSoldeCritique(MonthObservable.getMonth());
	}// update
	
	/** Met à jour les libellés "solde critique...le..." en fontion de la
	 * situation du compte au cours du mois spécifié. 
	 * <p>
	 * Si le compte n'est pas un compte bancaire (ce qui ne devrait jamais
	 * arriver), la méthode ne fait rien.
	 * 
	 * @param month	Le mois au titre duquel afficher la situation critique.
	 */
	private void setSoldeCritique(Month month) {
		if (compte instanceof CompteBancaire) {
			try {
				Entry<Date, BigDecimal> situationCritique =
						((CompteBancaire) compte).getSituationCritique(month);
				Date dateCritique =						// Date de la situation
						situationCritique.getKey();
				BigDecimal soldeCritique =				// Montant critique
						situationCritique.getValue();

				// Convertir au format texte
				crit.setText(NF.format(soldeCritique));
				date.setText("le " + DF.format(dateCritique));
				
			} catch (IOException e) {
				crit.setText(null);
				date.setText(null);
				
				e.printStackTrace();
			}// try
		}// if compte bancaire
	}// setSoldeCritique
}// class SoldesBancairesPanel

/** Un <code>SoldesPanel</code> pour les comptes budgétaires.
 * <p>
 * Affiche le solde théorique (opérations du mois) et la moyenne.
 * 
 * @author Olivier Haas
 */
@SuppressWarnings("serial")
class SoldesBudgetPanel extends SoldesPanel {

	// Les étiquettes contenant les montants (modifiables)
	private JLabel theo, moy;

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
		theo.setForeground(COLOR1);
		moy.setForeground(COLOR2);

		// Insérer dans le panel des soldes
		add(labelTheo);								// Libellé solde théorique
		add(labelAVue);								// Libellé moyenne

		add(theo);									// Solde théorique
		add(moy);									// Moyenne
	}// constructeur

	@Override
	protected void setSoldesToMonth(Month month) {
		theo.setText(NF.format(compte.getHistorique(month)));
		moy.setText(NF.format(((CompteBudget) compte).getMoyenne(month)));
	}// setSoldesToMonth

	@Override
	protected void setSoldesToDate(Date date) {
		theo.setText(NF.format(compte.getHistoriqueAt(date)));
		moy.setText(NF.format(((CompteBudget) compte)
				.getMoyenne(new Month(date))));
	}// setSoldesToDate
}// class SoldesBudgetPanel