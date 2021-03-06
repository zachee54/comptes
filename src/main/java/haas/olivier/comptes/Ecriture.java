/*
 * Copyright 2013-2021 Olivier HAAS. All rights reserved.
 */
package haas.olivier.comptes;


import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

/**
 * Une écriture comptable.
 * <p>
 * Suivant les classes concrètes dérivées de <code>Compte</code> utilisées en
 * débit et crédit, il peut s'agir d'une recette, d'une dépense, d'un virement
 * interne ou d'un virement de poste à poste.
 * 
 * @author Olivier Haas
 */
@Entity
public class Ecriture implements Comparable<Ecriture>, Serializable {
	private static final long serialVersionUID = 6211208191867018351L;

	/**
	 * Comparateur d'écritures selon les dates de pointages plutôt que selon
	 * l'ordre naturel.
	 * <p>
	 * Les écritures non pointées sont classées à la fin.<br>
	 * Les écritures de même pointage sont triées en fonction de leur date (tri
	 * croissant), ou en dernier recours en fonction de leur identifiant
	 * (croissant aussi).
	 * <p>
	 * Cette classe n'est imbriquée ici que par motif de cohérence.
	 * 
	 * @author Olivier HAAS
	 */
	public static class SortPointages
	implements Serializable, Comparator<Ecriture> {
		private static final long serialVersionUID = 979484917860753125L;

		/**
		 * Ce tri doit être consistant avec <code>equals</code>, ce qui
		 * implique d'utiliser les mêmes critères de départage que
		 * {@link Ecriture#compareTo(Ecriture)}.
		 */
		@Override
		public int compare(Ecriture e1, Ecriture e2) {
			int result;						// Résultat intermédiaire
			if (e1.pointage == null && e2.pointage != null) {		//1 pointage
				return 1;					// Le non pointé après

			} else if (e1.pointage != null && e2.pointage == null) {// l'autre
				return -1;					// Le pointé avant

			} else if (e1.pointage != null && e2.pointage != null) {// les deux
				// Tri croissant par date de pointage. Départage à suivre
				result = e1.pointage.compareTo(e2.pointage);
				
			} else {												// aucun
				// null/null -> égalité ! Départage à suivre
				result = 0;
			}
			
			// En cas d'égalité des pointages, départager par la date
			if (result == 0) {
				result = e1.date.compareTo(e2.date);	// Dates croissantes
			}
			
			// Départager par l'identifiant si besoin
			if (result == 0) {
				if (e1.id != null && e2.id != null) {
					return e1.id.compareTo(e2.id);
				} else if (e1.id != null && e2.id == null) {
					return -1;
				} else if (e1.id == null && e2.id != null) {
					return 1;
				}
			}
			
			return result;			// Si on arrive ici, renvoyer le résultat
		}
	}

	/**
	 * Format de date pour l'affichage.
	 */
	private static final DateFormat DF = new SimpleDateFormat("dd/MM/yyyy");

	/**
	 * Les qualifications possibles d'une écriture au regard de l'épargne.
	 * 
	 * @author Olivier HAAS
	 */
	public static enum TypeEpargne {
		EPARGNE, PRELEVEMENT, NEUTRE;
	}

	// Propriétés définies par le constructeur
	
	/**
	 * Identifiant unique.
	 */
	@Id
	@GeneratedValue
	public Integer id;
	
	/**
	 * Date d'écriture.
	 */
	@Temporal(TemporalType.DATE)
	public Date date;
	
	/**
	 * Compte débité.
	 */
	@ManyToOne(cascade = CascadeType.ALL)
	public Compte debit;
	
	/**
	 * Compte crédité.
	 */
	@ManyToOne(cascade = CascadeType.ALL)
	public Compte credit;
	
	/**
	 * Montant.
	 */
	public BigDecimal montant;
	
	/**
	 * Nom de la personne.
	 */
	public String tiers;
	
	/**
	 * Libellé.
	 */
	public String libelle;
	
	/**
	 * Numéro de chèque.
	 */
	public Integer cheque;
	
	/**
	 * Date de pointage.
	 */
	@Temporal(TemporalType.DATE)
	public Date pointage;

