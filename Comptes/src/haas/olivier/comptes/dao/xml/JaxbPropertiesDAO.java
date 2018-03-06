package haas.olivier.comptes.dao.xml;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.xml.sax.SAXException;

import haas.olivier.comptes.dao.PropertiesDAO;
import haas.olivier.comptes.dao.cache.CacheablePropertiesDAO;
import haas.olivier.comptes.dao.xml.jaxb.props.Diagram;
import haas.olivier.comptes.dao.xml.jaxb.props.Diagram.Serie;
import haas.olivier.comptes.dao.xml.jaxb.props.Properties;
import haas.olivier.comptes.dao.xml.jaxb.props.Properties.Diagrams;
import haas.olivier.diagram.DiagramMemento;

/** Une classe d'accès aux propriétés, au format XML.
 * <p>
 * Cette classe utilise le framework JAXB.
 *
 * @author Olivier HAAS
 */
public class JaxbPropertiesDAO implements CacheablePropertiesDAO {

	/** Sauvegarde des propriétés vers un flux XML.
	 * 
	 * @param propsDAO	L'objet d'accès aux propriétés à écrire.
	 * @param out		Le flux XML.
	 * 
	 * @throws IOException
	 */
	public static void save(PropertiesDAO propsDAO, OutputStream out)
			throws IOException {
		
		// Objet racine de l'arbre XML
		Properties properties = new Properties();
		properties.setDiagrams(createDiagrams(propsDAO));
		
		// Sérialiser vers XML
		try {
			// Créer le Marshaller
			Marshaller marshaller =
					JAXBContext.newInstance(
							JaxbPropertiesDAO.class.getPackage().getName()
							+ ".jaxb.props")
					.createMarshaller();
			
			// Demander une indentation du code produit
			marshaller.setProperty("jaxb.formatted.output", true);
			
			// Définir le schéma
			marshaller.setSchema(getSchema());
			
			// Écrire le flux
			marshaller.marshal(properties, out);
			
		} catch (Exception e) {
			throw new IOException(
					"Impossible d'écrire les propriétés", e);
		}// try
	}// save
	
	/** Renvoie le schéma XML des propriétés. 
	 * 
	 * @throws SAXException
	 */
	private static Schema getSchema() throws SAXException {
		return SchemaFactory.newInstance(
				XMLConstants.W3C_XML_SCHEMA_NS_URI)
				.newSchema(new StreamSource(
						JaxbPropertiesDAO.class
						.getResourceAsStream("properties.xsd")));
	}// getSchema
	
	/** Instancie les objets JAXB représentant les propriétés des diagrammes.
	 * 
	 * @param propsDAO	L'objet d'accès aux propriétés.
	 * @return			Les propriétés des diagrammes.
	 */
	private static Diagrams createDiagrams(PropertiesDAO propsDAO) {
		Diagrams diagrams = new Diagrams();
		List<Diagram> diagramList = diagrams.getDiagram();
		for (String name : propsDAO.getDiagramNames()) {
			Diagram diagramProps =
					prepareDiagram(propsDAO.getDiagramProperties(name));
			diagramProps.setName(name);
			diagramList.add(diagramProps);
		}// for
		return diagrams;
	}// createDiagrams
	
	/** Instancie un objet d'une classe JAXB représentant les propriétés d'un
	 * diagramme.
	 * 
	 * @param memento	Un memento des propriétés du diagramme.
	 * 
	 * @return			Une instance d'une classe JAXB.
	 */
	private static Diagram prepareDiagram(DiagramMemento memento) {
		Diagram diagramProps = new Diagram();
		List<Serie> seriesProps = diagramProps.getSerie();
		for (Integer id : memento.getSeries()) {
			Diagram.Serie serieProps = new Diagram.Serie();
			serieProps.setValue(id);
			serieProps.setHidden(memento.isHidden(id));
			seriesProps.add(serieProps);
		}// for
		return diagramProps;
	}// prepareBanque
	
	/** Les propriétés au format JAXB désérialisés. */
	private final Properties properties;
	
	/** Construit un objet d'accès aux propriétés au format XML.
	 * 
	 * @param in	Un flux XML contenant les propriétés.
	 * 
	 * @throws IOException
	 */
	public JaxbPropertiesDAO(InputStream in) throws IOException {
		
		// Lire entièrement le flux XML
		try {
			// Le Unmarshaller
			Unmarshaller unmarshaller =
					JAXBContext.newInstance(
							JaxbPropertiesDAO.class.getPackage().getName()
							+ ".jaxb.props")
					.createUnmarshaller();
			
			// Spécifier le schéma
			unmarshaller.setSchema(getSchema());
			
			// Désérialiser
			properties = (Properties) unmarshaller.unmarshal(in);
			
		} catch (Exception e) {
			throw new IOException("Impossible de lire les propriétés", e);
		}// try
	}// constructeur
	
	@Override
	public Map<String, DiagramMemento> getDiagramProperties() {
		Map<String, DiagramMemento> mementos = new HashMap<>();
		for (Diagram diagramProps : properties.getDiagrams().getDiagram())
			mementos.put(diagramProps.getName(), getMemento(diagramProps));
		return mementos;
	}// getDiagramProperties
	
	/** Instancie un memento des propriétés d'un diagramme à partir de la classe
	 * JAXB désérialisée.
	 * 
	 * @param diagramProps	La classe JAXB des propriétés d'un diagramme
	 * 						désérialisée.
	 * 
	 * @return				Un POJO.
	 */
	private DiagramMemento getMemento(Diagram diagramProps) {
		List<Integer> ids = new ArrayList<>();
		Set<Integer> hidden = new HashSet<>();
		for (Serie serie : diagramProps.getSerie()) {
			int id = serie.getValue();
			ids.add(id);
			if (serie.isHidden())
				hidden.add(id);
		}
		return new DiagramMemento(diagramProps.getName(), ids, hidden);
	}// getMemento
}
