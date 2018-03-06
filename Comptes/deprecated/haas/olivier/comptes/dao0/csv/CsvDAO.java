package haas.olivier.comptes.dao0.csv;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.CharArrayReader;
import java.io.CharArrayWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import com.csvreader.CsvReader;
import com.csvreader.CsvWriter;

import haas.olivier.comptes.MessagesFactory;
import haas.olivier.comptes.dao.buffer.BufferableCompteDAO;
import haas.olivier.comptes.dao.buffer.BufferableDAOFactory;
import haas.olivier.comptes.dao.buffer.BufferableEcritureDAO;
import haas.olivier.comptes.dao.buffer.BufferablePermanentDAO;
import haas.olivier.comptes.dao.buffer.BufferableSuiviDAO;

/**
 * DAOFactory qui traite le format CSV.
 * 
 * @author Olivier Haas
 */
// TODO réécrire tout ça !
public class CsvDAO implements BufferableDAOFactory {

	// Constantes pour les fichiers CSV
	private static final char DELIMITER = ',';
	private static final Charset CHARSET = Charset.forName("UTF-8");

	/**
	 * DateFormat utilisable par toutes les classes du paquet. Permet une
	 * implémentation uniforme des dates, lisible par un tableur.
	 */
	public static final DateFormat DF = new SimpleDateFormat("dd/MM/yy");

	/** Existence d'une ParseException sur la date (pour éviter les redondances)
	 */
	private static boolean dateParseException = false;

	/**
	 * Ouvre une ressource en lecture sur un contenu CSV. Lit les en-têtes mais
	 * sans les interpréter.
	 * 
	 * @param csv			Le contenu à lire
	 * @return 				Un CsvReader avec en-têtes lues.
	 */
	public static CsvReader getReader(char[] content) {
		CsvReader reader = null;
		reader = new CsvReader(					// Un CsvReader sur le contenu
				new CharArrayReader(content), DELIMITER);
		try {
			reader.readHeaders();				// Lire en-têtes
		} catch (IOException e) {
			e.printStackTrace();
		}
		return reader;							// Renvoyer le CsvReader
	}// getReader

	/**
	 * Crée un CsvWriter permettant d'écrire un contenu CSV dans un flux de
	 * caractères.
	 * L'intérêt de cette méthode est uniquement de centraliser le choix du
	 * délimiteur utilisé dans le contenu CSV.
	 */
	public static CsvWriter getWriter(Writer out) {
		return new CsvWriter(out, DELIMITER);
	}

	/**
	 * Ferme un CsvWriter. Le fichier original spécifié est remplacé par le
	 * nouveau fichier.
	 * 
	 * @param reader	Le CsvReader à fermer
	 * @param writer	Le CsvWriter à fermer
	 */
	public static void close(CsvReader reader, CsvWriter writer) {

		// Fermer le Reader
		if (reader != null) {
			reader.close();
		}

		// Fermer le Writer
		if (writer != null) {
			writer.close();
		}
	}// closeWriter

	/**
	 * Convertit une chaîne en BigDecimal. Au préalable, les virgules sont
	 * converties en points pour être interprétées comme un séparateur décimal.
	 * 
	 * @param s
	 *            Une chaîne
	 * @return Le montant correspondant
	 */
	public static BigDecimal parseAmount(String s) {
		return new BigDecimal(s.replace(',', '.'));
	}

	/**
	 * Parse une date
	 * 
	 * @throws IOException
	 */
	public static Date parseDate(String textDate) throws IOException {
		try {
			return DF.parse(textDate);				// Renvoyer la date
		} catch (ParseException e) {				// Erreur non bloquante
			if (!dateParseException) {

				// 1ère occurrence: afficher le message
				MessagesFactory.getInstance().showErrorMessage(
						"Format de date illisible");
				e.printStackTrace();

				// Noter pour ne pas afficher de message la prochaine fois
				dateParseException = true;
			}
			throw new IOException();
		}// try
	}// parseDate

	/** Les DAO. */
	private CsvCompteDAO compteDAO;
	private CsvEcritureDAO ecritureDAO;
	private CsvPermanentDAO permanentDAO;
	private CsvSuiviDAO historiqueDAO;
	private CsvSuiviDAO soldesDAO;
	private CsvSuiviDAO moyenneDAO;
	
