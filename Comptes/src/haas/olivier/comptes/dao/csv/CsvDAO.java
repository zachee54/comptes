package haas.olivier.comptes.dao.csv;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import javax.xml.stream.FactoryConfigurationError;
import com.csvreader.CsvReader;
import com.csvreader.CsvWriter;

import haas.olivier.comptes.Banque;
import haas.olivier.comptes.Compte;
import haas.olivier.comptes.Ecriture;
import haas.olivier.comptes.Permanent;
import haas.olivier.comptes.dao.BanqueDAO;
import haas.olivier.comptes.dao.CompteDAO;
import haas.olivier.comptes.dao.DAOFactory;
import haas.olivier.comptes.dao.EcritureDAO;
import haas.olivier.comptes.dao.PermanentDAO;
import haas.olivier.comptes.dao.PropertiesDAO;
import haas.olivier.comptes.dao.SuiviDAO;
import haas.olivier.comptes.dao.cache.CacheableDAOFactory;
import haas.olivier.comptes.dao.cache.CacheablePropertiesDAO;
import haas.olivier.comptes.dao.cache.CachePermanentDAO;
import haas.olivier.comptes.dao.cache.CacheSuiviDAO;
import haas.olivier.comptes.dao.cache.WriteOnlyCacheableDAOFactory;
import haas.olivier.comptes.dao.xml.JaxbBanqueDAO;
import haas.olivier.comptes.dao.xml.JaxbPermanentDAO;
import haas.olivier.comptes.dao.xml.JaxbPropertiesDAO;
import haas.olivier.util.Month;

/** Une couche lisant des données au format CSV en vue de leur mise en cache, et
 * les enregistrant dans une archive ZIP.<br>
 * Par exception, les banques et les opérations permanentes sont enregistrées en
 * XML dans l'archive ZIP.
 *
 * @author Olivier HAAS
 */
public class CsvDAO implements CacheableDAOFactory {

	/** Le Logger de cette classe. */
	private static final Logger LOGGER =
			Logger.getLogger(CsvDAO.class.getName());
	
	/** Séparateur de champs. */
	private static final char DELIMITER = ',';
	
	/** Encodage de caractères. */
	private static final Charset CHARSET = Charset.forName("UTF-8");
	
	/** Nom du fichier zippé contenant les données des banques. */
	private static final String BANQUES = "banques.xml";
	
	/** Nom du fichier zippé contenant les données des comptes. */
	private static final String COMPTES = "comptes.csv";
	
	/** Nom du fichier zippé contenant les données des écritures. */
	private static final String ECRITURES = "ecritures.csv";
	
	/** Nom du fichier zippé contenant les données des opérations permanentes.*/
	private static final String PERMANENTS = "permanents.xml";
	
	/** Nom du fichier zippé contenant l'historique des soldes. */
	private static final String HISTORIQUE = "historique.csv";
	
	/** Nom du fichier zippé contenant les soldes à vue. */
	private static final String SOLDES = "soldes.csv";
	
	/** Nom du fichier zippé contenant les moyennes glissantes. */
	private static final String MOYENNES = "moyennes.csv";
	
	/** Nom du fichier zippé contenant les propriétés du modèle. */
	private static final String PROPRIETES = "properties.xml";

	/** Existence d'une <code>ParseException</code> sur la date (pour éviter les
	 * redondances).
	 */
	private static boolean dateParseException = false;

	/** <code>DateFormat</code> utilisable par toutes les classes du paquet.
	 * <br>Permet une implémentation uniforme des dates, lisible par un tableur.
	 */
	static final DateFormat DF = new SimpleDateFormat("dd/MM/yy");

	/** Parse une date
	 * 
	 * @throws IOException
	 */
	static Date parseDate(String textDate) throws IOException {
		try {
			return DF.parse(textDate);				// Renvoyer la date
			
		} catch (ParseException e) {				// Erreur non bloquante
			if (!dateParseException) {

				// 1ère occurrence: afficher le message
				Logger.getLogger(CsvDAO.class.getName()).log(
						Level.SEVERE, "Format de date illisible", e);

				// Noter pour ne pas afficher de message la prochaine fois
				dateParseException = true;
			}// if
			
			// Occurrences suivantes : faire passer pour une exception banale
			throw new IOException(e);
		}// try
	}// parseDate

