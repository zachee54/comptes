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
				+ "(id, nom, type, couleur, ouverture, cloture, numero) "
				+ "VALUES (?,?,?,?,?,?,?)")) {
			
			Iterator<Compte> comptesIt = comptes.iterator();
			while (comptesIt.hasNext()) {
				Compte compte = comptesIt.next();
				
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
				}
				
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
			resultSet = statement.executeQuery("SELECT * FROM comptes ");
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
