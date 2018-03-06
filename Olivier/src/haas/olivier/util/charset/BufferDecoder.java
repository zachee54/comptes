/*
 * Copyright (c) 2018 Olivier HAAS - Tous droits réservés
 */
package haas.olivier.util.charset;

import java.io.Closeable;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CoderResult;
import java.util.LinkedList;
import java.util.Queue;

/** An object that decodes one or many byte buffers in one pass with any given
 * charset.
 * <p>
 * The buffers are decoded successively until the output buffer is full, the end
 * of input has been reached, or an error occurs.
 * <p>
 * The class implements a State pattern, either allowing or not mark and reset
 * operations.<br>
 * At first, the state allows mark and reset.<br>
 * It can be set to an unresetable state at any time. Afterwards, the state
 * cannot be turned back to the resetable state.
 *
 * @author Olivier HAAS
 */
class BufferDecoder implements Closeable {

	/** The buffer sequence containing the buffers to read. */
	private final BufferSequence buffers;

	/** The buffers that have been fully read since the last mark.
	 * <p>
	 * The mark lies on the head buffer.
	 */
	private final Queue<ByteBuffer> marked = new LinkedList<ByteBuffer>();
	
	/** The actual state, resetable or not, according to the possibility to
	 * invoke mark and reset operations.
	 * <p>
	 * At first, the state is the resetable one.
	 */
	private BufferState state = new ResetableBufferState();
	
	/** Constructs a byte buffers decoder, decoding one or many byte buffers in
	 * one pass with any given charset.
	 * 
	 * @param buffers	The buffer sequence to read.
	 */
	public BufferDecoder(BufferSequence buffers) {
		this.buffers = buffers;
	}// constructor
	
	/** Returns the next bytes.
	 * <p>
	 * This method makes the cursor go forward of <code>len</code> bytes, so
	 * that the returned bytes won't be decoded unless the caller performs a
	 * mark-reset operation.
	 * 
	 * @param len	The number of bytes to read.
	 * 
	 * @return		An byte array of length <code>len</code>. If the remaining
	 * 				bytes in the stream are less than <code>len</code>, the end
	 * 				of the array won't be written (let to default value at array
	 * 				creation).
	 * 
	 * @throws IOException
	 * 				If an error occurs while reading the underlying input
	 * 				stream.
	 */
	public byte[] getBytes(int len) throws IOException {
		byte[] bytes = new byte[len];					// Result array
		
		// Fill result array
		ByteBuffer buf = buffers.getBuffer();			// Actual buffer to read
		for (int i=0; i<len; i++) {
			
			// Look for a buffer with remaining bytes
			while (buf != null && !buf.hasRemaining()) {// Actual buffer at end
				state.bufferDeprecated(buf);			// Deprecate it
				buf = buffers.nextBuffer();				// Get the next one
			}// while
			
			// If no more buffer
			if (buf == null)
				return bytes;							// Return as it is
			
			// Fill the array with the next byte
			bytes[i] = buf.get();
		}// for
		
		return bytes;
	}// getBytes
	
	/** Decodes bytes from the actual position until the char buffer is full,
	 * the end of stream is reached or an error occurs.
	 * 
	 * @param outBuf	The destination char buffer.
	 * @param decoder	The decoder-flusher to use.
	 * 
	 * @return			The result of the last decoding operation.
	 * 
	 * @throws IOException
	 * 					If an error occurs while reading the underlying input
	 * 					stream.
	 */
	public CoderResult decode(CharBuffer outBuf, CharsetDecoderFlusher decoder)
			throws IOException {
		CoderResult result = null;					// Future decoding result
		ByteBuffer buf = buffers.getBuffer();		// Actual buffer
		
		while (true) {
			
			// Produce chars (decoding while buffer isn't null, else flushing)
			result = decoder.decode(buf, outBuf);
			
			// Examine decoder termination cause
			if (buf == null && !result.isOverflow()) {
				/* No more buffer to read : this was a flush work (end of input
				 * stream), and the flush was completed (no overflow)
				 */
				return CoderResult.UNDERFLOW;		// Nothing to write any more
				
			} else if (result.isUnderflow()) {
				// Actual buffer fully read : step to next one
				state.bufferDeprecated(buf);		// Deprecate actual buffer
				buf = buffers.nextBuffer();			// Get next one
				
			} else {
				// Overflow (i.e. char buffer full) or error
				return result;						// Method process over
			}// if
		}// while
	}// decode
	
	/** Marks the actual position in order to permit reset operation, if the
	 * object is still in resetable state. Else, throws an
	 * <code>UnsupportedOperationException</code> and doesn't do anything.
	 * <p>
	 * In resetable state, all bytes read from this mark will be kept in memory,
	 * until the method is invoked again.<br>
	 * All buffers that have already been fully read are also discarded.
	 * 
	 * @throws IOException
	 * 			If an error occurs while reading the byte stream. Although, this
	 * 			should never happen from this method.
	 * 
	 * @throws UnsupportedOperationException
	 * 			If the actual state doesn't allow any longer mark and reset
	 * 			operations.
	 */	
	public void mark() throws IOException {
		state.mark();
	}// mark
	
	/** Goes back to the last mark operation, if the object is still in
	 * resetable state. Else, throws en
	 * <code>UnsupportedOperationException</code> and doesn't do anything.
	 * <p>
	 * After the execution of this method while resetable state, the buffer
	 * which was being read during the last mark operation will be decoded
	 * again by the next invocation of
	 * {@link #decode(CharBuffer, CharsetDecoder)}, at the position it was
	 * during the last mark operation.<br>
	 * All buffers read since this time will also be returned afterwards, then
	 * the actual buffer and its following.
	 * 
	 * @throws IOException
	 * 			If an error occurs while reading the byte stream. Although, this
	 * 			should never happen from this method.
	 * 
	 * @throws UnsupportedOperationException
	 * 			If the state doesn't allow mark and reset operations.
	 */	
	public void reset() throws IOException {
		state.reset();
	}// reset
	
