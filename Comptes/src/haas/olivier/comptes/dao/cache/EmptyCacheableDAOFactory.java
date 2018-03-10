package haas.olivier.comptes.dao.cache;

import haas.olivier.comptes.Banque;
import haas.olivier.comptes.Compte;
import haas.olivier.comptes.Ecriture;
import haas.olivier.comptes.Permanent;
import haas.olivier.diagram.DiagramMemento;
import java.io.IOException;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

/**Une source de données fictive, ne contenant aucune donnée.<br>
 * <i>Pattern Null</i>.
 * <p>
 * En simulant une source de données réelle mais vide, cette classe permet de
 * gérer un cache de données entièrement saisies, avant de le transférer vers
 * une autre implémentation de <code>DAOFactory</code>.
 * <p> 
 * En pratique, la création d'un nouveau dossier de comptes s'appuie sur une
 * instance de cette classe.
 * <p>
 * Il va sans dire que la classe ne supporte pas la sauvegarde, et donc l'appel
 * à {@link #save(Iterable, Iterable, Iterable, Iterable, Map, Map, Map, Properties)}
 * provoque une exception.
 *
 * @author Olivier HAAS
 */
public class EmptyCacheableDAOFactory implements CacheableDAOFactory {

	/**
	 * Renvoie un itérateur vide.
	 */
	@Override
	public Iterator<Banque> getBanques() throws IOException {
		return Collections.emptyIterator();
	}

	/**
	 * Renvoie un itérateur vide.
	 */
	@Override
	public Iterator<Compte> getComptes() throws IOException {
		return Collections.emptyIterator();
	}

	/**
	 * Renvoie un itérateur vide.
	 */
	@Override
	public Iterator<Ecriture> getEcritures(CompteDAO cDAO) throws IOException {
		return Collections.emptyIterator();
	}

	/**
	 * Renvoie un itérateur vide.
	 */
	@Override
	public Iterator<Permanent> getPermanents(CachePermanentDAO cache,
			CompteDAO cDAO) throws IOException {
		return Collections.emptyIterator();
	}

	/**
	 * Renvoie un itérateur vide.
	 */
	@Override
	public Iterator<Solde> getHistorique() throws IOException {
		return Collections.emptyIterator();
	}

	/**
	 * Renvoie un itérateur vide.
	 */
	@Override
	public Iterator<Solde> getSoldesAVue() throws IOException {
		return Collections.emptyIterator();
	}

	/**
	 * Renvoie un itérateur vide.
	 */
	@Override
	public Iterator<Solde> getMoyennes() throws IOException {
		return Collections.emptyIterator();
	}

	/**
	 * Renvoie des propriétés vides.
	 */
	@Override
	public CacheablePropertiesDAO getProperties() throws IOException {
		return new CacheablePropertiesDAO() {

			@Override
			public Map<String, DiagramMemento> getDiagramProperties() {
				return Collections.emptyMap();
			}
			
		};// classe anonyme PropertiesDAO
	}

	/**
	 * Ne fait rien et soulève une <code>UnsupportedOperationException</code>.
	 * 
	 * @throws UnsupportedOperationException
	 * 			Dans tous les cas.
	 */
	@Override
	public void save(DAOFactory factory) {
		throw new UnsupportedOperationException(
				"Pas de source de données définie");
	}

	/**
	 * @return	<code>"indéfini"</code>.
	 */
	@Override
	public String getName() {
		return "indéfini";
	}

	/**
	 * @return	Une chaîne vide.
	 */
	@Override
	public String getSource() {
		return "";
	}

	/**
	 * @return	<code>null</code>.
	 */
	@Override
	public String getSourceFullName() {
		return null;
	}

	/**
	 * Aucune implémentation.
	 */
	@Override
	public void close() throws IOException {
	}

	/**
	 * @returns	<code>false</code>
	 */
	@Override
	public boolean canBeSaved() {
		return false;
	}

}