	/** Convertit une chaîne en <code>BigDecimal</code>. Au préalable, les
	 * virgules sont converties en points pour être interprétées comme un
	 * séparateur décimal.
	 * 
	 * @param s	Une chaîne.
	 * 
	 * @return	Le montant correspondant.
	 */
	static BigDecimal parseAmount(String s) {
		return new BigDecimal(s.replace(',', '.'));
	}// parseAmount

	/** Crée une nouvelle source de données au format CSV.
	 * <p>
	 * Si <code>file</code> ne correspond pas à un fichier ZIP existant, la
	 * source de données créée est en écriture seule. Les méthodes de lecture
	 * peuvent être appelées mais ne renverront aucun élément.
	 * <p>
	 * Cette méthode garantit que les méthodes de lecture ne seront jamais
	 * appelées si le fichier n'existe pas ou n'est pas un fichier ZIP valide.
	 * 
	 * @param file	Le fichier à utiliser pour lire ou sauvegarder les données.
	 * 
	 * @return		Un <code>CsvDAO</code> utilisant les données de
	 * 				<code>file</code> si celui-ci existe, sinon un
	 * 				<code>WriteOnlyCacheableDAOFactory</code> qui sauvegardera
	 * 				les données dans un nouveau fichier <code>file</code>.
	 * 
	 * @throws ZipException
	 * @throws IOException
	 */
	public static CacheableDAOFactory newInstance(File file)
			throws ZipException, IOException {
		
		// La nouvelle instance
		CsvDAO csvDAO = new CsvDAO(file);
		
		// Si file n'est pas un ZIP valide, mettre la source en écriture seule
		return csvDAO.zip == null
				? new WriteOnlyCacheableDAOFactory(csvDAO)
				: csvDAO;
	}// newInstance

	/** L'objet ZIP contenant les données. */
	private final ZipFile zip;
	
	/** Le fichier au format ZIP dans lequel lire et écrire les données. */
	private final File file;
	
	/** Construit un objet d'accès aux données utilisant des données CSV et XML
	 * dans un fichier ZIP.
	 * 
	 * @param file	Le fichier ZIP contenant les données. L'objet ne doit pas
	 * 				être <code>null</code> mais peut ne pas correspondre à un
	 * 				fichier existant.
	 * 
	 * @throws IOException
	 * 				En cas d'erreur de lecture.
	 * @throws ZipException
	 * 				En cas d'erreur relative au format ZIP.
	 */
	private CsvDAO(File file) throws ZipException, IOException {
		this.file = file;
		zip = file.exists() ? new ZipFile(file) : null;
	}// constructeur
	
	/** Renvoie un lecteur CSV à partir d'un fichier contenu dans l'archive ZIP.
	 * 
	 * @param entryName	Le nom du fichier à lire dans l'archive ZIP.
	 * 
	 * @return			Un lecteur CSV.
	 * 
	 * @throws IOException
	 */
	private CsvReader getReader(String entryName) throws IOException {
		return new CsvReader(getZipInputStream(entryName), DELIMITER, CHARSET);
	}// getReader
	
	/** Renvoie un flux de lecture de l'entrée ZIP spécifiée.
	 * 
	 * @param entryName	L'entrée ZIP à lire.
	 * 
	 * @return			Un flux de lecture.
	 * 
	 * @throws IOException
	 */
	private InputStream getZipInputStream(String entryName) throws IOException {
		return zip.getInputStream(zip.getEntry(entryName));
	}// getZipInputStream
	
	@Override
	public Iterator<Banque> getBanques() throws IOException {
		return new JaxbBanqueDAO(getZipInputStream(BANQUES));
	}// getBanques

	@Override
	public Iterator<Compte> getComptes() throws IOException {
		return new CsvCompteDAO(getReader(COMPTES));
	}// getComptes

