package haas.olivier.comptes.dao.csv;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.XMLConstants;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.SchemaFactory;

import haas.olivier.comptes.Compte;
import haas.olivier.comptes.CompteBancaire;
import haas.olivier.comptes.Permanent;
import haas.olivier.comptes.PermanentFixe;
import haas.olivier.comptes.PermanentProport;
import haas.olivier.comptes.PermanentSoldeur;
import haas.olivier.comptes.dao.CompteDAO;
import haas.olivier.comptes.dao.cache.CachePermanentDAO;
import haas.olivier.util.Month;
import haas.olivier.util.ReadOnlyIterator;
import haas.olivier.util.xml.IndentedStAXWriter;
import haas.olivier.util.xml.StAXWalker;

/** Un objet d'accès aux opérations permanentes au format XML.
 * 
 * @author Olivier HAAS
 */
class StAXPermanentDAO extends ReadOnlyIterator<Permanent> {
	
	/** Encodage utilisé. */
	private static final String ENCODING = "UTF-8";
	
	/** L'espace de noms du schéma. */
	private static final String NS = "urn:haas.olivier.comptes.permanents";
	
	/** Sauvegarde des opérations permanentes dans un flux XML.
	 * 
	 * @param permanents	Un itérateur parcourant les opérations permanentes à
	 * 						écrire.
	 * 
	 * @param out			Le flux de sortie.
	 * 
	 * @throws FactoryConfigurationError
	 * @throws XMLStreamException
	 */
	static void save(Iterator<Permanent> permanents, OutputStream out)
			throws XMLStreamException, FactoryConfigurationError {
		
		// Initialiser le flux XML
		XMLStreamWriter xmlOut = new IndentedStAXWriter(
				XMLOutputFactory.newFactory().createXMLStreamWriter(
						out, ENCODING));
		
		// Écrire le début du document
		xmlOut.writeStartDocument(ENCODING, "1.0");	// Début du document
		xmlOut.setPrefix("perm", NS);				// Préfixe général
		xmlOut.setPrefix("xsi",						// Préfixe W3C schéma inst.
				XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI);
		
		// Écrire la balise racine
		xmlOut.writeStartElement(NS, "permanents");	// Ouverture balise racine
		xmlOut.writeAttribute("xmlns:xsi",			// Déclarer schema instance
				XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI);
		xmlOut.writeNamespace("perm", NS);			// Déclarer l'espace de noms
		xmlOut.writeAttribute(						// Déclarer le schéma
				XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI,
				"schemaLocation", NS + " permanents.xsd");
		
		// Écrire chaque opération permanente
		while (permanents.hasNext())
			writePermanent(permanents.next(), xmlOut);
		
		// Écrire la fin du document
		xmlOut.writeEndElement();
		xmlOut.writeEndDocument();
		xmlOut.flush();
	}// save
	
	/** Écrit une opération permanente dans le flux XML.
	 * 
	 * @param p		L'opération à écrire.
	 * @param out	Le flux XML
	 * 
	 * @throws XMLStreamException
	 */
	static void writePermanent(Permanent p, XMLStreamWriter out)
			throws XMLStreamException {
		out.writeStartElement(NS, "permanent");		// Balise ouvrante
		out.writeAttribute("id", p.id.toString());	// Identifiant
		out.writeAttribute("nom", p.nom);			// Nom
		
		// Libellé
		writeSimpleXmlElement(out, "libelle", p.libelle);
		
		// Tiers
		writeSimpleXmlElement(out, "tiers", p.tiers);
		
		// Débit
		writeSimpleXmlElement(out, "debit", p.debit.getId().toString());
		
		// Crédit
		writeSimpleXmlElement(out, "credit", p.credit.getId().toString());
		
		// Pointage
		writeSimpleXmlElement(out, "pointage", p.pointer ? "true" : "false");
		
		// Jours
		out.writeStartElement(NS, "jours");			// Balise ouvrante jours
		for (Entry<Month, Integer> jour : p.jours.entrySet()) {
			out.writeStartElement(NS, "jour");		// Balise ouvrante 1 jour
			out.writeAttribute("annee",				// Attribut année
					Integer.toString(jour.getKey().getYear()));
			out.writeAttribute("mois",				// Attribut mois
					Integer.toString(jour.getKey().getNumInYear()));
			out.writeCharacters(					// n° du jour
					jour.getValue().toString());
			out.writeEndElement();					// Balise fermante 1 jour
		}// for jours
		out.writeEndElement();						// Balise fermante jours
		
		// Selon le type d'opération permanente
		if (p instanceof PermanentFixe) {			// Montants prédéfinis
			out.writeStartElement(NS, "montants");	// Début des montants
			
			for (Entry<Month, BigDecimal> entry :
				((PermanentFixe) p).montants.entrySet()) {
				
				Month month = entry.getKey();		// Le mois
				
				out.writeStartElement(NS, "montant");//Balise ouvrante montant
				out.writeAttribute("annee",			// Écrire l'année
						Integer.toString(month.getYear()));
				out.writeAttribute("mois",			// Écrire le mois
						Integer.toString(month.getNumInYear()));
				out.writeCharacters(				// Écrire le montant
						entry.getValue().toPlainString());
				out.writeEndElement();				// Balise fermante montant
			}// for montant
			out.writeEndElement();					// Balise fermante montants
			
		} else if (p instanceof PermanentProport) {	// Opération proportionnelle
			PermanentProport proport = (PermanentProport) p;
			
			out.writeEmptyElement(NS, "dependance");// Balise ouvrante dépendanc
			out.writeAttribute("id",				// Id de la dépendance
					Integer.toString(proport.dependance.id));
			out.writeAttribute("taux",				// Taux de la dépendance
					proport.taux.toPlainString());
		}// if
		
		out.writeEndElement();						// Balise fermante permanent
	}// writePermanent
	
