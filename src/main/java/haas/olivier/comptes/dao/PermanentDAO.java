/*
 * Copyright 2013-2018 Olivier HAAS. All rights reserved.
 */
package haas.olivier.comptes.dao;

import java.io.IOException;
import java.util.Collection;

import haas.olivier.comptes.Permanent;

/**
 * L'interface d'accès aux données des opérations permanentes.
 * 
 * @author Olivier HAAS
 */
public interface PermanentDAO {

	/**
	 * Renvoie toutes les écritures permanentes.
	 */
	public Collection<Permanent> getAll() throws IOException;
	
	/**
	 * Renvoie une opération permanente.
	 * 
	 * @param id	L'identifiant de l'opération permanente à renvoyer.
	 * 
	 * @return		L'opération ayant l'identifiant <code>id</code>, ou
	 * 				<code>null</code> s'il n'y a pas de telle opération.
	 */
	public Permanent get(int id);
	
	/**
	 * Ajoute une nouvelle opération permanente.
	 * <p>
	 * Un nouvel identifiant est attribué à l'objet ajouté.
	 * 
	 * @param p	L'opération permanente à ajouter. Elle ne doit pas avoir encore
	 * 			d'identifiant.
	 */
	public void add(Permanent p);
	
	/**
	 * Modifie dans le modèle une opération permanente existante.
	 */
	public void update(Permanent p);
	
	/**
	 * Supprime une opération permanente.
	 * 
	 * @param id	L'identifiant de l'opération permanente à supprimer.
	 */
	public void remove(int id);
}
