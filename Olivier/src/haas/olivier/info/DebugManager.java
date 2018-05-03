/*
 * Copyright (c) 2018 Olivier HAAS - Tous droits réservés
 */
package haas.olivier.info;

import java.io.File;
import java.io.IOException;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Une classe utilitaire statique de gestion des modes de débogage.
 *
 * @author Olivier HAAS
 */
public class DebugManager {
	
	/**
	 * Le <code>Logger</code> racine.
	 */
	private static Logger rootLogger = Logger.getLogger("haas.olivier");
	
	/**
	 * Gestionnaire de sortie des logs vers la console.
	 */
	private static ConsoleHandler consoleHandler = null;
	
	/**
	 * Gestionnaire de sortie des logs vers un fichier.
	 */
	private static FileHandler fileHandler = null;
	
	/**
	 * Niveau actuel de déclenchement des <code>Handler</code>s.<br>
	 * Par défaut, c'est {@link java.util.logging.Level#OFF Level.OFF}.
	 */
	private static Level level = Level.OFF;
	
	private DebugManager() {
	}
	
	/**
	 * Active la sortie des logs vers la console et vers un fichier.
	 * 
	 * @param prefix	Le préfixe dans le nom des fichiers de log.
	 * @param title		Le titre à donner en en-tête des fichiers de log.
	 */
	private static void enableLogOutput(String prefix, String title) {
		Logger logger = Logger.getLogger(DebugManager.class.getName());
		
		// Sortie des logs sur la console
		addConsoleHandler(title);
		
		// Sortie des logs en fichier
		try {
			addFileHandler(prefix, title);
		} catch (Exception e) {
			logger.log(Level.CONFIG,
					"Impossible de créer des fichiers de log", e);
		}
	}
	
	/**
	 * Ajoute un log vers la console.
	 *
	 * @param title	Le titre à donner aux en-têtes de fichiers de log.
	 */
	private static void addConsoleHandler(String title) {
		consoleHandler = new ConsoleHandler();
		consoleHandler.setFormatter(new DebugFormatter(title));
		rootLogger.addHandler(consoleHandler);
	}
	
	/**
	 * Ajoute un log vers des fichiers.
	 * 
	 * @param prefix	Le préfixe dans le nom des fichiers de log.
	 * @param title		Le titre à donner en en-tête des fichiers de log.
	 * 
	 * @throws IOException
	 * @throws SecurityException
	 */
	private static void addFileHandler(String prefix, String title)
			throws IOException {
		
		// Créer le sous-répertoire des fichiers log
		String lowerPrefix = prefix.toLowerCase();
		File logDir = new File(lowerPrefix + ".log");
		if (!logDir.exists()) {
			logDir.mkdirs();
		}
		
		// Créer le gestionnaire de fichiers de logs
		File logFile = new File(logDir, lowerPrefix + "%g.%u.log.txt");
		fileHandler = new FileHandler(
				logFile.getAbsolutePath(),
				524288,						// Taille maxi des fichiers log
				10,							// Nombre maxi des fichiers log
				false);						// Ne pas cumuler
		
		// Mise en forme personnalisée
		fileHandler.setFormatter(new DebugFormatter(title));
		
		// Enregistrer
		rootLogger.addHandler(fileHandler);
	}
	
	/**
	 * Désactive l'exportation des logs vers la console et vers les fichiers de
	 * log.
	 */
	private static void disableLogOutput() {
		
		// Retirer les Handlers
		rootLogger.removeHandler(consoleHandler);
		rootLogger.removeHandler(fileHandler);
		
		// Fermer les Handlers
		consoleHandler.close();
		fileHandler.close();		// Important pour les ressources fichiers
		
		// Oublier les objets
		consoleHandler = null;
		fileHandler = null;
	}
	
	/**
	 * Renvoie le niveau actuel à partir duquel les logs sont exportés vers la
	 * console et aux fichiers de log.
	 */
	public static Level getLogOutputLevel() {
		return level;
	}
	
	/**
	 * Définit le niveau minimal des logs à exporter vers la console et vers les
	 * fichiers de log.
	 * 
	 * @param newLevel	Le niveau minimal des logs à exporter.
	 * @param prefix	Le préfixe à utiliser pour les noms des fichiers de log.
	 * @param title		Le titre à inscrire en en-tête des logs.
	 */
	public static void setLogOutputLevel(Level newLevel, String prefix,
			String title) {
		
		if (newLevel == Level.OFF) {					// On ne veut rien
			disableLogOutput();							// Désactiver tout
			
		} else {										// On veut quelque chose
			
			// Définir les handlers s'ils n'existent pas encore
			if (level == Level.OFF) {
				enableLogOutput(prefix, title);
			}
			
			// Définir le niveau de chaque handler
			rootLogger.setLevel(newLevel);
			consoleHandler.setLevel(newLevel);
			fileHandler.setLevel(newLevel);
		}
		
		// Mémoriser le nouveau niveau
		level = newLevel;
	}

}