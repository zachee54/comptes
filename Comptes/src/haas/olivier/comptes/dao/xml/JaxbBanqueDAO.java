package haas.olivier.comptes.dao.xml;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.List;
import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.xml.sax.SAXException;

import haas.olivier.comptes.dao.xml.jaxb.banq.Banque;
import haas.olivier.comptes.dao.xml.jaxb.banq.Banques;
import haas.olivier.util.ReadOnlyIterator;

/** Une classe d'accès aux données des banques, au format XML.
 * <p>
 * Cette classe utilise le framework JAXB.
 *
 * @author Olivier HAAS
 */
public class JaxbBanqueDAO
extends ReadOnlyIterator<haas.olivier.comptes.Banque> {

	/** Sauvegarde des banques vers un flux XML.
	 * 
	 * @param it	Un itérateur des banques à écrire.
	 * @param out	Le flux XML.
	 * 
	 * @throws IOException
	 */
	public static void save(Iterator<haas.olivier.comptes.Banque> it,
			OutputStream out) throws IOException {
		
		// Objet racine de l'arbre XML
		Banques banques = new Banques();
		List<Banque> listBanque = banques.getBanque();
		
		// Construire une instance pour chaque banque
		while (it.hasNext())
			listBanque.add(prepareBanque(it.next()));
		
		// Sérialiser vers XML
		try {
			// Créer le Marshaller
			Marshaller marshaller =
					JAXBContext.newInstance(
							JaxbBanqueDAO.class.getPackage().getName()
							+ ".jaxb.banq")
					.createMarshaller();
			
			// Demander une indentation du code produit
			marshaller.setProperty("jaxb.formatted.output", true);
			
			// Définir le schéma
			marshaller.setSchema(getSchema());
			
			// Écrire le flux
			marshaller.marshal(banques, out);
			
		} catch (Exception e) {
			throw new IOException(
					"Impossible d'écrire les banques", e);
		}// try
	}// save
	
	/** Renvoie le schéma XML des banques. 
	 * 
	 * @throws SAXException
	 */
	private static Schema getSchema() throws SAXException {
		return SchemaFactory.newInstance(
				XMLConstants.W3C_XML_SCHEMA_NS_URI)
				.newSchema(new StreamSource(
						JaxbBanqueDAO.class
						.getResourceAsStream("banques.xsd")));
	}// getSchema
	
	/** Instancie un objet d'une classe JAXB représentant une banque.
	 * 
	 * @param b	La banque à représenter.
	 * 
	 * @return	Une instance d'une classe JAXB.
	 */
	private static Banque prepareBanque(haas.olivier.comptes.Banque b) {
		Banque banque = new Banque();
		banque.setId(b.id);
		banque.setNom(b.nom);
		banque.setValue(b.getBytes());
		return banque;
	}// prepareBanque
	
	/** L'itérateur parcourant les objets JAXB désérialisés. */
	private final Iterator<Banque> it;
	
	/** Construit un objet d'accès aux données des banques au format XML.
	 * 
	 * @param in	Un flux XML contenant les données des banques.
	 * 
	 * @throws IOException
	 */
	public JaxbBanqueDAO(InputStream in) throws IOException {
		
		// Lire entièrement le flux XML
		try {
			// Le Unmarshaller
			Unmarshaller unmarshaller =
					JAXBContext.newInstance(
							JaxbBanqueDAO.class.getPackage().getName()
							+ ".jaxb.banq")
					.createUnmarshaller();
			
			// Spécifier le schéma
			unmarshaller.setSchema(getSchema());
			
			// Désérialiser
			Banques banques = (Banques) unmarshaller.unmarshal(in);
			
			// Définir l'itérateur
			it = banques.getBanque().iterator();
			
		} catch (Exception e) {
			throw new IOException(
					"Impossible de lire les banques", e);
		}// try
	}// constructeur

	@Override
	public boolean hasNext() {
		return it.hasNext();
	}// hasNext

	@Override
	public haas.olivier.comptes.Banque next() {
		Banque banque = it.next();
		return new haas.olivier.comptes.Banque(
				banque.getId(), banque.getNom(), banque.getValue());
	}// next
}
