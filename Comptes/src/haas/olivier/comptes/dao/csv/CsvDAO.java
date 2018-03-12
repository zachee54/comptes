package haas.olivier.comptes.dao.csv;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
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
import haas.olivier.comptes.dao.EcritureDAO;
import haas.olivier.comptes.dao.PermanentDAO;
import haas.olivier.comptes.dao.PropertiesDAO;
import haas.olivier.comptes.dao.cache.CacheableDAOFactory;
import haas.olivier.comptes.dao.cache.CacheablePropertiesDAO;
import haas.olivier.comptes.dao.cache.Solde;
import haas.olivier.comptes.dao.cache.CacheDAOFactory;
import haas.olivier.comptes.dao.cache.CachePermanentDAO;
import haas.olivier.comptes.dao.cache.CacheSuiviDAO;
import haas.olivier.comptes.dao.cache.WriteOnlyCacheableDAOFactory;
import haas.olivier.comptes.dao.xml.JaxbBanqueDAO;
import haas.olivier.comptes.dao.xml.JaxbPermanentDAO;
import haas.olivier.comptes.dao.xml.JaxbPropertiesDAO;

/**
 * Une couche du modèle lisant et écrivant à la volée des données au format CSV
 * compressé.
 * <p>
 * Drrière ce principe général, certaines données sont traitées un peu
 * différemment :<ul>
 * <li> du point de vue du stockage, les banques et les opérations permanentes
 * 		sont enregistrées en XML dans l'archive ZIP, et non pas en CSV ;
 * <li>	du point de vue du processus de lecture, les comptes sont mis en cache
 * 		de façon interne pour permettre aux autres données d'y accéder par
 * 		identifiant.
 * </ul>
 *
 * @author Olivier HAAS
 */
public class CsvDAO implements CacheableDAOFactory {

	/**
	 * Le Logger de cette classe.
	 */
	private static final Logger LOGGER =
			Logger.getLogger(CsvDAO.class.getName());
	
	/**
	 * Séparateur de champs.
	 */
	private static final char DELIMITER = ',';
	
	/**
	 * Encodage de caractères.
	 */
	private static final Charset CHARSET = Charset.forName("UTF-8");
	
	/**
	 * Nom du fichier zippé contenant les données des banques.
	 */
	private static final String BANQUES = "banques.xml";
	
	/**
	 * Nom du fichier zippé contenant les données des comptes.
	 */
	private static final String COMPTES = "comptes.csv";
	
	/**
	 * Nom du fichier zippé contenant les données des écritures.
	 */
	private static final String ECRITURES = "ecritures.csv";
	
	/**
	 * Nom du fichier zippé contenant les données des opérations permanentes.
	 */
	private static final String PERMANENTS = "permanents.xml";
	
	/**
	 * Nom du fichier zippé contenant l'historique des soldes.
	 */
	private static final String HISTORIQUE = "historique.csv";
	
	/**
	 * Nom du fichier zippé contenant les soldes à vue.
	 */
	private static final String SOLDES = "soldes.csv";
	
	/**
	 * Nom du fichier zippé contenant les moyennes glissantes.
	 */
	private static final String MOYENNES = "moyennes.csv";
	
	/**
	 * Nom du fichier zippé contenant les propriétés du modèle.
	 */
	private static final String PROPRIETES = "properties.xml";

	/**
	 * Renvoie un format de date utilisable par toutes les classes du paquet.
	 * <p>
	 * Cette méthode utilitaire permet une implémentation uniforme des dates,
	 * lisible par un tableur.<br>
	 * Elle introduit une nouvelle instance pour chaque usage du fait que les
	 * formats de date ne sont pas thread-safe.
	 * 
	 * @return	Une nouvelle instance de format de date.
	 */
	static final DateFormat createDateFormat() {
		return new SimpleDateFormat("dd/MM/yy");
	}

	/**
	 * Convertit une chaîne en <code>BigDecimal</code>. Au préalable, les
	 * virgules sont converties en points pour être interprétées comme un
	 * séparateur décimal.
	 * 
	 * @param s	Une chaîne.
	 * 
	 * @return	Le montant correspondant.
	 */
	static BigDecimal parseAmount(String s) {
		return new BigDecimal(s.replace(',', '.'));
	}

	/**
	 * Crée une nouvelle source de données au format CSV.
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
	 * @throws IOException
	 */
	public static CacheableDAOFactory newInstance(File file)
			throws IOException {
		
		// La nouvelle instance
		CsvDAO csvDAO = new CsvDAO(file);
		
		// Si file n'est pas un ZIP valide, mettre la source en écriture seule
		return csvDAO.zip == null
				? new WriteOnlyCacheableDAOFactory(csvDAO)
				: csvDAO;
	}

	/**
	 * L'objet ZIP contenant les données.
	 */
	private final ZipFile zip;
	
	/**
	 * Le fichier au format ZIP dans lequel lire et écrire les données.
	 */
	private final File file;
	
