package haas.olivier.comptes.dao0.buffer;

import haas.olivier.comptes.Compte;
import haas.olivier.comptes.Ecriture;
import haas.olivier.comptes.MessagesFactory;
import haas.olivier.util.Month;
import haas.olivier.comptes.dao.EcritureDAO;
import haas.olivier.comptes.dao.util.CacheEcriture;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableSet;
import java.util.Set;
import java.util.TreeSet;

/**
 * Buffer pour les BufferableEcritureDAO.
 * 
 * @author Olivier Haas
 */
public class BufferedEcritureDAO extends EcritureDAO {

	// Sous-couche DAO
	private BufferableEcritureDAO dao;

	// Collections d'écritures à ajouter/supprimer/mettre à jour
	private Set<Integer>			remove	= new HashSet<Integer>();
	private Map<Integer, Ecriture>	add		= new HashMap<Integer, Ecriture>();
	private Map<Integer, Ecriture>	update	= new HashMap<Integer, Ecriture>();

	// Cache des écritures par ordre naturel et par ordre de pointage
	private CacheEcriture cache, cachePointage;
	
	public BufferedEcritureDAO(BufferableEcritureDAO dao) {
		this.dao = dao;								// Mémoriser le DAO
		cache = new CacheEcriture(dao);				// Créer le cache normal
		cachePointage = new CacheEcriture(cache);	// Créer le cache pointages
	}// constructeur
	
	/** Détermine quel identifiant utiliser pour une nouvelle écriture.
	 * Il s'agit de l'entier supérieur au plus grand identifiant de la base.
	 * La méthode parcourt la base au premier appel, puis incrémente le résultat
	 * à chaque appel suivant, jusqu'à une sauvegarde. 
	 * @return	Le plus grand identifiant augmenté de 1, ou -1 s'il n'y a aucune
	 * 			écriture. 
	 * @throws IOException */
	private synchronized int nextId() throws IOException {
		
		// Chercher dans le cache
		int maxId = cache.getNextId();

		// Chercher dans le buffer des écritures ajoutées
		for (Integer id : add.keySet()) {
			if (id >= maxId) {					// Aussi grand ou plus grand ?
				maxId = id + 1;					// Prendre encore plus grand
			}
		}// for add
		return maxId;
	}// getMaxId

	@Override
	public Ecriture add(Ecriture e) throws IOException {
		Integer id = e.id;

		// Une écriture du même identifiant aurait-elle été supprimée ?
		if (id != null && remove.contains(id)) {
			remove.remove(id);	// Ne pas supprimer
			update.put(id, e);	// Changer pour une mise à jour
			return e;
		}

		// Ajouter l'écriture
		Ecriture ajoutee;
		if (e.id == null) {
			// Pas d'identifiant: cloner l'écriture avec un nouvel identifiant

			// Définir une nouvelle Ecriture avec le nouvel identifiant
			ajoutee = new Ecriture(
					nextId(),e.date, e.pointage, e.debit, e.credit, e.montant,
					e.libelle, e.tiers, e.cheque);

		} else {
			// Déjà un identifiant: garder le même objet
			ajoutee = e;
		}

		// Stocker la nouvelle écriture pour ajout
		add.put(ajoutee.id, ajoutee);

		return ajoutee;
	}// add

	@Override
	public void update(Ecriture e) throws IOException {
		Integer id = e.id; // Identifiant

		// Cette écriture aurait-elle été supprimée ?
		if (remove.contains(id)) {

			// L'écriture n'est plus censée exister: lever une exception
			throw new IOException();
		}

		// Cette écriture aurait-elle été ajoutée récemment ?
		if (add.containsKey(id)) {

			// Remplacer l'écriture à ajouter par celle-ci
			add.put(id, e);

		} else {

			// Mettre à jour simplement cette écriture
			update.put(id, e);

		}// if add containsKey
	}// update

	@Override
	public void remove(int id) throws IOException {

		Integer i = new Integer(id);

		if (add.containsKey(i)) {
			// Cette écriture a été ajoutée récemment

			// Ne pas l'ajouter
			add.remove(i);

		} else if (update.containsKey(i)) {
			// Mise à jour récemment

			// Ne pas mettre à jour
			update.remove(i);
		}

		// Supprimer dans tous les cas
		remove.add(i);
	}// remove

