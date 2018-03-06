package haas.olivier.util.charset;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CoderResult;
import java.nio.charset.CodingErrorAction;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/** Un décodeur de flux multi-encodages.
 * <p>
 * Il transforme un flux de bytes en flux de caractères en utilisant un
 * encodage. Si cet encodage s'avère invalide, il bascule à la volée sur un
 * autre encodage.<br>
 * La liste des encodages à utiliser est fournie à l'instanciation.<br>
 * Les encodages sont utilisés dans l'ordre fourni.<br>
 * Dès qu'un encodage est incapable de décoder une partie du flux, il est mis de
 * côté et ne sera plus utilisé.<br>
 * <p>
 * En pratique, le flux produit comporte des caractères qui peuvent provenir de
 * plusieurs encodages successifs.<br>
 * Si une séquence de bytes correspond à des caractères différents selon les
 * encodages, il se peut qu'elle soit interprétée de manière différente suivant
 * sa position dans le flux si l'encodage a été modifié entretemps.
 *
 * @author Olivier HAAS
 */
public class MultiCharsetReaderStandalone extends Reader {

	/** Le flux de bytes en entrée. */
	private final InputStream in;

	/** Un itérateur parcourant les encodages successifs à utiliser. */
	private final Iterator<Charset> charsetIt;

	/** Le décodeur actuel. */
	private CharsetDecoder decoder;

	/** Un buffer de bytes lus le flux d'entrée. */
	private final byte[] bytes = new byte[4096];

	/** Un <code>ByteBuffer</code> enveloppant le tableau {@link #bytes}.
	 * <p>
	 * Lorsqu'une opération de décodage s'arrête parce que {@link #outBuf} est
	 * plein, tous les bytes lus sont considérés comme validés. En pratique, on
	 * marque la position du buffer.
	 * <p>
	 * Lorsqu'une opération de décodage s'arrête parce que ce buffer a été
	 * entièrement lu, tous les bytes lus depuis la marque sont considérés
	 * comme en cours de validation : on peut avoir des caractères à cheval
	 * entre la fin du buffer et les bytes suivants, sans savoir si on pourra
	 * les décoder.<br>
	 * On copie alors ces bytes en cours de validation dans un buffer temporaire
	 * de {@link #bufCache}. Comme on valide les caractères par blocs entiers
	 * dans {@link #outBuf} et que chaque caractère peut prendre plusieurs
	 * bytes, on peut avoir plusieurs buffers temporaires qui se cumulent. C'est
	 * pourquoi on les met en cache dans <code>bufCache</code>.
	 * <p>
	 * Si une erreur survient pendant une opération de décodage, on change
	 * d'encodage et on relit le contenu des buffers temporaires avant de relire
	 * le contenu de ce buffer.
	 */
	private final ByteBuffer inBuf = ByteBuffer.wrap(bytes);

	/** Un buffer de caractères décodés.
	 * <p>
	 * Lorsque ce buffer a été entièrement lu, on démarre un nouveau cycle qui
	 * consiste à décoder de nouveaux bytes, au besoin en en chargeant de
	 * nouveaux.
	 * <p>
	 * Si le décodage s'arrête avant que ce buffer soit rempli, les caractères
	 * déjà écrits sont validés. En pratique, on marque la position du buffer.
	 * <p>
	 * Si le décodage s'arrête en raison d'une erreur, on efface les caractères
	 * écrits depuis la dernière marque, on change d'encodage et on relit les
	 * derniers bytes lus depuis la marque.
	 */
	private final CharBuffer outBuf = CharBuffer.allocate(4096);

	/** Un cache de <code>ByteBuffer</code>s contenant les bytes en cours de
	 * lecture.<br>
	 * Il s'agit des bytes qui seront relus en cas d'échec de décodage.<br>
	 * Dès s
	 * <p>
	 * La liste est utilisée comme une file FIFO : lorsque des bytes ont dû être
	 * relus et qu'une partie d'entre eux ont pu être validés avec un nouvel
	 * encodage, on déplace les buffers validés à la fin de façon à garder au
	 * début de la file les buffers contenant les prochains bytes à valider.
	 * <p>
	 * Chaque buffer a une capacité au moins égale à {@link #inBuf}.
	 * <p>
	 * Il faut prévoir plusieurs buffers, car les bytes lus sont généralement
	 * plus nombreux que les caractères à écrire dans le buffer de sortie, et on
	 * ne peut pas définir de plafond <i>a priori</i> (le nombre maximum de
	 * bytes par caractère dépend de l'encodage).
	 * <p>
	 * Le principe du cache est de permettre l'ajout de nouveaux buffers, mais
	 * de ne pas en enlever. Il peut donc être trop grand. C'est la méthode qui
	 * l'utilise qui doit vérifier le nombre de buffers utiles et ignorer les
	 * suivants.
	 */
	private final List<ByteBuffer> bufCache = new ArrayList<ByteBuffer>();

