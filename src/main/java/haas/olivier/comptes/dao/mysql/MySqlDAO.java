package haas.olivier.comptes.dao.mysql;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

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
		// TODO Auto-generated method stub

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
