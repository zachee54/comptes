package haas.olivier.comptes.dao0.serialize;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import haas.olivier.comptes.Compte;
import haas.olivier.comptes.Ecriture;
import haas.olivier.util.Month;
import haas.olivier.comptes.Permanent;
import haas.olivier.comptes.dao.cache.CacheableCompteDAO;
import haas.olivier.comptes.dao.cache.CacheableDAOFactory;
import haas.olivier.comptes.dao.cache.CacheableEcritureDAO;
import haas.olivier.comptes.dao.cache.CacheablePermanentDAO;
import haas.olivier.comptes.dao.cache.CacheableSuiviDAO;

/** Un CacheableDAO utilisant la sérialisation par défaut de Java.
 * Les données sont sérialisées dans un fichier compressé au format gzip et
 * restituées telles quelles.
 * Lors de la sauvegarde, toutes les anciennes données sont écrasées.
 *
 * @author Olivier HAAS
 */
public class SerializeDAOFactory implements CacheableDAOFactory {

	/** Le fichier à lire/écrire. */
	private File file;
	
	// Les données en mémoire
	private HashSet<Compte> comptes = null;
	private TreeSet<Ecriture> ecritures = null;
	private HashSet<Permanent> permanents = null;
	private Map<Month,Map<Integer,BigDecimal>>
		historique = null, soldes = null, moyennes = null;
	private Properties diagramProperties;
	
	// Les DAO thématiques
	private CacheableCompteDAO cDAO		= new SerializeCompteDAO();
	private CacheableEcritureDAO eDAO	= new SerializeEcritureDAO();
	private CacheablePermanentDAO pDAO	= new SerializePermanentDAO();
	private CacheableSuiviDAO
		hDAO = new SerializeSuiviDAO(SerializeSuiviDAO.HISTORIQUE),
		sDAO = new SerializeSuiviDAO(SerializeSuiviDAO.SOLDEAVUE),
		mDAO = new SerializeSuiviDAO(SerializeSuiviDAO.MOYENNE);
	
	/** Implémentation de CacheableCompteDAO pour les besoins de façade. */
	private class SerializeCompteDAO implements CacheableCompteDAO {

		@Override
		public Set<Compte> getAll() throws IOException {
			return comptes;
		}

		@Override
		public void save(Set<Compte> comptes) throws IOException {
			SerializeDAOFactory.this.comptes =
				comptes instanceof HashSet
					? (HashSet<Compte>) comptes
					: new HashSet<Compte>(comptes);
		}
	}// inner class SerializableCompteDAO
	
	/** Implémentation de CacheableEcritureDAO pour les besoins de façade. */
	private class SerializeEcritureDAO implements CacheableEcritureDAO {

		@Override
		public TreeSet<Ecriture> getAll() throws IOException {
			return ecritures;
		}

		@Override
		/** Aucune implémentation. */
		public void refreshCompte(Compte compte) throws IOException {
		}

		@Override
		public void save(TreeSet<Ecriture> ecritures) throws IOException {
			SerializeDAOFactory.this.ecritures = ecritures;
		}
	}// inner class SerializableEcritureDAO
	
	/** Implémentation de CacheablePermanentDAO pour les besoins de façade. */
	private class SerializePermanentDAO implements CacheablePermanentDAO {

		@Override
		public Set<Permanent> getAll() throws IOException {
			return permanents;
		}

		@Override
		public void save(Set<Permanent> permanents) throws IOException {
			SerializeDAOFactory.this.permanents =
				permanents instanceof HashSet
					? (HashSet<Permanent>) permanents
					: new HashSet<Permanent>(permanents);
		}
	}// inner class SerializablePermanentDAO
	
	/** Implémentation de CacheableSuiviDAO pour les besoins de façade. */
	private class SerializeSuiviDAO implements CacheableSuiviDAO {
		
		// Les types d'instances possibles
		private static final int HISTORIQUE = 1;
		private static final int SOLDEAVUE = 2;
		private static final int MOYENNE = 3;
		
		/** Le type de cette instance */
		private final int type;
		
		/** Construit une instance du type spécifié. */
		public SerializeSuiviDAO(int type) {
			this.type = type;
		}

		@Override
		public Map<Month, Map<Integer, BigDecimal>> getAll()
				throws IOException {
			
			// Renvoyer les données en fonction du type d'instance
			switch(type) {
			case HISTORIQUE :	return historique;
			case SOLDEAVUE	:	return soldes;
			case MOYENNE	:	return moyennes;
			}// switch
			throw new IOException();	// Erreur !
		}// getAll

		@Override
		/** Aucune implémentation. */
		public void removeSuiviCompte(int id) throws IOException {
		}

		@Override
		public void save(Map<Month, Map<Integer, BigDecimal>> suivis)
				throws IOException {
			
			// Stocker les valeurs en fonction du type d'instance
			switch(type) {
			case HISTORIQUE	:	SerializeDAOFactory.this.historique = suivis;
			case SOLDEAVUE	:	SerializeDAOFactory.this.soldes = suivis;
			case MOYENNE	:	SerializeDAOFactory.this.moyennes = suivis;
			}// switch
		}// save
	}// inner class SerializableSuiviDAO
	
