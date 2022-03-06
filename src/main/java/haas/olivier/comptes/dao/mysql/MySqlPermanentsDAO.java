package haas.olivier.comptes.dao.mysql;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.function.Function;

import haas.olivier.comptes.Compte;
import haas.olivier.comptes.Permanent;
import haas.olivier.comptes.PermanentFixe;
import haas.olivier.comptes.PermanentProport;
import haas.olivier.comptes.PermanentSoldeur;
import haas.olivier.comptes.PermanentState;
import haas.olivier.util.Month;

class MySqlPermanentsDAO implements Iterator<Permanent> {

	/**
	 * Crée les tables des opérations permanentes si elles n'existent pas déjà
	 * dans la base de données.
	 * 
	 * @param statement	La statement à utiliser.
	 * 
	 * @throws SQLException
	 */
	static void createTables(Statement statement) throws SQLException {
		statement.execute(
				"CREATE TABLE IF NOT EXISTS permanents ("
				+ "id INT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,"
				+ "nom VARCHAR(50),"
				+ "debit_id INT UNSIGNED NOT NULL,"
				+ "credit_id INT UNSIGNED NOT NULL,"
				+ "libelle VARCHAR(50),"
				+ "tiers VARCHAR(50),"
				+ "dependance_id INT UNSIGNED DEFAULT NULL,"
				+ "taux DECIMAL(5,2) DEFAULT NULL,"
				+ "pointer TINYINT(1),"
				+ "CONSTRAINT FOREIGN KEY permanents_debits (debit_id) REFERENCES comptes(id) ON UPDATE CASCADE ON DELETE RESTRICT,"
				+ "CONSTRAINT FOREIGN KEY permanents_credits (credit_id) REFERENCES comptes(id) ON UPDATE CASCADE ON DELETE RESTRICT)");
		
		statement.execute(
				"CREATE TABLE IF NOT EXISTS permanents_jours ("
				+ "permanent_id INT UNSIGNED NOT NULL,"
				+ "year INT UNSIGNED NOT NULL,"
				+ "month INT UNSIGNED NOT NULL,"
				+ "jour INT NOT NULL,"
				+ "CONSTRAINT UNIQUE INDEX (permanent_id, year, month),"
				+ "CONSTRAINT FOREIGN KEY permanents_jours (permanent_id) REFERENCES permanents(id) ON UPDATE CASCADE ON DELETE CASCADE)");
		
		statement.execute(
				"CREATE TABLE IF NOT EXISTS permanents_montants ("
				+ "permanent_id INT UNSIGNED NOT NULL,"
				+ "year INT UNSIGNED NOT NULL,"
				+ "month INT UNSIGNED NOT NULL,"
				+ "montant INT NOT NULL,"
				+ "CONSTRAINT UNIQUE INDEX (permanent_id, year, month),"
				+ "CONSTRAINT FOREIGN KEY permanents_montants (permanent_id) REFERENCES permanents(id) ON UPDATE CASCADE ON DELETE CASCADE)");
	}
	
	/**
	 * Sauvegarde toutes les opérations permanentes.
	 * 
	 * @param permanents	Les opérations permanentes.
	 * @param connection	Une connexion valide.
	 * 
	 * @throws SQLException
	 */
	static void save(Iterable<Permanent> permanents, Connection connection)
			throws SQLException {
		
		try (Statement statement = connection.createStatement()) {
			
			// Première passe pour sauvegarder les opérations sans leurs états
			savePermanents(permanents, connection);
			
			// Deuxième passe pour sauvegarder les états et les jours
			saveDependances(permanents, connection);
			saveJours(permanents, connection);
			saveMontants(permanents, connection);
		}
	}
	
	/**
	 * Sauvegarde des opérations permanentes sans leur état.
	 * 
	 * Cette méthode effectue une première passe permettant de s'assurer que
	 * toutes les opérations permanentes existent dans la base avant de créer
	 * des états dépendant de certaines d'entre elles.
	 * 
	 * @param permanents	Les opérations à sauvegarder.
	 * @param connection	Une connexion valide.
	 * 
	 * @throws SQLException
	 */
	private static void savePermanents(Iterable<Permanent> permanents,
			Connection connection)
					throws SQLException {
		
		try (PreparedStatement statement = connection.prepareStatement(
				"INSERT INTO permanents"
				+ "(id, nom, debit_id, credit_id, libelle, tiers, pointer) "
				+ "VALUES (?,?,?,?,?,?,?)")) {
			
			for (Permanent permanent : permanents) {
				statement.setInt(1, permanent.getId());
				statement.setString(2, permanent.getNom());
				statement.setInt(3, permanent.getDebit().getId());
				statement.setInt(4, permanent.getCredit().getId());
				statement.setString(5, permanent.getLibelle());
				statement.setString(6, permanent.getTiers());
				statement.setBoolean(7, permanent.isAutoPointee());

				statement.execute();
			}
		}
	}
	
