package haas.olivier.comptes.gui.settings;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Map;

import haas.olivier.comptes.Compte;
import haas.olivier.comptes.Permanent;
import haas.olivier.comptes.PermanentFixe;
import haas.olivier.comptes.PermanentProport;
import haas.olivier.comptes.PermanentSoldeur;
import haas.olivier.comptes.dao.DAOFactory;
import haas.olivier.comptes.dao.PermanentDAO;
import haas.olivier.util.Month;

/**
 * Un contrôleur de <code>Permanent</code>. Permet de pré-visualiser les
 * modifications à apporter à un <code>Permanent</code>.
 */
class PermanentController implements Comparable<PermanentController> {
	
	/**
	 * L'opération permanente contrôlée. Peut être <code>null</code> dans le cas
	 * d'une opération à créer.
	 */
	private final Permanent permanent;

	/**
	 * Drapeau indiquant si des modifications ont été effectuées.
	 */
	private boolean modified = false;
	
	/**
	 * Le type de l'opération permanente.
	 */
	private String type = null;
	
	/**
	 * Le nom de l'opération permanente.
	 */
	private String nom = null;
	
	/**
	 * Le compte débité par l'opération permanente.
	 */
	private Compte debit = null;
	
	/**
	 * Le compte crédité par l'opération permanente.
	 */
	private Compte credit = null;
	
	/**
	 * Le libellé de l'opération permanente.
	 */
	private String libelle = null;
	
	/**
	 * Le nom du tiers de l'opération permanente.
	 */
	private String tiers = null;
	
	/**
	 * Drapeau indiquant si l'opération permanente doit être pointée
	 * automatiquement.
	 */
	private boolean pointer = false;
	
	/**
	 * Jours de l'opération permanente.
	 */
	private Map<Month,Integer> jours = null;
	
	/**
	 * Montants de l'opération permanente (pour les opérations à montant fixe).
	 */
	private Map<Month,BigDecimal> montants = null;
	
	/**
	 * L'opération permanente dont dépend celle-ci (pour les opérations
	 * dépendantes).
	 */
	private Permanent dependance = null;
	
	/**
	 * Taux de l'opération permanente (pour les opérations dépendantes).
	 */
	private BigDecimal taux;
	
	/**
	 * Construit un contrôleur contenant les données actuelles du
	 * <code>Permanent</code> spécifié.
	 * 
	 * @param permanent	Le <code>Permanent</code> à utiliser. Utiliser
	 * 					<code>null</code> pour la définition d'un nouveau
	 * 					<code>Permanent</code.
	 */
	public PermanentController(Permanent permanent) {
		this.permanent = permanent;
		if (permanent == null) {
			type = SetupPermanent.FIXE;				// Type FIXE par défaut
			taux = new BigDecimal("2");				// Taux par défaut
		} else {
			nom = permanent.nom;
			debit = permanent.debit;
			credit = permanent.credit;
			libelle	= permanent.libelle;
			tiers = permanent.tiers;
			pointer = permanent.pointer;
			jours = permanent.jours;

			// Déterminer le type et appliquer les propriétés particulières
			if (permanent instanceof PermanentFixe) {
				type = SetupPermanent.FIXE;
				montants = ((PermanentFixe) permanent).montants;

			} else if (permanent instanceof PermanentProport) {
				type = SetupPermanent.PROPORTIONNEL;
				dependance = ((PermanentProport) permanent).dependance;
				taux = ((PermanentProport) permanent).taux;
				
			} else if (permanent instanceof PermanentSoldeur) {				
				type = SetupPermanent.SOLDER;
			}
		}
	}
	
	/**
	 * Renvoie l'opération permanente contrôlée par cet objet.
	 * 
	 * @return	L'opération permanente contrôlée par cet objet, ou
	 * 			<code>null</code> si l'objet sert à créer une nouvelle opération
	 * 			permanente.
	 */
	public Permanent getPermanent() {
		return permanent;
	}
	
	/**
	 * Renvoie le type de l'opération permanente.
	 * 
	 * @return	Une des chaînes {@link SetupPermanent#FIXE},
	 * 			{@link SetupPermanent#PROPORTIONNEL} ou
	 * 			{@link SetupPermanent#SOLDER}.
	 */
	public String getType() {
		return type;
	}
	
