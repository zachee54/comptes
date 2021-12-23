package haas.olivier.comptes.dao.mysql;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;

import haas.olivier.comptes.Compte;
import haas.olivier.comptes.Ecriture;
import haas.olivier.comptes.EcritureMissingArgumentException;
import haas.olivier.comptes.InconsistentArgumentsException;

class MySqlEcrituresDAO implements Iterator<Ecriture> {

	/** Le résultat de la requête SQL sur l'ensemble des écritures. */
	private ResultSet resultSet;
	
	/** Les comptes, classés par identifiants. */
	private final Map<Integer, Compte> comptesById;
	
	/**
	 * Construit un itérateur sur l'ensemble des écritures.
	 * 
	 * @param connection	Une connexion à la base de données.
	 * @param comptesById	Les comptes, classés par identifiants.
	 * 
	 * @throws SQLException
	 */
	MySqlEcrituresDAO(Connection connection, Map<Integer, Compte> comptesById)
			throws SQLException {
		this.comptesById = comptesById;
		try (Statement statement = connection.createStatement()) {
			resultSet = statement.executeQuery("SELECT * from ecritures");
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
	public Ecriture next() {
		try {
			if (!resultSet.next()) {
				throw new NoSuchElementException();
			}
			
			return new Ecriture(
					resultSet.getInt("id"),
					resultSet.getDate("date"),
					resultSet.getDate("pointage"),
					comptesById.get(resultSet.getInt("debit_id")),
					comptesById.get(resultSet.getInt("credit_id")),
					new BigDecimal(resultSet.getInt("montant"))
					.movePointLeft(2),
					resultSet.getString("libelle"),
					resultSet.getString("tiers"),
					resultSet.getInt("cheque"));
			
		} catch (SQLException e) {
			throw new RuntimeException(e);
		} catch (EcritureMissingArgumentException e) {
			throw new RuntimeException(e);
		} catch (InconsistentArgumentsException e) {
			throw new RuntimeException(e);
		}
	}

}
