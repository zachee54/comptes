package haas.olivier.comptes.dao0.csv;

import java.awt.Color;
import java.io.CharArrayWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.csvreader.CsvReader;
import com.csvreader.CsvWriter;

import haas.olivier.comptes.Compte;
import haas.olivier.comptes.CompteBancaire;
import haas.olivier.comptes.CompteBudget;
import haas.olivier.comptes.MessagesFactory;
import haas.olivier.comptes.TypeCompte;
import haas.olivier.comptes.dao.buffer.BufferableCompteDAO;

/** Couche DAO au format CSV pour les comptes.
 * 
 * @author Olivier Haas
 */
public class CsvCompteDAO implements BufferableCompteDAO {

	/** Noms des champs CSV. */
	private static final String HEADER_ID = "id", HEADER_NOM = "nom", 
			HEADER_TYPE = "type", HEADER_OUV = "ouverture",
			HEADER_CLOTURE = "cloture", HEADER_NUM = "numero",
			HEADER_COLOR = "couleur";
	
	/** Contenu-type des en-têtes. */
	private static final String[] STANDARD_HEADERS = { HEADER_ID, HEADER_NOM,
		HEADER_TYPE, HEADER_OUV, HEADER_CLOTURE, HEADER_NUM, HEADER_COLOR };
	
	/** Contenu du fichier CSV. */
	char[] content;

	/** Collection des instances chargées, avec numérotation. */
	private Map<Integer,Compte> instances = new HashMap<Integer,Compte>();

	/** Construit un DAO pour les objets <code>Compte</code>. */
	public CsvCompteDAO() {
		setContent(null);
	}// constructeur
	
	/** Définit le contenu CSV à utiliser.
	 * 
	 * @param content	Le contenu d'un fichier CSV sous forme de tableau de
	 * 					caractères. Si l'argument est null, le DAO part d'une
	 * 					base vide.
	 */
	protected void setContent(char[] content) {
		if (content != null) {
			this.content = content;						// Contenu CSV spécifié
		} else {
			// Créer un contenu minimal s'il n'existe pas
			String[] record = STANDARD_HEADERS;
			CharArrayWriter out = new CharArrayWriter();// Ecriture en char[]
			CsvWriter writer = CsvDAO.getWriter(out);	// Flux CSV
			try {
				writer.writeRecord(record);				// Ecrire les en-têtes
			} catch (IOException e) {
				e.printStackTrace();
			}
			this.content = out.toCharArray();			// Récupérer le contenu
			writer.close();								// fermer
		}// if null
	}// setContent

	/** Instancie un compte à partir de l'enregistrement courant du
	 * <code>CsvReader</code>. Selon les données lues, l'instance sera de la
	 * classe <code>CompteBancaire</code> ou <code>CompteBudget</code>.
	 * 
	 * @param reader	La ressource <code>CsvReader</code> pointant sur la
	 * 					ligne concernée.
	 * 
	 * @return			Un POJO <code>Compte</code>.
	 * 
	 * @throws IOException
	 */
	private Compte getPojo(CsvReader reader) throws IOException {
		Compte compte = null;
		try {
			Integer id = Integer.valueOf(reader.get(HEADER_ID));
			
			// Retourner l'instance existante de ce compte s'il y en a une
			if (instances.containsKey(id)) {
				return instances.get(id);
			}

			// Déterminer le type de compte
			int typeLu = Integer.parseInt(reader.get(HEADER_TYPE));
			TypeCompte type = null;;
			for (TypeCompte t : TypeCompte.values()) {
				if (t.id == typeLu) {
					type = t;
				}
			}
			if (type == null) {
				throw new IOException("Type illisible : " + typeLu);
			}

			// Instancier selon le type de compte déterminé
			if (type.isBancaire()) {					// Compte bancaire
				compte = new CompteBancaire(id, reader.get(HEADER_NOM),
						Long.parseLong(reader.get(HEADER_NUM)), type);

			} else if (type.isBudgetaire()) {			// Compte budgétaire
				compte = new CompteBudget(id, reader.get(HEADER_NOM), type);

			} else {
				MessagesFactory.getInstance().showErrorMessage(
						"Type de compte incohérent");
				throw new IOException();
			}// if type
			
			// Ajouter la couleur
			try {
				long color = Long.parseLong(reader.get(HEADER_COLOR), 16);
				compte.setColor(new Color(
						(int) (color >> 16) & 0xFF,
						(int) (color >> 8) & 0xFF,
						(int) color & 0xFF,
						(int) (color >> 24) & 0xFF));
			} catch (NumberFormatException e) {
			}// try
			
			// Ajouter la date d'ouverture s'il y en a une
			String textOuv = reader.get(HEADER_OUV);			// Texte de date
			if (textOuv != "")									// Si non vide
				compte.setOuverture(CsvDAO.parseDate(textOuv));	// Définir
			
			// Ajouter la date de clôture s'il y en a une
			String textClot = reader.get(HEADER_CLOTURE);		// Texte de date
			if (textClot != "")									// Si non vide
				compte.setCloture(CsvDAO.parseDate(textClot));	// Définir

		} catch (NumberFormatException e) {
			MessagesFactory.getInstance().showErrorMessage(
					"Type ou numéro de compte illisible");
			throw new IOException();

		} catch (IOException e) {
			MessagesFactory.getInstance().showErrorMessage(
					"Erreur de lecture pendant la récupération d'un compte");
			throw e;
		}// try
		
		// Ajouter le nouveau compte dans la liste des instances
		instances.put(compte.id, compte);

		return compte;
	}// getPojo

