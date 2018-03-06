package haas.olivier.comptes;

import haas.olivier.comptes.ctrl.DailySolde;
import haas.olivier.comptes.ctrl.SituationCritique;
import haas.olivier.comptes.dao.DAOFactory;
import haas.olivier.util.Month;

import java.awt.Color;
import java.io.IOException;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.Random;

/**
 * Un compte.
 * 
 * @author Olivier HAAS
 */
public class Compte implements Comparable<Compte>, Serializable {
	private static final long serialVersionUID = 445986158370514520L;
	
	/**
	 * Le compte budgétaire virtuel permettant de retracer les versements en
	 * épargne.
	 * <p>
	 * Ce compte est virtuel au sens où il n'est pas sauvegardé dans la couche
	 * de données. Son seul intérêt est de permettre de sauvegarder le montant
	 * des versements d'épargne au titre de chaque mois, ainsi que la moyenne
	 * glissante. Ces montants sont enregistrés comme ceux d'un compte normal,
	 * sous l'identifiant <code>-1</code>.<br>
	 * L'intérêt de l'instancier en <code>CompteBudget</code> est que la moyenne
	 * glissante sur 12 mois est calculée et mise à jour automatiquement comme
	 * pour un compte implémentant un poste budgétaire quelconque.
	 */
	public static final Compte compteEpargne =
			new Compte(TypeCompte.SUIVI_EPARGNE);
	
	static {
		compteEpargne.nom = " Épargne";
	}
	
	/**
	 * Nom du compte.
	 */
	private String nom;
	
	/**
	 * Date d'ouverture.
	 * */
	private Date ouverture;
	
	/**
	 * Date de clôture.
	 */
	private Date cloture;

	/**
	 * La couleur du compte dans les diagrammes.
	 */
	private Color color;
	
	/**
	 * L'état du compte.
	 */
	private CompteState state;
	
	/**
	 * Construit un compte.
	 * 
	 * @param type	Le type de compte.
	 */
	public Compte(TypeCompte type) {
		setType(type);
		
		/* Date d'ouverture par défaut */
		Month debut = DAOFactory.getFactory().getDebut();
		if (debut != null)
			ouverture = debut.getFirstDay();
	}
	
	/**
	 * Supprime tous les suivis à compter du mois donné.
	 * <p>
	 * Les historiques de
	 * comptes, les soldes à vue, les moyennes, le suivi de l'épargne sont
	 * supprimés.<br>
	 * Toutefois, pour les comptes qui supportent le calcul de la moyenne
	 * glissante (y compris le compte virtuel d'épargne), les moyennes
	 * glissantes au titre des n-1 premiers mois (n étant le nombre de mois sur
	 * lesquels s'étend la moyenne glissante) sont réécrites pour tenir compte
	 * des incidences de la période immédiatement antérieure au mois spécifié.
	 * 
	 * @param debut	Le mois à partir duquel supprimer les historiques
	 *            
	 * @throws IOException
	 * 				Si la modification échoue dans la couche de données.
	 */
	public static void removeSuiviFrom(Month debut) throws IOException {
		DAOFactory.getFactory().getHistoriqueDAO().removeFrom(debut);
		DAOFactory.getFactory().getSoldeAVueDAO().removeFrom(debut);
		DAOFactory.getFactory().getMoyenneDAO().removeFrom(debut);
	}

	/**
	 * Renvoie le nom du compte.
	 */
	public String getNom() {
		return nom;
	}
	
	/**
	 * Renvoie le numéro du compte.
	 * 
	 * @return	Le numéro de compte, ou <code>null</code> si le compte n'a pas
	 * 			de numéro.
	 */
	public Long getNumero() {
		return state.getNumero();
	}
	
	/**
	 * Renvoie le type de compte.
	 */
	public TypeCompte getType() {
		return state.getType();
	}

	/**
	 * Renvoie la date d'ouverture du compte.
	 */
	public Date getOuverture() {
		return ouverture;
	}
	
	/**
	 * Renvoie la date de clôture du compte.
	 * 
	 * @return	La date de clôture, ou <code>null</code> si le compte n'est pas
	 * 			clôturé.
	 */
	public Date getCloture() {
		return cloture;
	}
	
	/**
	 * Renvoie la couleur du compte dans les diagrammes.
	 */
	public Color getColor() {
		
		/* Définir une couleur au hasard s'il n'y en a pas encore */
		if (color == null) {
			Random random = new Random();
			color = new Color(random.nextInt(256*256*256));
		}
		
		return color;
	}
	
	/**
	 * Définit le nom du compte.
	 * 
	 * @param nom	Le nouveau nom du compte.
	 */
	public void setNom(String nom) {
		this.nom = nom;
	}
	
	/**
	 * Définit le numéro de compte.
	 * 
	 * @param numero	Le nouveau numéro. Peut être <code>null</code>.
	 */
	public void setNumero(Long numero) {
		state.setNumero(numero);
	}
	
