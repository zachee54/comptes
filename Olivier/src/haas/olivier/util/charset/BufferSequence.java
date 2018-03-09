/*
 * Copyright (c) 2018 Olivier HAAS - Tous droits réservés
 */
package haas.olivier.util.charset;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Queue;

/** A sequence of <code>ByteBuffer</code>s.
 * <p>
 * This class provides byte buffers to read, filled with the bytes from the
 * underlying input stream.
 * <p>
 * The {@link #getBuffer()} method always returns the same buffer until the
 * {@link #nextBuffer()} method is invoked.
 * <p>
 * The buffers are taken from the given <code>BufferCache</code>, and filled
 * with bytes from the given input stream.
 * <p>
 * It's possible to insert some buffers that must be read first before
 * continuing the normal buffer iteration.
 *
 * @author Olivier HAAS
 */
class BufferSequence implements Closeable {
	
	/** A buffer cache.
	 * <p>
	 * All new buffers are provided from this cache.
	 * <p>
	 * These buffers <b>must</b> be backed by an array.
	 */
	private final BufferCache cache = new BufferCache();

	/** The byte stream to fill the buffers with. */
	final InputStream in;
	
	/** The buffers to read.
	 * <p>
	 * The head of queue is the actual buffer.
	 * <p>
	 * This collection shall contain only one buffer, since bytes are loaded in
	 * one buffer at a time.<br>
	 * It might only contain more of them if buffers have been inserted before
	 * the current one.
	 */
	private Queue<ByteBuffer> queue = new LinkedList<ByteBuffer>();
	
	/** Constructs a sequence of buffers filled with bytes from the given
	 * stream.
	 * 
	 * @param in	The byte stream to fill the buffers with.
	 */
	public BufferSequence(InputStream in) {
		this.in = in;
	}// constructor
	
	/** Returns the actual buffer to read.
	 * 
	 * @return	A buffer, or <code>null</code> if the end of stream has been
	 * 			reached and all buffers have been discarded.
	 * 
	 * @throws IOException
	 * 			If an error occurs while reading the stream.
	 */
	public ByteBuffer getBuffer() throws IOException {
		
		// Make sure that a buffer is present
		if (queue.isEmpty()) {						// No more buffer to read ?
			
			// Load new buffer
			ByteBuffer buf = cache.get();			// Take a new buffer
			int n = in.read(buf.array());			// Fill it with new bytes
			
			// Whether something has been read or not
			if (n == -1) {							// If end of stream reached
				cache.put(buf);						// Give back buffer (unused)
				return null;						// Give up and return null
				
			} else {								// Something has been read
				buf.limit(n);						// Set limit of new buffer
				queue.offer(buf);					// Put it in the queue
			}// if
		}// if queue empty
		
		// Return the head of queue as actual buffer
		return queue.peek();
	}// getBuffer
	
	/** Discards the actual buffer.
	 * <p>
	 * This method should be invoked when the actual buffer has been fully read.
	 * 
	 * @return	The next buffer to read, or <code>null</code> if end of stream
	 * 			has been reached and all buffers have been discarded.
	 * 
	 * @throws IOException
	 * 			If an error occurs while reading the stream.
	 */
	public ByteBuffer nextBuffer() throws IOException {
		
		// Remove actual buffer from the queue
		ByteBuffer actual = queue.poll();
		
		// If filled under its capacity, then it was the last buffer : it's over
		if (actual == null || actual.limit() < actual.capacity())
			return null;
		
		// Else get the next one
		return getBuffer();
	}// nextBuffer
	
	/** Inserts buffers to read before the actual one.<br>
	 * They will be read in the same order as <code>buffers</code> iterates over
	 * them.
	 * <p>
	 * The actual buffer will be rewinded in order to be read again from its
	 * beginning, after the newly inserted buffers.
	 * <p>
	 * It is guaranteed that the actual buffer's mark isn't discarded by this
	 * method.
	 * 
	 * @param buffers	The buffers to insert before the actual one.
	 */
	public void insert(Collection<ByteBuffer> buffers) {
		
		// Rewind actual buffer, keeping the mark if any
		if (!queue.isEmpty())
			queue.peek().position(0);	// not rewind() which would discard mark
		
		// Create a new queue with new buffers and actual buffers, in this order
		Queue<ByteBuffer> newQueue =
				new LinkedList<ByteBuffer>(buffers);// Queue with new buffers
		newQueue.addAll(queue);						// Add actual buffers
		queue = newQueue;							// Replace queue
	}// insert
	
	/** Deprecate byte buffers.
	 * <p>
	 * The specified buffers will fall into buffer cache and may be reused at
	 * any time.
	 * 
	 * @param bufs	Byte buffers to deprecate.
	 */
	public void deprecate(Collection<ByteBuffer> bufs) {
		for (ByteBuffer buf : bufs)
			deprecate(buf);
	}// decprecate buffers
	
	/** Deprecates a byte buffer.
	 * <p>
	 * The specified buffer will fall into buffer cache and may by reused at any
	 * time.
	 * 
	 * @param buf	The byte buffer to deprecate.
	 */
	public void deprecate(ByteBuffer buf) {
		cache.put(buf);
	}// deprecate buffer

	/** Closes the underlying input stream. */
	@Override
	public void close() throws IOException {
		in.close();
	}// close
}