	// Propriété calculée par le constructeur
	
	/**
	 * Qualification à l'égard de l'épargne.
	 */
	@Transient
	public TypeEpargne epargne;

	/**
	 * Vérifie la cohérence des arguments en vue de l'instanciation d'une
	 * écriture.
	 * 
	 * @param id		L'identifiant de l'écriture.
	 * @param date		La date de l'écriture.
	 * @param pointage	La date de pointage.
	 * @param debit		Le compte débité.
	 * @param credit	Le compte crédité.
	 * @param montant	Le montant de l'écriture.
	 * 
	 * @throws EcritureMissingArgumentException
	 * 					Si un argument essentiel est <code>null</code>.
	 * 
	 * @throws InconsistentArgumentException
	 * 					Si le compte débité est le même que le compte crédité,
	 * 					ou si la date de pointage est antérieure à la date de
	 * 					l'écriture.
	 */
	private static void checkArguments(Integer id, Date date, Date pointage,
			Compte debit, Compte credit, BigDecimal montant)
					throws EcritureMissingArgumentException,
					InconsistentArgumentsException {
	
		// Contrôler la présence de données minimales
		
		if (date == null) {
			throw new EcritureMissingArgumentException(
					"La date de l'écriture ne peut pas être null", id);
		}
		
		if (debit == null) {
			throw new EcritureMissingArgumentException(
					"Le compte débité ne peut pas être null", id);
		}
		
		if (credit == null) {
			throw new EcritureMissingArgumentException(
					"Le compte crédité ne peut pas être null", id);
		}
		
		if (montant == null) {
			throw new EcritureMissingArgumentException(
					"Le montant de l'écriture ne peut pas être null", id);
		}
	
		// Vérifier qu'on ne mouvemente pas un compte sur lui-même
		if (debit == credit) {
			throw new InconsistentArgumentsException(
					"Un compte ne peut pas être à la fois débité et crédité dans la même écriture");
		}
	
		// Vérifier que le pointage est postérieur ou égal à la date d'écriture
		if (pointage != null && pointage.before(date)) {
			throw new InconsistentArgumentsException(
					"Une écriture ne peut pas être pointée avant sa propre date");
		}
	}

	protected Ecriture() {
	}
	
	/**
	 * Construit une écriture.
	 * <p>
	 * Les paramètres correspondent aux données minimum pour que l'écriture soit
	 * régulière. Ils ne sont pas modifiables ultérieurement.
	 * 
	 * @param id		Identifiant de l'écriture.<br>
	 * 					Un identifiant négatif indique qu'elle n'a pas encore
	 * 					été enregistrée.
	 * @param date		La date.
	 * @param pointage	La date de pointage.
	 * @param debit		Le compte débité.
	 * @param credit	Le compte crédité.
	 * @param montant	Le montant.<br>
	 * 					S'il est négatif, on retient le montant opposé, et le
	 * 					compte de débit et le compte de crédit sont échangés.
	 * @param libelle	Le libellé.
	 * @param tiers		Le tiers.
	 * @param cheque	Le numéro de chèque.
	 * 
	 * @throws EcritureMissingArgumentException
	 * 					Si l'instanciation de l'écriture a échoué en raison de
	 * 					l'absence d'un argument nécessaire.
	 * 
	 * @throws InconsistentArgumentsException
	 * 					Si le compte débité est le même que le compte crédité,
	 * 					ou si la date de pointage est antérieure à la date de
	 * 					l'écriture.
	 */
	public Ecriture(Integer id, Date date, Date pointage, Compte debit,
			Compte credit, BigDecimal montant, String libelle, String tiers,
			Integer cheque)
					throws EcritureMissingArgumentException,
					InconsistentArgumentsException {

		// Vérifier la cohérence des arguments fournis
		checkArguments(id, date, pointage, debit, credit, montant);
		
		// Si le montant est négatif, on enregistre tout à l'envers
		if (montant.signum() < 0) {
			Compte tmp = debit;							// Échanger débit/crédit
			debit = credit;
			credit = tmp;
			montant = montant.negate();					// Montant opposé
		}
		
		// Définir les propriétés simples
		this.id = id;
		this.date = date;
		this.pointage = pointage;
		this.debit = debit;
		this.credit = credit;
		this.montant = montant;
		this.tiers = tiers;
		this.libelle = libelle;
		this.cheque = cheque;

		// Arrondir le montant au centième le plus proche si nécessaire
		montant = montant.setScale(2, RoundingMode.HALF_UP);
		
		// Déterminer le type d'épargne à partir des types de comptes
		updateTypeEpargne();
	}
	