	/** Écrit un élément XML de contenu simple.
	 * 
	 * @param out		Le flux XML dans lequel écrire.
	 * @param balise	Le nom de la balise de l'élément à écrire. Ce nom de
	 * 					balise est automatiquement préfixé de l'espace de noms
	 * 					{@link #NS}.
	 * @param text		Le texte de l'élément à écrire.
	 * @throws XMLStreamException
	 */
	static void writeSimpleXmlElement(XMLStreamWriter out, String balise,
			String text) throws XMLStreamException {
		out.writeStartElement(NS, balise);
		out.writeCharacters(text);
		out.writeEndElement();
	}// writeSimpleXmlElement
	
	/** L'objet qui parcourt le flux StAX. */
	private final StAXWalker walker;
	
	/** Le cache des opérations permanentes. */
	private final CachePermanentDAO cache;
	
	/** Un objet d'accès aux comptes. */
	private final CompteDAO comptes;
	
	/** Construit un objet d'accès aux opérations permanentes lisant le flux
	 * spécifié au format XML.
	 * 
	 * @param in		Le flux XML contenant les opérations permanentes à lire.
	 * @param cache		Un cache des opérations permanentes, utilisé pour
	 * 					récupérer des opérations déjà lues.
	 * @param comptes	Un objet d'accès aux comptes.
	 * 
	 * @throws IOException
	 */
	StAXPermanentDAO(InputStream in, CachePermanentDAO cache, CompteDAO comptes)
			throws IOException {
		this.cache = cache;
		this.comptes = comptes;
		
		ByteArrayOutputStream memOut = null;			// Écriture mémoire
		ByteArrayInputStream memIn = null;				// Relecture mémoire
		InputStream schemaIn = null;					// Lecture du schéma
		try {
			// Stocker le flux en mémoire
			memOut = new ByteArrayOutputStream();		// Bytes vers la mémoire
			int c;
			while ((c = in.read()) != -1)				// Transférer le flux
				memOut.write(c);
			
			// Récupérer le schéma embarqué dans l'application
			schemaIn = getClass().getResourceAsStream("permanents.xsd");
			
			// Valider le flux XML stocké en mémoire à l'aide du schéma embarqué
			memIn = new ByteArrayInputStream(			// Relecture mémoire
					memOut.toByteArray());
			SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI)
			.newSchema(new StreamSource(schemaIn))		// Schéma XSD
			.newValidator()								// Valideur du schéma
			.validate(new StreamSource(memIn));			// Valider le document
			
			// Instancier un parseur StAX et son walker
			walker = new StAXWalker(
					XMLInputFactory.newFactory().createXMLStreamReader(
							new ByteArrayInputStream(memOut.toByteArray())));
			
			// Positionner le curseur sur la balise racine
			walker.walkToElement("permanents");
			
		} catch (Exception e) {
			throw new IOException(
					"Impossible d'ouvrir les opérations permanentes", e);
			
		} finally {
			if (schemaIn != null) schemaIn.close();		// Flux du schéma
			if (memIn != null) memIn.close();			// Flux mémoire sortant
			if (memOut != null) memOut.close();			// Flux mémoire entrant
			in.close();									// Flux entrant
		}// try
	}// constructeur
	
	@Override
	public boolean hasNext() {
		try {
			// Marcher jusqu'au prochain et dire si on l'a trouvé
			return walker.walkToElementIn("permanent", "permanents");
			
		} catch (XMLStreamException e) {
			Logger.getLogger(getClass().getName()).log(
					Level.SEVERE,
					"Une erreur est survenue pendant la lecture des " +
					"opérations permanentes", e);
			
			// Par défaut, arrêter l'itération
			return false;
		}// try
	}// hasNext

	@Override
	public Permanent next() {
		
		// Veiller à ce qu'on ne soit pas à la fin du document
		if (!hasNext())
			throw new NoSuchElementException("Fin des opérations permanentes");
		
		int id = -1;	// Initialisation nécessaire pour le compilateur
		try {
			// Lire les attributs
			id = walker.getNumericAttribute("id");
			String nom = walker.getAttribute("nom");
			
			// Autres caractéristiques de l'opération à instancier
			boolean pointage = false;
			String libelle = null, tiers = null;
			Compte debit = null, credit = null;
			Map<Month, Integer> jours = null;
			Map<Month, BigDecimal> montants = null;
			Permanent depend = null;
			BigDecimal taux = null;
			
			// Lire les caractéristiques
			String element;					// Nom de l'élément en cours
			while ((element = walker.nextElementIn("permanent")) != null) {
				switch (element) {
				case "libelle":	libelle = walker.getSimpleText();	break;
				case "tiers":	tiers = walker.getSimpleText();		break;
				case "jours":	jours = readJours();				break;
				case "montants":
					montants = readMontants();
					break;
					
				case "dependance":
					depend = cache.get(					// Objet dont on dépend
							Integer.parseInt(walker.getAttribute("id")));
					taux = new BigDecimal(				// Taux à appliquer
							walker.getAttribute("taux"));
					break;
				
				case "debit":
					debit = comptes.get((int) walker.getSimpleNum());
					break;
					
				case "credit":
					credit = comptes.get((int) walker.getSimpleNum());
					break;
					
				case "pointage":
					pointage = walker.getSimpleText().equalsIgnoreCase("true");
					
				}// switch
			}// while
			
			// Instancier selon le type de données recueillies
			if (montants != null) {						// Montants prédéfinis
				return new PermanentFixe(id, nom, debit, credit, libelle,
						tiers, pointage, jours, montants);
				
			} else if (depend != null) {				// Dépendance
				return new PermanentProport(id, nom, debit, credit, libelle,
						tiers, pointage, jours, depend, taux);
				
			} else if (debit instanceof CompteBancaire) {//Soldeur
				return new PermanentSoldeur(id, nom, (CompteBancaire) debit,
						credit, libelle, tiers, pointage, jours);
			}// if
			
		} catch (Exception e) {
			throw new RuntimeException(
					"Impossible de lire les opérations permanentes", e);
		}// try
		
		// Problème
		throw new RuntimeException(
				"Impossible de déterminer le type de l'opération permanente n°"+
				id);
	}// next
	
	/** Lit une séquence de jours. 
	 * 
	 * @throws XMLStreamException
	 */
	private Map<Month, Integer> readJours() throws XMLStreamException {
		Map<Month, Integer> jours = new HashMap<>();
		
		// Lire chaque jour dans la séquence de jours
		while (walker.walkToElementIn("jour", "jours")) {
			
			// Ajouter à la collection
			jours.put(
					readMonth(),							// Mois en attributs
					Integer.parseInt(walker.getSimpleText()));// Numéro jour
		}// while
		
		return jours;
	}// readJours
	
	/** Lit une séquence de montants.
	 * 
	 * @throws XMLStreamException
	 */
	private Map<Month, BigDecimal> readMontants() throws XMLStreamException {
		Map<Month, BigDecimal> montants = new HashMap<>();
		
		// Lire chaque montant dans la séquence de jours
		while (walker.walkToElementIn("montant", "montants")) {
			
			// Ajouter à la collection
			montants.put(
					readMonth(),							// Mois en attributs
					new BigDecimal(walker.getSimpleText()));// Montant
		}// while
		
		return montants;
	}// readMontants
	
	/** Renvoie le mois calendaire défini par les attributs <code>"mois"</code>
	 * et <code>"annnee"</code> de l'élément actuel.
	 * 
	 * @return	Un mois défini dans l'élément XML actuel.
	 */
	private Month readMonth() {
		return new Month(
				walker.getNumericAttribute("annee"),
				walker.getNumericAttribute("mois"));
	}// readMonth
}