	@Override
	public Iterator<Ecriture> getEcritures(CompteDAO cDAO) throws IOException {
		return new CsvEcritureDAO(getReader(ECRITURES), cDAO);
	}// getEcritures

	@Override
	public Iterator<Permanent> getPermanents(CachePermanentDAO cache,
			CompteDAO cDAO) throws IOException {
		return new JaxbPermanentDAO(getZipInputStream(PERMANENTS), cache, cDAO);
	}// getPermanents

	@Override
	public Iterator<Entry<Month, Entry<Compte, BigDecimal>>> getHistorique()
			throws IOException {
		return getSuivi(HISTORIQUE);
	}

	@Override
	public Iterator<Entry<Month, Entry<Compte, BigDecimal>>> getSoldesAVue()
			throws IOException {
		return getSuivi(SOLDES);
	}

	@Override
	public Iterator<Entry<Month, Entry<Compte, BigDecimal>>> getMoyennes()
			throws IOException {
		return getSuivi(MOYENNES);
	}
	
	/**
	 * Renvoie un itérateur sur un fichier de suivi CSV contenu dans l'archive
	 * ZIP.
	 * 
	 * @param entryName	Le nom de l'entrée ZIP à utiliser.
	 * 
	 * @throws IOException
	 */
	private Iterator<Entry<Month, Entry<Compte, BigDecimal>>> getSuivi(
			String entryName) throws IOException {
		return  new CsvSuiviDAO(getReader(entryName));
	}
	
	@Override
	public CacheablePropertiesDAO getProperties()
			throws IOException {
		return new JaxbPropertiesDAO(getZipInputStream(PROPRIETES));
		
//		// Si l'archive ZIP contient un fichier propriétés, charger ses valeurs
//		ZipEntry zipEntry = zip.getEntry(PROPRIETES);
//		if (zipEntry != null)
//			return new JaxbPropertiesDAO(zip.getInputStream(zipEntry));
//			
//		// Sinon, retourner des propriétés vides
//		CacheableDAOFactory emptyDAO = null;
//		try {
//			emptyDAO = new EmptyCacheableDAOFactory();
//			return emptyDAO.getProperties();
//		} finally {
//			if (emptyDAO != null) emptyDAO.close();
//		}// try
	}// getProperties

	@Override
	public void save(DAOFactory factory) throws IOException {
		
		// Créer un flux d'écriture ZIP vers un fichier temporaire
		File tmp = File.createTempFile(
				"comptes-", ".tmp", file.getParentFile());
		ZipOutputStream zipOut = null;
		try {
			zipOut = new ZipOutputStream(			// Flux d'écriture du ZIP
					new FileOutputStream(tmp), CHARSET);
			CsvWriter csvOut =						// Flux d'écriture CSV
					new CsvWriter(zipOut, DELIMITER, CHARSET);

			// Écrire les banques
			saveBanques(zipOut, factory.getBanqueDAO());
			
			// Écrire les comptes
			Map<Integer, Compte> comptesById =
					createIds(factory.getCompteDAO().getAll());
			saveComptes(zipOut, csvOut, comptesById);
			
			// Écrire les écritures
			saveEcritures(zipOut, csvOut, factory.getEcritureDAO());
			
			// Écrire les écritures permanentes
			savePermanents(zipOut, factory.getPermanentDAO());
			
			// Écrire les trois types de suivis
			saveSuivis(factory.getHistoriqueDAO(), HISTORIQUE, zipOut, csvOut);
			saveSuivis(factory.getSoldeAVueDAO(), SOLDES, zipOut, csvOut);
			saveSuivis(factory.getMoyenneDAO(), MOYENNES, zipOut, csvOut);
			
			// Écrire les propriétés
			saveProperties(zipOut, factory.getPropertiesDAO());
			
		} catch (FactoryConfigurationError e) {
			throw new IOException("Erreur d'écriture XML", e);
			
		} finally {
			if (zipOut != null) zipOut.close();		// Fermer les ressources
		}// try

		// Remplacer l'ancien fichier
		File bak = null;
		if (file.exists()) {

			// Sauvegarde temporaire du fichier initial
			bak = new File(
					file.getParentFile(),		// Même répertoire
					file.getName() + ".bak");	// Extension ".bak"

			// Supprimer l'ancienne sauvegarde si elle existe
			if (bak.exists() && !bak.delete()) {
				throw new IOException(
						"Impossible de supprimer le fichier "
								+ bak.getAbsolutePath());
			}// if

			// Renommer le fichier initial en sauvegarde
			file.renameTo(bak);
		}// if

		// Renommer le fichier temporaire en fichier définitif
		Files.move(Paths.get(tmp.getAbsolutePath()),
				Paths.get(file.getAbsolutePath()));

		// Effacer la sauvegarde devenue inutile
		if (bak != null)
			bak.delete();
	}// save

