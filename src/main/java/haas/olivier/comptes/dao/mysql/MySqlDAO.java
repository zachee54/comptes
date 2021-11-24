package haas.olivier.comptes.dao.mysql;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import org.mariadb.jdbc.MariaDbDataSource;

import haas.olivier.comptes.dao.BanqueDAO;
import haas.olivier.comptes.dao.CompteDAO;
import haas.olivier.comptes.dao.DAOFactory;
import haas.olivier.comptes.dao.EcritureDAO;
import haas.olivier.comptes.dao.PermanentDAO;
import haas.olivier.comptes.dao.PropertiesDAO;
import haas.olivier.comptes.dao.SuiviDAO;
import haas.olivier.util.Month;

/**
 * Un objet d'accès aux données au format MySQL.
 *
 * @author Olivier Haas
 */
public class MySqlDAO extends DAOFactory {
	
	/** Le nom d'utilisateur de la base de données. */
	private final String username;
	
	/** Le mot de passe de la base de données. */
	private final String password;
	
	/** La source de données. */
	private final MariaDbDataSource dataSource;
	
	/** La connexion courante à la base de données. */
	private Connection connection;
	
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
	public BanqueDAO getBanqueDAO() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CompteDAO getCompteDAO() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public EcritureDAO getEcritureDAO() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public PermanentDAO getPermanentDAO() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SuiviDAO getHistoriqueDAO() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SuiviDAO getSoldeAVueDAO() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SuiviDAO getMoyenneDAO() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public PropertiesDAO getPropertiesDAO() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean canBeSaved() {
		return true;
	}

	@Override
	public boolean mustBeSaved() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void save() throws IOException {
		try (Connection connection = getConnection()) {
			createTablesIfNotExist(connection);
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
					"CREATE TABLE IF NOT EXISTS compte_states ("
					+ "id INT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,"
					+ "type INT UNSIGNED NOT NULL,"
					+ "numero BIGINT DEFAULT NULL,"
					+ "CONSTRAINT UNIQUE KEY (type, numero))");
			statement.execute(
					"CREATE TABLE IF NOT EXISTS comptes ("
					+ "id INT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,"
					+ "nom VARCHAR(50) NOT NULL,"
					+ "ouverture DATE NOT NULL,"
					+ "cloture DATE DEFAULT NULL,"
					+ "couleur INT UNSIGNED NOT NULL,"
					+ "compte_state_id INT UNSIGNED NOT NULL,"
					+ "CONSTRAINT FOREIGN KEY comptes_states (compte_state_id) REFERENCES compte_states(id) ON UPDATE CASCADE ON DELETE RESTRICT)");
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
	protected void erase() throws IOException {
		// TODO Auto-generated method stub

	}

	@Override
	public Month getDebut() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getName() {
		return "MySQL";
	}

	@Override
	public String getSource() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getSourceFullName() {
		// TODO Auto-generated method stub
		return null;
	}

}
