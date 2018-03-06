package haas.olivier.comptes.dao0.buffer;

import haas.olivier.util.Month;
import haas.olivier.comptes.dao.AbstractSuiviDAO;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Map;

/**
 * Interface d'accès aux données permettant un traitement par lots des suivis de
 * comptes. Cette interface peut être utilisée avec une sous-couche DAO
 * implémentant un buffer.
 * 
 * @author Olivier HAAS
 */
public interface BufferableSuiviDAO extends AbstractSuiviDAO {

	/**
	 * Supprime les suivis des comptes et remplace plusieurs valeurs en un seul
	 * accès. Par convention, from est antérieur ou égal à toutes les clés de
	 * toSet. Dans le cas contraire, le comportement de la méthode n'est pas
	 * défini.
	 * 
	 * @param from
	 *            Le mois (inclus) à partir duquel effacer les anciens suivis.
	 * @param toSet
	 *            La multimap des suivis à (re)définir.
	 * @throws IOException
	 */
	void save(Month from, Map<Month, Map<Integer, BigDecimal>> toSet)
			throws IOException;
}