	/**
	 * Renvoie le nom de l'opération permamente.
	 * 
	 * @return	Le nom de l'opération permanente.
	 */
	public String getNom() {
		return nom;
	}
	
	/**
	 * Renvoie le compte débité par l'opération permanente.
	 * 
	 * @return	Le compte débité par l'opération permanente.
	 */
	public Compte getDebit() {
		return debit;
	}
	
	/**
	 * Renvoie le compte crédité par l'opération permanente.
	 * 
	 * @return	Le compte crédité par l'opération permanente.
	 */
	public Compte getCredit() {
		return credit;
	}
	
	/**
	 * Renvoie le libellé de l'opération permanente.
	 * 
	 * @return	Le libellé de l'opération permanente.
	 */
	public String getLibelle() {
		return libelle;
	}
	
	/**
	 * Renvoie le nom du tiers de l'opération permanente.
	 * 
	 * @return	Le nom du tiers de l'opération permanente.
	 */
	public String getTiers() {
		return tiers;
	}
	
	/**
	 * Indique si l'opération permanente doit être pointée automatiquement.
	 * 
	 * @return	<code>true</code> si l'opération permanente doit être pointée
	 * 			automatiquement.
	 */
	public boolean getPointer() {
		return pointer;
	}
	
	/**
	 * Renvoie les jours de l'opération permanente.
	 * 
	 * @return	Les jours de l'opération permanente.
	 */
	public Map<Month,Integer> getJours() {
		return jours;
	}
	
	/**
	 * Renvoie les montants de l'opération permanente.
	 * 
	 * @return	Les montants de l'opération permanente.
	 */
	public Map<Month,BigDecimal> getMontants() {
		return montants;
	}
	
	/**
	 * Renvoie l'opération permanente dont dépend celle-ci.
	 * 
	 * @return	L'opération permanente dont dépend celle-ci, ou
	 * 			<code>null</code>.
	 */
	public Permanent getDependance() {
		return dependance;
	}
	
	/**
	 * Renvoie le taux de l'opération permanente.
	 * 
	 * @return	Le taux de l'opération permanente, en %.
	 */
	public BigDecimal getTaux() {
		return taux;
	}
	
	/**
	 * Modifie le type de l'opération permanente.
	 * 
	 * @param type	Une des chaînes {@link SetupPermanent#FIXE},
	 * 				{@link SetupPermanent#PROPORTIONNEL} ou
	 * 				{@link SetupPermanent#SOLDER}.
	 */
	public void setType(String type) {
		this.type = type;
		modified = true;
	}
	
	/**
	 * Modifie le nom de l'opération permanente.
	 * 
	 * @param nom	Le nouveau nom de l'opération permanente.
	 */
	public void setNom(String nom) {
		this.nom = nom;
		modified = true;
	}
	
	/**
	 * Modifie le compte débité par l'opération permanente.
	 * 
	 * @param debit	Le nouveau compte débité par l'opération permanente.
	 */
	public void setDebit(Compte debit) {
		this.debit = debit;
		modified = true;
	}
	
	/**
	 * Modifie le compte crédité par l'opération permanente.
	 * 
	 * @param credit	Le nouveau compte crédité par l'opération permanente.
	 */
	public void setCredit(Compte credit) {
		this.credit = credit;
		modified = true;
	}
	
	/**
	 * Modifie le libellé de l'opération permanente.
	 * 
	 * @param libelle	Le nouveau libellé de l'opération permanente.
	 */
	public void setLibelle(String libelle) {
		this.libelle = libelle;
		modified = true;
	}
	
	/**
	 * Modifie le nom du tiers de l'opération permanente.
	 * 
	 * @param tiers	Le nom du nouveau tiers de l'opération permanente.
	 */
	public void setTiers(String tiers) {
		this.tiers = tiers;
		modified = true;
	}
	
	/**
	 * Modifie le pointage automatique de l'opération permanente.
	 * 
	 * @param pointer	<code>true</code> pour que l'opération permanent soit
	 * 					pointée automatiquement.
	 */
	public void setPointer(boolean pointer) {
		this.pointer = pointer;
		modified = true;
	}
	
