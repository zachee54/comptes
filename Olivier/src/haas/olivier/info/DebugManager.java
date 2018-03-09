/*
 * Copyright (c) 2018 Olivier HAAS - Tous droits réservés
 */
package haas.olivier.info;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

/** Une classe utilitaire statique de gestion des modes de débogage.
 *
 * @author Olivier HAAS
 */
public class DebugManager {
	
	/** Le <code>Logger</code> racine. */
	private static Logger rootLogger = Logger.getLogger("haas.olivier");
	
	/** Gestionnaire de sortie des logs vers la console. */
	private static ConsoleHandler consoleHandler = null;
	
	/** Gestionnaire de sortie des logs vers un fichier. */
	private static FileHandler fileHandler = null;
	
	/** Niveau actuel de déclenchement des <code>Handler</code>s.<br>
	 * Par défaut, c'est <code>Level.OFF</code>. */
	private static Level level = Level.OFF;
	
	/** Active la sortie des logs vers la console et vers un fichier.
	 * 
	 * @param prefix	Le préfix dans le nom des fichiers de log.
	 * @param title		Le titre à donner en en-tête des fichiers de log.
	 */
	private static void enableLogOutput(String prefix, String title) {
		
		// Sortie des logs sur la console
		consoleHandler = new ConsoleHandler();	// Définir le Handler
		consoleHandler.setFormatter(			// Mise en forme personnalisée
				new DebugFormatter(title));
		rootLogger.addHandler(consoleHandler);	// Enregistrer
		
		// Sortie des logs en fichier
		try {
			
			// Créer le sous-répertoire des fichiers log
			prefix = prefix.toLowerCase();		// Répertoire en minuscules
			File logDir =						// Nom du sous-répertoire
					new File(prefix+".log");
			if (!logDir.exists())				// S'il n'existe pas
				logDir.mkdirs();				// On le crée
			
			// Créer le gestionnaire de fichiers de logs
			fileHandler = new FileHandler(		// Fichiers de log (rép courant)
					logDir.getName() + File.separator	// Répertoire
							+ prefix + "%g.%u.log.txt",	// Nom fichier log
					524288,						// Taille maxi des fichiers log
					10,							// Nombre maxi des fichiers log
					false);						// Ne pas cumuler
			
			fileHandler.setFormatter(			// Mise en forme personnalisée
					new DebugFormatter(title));
			
			rootLogger.addHandler(fileHandler);	// Enregistrer
			rootLogger.addHandler(new DialogHandler(null));
			
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}// try FileHandler
	}// enableLogOutput
	
	/** Désactive l'exportation des logs vers la console et vers les fichiers de
	 * log.
	 */
	private static void disableLogOutput() {
		
		// Retirer les Handlers
		rootLogger.removeHandler(consoleHandler);
		rootLogger.removeHandler(fileHandler);
		
		// Fermer les Handlers
		consoleHandler.close();
		fileHandler.close();	// Important pour les ressources fichiers
		
		// Oublier les objets
		consoleHandler = null;
		fileHandler = null;
	}// disableLogOutput
	
	/** Renvoie le niveau actuel à partir duquel les logs sont exportés vers la
	 * console et aux fichiers de log.
	 */
	public static Level getLogOutputLevel() {
		return level;
	}// getLogOutputLevel
	
	/** Définit le niveau minimal des logs à exporter vers la console et vers
	 * les fichiers de log.
	 * 
	 * @param newLevel	Le niveau minimal des logs à exporter.
	 * @param prefix	Le préfixe à utiliser pour le nom des fichiers de log.
	 * @param title		Le titre à inscrire en en-tête des logs.
	 */
	public static void setLogOutputLevel(Level newLevel, String prefix,
			String title) {
		
		if (newLevel == Level.OFF) {					// On ne veut rien
			disableLogOutput();							// Désactiver tout
			
		} else {										// On veut quelque chose
			
			// Définir les handlers s'ils n'existent pas encore
			if (level == Level.OFF)
				enableLogOutput(prefix, title);
			
			// Définir le niveau de chaque handler
			rootLogger.setLevel(newLevel);
			consoleHandler.setLevel(newLevel);
			fileHandler.setLevel(newLevel);
		}// if level
		
		// Mémoriser le nouveau niveau
		level = newLevel;
	}// setLogOutputLevel

}// class DebugManager

/** Classe de mise en forme des logs en vue du débogage.
 * <p>
 * Les logs formatés par cette classe indiquent un horodatage complet au début,
 * puis un horodatage plus bref en début de chaque ligne.<br>
 * Chaque instance est faite pour n'être utilisée qu'avec un seul
 * <code>Handler</code> à la fois. Si la même instance est utilisée pour
 * plusieurs <code>Handler</code>s, un seul d'entre eux aura l'horodatage de
 * départ. 
 * 
 * @author Olivier HAAS
 */
class DebugFormatter extends Formatter {

	/** Format de temps pour l'horodatage ligne par ligne. */
	private static final DateFormat df =
			new SimpleDateFormat("kk:mm:ss.SSS");
	
	/** Titre du log */
	private final String title;
	
	/** Drapeau indiquant si le log à traiter est le premier depuis
	 * l'instanciation.
	 */
	private boolean debut = true;
	
	/** Constructeur un formateur de logs avec horodatage simple.
	 * 
	 * @param title	Le titre à donner au log.
	 */
	public DebugFormatter(String title) {
		this.title = title;
	}// constructeur
	
	@Override
	public String format(LogRecord record) {
		String eol = System.getProperty("line.separator");	// Char fin de ligne
		String result = "";
		
		// Horodater le début du premier log
		if (debut) {
			result += title + ", "
					+ new SimpleDateFormat("EEEE d MMMM yyyy").format(
							record.getMillis())
					+ eol;
			debut = false;									// Seulement 1 fois
		}// if debut
		
		// Horodatage de ligne et message principal
		result += df.format(record.getMillis()) + " : "		// Horodatage
				+ record.getMessage()						// Message
				+ " (" + Thread.currentThread().getName() + ")" + eol;// Thread
		
		// Décrire l'exception s'il y en a une
		Throwable thrown = record.getThrown();				// L'exception
		boolean first = true;								// Drapeau niveau 1
		while (thrown != null) {							// Chaque niveau
			
			// Traiter différemment la première exception et les suivantes
			if (first) {									// La première
				first = false;
			} else {										// Les suivantes
				result += "Causée par : ";
			}// if first
			
			// Type et message de l'exception
			result += thrown.getClass().getName()+": "		// Classe exception
					+ thrown.getMessage() + eol;			// Message
			
			// StackTrace
			for (StackTraceElement stackElement : thrown.getStackTrace()) {
				result += "\tà "
						+ stackElement.getClassName()		// Classe traversée
						+ "." + stackElement.getMethodName()// Méthode et ligne
						+ " (" + stackElement.getLineNumber() + ")" + eol;
			}
			
			// Continuer par récursivité sur la cause
			thrown = thrown.getCause();
		}// while thrown
		
		return result;
	}// format
	
}// class debugFormatter