	/** Turn the state to unresetable.
	 * <p>
	 * All buffers read since the last mark operation, if any, will be
	 * discarded.<br>
	 * Mark and reset operations will no longer be supported.
	 */
	public void setUnresetable() {
		
		// Turn the state to unresetable
		state = new UnresetableBufferState();
		
		// Discard obsolete buffers
		discardMarkedBuffers();
	}// setUnresetable
	
	/** Discards all buffers read since last mark operation, if any. */
	private void discardMarkedBuffers() {
		buffers.deprecate(marked);						// Put them in cache
		marked.clear();									// Forget them
	}// discardMarkedBuffers
	
	@Override
	public void close() throws IOException {
		buffers.close();
	}// close

	/** The interface of both possible states, according to the possibility to
	 * mark and reset the buffers.
	 *
	 * @author Olivier HAAS
	 */
	private interface BufferState {

		/** Receives a deprecated buffer after it was fully read.
		 * <p>
		 * Concrete implementations may reuse the buffer, either by keeping it
		 * available in order to be able to re-read it, or by putting it in the
		 * buffer cache. They could also don't do anything, making it
		 * potentially eligible to garbage collection.
		 * 
		 * @param buf	The buffer to tear down.
		 */
		void bufferDeprecated(ByteBuffer buf);
		
		/** Marks the actual position in order to permit reset operation
		 * (optional operation).
		 * <p>
		 * All bytes read from this mark will be kept in memory, until the
		 * method is invoked again.<br>
		 * All buffers that have already been fully read are discarded.
		 * 
		 * @throws IOException
		 * 			If an error occurs while reading the byte stream. Although,
		 * 			this should never happen from this method.
		 * 
		 * @throws UnsupportedOperationException
		 * 			If the state doesn't allow mark and reset operations.
		 */
		void mark() throws IOException;
		
		/** Goes back to the last mark operation (optional operation).
		 * <p>
		 * After the execution of this method, the buffer which was being read
		 * during the last mark operation will be decoded again by the next
		 * invocation of
		 * {@link BufferDecoder#decode(CharBuffer, CharsetDecoder)}, at the
		 * position it was during the last mark operation.<br>
		 * All buffers read since this time will be returned afterwards, then
		 * the actual buffer and its following.
		 * 
		 * @throws IOException
		 * 			If an error occurs while reading the byte stream. Although,
		 * 			this should never happen from this method.
		 * 
		 * @throws UnsupportedOperationException
		 * 			If the state doesn't allow mark and reset operations.
		 */
		void reset() throws IOException;
	}// private interface BufferState
	
	/** The state allowing mark and reset operations.
	 * 
	 * @author Olivier HAAS
	 */
	private class ResetableBufferState implements BufferState {

		/** Keeps the deprecated buffer available in order to be able to re-read
		 * it.
		 */
		@Override
		public void bufferDeprecated(ByteBuffer buf) {
			marked.add(buf);
		}// bufferDeprecated
		
		/** Marks the actual position in order to permit reset operation.
		 * <p>
		 * All bytes read from this mark will be kept in memory, until the
		 * method is invoked again.<br>
		 * All buffers that have already been fully read are discarded.
		 * 
		 * @throws IOException
		 * 			If an error occurs while reading the byte stream. Although,
		 * 			this should never happen from this method.
		 */
		@Override
		public void mark() throws IOException {
			
			// Mark actual buffer
			ByteBuffer buf = buffers.getBuffer();
			if (buf != null)
				buf.mark();
			
			// Discard obsolete buffers
			discardMarkedBuffers();
		}// mark
		
		/** Goes back to the last mark operation.
		 * <p>
		 * After the execution of this method, the buffer which was being read
		 * during the last mark operation will be decoded again by the next
		 * invocation of
		 * {@link BufferDecoder#decode(CharBuffer, CharsetDecoder)}, at the
		 * position it was during the last mark operation.<br>
		 * All buffers read since this time will be returned afterwards, then
		 * the actual buffer and its following.
		 * 
		 * @throws IOException
		 * 			If an error occurs while reading the byte stream. Although,
		 * 			this should never happen from this method.
		 */
		@Override
		public void reset() throws IOException {
			
			// Reinsert marked buffers at head of the sequence
			buffers.insert(marked);
			
			// Clear the marked collection
			marked.clear();
			
			// Reset new actual buffer to its own mark
			ByteBuffer buf = buffers.getBuffer();
			if (buf != null)	// Might be null, especially if empty source
				buf.reset();
		}// reset
	}// private inner class ResetableBufferState
	
	/** The state forbidding mark and reset operations.
	 * 
	 * @author Olivier HAAS
	 */
	private class UnresetableBufferState implements BufferState {

		/** Puts the deprecated buffer in buffer cache. */
		@Override
		public void bufferDeprecated(ByteBuffer buf) {
			buffers.deprecate(buf);
		}// bufferDeprecated

		/** Throws an <code>UnsupportedOperationException</code>. */
		@Override
		public void mark() throws IOException {
			throw new UnsupportedOperationException();
		}// mark

		/** Throws an <code>UnsupportedOperationException</code>. */
		@Override
		public void reset() throws IOException {
			throw new UnsupportedOperationException();
		}// reset
	}// private inner class UnresetableBufferState
}