	/**
	 * Sauvegarde les dépendances d'une opération permanente à une autre.
	 * 
	 * @param permanents	Les opérations permanentes.
	 * @param connection	Une connexion valide.
	 * 
	 * @throws SQLException
	 */
	private static void saveDependances(Iterable<Permanent> permanents,
			Connection connection) throws SQLException {
		
		try (PreparedStatement statement = connection.prepareStatement(
				"UPDATE permanents SET dependance_id = ?, taux = ? "
				+ "WHERE id = ?")) {
			
			for (Permanent permanent : permanents) {
				PermanentState state = permanent.getState();
				if (state instanceof PermanentProport) {
					PermanentProport proport = (PermanentProport) state;
					statement.setInt(1, proport.dependance.getId());
					statement.setBigDecimal(2, proport.taux);
					statement.setInt(3, permanent.getId());
					statement.execute();
				}
			}
		}
	}
	
	/**
	 * Sauvegarde les jours des opérations permanentes.
	 * 
	 * @param permanents	Les opérations permanentes.
	 * @param connection	Une connexion valide.
	 * 
	 * @throws SQLException
	 */
	private static void saveJours(Iterable<Permanent> permanents,
			Connection connection) throws SQLException {
		saveMultiple(
				permanents,
				"INSERT INTO permanents_jours(permanent_id, year, month, jour) "
						+ "VALUES (?,?,?,?)",
				Permanent::getJours,
				Function.identity(),
				connection);
	}
	
	/**
	 * Sauvegarde les montants des opérations permanentes à montants fixés.
	 * 
	 * @param permanents	Les opérations permanentes.
	 * @param connection	Une connexion valide.
	 * 
	 * @throws SQLException
	 */
	private static void saveMontants(Iterable<Permanent> permanents,
			Connection connection) throws SQLException {
		saveMultiple(
				permanents,
				"INSERT INTO permanents_montants"
						+ "(permanent_id, year, month, montant) "
						+ "VALUES (?,?,?,?)",
				p -> p.getState() instanceof PermanentFixe
						? ((PermanentFixe) p.getState()).montants
						: Collections.emptyMap(),
				d -> d.movePointRight(2).intValue(),
				connection);
	}
	
	/**
	 * Sauvegarde des données multiples d'opérations permanentes sous forme
	 * d'entiers.
	 * En pratique, cette méthode sert pour les jours et les montants.
	 * 
	 * @see #saveJours(Permanent, Connection)
	 * @see #saveMontants(Permanent, Connection)
	 * 
	 * @param <T>			Le type des données de départ.
	 * @param permanents	Les opérations permanentes.
	 * @param sql			La requête SQL.
	 * @param dataFunction	La fonction permettant d'obtenir les données à
	 * 						sauvegarder.
	 * @param function		La conversion des données en int.
	 * @param connection	Une connexion valide.
	 * 
	 * @throws SQLException
	 */
	private static <T> void saveMultiple(Iterable<Permanent> permanents,
			String sql, Function<Permanent, Map<Month, T>> dataFunction,
			Function<T, Integer> formatter, Connection connection)
					throws SQLException {
		
		try (PreparedStatement statement = connection.prepareStatement(sql)) {
			
			for (Permanent permanent : permanents) {
				Map<Month, T> data = dataFunction.apply(permanent);
				
				for (Entry<Month, T> entry : data.entrySet()) {
					Month month = entry.getKey();
					statement.setInt(1, permanent.getId());
					statement.setInt(2, month.getYear());
					statement.setInt(3, month.getNumInYear());
					statement.setInt(4, formatter.apply(entry.getValue()));
					statement.execute();
				}
			}
		}
	}
	
	private final ResultSet resultSet;
	
	/** Les comptes. */
	private final Map<Integer, Compte> comptesById;
	
	/**
	 * POJO déjà instanciés. Ils peuvent servir dans la suite de l'itération.
	 */
	private final Map<Integer, Permanent> permanents = new HashMap<>();
	
	/**
	 * Les jours, triés par identifiant d'opération permanente et par mois.
	 */
	private final Map<Integer, Map<Month, Integer>> joursByPermanent;
	
	/**
	 * Les montants, triés par identifiant d'opération permanente et par mois.
	 */
	private final Map<Integer, Map<Month, BigDecimal>> montantsByPermanent;
	
