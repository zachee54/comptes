/*
 * Copyright 2013-2018 Olivier HAAS. All rights reserved.
 */
package haas.olivier.comptes.dao.xml;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.xml.sax.SAXException;

import haas.olivier.comptes.Compte;
import haas.olivier.comptes.PermanentFixe;
import haas.olivier.comptes.PermanentProport;
import haas.olivier.comptes.PermanentSoldeur;
import haas.olivier.comptes.dao.cache.CachePermanentDAO;
import haas.olivier.comptes.dao.xml.jaxb.perm.Jours;
import haas.olivier.comptes.dao.xml.jaxb.perm.Jours.Jour;
import haas.olivier.comptes.dao.xml.jaxb.perm.Montants;
import haas.olivier.comptes.dao.xml.jaxb.perm.Montants.Montant;
import haas.olivier.comptes.dao.xml.jaxb.perm.Permanent;
import haas.olivier.comptes.dao.xml.jaxb.perm.Permanent.Dependance;
import haas.olivier.comptes.dao.xml.jaxb.perm.Permanents;
import haas.olivier.util.Month;
import haas.olivier.util.ReadOnlyIterator;

/**
 * Une classe d'accès aux données des opérations permanentes au format XML.
 * <p>
 * Cette classe utilise le framework JAXB.
 *
 * @author Olivier HAAS
 */
