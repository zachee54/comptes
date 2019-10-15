/*
 * Copyright (c) 2019 DGFiP - Tous droits réservés
 * 
 */
package haas.olivier.comptes.dao.hibernate;

import java.io.Closeable;
import java.io.IOException;
import java.util.Collection;

import javax.persistence.EntityManager;

import haas.olivier.comptes.Compte;
import haas.olivier.comptes.TypeCompte;
import haas.olivier.comptes.dao.CompteDAO;

/**
 * HibernateCompteDAO.java
 * DOCUMENTEZ_MOI
 * @author Olivier HAAS
 * Date: 15 oct. 2019
 */
class HibernateCompteDAO implements CompteDAO, Closeable {

	/**
	 * Gestionnaire d'entités JPA.
	 */
	private final EntityManager entityManager;
	
	HibernateCompteDAO(EntityManager entityManager) {
		this.entityManager = entityManager;
	}
	
	/** 
	 * (methode de remplacement)
	 * {@inheritDoc}
	 * @see haas.olivier.comptes.dao.CompteDAO#getAll()
	 */
	@Override
	public Collection<Compte> getAll() throws IOException {
		return null; // DOCUMENTEZ_MOI Raccord de méthode auto-généré
	}

	@Override
	public void add(Compte compte) {
		entityManager.persist(compte);
	}

	/** 
	 * (methode de remplacement)
	 * {@inheritDoc}
	 * @see haas.olivier.comptes.dao.CompteDAO#createAndAdd(haas.olivier.comptes.TypeCompte)
	 */
	@Override
	public Compte createAndAdd(TypeCompte type) {
		return null; // DOCUMENTEZ_MOI Raccord de méthode auto-généré
	}

	/** 
	 * (methode de remplacement)
	 * {@inheritDoc}
	 * @see haas.olivier.comptes.dao.CompteDAO#remove(haas.olivier.comptes.Compte)
	 */
	@Override
	public void remove(Compte compte) throws IOException {
		// DOCUMENTEZ_MOI Raccord de méthode auto-généré
	}

	/** 
	 * (methode de remplacement)
	 * {@inheritDoc}
	 * @see haas.olivier.comptes.dao.CompteDAO#erase()
	 */
	@Override
	public void erase() {
		// DOCUMENTEZ_MOI Raccord de méthode auto-généré
	}

	@Override
	public void close() {
		entityManager.close();
	}
}
