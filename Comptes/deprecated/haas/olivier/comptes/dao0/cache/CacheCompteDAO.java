package haas.olivier.comptes.dao0.cache;

import haas.olivier.comptes.Compte;
import haas.olivier.comptes.CompteBancaire;
import haas.olivier.comptes.CompteBudget;
import haas.olivier.comptes.Ecriture;
import haas.olivier.comptes.MessagesFactory;
import haas.olivier.comptes.dao.CompteDAO;
import haas.olivier.comptes.dao.DAOFactory;
import haas.olivier.comptes.dao.util.CacheCompte;

import java.io.IOException;
import java.util.Iterator;
import java.util.Set;

/**
 * Classe d'accès aux données des comptes implémentant un cache.
 * Les données sont entièrement chargées en mémoire à partir d'une sous-couche
 * avant d'être exploitées. La sauvegarde écrit le contenu du cache en écrasant
 * toutes les anciennes données.
 *
 * @author Olivier HAAS
 */
public class CacheCompteDAO implements CompteDAO {
	
	/** Sous-couche DAO */
	private CacheableCompteDAO dao;
	
	/** Le cache */
	private CacheCompte cache;
	
	/** Indicateur de modification depuis la dernière sauvegarde. */
	boolean mustBeSaved = false;
	
	/** Construit une couche DAO avec cache utilisant les données du DAO
	 * spécifié. 
	 * @throws IOException  */
	public CacheCompteDAO(CacheableCompteDAO dao) {
		this.dao = dao;								// Mémoriser le sous-DAO
		cache = new CacheCompte(dao);				// Créer le cache
	}// reload
	
	@Override
	public Set<Compte> getAll() throws IOException {
		return cache.getCloneCache();
	}// getAll

	@Override
	public Compte get(int id) throws IOException {
		for (Compte c : cache.getCache()) {			// Parcourir le cache
			if (c.id != null						// Il y a un identifiant
					&& c.id.intValue() == id) {		// et c'est le bon
				return c;							// Renvoyer le bon compte
			}
		}
		MessagesFactory.getInstance()				// Pas trouvé: erreur !
		.showErrorMessage("Impossible de trouver le compte n°" + id);
		throw new IOException();
	}// get

	/** La méthode s'exécute en temps logarithmique. */
	@Override
	public Compte add(Compte c) throws IOException {
		Compte compte2add = c;						// Compte à ajouter
		
		// Si le compte n'a pas encore d'identifiant
		if (c.id == null) {
			
			// Instancier un nouveau Compte avec cet id
			if (c instanceof CompteBancaire) {		// Compte bancaire
				compte2add = new CompteBancaire(
						cache.getNextId(),			// Le plus grand + 1
						c.getNom(),
						((CompteBancaire) c).getNumero(),
						c.getType());
			} else if (c instanceof CompteBudget) {	// Compte budget
				compte2add = new CompteBudget(
						cache.getNextId(),			// Le plus grand + 1
						c.getNom(),
						c.getType());
			} else {								// Erreur !
				MessagesFactory.getInstance().showErrorMessage(
						"Compte non reconnu: " + c);
				throw new IOException();
			}// if type de compte
			
			// Copier les dates d'ouverture et de clôture
			compte2add.setOuverture(c.getOuverture());
			compte2add.setCloture(c.getCloture());
			
		} else {									// Déjà un identifiant
			
			// Vérifier que l'identifiant n'existe pas déjà dans le DAO
			for (Compte compte : cache.getCache()) {
				if (compte.id.equals(c.id)) {		// C'est le même id !
					MessagesFactory.getInstance().showErrorMessage(
							"Il existe déjà un compte n°" + c.id +
							".\nImpossible d'en ajouter un autre.");
					throw new IOException();
				}
			}
		}// if id null
		
		// Ajouter le compte au cache
		cache.getCache().add(compte2add);
		
		// Indiquer que les données ont été modifiées
		mustBeSaved = true;
		
		return compte2add;
	}// add

	/** La méthode s'exécute en temps logarithmique. */
	@Override
	public void update(Compte c) throws IOException {
		
		// Rechercher un compte ayant même id
		Compte ancien = null;
		Set<Compte> cacheComptes = cache.getCache();
		for (Compte compte : cacheComptes) {
			if (compte.id.equals(c.id)) {
				ancien = compte;
				break;
			}
		}// for cache
		
		// A condition d'avoir trouvé un compte de même identifiant en cache
		if (ancien != null) {
			cacheComptes.remove(ancien);				// Supprimer l'ancien
			if (!cacheComptes.add(c)) {					// Insérer le nouveau
				MessagesFactory.getInstance().showErrorMessage(
						"Le compte n°" + c.id + " n'a pas pu être mis à jour");
				throw new IOException();
			}
			
			// Indiquer que les données ont été modifiées
			mustBeSaved = true;
			
		}// s'il y a un ancien
	}// update

	@Override
	/** La méthode s'exécute en temps linéaire en fonction du nombre d'écritures
	 * et du nombre de comptes. */
	public void remove(int id) throws IOException {
		Integer i = new Integer(id);
		
		// Vérifier que ce Compte n'est pas utilisé
		for (Ecriture e : DAOFactory.getFactory().getEcritureDAO().getAll()) {
			if (i.equals(e.debit.id) || i.equals(e.credit.id)) {
				MessagesFactory.getInstance().showErrorMessage(
						"Impossible de supprimer le compte n°" + id +
						"\nIl est utilisé dans l'écriture n°" + e.id);
				throw new IOException();
			}
		}// for all Ecritures
		
		// Chercher et supprimer
		Iterator<Compte> it = cache.getCache().iterator();
		while (it.hasNext()) {
			if (i.equals(it.next().id)) {
				it.remove();							// Supprimer
				
				// Indiquer que les données ont été modifiées
				mustBeSaved = true;
				
				break;									// Arrêter là
			}
		}// for comptes
	}// remove
	
	/** Sauvegarde le cache. 
	 * @throws IOException */
	public void save() throws IOException {
		dao.save(cache.getCache());
	}
	
	/** Efface toutes les données du cache. */
	void erase() {
		cache = new CacheCompte(dao);
	}
}