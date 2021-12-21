package haas.olivier.comptes.dao.mysql;

import java.awt.Color;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;
import haas.olivier.comptes.Compte;
import haas.olivier.comptes.TypeCompte;

class MySqlComptesLoader implements Iterator<Compte> {
	
	/**
	 * Sauvegarde des comptes.
	 * 
	 * @throws SQLException
	 */
	static void save(Collection<Compte> comptes, Connection connection)
			throws SQLException {
		try (PreparedStatement compteStatement = connection.prepareStatement(
				"REPLACE INTO comptes"
				+ "(id, nom, ouverture, cloture, couleur, compte_state_id) "
				+ "VALUES (?,?,?,?,?,?)");
				
				PreparedStatement stateStatement = connection.prepareStatement(
						"REPLACE INTO compte_states (type, numero) "
						+ "VALUES (?,?)",
						Statement.RETURN_GENERATED_KEYS)) {
			
			Iterator<Compte> comptesIt = comptes.iterator();
			while (comptesIt.hasNext()) {
				Compte compte = comptesIt.next();
				
				stateStatement.setInt(1, compte.getType().ordinal());
				stateStatement.setLong(2, compte.getNumero());
				stateStatement.executeUpdate();
				Integer stateId = null;
				try (ResultSet keys = stateStatement.getGeneratedKeys()) {
					if (keys.next()) {
						stateId = keys.getInt(1);
					}
				}
				
				compteStatement.setInt(1, compte.getId());
				compteStatement.setString(2, compte.getNom());
				compteStatement.setDate(3,
						new Date(compte.getOuverture().getTime()));
				compteStatement.setDate(4,
						new Date(compte.getCloture().getTime()));
				compteStatement.setInt(5, compte.getColor().getRGB());
				compteStatement.setInt(6, stateId);
				compteStatement.execute();
			}
		}
	}

	/**
	 * Le résultat de la requête SQL sur l'ensemble des comptes.
	 */
	private ResultSet resultSet;
	
	/**
	 * Construit un itérateur sur l'ensemble des comptes de la base de données.
	 * 
	 * @param connection	Une connexion à la base de données.
	 * 
	 * @throws SQLException
	 */
	MySqlComptesLoader(Connection connection) throws SQLException {
		try (Statement statement = connection.createStatement()) {
			resultSet = statement.executeQuery(
					"SELECT *, type, numero FROM comptes "
					+ "JOIN compte_states ON (comptes.compte_state_id = compte_states.id)");
		}
	}
	
	@Override
	public boolean hasNext() {
		try {
			return !resultSet.isLast();
			
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
