package haas.olivier.comptes.gui.settings;

import java.awt.Color;
import java.io.IOException;
import java.text.ParseException;
import java.util.Date;
import java.util.Objects;

import haas.olivier.comptes.Compte;
import haas.olivier.comptes.TypeCompte;
import haas.olivier.comptes.dao.DAOFactory;

/**
 * Un contrôleur d'éditeur de compte.
 * <p>
 * Il supervise l'affichage, puis la validation des propriétés d'un compte dans
 * un éditeur graphique.
 *
 * @author Olivier Haas
 */
class CompteEditorController {

	/**
	 * L'éditeur graphique.
	 */
	private final CompteEditor editor;
	
	/**
	 * Le compte actuellement édité, ou <code>null</code> si on propose la
	 * saisie d'un nouveau compte.
	 */
	private Compte compte;
	
	/**
	 * Construit un contrôleur d'éditeur de compte.
	 * 
	 * @param editor	L'éditeur de compte associé.
	 */
	CompteEditorController(CompteEditor editor) {
		this.editor = editor;
	}
	
	/**
	 * Définit un nouveau compte à éditer.
	 * 
	 * @param compte	Le nouveau compte à éditer, ou <code>null</code> si on
	 * 					propose la saisie d'un nouveau compte.
	 */
	void setCompte(Compte compte) {
		this.compte = compte;
		if (compte == null) {
			editor.setNom(null);
			editor.setNumero(null);
			editor.setColor(null);
			editor.setOuverture(null);
			editor.setCloture(null);
			editor.setTypeCompte(TypeCompte.COMPTE_COURANT);
		} else {
			editor.setNom(compte.getNom());
			editor.setNumero(compte.getNumero());
			editor.setColor(compte.getColor());
			editor.setOuverture(compte.getOuverture());
			editor.setCloture(compte.getCloture());
			editor.setTypeCompte(compte.getType());
		}
	}
	
	/**
	 * Indique si les propriétés du compte ont été modifiées dans l'éditeur.
	 * 
	 * @return	<code>true</code> si les propriétés du compte ont été modifiées
	 * 			dans l'éditeur.
	 */
	boolean isModified() {
		return (compte == null)
				? isNewCompteSaisi() : isExistingCompteModified();
	}

	/**
	 * Indique si une des données essentielles ont été saisies, ce qui indique
	 * en principe que l'utilisateur a voulu saisir un compte dans l'éditeur.
	 * <p>
	 * Les données essentielles sont le nom, le numéro et la date d'ouverture.
	 * 
	 * @return	<code>true</code> si le nom, le numéro ou une date d'ouverture
	 * 			valide ont été saisis.
	 */
	private boolean isNewCompteSaisi() {
		
		// Si un nom ou un numéro a été saisi, c'est une modification
		if (!editor.getNom().isEmpty() || (editor.getNumero() != null))
			return true;
		
		// Si la date d'ouverture est saisie ET valide, c'est une modif
		try {
			return editor.getOuverture() == null;
		} catch (ParseException e) {
			return false;			// Date d'ouverture seule et invalide
		}
	}
	
	/**
	 * Indique si les données de l'éditeur sont différentes des propriétés du
	 * compte.
	 * 
	 * @return	<code>false</code> si les données de l'éditeur correspondent aux
	 * 			propriétés du compte. Pour un compte budgétaire, le numéro est
	 * 			ignoré.
	 */
	private boolean isExistingCompteModified() {
		try {
			if (compte.getType() != editor.getTypeCompte() 
					|| !compte.getNom().equals(editor.getNom())
					|| !Objects.equals(compte.getColor(), editor.getColor())
					|| !Objects.equals(
							compte.getOuverture(), editor.getOuverture())
					|| !Objects.equals(
							compte.getCloture(), editor.getCloture()) ) {
				return true;
			}
			
		} catch (ParseException e) {
			return false;	// Date invalide, donc modifiée
		}
		
		// Le numéro n'a pas d'importance que si c'est un compte bancaire
		return compte.getType().isBancaire()
				&& !Objects.equals(compte.getNumero(), editor.getNumero());
	}
	
	/**
	 * Applique au compte les modifications saisies.
	 * 
	 * @throws IOException
	 * 				Si le nom, la couleur, la date d'ouverture ne sont pas
	 * 				saisis, ou si la date de d'ouverture ou de clôture est
	 * 				invalide, ou si le type correspond à un compte bancaire et
	 * 				que le numéro est vide ou invalide.
	 */
	void applyChanges() throws IOException {
		
		// Récupérer les propriétés saisies
		TypeCompte type = editor.getTypeCompte();
		String nom = editor.getNom();
		Color color = editor.getColor();
		Long numero = editor.getNumero();
		Date ouverture;
		Date cloture;
		try {
			ouverture = editor.getOuverture();
			cloture = editor.getCloture();
		} catch (ParseException e) {
			throw new IOException(
					"Date invalide : " + e.getLocalizedMessage(), e);
		}
		
		if (ouverture == null) {
			throw new IOException("Saisissez une date d'ouverture");
		}
		if (nom.isEmpty()) {
			throw new IOException("Saisissez un nom pour ce compte");
		}
		if (color == null) {
			throw new IOException("Choisissez une couleur pour ce compte");
		}
		if (type.isBancaire() && numero == null) {
			throw new IOException("Saisissez un numéro de compte valide");
		}
		
		createOrSetType(type);
		
		// Modifier le compte
		compte.setCloture(cloture);
		compte.setColor(color);
		compte.setNom(nom);
		compte.setOuverture(ouverture);
		if (type.isBancaire())
			compte.setNumero(numero);
	}
	
	/**
	 * Assure l'existence et le type du compte.
	 * <p>
	 * Si le compte n'existe pas, il est créé dans le modèle avec le type
	 * spécifié.<br>
	 * Si le compte existe, son type est modifié pour correspondre au type
	 * spécifié.
	 * 
	 * @param type	Le type de compte.
	 */
	private void createOrSetType(TypeCompte type) {
		if (compte == null) {
			compte = DAOFactory.getFactory().getCompteDAO().createAndAdd(type);
		} else {
			compte.setType(editor.getTypeCompte());
		}
	}
}
