/*
 * Copyright 2013-2018 Olivier HAAS. All rights reserved.
 */
package haas.olivier.comptes;

import haas.olivier.comptes.ctrl.EcritureController;
import haas.olivier.comptes.ctrl.EcritureDraft;
import haas.olivier.comptes.dao.DAOFactory;
import haas.olivier.util.Month;

import java.io.IOException;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Une opération récurrente capable de générer une écriture pré-définie.
 * <p>
 * Les instances sont de trois types, selon l'écriture qu'ils génèrent:<ul>
 * <li>	une écriture avec un montant prédéfini (par exemple le prélèvement
 * 		bancaire d'un abonnement)
 * <li>	dont le montant dépend du solde d'un compte bancaire (par exemple pour
 * 		solder les comptes cartes vers le compte courant)
 * <li>	ou dont le montant dépend d'une autre écriture (par exemple les
 * 		prélèvements sur des versements en assurance-vie).
 * </ul>
 * 
 * @author Olivier Haas
 */
public class Permanent implements Comparable<Permanent>, Serializable {
	private static final long serialVersionUID = 8897891019381288870L;
	
	/**
	 * Le Logger de cette classe.
	 */
	private static final Logger LOGGER =
			Logger.getLogger(Permanent.class.getName());

	/**
	 * Génère pour ce mois les écritures de toutes les instances.
	 */
	public static void createAllEcritures(Month month) {
		Permanent cursor = null;
		try {
			// Liste des écritures générées
			List<Ecriture> generated = new ArrayList<>();
			
			// Récupérer tous les Permanents
			Iterable<Permanent> permanents = DAOFactory.getFactory()
					.getPermanentDAO().getAll();

			// Parcourir les Permanents
			for (Permanent p : permanents) {
				cursor = p;
				Ecriture e = p.createEcriture(month); // L'écriture

				// Mémoriser l'écriture à ajouter
				generated.add(e);
			}
			
			// Ajouter au modèle en mettant à jour les données de suivi
			EcritureController.add(generated);

		} catch (IOException e1) {
			LOGGER.log(Level.SEVERE,
					"Impossible d'enregistrer une des écritures générées", e1);

		} catch (Exception e1) {
			LOGGER.log(Level.SEVERE,
					(cursor == null)
					? "Impossible de générer une des écritures"
					: "Impossible de générer l'écriture "+cursor,
					e1);
		}
	}

	/**
	 * L'identifiant de l'opération permanente.
	 */
	private Integer id;
	
	/**
	 * Le nom de l'opération permanente.
	 */
	private String nom;
	
	/**
	 * Les dates (quantièmes) des écritures à générer en fonction du mois.
	 */
	private final Map<Month, Integer> jours;
	
	/**
	 * Le compte à débiter par les écritures générées.
	 */
	private Compte debit;
	
	/**
	 * Le compte à créditer par les écritures générées.
	 */
	private Compte credit;
	
	/**
	 * Le nom du tiers dans les écritures à générer.
	 */
	private String tiers;
	
	/**
	 * Le libellé des écritures à générer.
	 */
	private String libelle;

	/**
	 * Détermine si l'écriture doit être pointée automatiquement.
	 */
	private boolean pointer;
	
	/**
	 * L'état déterminante le comportement de l'opération permanente.
	 */
	private PermanentState state = new PermanentFixe(Collections.emptyMap());

	/**
	 * Construit une opération permanente.
	 * 
	 * @param id		L'identifiant de l'opération.
	 * @param nom		Le nom de l'opération.
	 * @param debit		Le compte à débiter.
	 * @param credit	Le compte à créditer.
	 * @param libelle	Le libellé de l'écriture à générer.
	 * @param tiers		Le nom du tiers dans l'écriture à générer.
	 * @param pointer	<code>true</code> si les écritures générées doivent être
	 * 					pointées par défaut.
	 * @param jours		Le planning des jours de l'opération à générer, selon
	 * 					les mois.
	 */
	// TODO Simplifier le constructeur et prévoir des valeurs par défaut
	public Permanent(Integer id, String nom, Compte debit, Compte credit,
			String libelle, String tiers, boolean pointer,
			Map<Month, Integer> jours) {
		if (debit == null						// Il faut un compte de débit
				|| credit == null				// Il faut un compte de crédit
				|| jours == null) {				//Il faut un planning, même vide
			throw new IllegalArgumentException();
		}
		
		this.id = id;
		this.nom = (nom == null) ? "" : nom;	// Nom non null pour compareTo()
		this.debit = debit;
		this.credit = credit;
		this.libelle = libelle;
		this.tiers = tiers;
		this.pointer = pointer;
		this.jours = jours;
	}