	/** Convertit un compte en ligne CSV.
	 * 
	 * @param c			Le compte à convertir.
	 * @param nbcols	Le nombre de colonnes à écrire.
	 * @return			Un tableau de <code>String</code> comportant les données
	 * 					du compte.
	 */
	private String[] getCompte2csv(Compte c, int nbcols) {
		String[] record = new String[STANDARD_HEADERS.length];
		List<String> headers = Arrays.asList(STANDARD_HEADERS);
		
		record[headers.indexOf(HEADER_ID)] = "" + c.id;
		record[headers.indexOf(HEADER_TYPE)] = "" + c.getType().id;
		record[headers.indexOf(HEADER_NOM)] = c.getNom();
		record[headers.indexOf(HEADER_COLOR)] =
				Integer.toHexString(c.getColor().getRGB());

		// Date d'ouverture
		Date ouverture = c.getOuverture();
		if (ouverture != null) {
			record[headers.indexOf(HEADER_OUV)] = CsvDAO.DF.format(ouverture);
		}

		// Date de clôture
		Date cloture = c.getCloture();
		if (cloture != null) {
			record[headers.indexOf(HEADER_CLOTURE)] = CsvDAO.DF.format(cloture);
		}

		// Numéro de compte bancaire
		if (c instanceof CompteBancaire) {
			record[headers.indexOf(HEADER_NUM)] =
					((CompteBancaire) c).getNumero() + "";
		}

		return record;
	}// getCompte2csv

	/** Ouvre une ressource en lecture sur le contenu CSV des comptes. Les index
	 * de colonnes colId, colNom, etc, sont initialisés.
	 * 
	 * @return Un CsvReader prêt à l'emploi
	 * @throws IOException
	 */
	private CsvReader getReader() throws IOException {
		return CsvDAO.getReader(content);
	}// getReader

	@Override
	public Set<Compte> getAll() throws IOException {

		Set<Compte> set = new HashSet<Compte>();
		CsvReader reader = null;

		try {
			// Ouvrir le fichier
			reader = getReader();

			// Récupérer ligne par ligne, instancier et stocker les Pojo
			while (reader.readRecord()) {
				set.add(getPojo(reader));
			}

		} catch (IOException e) {
			MessagesFactory.getInstance().showErrorMessage(
					"Impossible de récupérer les comptes");
			throw e;

		} finally {
			// Fermer le fichier
			if (reader != null) {
				reader.close();
			}
		}// try

		return set;
	}// getAll

	@Override
	public void save(
			Map<Integer, Compte> add,
			Map<Integer, Compte> update,
			Set<Integer> remove) throws IOException {

		// S'il n'y a pas de changements, ne rien faire !
		if (add.size() + update.size() + remove.size() == 0) {
			return;
		}

		CsvReader reader = getReader();					// Lecteur
		CharArrayWriter out = new CharArrayWriter();	// Écriture en mémoire
		CsvWriter writer = CsvDAO.getWriter(out);		// Flux d'écriture CSV

		try {
			// Ecrire les en-têtes
			writer.writeRecord(STANDARD_HEADERS);

			// Parcourir les lignes
			Integer id;
			while (reader.readRecord()) {

				// Identifiant
				id = Integer.valueOf(reader.get(HEADER_ID));

				// Faut-il le supprimer ?
				if (remove.contains(id)) {
					continue; // On passe tout de suite à la ligne suivante
				}

				// Faut-il le mettre à jour ?
				if (update.containsKey(id)) {

					// Ajouter ce compte dans le fichier
					writer.writeRecord(getCompte2csv(update.get(id),
							reader.getHeaderCount()));

				} else {

					// Ni suppression, ni mise à jour: transcrire tel quel
					String[] outValues = new String[STANDARD_HEADERS.length];
					for (int i=0; i<outValues.length; i++) {
						outValues[i] = reader.get(STANDARD_HEADERS[i]);
					}
					writer.writeRecord(outValues);

				}// if update
			}// while readRecord

			// Ajouter les nouveaux comptes
			for (Compte c : add.values()) {

				// Ecrire ce compte
				writer.writeRecord(getCompte2csv(c, reader.getHeaderCount()));
			}

			content = out.toCharArray();		// Enregistrer le résultat
			instances.clear();					// Réinitialiser les instances
		} catch (IOException e) {
			MessagesFactory.getInstance().showErrorMessage(
					"Erreur pendant la génération de la sauvegarde CSV.");
		} finally {
			CsvDAO.close(reader, writer);		// Fermer les ressources
		}
	}// save
}
