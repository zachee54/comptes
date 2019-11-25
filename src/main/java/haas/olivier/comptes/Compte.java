/*
 * Copyright 2013-2018 Olivier HAAS. All rights reserved.
 */
package haas.olivier.comptes;

import haas.olivier.comptes.ctrl.DailySolde;
import haas.olivier.comptes.ctrl.SituationCritique;
import haas.olivier.comptes.dao.DAOFactory;
import haas.olivier.util.Month;

import java.awt.Color;
import java.io.IOException;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Comparator;
import java.util.Date;
import java.util.Random;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import org.hibernate.annotations.GenericGenerator;

/**
 * Un compte.
 * 
 * @author Olivier HAAS
 */
@Entity
public class Compte implements Comparable<Compte>, Serializable {
	private static final long serialVersionUID = 445986158370514520L;
	
	/**
	 * Le comparateur de la classe.
	 * <p>
	 * Il compare les comptes par :<ul>
	 * <li>	existence d'une date de clôture (les clôturés en dernier ; si les
	 * 		deux comptes ont une date de clôture, ils sont considérés comme
	 * 		égaux à ce stade)
	 * <li>	type
	 * <li>	nom ; les éventuels comptes sans nom passent en premier
	 * <li> identifiant.</ul>
	 */
	private static final Comparator<Compte> COMPARATOR =
			Comparator.comparing(		// Existence seule d'une date de clôture
					Compte::getCloture,
					Comparator.nullsFirst((d1, d2) -> 0))
			.thenComparing(
					Compte::getType)
			.thenComparing(
					Compte::getNom,
					Comparator.nullsFirst(Comparator.naturalOrder()))
			.thenComparing(
					Compte::getId,
					Comparator.nullsFirst(Comparator.naturalOrder()));
	
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
	public static final Compte COMPTE_EPARGNE =
			new Compte(-1, TypeCompte.SUIVI_EPARGNE);
	
	static {
		COMPTE_EPARGNE.nom = " Épargne";
	}
	
	/**
	 * Identifiant unique et persistant.
	 * <p>
	 * Il sert à sauvegarder la configuration des comptes dans les diagrammes.
	 * 
	 * @see {@link haas.olivier.comptes.gui.diagram.ComptesDiagramFactory#newModel()}
	 */
	@Id
	@GeneratedValue(generator="increment")
	@GenericGenerator(name="increment", strategy = "increment")
	private Integer id;
	
	/**
	 * Nom du compte.
	 */
	private String nom;
	
	/**
	 * Date d'ouverture.
	 */
	@Temporal(TemporalType.DATE)
	private Date ouverture;
	
	/**
	 * Date de clôture.
	 */
	@Temporal(TemporalType.DATE)
	private Date cloture;

	/**
	 * La couleur du compte dans les diagrammes.
	 */
	@Transient
	private Color color;
	
	/**
	 * L'état du compte.
	 */
	@Transient
	private CompteState state = new CompteBancaireState(TypeCompte.COMPTE_CARTE, null);
	
	protected Compte() {
	}
	
	/**
	 * Construit un compte.
	 * 
	 * @param id	L'identifiant unique du compte.
	 * @param type	Le type de compte.
	 */
	public Compte(Integer id, TypeCompte type) {
		this.id = id;
		setType(type);
		
		// Date d'ouverture par défaut
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
	 */
	public static void removeSuiviFrom(Month debut) {
		DAOFactory.getFactory().getHistoriqueDAO().removeFrom(debut);
		DAOFactory.getFactory().getSoldeAVueDAO().removeFrom(debut);
		DAOFactory.getFactory().getMoyenneDAO().removeFrom(debut);
	}

	/**
	 * Renvoie l'identifiant unique et persistant du compte.
	 * 
	 * @return	Un identifiant unique et persistant.
	 */
	public Integer getId() {
		return id;
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
	public void addHistorique(Month month, BigDecimal delta) {
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
	 */
	public void addPointages(Month month, BigDecimal delta) {
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
	 * derniers), selon leur type, selon leur nom (ceux qui n'ont pas de nom en
	 * premier), puis leur identifiant.
	 */
	@Override
	public int compareTo(Compte c) {
		return COMPARATOR.compare(this, c);
	}

	/**
	 * Deux comptes sont égaux s'ils ont le même type, le même nom, le même
	 * identifiant, et s'ils sont tous les deux clôturés ou tous les deux non
	 * clôturés.
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!(obj instanceof Compte))
			return false;
		
		Compte c = (Compte) obj;
		return compareTo(c) == 0;
	}

	/**
	 * Les éléments caractéristiques d'un compte sont l'existence d'une date de
	 * clôture, le type, le nom et l'identifiant.
	 */
	@Override
	public int hashCode() {
		int res = 13;
		int mul = 29;
		
		res = mul*res + Integer.hashCode(id);
		res = mul*res + (cloture == null ? 0 : -45782);
		res = mul*res + state.hashCode();
		res = mul*res + (nom == null ? 0 : nom.hashCode());
		return res;
	}
	
	/**
	 * Renvoie le nom du compte, suivi de son numéro s'il en a un.
	 */
	@Override
	public String toString() {
		Long numero = getNumero();
		return (numero == null) ? nom : String.format("%s %s", nom, numero);
	}
}
