package haas.olivier.comptes.dao.csv;

import java.awt.Color;
import java.io.IOException;
import java.util.Date;
import java.util.Map;
import java.util.Map.Entry;

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
	 * Noms des champs CSV.
	 */
	private static final String HEADER_ID = "id", HEADER_NOM = "nom", 
			HEADER_TYPE = "type", HEADER_OUV = "ouverture",
			HEADER_CLOTURE = "cloture", HEADER_NUM = "numero",
			HEADER_COLOR = "couleur";

	/**
	 * Contenu-type des en-têtes.
	 */
	private static final String[] STANDARD_HEADERS = { HEADER_ID, HEADER_NOM,
		HEADER_TYPE, HEADER_OUV, HEADER_CLOTURE, HEADER_NUM, HEADER_COLOR };
	
	/**
	 * Sauvegarde les éléments.
	 * 
	 * @param comptesById	Une collection des comptes à sauvegarder, selon leurs
	 * 					identifiants.
	 * 
	 * @param writer	Un flux CSV.
	 * 
	 * @throws IOException
	 */
	public static void save(Map<Integer, Compte> comptesById, CsvWriter writer)
			throws IOException {
		
		// Ecrire les en-têtes
		writer.writeRecord(STANDARD_HEADERS);
	
		// Parcourir les comptes
		for (Entry<Integer, Compte> compteById : comptesById.entrySet()) {
			Compte c = compteById.getValue();
			for (String header : STANDARD_HEADERS) {
				String value = null;
				switch (header) {
				case HEADER_ID :
					value = compteById.getKey().toString();
					break;
					
				case HEADER_TYPE :
					value = c.getType().id + "";
					break;
					
				case HEADER_NOM :
					value = c.getNom();
					break;
					
				case HEADER_COLOR :
					value = Integer.toHexString(c.getColor().getRGB());
					break;
					
				case HEADER_OUV :
					Date ouv = c.getOuverture();
					if (ouv != null)
						value = CsvDAO.DF.format(ouv);
					break;
					
				case HEADER_CLOTURE :
					Date cloture = c.getCloture();
					if (cloture != null)
						value = CsvDAO.DF.format(cloture);
					break;
					
				case HEADER_NUM :
					Long num = c.getNumero();
					value = (num == null) ? "" : num.toString();
					break;
				}
				writer.write(value);
			}
			writer.endRecord();
		}
	}

	/**
	 * Construit un objet d'accès aux comptes, au format CSV.
	 * 
	 * @param reader	Un lecteur de la source CSV.
	 */
	CsvCompteDAO(CsvReader reader) throws IOException {
		super(reader);
	}

	@Override
	protected Compte readNext(CsvReader reader)
			throws NumberFormatException, IOException {
		Integer id = Integer.valueOf(reader.get(HEADER_ID));

		// Déterminer le type de compte
		TypeCompte type = null;
		try {
			// TODO Compatibilité descendante à supprimer
			int typeLu = Integer.parseInt(reader.get(HEADER_TYPE));
			for (TypeCompte t : TypeCompte.values()) {
				if (t.id == typeLu) {
					type = t;
					break;
				}// if type
			}// for type
			if (type == null) {
				throw new IOException(
						"Type illisible : " + typeLu + " (compte "
								+ reader.get(HEADER_NOM) + " n°" + id + ")");
			}// if type null
			
		} catch (Exception e) {
			// Nouvelle implémentation à conserver
			TypeCompte.valueOf(reader.get(HEADER_TYPE));
		}

		// Instancier le compte
		Compte c = new Compte(type);
		c.setNom(reader.get(HEADER_NOM));
		
		// Numéro du compte bancaire, le cas échéant
		String numText = reader.get(HEADER_NUM);
		if (!numText.isEmpty())
			c.setNumero(Long.valueOf(numText));

		// Ajouter la couleur
		String colorText = reader.get(HEADER_COLOR);	// Valeur enregistrée
		if (!colorText.isEmpty()) {						// Si définie
			long color = Long.parseLong(colorText, 16);	// Couleur hexadécimale
			c.setColor(new Color(						// Définir cette couleur
					(int) (color >> 16) & 0xFF,
					(int) (color >> 8) & 0xFF,
					(int) color & 0xFF,
					(int) (color >> 24) & 0xFF));
		}

		// Ajouter la date d'ouverture s'il y en a une
		String textOuv = reader.get(HEADER_OUV);		// Texte de date
		if (textOuv != "")								// Si non vide
			c.setOuverture(CsvDAO.parseDate(textOuv));	// Définir

		// Ajouter la date de clôture s'il y en a une
		String textClot = reader.get(HEADER_CLOTURE);	// Texte de date
		if (textClot != "")								// Si non vide
			c.setCloture(CsvDAO.parseDate(textClot));	// Définir
		
		return c;
	}
}