	/** Écrit un fichier XML des banques dans l'archive ZIP.
	 *  
	 * @param zipOut	Le flux d'écriture vers l'archive ZIP.
	 * @param bDAO		L'objet d'accès aux banques.
	 * 
	 * @throws IOException
	 */
	private void saveBanques(ZipOutputStream zipOut, BanqueDAO bDAO)
			throws IOException {

		// Écrire les données des banques
		zipOut.putNextEntry(new ZipEntry(BANQUES));
		JaxbBanqueDAO.save(bDAO.getAll().iterator(), zipOut);
		zipOut.closeEntry();

		// Écrire le schéma XSD
		saveSchema(JaxbBanqueDAO.class, "banques.xsd", zipOut);
	}// saveBanques

	/**
	 * Écrit un fichier CSV des comptes dans l'archive ZIP.
	 * 
	 * @param zipOut		Le flux d'écriture vers l'archive ZIP.
	 * @param csvOut		Le flux d'écriture CSV.
	 * @param comptesById	Les comptes classés par identifiants.
	 * 
	 * @throws IOException
	 */
	private void saveComptes(ZipOutputStream zipOut, CsvWriter csvOut,
			Map<Integer, Compte> comptesById) throws IOException {
		zipOut.putNextEntry(new ZipEntry(COMPTES));
		CsvCompteDAO.save(comptesById, csvOut);
		csvOut.flush();
		zipOut.closeEntry();
	}
	
	/**
	 * Affecte un identifiant unique à chaque objet.
	 * 
	 * @param objects	La collection des objets à numéroter.
	 * 
	 * @return			Tous les objets, classés en fonction de leur identifiant
	 * 					créé.
	 * 
	 * @throws IOException
	 */
	private <T> Map<Integer, T> createIds(Iterable<T> objects)
			throws IOException {
		Map<Integer, T> objectsById = new HashMap<>();
		int id = 1;
		for (T t : objects)
			objectsById.put(id++, t);
		return objectsById;
	}
	
	/** Écrit un fichier CSV des écritures dans l'archive ZIP.
	 * 
	 * @param zipOut	Le flux d'écriture vers l'archive ZIP.
	 * @param csvOut	Le flux d'écriture CSV.
	 * @param eDAO		L'objet d'accès aux écritures.
	 * 
	 * @throws IOException
	 */
	private void saveEcritures(ZipOutputStream zipOut, CsvWriter csvOut,
			EcritureDAO eDAO) throws IOException {
		zipOut.putNextEntry(new ZipEntry(ECRITURES));
		CsvEcritureDAO.save(eDAO.getAll().iterator(), csvOut);
		csvOut.flush();
		zipOut.closeEntry();
	}// saveEcritures
	
	/** Écrit un fichier XML des opérations permanentes dans l'archive ZIP.
	 * 
	 * @param zipOut	Le flux d'écriture de l'archive ZIP.
	 * @param pDAO		Les opérations permanentes.
	 * 
	 * @throws IOException
	 */
	private void savePermanents(ZipOutputStream zipOut, PermanentDAO pDAO)
			throws IOException {
		
		// Écrire les données des opérations permanentes
		zipOut.putNextEntry(new ZipEntry(PERMANENTS));
		JaxbPermanentDAO.save(pDAO.getAll().iterator(), zipOut);
		zipOut.closeEntry();
		
		// Enregistrer aussi le schéma XSD
		saveSchema(JaxbPermanentDAO.class, "permanents.xsd", zipOut);
	}// savePermanents

