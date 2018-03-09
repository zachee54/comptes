/*
 * Copyright (c) 2018 Olivier HAAS - Tous droits réservés
 */
package haas.olivier.util.xml;

import javax.xml.namespace.NamespaceContext;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

/** A decorator for XML StAX writers that adds an indent to write more
 * human-readable output.
 * 
 * @author Olivier HAAS
 */
public class IndentedStAXWriter implements XMLStreamWriter {

	/** The text to display for each indentation. */
	private static final String INDENT = "  ";			// Double space
	
	/** Line break character. */
	private static final String LF = System.getProperty("line.separator");
	
	/** Delegate XML StAX writer. */
	private final XMLStreamWriter writer;
	
	/** Flag indicating if the actual node is a leaf.<br>
	 * Leaves are inlined and don't need line break neither indent before their
	 * closing brackets.
	 */
	private boolean leaf = true;
	
	/** Actual level of indentation. */
	private int level = 0;
	
	/** Constructs a decorator for StAX writer that adds an indent.
	 * 
	 * @param writer	The delegate XML StAX writer.
	 */
	public IndentedStAXWriter(XMLStreamWriter writer) {
		this.writer = writer;
	}// constructor
	
	/** Writes a line break and an indent, according to the actual level. */
	private void writeIndent() throws XMLStreamException {
		writer.writeCharacters(LF);					// Line break
		for (int n=0; n<level; n++)
			writer.writeCharacters(INDENT);			// Indent
	}// writeIndent
	
	/** This method is used when creating a new node.
	 * <p>
	 * Writes a line break and an indent according to the actual level, and
	 * increment the level. The actual node is then supposed to be a leaf.
	 */
	private void addIndent() throws XMLStreamException {
		writeIndent();
		level++;
		leaf = true;
	}// addIndent
	
	/** This method is used when going back when ending a node.
	 * <p>
	 * Writes a line beak and an indent, only if the actual is not a leaf. Then
	 * decrement the level and says that the new node is not a leaf.
	 * 
	 * @throws XMLStreamException
	 */
	private void removeIndent() throws XMLStreamException {
		level--;
		if (!leaf)
			writeIndent();
		leaf = false;
	}// removeIndent
	
	@Override
	public void writeStartElement(String localName) throws XMLStreamException {
		addIndent();
		writer.writeStartElement(localName);
	}// writeStartElement

	@Override
	public void writeStartElement(String namespaceURI, String localName)
			throws XMLStreamException {
		addIndent();
		writer.writeStartElement(namespaceURI, localName);
	}// writeStartElement

	@Override
	public void writeStartElement(String prefix, String localName,
			String namespaceURI) throws XMLStreamException {
		addIndent();
		writer.writeStartElement(prefix, localName, namespaceURI);
	}// writeStartElement

	@Override
	public void writeEmptyElement(String namespaceURI, String localName)
			throws XMLStreamException {
		writeIndent();
		writer.writeEmptyElement(namespaceURI, localName);
	}// writeEmptyElement

	@Override
	public void writeEmptyElement(String prefix, String localName,
			String namespaceURI) throws XMLStreamException {
		writeIndent();
		writer.writeEmptyElement(prefix, localName, namespaceURI);
	}// writeEmptyElement

	@Override
	public void writeEmptyElement(String localName) throws XMLStreamException {
		writeIndent();
		writer.writeEmptyElement(localName);
	}// writeEmptyElement

	@Override
	public void writeEndElement() throws XMLStreamException {
		removeIndent();
		writer.writeEndElement();
	}// writeEndElement

	@Override
	public void writeEndDocument() throws XMLStreamException {
		removeIndent();
		writer.writeEndDocument();
	}// writeEndDocument

	@Override
	public void close() throws XMLStreamException {
		writer.close();
	}// close

	@Override
	public void flush() throws XMLStreamException {
		writer.flush();
	}// flush

	@Override
	public void writeAttribute(String localName, String value)
			throws XMLStreamException {
		writer.writeAttribute(localName, value);
	}// writeAttribute

	@Override
	public void writeAttribute(String prefix, String namespaceURI,
			String localName, String value) throws XMLStreamException {
		writer.writeAttribute(prefix, namespaceURI, localName, value);
	}// writeAttribute

	@Override
	public void writeAttribute(String namespaceURI, String localName,
			String value) throws XMLStreamException {
		writer.writeAttribute(namespaceURI, localName, value);
	}// writeAttribute

	@Override
	public void writeNamespace(String prefix, String namespaceURI)
			throws XMLStreamException {
		writer.writeNamespace(prefix, namespaceURI);
	}// writeNamespace

	@Override
	public void writeDefaultNamespace(String namespaceURI)
			throws XMLStreamException {
		writer.writeDefaultNamespace(namespaceURI);
	}// writeDefaultNamespace

	@Override
	public void writeComment(String data) throws XMLStreamException {
		writeIndent();
		writer.writeComment(data);
	}// writeComment

	@Override
	public void writeProcessingInstruction(String target)
			throws XMLStreamException {
		writeIndent();
		writer.writeProcessingInstruction(target);
	}// writeProcessingInstuction

	@Override
	public void writeProcessingInstruction(String target, String data)
			throws XMLStreamException {
		writeIndent();
		writer.writeProcessingInstruction(target, data);
	}// writeProcessingInstruction

	@Override
	public void writeCData(String data) throws XMLStreamException {
		writer.writeCData(data);
	}// writeCData

	@Override
	public void writeDTD(String dtd) throws XMLStreamException {
		writer.writeDTD(dtd);
	}// writeDTD

	@Override
	public void writeEntityRef(String name) throws XMLStreamException {
		writer.writeEntityRef(name);
	}// writeEntityRef

	@Override
	public void writeStartDocument() throws XMLStreamException {
		writer.writeStartDocument();
	}// writeStartDocument

	@Override
	public void writeStartDocument(String version) throws XMLStreamException {
		writer.writeStartDocument(version);
	}// writeStartDocument

	@Override
	public void writeStartDocument(String encoding, String version)
			throws XMLStreamException {
		writer.writeStartDocument(encoding, version);
	}// wroteStartDocument

	@Override
	public void writeCharacters(String text) throws XMLStreamException {
		writer.writeCharacters(text);
	}// writeCharacters

	@Override
	public void writeCharacters(char[] text, int start, int len)
			throws XMLStreamException {
		writer.writeCharacters(text, start, len);
	}// writeCharacters

	@Override
	public String getPrefix(String uri) throws XMLStreamException {
		return writer.getPrefix(uri);
	}// getPrefix

	@Override
	public void setPrefix(String prefix, String uri) throws XMLStreamException {
		writer.setPrefix(prefix, uri);
	}// setPrefix

	@Override
	public void setDefaultNamespace(String uri) throws XMLStreamException {
		writer.setDefaultNamespace(uri);
	}// setDefaultNamespace

	@Override
	public void setNamespaceContext(NamespaceContext context)
			throws XMLStreamException {
		writer.setNamespaceContext(context);
	}// setNamespaceContext

	@Override
	public NamespaceContext getNamespaceContext() {
		return writer.getNamespaceContext();
	}// getNamespaceContext

	@Override
	public Object getProperty(String name) throws IllegalArgumentException {
		return writer.getProperty(name);
	}// getProperty

}
