package haas.olivier.comptes.dao.csv;

import java.io.IOException;
import java.text.ParseException;
import java.util.Iterator;
import java.util.zip.ZipFile;

import javax.imageio.ImageIO;

import com.csvreader.CsvReader;
import com.csvreader.CsvWriter;

import haas.olivier.comptes.Banque;


/** Un objet d'accès aux banques, au format CSV.
 * 
 * @author Olivier HAAS
 */
class CsvBanqueDAO extends AbstractCsvLayer<Banque> {

	/** Noms des champs CSV. */
	private static final String HEADER_ID = "id", HEADER_NOM = "nom", 
			HEADER_ICON = "icone";
	
	/** Sauvegarde les éléments.
	 * 
	 * @param elements	Un itérateur des banques à sauvegarder.
	 * @param writer	Un flux CSV.
	 * 
	 * @throws IOException
	 */
	static void save(Iterator<Banque> elements, CsvWriter writer)
			throws IOException {
		
		// Contenu-type des en-têtes
		String[] headers = {HEADER_ID, HEADER_NOM, HEADER_ICON};
		
		// Écrire la ligne d'en-tête
		writer.writeRecord(headers);
		
		// Écrire chaque banque
		while (elements.hasNext()) {			// Pour chaque banque
			Banque banque = elements.next();	// La banque à écrire
			String[] values =					// Créer une ligne vide
					new String[headers.length];
			
			// Remplir la ligne
			for (int i=0; i<values.length; i++) {
				switch (headers[i]) {
				case HEADER_ID : values[i] = banque.id.toString();
				case HEADER_NOM :values[i] = banque.nom;
				case HEADER_ICON:values[i] = banque.fileName;
				default:
				}// switch header
			}// for colonne
		}// while element
	}// save

	/** Le fichier ZIP dans lequel lire les images. */
	private final ZipFile zip;
	
	/** Construit un objet d'accès aux banques, au format CSV.
	 * 
	 * @param reader	Le lecteur CSV à utiliser.
	 * @param zip		Le fichier ZIP dans lequel se trouvent les fichiers
	 * 					images.
	 * 
	 * @throws IOException
	 */
	CsvBanqueDAO(CsvReader reader, ZipFile zip) throws IOException {
		super(reader);
		this.zip = zip;
	}// constructeur

	@Override
	protected Banque readNext(CsvReader reader) throws NumberFormatException,
			ParseException, IOException {
		
		// Nom du fichier image à l'intérieur du ZIP
		String fileName = reader.get(HEADER_ICON);
		
		// Instancier une banque
		return new Banque(
				Integer.parseInt(reader.get(HEADER_ID)),	// Identifiant
				reader.get(HEADER_NOM),						// Nom de la banque
				ImageIO.read(								// Image enregistrée
						zip.getInputStream(zip.getEntry(fileName))),
				fileName);									// Nom d'entrée ZIP
	}// readNext
}
