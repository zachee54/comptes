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
		try (PreparedStatement compteStatement = connection.prepareStatement(
				"INSERT INTO comptes"
				+ "(id, nom, type, couleur, ouverture, cloture, numero) "
				+ "VALUES (?,?,?,?,?,?,?)")) {
			
			connection.setAutoCommit(false);
			compteStatement.execute("DELETE FROM ecritures");
			compteStatement.execute("DELETE FROM comptes");
			
			for (Compte compte : comptes) {
				compteStatement.setInt(1, compte.getId());
				compteStatement.setString(2, compte.getNom());
				compteStatement.setInt(3, compte.getType().ordinal());
				compteStatement.setInt(4, compte.getColor().getRGB());
				compteStatement.setDate(5,
						new Date(compte.getOuverture().getTime()));
				
				java.util.Date cloture = compte.getCloture();
				compteStatement.setDate(6,
						cloture == null ? null : new Date(cloture.getTime()));
				
				Long numero = compte.getNumero();
				if (numero != null) {
					compteStatement.setLong(7, compte.getNumero());
				} else {
					compteStatement.setNull(7, Types.BIGINT);
				}
				
				compteStatement.execute();
			}
			
		} catch (SQLException e) {
			connection.rollback();
			throw e;
		}
	}

	/**
	 * Le résultat de la requête SQL sur l'ensemble des comptes.
	 */
	private ResultSet resultSet;
	
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
			if (resultSet.isLast()) {
				resultSet.close();
				return false;
			}
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
			
			TypeCompte[] types = TypeCompte.values();
			TypeCompte type = types[resultSet.getInt("type")];
			
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

}
