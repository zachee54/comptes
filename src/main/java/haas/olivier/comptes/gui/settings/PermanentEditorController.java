/*
 * Copyright 2019 Olivier HAAS. All rights reserved.
 */
package haas.olivier.comptes.gui.settings;

import java.io.IOException;
import java.util.Map;
import haas.olivier.comptes.Compte;
import haas.olivier.comptes.Permanent;
import haas.olivier.comptes.PermanentFixe;
import haas.olivier.comptes.PermanentProport;
import haas.olivier.comptes.PermanentSoldeur;
import haas.olivier.comptes.PermanentState;
import haas.olivier.comptes.dao.DAOFactory;
import haas.olivier.util.Month;

/**
 * Un contrôleur d'éditeur d'opérations permanentes.
 * <p>
 * Il supervise l'affichage, puis la validation des propriétés d'une opération
 * permanente dans un éditeur graphique.
 *
 * @author Olivier Haas
 */
class PermanentEditorController {

	/**
	 * L'éditeur graphique.
	 */
	private final PermanentEditor editor;
	
	/**
	 * L'opération permanente actuellement éditée, ou <code>null</code> si on
	 * propose la saisie d'une nouvelle opération permanente.
	 */
	private Permanent permanent;
	
	/**
	 * Construit un contrôleur d'éditeur d'opération permanente.
	 * 
	 * @param editor	L'éditeur d'opérations permanentes associé.
	 */
	PermanentEditorController(PermanentEditor editor) {
		this.editor = editor;
	}
	
	/**
	 * Renvoie l'opération permanente actuellement affichée dans l'éditeur.
	 * 
	 * @return	L'opération permanente actuellement affichée dans l'éditeur, ou
	 * 			<code>null</code> si aucune opération permanente n'a été
	 * 			définie.
	 */
	Permanent getPermanent() {
		return permanent;
	}
	
	/**
	 * Définit une nouvelle opération permanente à éditer.
	 * 
	 * @param permanent	La nouvelle opération permanente à éditer, ou
	 * 					<code>null</code> si on propose la saisie d'une nouvelle
	 * 					opération permanente.
	 */
	void setPermanent(Permanent permanent) {
		this.permanent = permanent;
		if (permanent == null) {
			editor.setNom(null);
			editor.setDebit(null);
			editor.setCredit(null);
			editor.setLibelle(null);
			editor.setTiers(null);
			editor.setAutoPointee(false);
			editor.setJours(null);
			editor.setType(PermanentEditor.FIXE);
			editor.setMontants(null);
			editor.setDependance(null);
			editor.setTaux(null);
			
		} else {
			editor.setNom(permanent.getNom());
			editor.setDebit(permanent.getDebit());
			editor.setCredit(permanent.getCredit());
			editor.setLibelle(permanent.getLibelle());
			editor.setTiers(permanent.getTiers());
			editor.setAutoPointee(permanent.isAutoPointee());
			editor.setJours(permanent.getJours());
			
			PermanentState state = permanent.getState();
			if (state instanceof PermanentFixe) {
				editor.setType(PermanentEditor.FIXE);
				editor.setMontants(((PermanentFixe) state).montants);
				
			} else if (state instanceof PermanentProport) {
				editor.setType(PermanentEditor.PROPORTIONNEL);
				PermanentProport proport = (PermanentProport) state;
				editor.setDependance(proport.dependance);
				editor.setTaux(proport.taux);
			
			} else {
				editor.setType(PermanentEditor.SOLDER);
			}
		}
	}
	
	/**
	 * Indique si les propriétés du compte ont été modifiées dans l'éditeur.
	 * 
	 * @return	<code>true</code> si les propriétés de l'opération permanente
	 * 			ont été modifiées dans l'éditeur.
	 */
	boolean isModified() {
		return (permanent == null)
				? isNewPermanentSaisi() : isExistingPermanentModified();
	}