	/** Construit des collections vides. */
	private void fillWithEmptyValues() {
		comptes		= new HashSet<Compte>();
		ecritures	= new TreeSet<Ecriture>();
		permanents	= new HashSet<Permanent>();
		historique	= new HashMap<Month,Map<Integer,BigDecimal>>();
		soldes		= new HashMap<Month,Map<Integer,BigDecimal>>();
		moyennes	= new HashMap<Month,Map<Integer,BigDecimal>>();
	}// fillWithEmptyValues
	
	/** Construit un SerializeDAOFactory appuyé sur le fichier spécifié.
	 * Les données sont chargées immédiatement et une fois pour toutes.
	 * A l'enregistrement, elles sont sérialisées selon l'implémentation choisie
	 * du DAOMemento.
	 * 
	 * @param file	Le fichier à lire/écrire.
	 * @param write	Le DAOMemento à utiliser pour écrire les données.
	 * @throws IOException 
	 * @throws FileNotFoundException 
	 */
	public SerializeDAOFactory(File file)
			throws IOException {
		this.file = file;							// Le fichier
	}// constructeur

	@Override
	public void load() throws IOException {
		// Désérialiser les données
		ObjectInputStream in = null;
		try {
			in = new ObjectInputStream(				// Flux objet
					new GZIPInputStream(			// Décompression gzip
							new BufferedInputStream(
									new FileInputStream(file))));// Flux fichier
			DAOMemento memento = (DAOMemento) in.readObject();	// Lecture
			comptes		= memento.comptes;			// Mémoriser les comptes
			ecritures	= memento.ecritures;		// Mémoriser les écritures
			permanents	= memento.permanents;		// Mémoriser les permanents
			historique	= memento.historique;		// Affecter les suivis
			soldes		= memento.soldes;
			moyennes	= memento.moyennes;
			diagramProperties = memento.diagramProperties;
			
		} catch (FileNotFoundException e) {
			// Pas de fichier: partir de données vierges
			fillWithEmptyValues();
			
		} catch (Exception e) {
			e.printStackTrace();
			throw new IOException();				// Erreur non gérable
			
		} finally {
			if (in != null) {
				in.close();							// Fermer le flux
			}
		}// try désérialiser
	}// load
	
	@Override
	/** @return false. */
	public boolean mustBeSaved() {
		return false;
	}

	@Override
	public String getName() {
		return "Serialize";
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
	public CacheableCompteDAO getCompteDAO() {
		return cDAO;
	}

	@Override
	public CacheableEcritureDAO getEcritureDAO() {
		return eDAO;
	}

	@Override
	public CacheablePermanentDAO getPermanentDAO() {
		return pDAO;
	}

	@Override
	public CacheableSuiviDAO getHistoriqueDAO() {
		return hDAO;
	}

	@Override
	public CacheableSuiviDAO getSoldeAVueDAO() {
		return sDAO;
	}

	@Override
	public CacheableSuiviDAO getMoyenneDAO() {
		return mDAO;
	}

	@Override
	public Properties getProperties() {
		return diagramProperties;
	}
	
	/** Aucune implémentation. */
	@Override
	public String getProperty(String prop) {return null;}
	
	/** Aucune implémentation. */
	@Override
	public void setProperty(String prop, String value) {}
	
	@Override
	public void flush() throws IOException {
		
		DAOMemento memento = new DAOMemento();
		
		// Ecrire les données dans le mémento
		memento.comptes = comptes;
		memento.ecritures = ecritures;
		memento.permanents = permanents;
		memento.historique = historique;
		memento.soldes = soldes;
		memento.moyennes = moyennes;
		memento.diagramProperties = diagramProperties;
		
		// Définir le fichier temporaire à utiliser
		File tmp = new File(file.getAbsolutePath() + ".tmp");
		
		// Ecrire le fichier temporaire
		ObjectOutputStream out = null;
		try {
			out = new ObjectOutputStream(				// Flux objet
					new GZIPOutputStream(				// Compression gzip
							new BufferedOutputStream(
									new FileOutputStream(tmp))));// Flux fichier
			out.writeObject(memento);				// Ecrire le bloc de données
		
		} catch (Exception e) {
		} finally {
			out.close();								// Fermer le flox
		}
		
		// Remplacer le fichier d'origine par le fichier temporaire
		if (tmp.exists()) {					// Le fichier tmp existe bien
			if (file.delete()				// On efface l'original...
					|| !file.exists()) {	// ...s'il existe !
				tmp.renameTo(file);			// Renommer le fichier temporaire
			}
		}// if tmp exists
	}// flush
	
	@Override
	/**
	 * Pas d'implémentation: la sauvegarde est pilotée par une sur-couche DAO.
	 */
	public void save() throws IOException {
	}

	@Override
	/** Efface les données précédemment désérialisées. */
	public void erase() {
		fillWithEmptyValues();
	}
}