	/** Le nombre de buffers temporaires utiles dans {@link #bufCache}. */
	private int tmpBufCount = 0;

	/** Index du buffer temporaire en cours de lecture dans {@link #bufCache}.*/
	private int tmpBufIndex = 0;

	/** Indique si la fin du flux {@link #in} a été atteinte. */
	private boolean endOfInput = false;
	
	/** Indique qu'un <code>flush</code> a été commencé.<br>
	 * Il peut s'être terminé normalement en ou pas. Dans tous les cas, on ne
	 * fera plus d'autre appel au décodeur que pour un flush.
	 */
	private boolean flushing = false;

	/** Construit un flux de caractères multi-encodages.
	 *
	 * @param in		Le flux à lire.
	 * @param charsets	Les encodages successifs à utiliser.
	 */
	public MultiCharsetReaderStandalone(InputStream in, Iterator<Charset> charsetIt) {
		this.in = in;
		this.charsetIt = charsetIt;

		// Mettre en place le premier décodeur
		if (!nextCharset())
			throw new IllegalArgumentException(
					"MultiCharsetReader nécessite au moins un encodage");

		// Initialiser les buffers (pas de données au départ)
		outBuf.flip();							// Pas de données en sortie
		inBuf.flip();							// Pas de données en entrée
		inBuf.mark();							// Position de départ "sûre"
	}// constructeur

	/** Installe le décodeur de l'encodage suivant.<br>
	 * S'il n'y a plus d'encodage disponible, l'encodage actuel est conservé.
	 *
	 * @return	<code>true</code> si un nouvel encodage a pu être installé,
	 *			 <code>false</code> s'il n'y a plus d'autre encodage disponible.
	 */
	private boolean nextCharset() {
		if (charsetIt.hasNext()) {					// Encore un encodage ?
			decoder = charsetIt.next().newDecoder();// Utiliser son décodeur
			return true;							// C'est bon
			
		} else {									// Plus d'encodage dispo
			
			// Ne plus reporter les erreurs de décodage : remplacer plutôt
			decoder.onMalformedInput(CodingErrorAction.REPLACE);
			decoder.onUnmappableCharacter(CodingErrorAction.REPLACE);
			return false;
		}// if
	}// nextCharset

	@Override
	public int read(char[] cbuf, int off, int len) throws IOException {

		// Si on a tout donné et que c'étaient les derniers caractères : fin
		if (!outBuf.hasRemaining() && endOfInput)
			return -1;							// C'est fini

		int n = 0;								// Compteur de caractères lus
		int remaining;							// Nb de carctères dispos

		// Tant que le buffer actuel ne suffit pas à remplir la demande
		while ((remaining = outBuf.remaining()) < len) {

			// Copier tout ce qui reste
			outBuf.get(cbuf, off, remaining);
			n += remaining;						// Nb de nvx caractères lus

			// Voir s'il reste des bytes qu'on peut décoder
			if (endOfInput) {					// Il ne reste plus de bytes
				return n;						// Se contenter de ça
			} else {							// Il reste des bytes
				fillOut();						// Re-remplir le buffer décodé
			}// if
		}// while

		// Lire ce qu'il faut encore (len - n) pour arriver à la cible (len)
		outBuf.get(cbuf, off, len - n);

		return n;
	}// read

	/** Remplit le buffer de sortie avec de nouveaux caractères décodés.
	 *
	 * @throws IOException	Si le flux d'entrée n'a pas pu être lu.
	 */
	private void fillOut() throws IOException {
		outBuf.clear();							// Réinitialiser buf de sortie
		CoderResult result;						// État de sortie du décodeur

		// Essayer de lire des bytes jusqu'à remplir le buffer de sortie
		while (!(result = decode()).isOverflow()) {// Sortie pas remplie

			// Si erreur de décodage et qu'on peut changer d'encodage
			if (result.isError() && nextCharset()) {
				outBuf.clear();					// Oublier les chars lus
				getCurrentBuffer().reset();		// Relire la partie à problème
			}// if error
			
			// Si on n'a pas assez de bytes (fin du flux)
			if (result.isUnderflow())			// Plus rien à lire
				break;							// On se contente de ça
		}// while

		// Marquer la position comme "sûre" dans le buffer d'entrée
		getCurrentBuffer().mark();

		// Oublier les buffers temporaires dont le contenu vient d'être validé
		tmpBufCount -= tmpBufIndex;				// Moins de tmp buffers utiles
		while (tmpBufIndex > 0) {
			tmpBufIndex--;						// Oublier les tmp buff d'avant
			bufCache.add(bufCache.remove(0));	// En les déplaçant à la fin
		}// while
		
		// Préparer le buffer de sortie pour la relecture
		outBuf.flip();
	}// fillOutBuf

