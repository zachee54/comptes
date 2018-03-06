package haas.olivier.comptes.dao0.util;

import haas.olivier.util.Month;
import haas.olivier.comptes.dao.AbstractSuiviDAO;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/** Encapsule un cache contenant des suivis de comptes (historiques, soldes à
 * vue, moyennes glissantes). Les données sont chargées à partir d'un
 * AbstractSuiviDAO à la première utilisation.
 *
 * @author Olivier HAAS
 */
public class CacheSuivi
extends AbstractCache<HashMap<Month,Map<Integer,BigDecimal>>> {
	
	/** Le DAO sous-jacent. */
	private AbstractSuiviDAO dao;

	public CacheSuivi(AbstractSuiviDAO dao) {
		this.dao = dao;
	}
	
	@Override
	protected HashMap<Month, Map<Integer, BigDecimal>> load()
			throws IOException {
		
		// Charge les données
		Map<Month,Map<Integer,BigDecimal>> all = dao.getAll();
		
		// Bonne classe ?
		if (all instanceof HashMap) {
			
			// Retourner tel quel
			return (HashMap<Month, Map<Integer, BigDecimal>>) all;
			
		} else {
			
			// Transférer dans une HashMap
			return new HashMap<Month,Map<Integer,BigDecimal>>(all);
		}
	}// load

	@Override
	@SuppressWarnings("unchecked")
	public HashMap<Month, Map<Integer, BigDecimal>> getCloneCache()
			throws IOException {
		return (HashMap<Month, Map<Integer, BigDecimal>>) getCache().clone();
	}

	@Override
	/** Méthode sans objet, non implémentée. */
	protected void initMaxId() throws IOException {
	}
}
