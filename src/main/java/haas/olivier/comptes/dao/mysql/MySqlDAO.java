package haas.olivier.comptes.dao.mysql;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.mariadb.jdbc.MariaDbDataSource;

import haas.olivier.comptes.Banque;
import haas.olivier.comptes.Compte;
import haas.olivier.comptes.Ecriture;
import haas.olivier.comptes.Permanent;
import haas.olivier.comptes.dao.cache.CacheDAOFactory;
import haas.olivier.comptes.dao.cache.CachePermanentDAO;
import haas.olivier.comptes.dao.cache.CacheableDAOFactory;
import haas.olivier.comptes.dao.cache.CacheablePropertiesDAO;
import haas.olivier.comptes.dao.cache.Solde;

/**
 * Un objet d'accès aux données au format MySQL.
 *
 * @author Olivier Haas
 */
public class MySqlDAO implements CacheableDAOFactory {
	
	/** Le fournisseur de connexion. */
	private final ConnectionProvider connectionProvider;
	
	/** Les comptes, classés par identifiants. */
	private Map<Integer, Compte> comptesById;
	
	/** La source de données. */
	private final MariaDbDataSource dataSource;
	
	/**
	 * Construit un accès à une source de données MySQL.
	 * 
	 * @param hostname	Le nom de l'hôte.
	 * @param port		Le port.
	 * @param database	Le nom de la base de données.
	 */
	public MySqlDAO(String hostname, int port, String database, String username,
			String password) {
		dataSource = new MariaDbDataSource(hostname, port, database);
		connectionProvider =
				new ConnectionProvider(dataSource, username, password);
	}

	@Override
	public Iterator<Banque> getBanques() throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Iterator<Compte> getComptes() throws IOException {
		try {
			return getComptesById().values().iterator();
		} catch (SQLException e) {
			throw new IOException(e);
		}
	}
	
	/**
	 * Renvoie les comptes classés par identifiants.
	 * 
	 * @return	Les comptes, classés par identifiants.
	 * 
	 * @throws SQLException
	 */
	private Map<Integer, Compte> getComptesById() throws SQLException {
		if (comptesById == null) {
			comptesById = loadComptes();
		}
		return comptesById;
	}
	
	/**
	 * Charge les comptes à partir de la base de données.
	 * 
	 * @return	Les comptes, classés par identifiants.
	 * 
	 * @throws SQLException
	 */
	private Map<Integer, Compte> loadComptes() throws SQLException {
		Iterator<Compte> comptesIterator =
				new MySqlComptesDAO(connectionProvider);

		Map<Integer, Compte> comptesMap = new HashMap<>();
		while (comptesIterator.hasNext()) {
			Compte compte = comptesIterator.next();
			comptesMap.put(compte.getId(), compte);
		}


		return comptesMap;
	}

	@Override
	public Iterator<Ecriture> getEcritures() throws IOException {
		try {
			return new MySqlEcrituresDAO(connectionProvider, getComptesById());
		} catch (SQLException e) {
			throw new IOException(e);
		}
	}

	@Override
	public Iterator<Permanent> getPermanents(CachePermanentDAO cache) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Iterator<Solde> getHistorique() throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Iterator<Solde> getSoldesAVue() throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Iterator<Solde> getMoyennes() throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CacheablePropertiesDAO getProperties() throws IOException {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public boolean canBeSaved() {
		return true;
	}

	@Override
	public void save(CacheDAOFactory cache) throws IOException {
		try (Connection connection = connectionProvider.getConnection()) {
			createTablesIfNotExist(connection);
			
			MySqlComptesDAO.save(cache.getCompteDAO().getAll(), connection);
			MySqlEcrituresDAO.save(cache.getEcritureDAO().getAll(), connection);
			
		} catch (SQLException e) {
			throw new IOException(e);
		}
	}
	
	/**
	 * Crée les tables SQL si elles n'existent pas déjà.
	 * 
	 * @param connection	Une connexion.
	 * @throws SQLException
	 */
	private void createTablesIfNotExist(Connection connection)
			throws SQLException {
		try (Statement statement = connection.createStatement()) {
			
			statement.execute(
					"CREATE TABLE IF NOT EXISTS comptes ("
					+ "id INT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,"
					+ "nom VARCHAR(50) NOT NULL,"
					+ "type INT UNSIGNED NOT NULL,"
					+ "numero BIGINT DEFAULT NULL,"
					+ "ouverture DATE NOT NULL,"
					+ "cloture DATE DEFAULT NULL,"
					+ "couleur INT NOT NULL)");
			
			statement.execute(
					"CREATE TABLE IF NOT EXISTS ecritures ("
					+ "id INT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,"
					+ "debit_id INT UNSIGNED NOT NULL,"
					+ "credit_id INT UNSIGNED NOT NULL,"
					+ "date DATE NOT NULL,"
					+ "pointage DATE DEFAULT NULL,"
					+ "libelle VARCHAR(50) NULL,"
					+ "tiers VARCHAR(50) NULL,"
					+ "cheque INT UNSIGNED DEFAULT NULL,"
					+ "montant INT NOT NULL,"
					+ "CONSTRAINT FOREIGN KEY ecritures_debits (debit_id) REFERENCES comptes(id) ON UPDATE CASCADE ON DELETE RESTRICT,"
					+ "CONSTRAINT FOREIGN KEY ecritures_credits (credit_id) REFERENCES comptes(id) ON UPDATE CASCADE ON DELETE RESTRICT)");
		}
	}

	@Override
	public String getName() {
		return "MySQL";
	}

	@Override
	public String getSource() {
		return dataSource.getDatabaseName();
	}

	@Override
	public String getSourceFullName() {
		return String.format("%s %s",
				dataSource.getServerName(),
				dataSource.getDatabaseName());
	}

	@Override
	public void close() throws IOException {
		connectionProvider.close();
	}

}
