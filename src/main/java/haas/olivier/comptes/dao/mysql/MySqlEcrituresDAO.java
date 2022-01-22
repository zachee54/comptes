package haas.olivier.comptes.dao.mysql;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;

import haas.olivier.comptes.Compte;
import haas.olivier.comptes.Ecriture;
import haas.olivier.comptes.EcritureMissingArgumentException;
import haas.olivier.comptes.InconsistentArgumentsException;

class MySqlEcrituresDAO implements Iterator<Ecriture> {

	/**
	 * Crée la table des écritures si elle n'existe pas déjà dans la base de
	 * données.
	 * 
	 * @param statement	La statement à utiliser.
	 * 
	 * @throws SQLException
	 */
	static void createTable(Statement statement) throws SQLException {
		statement.execute(
				"CREATE TABLE IF NOT EXISTS ecritures ("
				+ "id INT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,"
				+ "debit_id INT UNSIGNED NOT NULL,"
				+ "credit_id INT UNSIGNED NOT NULL,"
				+ "date DATE NOT NULL,"
				+ "pointage DATE DEFAULT NULL,"
				+ "libelle VARCHAR(50) NULL,"
				+ "tiers VARCHAR(50) NULL,"
				+ "cheque INT UNSIGNED DEFAULT NULL,"
				+ "montant INT NOT NULL,"
				+ "CONSTRAINT FOREIGN KEY ecritures_debits (debit_id) REFERENCES comptes(id) ON UPDATE CASCADE ON DELETE RESTRICT,"
				+ "CONSTRAINT FOREIGN KEY ecritures_credits (credit_id) REFERENCES comptes(id) ON UPDATE CASCADE ON DELETE RESTRICT)");
	}
	
	/**
	 * Sauvegarde des écritures.
	 * Les anciennes écritures seront supprimées.
	 * 
	 * @param ecritures		Les écritures à sauvegarder.
	 * @param connection	Une connexion active.
	 * 
	 * @throws SQLException
	 */
	static void save(Iterable<Ecriture> ecritures, Connection connection) throws SQLException {
		try (Statement statement = connection.createStatement();
				PreparedStatement ecritureStatement =
						connection.prepareStatement(
								"INSERT INTO ecritures"
								+ "(id, debit_id, credit_id, date, pointage, libelle, tiers, cheque, montant) "
								+ "VALUES (?,?,?,?,?,?,?,?,?)")) {
			
			for (Ecriture ecriture : ecritures) {
				ecritureStatement.setInt(1, ecriture.id);
				ecritureStatement.setInt(2, ecriture.debit.getId());
				ecritureStatement.setInt(3, ecriture.credit.getId());
				ecritureStatement.setDate(4, new Date(ecriture.date.getTime()));
				
				java.util.Date pointage = ecriture.pointage;
				ecritureStatement.setDate(5,
						pointage == null ? null : new Date(pointage.getTime()));
				
				ecritureStatement.setString(6, ecriture.libelle);
				ecritureStatement.setString(7, ecriture.tiers);
				
				if (ecriture.cheque != null) {
					ecritureStatement.setInt(8, ecriture.cheque);
				} else {
					ecritureStatement.setNull(8, Types.INTEGER);
				}
				
				ecritureStatement.setInt(9, ecriture.montant.movePointRight(2).intValue());
				
				ecritureStatement.execute();
			}
			
		} catch (SQLException e) {
			connection.rollback();
			throw e;
		}
	}
	
	/** Le résultat de la requête SQL sur l'ensemble des écritures. */
	private final ResultSet resultSet;
	
	/** Les comptes, classés par identifiants. */
	private final Map<Integer, Compte> comptesById;
	
	/**
	 * Construit un itérateur sur l'ensemble des écritures.
	 * 
	 * @param connectionProvider	Un fournisseur de connexion à la base de
	 * 								données.
	 * 
	 * @param comptesById			Les comptes, classés par identifiants.
	 * 
	 * @throws SQLException
	 */
	MySqlEcrituresDAO(ConnectionProvider connectionProvider,
			Map<Integer, Compte> comptesById)
					throws SQLException {
		this.comptesById = comptesById;
		try (Connection connection = connectionProvider.getConnection();
				Statement statement = connection.createStatement()) {
			resultSet = statement.executeQuery("SELECT * from ecritures");
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
	public Ecriture next() {
		try {
			if (!resultSet.next()) {
				throw new NoSuchElementException();
			}
			
			Integer cheque = resultSet.getInt("cheque");
			if (resultSet.wasNull()) {
				cheque = null;
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
					cheque);
			
		} catch (SQLException e) {
			throw new RuntimeException(e);
		} catch (EcritureMissingArgumentException e) {
			throw new RuntimeException(e);
		} catch (InconsistentArgumentsException e) {
			throw new RuntimeException(e);
		}
	}

}