	/** Réalise une opération de décodage.
	 * <p>
	 * Les bytes sont lus à partir des buffers temporaires en cache s'il y a eu
	 * une précédente erreur et qu'on n'a pas fini de relire ces bytes.<br>
	 * Sinon, ils sont lus normalement depuis le buffer d'entrée.
	 *
	 * @return	Le résultat renvoyé par
	 *			 {@link java.nio.charset.CharsetDecoder#decode(ByteBuffer, CharBuffer, boolean)}.
	 *
	 * @throws IOException
	 *			 Si le flux d'entrée n'a pas pu être lu.
	 */
	private CoderResult decode() throws IOException {
		CoderResult result = null;				// État de sortie du décodeur

		// Si on a déjà fait un flush, arrivé ici on ne peut rien faire d'autre
		if (flushing)
			return decoder.flush(outBuf);
		
		// Lire autant de bytes que possible
		while ((result = decoder.decode(getCurrentBuffer(), outBuf, endOfInput))
						.isUnderflow()) {		// Ce ByteBuffer ne suffit pas

			// Selon ce qu'il reste à lire
			if (tmpBufIndex < tmpBufCount) {	// Encore un buffer temporaire
				tmpBufIndex++;					// Passer au suivant

			} else if (endOfInput) {			// Si c'est la fin du flux
				flushing = true;				// Entrée en phase de flush
				return decoder.flush(outBuf);	// Écrire la fin du flux décodé
				
			} else {							// Sinon (cas général)
				saveCurrentBytes();				// Copier les bytes non validés
				fillIn();						// Charger de nouveaux bytes
			}// if buffer temporaire
		}// while

		return result;
	}// decode

	/** Renvoie le buffer en cours de lecture.
	 *
	 * @return	Un buffer temporaire de {@link #bufCache} si on est en train de
	 *			relire des bytes suite à une erreur, ou {@link #inBuf} sinon.
	 */
	private ByteBuffer getCurrentBuffer() {
		return tmpBufIndex < tmpBufCount
				? bufCache.get(tmpBufIndex)
				: inBuf;
	}// getCurrentBuffer

	/** Copie dans un nouveau buffer temporaire tous les bytes lus depuis la
	 * dernière position sûre dans {@link #inBuf}.
	 */
	private void saveCurrentBytes() {
		
		// Reculer jusqu'au début des bytes à sauvegarder
		inBuf.reset();
		
		// Si la marque est en fin de buffer, on n'a rien à faire
		if (!inBuf.hasRemaining())
			return;

		// Vérifier qu'on a un buffer temporaire disponible en cache
		if (tmpBufCount == bufCache.size())
			bufCache.add(ByteBuffer.allocate(inBuf.capacity()));

		/* Mémoriser les bytes déjà lus mais pas encore validés.
		 * Cette partie peut concerner juste la fin du buffer d'entrée, mais
		 * peut aussi s'exécuter plusieurs fois d'affilée avec un ou plusieurs
		 * buffers entiers (en fait autant qu'il faut de bytes pour remplir le
		 * CharBuffer de sortie).
		 */
		ByteBuffer tmpBuf =						// Le prochain buffer temporaire
				bufCache.get(tmpBufCount++);
		tmpBufIndex++;							// Ne pas utiliser maintenant
		tmpBuf.clear();							// Remettre le buffer à blanc
		tmpBuf.put(inBuf);						// Recopier les bytes
		tmpBuf.flip();							// Clore le tmp buffer
	}// saveCurrentBytes

	/** Remplit le buffer d'entrée avec de nouveaux bytes lus.
	 * <p>
	 * La démarche consiste à remplir le tableau {@link #bytes}, car la
	 * méthode {@link java.io.InputStream#read(byte[])} ne peut de toute façon
	 * écrire que dans un <code>byte[]</code>, pas dans un
	 * <code>ByteBuffer</code>.<br>
	 * Comme {@link #inBuf} est un <i>wrapper</i> de {@link #bytes}, il suffit
	 * de le réinitialiser sans passer par des opérations <code>put()</code>
	 * puis <code>flip()</code>.<br>
	 * <code>inBuf</code> est limité aux bytes lus.
	 * <p>
	 * Si la fin du flux a été atteinte, {@link #endOfInput} passe à
	 * <code>true</code>.
	 *
	 * @throws IOException	Si le flux d'entrée n'a pas pu être lu.
	 */
	private void fillIn() throws IOException {
		inBuf.clear();							// Réinitialiser le buf d'entrée
		inBuf.mark();							// Par défaut, relire tout
		int len = in.read(bytes);				// Lire de nouveaux bytes

		// Selon que le flux était terminé ou pas
		if (len == -1) {						// C'est fini
			endOfInput = true;					// Mémoriser
			inBuf.flip();						// Pas de données dans le buffer
		} else {								// On a lu quelque chose
			inBuf.limit(len);					// Limiter le buf à la qté lue
		}// if
	}// fillInBuf

	@Override
	public void close() throws IOException {
		in.close();
	}// close

}
