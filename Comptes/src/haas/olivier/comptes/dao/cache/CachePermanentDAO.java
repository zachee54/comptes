package haas.olivier.comptes.dao.cache;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import haas.olivier.comptes.Permanent;
import haas.olivier.comptes.PermanentFixe;
import haas.olivier.comptes.PermanentProport;
import haas.olivier.comptes.PermanentSoldeur;
import haas.olivier.comptes.dao.CompteDAO;
import haas.olivier.comptes.dao.IdGenerator;
import haas.olivier.comptes.dao.PermanentDAO;

/**
 * Un objet d'accès aux données qui garde en cache toutes les opérations
 * permanentes.
 *
 * @author Olivier HAAS
 */
public class CachePermanentDAO implements PermanentDAO {

	/**
	 * Toutes les opérations permanentes.
	 */
	private final Map<Integer, Permanent> permanents = new HashMap<>();
	
	/**
	 * Drapeau indiquant si les données ont été modifiées depuis la dernière
	 * sauvegarde.
	 */
	private boolean mustBeSaved = false;
	
	/** Le générateur d'identifiants. */
	// TODO Supprimer les identifiants des opérations permanentes
	private IdGenerator idGen = new IdGenerator();
	
	/** Construit un objet d'accès aux données qui garde en cache toutes les
	 * opérations permanentes.
	 * 
	 * @param factory	Une fabrique de DAO permettant de récupérer au cours de
	 * 					l'énumération les objets <code>Permanent</code> déjà
	 * 					instanciés (utile pour les instances qui dépendent d'une
	 * 					autre).
	 * 
	 * @param cDAO		Un DAO d'accès aux comptes, pour permettre de récupérer
	 * 					les objets <code>Compte</code> auxquelles doivent se
	 * 					référer les <code>Permanent</code>s à instancier.
	 * 
	 * @throws IOException
	 */
	CachePermanentDAO(CacheableDAOFactory factory, CompteDAO cDAO)
			throws IOException {
		Iterator<Permanent> perm = factory.getPermanents(this);
		while (perm.hasNext()) {
			Permanent p = perm.next();		// L'opération permanente
			permanents.put(p.id, p);		// Ajouter l'opération permanente
			idGen.addId(p.id);				// Mémoriser l'identifiant
		}// for permanent
	}// constructeur

	/** Renvoie toutes les opérations permanentes.<br>
	 * Les opérations dépendant d'une autre sont toujours parcourues après
	 * l'opération dont elles dépendent.
	 */
	@Override
	public Iterable<Permanent> getAll() throws IOException {
		
		// Récupérer une liste des instances
		List<Permanent> list = new ArrayList<>(permanents.values());
		
		// Placer toujours les dépendances avant les dépendantes
		for (int i=0; i<list.size(); i++) {
			Permanent p = list.get(i);				// Chaque permanent
			if (p instanceof PermanentProport) {	// Si elle est dépendante
				int j = list.indexOf(				// Index de la dépendance
						((PermanentProport) p).dependance);
				if (j > i) {						// Si dépendance est après
					/* Déplacer la dépendance juste devant la dépendante.
					 * On décrémente i afin de refaire la boucle sur la
					 * dépendance, des fois qu'elle soit elle-même dépendante
					 * d'une autre opération...
					 */
					list.add(i--, list.remove(j));	// Enlever j et mettre en i
				}// if dépendance après
			}// if opération dépendante
		}// for index
		
		// Renvoyer la liste
		return list;
	}// getAll

	@Override
	public Permanent get(int id) {
		return permanents.get(id);
	}// get

	@Override
	public Permanent add(Permanent p) {
		
		// Selon que l'opération à ajouter possède déjà un identifiant ou non
		if (p.id == null) {
			
			// Réinstancier avec un identifiant (selon le type d'opération)
			if (p instanceof PermanentFixe) {
				p = new PermanentFixe(idGen.getId(), p.nom, p.debit, p.credit,
						p.libelle, p.tiers, p.pointer, p.jours,
						((PermanentFixe) p).montants);
				
			} else if (p instanceof PermanentSoldeur) {
				p = new PermanentSoldeur(idGen.getId(), p.nom, p.debit,
						p.credit, p.libelle, p.tiers, p.pointer, p.jours);
				
			} else if (p instanceof PermanentProport) {
				p = new PermanentProport(idGen.getId(), p.nom, p.debit,
						p.credit, p.libelle, p.tiers, p.pointer, p.jours,
						((PermanentProport) p).dependance,
						((PermanentProport) p).taux);
			}// if instance Permanent
			
		} else if (permanents.containsKey(p.id)) {	// Identifiant existant
			throw new IllegalArgumentException(
					"Impossible d'ajouter au modèle : L'opération n°" + p.id +
					"existe déjà");
			
		} else {									// Identifiant inexistant
			permanents.put(p.id, p);
		}// if id null
		
		mustBeSaved = true;							// Sauvegarde attendue
		return p;
	}// add

	@Override
	public void update(Permanent p) {
		remove(p.id);								// Supprimer l'ancien
		add(p);										// Ajouter le nouveau
		mustBeSaved = true;
	}// update

	@Override
	public void remove(int id) {
		permanents.remove(id);						// Supprimer du cache
		mustBeSaved = true;							// Sauvegarde attendue
	}// remove
	
	/** Efface toutes les données. */
	void erase() {
		permanents.clear();
		idGen = new IdGenerator();
		mustBeSaved = true;
	}// erase
	
	/** Indique si les données ont été modifiées depuis la dernière sauvegarde.
	 */
	boolean mustBeSaved() {
		return mustBeSaved;
	}// mustBeSaved

	/** Oblige l'objet à considérer que les modifications actuelles ont été
	 * sauvegardées.
	 */
	void setSaved() {
		mustBeSaved = false;
	}// setSaved
}
