package haas.olivier.comptes.dao.csv;

import java.io.Closeable;
import java.io.IOException;
import java.text.ParseException;
import java.util.NoSuchElementException;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.csvreader.CsvReader;

import haas.olivier.util.ReadOnlyIterator;

/**
 * Une couche d'accès aux données CSV.
 * <p>
 * Cette couche s'intercale entre le cache et le fichier CSV.
 * <p>
 * Cette classe abstraite permet de réunir l'architecture générale des classes
 * concrètes qui permettent d'accéder chacune à un type de données (comptes,
 * écritures, suivis, etc).
 * 
 * @author Olivier HAAS
 *
 * @param <T>	Le type d'éléments restitués par la classe (Compte, Ecriture,
 * 				Banque, etc).
 */
abstract class AbstractCsvLayer<T> extends ReadOnlyIterator<T>
implements Closeable {

	/**
	 * Le Logger de cette classe.
	 */
	private static final Logger LOGGER =
			Logger.getLogger(AbstractCsvLayer.class.getName());
	
	/**
	 * Le lecteur CSV.
	 */
	protected final CsvReader reader;
	
	/**
	 * Le prochain objet à renvoyer (ou <code>null</code> s'il a déjà été
	 * renvoyé, ou s'il n'y a plus rien à renvoyer).
	 */
	protected T next;
	
	/**
	 * Construit une couche d'accès aux données CSV.
	 * 
	 * @param reader	Le lecteur CSV permettant de lire les données.<br>
	 * 					S'il est <code>null</code>, la classe ne fait rien
	 * 					puisqu'il n'y a rien à lire.
	 * 
	 * @throws IOException
	 */
	AbstractCsvLayer(CsvReader reader) throws IOException {
		this.reader = reader;
		
		// Lire les en-têtes
		if (reader != null)
			reader.readHeaders();
	}

	@Override
	public boolean hasNext() {
		
		// Selon qu'il y a ou non un élément déjà prêt
		if (next == null) {					// Pas d'élément tout prêt
			try {
				
				/*
				 * S'il y a quelque chose à lire (reader non null), pas terminé
				 * (readRecord renvoie true) et pas fermé (sinon IOException) ?
				 */
				if (reader != null && reader.readRecord()) {
					next = readNext(reader);// Charger l'élément suivant
				} else {					// Fin du fichier
					close();				// Fermer le flux
				}
				
				// Renvoyer le résultat (dire s'il y a un élément)
				return next != null;
				
			} catch (NumberFormatException e) {// Pb de format numérique
				LOGGER.log(Level.SEVERE, "Erreur de format numérique", e);
				
			} catch (ParseException e) {	// Erreur de parsage de date
				LOGGER.log(Level.SEVERE, "Erreur de format de date", e);
				
			} catch (IOException e) {		// Lecteur fermé ou autre erreur
				LOGGER.log(Level.SEVERE, "Erreur pendant la lecture", e);
			}
			
			// Code exécuté uniquement en cas d'exception
			close();						// Fermer les ressources
			return false;					// Impossible de charger l'élément
			
		} else {							// S'il y a un élément tout prêt
			return true;					// C'est oui
		}
	}

	@Override
	public T next() {
		
		// S'il y a quelque chose à renvoyer
		if (next != null || hasNext()) {	// Déjà prêt ou préparé exprès pour
			T result = next;				// Mémoriser l'élément à renvoyer
			next = null;					// Remettre à null
			return result;					// Renvoyer
			
		} else {							// Plus rien à renvoyer
			throw new NoSuchElementException();
		}
	}
	
	/**
	 * Instancie l'élément suivant à partir de la ligne en cours du lecteur
	 * {@link #reader}.
	 * 
	 * @param reader	Le lecteur CSV.
	 * 
	 * @return			Une nouvelle instance correspondant aux données
	 * 					contenues dans la ligne en cours de lecture par le
	 * 					{@link #reader}.
	 * 
	 * @throws NumberFormatException
	 * @throws ParseException
	 * @throws IOException
	 */
	protected abstract T readNext(CsvReader reader)
			throws NumberFormatException, ParseException, IOException;
	
	/**
	 * Ferme les ressources.
	 */
	public void close() {
		if (reader != null) reader.close();
	}
}
