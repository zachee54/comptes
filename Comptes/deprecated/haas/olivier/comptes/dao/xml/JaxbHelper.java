package haas.olivier.comptes.dao.xml;

import java.io.OutputStream;
import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.xml.sax.SAXException;

/** Un assistant JAXB.<br>
 * Il s'agit d'une classe de confort.
 * 
 * @author Olivier HAAS
 */
class JaxbHelper {

	/** Le nom du paquet contenant les classes JAXB. */
	private final String jaxbPackage;
	
	/** Le nom du fichier contenant le schéma XML. */
	private final String schemaName;
	
	/** Construit un assistant JAXB.
	 * 
	 * @param jaxbPackage	Le nom du package contenant les classes JAXB.
	 * @param schemaName	Le nom du fichier contenant le schéma XML.
	 */
	JaxbHelper(String jaxbPackage, String schemaName) {
		this.jaxbPackage = jaxbPackage;
		this.schemaName = schemaName;
	}// constructeur
	
	/** Sérialise un objet en utilisant JAXB.
	 * 
	 * @param obj	L'objet à écrire.
	 * @param out	Le flux XML.
	 * 
	 * @throws JAXBException
	 * 				En cas d'erreur lors de l'initialisation, la configuration
	 * 				ou l'exécution de la sauvegarde JAXB.
	 * 
	 * @throws SAXException
	 * 				En cas d'erreur lors de la récupération du schéma.
	 */
	void save(Object obj, OutputStream out) throws JAXBException, SAXException {
		
		// Créer le Marshaller
		Marshaller marshaller =
				JAXBContext.newInstance(jaxbPackage)
				.createMarshaller();

		// Demander une indentation du code produit
		marshaller.setProperty("jaxb.formatted.output", true);

		// Définir le schéma
		marshaller.setSchema(getSchema());

		// Écrire le flux
		marshaller.marshal(obj, out);
	}// save
	
	/** Renvoie le schéma à utiliser. */
	private Schema getSchema() throws SAXException {
		return SchemaFactory.newInstance(
				XMLConstants.W3C_XML_SCHEMA_NS_URI)
				.newSchema(new StreamSource(
						getClass().getResourceAsStream(schemaName)));
	}// getSchema
}
