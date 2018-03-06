package haas.olivier.util.charset;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/** A class miming a <code>ByteBuffer</code> by appending many of them without
 * theorical size limit.
 * <p>
 * Concretely, it returns a readable byte buffer on demand.<br>
 * If the content of all buffers has been read, new bytes are silently read
 * from the given source.
 * <p>
 * The class maintains a collection of buffers containing all data since the
 * last mark, so that the reset operation remains possible at any time.
 * <p>
 * The data read before the last mark are discarded, saving heap memory as far
 * as reasonably possible.<br>
 * The class maintains also a buffer cache, allowing reuse of buffers containing
 * discarded bytes.
 *
 * @author Olivier HAAS
 */
class ExtensibleByteBuffer {
	
	/** The size of internal buffers. */
	private static final int BUF_SIZE = 1<<12;			// 4 ko
	
	/** A buffer cache.
	 * <p>
	 * All buffers aren't necessary useful, but they're kept here to avoid
	 * useless reallocations.<br>
	 * Useful buffers are always at the beginning of this list. The buffers must
	 * be moved to the tail as soon as their content is definitely consumed.
	 * <p>
	 * By construction, the mark relies on the head buffer.
	 * <p>
	 * {@link java.util.ArrayDeque} shall be more suitable, but actually more
	 * complex since it doesn't support random access. The current buffer would
	 * be quite necessarily determined by an iterator, which would be discarded
	 * at each modification of the collection, i.e. each {@link #mark()}
	 * invocation.<br>
	 * Far less readable for the same perf.
	 */
	private List<ByteBuffer> buffers = new ArrayList<ByteBuffer>();
	
	/** The index of actual buffer in {@link #buffers}. */
	private int index = -1;							// No buffer at this time
	
	/** The number of useful buffers in {@link #buffers}. */
	private int count = 0;
	
	/** Constructs an object miming <code>ByteBuffer</code> by appending many of
	 * them.
	 */
	ExtensibleByteBuffer() {
		// Load first buffer
		addBuffer();
	}// constructor
	
	/** Creates a new <code>ByteBuffer</code> and inserts it at the end of the
	 * cache.
	 */
	private void addBuffer() {
		buffers.add(ByteBuffer.allocate(BUF_SIZE));
	}// addBuffer
	
	/** Returns the buffer containing the next readable bytes.<br>
	 * If needed, new bytes are read from the given <code>InputStream</code>.
	 * <p>
	 * The returned <code>ByteBuffer</code> is guaranteed to contain remaining
	 * bytes, unless the very end of the <code>InputStream</code> has been
	 * reached.
	 * <p>
	 * Once the returned buffer has been fully read, a new invocation to the
	 * method returns a (possibly other) buffer containing the following bytes,
	 * if any.
	 * <p>
	 * Nota : java.nio package doesn't allow to extend <code>ByteBuffer</code>
	 * class for comprehensive security reasons.
	 * 
	 * @param in	The stream to read from in case all buffers have been read.
	 * 				It isn't invoked when the current buffer still has remaining
	 * 				bytes, especially when the object has just been reset.
	 * 
	 * @return		A buffer containing the next bytes to read, containing at
	 * 				least one remaining byte, or <code>null</code> if the end of
	 * 				<code>in</code> has been reached and all bytes have been
	 * 				read. 
	 * 
	 * @throws IOException
	 * 				May occur during source stream reading operation.
	 */
	ByteBuffer getBuffer(InputStream in) throws IOException {
		
		// If the actual buffer still has remaining bytes, return it
		ByteBuffer buf = buffers.get(index);		// Actual buffer
		if (buf.hasRemaining())
			return buf;
			
		// Else increment index and return the next useful buffer, if any
		if (++index < count)						// Existing next useful buf
			return buffers.get(index);				// Return it
		
		// No more unread useful buffer
		assert (index == count);
		
		// Load new bytes and return the buffer containing them
		return loadNextBuffer(in);
	}// getBuffer
	
	/** Load bytes into a unused buffer, creating it if necessary.
	 * 
	 * @param in	The byte stream to load the buffer with.
	 * 
	 * @return		The newly loaded buffer, or <code>null</code> if the end of
	 * 				<code>in</code> has been reached.
	 * 
	 * @throws IOException
	 * 				May occur during source stream reading operation.
	 */
	private ByteBuffer loadNextBuffer(InputStream  in) throws IOException {
		
		// Get an unused buffer
		if (buffers.size() == count)				// If no more unused buffer
			addBuffer();							// Create it
		ByteBuffer buf = buffers.get(count++);		// Use next unused buffer
		
		// Load new bytes
		int len = in.read(buf.array());
		
		// Return the loaded buffer... or null
		if (len == -1) {							// End of stream reached ?
			return null;							// null by specification
		} else {
			buf.limit(len);							// Limit buf to read length
			return buf;								// Return this buffer
		}// if
	}// loadNextBuffer
	
	/** Marks the actual position.
	 * <p>
	 * Every subsequent bytes read will be kept in heap memory and will be again
	 * readable through the {@link #reset()} operation.
	 * <p>
	 * The content of all previous buffers is discarded, even if it may be kept
	 * in heap for buffer cache reasons only.
	 */
	void mark() {
		
		// Mark the position of the current buffer
		buffers.get(index).mark();
		
		// Move obsolete buffers to the end of the cache list
		count -= index;								// New count of useful bufs
		while (index > 0) {							// While head is obsolete
			buffers.add(buffers.remove(0));			// Move to end
			index--;								// One less before actual
		}// while
	}// mark
	
	/** Go back to the previously marked position.
	 * 
	 * @throws InvalidMarkException
	 * 			If the mark has not been set.
	 */
	void reset() {
		
		// Rewind actual buffer for further read
		buffers.get(index).rewind();
		
		// Go back to marked position of marked buffer (head from cache)
		index = 0;
		buffers.get(0).reset();
	}// reset
}
