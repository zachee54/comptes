package haas.olivier.comptes.dao.cache;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

import haas.olivier.comptes.Compte;
import haas.olivier.comptes.dao.SuiviDAO;
import haas.olivier.util.Month;

/**
 * Un objet d'accès aux données qui garde en cache tous les suivi des comptes.
 * 
 * @author Olivier HAAS
 */
public class CacheSuiviDAO implements SuiviDAO {
	
	/**
	 * Les suivis des comptes.
	 */
	private final Map<Month, Map<Compte, BigDecimal>> suivis = new HashMap<>();

	/**
	 * Construit un objet d'accès aux données qui garde en cache tous les suivis
	 * des comptes.
	 * 
	 * @param soldes	Un itérateur de soldes.
	 */
	public CacheSuiviDAO(Iterator<Solde> soldes){
		while (soldes.hasNext())
			set(soldes.next());
	}
	
	@Override
	public void set(Solde solde) {
		set(solde.compte, solde.month, solde.montant);
	}

	@Override
	public void set(Compte compte, Month month, BigDecimal montant) {
		if (!suivis.containsKey(month))
			suivis.put(month, new HashMap<>());
		suivis.get(month).put(compte, montant);
	}
	
	@Override
	public Iterator<Solde> getAll() {
		return new SoldeIterator();
	}
	
	/**
	 * Renvoie une liste des comptes suivis.
	 * 
	 * @return	Une liste des comptes. Chaque compte apparaît au plus une fois
	 * 			dans la liste. Cette liste n'est pas triée.
	 */
	public List<Compte> getComptes() {
		return suivis.values().stream()
				.flatMap(map -> map.keySet().stream())
				.distinct()
				.collect(Collectors.toList());
	}

	public Iterable<Month> getMonths() {
		return Collections.unmodifiableSet(suivis.keySet());
	}
	
	@Override
	public BigDecimal get(Compte compte, Month month) {
		if (!suivis.containsKey(month))
			return null;
		
		Map<Compte, BigDecimal> montantsByCompte = suivis.get(month);
		if (!montantsByCompte.containsKey(compte))
			return null;
		
		return montantsByCompte.get(compte);
	}

	@Override
	public void removeFrom(Month debut) {
		Iterator<Month> it = suivis.keySet().iterator();
		while (it.hasNext()) {
			if (!debut.after(it.next())) {
				it.remove();
			}
		}
	}

	/**
	 * Efface toutes les données.
	 */
	public void erase() {
		suivis.clear();
	}
	
	/**
	 * Un itérateur de soldes.
	 * 
	 * @author Olivier Haas
	 */
	private class SoldeIterator implements Iterator<Solde> {

		/**
		 * Un itérateur sur les mois et leurs données.
		 */
		private final Iterator<Month> monthIterator =
				suivis.keySet().iterator();
		
		/**
		 * Le mois actuel.
		 */
		private Month month;
		
		/**
		 * L'itérateur sur les comptes et leurs données pour un mois spécifique.
		 */
		private Iterator<Entry<Compte, BigDecimal>> compteIterator;
		
		/**
		 * Construit un itérateur de soldes.
		 */
		private SoldeIterator() {
			if (monthIterator.hasNext()) {
				nextMonth();
			} else {
				compteIterator = Collections.emptyIterator();
			}
		}
		
		@Override
		public boolean hasNext() {
			
			/* Si on a fini le mois actuel, passer au prochain mois à lire */
			while (!compteIterator.hasNext()) {
				if (monthIterator.hasNext()) {
					nextMonth();
				} else {
					return false;
				}
			}
			
			return true;
		}
		
		/**
		 * Affecte le prochain itérateur de comptes à {@link #compteIterator}.
		 */
		private void nextMonth() {
			month = monthIterator.next();
			compteIterator = suivis.get(month).entrySet().iterator();
		}

		@Override
		public Solde next() {
			if (!hasNext())				// Passe au mois suivant si nécessaire
				throw new NoSuchElementException();
			
			Entry<Compte, BigDecimal> compteAndMontant = compteIterator.next();
			return new Solde(
					month,
					compteAndMontant.getKey(),
					compteAndMontant.getValue());
		}
	}
}