	/**
	 * Définit le type du compte.
	 * 
	 * @param type	Le nouveau type du compte.
	 */
	public void setType(TypeCompte type) {
		state = type.isBancaire()
				? new CompteBancaireState(type, state)
				: new CompteBudgetState(type);
	}

	/**
	 * Définit la date d'ouverture du compte.
	 * 
	 * @param ouverture	La nouvelle date d'ouverture.
	 */
	public void setOuverture(Date ouverture) {
		if (ouverture == null)
			throw new IllegalArgumentException(
					"La date d'ouverture ne peut pas être null");
		this.ouverture = ouverture;
	}
	
	/**
	 * Définit la date de clôture du compte.
	 * 
	 * @param cloture	La nouvelle date de clôture, ou <code>null</code> si le
	 * 					compte n'est pas clôturé.
	 */
	public void setCloture(Date cloture) {
		this.cloture = cloture;
	}
	
	/**
	 * Modifie la couleur du compte dans les diagrammes.
	 */
	public void setColor(Color color) {
		this.color = color;
	}
	
	/**
	 * Récupère le solde théorique du compte au titre d'un mois donné.
	 * 
	 * @return	Le montant du solde de ce mois (ou zéro si non défini)
	 */
	public BigDecimal getHistorique(Month month) {
		return state.getSuivi(this, DAOFactory.getFactory().getHistoriqueDAO(),
				month);
	}
	
	/**
	 * Renvoie les soldes théoriques de chaque jour pour le mois spécifié.
	 * 
	 * @param month	Le mois.
	 * @return		Les soldes théoriques de chaque jour du mois.
	 * 
	 * @throws IOException
	 */
	public DailySolde getHistoriqueIn(Month month) throws IOException {
		return new DailySolde(this, month, false);
	}

	/**
	 * Renvoie le solde réel du compte à la fin d'un mois.<br>
	 * Cette méthode ne tient compte que des pointages.
	 * 
	 * @param month	Le mois à la fin duquel le solde est souhaité.
	 * @return		Le solde réel du compte à la fin du mois.
	 */
	public BigDecimal getSoldeAVue(Month month) {
		return state.getSuivi(this, DAOFactory.getFactory().getSoldeAVueDAO(),
				month);
	}
	
	/**
	 * Renvoie les soldes à vue de chaque jour pour le mois spécifié.<br>
	 * Cette méthode n'a de sens que pour les comptes bancaires.
	 * 
	 * @param month	Le mois.
	 * @return		Les soldes à vue pour chaque jour du mois.
	 * 
	 * @throws IOException
	 */
	public DailySolde getSoldeAVueIn(Month month) throws IOException {
		return new DailySolde(this, month, true);
	}

	/**
	 * Renvoie la moyenne sur 12 mois glissants.<br>
	 * Cette méthode n'a de sens que pour les comptes budgétaires.
	 * 
	 * @param month	Le dernier des 12 mois sur lesquels porte la moyenne.
	 * 
	 * @returns		La moyenne sur 12 mois glissants, ou zéro si aucune
	 * 				opération sur les 12 mois.
	 */
	public BigDecimal getMoyenne(Month month) {
		return state.getSuivi(this, DAOFactory.getFactory().getMoyenneDAO(),
				month);
	}
	
	/** 
	 * Modifie l'historique du compte au titre d'un mois en lui ajoutant un
	 * montant spécifié.
	 * 
	 * @param month	Le mois concerné.
	 * 
	 * @param delta	Le montant à ajouter (ou soustraire si négatif). S'il est
	 * 				<code>null</code> ou égal à zéro, la méthode ne fait rien.
	 * 
	 * @throws IOException
	 */
	public void addHistorique(Month month, BigDecimal delta)
			throws IOException {
		if (delta != null && delta.signum() != 0)
			state.addHistorique(this, month, delta);
	}
	
	/** 
	 * Modifie l'historique des pointages du compte au titre d'un mois en lui
	 * ajoutant un montant spécifié.
	 * 
	 * @param month	Le mois concerné.
	 * 
	 * @param delta	Le montant à ajouter (ou soustraire si négatif). S'il est
	 * 				<code>null</code> ou égal à zéro, la méthode ne fait rien.
	 * 
	 * @throws IOException
	 */
	public void addPointages(Month month, BigDecimal delta)
			throws IOException {
		if (delta != null && delta.signum() != 0)
			state.addPointage(this, month, delta);
	}

	/**
	 * Renvoie le sens dans lequel il faut lire l'écriture en consultant le
	 * compte.
	 * <p>
	 * Lorsqu'une écriture apparaît dans le relevé du compte, le montant doit en
	 * principe être reproduit tel quel si le compte figure au crédit, ou pour
	 * son opposé si le compte figure au débit.
	 * <p>
	 * Le résultat est inversé les comptes budgétaires, puisqu'ils utilisent une
	 * comptabilisation "en miroir".
	 * <p>
	 * Cette méthode donne la clé de lecture en fonction du compte au débit et
	 * du compte au crédit.
	 * 
	 * @param debit		Le compte figurant au débit.
	 * 
	 * @param credit	Le compte figurant au débit.
	 * 
	 * @return			1 si le montant doit figurer tel quel dans le relevé,
	 * 					-1 si c'est l'opposé du montant qui doit figurer,
	 * 					0 si aucun des comptes ne correspond à l'instance.
	 */
	public int getViewSign(Compte debit, Compte credit) {
		return state.getViewSign(this, debit, credit);
	}
	