	/** Propriétés des diagrammes. */
	private Properties diagramProperties = new Properties();
	
	/** Fichier principal. */
	private File file;

	/** Décompresse l'archive ZIP et renvoie l'entrée spécifiée.
	 * 
	 * @param entry	Le nom de l'entrée à lire.
	 * @return		Un tableau de caractères correspondant au contenu du
	 * 				fichier décompressé, ou null en cas d'erreur (notamment si
	 * 				l'archive ZIP ou l'entrée n'existe pas).
	 */
	private char[] load(String entry) {
		if (!file.exists()) {							// Pas de fichier
			return null;								// Rien
		}
		
		ZipFile zfile = null;							// Archive compressée
		CharArrayWriter out = null;						// Flux mémoire
		try {
			// Définir les flux
			zfile = new ZipFile(file);					// Fichier archive
			Reader in = new InputStreamReader(			// Lecture de caractères
					zfile.getInputStream(zfile.getEntry(entry)), CHARSET);
			out = new CharArrayWriter();				// Flux vers la mémoire
			
			// Charger en mémoire
			int c;										// Caractère tampon
			while ((c = in.read()) != -1) {				// Pour chaque caractère
				out.write(c);							// Écrire en mémoire
			}
			return out.toCharArray();					// Renvoyer en tableau
		
		} catch (IOException e) {
			MessagesFactory.getInstance().showErrorMessage(
					"Impossible de lire l'entrée " + entry + " du fichier " + 
					file.getName());
			e.printStackTrace();
			
		} finally {
			if (zfile != null) {
				try {
					zfile.close();						// Fermer le fichier zip
				} catch (IOException e) {
				}
			}
			if (out != null) {
				out.close();							//Fermer le flux mémoire
			}
		}// try
		return null;									// En cas d'erreur
	}// load
	
	/**
	 * Construit un DAOFactory pour format CSV.
	 * 
	 * @param file
	 *            Le fichier principal, sachant que cette implémentation génère
	 *            plusieurs fichiers CSV dont le nom dérive de celui-ci
	 * @throws IOException
	 */
	public CsvDAO(File file) throws IOException {
		this.file = file;							// Fichier
		
		// Construire les DAO
		compteDAO = new CsvCompteDAO();
		ecritureDAO = new CsvEcritureDAO();
		permanentDAO = new CsvPermanentDAO();
		historiqueDAO = new CsvSuiviDAO();
		soldesDAO = new CsvSuiviDAO();
		moyenneDAO = new CsvSuiviDAO();
	}// constructeur

	@Override
	public void load() {
		
		// Charger les DAO
		compteDAO.setContent(load("comptes.csv"));
		ecritureDAO.setContent(load("ecritures.csv"));
		permanentDAO.setContent(load("permanents.csv"));
		historiqueDAO.setContent(load("historique.csv"));
		soldesDAO.setContent(load("soldes.csv"));
		moyenneDAO.setContent(load("moyennes.csv"));
		
		// Charger les propriétés des diagrammes
		ZipFile zFile = null;
		try {
			zFile = new ZipFile(file);
			ZipEntry entry = zFile.getEntry("diagrams.ini");
			if (entry != null)
				diagramProperties.load(zFile.getInputStream(entry));
			
		} catch (IOException e) {
			e.printStackTrace();
			
		} finally {
			if (zFile != null) {
				try {
					zFile.close();
				} catch (IOException e) {
				}// try
			}// if
		}// try
	}// load

	@Override
	public BufferableCompteDAO getCompteDAO() {
		return compteDAO;
	}

	@Override
	public BufferableEcritureDAO getEcritureDAO() {
		return ecritureDAO;
	}

	@Override
	public BufferablePermanentDAO getPermanentDAO() {
		return permanentDAO;
	}

	@Override
	public BufferableSuiviDAO getHistoriqueDAO() {
		return historiqueDAO;
	}

	@Override
	public BufferableSuiviDAO getSoldeAVueDAO() {
		return soldesDAO;
	}

	@Override
	public BufferableSuiviDAO getMoyenneDAO() {
		return moyenneDAO;
	}
	
