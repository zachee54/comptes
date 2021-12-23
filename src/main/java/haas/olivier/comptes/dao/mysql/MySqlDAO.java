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
	
	/** Le nom d'utilisateur de la base de données. */
	private final String username;
	
	/** Le mot de passe de la base de données. */
	private final String password;
	
	/** La source de données. */
	private final MariaDbDataSource dataSource;
	
	/** La connexion courante à la base de données. */
	private Connection connection;
	
	/** Les comptes, classés par identifiants. */
	private Map<Integer, Compte> comptesById;
	
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
		this.username = username;
		this.password = password;
	}
	
	/**
	 * Renvoie une connexion à la base de données.
	 * 
	 * @return	Une connexion ouverte.
	 * 
	 * @throws SQLException
	 */
	Connection getConnection() throws SQLException {
		if (connection == null || connection.isClosed()) {
			connection = dataSource.getConnection(username, password);
		}
		return connection;
	}

	@Override
	public Iterator<Banque> getBanques() throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Iterator<Compte> getComptes() throws IOException {
		return getComptesById().values().iterator();
	}
	
	/**
	 * Renvoie les comptes classés par identifiants.
	 * 
	 * @return	Les comptes, classés par identifiants.
	 * 
	 * @throws IOException
	 */
	private Map<Integer, Compte> getComptesById() throws IOException {
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
	 * @throws IOException
	 */
	private Map<Integer, Compte> loadComptes() throws IOException {
		try (Connection connection = getConnection()) {
			Iterator<Compte> comptesIterator =
					new MySqlComptesDAO(connection);
			
			Map<Integer, Compte> comptesMap = new HashMap<>();
			while (comptesIterator.hasNext()) {
				Compte compte = comptesIterator.next();
				comptesMap.put(compte.getId(), compte);
			}
			
			
			return comptesMap;
			
		} catch (SQLException e) {
			throw new IOException(e);
		}
	}

	@Override
	public Iterator<Ecriture> getEcritures() throws IOException {
		// TODO Auto-generated method stub
		return null;
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
		try (Connection connection = getConnection()) {
			createTablesIfNotExist(connection);
			
			MySqlComptesDAO.save(cache.getCompteDAO().getAll(), connection);
			
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
					+ "cheque BIGINT UNSIGNED DEFAULT NULL,"
					+ "montant INT NOT NULL,"
					+ "epargne INT NOT NULL DEFAULT 0,"
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
		try {
			connection.close();
		} catch (SQLException e) {
			throw new IOException (e);
		}
	}

}
