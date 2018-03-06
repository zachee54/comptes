package haas.olivier.comptes.dao0.cache;

import haas.olivier.comptes.Compte;
import haas.olivier.comptes.Ecriture;
import haas.olivier.comptes.MessagesFactory;
import haas.olivier.util.Month;
import haas.olivier.comptes.dao.CompteDAO;
import haas.olivier.comptes.dao.DAOFactory;
import haas.olivier.comptes.dao.EcritureDAO;
import haas.olivier.comptes.dao.util.CacheEcriture;

import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.NavigableSet;
import java.util.TreeSet;

/** Classe d'accès aux données des écritures implémentant un cache.
 * Les données sont entièrement chargées en mémoire à partir d'une sous-couche
 * avant d'être exploitées. La sauvegarde écrit le contenu du cache en écrasant
 * toutes les anciennes données.
 * 
 * La classe utilise en interne deux caches triés différemment. Par principe,
 * les deux caches doivent pointer vers les mêmes instances Ecriture. Seul le
 * tri diffère.
 *
 * @author Olivier HAAS
 */
public class CacheEcritureDAO extends EcritureDAO {
	
	/** Sous-couche DAO. */
	private CacheableEcritureDAO dao;
	
	/** Les caches. */
	private CacheEcriture cache, cachePointage;
	
	/** Indicateur de modification des données. */
	boolean mustBeSaved = false;
	
	/** Construit une couche DAO avec cache utilisant les données du DAO
	 * spécifié.
	 * @param dao	Un CacheableEcritureDAO
	 * @throws IOException 
	 */
	public CacheEcritureDAO(CacheableEcritureDAO dao) {
		this.dao = dao;								// Mémoriser le DAO
		cache = new CacheEcriture(dao);				// Créer le cache normal
		cachePointage = new CacheEcriture(cache);	// Créer le cache pointages
	}// constructeur
	
	@Override
	public TreeSet<Ecriture> getAll() throws IOException {
		return cache.getCloneCache();
	}

	@Override
	public Ecriture add(Ecriture e) throws IOException {
		Ecriture nouvelle = e;
		
		// Si l'Ecriture n'a pas d'index: lui en attribuer un
		if (e.id == null) {
			
			// Créer une nouvelle Ecriture avec l'identifiant approprié
			nouvelle = new Ecriture(cache.getNextId(), e.date, e.pointage,
					e.debit, e.credit, e.montant, e.libelle, e.tiers, e.cheque);
		}
		
		// Insérer l'Ecriture
		cache.getCache().add(nouvelle);
		cachePointage.getCache().add(nouvelle);
		
		// Indiquer que les données ont été modifiées
		mustBeSaved = true;
		
		return nouvelle;
	}// add

	@Override
	public void update(Ecriture e) throws IOException {
		remove(e.id);						// Supprimer
		add(e);								// Ajouter
	}// update

	/** Supprime une Ecriture dans un cache. 
	 * @throws IOException */
	private void removeInCache(Integer id, CacheEcriture aCache)
			throws IOException {
		Iterator<Ecriture> it = aCache.getCache().iterator();
		while (it.hasNext()) {					// Parcourir la collection
			if (id.equals(it.next().id)) {		// Trouver le même identifiant
				it.remove();					// Supprimer
				break;							// Arrêter la boucle
			}// if même id
		}// while it
	}// removeInCache

	@Override
	public void remove(int id) throws IOException {
		removeInCache(id, cache);			// Supprimer dans le cache normal
		removeInCache(id, cachePointage);	// Supprimer dans le cache pointages
		
		// Indiquer que les données ont été modifiées
		mustBeSaved = true;
	}// remove

	@Override
	public Ecriture get(int id) throws IOException {
		for (Ecriture e : cache.getCache()) {	// Parcourir le cache normal
			if (e.id.intValue() == id) {		// Bon identifiant ?
				return e;						// Renvoyer cette Ecriture
			}// if id
		}// for cache
		MessagesFactory.getInstance().showErrorMessage(
				"Ecriture n°" + id + " non trouvée");
		throw new IOException();
	}// get

	@Override
	public NavigableSet<Ecriture> getAllSince(Month mois) throws IOException {
		TreeSet<Ecriture> cloneCache = cache.getCloneCache();
		for (Ecriture e : cloneCache) {		// Parcourir le cache cloné
			if (mois.after(e.date)) {		// Première Ecriture trop ancienne
				
				// Renvoyer le début du cache cloné
				return cloneCache.headSet(e, false);
			}// if id
		}// for cache
		
		// Pas de date antérieure: renvoyer tout le clone !
		return cloneCache;
	}// getAllSince

	@Override
	public NavigableSet<Ecriture> getPointagesSince(Month mois)
			throws IOException {
		TreeSet<Ecriture> cloneCache = cachePointage.getCloneCache();
		for (Ecriture e : cloneCache) {		// Parcourir le cache par pointages
			if (e.pointage != null					// Il y a un pointage ?
					&& mois.after(e.pointage)) {	// Le premier trop ancien ?
				
				// Retourné le début du cache cloné
				return cloneCache.headSet(e, false);
			}// if id
		}// for cache pointages
		
		// Pas de pointage antérieure: renvoyer tout (après clonage) !
		return cloneCache;
	}// getPointagesSince
	
	@Override
	/** Rafraîchit les comptes. La méthode garantit que les appels ultérieurs à
	 * getAll et getAllPointages renverront les mêmes instances (seul l'ordre
	 * diffère).
	 */
	public void refreshCompte(Compte compte) throws IOException {
		
		// Collection des écritures réinstanciées
		HashSet<Ecriture> nouvelles = new HashSet<Ecriture>();
		
		// Accès aux données des comptes
		CompteDAO cDAO = DAOFactory.getFactory().getCompteDAO();
		
		// Parcourir le cache pour chercher les écritures à réinstancier
		Iterator<Ecriture> it = cache.getCache().iterator();
		while (it.hasNext()) {
			Ecriture e = it.next();
			
			// Si l'Ecriture fait référence au compte modifié
			if (e.debit.id.equals(compte.id) || e.credit.id.equals(compte.id)) {
				nouvelles.add(new Ecriture(		//Mémoriser la nouvelle écriture
						e.id,
						e.date,
						e.pointage,
						cDAO.get(e.debit.id),	// Compte débit rechargé
						cDAO.get(e.credit.id),	// Compte crédit rechargé
						e.montant,
						e.libelle,
						e.tiers,
						e.cheque));
			}// if débit ou crédit du compte
		}// while cache
		
		/* Enlever les anciennes Ecritures et ajouter les nouvelles.
		 * Pour la méthode removeAll, on peut utiliser directement les nouvelles
		 * Ecritures, ce qui permet de supprimer toutes les écritures égales
		 * (equals) aux nouvelles.
		 */
		cache.getCache().removeAll(nouvelles);
		cache.getCache().addAll(nouvelles);
		cachePointage.getCache().removeAll(nouvelles);
		cachePointage.getCache().addAll(nouvelles);
		
		// Indiquer que les données ont été modifiées
		mustBeSaved = true;
		
		// Faire suivre au DAO sous-jacent
		dao.refreshCompte(compte);
	}// refreshCompte
	
	/** Sauvegarde le contenu du cache dans le CacheableEcritureDAO sous-jacent.
	 * @throws IOException 
	 */
	public void save() throws IOException {
		dao.save(cache.getCache());
	}
	
	/** Efface toutes les données du cache. */
	void erase() {
		cache = new CacheEcriture(dao);
		cachePointage = new CacheEcriture(cache);
	}
}
