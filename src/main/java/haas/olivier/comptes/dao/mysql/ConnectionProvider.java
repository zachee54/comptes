package haas.olivier.comptes.dao.mysql;

import java.io.Closeable;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

class ConnectionProvider implements Closeable {
	
	/** La source de données SQL. */
	private final DataSource dataSource;
	
	/** Le nom d'utilisateur de la base de données. */
	private final String username;
	
	/** Le mot de passe de la base de données. */
	private final String password;
	
	/** La connexion courante à la base de données. */
	private Connection connection;
	
	ConnectionProvider(DataSource dataSource, String username, String password) {
		this.dataSource = dataSource;
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
	public Connection getConnection() throws SQLException {
		if (connection == null || connection.isClosed()) {
			connection = dataSource.getConnection(username, password);
		}
		return connection;
	}
	
	public void close() throws IOException {
		try {
			connection.close();
		} catch (SQLException e) {
			throw new IOException (e);
		}
	}
}
