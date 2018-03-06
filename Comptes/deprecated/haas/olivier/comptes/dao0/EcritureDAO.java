package haas.olivier.comptes.dao0;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.NavigableSet;
import haas.olivier.comptes.Ecriture;
import haas.olivier.util.Month;

/**
 * Classe d'accès aux données pour les écritures.
 * Il s'agit de toutes les méthodes utilisées depuis l'extérieur du modèle.
 * 
 * @author Olivier HAAS
 */
public abstract class EcritureDAO implements AbstractEcritureDAO {

	/** Un index des commentaires (libellés, noms de tiers, etc) utilisés dans
	 * les écritures.
	 * Pour chaque valeur, la carte associe le nombre d'occurrences présentes
	 * dans le modèle.
	 */
	private Map<String,Integer> commentIndex = new HashMap<String,Integer>();
	
	/**
	 * Ajoute une nouvelle écriture et en renvoie une nouvelle avec le numéro
	 * d'index. Si l'écriture a déjà un index, elle est enregistrée avec le même
	 * index.
	 * 
	 * @return La nouvelle écriture enregistrée, avec son index.
	 * @throws IOException
	 */
	public abstract Ecriture add(Ecriture e) throws IOException;

	/**
	 * Met à jour une écriture. L'écriture à mettre à jour est déterminée par
	 * son index.
	 * 
	 * @throws IOException
	 */
	public abstract void update(Ecriture e) throws IOException;

	/**
	 * Supprime l'écriture correspondant à l'index spécifié.
	 * 
	 * @throws IOException
	 */
	public abstract void remove(int id) throws IOException;

	/**
	 * Charge une écriture
	 * 
	 * @param id
	 *            L'identifiant de l'écriture voulue
	 * @return L'écriture voulue
	 * @throws IOException
	 *             Si la ressource est indisponible ou si l'écriture n'existe
	 *             pas
	 */
	public abstract Ecriture get(int id) throws IOException;

	/**
	 * Charge toutes les écritures depuis un mois donné, de la plus récente à la
	 * plus ancienne.
	 * 
	 * @throws IOException
	 */
	public abstract NavigableSet<Ecriture> getAllSince(Month mois) throws IOException;

	/**
	 * Charge toutes les écritures pointées après un mois donné, du pointage le
	 * plus récent au pointage le plus ancien.
	 * 
	 * @throws IOException
	 */
	public abstract NavigableSet<Ecriture> getPointagesSince(Month mois)
			throws IOException;
	
	/** Construit l'index des commentaires. 
	 * 
	 * @return	L'index construit. 
	 */
	public Map<String, Integer> constructCommentIndex() {
		commentIndex.clear();						// Vider l'index
		
		try {

			for (Ecriture e : getAll()) {			// Pour chaque écriture
				indexString(e.libelle);				// Indexer le libellé
				indexString(e.tiers);				// Indexer le nom du tiers
			}// for écriture
			
		} catch (IOException e) {
			e.printStackTrace();					// Erreur de chargement
		}// try
		
		return commentIndex;						// Retourner l'index
	}// constructIndex
	
	/** Indexe un commentaire unique. */
	private void indexString(String s) {
		if (s == null || "".equals(s))				// S'il n'y a pas de texte
			return;									// Arrêter là
		
		if (commentIndex.containsKey(s)) {			// Déjà indexée ?
			commentIndex.put(s,
					commentIndex.get(s)+1);			// Ajouter 1 au compteur
		} else {
			commentIndex.put(s, 1);					// Sinon, commencer à 1
		}// if containsKey
	}// indexString
}
