/*
 * Copyright 2013-2018 Olivier HAAS. All rights reserved.
 */
package haas.olivier.comptes.dao.csv;

import java.awt.Color;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import com.csvreader.CsvReader;
import com.csvreader.CsvWriter;

import haas.olivier.comptes.Compte;
import haas.olivier.comptes.TypeCompte;

/**
 * Un objet d'accès aux comptes, au format CSV.
 * 
 * @author Olivier HAAS
 */
class CsvCompteDAO extends AbstractCsvLayer<Compte> {

	/**
	 * Nom du champ CSV contenant l'identifiant du compte.
	 */
	private static final String HEADER_ID = "id";
	
	/**
	 * Nom du champ CSV contenant le nom du compte.
	 */
	private static final String HEADER_NOM = "nom";
	
	/**
	 * Nom du champ CSV contenant le type du compte.
	 */
	private static final String HEADER_TYPE = "type";
	
	/**
	 * Nom du champ CSV contenant la date d'ouverture du compte.
	 */
	private static final String HEADER_OUV = "ouverture";
	
	/**
	 * Nom du champ CSV contenant la date de clôture.
	 */
	private static final String HEADER_CLOTURE = "cloture";
	
	/**
	 * Nom du champ CSV contenant le numéro du compte.
	 */
	private static final String HEADER_NUM = "numero";
	
	/**
	 * Nom du champ CSV contenant la couleur du compte.
	 */
	private static final String HEADER_COLOR = "couleur";

	/**
	 * Contenu-type des en-têtes.
	 */
	private static final String[] STANDARD_HEADERS = { HEADER_ID, HEADER_NOM,
		HEADER_TYPE, HEADER_OUV, HEADER_CLOTURE, HEADER_NUM, HEADER_COLOR };
	
	/**
	 * Construit un objet d'accès aux comptes, au format CSV.
	 * 
	 * @param reader	Un lecteur de la source CSV.
	 */
	public CsvCompteDAO(CsvReader reader) throws IOException {
		super(reader);
	}

	/**
	 * Lit les comptes à partir d'un flux CSV et les renvoie classés par
	 * identifiant.
	 * 
	 * @param reader	Le flux CSV contenant les informations des comptes.
	 * @return			Les comptes, classés par identifiant.
	 * 
	 * @throws IOException
	 */
	static Map<Integer, Compte> loadComptes(CsvReader reader)
			throws IOException {
		Map<Integer, Compte> comptesById = new HashMap<>();
		CsvCompteDAO compteDAO = new CsvCompteDAO(reader);
		
		compteDAO.forEachRemaining(
				compte -> comptesById.put(compte.getId(), compte));
		
		compteDAO.close();
		return comptesById;
	}

	/**
	 * Sauvegarde les comptes.
	 * 
	 * @param comptes	Les comptes à sauvegarder
	 * @param writer	Un flux CSV.
	 * 
	 * @throws IOException
	 */
	public static void save(Iterable<Compte> comptes, CsvWriter writer)
			throws IOException {
		
		// Ecrire les en-têtes
		writer.writeRecord(STANDARD_HEADERS);
	
		// Parcourir les comptes
		DateFormat dateFormat = CsvDAO.createDateFormat();
		for (Compte compte : comptes)
			writeCompte(compte, writer, dateFormat);
	}
	
	/**
	 * Écrit les caractérisiques du compte dans une ligne CSV.
	 * 
	 * @param compte		Le compte à écrire.
	 * @param writer		Le flux d'écriture CSV.
	 * @param dateFormat	Le format de date.
	 * 
	 * @throws IOException
	 */
	private static void writeCompte(Compte compte, CsvWriter writer,
			DateFormat dateFormat) throws IOException {
		for (String header : STANDARD_HEADERS) {
			String value = null;
			switch (header) {
			case HEADER_ID :
				value = Integer.toString(compte.getId());
				break;
				
			case HEADER_TYPE :
				value = compte.getType().id + "";
				break;
				
			case HEADER_NOM :
				value = compte.getNom();
				break;
				
			case HEADER_COLOR :
				value = Integer.toHexString(compte.getColor().getRGB());
				break;
				
			case HEADER_OUV :
				Date ouv = compte.getOuverture();
				if (ouv != null)
					value = dateFormat.format(ouv);
				break;
				
			case HEADER_CLOTURE :
				Date cloture = compte.getCloture();
				if (cloture != null)
					value = dateFormat.format(cloture);
				break;
				
			case HEADER_NUM :
				Long num = compte.getNumero();
				value = (num == null) ? "" : num.toString();
				break;
			
			default:
				break;
			}
			writer.write(value);
		}
		writer.endRecord();
	}

	@Override
	protected Compte readNext(CsvReader reader)
			throws IOException, ParseException {
		Integer id = Integer.valueOf(reader.get(HEADER_ID));

		// Déterminer le type de compte
		TypeCompte type = null;
		int typeLu = Integer.parseInt(reader.get(HEADER_TYPE));
		for (TypeCompte t : TypeCompte.values()) {
			if (t.id == typeLu) {
				type = t;
				break;
			}
		}
		if (type == null) {
			throw new IOException(String.format(
					"Type illisible : %s (compte %s n°%s)",
					typeLu,
					reader.get(HEADER_NOM),
					id));
		}

		// Instancier le compte
		Compte compte = new Compte(id, type);
		compte.setNom(reader.get(HEADER_NOM));
		
		// Numéro du compte bancaire, le cas échéant
		String numText = reader.get(HEADER_NUM);
		if (!numText.isEmpty())
			compte.setNumero(Long.valueOf(numText));

		// Ajouter la couleur
		String colorText = reader.get(HEADER_COLOR);	// Valeur enregistrée
		if (!colorText.isEmpty()) {						// Si définie
			long color = Long.parseLong(colorText, 16);	// Couleur hexadécimale
			compte.setColor(new Color(						// Définir cette couleur
					(int) (color >> 16) & 0xFF,
					(int) (color >> 8) & 0xFF,
					(int) color & 0xFF,
					(int) (color >> 24) & 0xFF));
		}

		DateFormat dateFormat = CsvDAO.createDateFormat();
		
		// Ajouter la date d'ouverture s'il y en a une
		String textOuv = reader.get(HEADER_OUV);		// Texte de date
		if (textOuv != "")								// Si non vide
			compte.setOuverture(dateFormat.parse(textOuv));	// Définir

		// Ajouter la date de clôture s'il y en a une
		String textClot = reader.get(HEADER_CLOTURE);	// Texte de date
		if (textClot != "")								// Si non vide
			compte.setCloture(dateFormat.parse(textClot));	// Définir
		
		return compte;
	}
}