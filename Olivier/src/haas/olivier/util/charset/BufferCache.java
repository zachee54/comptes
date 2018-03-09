/*
 * Copyright (c) 2018 Olivier HAAS - Tous droits réservés
 */
package haas.olivier.util.charset;

import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.Queue;

/** A cache for <code>ByteBuffer</code>s.
 * <p>
 * The useless buffers can be put in this class in order to be resued
 * afterwards.
 * <p>
 * This class can also allocate new buffers if none is available.
 * 
 * @author Olivier HAAS
 */
class BufferCache {

	/** The size of new buffers. */
	private static final int BUF_SIZE = 1<<12;			// 4 ko
	
	/** The cached buffers. */
	private final Queue<ByteBuffer> buffers = new LinkedList<ByteBuffer>();
	
	/** Polls a <code>ByteBuffer</code> from cache, or allocate a new one if
	 * necessary.
	 * 
	 * @return	An unused <code>ByteBuffer</code>.
	 */
	public ByteBuffer get() {
		
		// Get a new buffer from cache
		ByteBuffer buf = buffers.poll();
		
		// Return it, or a new one if null
		return buf == null ? ByteBuffer.allocate(BUF_SIZE) : buf;
	}// get
	
	/** Inserts a single newly unused buffer in the cache.
	 * 
	 * @param buf	The unused buffer.
	 */
	public void put(ByteBuffer buf) {
		
		/* Check that the buffer isn't already in the cache (prevent a duplicate
		 * insertion).
		 */
		for (ByteBuffer b : buffers) {
			if (buf == b)								// Same instance found
				return;
		}// for
		
		// Insert in cache
		buf.clear();									// Clear the buffer
		buffers.add(buf);								// Put it in collection
	}// put single buffer
}