	/**
	 * Met à jour le type d'épargne en fonction des types de comptes.
	 */
	private void updateTypeEpargne() {
		if (!debit.isEpargne() && credit.isEpargne()) {
			epargne = TypeEpargne.EPARGNE;
		} else if (debit.isEpargne() && !credit.isEpargne()) {
			epargne = TypeEpargne.PRELEVEMENT;
		} else {
			epargne = TypeEpargne.NEUTRE;
		}
	}
	
	/**
	 * Renvoie l'identifiant unique.
	 */
	public Integer getId() {
		return id;
	}
	
	/**
	 * Renvoie la date.
	 */
	public Date getDate() {
		return date;
	}
	
	/**
	 * Modifie la date.
	 */
	public void setDate(Date date) {
		this.date = date;
	}
	
	/**
	 * Renvoie le compte débité.
	 */
	public Compte getDebit() {
		return debit;
	}
	
	/**
	 * Modifie le compte débité.
	 */
	public void setDebit(Compte debit) {
		this.debit = debit;
		updateTypeEpargne();
	}
	
	/**-
	 * Renvoie le compte crédité.
	 */
	public Compte getCredit() {
		return credit;
	}
	
	/**
	 * Modifie le compte crédité.
	 */
	public void setCredit(Compte credit) {
		this.credit = credit;
		updateTypeEpargne();
	}
	
	/**
	 * Renvoie le montant.
	 */
	public BigDecimal getMontant() {
		return montant;
	}
	
	/**
	 * Modifie le montant.
	 */
	public void setMontant(BigDecimal montant) {
		this.montant = montant;
	}
	
	/**
	 * Renvoie le nom du tiers.
	 */
	public String getTiers() {
		return tiers;
	}
	
	/**
	 * Modifie le nom du tiers.
	 */
	public void setTiers(String tiers) {
		this.tiers = tiers;
	}
	
	/**
	 * Renvoie le libellé.
	 */
	public String getLibelle() {
		return libelle;
	}
	
	/**
	 * Modifie le libellé.
	 */
	public void setLibelle(String libelle) {
		this.libelle = libelle;
	}
	
	/**
	 * Renvoie le numéro du chèque.
	 * 
	 * @return	Le numéro du chèque, ou <code>null</code> s'il n'y a pas de
	 * 			chèque.
	 */
	public Integer getCheque() {
		return cheque;
	}
	
	/**
	 * Modifie le numéro du chèque.
	 * 
	 * @param cheque	Le nouveau numéro de chèque, ou <code>null</code> pour
	 * 					supprimer le numéro actuel.
	 */
	public void setCheque(Integer cheque) {
		this.cheque = cheque;
	}
	
	/**
	 * Renvoie la date de pointage.
	 */
	public Date getPointage() {
		return pointage;
	}
	
	/**
	 * Modifie la date de pointage.
	 */
	public void setPointage(Date pointage) {
		this.pointage = pointage;
	}
	
	/**
	 * Détermine si le libellé ou le nom du tiers de cette écriture contient
	 * l'expression régulière spécifiée.
	 * <p>
	 * La comparaison se fait sur une chaîne en minuscules uniquement.
	 */
	public boolean matches(Pattern pattern) {
		String[] strings = {tiers, libelle};			// Pour chacun des deux
		for (String s : strings) {
			if (s != null) {							// S'il y a une chaîne
				Matcher matcher =
						pattern.matcher(s.toLowerCase());// Créer un Matcher
				if (matcher.find()) {
					return true;						// Trouvé
				}
			}
		}
		return false;									// Pas trouvé
	}

