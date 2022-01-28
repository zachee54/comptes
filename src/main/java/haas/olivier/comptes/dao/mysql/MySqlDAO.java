package haas.olivier.comptes.dao.mysql;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.sql.DataSource;

import org.mariadb.jdbc.MariaDbDataSource;

import haas.olivier.comptes.Banque;
import haas.olivier.comptes.Compte;
import haas.olivier.comptes.Ecriture;
import haas.olivier.comptes.Permanent;
import haas.olivier.comptes.TypeCompte;
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
	 * @param username	Le nom d'utilisateur de la base de données.
	 * @param password	Le mot de passe.
	 * 
	 * @throws IOException
	 */
	public MySqlDAO(String hostname, int port, String database, String username,
			String password) throws IOException {
		dataSource = new MariaDbDataSource(hostname, port, database);
		connectionProvider =
				new ConnectionProvider(dataSource, username, password);
		createDatabaseIfNotExists();
	}
	
	/**
	 * Crée la base de données si elle n'existe pas encore.
	 * 
	 * @throws IOException
	 */
	private void createDatabaseIfNotExists() throws IOException {
		DataSource rootDataSource = new MariaDbDataSource(
				dataSource.getServerName(),
				dataSource.getPort(),
				null);
		
		try (Connection rootConnection = rootDataSource.getConnection(
				connectionProvider.getUsername(),
				connectionProvider.getPassword());
				Statement statement = rootConnection.createStatement()) {
			statement.execute(
					"CREATE DATABASE IF NOT EXISTS "
							+ dataSource.getDatabaseName());
			
			createTablesIfNotExist();
			
		} catch (SQLException e) {
			throw new IOException(e);
		}
	}
	
	/**
	 * Crée les tables SQL si elles n'existent pas déjà.
	 * 
	 * @throws SQLException
	 */
	private void createTablesIfNotExist() throws SQLException {
		try (Connection connection = connectionProvider.getConnection();
				Statement statement = connection.createStatement()) {
			createTypesComptesTable(statement);
			MySqlComptesDAO.createTable(statement);
			MySqlEcrituresDAO.createTable(statement);
			MySqlPermanentsDAO.createTables(statement);
			MySqlPropertiesDAO.createTables(statement);
		}
	}
	
	/**
	 * Crée la table des types de comptes.
	 * 
	 * @param statement		Une instruction SQL prête à l'emploi.
	 * 
	 * @throws SQLException
	 */
	private void createTypesComptesTable(Statement statement)
			throws SQLException {
		statement.execute(
				"CREATE TABLE IF NOT EXISTS types_comptes("
				+ "id INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,"
				+ "nom VARCHAR(50) NOT NULL,"
				+ "isEpargne TINYINT(1) NOT NULL DEFAULT 0,"
				+ "isBancaire TINYINT(1) NOT NULL,"
				+ "rank INT UNSIGNED NOT NULL)");
	}

	@Override
	public Iterator<Banque> getBanques() throws IOException {
		return Collections.emptyIterator();
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
		try {
			return new MySqlPermanentsDAO(connectionProvider, getComptesById());
		} catch (SQLException e) {
			throw new IOException(e);
		}
	}

	@Override
	public Iterator<Solde> getHistorique() throws IOException {
		return Collections.emptyIterator();
	}

	@Override
	public Iterator<Solde> getSoldesAVue() throws IOException {
		return Collections.emptyIterator();
	}

	@Override
	public Iterator<Solde> getMoyennes() throws IOException {
		return Collections.emptyIterator();
	}

	@Override
	public CacheablePropertiesDAO getProperties() throws IOException {
		try {
			return new MySqlPropertiesDAO(connectionProvider);
		} catch (SQLException e) {
			throw new IOException(e);
		}
	}
	
	@Override
	public boolean canSaveSuivis() {
		return false;
	}

	@Override
	public boolean canBeSaved() {
		return true;
	}

	@Override
	public void save(CacheDAOFactory cache) throws IOException {
		try (Connection connection = connectionProvider.getConnection();
				Statement statement = connection.createStatement()) {

			connection.setAutoCommit(false);

			/* 
			 * Vident aussi les tables associées, par cascade de clés
			 * étrangères.
			 */
			statement.execute("DELETE FROM permanents");
			statement.execute("DELETE FROM diagrams");

			statement.execute("DELETE FROM ecritures");
			statement.execute("DELETE FROM comptes");
			statement.execute("DELETE FROM types_comptes");

			fillTypesComptesTable(connection);
			MySqlComptesDAO.save(cache.getCompteDAO().getAll(), connection);
			MySqlEcrituresDAO.save(
					cache.getEcritureDAO().getAll(), connection);
			MySqlPermanentsDAO.save(
					cache.getPermanentDAO().getAll(), connection);
			MySqlPropertiesDAO.save(cache.getPropertiesDAO(), connection);

			connection.setAutoCommit(true);

		} catch (SQLException e) {
			throw new IOException(e);
		}
	}
	
	/**
	 * Sauvegarde les types de comptes.
	 * 
	 * @param connection	Une connexion valide.
	 * 
	 * @throws SQLException
	 */
	private void fillTypesComptesTable(Connection connection)
			throws SQLException {
		try (PreparedStatement prepared = connection.prepareStatement(
				"REPLACE INTO types_comptes (id, nom, isEpargne, isBancaire, rank)"
				+ " VALUES (?,?,?,?,?)")) {
			
			for (TypeCompte type : TypeCompte.values()) {
				if (type.id < 0) {
					continue;
				}
				prepared.setInt(1, type.id);
				prepared.setString(2, type.nom);
				prepared.setBoolean(3, type.isEpargne());
				prepared.setBoolean(4, type.isBancaire());
				
				Integer rank = null;
				switch (type.id) {
				case 1: rank = 6;
				case 2: rank = 7;
				case 3: rank = 8;
				case 4: rank = 9;
				case 5: rank = 1;
				case 6: rank = 2;
				case 7: rank = 3;
				case 8: rank = 4;
				case 9: rank = 5;
				}
				prepared.setInt(5, rank);
				
				prepared.execute();
			}
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
		return String.format("jdbc:mysql:%s:%s:%s:%s:%s",
				dataSource.getServerName(),
				dataSource.getPort(),
				dataSource.getDatabaseName(),
				dataSource.getUserName(),
				connectionProvider.getPassword());
	}

	/**
	 * Ferme les ressources et lance le recalcul des soldes.
	 */
	@Override
	public void close() throws IOException {
		connectionProvider.close();
	}

}
