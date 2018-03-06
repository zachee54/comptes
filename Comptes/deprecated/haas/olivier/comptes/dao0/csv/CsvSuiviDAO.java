package haas.olivier.comptes.dao0.csv;

import java.io.CharArrayWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.csvreader.CsvReader;
import com.csvreader.CsvWriter;

import haas.olivier.comptes.MessagesFactory;
import haas.olivier.util.Month;
import haas.olivier.comptes.dao.buffer.BufferableSuiviDAO;

public class CsvSuiviDAO implements BufferableSuiviDAO {

	/** Contenu du fichier CSV. */
	char[] content;

	// Colonne contenant les mois
	private int colMois;

	/**
	 * Construit un DAO pour les suivis de comptes.
	 */
	public CsvSuiviDAO() {
		setContent(null);
	}
	
	/**
	 * Définit le contenu CSV à utiliser.
	 * 
	 * @param content	Le contenu CSV à lire.
	 */
	protected void setContent(char[] content) {
		if (content != null) {
			this.content = content;					// Contenu CSV spécifié
		} else {
			// Créer un fichier minimal s'il n'existe pas
			CharArrayWriter out = new CharArrayWriter();// Écriture en char[]
			CsvWriter writer = CsvDAO.getWriter(out);	// Flux CSV
			try {
				writer.write("mois");					// Ecrire en-tête unique
			} catch (IOException e) {
				e.printStackTrace();
			}
			this.content = out.toCharArray();			// Mettre en mémoire
			CsvDAO.close(null, writer);					// On ferme
		}// if null
	}// constructeur

	/**
	 * Ouvre une ressource en lecture sur le fichier CSV des historiques. Les
	 * index de colonnes colMois et colCompte sont initialisés.
	 * 
	 * @param id
	 *            Identifiant du compte à lire
	 * @return un CsvReader prêt à l'emploi.
	 * @throws IOException
	 */
	private CsvReader getReader() throws IOException {

		CsvReader reader = CsvDAO.getReader(content);
		colMois = reader.getIndex("mois");

		return reader;
	}// getReader

	@Override
	public Map<Month, Map<Integer, BigDecimal>> getAll() throws IOException {

		// Construire la multimap
		Map<Month, Map<Integer, BigDecimal>> map =
				new HashMap<Month, Map<Integer, BigDecimal>>();

		CsvReader reader = null;

		try {
			// Ouvrir un Reader
			reader = getReader();

			// Récupérer les en-têtes
			String[] headers = reader.getHeaders();

			// Parcourir les lignes
			while (reader.readRecord()) {
				try {
					// Récupérer le mois
					Month mois =
							new Month(CsvDAO.DF.parse(reader.get(colMois)));

					// Préparer une sous-map
					Map<Integer, BigDecimal> submap =
							new HashMap<Integer, BigDecimal>();

					// Insérer la sous-map dans la multimap
					map.put(mois, submap);

					// Ajouter tous les montants de ce mois dans la sous-map
					String[] values = reader.getValues();
					for (int i = 0; i < values.length; i++) {
						if (i != colMois) {// Ne pas lire la colonne période
							try {
								submap.put(
										Integer.valueOf(headers[i]),// Id
										new BigDecimal( // Montant
												values[i].replace(',', '.')));
							} catch (NumberFormatException e1) {
								// Montant illisible: passer à la suite
							}
						}
					}
				} catch (ParseException e) {
					// Mois illisible: passer la ligne simplement
				}// try
			}// while

		} catch (IOException e1) {
			MessagesFactory.getInstance().showErrorMessage(
					"Impossible de lire le suivi CSV");
			throw e1;

		} finally {
			// Fermer la ressource
			if (reader != null) {
				reader.close();
			}
		}

		return map;
	}// getAll

