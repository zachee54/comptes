/*
 * Copyright (c) 2018 Olivier HAAS - Tous droits réservés
 * 
 */
package haas.olivier.info;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

/**
 * Classe de mise en forme des logs en vue du débogage.
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

	/**
	 * Séparateur de lignes propre à la plate-forme.
	 */
	private static final String EOL = System.getProperty("line.separator");
	
	/**
	 * Format de temps pour l'horodatage ligne par ligne.
	 */
	private final DateFormat dateFormat = new SimpleDateFormat("kk:mm:ss.SSS");
	
	/**
	 * Titre du log.
	 */
	private final String title;
	
	/**
	 * Drapeau indiquant si le log à traiter est le premier depuis
	 * l'instanciation.
	 */
	private boolean debut = true;
	
	/**
	 * Constructeur un formateur de logs avec horodatage simple.
	 * 
	 * @param title	Le titre à donner au log.
	 */
	public DebugFormatter(String title) {
		this.title = title;
	}
	
	@Override
	public String format(LogRecord record) {
		StringBuilder builder = new StringBuilder(400);
		if (debut) {
			appendTitleAndDate(builder, record);
			debut = false;									// Seulement 1 fois
		}
		appendMessage(builder, record);
		appendExceptions(builder, record.getThrown());
		return builder.toString();
	}
	
	/**
	 * Ajoute le titre et la date du log dans le <code>StringBuilder</code>
	 * spécifié.
	 *
	 * @param builder	Le <code>StringBuilder</code> à compléter.
	 * @param record	Le log dont il faut extraire la date.
	 */
	private void appendTitleAndDate(StringBuilder builder, LogRecord record) {
		builder.append(title)
		.append(", ")
		.append(new SimpleDateFormat("EEEE d MMMM yyyy").format(
						record.getMillis()))
		.append(EOL);
	}
	
	/**
	 * Ajoute dans le <code>StringBuilder</code> spécifié un horodatage de
	 * l'heure, le message de log et le nom du thread.
	 *
	 * @param builder	Le <code>StringBuilder</code> à compléter.
	 * @param record	Le message de log.
	 */
	private void appendMessage(StringBuilder builder, LogRecord record) {
		builder.append(
				dateFormat.format(record.getMillis()))		// Horodatage
		.append(" : ")
		.append(formatMessage(record))						// Message
		.append(" (")
		.append(Thread.currentThread().getName())			// Thread
		.append(')')
		.append(EOL);
	}
	
	/**
	 * Ajoute au <code>StringBuilder</code> une description de l'exception
	 * spécifiée et de toutes ses causes successives.
	 *
	 * @param builder	Le <code>StringBuilder</code> à compléter.
	 * @param thrown	L'exception à décrire.
	 */
	private void appendExceptions(StringBuilder builder, Throwable thrown) {
		Throwable cause = thrown;
		while (cause != null) {
			if (cause != thrown) {
				builder.append("Causée par : ");
			}
			appendException(builder, cause);
			cause = cause.getCause();
		}
	}
	
	/**
	 * Ajoute au <code>StringBuilder</code> une description de l'exception
	 * spécifiée.
	 *
	 * @param builder	Le <code>StringBuilder</code> à compléter.
	 * @param thrown	L'exceptino à décrire.
	 */
	private void appendException(StringBuilder builder, Throwable thrown) {
		
		// Type et message de l'exception
		builder.append(thrown.getClass().getName())		// Classe de l'exception
		.append(':').append(' ')
		.append(thrown.getMessage())					// Message
		.append(EOL);
		
		// StackTrace
		for (StackTraceElement stackElement : thrown.getStackTrace()) {
			builder.append("\tà ")
			.append(stackElement.getClassName())		// Classe traversée
			.append('.')
			.append(stackElement.getMethodName())		// Méthode
			.append(" (")
			.append(stackElement.getLineNumber())		// Numéro de ligne
			.append(')')
			.append(EOL);
		}
	}
}