	/**
	 * Renvoie l'impact d'une écriture sur le solde de ce compte.<br>
	 * L'impact est égal au montant de l'écriture ou à son inverse, suivant que
	 * ce compte est crédité ou débité.
	 * <p>
	 * Comme les <code>CompteBudget</code> enregistrent les soldes à l'envers,
	 * l'impact est également inversé.<br>
	 * Si l'écriture ne mouvemente pas ce compte, l'impact est nul.
	 * 
	 * @return	Le montant de l'écriture ou son opposé selon les cas, ou 0 si
	 * 			l'écriture ne mouvemente pas ce compte.
	 */
	public BigDecimal getImpactOf(Ecriture e) {
		switch(getViewSign(e.debit, e.credit)) {	// Selon le sens de lecture
		case 1:		return e.montant;				// Montant de l'écriture
		case -1:	return e.montant.negate();		// Montant opposé
		default:	return BigDecimal.ZERO;			// Pas concerné (donc zéro)
		}
	}
	
	/**
	 * Détermine si ce compte est un compte d'épargne.
	 * <p>
	 * En pratique, il s'agit de vérifier si son type correspond à un type
	 * "épargne", à retenir pour le calcul de l'épargne mensuelle.
	 * 
	 * @return	<code>true</code> si c'est un compte d'épargne.
	 */
	public boolean isEpargne() {
		return getType().isEpargne();
	}
	
	/**
	 * Détermine la situation critique du compte bancaire.<br>
	 * Cette méthode n'a de sens que pour les comptes bancaires.
	 * <p>
	 * Cette méthode renvoie une <code>Map.Entry</code> contenant la date et le
	 * solde critiques.<br>
	 * La date critique s'entend de la première date, postérieure à la date du
	 * jour, à laquelle le solde théorique devient négatif, ou le plus
	 * faiblement positif.<br>
	 * Le solde critique est le solde le plus faible prévu au-delà de la date du
	 * jour.
	 * <p>
	 * Seul le mois spécifié est examiné, jusqu'au jour de la première écriture
	 * du mois suivant
	 * 
	 * @return		La situation critique du compte.
	 * 
	 * @throws IOException
	 */
	public SituationCritique getSituationCritique()
			throws IOException {
		return new SituationCritique(this, new Date());
	}

	/**
	 * Compare deux comptes selon qu'ils sont clôturés (les clôturés passent en
	 * derniers), selon leur type, selon leur nom, puis leur numéro.<br>
	 * Si l'un des deux n'est pas numéroté, il passe en dernier.
	 */
	@Override
	public int compareTo(Compte c) {
		
		/* Comparer l'existence de dates de clôture */
		if (cloture != c.cloture) {
			if (cloture == null)
				return -1;
			if (c.cloture == null)
				return 1;
		}
		
		/* Comparer par les types */
		TypeCompte type = getType();
		TypeCompte type2 = c.getType();
		if (type != type2)
			return type.compareTo(type2);

		/* Comparer par les noms */
		if (nom != c.nom) {
			if (c.nom == null)
				return 1;
			if (nom == null)
				return -1;

			int compNoms = nom.compareTo(c.nom);
			if (compNoms != 0)
				return compNoms;
		}
		
		/* Comparer les numéros */
		Long numero = getNumero();
		Long numero2 = c.getNumero();
		if (numero != numero2) {
			if (numero == null)
				return 1;
			if (numero2 == null)
				return -1;
			
			return numero.compareTo(numero2);
		}
		return 0;
	}

	/**
	 * Deux comptes sont égaux s'ils ont le même type, le même nom, le même
	 * numéro, et la même situation de clôture (tous les deux clôturés ou tous
	 * les deux non clôturés).<br>
	 * La date d'ouverture et la date exacte de clôture n'ont pas d'importance. 
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!(obj instanceof Compte))
			return false;
		
		Compte c = (Compte) obj;
		return state.equals(c.state)
				&& nom.equals(c.nom)
				&& (cloture == null) == (c.cloture == null);
	}

	@Override
	public int hashCode() {
		int res = 13;
		int mul = 29;
		
		res = mul*res + (cloture == null ? 0 : cloture.hashCode());
		res = mul*res + state.hashCode();
		res = mul*res + (nom == null ? 0 : nom.hashCode());
		return res;
	}
	
	/**
	 * Renvoie le nom du compte, suivi de son numéro s'il en a un.
	 */
	public String toString() {
		return nom + state.toString();
	}
}
