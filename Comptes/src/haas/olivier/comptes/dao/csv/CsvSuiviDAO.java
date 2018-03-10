package haas.olivier.comptes.dao.csv;

import haas.olivier.comptes.Compte;
import haas.olivier.comptes.dao.cache.CacheSuiviDAO;
import haas.olivier.comptes.dao.cache.Solde;
import haas.olivier.util.Month;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.ParseException;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedSet;
import java.util.TreeSet;

import com.csvreader.CsvReader;
import com.csvreader.CsvWriter;

/**
 * Un objet d'accès aux suivis des comptes, au format CSV.
 * 
 * @author Olivier HAAS
 */
class CsvSuiviDAO extends AbstractCsvLayer<Solde> {

	/**
	 * Nom de l'en-tête de la colonne contenant les mois.
	 */
	private static final String MONTH_HEADER = "mois";
	
	/**
	 * Sauvegarde les éléments de suivi.
	 * 
	 * @param cache			Le cache des suivis.
	 * 
	 * @param comptesById	Les comptes, classés en fonction de leurs
	 * 						identifiants.
	 * 
	 * @param writer		Le flux d'écriture CSV dans lequel sauvegarder les
	 * 						données de suivi.
	 * 
	 * @throws IOException
	 */
	static void save(CacheSuiviDAO cache, Map<Integer, Compte> comptesById,
			CsvWriter writer) throws IOException {
		
		// Fixer l'ordre des identifiants
		Integer[] ids =
				comptesById.keySet().toArray(new Integer[comptesById.size()]);
		
		// Écrire les en-têtes
		writeHeaders(ids, writer);
		
		// Écrire les soldes
		for (Month month : cache.getMonths()) {
			String[] values = new String[ids.length];
			
			// Écrire le mois
			values[0] = CsvDAO.DF.format(month.getFirstDay());
			
			// Écrire les soldes
			int i = 1;
			for (Integer id : ids) {
				Compte compte = comptesById.get(id);
				values[i++] = cache.get(compte, month).toPlainString();
			}
			
			// Écrire la ligne
			writer.writeRecord(values);
		}
	}
	
	/**
	 * Écrit la ligne d'en-têtes, contenant une cellule qui constitue l'en-tête
	 * des mois, et tous les identifiants de comptes.
	 * 
	 * @param ids		Les identifiants des comptes, dans l'ordre retenu.
	 * @param writer	Le flux d'écriture CSV.
	 * 
	 * @throws IOException
	 */
	private static void writeHeaders(Integer[] ids, CsvWriter writer)
			throws IOException {
		String[] headers =							// Autant que de comptes + 1
				new String[ids.length + 1];
		headers[0] = MONTH_HEADER;					// Colonne du mois
		int n = 1;
		for (Integer id : ids)
			headers[n++] = id.toString();
		writer.writeRecord(headers);
	}
	
	/**
	 * Les en-têtes du fichier.
	 */
	private final String[] headers;
	
	/**
	 * L'index de la colonne contenant le mois, les autres colonnes contenant
	 * les données propres à chaque compte.
	 */
	private final int colMois;
	
	/**
	 * Le mois en cours de lecture.
	 */
	private Month mois;
	
	/**
	 * L'index de la dernière valeur lue dans la ligne en cours.
	 */
	private int col = -1;
	
	/**
	 * Construit un objet d'accès aux suivis des comptes, au format CSV.
	 * 
	 * @param reader	Le lecteur CSV à utiliser.
	 * 
	 * @throws IOException
	 */
	public CsvSuiviDAO(CsvReader reader) throws IOException {
		super(reader);
		
		// Mémoriser les en-têtes
		headers = reader.getHeaders();
		
		// Déterminer l'index de la colonne contenant le mois
		colMois = reader.getIndex(MONTH_HEADER);
	}
	
	@Override
	public boolean hasNext() {
		
		// Vérifier s'il n'y a pas déjà une valeur prête à être renvoyée
		if (next != null)
			return true;
		
		// S'il n'y a rien de prêt, chercher le prochain élément
		try {
			// Essayer de le lire et dire si on l'a trouvé
			return (next = readNext(reader)) != null;
			
		} catch (Exception e) {					// Erreur quelconque
			mois = null;						// Oublier le mois
			return super.hasNext();				// Laisser la main à classe mère
		}
	}

	/**
	 * Contrairement aux spécifications de la classe mère, cette méthode lit la
	 * valeur suivante <b>dans la même ligne</b> ou, s'il n'y a plus rien à
	 * lire, au début de la prochaine ligne contenant une valeur.
	 */
	protected Solde readNext(CsvReader reader)
			throws NumberFormatException, ParseException, IOException {
		
		// Trouver la prochaine colonne non vide dans cette ligne
		String text = null;						// Texte lu
		while (++col == colMois					// Éviter la colonne du mois
				|| (text = reader.get(col)).isEmpty()) {// et valeurs vides
			
			// Si la ligne est finie
			if (col >= reader.getColumnCount()) {
				
				// S'il n'y a plus de ligne à lire, on n'a rien à renvoyer
				if (!reader.readRecord())
					return null;
				
				// Sinon, on continue la boucle avec la ligne suivante
				mois = null;					// Oublier le mois en cours
				col = -1;						// Réinitialiser index colonne
			}
		}
		
		// Si le mois est inconnu (début de ligne), on le lit
		if (mois == null)
			mois = new Month(CsvDAO.DF.parse(reader.get(colMois)));
		
		// Renvoyer le solde trouvé, avec son mois et le compte correspondants
		return new Solde(mois, Integer.parseInt(headers[col]),
						CsvDAO.parseAmount(text));
	}
}