	@Override
	public String toString() {
		return String.format(
				"Ecriture: %s Débit: %s  crédit: %s Date: %s Pointage le: %s Tiers: %s, Libellé: %s, Chèque n°%s",
				montant,
				debit,
				credit,
				DF.format(date),
				(pointage == null ? "" : DF.format(pointage)),
				tiers,
				libelle,
				cheque,
				(epargne == TypeEpargne.NEUTRE ? "" : epargne));
	}

	/**
	 * Range les écritures de la plus ancienne à la plus récente.
	 * <p>
	 * Elles sont classées:<ul>
	 * <li>	par dates
	 * <li>	ou en cas d'égalité, par numéros de chèques : les écritures avec un
	 * 		chèque sont classées après les autres ; entre elles, elles sont
	 * 		classées dans l'ordre des numéros de chèques(un numéro de chèque
	 * 		plus élevé indique que l'opération est plus tardive, l'écriture sera
	 * 		donc classée après l'autre)
	 * <li>	par pointages : les non pointées après les autres ; entre elles,
	 * 		par ordre chronologique des pointages
	 * <li>	et en dernier ressort, par identifiant. En principe, les écritures
	 * 		sans identifiant sont utilisées de manière éphémère et n'ont jamais
	 * 		besoin d'être comparées. Mais pour la complétude de la
	 * 		spécification, elles sont classées après celles qui ont un
	 * 		identifiant.
	 * </ul>
	 * Délibérément, on considère qu'une écriture qui a été modifiée par
	 * l'utilisateur (et qui, donc, porte en principe le même identifiant) peut
	 * être égale à l'écriture d'origine. Cela permet une sécurité
	 * supplémentaire en vue d'éviter les doublons, puisque les écritures sont
	 * ensuite stockées dans un Set.
	 */
	@Override
	public int compareTo(Ecriture e) {
		
		// Comparer les dates
		int result = date.compareTo(e.date);

		/*
		 * Sinon, comparer les n° de chèques (ordre inverse aussi).
		 * Les écritures avec chèque sont placées après les écritures sans
		 * chèque (il fallait bien faire un choix pour garder la transitivité !)
		 */
		if (result == 0) {
			if (cheque == null && e.cheque != null) {
				result = -1;			// Pas de chèque : en premier
			} else if (cheque != null && e.cheque == null) {
				result = 1;				// Celui qui a un chèque : en dernier
			}
			else if (cheque != null && e.cheque != null) {
				result = cheque.compareTo(e.cheque);			// Deux chèques
			}
		}
		
		// Sinon, comparer les pointages
		if (result == 0) {
			if (pointage == null && e.pointage != null) {		// 1 pointage
				return 1;					// Le non pointé après

			} else if (pointage != null && e.pointage == null) {// l'autre
				return -1;					// Le pointé avant

			} else if (pointage != null && e.pointage != null) {// les deux
				// Tri par date de pointage. Départage à suivre
				result = pointage.compareTo(e.pointage);

			} else {												// aucun
				// null/null -> égalité ! Départage à suivre
				result = 0;
			}
		}

		// Sinon, comparer les identifiants
		if (result == 0) {
			if (id != null && e.id != null) {
				return id.compareTo(e.id);
			} else if (id != null && e.id == null) {
				return -1;
			} else if (id == null && e.id != null) {
				return 1;
			}
		}

		return result;
	}

	/**
	 * Deux écritures sont égales si elles ne peuvent pas être départagées par
	 * la méthode <code>compareTo</code>.
	 * <p>
	 * Autrement dit, elles sont égales ssi elles ont les mêmes dates, même
	 * pointage, mêmes numéros de chèques, et mêmes identifiants.
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Ecriture) {
			return compareTo((Ecriture) obj) == 0;
		} else {
			return false;
		}
	}
	
	@Override
	public int hashCode() {
		int res= 7;
		int mul = 17;
		
		res = mul*res + (id == null ? 0 : id.hashCode());
		res = mul*res + (pointage == null ? 0 : pointage.hashCode());
		res = mul*res + (cheque == null ? 0 : cheque.hashCode());
		res = mul*res + (date == null ? 0 : date.hashCode());
		return res;
	}
}
