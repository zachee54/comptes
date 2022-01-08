package haas.olivier.comptes.dao.mysql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import haas.olivier.comptes.dao.PropertiesDAO;
import haas.olivier.comptes.dao.cache.CacheablePropertiesDAO;
import haas.olivier.diagram.DiagramMemento;

class MySqlPropertiesDAO implements CacheablePropertiesDAO {

	/**
	 * Crée les tables des propriétés de diagramme si elles n'existent pas déjà
	 * dans la base de données.
	 * 
	 * @param statement	La statement à utiliser.
	 * 
	 * @throws SQLException
	 */
	static void createTables(Statement statement) throws SQLException {
		statement.execute(
				"CREATE TABLE IF NOT EXISTS diagrams ("
				+ "id INT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,"
				+ "nom VARCHAR(50) NOT NULL,"
				+ "CONSTRAINT UNIQUE INDEX (nom))");
		
		statement.execute(
				"CREATE TABLE IF NOT EXISTS diagram_ordering ("
				+ "diagram_id INT UNSIGNED NOT NULL,"
				+ "rank INT UNSIGNED NOT NULL,"
				+ "serie INT UNSIGNED NOT NULL,"
				+ "CONSTRAINT FOREIGN KEY diagrams_diagram_ordering (diagram_id) REFERENCES diagrams(id) ON UPDATE CASCADE ON DELETE CASCADE)");
		
		statement.execute(
				"CREATE TABLE IF NOT EXISTS hidden_series ("
				+ "diagram_id INT UNSIGNED NOT NULL,"
				+ "serie INT UNSIGNED NOT NULL,"
				+ "CONSTRAINT FOREIGN KEY diagrams_hidden_series (diagram_id) REFERENCES diagrams(id) ON UPDATE CASCADE ON DELETE CASCADE)");
	}
	
	/**
	 * Sauvegarde les propriétés.
	 * 
	 * @param propertiesDAO	L'objet d'accès aux propriétés à sauvegarder.
	 * @param connection	Une connexion valide.
	 * 
	 * @throws SQLException
	 */
	static void save(PropertiesDAO propertiesDAO, Connection connection)
			throws SQLException {
		
		try (PreparedStatement diagramStatement = connection.prepareStatement(
				"INSERT INTO diagrams (nom) VALUES (?)",
				Statement.RETURN_GENERATED_KEYS);
				
				PreparedStatement orderStatement = connection.prepareStatement(
						"INSERT INTO diagram_ordering"
						+ "(diagram_id, rank, serie) "
						+ "VALUES (?,?,?)");
				
				PreparedStatement hiddenSeriesStatement =
						connection.prepareStatement(
								"INSERT INTO hidden_series (diagram_id, serie) "
								+ "VALUES (?,?)")) {
			
			for (String diagramName : propertiesDAO.getDiagramNames()) {
				
				// Sauvegarde le nom du diagramme
				diagramStatement.setString(1, diagramName);
				diagramStatement.execute();
				int id = getLastId(diagramStatement);
				
				DiagramMemento memento =
						propertiesDAO.getDiagramProperties(diagramName);
				int n = 1;
				
				for (Integer serie : memento.getSeries()) {
					
					// Sauvegarde l'ordre des séries
					orderStatement.setInt(1, id);
					orderStatement.setInt(2, n++);
					orderStatement.setInt(3, serie);
					orderStatement.execute();
					
					// Sauvegarde les séries masquées
					if (memento.isHidden(serie)) {
						hiddenSeriesStatement.setInt(1, id);
						hiddenSeriesStatement.setInt(2, serie);
						hiddenSeriesStatement.execute();
					}
				}
			}
		}
	}
	
	/**
	 * Renvoie la dernière clé primaire générée par la Statement.
	 * 
	 * @param statement	La statement.
	 * 
	 * @return			La dernière clé primaire générée. 
	 * 
	 * @throws SQLException
	 */
	private static int getLastId(PreparedStatement statement)
			throws SQLException {
		ResultSet generatedKeys = statement.getGeneratedKeys();
		generatedKeys.next();
		return generatedKeys.getInt(1);
	}
	
	/** Les propriétés de diagrammes. */
	private final Map<String, DiagramMemento> diagramProperties =
			new HashMap<>();
	
	/**
	 * Construit un objet d'accès aux propriétés.
	 * 
	 * @param connectionProvider	Un fournisseur de connexion à la base de
	 * 								données.
	 * 
	 * @throws SQLException
	 */
	MySqlPropertiesDAO(ConnectionProvider connectionProvider)
			throws SQLException {
		try (Connection connection = connectionProvider.getConnection();
				Statement statement = connection.createStatement()) {
			
			Map<String, List<Integer>> seriesOrder = getSeriesOrder(statement);
			Map<String, Set<Integer>> hiddenSeries = getHiddenSeries(statement);
			
			for (String name : seriesOrder.keySet()) {
				DiagramMemento memento = new DiagramMemento(
						name,
						seriesOrder.getOrDefault(name, new ArrayList<>()),
						hiddenSeries.getOrDefault(name, new HashSet<>()));
				diagramProperties.put(name, memento);
			}
		}
	}
	
	/**
	 * Lit l'ordre des séries des diagrammes dans la base de données.
	 * 
	 * @param statement	Une statement.
	 * @return			L'ordre des séries, triés par noms de diagrammes.
	 * 
	 * @throws SQLException
	 */
	private Map<String, List<Integer>> getSeriesOrder(Statement statement)
			throws SQLException {
		
		try (ResultSet orderResultSet = statement.executeQuery(
				"SELECT serie, nom FROM diagram_ordering "
						+ "JOIN diagrams ON (diagram_id = diagrams.id) "
						+ "ORDER BY rank")) {
			
			Map<String, List<Integer>> result = new HashMap<>();
			while (orderResultSet.next()) {
				String name = orderResultSet.getString("nom");
				int serie = orderResultSet.getInt("serie");
				
				result.putIfAbsent(name, new ArrayList<>());
				result.get(name).add(serie);
			}
			return result;
		}
	}
	
	/**
	 * Renvoie les identifiants des séries masquées pour chaque diagramme.
	 * 
	 * @param statement	Une statement.
	 * @return			Les séries masquées, triées par noms de diagrammes.
	 * 
	 * @throws SQLException
	 */
	private Map<String, Set<Integer>> getHiddenSeries(Statement statement)
			throws SQLException {
		
		try (ResultSet hiddenSeriesResultSet = statement.executeQuery(
				"SELECT serie, nom FROM hidden_series "
				+ "JOIN diagrams ON (diagram_id = diagrams.id)")) {
			
			Map<String, Set<Integer>> result = new HashMap<>();
			while (hiddenSeriesResultSet.next()) {
				String name = hiddenSeriesResultSet.getString("nom");
				int serie = hiddenSeriesResultSet.getInt("serie");
				
				result.putIfAbsent(name, new HashSet<>());
				result.get(name).add(serie);
			}
			return result;
		}
	}
	
	@Override
	public Map<String, DiagramMemento> getDiagramProperties() {
		return diagramProperties;
	}

}
