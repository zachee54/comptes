package haas.olivier.comptes.dao.csv;

import haas.olivier.comptes.Compte;
import haas.olivier.comptes.dao.cache.CacheSuiviDAO;
import haas.olivier.comptes.dao.cache.Solde;
import haas.olivier.util.Month;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.Map;
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
	 * @param idByCompte	Les comptes, avec leurs identifiants.
	 * 
	 * @param writer		Le flux d'écriture CSV dans lequel sauvegarder les
	 * 						données de suivi.
	 * 
	 * @throws IOException
	 */
	static void save(CacheSuiviDAO cache, Map<Compte, Integer> idByCompte,
			CsvWriter writer) throws IOException {
		
		// Fixer l'ordre des identifiants
		Compte[] comptes =
				idByCompte.keySet().toArray(new Compte[idByCompte.size()]);
		
		// Écrire les en-têtes
		Integer[] ids = new Integer[comptes.length];
		for (int i=0; i<ids.length; i++)
			ids[i] = idByCompte.get(comptes[i]);
		writeHeaders(ids, writer);
		
		// Écrire les soldes
		DateFormat dateFormat = CsvDAO.createDateFormat();
		for (Month month : cache.getMonths()) {
			String[] values = new String[comptes.length];
			
			// Écrire le mois
			values[0] = dateFormat.format(month.getFirstDay());
			
			// Écrire les soldes
			int i = 1;
			for (Compte compte : comptes)
				values[i++] = cache.get(compte, month).toPlainString();
			
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
	 * Le format de date.
	 */
	private final DateFormat dateFormat = CsvDAO.createDateFormat();
	
	/**
	 * Les comptes, classés par identifiant.
	 */
	private final Map<Integer, Compte> comptesById;
	
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
	 * @param reader		Le lecteur CSV à utiliser.
	 * @param compteById	Les comptes, classés par identifiant.
	 * 
	 * @throws IOException
	 */
	public CsvSuiviDAO(CsvReader reader, Map<Integer, Compte> comptesById)
			throws IOException {
		super(reader);
		this.comptesById = comptesById;
		
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
			throws ParseException, IOException {

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
			mois = new Month(dateFormat.parse(reader.get(colMois)));
		
		// Renvoyer le solde trouvé, avec son mois et le compte correspondants
		return new Solde(
				mois,
				comptesById.get(Integer.parseInt(headers[col])),
				CsvDAO.parseAmount(text));
	}
}