	/**
	 * Les comptes, classés par identifiant.
	 * <p>
	 * Il s'agit d'un mini-cache nécessaire car la plupart des flux dépendent
	 * des identifiants des comptes.
	 */
	private final Map<Integer, Compte> comptesById;
	
	/**
	 * Construit un objet d'accès aux données utilisant des données CSV et XML
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
	private CsvDAO(File file) throws IOException {
		this.file = file;
		zip = file.exists() ? new ZipFile(file) : null;
		comptesById = CsvCompteDAO.loadComptes(getReader(COMPTES));
	}
	
	/**
	 * Renvoie un lecteur CSV à partir d'un fichier contenu dans l'archive ZIP.
	 * 
	 * @param entryName	Le nom du fichier à lire dans l'archive ZIP.
	 * 
	 * @return			Un lecteur CSV.
	 * 
	 * @throws IOException
	 */
	private CsvReader getReader(String entryName) throws IOException {
		return new CsvReader(getZipInputStream(entryName), DELIMITER, CHARSET);
	}
	
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
	}
	
	@Override
	public Iterator<Banque> getBanques() throws IOException {
		return new JaxbBanqueDAO(getZipInputStream(BANQUES));
	}

	@Override
	public Iterator<Compte> getComptes() throws IOException {
		return comptesById.values().iterator();
	}

	@Override
	public Iterator<Ecriture> getEcritures() throws IOException {
		return new CsvEcritureDAO(getReader(ECRITURES), comptesById);
	}

	@Override
	public Iterator<Permanent> getPermanents(CachePermanentDAO cache)
			throws IOException {
		return new JaxbPermanentDAO(
				getZipInputStream(PERMANENTS), cache, comptesById);
	}

	@Override
	public Iterator<Solde> getHistorique()
			throws IOException {
		return getSuivi(HISTORIQUE);
	}

	@Override
	public Iterator<Solde> getSoldesAVue()
			throws IOException {
		return getSuivi(SOLDES);
	}

	@Override
	public Iterator<Solde> getMoyennes()
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
	private Iterator<Solde> getSuivi(String entryName) throws IOException {
		return new CsvSuiviDAO(getReader(entryName), comptesById);
	}
	
	@Override
	public CacheablePropertiesDAO getProperties()
			throws IOException {
		return new JaxbPropertiesDAO(getZipInputStream(PROPRIETES));
	}

	@Override
	public void save(CacheDAOFactory cache) throws IOException {
		
		// Créer un flux d'écriture ZIP vers un fichier temporaire
		File tmp = File.createTempFile(
				"comptes-", ".tmp", file.getParentFile());
		
		try (ZipOutputStream zipOut =
				new ZipOutputStream(new FileOutputStream(tmp), CHARSET)) {
			
			// Flux d'écriture CSV
			CsvWriter csvOut = new CsvWriter(zipOut, DELIMITER, CHARSET);

			// Écrire les banques
			saveBanques(zipOut, cache.getBanqueDAO());
			
			// Écrire les comptes
			Map<Compte, Integer> idByCompte =
					createIds(cache.getCompteDAO().getAll());
			saveComptes(zipOut, csvOut, idByCompte);
			
			// Écrire les écritures
			saveEcritures(zipOut, csvOut, cache.getEcritureDAO(), idByCompte);
			
			// Écrire les écritures permanentes
			savePermanents(zipOut, cache.getPermanentDAO(), idByCompte);
			
			// Écrire les trois types de suivis
			saveSuivis(cache.getHistoriqueDAO(), idByCompte,
					HISTORIQUE, zipOut, csvOut);
			saveSuivis(cache.getSoldeAVueDAO(), idByCompte,
					SOLDES, zipOut, csvOut);
			saveSuivis(cache.getMoyenneDAO(), idByCompte,
					MOYENNES, zipOut, csvOut);
			
			// Écrire les propriétés
			saveProperties(zipOut, cache.getPropertiesDAO());
			
		} catch (FactoryConfigurationError e) {
			throw new IOException("Erreur d'écriture XML", e);
		}
		
		// Remplacer l'ancien fichier
		File bak = null;
		if (file.exists()) {

			// Sauvegarde temporaire du fichier initial
			bak = new File(
					file.getParentFile(),		// Même répertoire
					file.getName() + ".bak");	// Extension ".bak"

			// Supprimer l'ancienne sauvegarde si elle existe
			Files.deleteIfExists(bak.toPath());

			// Renommer le fichier initial en sauvegarde
			if (!file.renameTo(bak)) {
				throw new IOException(
						"Impossible de renommer le fichier temporaire créé");
			}
		}

		// Renommer le fichier temporaire en fichier définitif
		Files.move(tmp.toPath(), file.toPath());

		// Effacer la sauvegarde devenue inutile
		if (bak != null)
			Files.deleteIfExists(bak.toPath());
	}

	/**
	 * Écrit un fichier XML des banques dans l'archive ZIP.
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
	}

	/**
	 * Écrit un fichier CSV des comptes dans l'archive ZIP.
	 * 
	 * @param zipOut		Le flux d'écriture vers l'archive ZIP.
	 * @param csvOut		Le flux d'écriture CSV.
	 * @param idByCompte	Les comptes et leurs identifiants.
	 * 
	 * @throws IOException
	 */
	private void saveComptes(ZipOutputStream zipOut, CsvWriter csvOut,
			Map<Compte, Integer> idByCompte) throws IOException {
		zipOut.putNextEntry(new ZipEntry(COMPTES));
		CsvCompteDAO.save(idByCompte, csvOut);
		csvOut.flush();
		zipOut.closeEntry();
	}
	
	/**
	 * Affecte un identifiant unique à chaque objet.
	 * 
	 * @param objects	La collection des objets à numéroter.
	 * 
	 * @return			Tous les objets, avec leur identifiant créé.
	 * 
	 * @throws IOException
	 */
	private <T> Map<T, Integer> createIds(Iterable<T> objects) {
		Map<T, Integer> objectsById = new HashMap<>();
		int id = 1;
		for (T t : objects)
			objectsById.put(t, id++);
		return objectsById;
	}
	
	/**
	 * Écrit un fichier CSV des écritures dans l'archive ZIP.
	 * 
	 * @param zipOut		Le flux d'écriture vers l'archive ZIP.
	 * @param csvOut		Le flux d'écriture CSV.
	 * @param eDAO			L'objet d'accès aux écritures.
	 * @param idByCompte	Les comptes, avec leurs identifiants.
	 * 
	 * @throws IOException
	 */
	private void saveEcritures(ZipOutputStream zipOut, CsvWriter csvOut,
			EcritureDAO eDAO, Map<Compte, Integer> idByCompte)
					throws IOException {
		zipOut.putNextEntry(new ZipEntry(ECRITURES));
		CsvEcritureDAO.save(eDAO.getAll().iterator(), idByCompte, csvOut);
		csvOut.flush();
		zipOut.closeEntry();
	}
	
	/**
	 * Écrit un fichier XML des opérations permanentes dans l'archive ZIP.
	 * 
	 * @param zipOut		Le flux d'écriture de l'archive ZIP.
	 * @param pDAO			Les opérations permanentes.
	 * @param idByCompte	Les comptes, avec leurs identifiants.
	 * 
	 * @throws IOException
	 */
	private void savePermanents(ZipOutputStream zipOut, PermanentDAO pDAO,
			Map<Compte, Integer> idByCompte) throws IOException {
		
		// Écrire les données des opérations permanentes
		zipOut.putNextEntry(new ZipEntry(PERMANENTS));
		JaxbPermanentDAO.save(pDAO.getAll().iterator(), idByCompte, zipOut);
		zipOut.closeEntry();
		
		// Enregistrer aussi le schéma XSD
		saveSchema(JaxbPermanentDAO.class, "permanents.xsd", zipOut);
	}

	/**
	 * Écrit un fichier XML des propriétés dans l'archive ZIP.
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
	}
	
	/**
	 * Sauvegarde un fichier dans l'archive ZIP.
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
		
		try (InputStream schemaIn = new BufferedInputStream(
					classe.getResourceAsStream(fileName))) {
			int b;
			while ((b = schemaIn.read()) != -1)		// Copier le contenu
				zipOut.write(b);					// vers le fichier ZIP
			
		} catch (IOException e) {
			LOGGER.log(Level.WARNING,
					"Le schéma XML " + fileName + " n'a pas pu " +
					"être intégré au fichier enregistré.\n" +
					"Cette erreur n'est pas bloquante", e);
		}
		zipOut.closeEntry();						// Fermer l'entrée ZIP
	}
	
	/**
	 * Sauvegarde un type de suivis.
	 * 
	 * @param suivis		L'objet d'accès aux suivis à sauvegarder.
	 * 
	 * @param idByCompte	Les comptes des suivis à sauvegarder, avec leurs
	 * 						identifiant.
	 * 
	 * @param entryName		Le nom de l'entrée ZIP dans laquelle sauvegarder
	 * 						<code>suivis</code>.
	 * 
	 * @param zipOut		Un flux d'écriture ZIP.
	 * 
	 * @param writer		Un flux CSV écrivant vers <code>zipOut</code>.
	 * 
	 * @throws IOException 
	 */
	private void saveSuivis(CacheSuiviDAO suivis,
			Map<Compte, Integer> idByCompte, String entryName,
			ZipOutputStream zipOut, CsvWriter csvOut) throws IOException {
		zipOut.putNextEntry(new ZipEntry(entryName));
		CsvSuiviDAO.save(suivis, idByCompte, csvOut);
		csvOut.flush();
		zipOut.closeEntry();
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
	
	/**
	 * @returns	<code>true</code>
	 */
	@Override
	public boolean canBeSaved() {
		return true;
	}

	/**
	 * Aucune implémentation.
	 */
	@Override
	public void close() throws IOException {
		// Rien à fermer
	}

}