	/**
	 * Modifie les jours de l'opération permanente.
	 * 
	 * @param jours	Les nouveaux jours de l'opération permanente.
	 */
	public void setJours(Map<Month,Integer> jours) {
		this.jours = jours;
		modified = true;
	}
	
	/**
	 * Modifie les montants de l'opération permanente.
	 * 
	 * @param montants	Les nouveaux montants de l'opération permanente.
	 */
	public void setMontants(Map<Month,BigDecimal> montants) {
		this.montants = montants;
		modified = true;
	}
	
	/**
	 * Modifie l'opération permanente dont dépend celle-ci.
	 * 
	 * @param dependance	La nouvelle opération permanente dont doit dépendre
	 * 						celle-ci.
	 */
	public void setDependance(Permanent dependance) {
		this.dependance = dependance;
		modified = true;
	}
	
	/**
	 * Modifie le taux de l'opération permanente.
	 * 
	 * @param taux	Le nouveau taux de l'opération permanente.
	 */
	public void setTaux(BigDecimal taux) {
		this.taux = taux;
		modified = true;
	}
	
	/**
	 * Indique si les données ont été modifiées.
	 * 
	 * @return	code>true</code> si les données ont été modifiées.
	 */
	public boolean isModified() {
		return modified;
	}
	
	/**
	 * Applique les modifications au Permanent et envoie la nouvelle version au
	 * DAO.<br>
	 * Le contrat est que cette instance ne doit plus être utilisée après
	 * l'appel à cette méthode.
	 * 
	 * @return	Le nouveau <code>Permanent</code> enregistré dans le DAO. S'il
	 * 			n'y a pas de modifications, c'est la même instance
	 * 			qu'auparavant.
	 * 
	 * @throws IOException
	 */
	public Permanent applyChanges() throws IOException {
		if (!modified)									// Si rien n'a changé
			return permanent;							// Ne rien faire

		// Instancier un nouveau Permanent en remplacement de l'actuel
		Permanent newPermanent = null;
		Integer id = (permanent == null) ? null : permanent.id;
		if (SetupPermanent.FIXE.equals(type)) {
			newPermanent = new PermanentFixe(
					id, nom, debit, credit, libelle, tiers, pointer, jours,
					montants);
		} else if (SetupPermanent.PROPORTIONNEL.equals(type)) {
			newPermanent = new PermanentProport(
					id, nom, debit, credit, libelle, tiers, pointer, jours,
					dependance, taux);
		} else if (SetupPermanent.SOLDER.equals(type)) {
			newPermanent = new PermanentSoldeur(
					id, nom, debit, credit, libelle, tiers, pointer, jours);
		}

		// Enregistrer dans le DAO
		PermanentDAO dao = DAOFactory.getFactory().getPermanentDAO();
		if (permanent == null) {						// Pas d'ancien objet
			return dao.add(newPermanent);
		} else {										// Objet existant
			dao.update(newPermanent);
			return dao.get(id);
		}
	}
	
	/**
	 * Supprime le <code>Permanent</code>. 
	 * 
	 * @throws IOException
	 */
	public void deletePermanent() throws IOException {
		DAOFactory.getFactory().getPermanentDAO().remove(permanent.id);
	}
	
	/**
	 * Applique la relation d'ordre des Permanents au contrôleurs de
	 * <code>Permanent</code>s. Si on contrôleur est vide ("Nouveau..."), il
	 * passe avant.
	 */
	@Override
	public int compareTo(PermanentController controller) {
		Permanent permanent2 = controller.getPermanent();
		if (permanent == null) {
			if (permanent2 == null) {
				return 0;							// Deux null: égalité !
			} else {
				return -1;							// Le null en premier
			}
		} else if (permanent2 != null) {
			return 1;								// Le non-null en deuxième
		} else {
			return permanent.compareTo(permanent2);
		}
	}
	
	/**
	 * @return	le nom du <code>Permanent</code> vers lequel pointe l'objet.
	 */
	@Override
	public String toString() {
		return (permanent == null) ? "Nouveau..." : permanent.toString();
	}
}