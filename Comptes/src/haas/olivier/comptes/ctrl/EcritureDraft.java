package haas.olivier.comptes.ctrl;

import haas.olivier.comptes.Compte;
import haas.olivier.comptes.Ecriture;
import haas.olivier.comptes.EcritureMissingArgumentException;
import haas.olivier.comptes.InconsistentArgumentsException;

import java.math.BigDecimal;
import java.util.Date;

/**
 * Un brouillon d'écritures, pour permettre de stocker des données temporaires
 * ou incomplètes avant d'instancier une écriture.
 * <p>
 * En pratique, cette classe est utilisée :<ul>
 * <li>	soit pour modifier une <code>Ecriture</code> existante. La classe sert
 * 		alors à stocker temporairement les modifications avant leur validation
 * 		et la mise à jour du modèle ;
 * <li>	soit pour créer une nouvelle <code>Ecriture</code>. Dans ce cas,
 * 		l'instance de départ est vide et les données peuvent y être définies
 * 		l'une après l'autre. Lorsque toutes les données souhaitées ont été
 * 		définies, la classe permet d'instancier l'<code>Ecriture</code>.
 * </ul>
 *
 * @author Olivier HAAS
 */
public class EcritureDraft {

	/**
	 * L'écriture d'origine, ou <code>null</code> si on est parti d'un brouillon
	 * vide.
	 */
	public final Ecriture e;
	
	/**
	 * L'identifiant de l'écriture.
	 */
	public Integer id = null;
	
	/**
	 * La date de l'écriture.
	 */
	public Date date = null;
	
	/**
	 * La date de pointage.
	 */
	public Date pointage = null;
	
	/**
	 * Le nom du tiers.
	 */
	public String tiers = null;
	
	/**
	 * Le libellé
	 */
	public String libelle = null;
	
	/**
	 * Le numéro de chèque.
	 */
	public Integer cheque = null;
	
	/**
	 * Le compte débité.
	 */
	public Compte debit = null;
	
	/**
	 * Le compte crédité.
	 */
	public Compte credit = null;
	
	/**
	 * Le montant.
	 */
	public BigDecimal montant = null;
	
	/**
	 * Construit un brouillon vide.
	 */
	public EcritureDraft() {
		this(null);
	}
	
	/**
	 * Construit un brouillon à partir de l'écriture spécifiée.
	 * 
	 * @param e		L'<code>Ecriture</code> dont il faut récupérer les
	 * 				informations.
	 */
	public EcritureDraft(Ecriture e) {
		
		// Mémoriser l'écriture
		this.e = e;
		
		// Si l'écriture n'est pas null
		if (e != null) {
			
			// Récupérer les propriétés de l'écriture
			id		= e.id;
			date	= e.date;
			pointage= e.pointage;
			tiers	= e.tiers;
			libelle	= e.libelle;
			cheque	= e.cheque;
			debit	= e.debit;
			credit	= e.credit;
			montant	= e.montant;
		}
	}

	/**
	 * Tente d'instancier une <code>Ecriture</code> à partir des données
	 * saisies.
	 * <p>
	 * Cela suppose que les propriétés de l'objet aient été définies
	 * manuellement.
	 * 
	 * @return	Une nouvelle écriture.
	 * 
	 * @throws EcritureMissingArgumentException
	 * 			Si les données sont insuffisantes pour instancier une écriture.
	 * 
	 * @throws InconsistentArgumentsException 
	 * 			En cas d'incohérence des données.
	 */
	public Ecriture createEcriture()
			throws EcritureMissingArgumentException,
			InconsistentArgumentsException {
		return new Ecriture(id, date, pointage, debit, credit, montant,
				libelle, tiers, cheque);
	}
}
