package haas.olivier.comptes.dao.mysql;

import java.awt.Color;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;
import haas.olivier.comptes.Compte;
import haas.olivier.comptes.TypeCompte;

class MySqlComptesDAO implements Iterator<Compte> {
	
	/**
	 * Crée la table des comptes si elle n'existe pas déjà dans la base de
	 * données.
	 * 
	 * @param statement	La statement à utiliser.
	 * 
	 * @throws SQLException
	 */
	static void createTable(Statement statement) throws SQLException {
		statement.execute(
				"CREATE TABLE IF NOT EXISTS comptes ("
				+ "id INT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,"
				+ "nom VARCHAR(50) NOT NULL,"
				+ "type INT UNSIGNED NOT NULL,"
				+ "numero BIGINT DEFAULT NULL,"
				+ "ouverture DATE NOT NULL,"
				+ "cloture DATE DEFAULT NULL,"
				+ "couleur INT NOT NULL,"
				+ "CONSTRAINT FOREIGN KEY (type) REFERENCES types_comptes(id) ON UPDATE CASCADE ON DELETE RESTRICT)");
	}
	
	/**
	 * Sauvegarde des comptes.
	 * Les anciens comptes seront supprimés.
	 * 
	 * @param comptes		Les comptes à sauvegarder.
	 * @param connection	Une connexion active.
	 * 
	 * @throws SQLException
	 */
	static void save(Collection<Compte> comptes, Connection connection)
			throws SQLException {
		try (PreparedStatement statement = connection.prepareStatement(
				"INSERT INTO comptes"
				+ "(id, nom, type, couleur, ouverture, cloture, numero) "
				+ "VALUES (?,?,?,?,?,?,?)")) {
			
			for (Compte compte : comptes) {
				statement.setInt(1, compte.getId());
				statement.setString(2, compte.getNom());
				statement.setInt(3, compte.getType().id);
				statement.setInt(4, compte.getColor().getRGB());
				statement.setDate(5,
						new Date(compte.getOuverture().getTime()));
				
				java.util.Date cloture = compte.getCloture();
				statement.setDate(6,
						cloture == null ? null : new Date(cloture.getTime()));
				
				Long numero = compte.getNumero();
				if (numero != null) {
					statement.setLong(7, compte.getNumero());
				} else {
					statement.setNull(7, Types.BIGINT);
				}
				
				statement.execute();
			}
			
		} catch (SQLException e) {
			connection.rollback();
			throw e;
		}
	}

	/**
	 * Le résultat de la requête SQL sur l'ensemble des comptes.
	 */
	private final ResultSet resultSet;
	
	/**
	 * Construit un itérateur sur l'ensemble des comptes de la base de données.
	 * 
	 * @param connectionProvider	Un fournisseur de connexion à la base de
	 * 								données.
	 * 
	 * @throws SQLException
	 */
	MySqlComptesDAO(ConnectionProvider connectionProvider) throws SQLException {
		try (Connection connection = connectionProvider.getConnection();
				Statement statement = connection.createStatement()) {
			resultSet = statement.executeQuery("SELECT * FROM comptes ");
		}
	}
	
	@Override
	public boolean hasNext() {
		try {
			if (!resultSet.next()) {
				resultSet.close();
				return false;
			}
			resultSet.previous();
			return true;
			
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public Compte next() {
		try {
			if (!resultSet.next()) {
				throw new NoSuchElementException();
			}
			
			TypeCompte type = getTypeById(resultSet.getInt("type"));
			
			Compte compte = new Compte(resultSet.getInt("id"), type);
			compte.setNom(resultSet.getString("nom"));
			compte.setOuverture(resultSet.getDate("ouverture"));
			compte.setCloture(resultSet.getDate("cloture"));
			compte.setNumero(resultSet.getLong("numero"));
			compte.setColor(new Color(resultSet.getInt("couleur")));
			
			return compte;
			
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Renvoie un type de compte à partir de son identifiant.
	 * 
	 * @param id	L'identifiant du type de compte.
	 * @return		Le type compte portant cet identifiant.
	 */
	private TypeCompte getTypeById(int id) {
		for (TypeCompte type : TypeCompte.values()) {
			if (type.id == id) {
				return type;
			}
		}
		throw new RuntimeException("Type de compte inconnu");
	}

}