	@Override
	/** Supprime et définit des suivis de compte en un seul accès
	 * @param from	Mois à partir duquel supprimer les anciennes données.
	 * @param toSet	Nouvelles données à définir (en plus de celles qui n'ont
	 * 				pas été supprimées)
	 * @throws IOException */
	public void save(Month from, Map<Month, Map<Integer, BigDecimal>> toSet)
			throws IOException {
		CsvReader reader = getReader();
		CharArrayWriter out = new CharArrayWriter();
		CsvWriter writer = CsvDAO.getWriter(out);

		try {
			// 1°) Recenser les colonnes actuelles et les nouveaux comptes

			// Collection des identifiants des comptes (actuels et nouveaux)
			Set<Integer> comptes = new HashSet<Integer>();

			// Map d'id de comptes/index des colonnes (actuels et nouveaux)
			Map<Integer, Integer> oldcols = new HashMap<Integer, Integer>();
			Map<Integer, Integer> newcols;

			// Y a-t-il de nouveaux comptes ?
			boolean nouveauxComptes = false; // Valeur par défaut

			// Colonnes actuelle et nouvelle contenant le mois
			int newcolMois;

			// Déterminer la colonne de chaque compte actuel

			// Recenser les en-têtes actuels
			for (int i = 0; i < reader.getHeaderCount(); i++) {

				// Valeur de cet en-tête
				String header = reader.getHeaders()[i];

				// Pour toutes les colonnes autres que la colonne du mois
				if (!header.equals("mois")) {

					// Identifiant numérique du compte
					Integer id = Integer.valueOf(header);

					// Ajouter ce compte à la collection
					comptes.add(id);

					// Ajouter le lien entre l'id de compte et cette colonne
					oldcols.put(id, Integer.valueOf(i));
				}
			}// for Headers

			// Récupérer les identifiants des nouveaux comptes
			for (Map<Integer, BigDecimal> submap : toSet.values()) {

				// Identifiants de comptes dans cette ligne
				for (Integer id : submap.keySet()) {

					// Est-ce un nouveau compte ?
					if (!oldcols.keySet().contains(id)) {
						nouveauxComptes = true;

						// Ajouter à la collection
						comptes.add(id);
					}
				}
			}// for toSet

			// Ecrire les en-têtes
			if (nouveauxComptes) { // Cas où il fait refaire les en-têtes

				// Nouveaux en-têtes: autant que de comptes + la colonne période
				String[] headers = new String[comptes.size() + 1];

				// Préparer les nouvelles colonnes d'en-tête
				newcols = new HashMap<Integer, Integer>();
				int index = 0; // Pointeur de colonne
				newcolMois = index;
				headers[index++] = "mois"; // Première colonne: le mois

				// Redéfinir les index de colonnes et en-têtes
				for (Integer id : comptes) {
					Integer col = index++; // Id de la nouvelle colonne
					newcols.put(id, col); // Stocker l'id de colonne
					headers[col] = "" + id; // Nouvel en-tête
				}

				// Ecrire les en-têtes
				writer.writeRecord(headers);

			} else {

				// Pas de changements: garder les mêmes en-têtes (gain de temps)
				writer.writeRecord(reader.getHeaders());

				// Garder la même map id de comptes/id de colonnes
				newcols = oldcols;
				newcolMois = colMois;

			}// if nouveauxComptes

			// Transcrire les lignes à conserver
			while (reader.readRecord()) {

				// Obtenir le mois
				Month mois = new Month(CsvDAO.DF.parse(reader.get(colMois)));

				// Faut-il supprimer les valeurs de cette ligne ?
				if (from != null) {
					if (!from.after(mois)) {
						// Ignorer cette ligne et passer à la suivante
						continue;
					}
				}

				// Tableaux d'anciennes et nouvelles valeurs
				String[] oldValues = reader.getValues();
				// +1 pour avoir une colonne pour la période
				String[] newValues = new String[newcols.keySet().size() + 1];

				// Retranscrire le mois tel quel
				newValues[newcolMois] = reader.get(colMois);

				// Parcourir les id des anciennes colonnes
				for (Integer id : oldcols.keySet()) {

					// Insérer l'ancienne valeur dans la nouvelle colonne
					newValues[newcols.get(id)] = oldValues[oldcols.get(id)];
				}

				// Transcrire
				writer.writeRecord(newValues);

			}// while

			// Traiter les nouvelles données

			// Pour chaque mois
			for (Month mois : toSet.keySet()) {

				// Créer un nouvel enregistrement
				String[] values = new String[newcols.size() + 1];

				// Insérer le mois dans la (nouvelle) colonne
				values[newcolMois] = CsvDAO.DF.format(mois.getFirstDay());

				// Map des montants à insérer
				Map<Integer, BigDecimal> submap = toSet.get(mois);

				// Parcourir les comptes à définir sur ce mois
				for (Integer id : submap.keySet()) {

					// Convertir en texte avec séparateur décimal en virgule
					String txtValeur = submap.get(id).toString()
							.replace('.', ',');

					// Insérer son montant en texte dans la (nouvelle) colonne
					values[newcols.get(id)] = txtValeur;
				}

				// Ecrire la ligne
				writer.writeRecord(values);

			}// for multimap

			content = out.toCharArray();				// Récupérer le contenu
			
		} catch (ParseException e) {
			MessagesFactory.getInstance().showErrorMessage(
					"Mois illisible dans le contenu CSV.");
			throw new IOException();

		} catch (IOException e) {
			MessagesFactory.getInstance().showErrorMessage(
					"Impossible de modifier le contenu CSV");
			throw e;

		} finally {
			// Fermer les ressources
			CsvDAO.close(reader, writer);
		}// try
	}// save

	@Override
	/** Supprime un compte du fichier de suivi.
	 * @param id	L'identifiant du compte à supprimer
	 * @param file	Le fichier à modifier.
	 * @throws IOException 
	 */
	public void removeSuiviCompte(int id) throws IOException {

		int colCompte;

		// Ouvrir un Reader et un Writer
		CsvReader reader = getReader();
		CharArrayWriter out = new CharArrayWriter();
		CsvWriter writer = CsvDAO.getWriter(out);

		try {
			// Index de la colonne
			colCompte = reader.getIndex("" + id);

			// À partir des en-têtes, parcourir le fichier
			List<String> ligne;
			boolean headers = true; // S'occuper des en-têtes en premier
			do {
				// Récupérer en liste
				ligne = new ArrayList<String>();
				ligne.addAll(
						Arrays.asList(headers ? reader.getHeaders()	// En-têtes
						: reader.getValues()));			// Ou corps de fichier
				headers = false;

				// Supprimer la colonne
				ligne.remove(colCompte);

				// Retranscrire dans le writer par un tableau de String
				writer.writeRecord(ligne.toArray(new String[0]));

			} while (reader.readRecord());

			content = out.toCharArray();			// Récupérer le contenu
			
		} catch (IOException e) {
			MessagesFactory.getInstance().showErrorMessage(
					"Impossible de modifier le contenu CSV");
			throw e;

		} finally {
			// Fermer les ressources
			CsvDAO.close(reader, writer);
		}// try
	}// removeSuiviCompte
}