public class JaxbPermanentDAO
extends ReadOnlyIterator<haas.olivier.comptes.Permanent> {
	
	/**
	 * Sauvegarde des opérations permanentes vers un flux XML.
	 * 
	 * @param it			Un itérateur des opérations permanentes à écrire.
	 * @param out			Le flux XML.
	 * 
	 * @throws IOException
	 */
	public static void save(Iterator<haas.olivier.comptes.Permanent> it,
			OutputStream out)
					throws IOException {
		
		// Objet racine de l'arbre XML
		Permanents permanents = new Permanents();
		List<Permanent> listPermanents = permanents.getPermanent();
		
		// Construire une instance pour chaque opération permanente
		while (it.hasNext())
			listPermanents.add(preparePermanent(it.next()));
		
		// Sérialiser vers XML
		try {
			// Créer le Marshaller
			Marshaller marshaller =
					JAXBContext.newInstance(
							JaxbPermanentDAO.class.getPackage().getName()
							+ ".jaxb.perm")
					.createMarshaller();
			
			// Demander une indentation du code produit
			marshaller.setProperty("jaxb.formatted.output", true);
			
			// Définir le schéma
			marshaller.setSchema(getSchema());
			
			// Écrire le flux
			marshaller.marshal(permanents, out);
			
		} catch (Exception e) {
			throw new IOException(
					"Impossible d'écrire les opérations permanentes", e);
		}
	}
	
	/**
	 * Renvoie le schéma XML des opérations permanentes. 
	 * 
	 * @throws SAXException
	 */
	private static Schema getSchema() throws SAXException {
		return SchemaFactory.newInstance(
				XMLConstants.W3C_XML_SCHEMA_NS_URI)
				.newSchema(new StreamSource(
						JaxbPermanentDAO.class
						.getResourceAsStream("permanents.xsd")));
	}
	
	/**
	 * Instancie un objet d'une classe JAXB représentant une opération
	 * permanente.
	 * 
	 * @param p				L'opération permanente à représenter.
	 * 
	 * @return	Une instance d'une classe JAXB.
	 */
	private static Permanent preparePermanent(
			haas.olivier.comptes.Permanent p) {
		Permanent result = new Permanent();
		
		// Les caractéristiques générales de l'opération
		result.setId(p.id);
		result.setNom(p.nom);
		result.setLibelle(p.libelle);
		result.setTiers(p.tiers);
		result.setCredit(p.credit.getId());
		result.setDebit(p.debit.getId());
		result.setPointage(p.pointer);
		result.setJours(prepareJours(p.jours));
		
		// Selon le type d'opération
		if (p instanceof PermanentFixe) {
			result.setMontants(prepareMontants(((PermanentFixe) p).montants));
			
		} else if (p instanceof PermanentProport) {
			result.setDependance(prepareDependance(((PermanentProport) p)));
		}
		
		return result;
	}
	
	/**
	 * Instancie un objet d'une classe JAXB représentant les numéros des jours.
	 * 
	 * @param jours	La map des numéros de jours selon le mois.
	 * 
	 * @return		Une instance d'une classe JAXB.
	 */
	private static Jours prepareJours(Map<Month, Integer> jours) {
		Jours result = new Jours();
		List<Jour> listJours = result.getJour();
		
		// Parcourir les jours
		for (Entry<Month, Integer> entry : jours.entrySet()) {
			Jour jour = new Jour();					// Sous-classe JAXB
			
			// Définir le moir
			Month month = entry.getKey();
			jour.setAnnee(month.getYear());
			jour.setMois(month.getNumInYear());
			
			// Définir le numéro du mois
			jour.setValue(entry.getValue());
			
			// Ajouter à l'objet rassemblant les jours
			listJours.add(jour);
		}
		
		return result;
	}
	
	/**
	 * Instancie un objet d'une classe JAXB représentant les montants.
	 * 
	 * @param montants	La map des montants selon le mois.
	 * 
	 * @return			Une instance d'une classe JAXB.
	 */
	private static Montants prepareMontants(Map<Month, BigDecimal> montants) {
		Montants result = new Montants();
		List<Montant> listMontants = result.getMontant();
		
		// Parcourir les montants
		for (Entry<Month, BigDecimal> entry : montants.entrySet()) {
			Montant montant = new Montant();		// Sous-classe JAXB
			
			// Définir le mois
			Month month = entry.getKey();
			montant.setAnnee(month.getYear());
			montant.setMois(month.getNumInYear());
			
			// Définir le montant
			montant.setValue(entry.getValue());
			
			// Ajouter à l'objet rassemblant les montants
			listMontants.add(montant);
		}
		
		return result;
	}
	
	/**
	 * Instancie un objet d'une classe JAXB représentant la dépendance à une
	 * autre opération permanente.
	 * 
	 * @param p	L'opération permanente dépendante d'une autre.
	 * 
	 * @return	Une instance d'une classe JAXB.
	 */
	private static Dependance prepareDependance(PermanentProport p) {
		Dependance result = new Dependance();
		result.setId(p.dependance.id);
		result.setTaux(p.taux.doubleValue());
		return result;
	}
	
	/**
	 * L'itérateur parcourant les objets JAXB désérialisés.
	 */
	private final PermanentOrdener it;
	
	/**
	 * Le cache des opérations permanentes déjà instanciées.
	 */
	private final CachePermanentDAO cache;
	
	/**
	 * Les comptes, classés par identifiant.
	 */
	private final Map<Integer, Compte> comptesById;
	
	/**
	 * Construit un objet d'accès aux opérations permanentes à partir d'un flux
	 * XML.
	 * <p>
	 * Cette classe utilise le framework JAXB.
	 * 
	 * @param in	Le flux XML contenant les opérations permanentes.
	 * @param cache	Un cache d'opérations permanentes, pour permettre de
	 * 				récupérer des instances lues précédemment.
	 * @param comptesById	Les comptes, classés par identifiant.
	 * 
	 * @throws IOException
	 */
	public JaxbPermanentDAO(InputStream in, CachePermanentDAO cache,
			Map<Integer, Compte> comptesById) throws IOException {
		this.cache = cache;
		this.comptesById = comptesById;
		try {
			// Le Unmarshaller
			Unmarshaller unmarshaller =
					JAXBContext.newInstance(
							JaxbPermanentDAO.class.getPackage().getName()
							+ ".jaxb.perm")
					.createUnmarshaller();
			
			// Spécifier le schéma
			unmarshaller.setSchema(getSchema());
			
			// Désérialiser
			Permanents permanents = (Permanents) unmarshaller.unmarshal(in);
			
			// Définir l'itérateur
			it = new PermanentOrdener(permanents.getPermanent().iterator());
			
		} catch (Exception e) {
			throw new IOException(
					"Impossible de lire les opérations permanentes", e);
		}
	}
	
	@Override
	public boolean hasNext() {
		return it.hasNext();
	}

	@Override
	public haas.olivier.comptes.Permanent next() {
		Integer id = null;
		
		// Récupérer l'objet JAXB
		Permanent p = it.next();

		// Caractéristiques de l'opération permanente
		id = p.getId();
		String nom = p.getNom();
		String libelle = p.getLibelle();
		String tiers = p.getTiers();
		Compte credit = comptesById.get(p.getCredit());
		Compte debit = comptesById.get(p.getDebit());
		boolean pointer = p.isPointage();
		Map<Month, Integer> jours = readJours(p.getJours());
		Montants montants = p.getMontants();
		
		// Cas des opérations à montants prédéfinis
		// TODO Refactoriser les constructeurs des permanents
		if (montants != null) {
			return new PermanentFixe(id, nom, debit, credit, libelle, tiers,
					pointer, jours, readMontants(montants));
		}
			
		// Cas des opérations dépendantes
		Dependance dependance = p.getDependance();
		if (dependance != null) {
			return new PermanentProport(id, nom, debit, credit, libelle, tiers,
					pointer, jours, cache.get(dependance.getId()),
					new BigDecimal(dependance.getTaux().toString()));
		}
		
		// Cas des opérations qui soldent un compte bancaire
		return new PermanentSoldeur(
				id, nom, debit, credit, libelle, tiers, pointer, jours);
	}
	
	/**
	 * Lit les jours JAXB et le splace dans une map.
	 * 
	 * @param jours	L'instance JAXB des jours
	 * 
	 * @return		Une Map.
	 */
	private Map<Month, Integer> readJours(Jours jours) {
		Map<Month, Integer> result = new HashMap<>();
		for (Jour jour : jours.getJour()) {
			result.put(
					Month.getInstance(jour.getAnnee(), jour.getMois()),
					jour.getValue());
		}
		return result;
	}

	/**
	 * Lit les montants JAXB et les place dans une map.
	 * 
	 * @param montants	L'instance JAXB des montants.
	 * 
	 * @return			Une Map.
	 */
	private Map<Month, BigDecimal> readMontants(Montants montants) {
		Map<Month, BigDecimal> result = new HashMap<>();
		for (Montant montant : montants.getMontant()) {
			result.put(
					Month.getInstance(montant.getAnnee(), montant.getMois()),
					montant.getValue());
		}
		return result;
	}
}