	/** Écrit un fichier XML des propriétés dans l'archive ZIP.
	 *  
	 * @param zipOut	Le flux d'écriture vers l'archive ZIP.
	 * @param pDAO		L'objet d'accès aux propriétés.
	 * 
	 * @throws IOException
	 */
	private void saveProperties(ZipOutputStream zipOut, PropertiesDAO propsDAO)
			throws IOException {

		// Écrire les données des banques
		zipOut.putNextEntry(new ZipEntry(PROPRIETES));
		JaxbPropertiesDAO.save(propsDAO, zipOut);
		zipOut.closeEntry();

		// Écrire le schéma XSD
		saveSchema(JaxbPropertiesDAO.class, "properties.xsd", zipOut);
	}// saveBanques
	
	/** Sauvegarde un fichier dans l'archive ZIP.
	 * <p>
	 * Cette méthode est utilisée pour intégrer les schémas XSD dans l'archive.
	 * 
	 * @param classe	La classe à partir de laquelle rechercher la ressource.
	 * 					C'est le <code>ClassLoader</code> de cette classe qui
	 * 					sera utilisé pour trouver le fichier.
	 * 
	 * @param fileName	Le nom du fichier à sauvegarder. Ce fichier doit se
	 * 					trouver dans le même répertoire que la classe actuelle.
	 * 
	 * @param zipOut	Le flux d'écriture vers l'archive ZIP.
	 * 
	 * @throws IOException
	 */
	private void saveSchema(Class<?> classe, String fileName,
			ZipOutputStream zipOut) throws IOException {
		
		// Écrire le schéma XML des opérations permanentes
		zipOut.putNextEntry(						// Créer l'entrée ZIP
				new ZipEntry(fileName));
		InputStream schemaIn = null;				// Flux de lecture du schéma
		try {
			schemaIn = new BufferedInputStream(		// Ouvrir flux avec buffer
					classe.getResourceAsStream(fileName));
			int b;
			while ((b = schemaIn.read()) != -1)		// Copier le contenu
				zipOut.write(b);					// vers le fichier ZIP
			
		} catch (IOException e) {
			LOGGER.log(Level.WARNING,
					"Le schéma XML " + fileName + " n'a pas pu " +
					"être intégré au fichier enregistré.\n" +
					"Cette erreur n'est pas bloquante", e);
			
		} finally {
			if (schemaIn != null)
				schemaIn.close();
		}// try
		zipOut.closeEntry();						// Fermer l'entrée ZIP
	}// saveSchema
	
	/** Sauvegarde un type de suivis.
	 * 
	 * @param suivis	L'objet d'accès aux suivis à sauvegarder.
	 * @param entryName	Le nom de l'entrée ZIP dans laquelle sauvegarder
	 * 					<code>suivis</code>.
	 * @param zipOut	Un flux d'écriture ZIP.
	 * @param writer	Un flux CSV écrivant vers <code>zipOut</code>.
	 * @throws IOException 
	 */
	private void saveSuivis(CacheSuiviDAO suivis, String entryName,
			ZipOutputStream zipOut, CsvWriter csvOut)
					throws IOException {
		zipOut.putNextEntry(new ZipEntry(entryName));
		CsvSuiviDAO.save(suivis, csvOut);
		csvOut.flush();
		zipOut.closeEntry();
	}// saveSuivis
	
	@Override
	public String getName() {
		return "CSV";
	}// getName

	@Override
	public String getSource() {
		return file.getName();
	}// getSource

	@Override
	public String getSourceFullName() {
		return file.getAbsolutePath();
	}// getSourceFullName
	
	/** @returns	<code>true</code> */
	@Override
	public boolean canBeSaved() {
		return true;
	}// canBeSaved

	/** Aucune implémentation. */
	@Override
	public void close() throws IOException {
	}// close

}