	/**
	 * Construit un itérateur sur l'ensemble des opérations permanentes.
	 * 
	 * @param connectionProvider	Un objet d'accès à une connexion valide.
	 * @param comptesById			Les comptes, triés par identifiants.
	 * 
	 * @throws SQLException
	 */
	MySqlPermanentsDAO(ConnectionProvider connectionProvider,
			Map<Integer, Compte> comptesById)
					throws SQLException {
		this.comptesById = comptesById;
		try (Connection connection = connectionProvider.getConnection();
				Statement statement = connection.createStatement()) {
			
			resultSet = statement.executeQuery(
					"SELECT * from permanents ORDER BY dependance_id ASC");
			
			joursByPermanent = fetchJoursByPermanent(statement);
			montantsByPermanent = fetchMontantsByPermanent(statement);
		}
	}
	
	/**
	 * Collecte les jours par permanent et par mois.
	 * 
	 * @param statement	Une instruction SQL.
	 * 
	 * @return			Les jours, triés par identifiant de permanents et par
	 * 					mois.
	 * 
	 * @throws SQLException
	 */
	private Map<Integer, Map<Month, Integer>> fetchJoursByPermanent(
			Statement statement) throws SQLException {
		return collectByPermanentAndMonth(
				statement,
				"SELECT * FROM permanents_jours",
				"jour",
				Function.identity());
	}
	
	/**
	 * Collecte les montants des permanents fixes, par permanent et par mois.
	 * 
	 * @param statement	Une instruction SQL.
	 * 
	 * @return			Les montants, triés par identifiant de permanent et par
	 * 					mois.
	 * 
	 * @throws SQLException
	 */
	private Map<Integer, Map<Month, BigDecimal>> fetchMontantsByPermanent(
			Statement statement) throws SQLException {
		return collectByPermanentAndMonth(
				statement,
				"SELECT * FROM permanents_montants",
				"montant",
				i -> new BigDecimal(i).movePointLeft(2));
	}
	
	/**
	 * Collecte des données par permanent et par mois.
	 * 
	 * @see #fetchJoursByPermanent(Statement)
	 * @see {@link #fetchMontantsByPermanent(Statement)}
	 * 
	 * @param <T>		Le type des données à collecter.
	 * @param statement	Une instruction SQL.
	 * @param sql		Le texte de la requête à exécuter.
	 * @param field		Le nom du champ contenant les valeurs.
	 * @param function	Une fonction qui permet de convertir des entiers en
	 * 					valeur finale, par exemple en <code>BigDecimal</code>.
	 * 
	 * @return			Une Map à deux niveaux contenant les valeurs par
	 * 					identifiant de permanent et par mois.
	 * 
	 * @throws SQLException
	 */
	private <T> Map<Integer, Map<Month, T>> collectByPermanentAndMonth(
			Statement statement, String sql, String field,
			Function<Integer, T> function)
					throws SQLException {
		try (ResultSet resultSet = statement.executeQuery(sql)) {
			
			Map<Integer, Map<Month, T>> result = new HashMap<>();
			while (resultSet.next()) {
				int id = resultSet.getInt("permanent_id");
				result.putIfAbsent(id, new HashMap<>());
				
				Map<Month, T> dataByMonth = result.get(id);
				dataByMonth.put(
						Month.getInstance(
								resultSet.getInt("year"),
								resultSet.getInt("month")),
						function.apply(resultSet.getInt(field)));
			}
			return result;
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
	public Permanent next() {
		try {
			if (!resultSet.next()) {
				throw new NoSuchElementException();
			}
			
			int id = resultSet.getInt("id");
			Permanent permanent = new Permanent(
					id,
					resultSet.getString("nom"),
					comptesById.get(resultSet.getInt("debit_id")),
					comptesById.get(resultSet.getInt("credit_id")),
					resultSet.getString("libelle"),
					resultSet.getString("tiers"),
					resultSet.getBoolean("pointer"),
					joursByPermanent.get(id));
			
			PermanentState state = null;
			Permanent dependance =
					permanents.get(resultSet.getInt("dependance_id"));
			
			if (montantsByPermanent.containsKey(id)) {
				state = new PermanentFixe(montantsByPermanent.get(id));
				
			} else if (!resultSet.wasNull()) {
				int intTaux = resultSet.getInt("taux");
				BigDecimal taux = new BigDecimal(intTaux).movePointLeft(2);
				state = new PermanentProport(dependance, taux);
				
			} else {
				state = new PermanentSoldeur(permanent);
			}
			permanent.setState(state);
			
			// Mémoriser pour servir d'eventuelle dépendance future
			permanents.put(id, permanent);
			
			return permanent;
			
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

}