	/**
	 * Indique si une des données essentielles ont été saisies, ce qui indique
	 * en principe que l'utilisateur a voulu saisir une opération permanente
	 * dans l'éditeur.
	 * <p>
	 * Les données essentielles sont le nom, le compte débité et le compte
	 * crédité.
	 * 
	 * @return	<code>true</code> si le nom, ou le compte débité et le compte
	 * 			crédité ont été saisis.
	 */
	private boolean isNewPermanentSaisi() {
		return !editor.getNom().isEmpty()
				|| (editor.getCredit() != null && editor.getDebit() != null);
	}
	
	/**
	 * Indique si les données de l'éditeur sont différentes des propriétés du
	 * compte.
	 * 
	 * @return	<code>false</code> si les données de l'éditeur correspondent aux
	 * 			propriétés de l'opération permanente
	 */
	private boolean isExistingPermanentModified() {
		if (!permanent.getNom().equals(editor.getNom()))
			return true;
		if (permanent.getDebit() != editor.getDebit())
			return true;
		if (permanent.getCredit() != editor.getCredit())
			return true;
		if (!permanent.getLibelle().equals(editor.getLibelle()))
			return true;
		if (!permanent.getTiers().equals(editor.getTiers()))
			return true;
		if (permanent.isAutoPointee() != editor.isAutoPointee())
			return true;
		if (!permanent.getJours().equals(editor.getJours()))
			return true;
		
		PermanentState state = permanent.getState();
		if (state instanceof PermanentFixe) {
			if (editor.getType() != PermanentEditor.FIXE)
				return true;
			if (!((PermanentFixe) state).montants.equals(editor.getMontants()))
				return true;
		} else if (state instanceof PermanentProport) {
			if (editor.getType() != PermanentEditor.PROPORTIONNEL)
				return true;
			PermanentProport proport = (PermanentProport) state;
			if (proport.dependance != editor.getDependance())
				return true;
			if (proport.taux.compareTo(editor.getTaux()) != 0)
				return true;
		} else {
			if (editor.getType() != PermanentEditor.SOLDER) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Applique à l'opération permanente les modifications saisies.
	 * 
	 * @throws IOException
	 * 				Si le nom est vide, ou si le compte débité ou le compte
	 * 				crédité est <code>null</code>.
	 */
	void applyChanges() throws IOException {
		
		// Récupérer les propriétés saisies
		String nom = editor.getNom();
		Compte debit = editor.getDebit();
		Compte credit = editor.getCredit();
		String libelle = editor.getLibelle();
		String tiers = editor.getTiers();
		boolean pointer = editor.isAutoPointee();
		String type = editor.getType();
		Map<Month, Integer> jours = editor.getJours();
		
		if (nom.isEmpty()) {
			throw new IOException("Saisissez un nom pour ce compte");
		}
		if (debit == null || credit == null) {
			throw new IOException("Saisissez les comptes à débiter et à créditer");
		}
		
		if (permanent == null) {
			permanent = new Permanent(
					null, nom, debit, credit, libelle, tiers, pointer, jours);
			DAOFactory.getFactory().getPermanentDAO().add(permanent);
			
		} else {
			permanent.setNom(nom);
			permanent.setDebit(debit);
			permanent.setCredit(credit);
			permanent.setLibelle(libelle);
			permanent.setTiers(tiers);
			permanent.setPointee(pointer);
			
			Map<Month, Integer> permanentJours = permanent.getJours();
			permanentJours.clear();
			permanentJours.putAll(jours);
		}
		
		if (PermanentEditor.FIXE == type) {
			permanent.setState(new PermanentFixe(editor.getMontants()));
		} else if (PermanentEditor.PROPORTIONNEL == type) {
			permanent.setState(new PermanentProport(
					editor.getDependance(), editor.getTaux()));
		} else {
			permanent.setState(new PermanentSoldeur(permanent));
		}
	}
}
