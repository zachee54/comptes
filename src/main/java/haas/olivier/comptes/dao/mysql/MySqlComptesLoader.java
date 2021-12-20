package haas.olivier.comptes.dao.mysql;

import java.awt.Color;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Iterator;
import java.util.NoSuchElementException;

import haas.olivier.comptes.Compte;
import haas.olivier.comptes.TypeCompte;

class MySqlComptesLoader implements Iterator<Compte> {

	/**
	 * Le résultat de la requête SQL sur l'ensemble des comptes.
	 */
	private ResultSet resultSet;
	
	MySqlComptesLoader(MySqlDAO dao) throws SQLException {
		try (Connection connection = dao.getConnection();
				Statement statement = connection.createStatement()) {
			
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