	@Override
	public String getProperty(String prop) {
		return diagramProperties.getProperty(prop);
	}
	
	@Override
	public void setProperty(String prop, String value) {
		diagramProperties.setProperty(prop, value);
	}

	@Override
	public String getName() {
		return "CSV";
	}

	@Override
	public String getSource() {
		return file.getName();
	}

	@Override
	public String getSourceFullName() {
		return file.getAbsolutePath();
	}

	
	@Override
	/**
	 * Pas d'implémentation: la nécessité de sauvegarder est pilotée par une
	 * sur-couche DAO.
	 * 
	 * @return	false
	 */
	public boolean mustBeSaved() {
		return false;
	}
	
	/** Ajoute une entrée dans une archive ZIP.
	 * 
	 * @param zipOut	Le flux d'écriture vers une archive zip.
	 * @param charOut	Flux de caractères permettant d'écrire dans zipOut.
	 * @param name		Le nom de l'entrée à ajouter.
	 * @param content	Le contenu à compresser, sous forme de tableau de
	 * 					caractères.
	 * @throws IOException 
	 */
	private void addZipEntry(
			ZipOutputStream zipOut, Writer charOut, String name, char[] content)
					throws IOException {
		Reader in = null;
		try {
			in = new CharArrayReader(content);			// Lecture du contenu
			zipOut.putNextEntry(new ZipEntry(name));	// Créer l'entrée zip
			int c;										// Un caractère
			while ((c = in.read()) != -1) {				// Chaque caractère
				charOut.write(c);
			}
			charOut.flush();							// Vider le tampon
			zipOut.closeEntry();						// Fermer l'entrée ZIP
		} finally {
			if (in != null) {
				in.close();								// Fermer le flux in
			}
		}// try
	}// addZipEntry

	@Override
	public void save() throws IOException {
		Writer charOut = null;							// Flux d'archive ZIP
		File temp = new File(							// Fichier temporaire
				file.getAbsolutePath() + ".tmp");
		try {
			if (temp.exists()) {
				temp.delete();							// Effacer si existant
			}
			
			// Flux de sortie ZIP, avec tampon, vers le fichier temporaire
			ZipOutputStream zipOut = new ZipOutputStream(
					new BufferedOutputStream(			// avec buffer
							new FileOutputStream(temp)));//vers le fichier temp
			
			// Flux de conversion caractères/byte vers le flux ZIP 
			charOut = new BufferedWriter(
					new OutputStreamWriter(zipOut, CHARSET));
			
			// Sauvegarder chaque module l'un après l'autre
			addZipEntry(zipOut,charOut,"comptes.csv",compteDAO.content);
			addZipEntry(zipOut,charOut,"ecritures.csv",ecritureDAO.content);
			addZipEntry(zipOut,charOut,"permanents.csv",permanentDAO.content);
			addZipEntry(zipOut,charOut,"historique.csv",historiqueDAO.content);
			addZipEntry(zipOut,charOut,"soldes.csv",soldesDAO.content);
			addZipEntry(zipOut,charOut,"moyennes.csv",moyenneDAO.content);
			
			// Sauvegarder les propriétés des diagrammes
			zipOut.putNextEntry(new ZipEntry("diagrams.ini"));
			diagramProperties.store(zipOut, "Propriétés des diagrammes");
			
		} catch (IOException e) {
			MessagesFactory.getInstance().showErrorMessage(
					"Erreur de sauvegarde/compression: impossible " +
					"d'enregistrer les données.");
		} finally {
			if (charOut != null) {
				charOut.close();
			}
		}// try
		
		// Remplacer le fichier d'origine par le fichier temporaire
		if (!file.exists()						// Si le fichier n'existe pas...
				|| file.delete()) {				// ...ou est bien effacé
			if (!temp.renameTo(file)) {			// Renommer le fichier temp
				MessagesFactory.getInstance().showErrorMessage(
						"Impossible de renommer le fichier temporaire. Les " +
						"données ont été enregistrées dans le fichier :\n" +
								temp.getAbsolutePath());
			}// rename
		}// delete
	}// save

	@Override
	/** Aucune implémentation. */
	public void erase() {
	}
}