	@Override
	public Ecriture get(int id) throws IOException {

		// Ecriture supprimée ?
		if (remove.contains(id)) {
			MessagesFactory.getInstance().showErrorMessage(
					"Tentative de lire une écriture supprimée");
			throw new IOException();
		}

		// Ecriture mise à jour ?
		if (update.containsKey(id)) {
			return update.get(id);
		}

		// Ecriture ajoutée ?
		if (add.containsKey(id)) {
			return add.get(id);
		}

		// Sinon, aller lire dans le cache
		for (Ecriture e : cache.getCache()) {
			if (new Integer(id).equals(e.id)) {
				return e;
			}
		}// for cache
		
		// Inconnu au bataillon: erreur !
		throw new IOException();
	}// get

	@Override
	public TreeSet<Ecriture> getAll() throws IOException {

		// Obtenir toutes les écritures
		TreeSet<Ecriture> all = cache.getCloneCache();

		// Enlever les écritures modifiées ou supprimées
		Iterator<Ecriture> it = all.iterator();		// Itérateur obligatoire
		while (it.hasNext()) {						// Pour chaque écriture
			Integer id = it.next().id;
			if (remove.contains(id)					// Identifiant à supprimer ?
					|| update.containsKey(id)) {	// Identifiant mis à jour ?
				it.remove();						// Enlever l'écriture
			}
		}

		// Ajouter les écritures mises à jour
		all.addAll(update.values());
			
		// Ecritures ajoutées entretemps
		all.addAll(add.values());

		return all;
	}// getAll

	@Override
	public NavigableSet<Ecriture> getAllSince(Month mois) throws IOException {

		// Cloner le cache
		NavigableSet<Ecriture> all = cache.getCloneCache();

		// Parcourir la collection pour voir où s'arrêter
		Ecriture stop = null;
		for (Ecriture e : all) {
			if (mois.after(e.date)) {			// Ecriture antérieure au mois ?
				stop = e;						// On a trouvé l'Ecriture limite
				break;
			}
		}
		
		// Créer une collection avec le début uniquement
		NavigableSet<Ecriture> allSince = (stop == null)
				? all							// Pas de limite: tout prendre
				: all.headSet(stop, false);		// Tout à partir de la limite

		// Enlever les écritures modifiées ou supprimées
		Iterator<Ecriture> it = allSince.iterator();
		while (it.hasNext()) {
			Ecriture e = it.next();
			Integer id = e.id;

			// Ecriture supprimée ou modifiée ?
			if (remove.contains(id) || update.containsKey(id)) {
				it.remove(); 				// Supprimer pour l'instant
			}
		}// while allSince

		// Compiler les écritures ajoutées ou mises à jour
		Set<Ecriture> nouveaux = new HashSet<Ecriture>(update.values());
		nouveaux.addAll(add.values());

		// Sélectionner les nouvelles écritures en fonction de leur date
		for (Ecriture e : nouveaux) {
			if (!mois.after(e.date)) {		// Si à partir du mois cible
				try {
					allSince.add(e);		// Ajouter à la liste
				} catch (IllegalArgumentException e1) {
					// Exception si l'écriture est hors champ (précaution)
				}
			}
		}

		return allSince;
	}// getAllSince

	@Override
	public NavigableSet<Ecriture> getPointagesSince(Month mois)
			throws IOException {

		// Cloner le cache
		NavigableSet<Ecriture> all = cachePointage.getCloneCache();

		// Parcourir la collection pour voir où s'arrêter
		Ecriture stop = null;
		for (Ecriture e : all) {
			if (e.pointage != null				// Pointée...
					&& mois.after(e.pointage)) {// ...depuis ce mois ?
				stop = e;						// On a trouvé l'Ecriture limite
				break;
			}
		}
		
		// Créer une collection avec le début uniquement
		NavigableSet<Ecriture> allSince =
				stop == null					// Si pas de limité trouvée
				? all							// On prend tout
				: all.headSet(stop, false);		// Sinon, le début seulement
		
		// Enlever les écritures modifiées ou supprimées
		Iterator<Ecriture> it = all.iterator();
		while (it.hasNext()) {
			Integer id = it.next().id;			// Identifiant
			
			// Ecriture supprimée ou modifiée
			if (remove.contains(id) || update.containsKey(id)) {
				it.remove();					// Supprimer pour l'instant
			}
		}// while all

		// Compiler les écritures ajoutées ou mises à jour
		Set<Ecriture> nouveaux = new HashSet<Ecriture>(update.values());
		nouveaux.addAll(add.values());

		// Sélectionner les nouvelles écritures en fonction de leur pointage
		for (Ecriture e : nouveaux) {

			// Pas de pointage, ou pendant ou après le mois
			if (e.pointage == null || !mois.after(e.pointage)) {
				try {
					allSince.add(e);				// Ajouter à la liste
				} catch (IllegalArgumentException e1) {
					// Exception si l'écriture est hors champ (précaution)
				}
			}
		}// for nouvelles écritures
		return allSince;
	}// getPointagesSince