	/**
	 * Génère une écriture au titre du mois donné.
	 * <p>
	 * La méthode purge aussi les occurrences de jours et de montants obsolètes
	 * de plus de 12 mois.
	 * 
	 * @return	Une écriture, ou <code>null</code> si les données ne permettent
	 * 			pas de générer une écriture valide.
	 * 
	 * @throws EcritureMissingArgumentException
	 * 			Si les données sont insuffisantes pour instancier une écriture.
	 * 
	 * @throws InconsistentArgumentsException
	 * 			Si aucune date ou aucun montant n'est spécifié ni pour le mois
	 * 			concerné ni pour un mois antérieur.
	 */
	public Ecriture createEcriture(Month month)
			throws EcritureMissingArgumentException, InconsistentArgumentsException {

		// Trouver la date

		// Parcourir les mois à la recherche du plus récent avant la cible
		Month maxMonth = null;
		for (Month m : jours.keySet()) {
			if (!month.before(m)) { // Mois antérieur à la cible
				if (maxMonth == null) {
					// Rien de défini: prendre ce mois par défaut
					maxMonth = m;

				} else if (maxMonth.before(m)) {
					// m est plus récent que le max actuel

					maxMonth = m; // m est le nouveau max

				}
			}
		}

		// Si pas de date antérieure définie, lever une exception
		if (maxMonth == null) {
			throw new InconsistentArgumentsException(
					"Opération permanente  " + nom
					+ ": pas de date trouvée avant le mois spécifié");
		}

		// Déterminer la date
		Calendar cal = Calendar.getInstance();	// Calendrier
		Integer jour = jours.get(maxMonth);		// Le quantième voulu
		cal.setTime(month.getFirstDay());		// Partir du mois spécifié
		cal.setLenient(true);		// Accepter les quantièmes hors champ (1-31)
		cal.set(Calendar.DAY_OF_MONTH, jour);	// Changer le jour du mois
		Date date = cal.getTime();				// Date définitive

		// Purger la liste des dates
		Month today = Month.getInstance();
		Iterator<Month> it = jours.keySet().iterator();
		while (it.hasNext()) {

			// Ce mois est-il obsolète de plus de 12 mois ?
			Month m2 = it.next().getTranslated(12);
			if (m2.before(maxMonth) && m2.before(today)) {
				it.remove();					// Supprimer
			}
		}

		// Créer l'écriture
		EcritureDraft draft = new EcritureDraft();
		draft.date = date;
		draft.debit = debit;
		draft.credit = credit;
		draft.montant = getMontant(month);
		draft.libelle = libelle;
		draft.tiers = tiers;
		if (pointer)
			draft.pointage = date;
		
		return draft.createEcriture();
	}
	
	/**
	 * Renvoie l'identifiant de l'opération permanente.
	 * 
	 * @return	L'identifiant de l'opération permanente. Peut être
	 * 			<code>null</code>.
	 */
	public Integer getId() {
		return id;
	}
	
	/**
	 * Définit l'identifiant de l'opération permanente.
	 * <p>
	 * L'identifiant peut être défini une seule fois.
	 * 
	 * @param id	L'identifiant de l'opération permanente.
	 * 
	 * @throws IllegaleStateException
	 * 				Si l'identifiant a déjà été défini.
	 */
	public void setId(int id) {
		if (this.id != null)
			throw new IllegalStateException("Identifiant déjà défini : " + id);
		this.id = id;
	}
	
	/**
	 * Renvoie le montant de l'écriture à générer (dépend de l'implémentation
	 * concrète).
	 * 
	 * @param month	Le mois au titre duquel générer l'écriture.
	 * 
	 * @throws EcritureMissingArgumentException
	 * 				Si les données sont insuffisantes pour instancier
	 * 				l'écriture.
	 * 
	 * @throws InconsistentArgumentException
	 * 				Si des informations manquent pour définir le montant au
	 * 				titre de ce mois.
	 */
	BigDecimal getMontant(Month month)
			throws EcritureMissingArgumentException,
			InconsistentArgumentsException {
		return state.getMontant(month);
	}
	
