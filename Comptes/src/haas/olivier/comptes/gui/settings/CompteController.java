package haas.olivier.comptes.gui.settings;

import java.awt.Color;
import java.io.IOException;
import java.util.Date;
import haas.olivier.comptes.Compte;
import haas.olivier.comptes.TypeCompte;
import haas.olivier.comptes.dao.DAOFactory;

/**
 * Un contrôleur de compte.<br>
 * Il permet de pré-visualiser les changements à apporter à un compte.
 */
class CompteController implements Comparable<CompteController> {
	
	/**
	 * Le compte contrôlé.
	 */
	private Compte compte;
	
	/**
	 * Marqueur de modifications.
	 */
	private boolean modified = false;
	
	/**
	 * Le nom du compte.
	 */
	private String nom		= null;
	
	/**
	 * La couleur du compte.
	 */
	private Color color		= null;
	
	/**
	 * Le type du compte.
	 */
	private TypeCompte type	= TypeCompte.COMPTE_COURANT;
	
	/**
	 * Le numéro du compte bancaire.
	 */
	private long numero		= 0L;
	
	/**
	 * La date d'ouverture du compte.
	 */
	private Date ouverture	= null;
	
	/**
	 * La date de clôture du compte.
	 */
	private Date cloture	= null;
	
	/**
	 * Construit un contrôleur contenant les données actuelles du compte
	 * spécifié.
	 * 
	 * @param compte	Le compte à éditer. Utiliser <code>null</code> pour un
	 * 					nouveau compte.
	 */
	public CompteController(Compte compte) {
		this.compte = compte;
		refresh();
	}
	
	/**
	 * Rafraîchit les données à partir du compte actuel.
	 */
	private void refresh() {
		if (compte != null) {
			nom = compte.getNom();
			color = compte.getColor();
			type = compte.getType();
			ouverture = compte.getOuverture();
			cloture = compte.getCloture();
			if (type.isBancaire()) {
				numero = compte.getNumero();
			}
		}
	}
	
	/**
	 * Renvoie le compte.
	 */
	public Compte getCompte() {
		return compte;
	}
	
	public String getNom() {
		return nom;
	}
	
	public Color getColor() {
		return color;
	}
	
	public TypeCompte getType() {
		return type;
	}
	
	public long getNumero() {
		return numero;
	}
	
	public Date getOuverture() {
		return ouverture;
	}
	
	public Date getCloture() {
		return cloture;
	}
	
	public void setNom(String nom) {
		this.nom = nom;
		modified = true;
	}
	
	public void setColor(Color color) {
		this.color = color;
		modified = true;
	}
	
	public void setType(TypeCompte type) {
		this.type = type;
		modified = true;
	}
	
	/**
	 * Modifie le numéro.
	 * 
	 * @param numeroText	Le numéro, au format texte.
	 */
	public void setNumero(String numeroText) {
		this.numero = Long.parseLong(numeroText);
		modified = true;
	}
	
	public void setOuverture(Date ouverture) {
		this.ouverture = ouverture;
		modified = true;
	}
	
	public void setCloture(Date cloture) {
		this.cloture = cloture;
		modified = true;
	}
	
	/**
	 * Indique si les données ont été modifiées par rapport à l'état d'origine
	 * du compte.
	 */
	public boolean isModified() {
		return modified;
	}
	
	/**
	 * Applique les changements au compte et envoie la nouvelle instance au DAO.
	 * 
	 * @return	Le compte. S'il était <code>null</code>, il s'agit d'une
	 * 			nouvelle instance ; sinon, c'est le même objet que
	 * 			{@ŀink #getCompte()}.
	 */
	public Compte applyChanges() {
		if (!modified)								// Pas de modifications
			return compte;							// Ne rien faire
		
		// Instancier un nouveau Compte si besoin
		if (compte == null)
			compte = DAOFactory.getFactory().getCompteDAO().createAndAdd(type);
		
		// Ajuster les nouvelles propriétés
		compte.setColor(color);
		compte.setOuverture(ouverture);
		compte.setCloture(cloture);
		
		// Réinitialiser le marqueur de modifications
		modified = false;
		
		// Rafraîchir les données
		refresh();
		
		return compte;
	}
	
	/**
	 * Supprime le compte.
	 * 
	 * @throws IOException
	 */
	public void deleteCompte() throws IOException {
		DAOFactory.getFactory().getCompteDAO().remove(compte);
	}

	/**
	 * Applique la relation d'ordre des comptes aux contrôleurs de comptes.<br>
	 * Si un contrôleur et vide ("Nouveau..."), il passe avant.
	 */
	@Override
	public int compareTo(CompteController controller) {
		Compte compte2 = controller.getCompte();
		if (compte == null) {
			return (compte2 == null) ? 0 : -1;
		} else if (compte2 == null) {
			return 1;
		} else {
			return compte.compareTo(compte2);
		}
	}
	
	@Override
	public boolean equals(Object o) {
		return (o instanceof CompteController)
				&& (compareTo((CompteController) o) == 0);
	}
	
	@Override
	public int hashCode() {
		return (compte == null) ? 0 : compte.hashCode();
	}
	
	/**
	 * Renvoie le nom du compte vers lequel pointe l'objet.
	 */
	@Override
	public String toString() {
		return (compte == null) ? "Nouveau..." : compte.toString();
	}
}