	/** Renvoie une écriture pointant, en tant que de besoin, vers le compte
	 * spécifié.
	 * @param e	L'écriture à réinstancier.
	 * @param c	Le nouveau compte
	 * @return	Si l'écriture e ne mouvemente pas de compte ayant le même
	 * 			identifiant que c, renvoie l'écriture e. Sinon, une nouvelle
	 * 			écriture qui pointe vers c en lieu et place du compte de même
	 * 			identifiant que c. */
	private Ecriture reinstantiateEcriture(Ecriture e, Compte c) {
		Ecriture newOne = e;
		if (e.debit.id.equals(c.id)) {
			newOne = new Ecriture(e.id, e.date, e.pointage,
					c,				// Nouveau compte débité
					e.credit, e.montant, e.libelle, e.tiers, e.cheque);
		} else if(e.credit.id.equals(c.id)) {
			newOne = new Ecriture(e.id, e.date, e.pointage, e.debit,
					c,				// Nouveau compte crédité
					e.montant, e.libelle, e.tiers, e.cheque);
		}
		return newOne;
	}// reinstantiateEcriture
	
	@Override
	/** Remplace les écritures dans add, update et cache par une écriture
	 * adéquate (la même instance si c'est possible). */
	public void refreshCompte(Compte compte) throws IOException {
		
		// Changer les écritures dans le cache des écritures à ajouter
		for (Entry<Integer,Ecriture> entry : add.entrySet()) {
			add.put(entry.getKey(),		// Remplacer l'écriture par la nouvelle
					reinstantiateEcriture(entry.getValue(), compte));
		}
		
		// Changer les écritures dans le cache des écritures à modifier
		for (Entry<Integer,Ecriture> entry : update.entrySet()) {
			update.put(entry.getKey(),	// Remplacer l'écriture par la nouvelle
					reinstantiateEcriture(entry.getValue(), compte));
		}
		
		// Changer les écritures dans le cache classique
		TreeSet<Ecriture> contenuCache = cache.getCache();
		HashSet<Ecriture> nouvelles = new HashSet<Ecriture>();
		for (Ecriture e : contenuCache) {
			Ecriture nouvelle =			// Rafraîchir l'Ecriture
					reinstantiateEcriture(e, compte);
			if (nouvelle != e) {		// S'il y a un changement
				nouvelles.add(nouvelle);// Mémoriser la nouvelle Ecriture
			}
		}// for cache
		
		// Insérer les nouvelles (les anciennes font doublon et sont supprimées)
		contenuCache.addAll(nouvelles);
		
		// Appeler la méthode sur le DAO sous-jacent
		dao.refreshCompte(compte);
	}// refreshCompte
	
	/** Indique si ce DAO a besoin d'être sauvegardé. */
	public boolean mustBeSaved() {
		
		// Dès qu'il y a au moins un élément dans le buffer, il faut sauvegarder
		return ((add.size() + update.size() + remove.size()) != 0);
	}
	
	/**
	 * Sauvegarde les données en cours et vide le buffer.
	 * 
	 * @throws IOException
	 */
	void flush() throws IOException {

		// Sauvegarder
		dao.save(add, update, remove);

		synchronized(this) {
			
			// Vider le buffer
			add.clear();
			update.clear();
			remove.clear();
			
			// Vider le cache
			cache.clear();
			cachePointage.clear();
		}
	}// save

	
	/** Efface toutes les données du buffer et du cache. */
	void erase() {
		add.clear();
		remove.clear();
		update.clear();
		cache = new CacheEcriture(dao);
		cachePointage = new CacheEcriture(cache);
	}

}