	/**
	 * Renvoie l'état actuel de l'opération permanente.
	 * 
	 * @return	L'état actuel de l'opération permanente.
	 */
	public PermanentState getState() {
		return state;
	}
	
	/**
	 * Modifie l'état de l'opération permanente.
	 * 
	 * @param state	Le nouvel état de l'opération permanente.
	 */
	public void setState(PermanentState state) {
		if (state == null)
			throw new NullPointerException();
		this.state = state;
	}
	
	/**
	 * Renvoie le nom de l'opération permanente.
	 * 
	 * @return	Le nom de l'opération permanente.
	 */
	public String getNom() {
		return nom;
	}
	
	/**
	 * Modifie le nom de l'opération permanente.
	 * 
	 * @param nom	Le nouveau nom de l'opération permanente.
	 */
	public void setNom(String nom) {
		this.nom = nom;
	}
	
	/**
	 * Renvoie le compte debité.
	 * 
	 * @return	Le compte débité.
	 */
	public Compte getDebit() {
		return debit;
	}
	
	/**
	 * Modifie le compte débité.
	 * 
	 * @param debit	Le nouveau compte débité.
	 */
	public void setDebit(Compte debit) {
		this.debit = debit;
	}
	
	/**
	 * Renvoie le compte crédité.
	 * 
	 * @return	Le compte crédité.
	 */
	public Compte getCredit() {
		return credit;
	}
	
	/**
	 * Modifie le compte crédité.
	 * 
	 * @param credit	Le nouveau compte crédité.
	 */
	public void setCredit(Compte credit) {
		this.credit = credit;
	}
	
	/**
	 * Renvoie le nom du tiers.
	 * 
	 * @return	Le nom du tiers.
	 */
	public String getTiers() {
		return tiers;
	}
	
	/**
	 * Modifie le nom du tiers.
	 * 
	 * @param tiers	Le nom du nouveau tiers.
	 */
	public void setTiers(String tiers) {
		this.tiers = tiers;
	}
	
	/**
	 * Renvoie le libellé.
	 * 
	 * @return	Le libellé.
	 */
	public String getLibelle() {
		return libelle;
	}
	
	/**
	 * Modifie le libellé.
	 * 
	 * @param libelle	Le nouveau libellé.
	 */
	public void setLibelle(String libelle) {
		this.libelle = libelle;
	}
	
	/**
	 * Indique si les écritures générées sont pointées automatiquement.
	 * 
	 * @return	<code>true</code> si les écritures sont pointées
	 * 			automatiquement.
	 */
	public boolean isPointee() {
		return pointer;
	}
	
	/**
	 * Renvoie les dates (quantièmes) des écritures à générer en fonction des
	 * mois.
	 * 
	 * @return	Les dates des écritures à générer en fonction des mois.
	 */
	public Map<Month, Integer> getJours() {
		return jours;
	}
	
	/**
	 * Définit si les écritures doivent être pointées automatiquement.
	 * 
	 * @param pointer	<code>true</code> si les écritures doivent être pointées
	 * 					automatiquement.
	 */
	public void setPointee(boolean pointer) {
		this.pointer = pointer;
	}

	@Override
	public boolean equals(Object obj) {
		
		// Même instance ?
		if (this == obj) {
			return true;
		}

		// Objet de la bonne classe ?
		if (!(obj instanceof Permanent)) {
			return false;
		}

		// Égaux si impossible à départager par compareTo
		return compareTo((Permanent) obj) == 0;
	}
	
	/**
	 * Renvoie le nom du Permanent.
	 */
	public String toString() {
		if (nom == null || nom.equals("")) {
			return "(Sans nom)";
		} else {
			return nom;
		}
	}

	/**
	 * Compare selon les noms, puis les identifiants.
	 */
	@Override
	public int compareTo(Permanent p) {
		int result = nom.compareTo(p.nom);	// Comparer selon les noms
		if (result == 0) {
			// Ou selon le hashcode
			result = new Integer(hashCode()).compareTo(p.hashCode());
		}
		return result;
	}
	
	@Override
	public int hashCode() {
		int res = 11;
		int mul = 23;
		
		res = mul*res + (id == null ? 0 : id.hashCode());
		res = mul*res + (nom == null ? 0 : nom.hashCode());
		return res;
	}
}
