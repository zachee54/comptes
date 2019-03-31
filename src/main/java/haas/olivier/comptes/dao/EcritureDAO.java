/*
 * Copyright 2013-2018 Olivier HAAS. All rights reserved.
 */
package haas.olivier.comptes.dao;

import haas.olivier.comptes.Ecriture;
import haas.olivier.util.Month;

import java.io.IOException;
import java.util.Map;

/** L'interface d'accès aux données des écritures.
 * 
 * @author Olivier HAAS
 */
public interface EcritureDAO {

	/** Renvoie l'écriture portant l'identifiant spécifié, ou <code>null</code>
	 * si aucune écriture ne porte cet identifiant.
	 * 
	 * @param id	L'identifiant de l'écriture souhaitée.
	 */
	Ecriture get(Integer id) throws IOException;
	
	/** Renvoie toutes les écritures triées dans l'ordre inverse de l'ordre
	 * chronologique.
	 */
	Iterable<Ecriture> getAll() throws IOException;
	
	/** Renvoie toutes les écritures entre les deux mois spécifiés, dans l'ordre
	 * inverse de l'ordre chronologique.
	 * 
	 * @param from	Le mois de départ (inclus).
	 * @param to	Le mois d'arrivée (inclus).
	 */
	Iterable<Ecriture> getAllBetween(Month from, Month to) throws IOException;
	
	/** Renvoie toutes les écritures depuis le mois spécifié, dans l'ordre
	 * chronologique.
	 * 
	 * @param month	Le mois souhaité.
	 */
	Iterable<Ecriture> getAllSince(Month month) throws IOException;
	
	/** Renvoie toutes les écritures pointées depuis le mois spécifié, dans
	 * l'ordre chronologique des pointages, ainsi que les écritures non
	 * pointées.<br>
	 * Les écritures non pointées sont présentées en dernier.
	 * 
	 * @param month	Le mois souhaité.
	 */
	Iterable<Ecriture> getPointagesSince(Month month) throws IOException;
	
	/** Renvoie toutes les écritures du mois spécifié et des mois précédents,
	 * dans l'ordre inverse de l'ordre chronologique.
	 * <p>
	 * Ces écritures ne sont pas nécessairement chargées en mémoire, et leur
	 * itération n'est pas censée être parcourue jusqu'au bout. C'est seulement
	 * pour avoir les écritures du mois en question ou, si elles sont vraiment
	 * peu nombreuses, les écritures qui ont précédé.
	 * 
	 * @param month	Le mois à partir duquel renvoyer les écritures.
	 */
	Iterable<Ecriture> getAllTo(Month month) throws IOException;
	
	/** Renvoie toutes les écritures pointées au cours du mois spécifié ou des
	 * mois précédents, dans l'ordre inverse de l'ordre chronologique des
	 * pointages.
	 * <p>
	 * Ces écritures ne sont pas nécessairement chargées en mémoire, et leur
	 * itération n'est pas censée être parcourue jusqu'au bout. C'est seulement
	 * pour avoir les écritures du mois en question ou, si elles sont vraiment
	 * peu nombreuses, les écritures qui ont précédé.
	 * 
	 * @param month	Le mois à partir duquel renvoyer les écritures.
	 */
	Iterable<Ecriture> getPointagesTo(Month month) throws IOException;
	
	/** Ajoute une écriture.
	 * <p>
	 * Si <code>e</code> a un identifiant <code>null</code>, alors elle est
	 * réinstanciée avec un identifiant immédiatement supérieur au plus grand
	 * identifiant actuel.
	 * 
	 * @param e	L'écriture à ajouter.
	 * 
	 * @throws IOException 
	 */
	void add(Ecriture e) throws IOException;
	
	/** Modifie une écriture.
	 * 
	 * @param e	La nouvelle version de l'écriture à modifier.
	 */
	void update(Ecriture e);
	
	/** Supprime une écriture.
	 * 
	 * @param id	L'identifiant de l'écriture à supprimer.
	 */
	void remove (int id) throws IOException;
	
	/** Renvoie un index des libellés et tiers utilisés dans les écritures.
	 * 
	 * @return	Une collection des valeurs existantes associées à leurs
	 * 			nombres d'occurrences dans le modèle.
	 */
	Map<String, Integer> constructCommentIndex() throws IOException;
}
