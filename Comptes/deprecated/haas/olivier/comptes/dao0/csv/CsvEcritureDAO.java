package haas.olivier.comptes.dao0.csv;

import java.io.CharArrayWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.ParseException;
import java.util.Date;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import com.csvreader.CsvReader;
import com.csvreader.CsvWriter;

import haas.olivier.comptes.Compte;
import haas.olivier.comptes.Ecriture;
import haas.olivier.comptes.MessagesFactory;
import haas.olivier.comptes.dao.CompteDAO;
import haas.olivier.comptes.dao.DAOFactory;
import haas.olivier.comptes.dao.buffer.BufferableEcritureDAO;

/**
 * Couche DAO au format CSV pour les écritures.
 * 
 * @author Olivier Haas
 */
public class CsvEcritureDAO implements BufferableEcritureDAO {

	/** Contenu du fichier CSV. */
	char[] content;

	// Numéros de colonne des champs
	private int colId, colDate, colPointage, colDebit, colCredit, colLibelle,
			colTiers, colCheque, colMontant;

	/**
	 * Construit un CsvEcritureDAO
	 */
	public CsvEcritureDAO() {
		setContent(null);
	}
	
	/**
	 * Définit le contenu CSV à utiliser.
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
			String[] record =							// En-têtes de base
				{ "id", "date", "pointage", "debit", "credit",
					"commentaire", "tiers", "cheque", "montant" };
			CharArrayWriter out = new CharArrayWriter();// Écriture en char[]
			CsvWriter writer = CsvDAO.getWriter(out);	// Flux CSV
			try {
				writer.writeRecord(record);				// Ecrire les en-têtes
			} catch (IOException e) {
				e.printStackTrace();
			}
			this.content = out.toCharArray();			// Récupérer le contenu
			writer.close();								// Fermer le flux
		}// if null
	}// constructeur

	/**
	 * Ouvre une ressource en lecture sur le fichier CSV des comptes. Les index
	 * de colonnes colId, colNom, etc, sont initialisés.
	 * 
	 * @return un CsvReader prêt à l'emploi
	 * @throws IOException
	 */
	private CsvReader getReader() throws IOException {
		CsvReader reader = CsvDAO.getReader(content);

		// Récupérer les numéros de colonnes
		colId = reader.getIndex("id");
		colDate = reader.getIndex("date");
		colPointage = reader.getIndex("pointage");
		colDebit = reader.getIndex("debit");
		colCredit = reader.getIndex("credit");
		colLibelle = reader.getIndex("commentaire");
		colTiers = reader.getIndex("tiers");
		colCheque = reader.getIndex("cheque");
		colMontant = reader.getIndex("montant");
		return reader;
	}// getReader

	/**
	 * Instancie une écriture à partir de l'enregistrement courant du CsvReader.
	 * 
	 * @param reader
	 *            La ressource CsvReader pointant sur la ligne concernée
	 * @return Un POJO Ecriture
	 * @throws IOException
	 */
	private Ecriture getPojo(CsvReader reader) throws IOException {

		Ecriture ecriture = null;

		try {
			// Accès aux données des comptes
			CompteDAO cDao = DAOFactory.getFactory().getCompteDAO();

			// Remplacer les virgules par des points dans le montant
			String montantTxt = reader.get(colMontant);
			montantTxt = montantTxt.replace(',', '.');

			// Vérifier les types numériques
			int id;
			try {
				id = Integer.parseInt(reader.get(colId));
			} catch (NumberFormatException e) {
				MessagesFactory.getInstance().showErrorMessage(
						"Identifiant illisible : " + reader.get(colId));
				throw new IOException();
			}
			
			int debit;
			try {
				debit = Integer.parseInt(reader.get(colDebit));
			} catch (NumberFormatException e) {
				MessagesFactory.getInstance().showErrorMessage(
						"Compte de débit illisible : ligne " + id);
				throw new IOException();
			}

			int credit;
			try {
				credit = Integer.parseInt(reader.get(colCredit));
			} catch (NumberFormatException e) {
				MessagesFactory.getInstance().showErrorMessage(
						"Compte de crédit illisible : ligne " + id);
				throw new IOException();
			}

			BigDecimal montant;
			try {
				montant = new BigDecimal(montantTxt);
			} catch (NumberFormatException e) {
				MessagesFactory.getInstance().showErrorMessage(
						"Montant illisible : ligne " + id);
				throw new IOException();
			}

			// Chèque
			Integer cheque = null;
			String chequeTxt = reader.get(colCheque);
			if (chequeTxt != "") {
				try {
					cheque = Integer.parseInt(chequeTxt);
				} catch (NumberFormatException e) {
					MessagesFactory.getInstance().showErrorMessage(
							"N° de chèque illisible : ligne " + id);
					throw new IOException();
				}
			}

			// Pointage
			Date pointage = null;
			String pointageTxt = reader.get(colPointage);
			if (pointageTxt != "") {
				pointage = CsvDAO.DF.parse(pointageTxt);
			}

			// Libellé
			String libelle = reader.get(colLibelle);

			// Tiers
			String tiers = reader.get(colTiers);

			/*
			 * Instancier l'écriture. Le cas échéant, on récupère les Compte
			 * déjà instanciés, s'ils existent.
			 */
			ecriture = new Ecriture(id,						// Id
					CsvDAO.DF.parse(reader.get(colDate)),	// Date
					pointage, cDao.get(debit),				// Debit
					cDao.get(credit),						// Credit
					montant,								// Montant
					libelle,								// Libellé
					tiers,									// Tiers
					cheque);								// Chèque

		} catch (ParseException e) {
			MessagesFactory.getInstance().showErrorMessage(
					"Date illisible");
			throw new IOException();
		}// try
		return ecriture;
	}// getPojo

	/**
	 * Convertit une écriture en ligne CSV.
	 * 
	 * @param e
	 *            L'écriture à convertir
	 * @param nbCols
	 *            Le nombre de colonnes à écrire
	 * @return Un tableau de String comportant les données de l'écriture.
	 */
	private String[] getEcriture2csv(Ecriture e, int nbCols) {
		String[] record = new String[nbCols];
		record[colId] = "" + e.id; // Id écriture
		record[colDate] = CsvDAO.DF.format(e.date);// Date
		record[colDebit] = "" + e.debit.id; // Id du compte débit
		record[colCredit] = "" + e.credit.id;// Id du compte credit
		record[colLibelle] = e.libelle; // Libellé
		record[colTiers] = e.tiers; // Nom du tiers
		record[colMontant] = "" + e.montant; // Montant

		// Date de pointage
		if (e.pointage != null) {
			record[colPointage] = CsvDAO.DF.format(e.pointage);
		}

		// N° de chèque
		if (e.cheque != null) {
			record[colCheque] = "" + e.cheque; // N° du chèque
		}

		return record;
	}// getEcriture2csv

	@Override
	public TreeSet<Ecriture> getAll() throws IOException {

		TreeSet<Ecriture> set = new TreeSet<Ecriture>();
		CsvReader reader = null;

		try {
			// Ouvrir le fichier
			reader = getReader();

			// Récupérer ligne par ligne, instancier et stocker les Pojos
			while (reader.readRecord()) {
				set.add(getPojo(reader));
			}

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			// Fermer le Reader
			if (reader != null) {
				reader.close();
			}
		}// try
		return set;
	}// getAll

	@Override
	public void save(Map<Integer, Ecriture> add, Map<Integer, Ecriture> update,
			Set<Integer> remove) throws IOException {
		
		CsvReader reader = getReader();					// Lecteur CSV
		CharArrayWriter out = new CharArrayWriter();	// Écriture en mémoire
		CsvWriter writer = CsvDAO.getWriter(out);		// Flux d'écriture CSV

		try {
			// Ecrire les en-têtes
			writer.writeRecord(reader.getHeaders());

			// Parcourir les lignes
			Integer id;
			while (reader.readRecord()) {

				// Identifiant
				id = Integer.valueOf(reader.get(colId));

				// Faut-il la supprimer ?
				if (remove.contains(id)) {
					continue; // On passe tout de suite à la ligne suivante
				}

				// Faut-il la mettre à jour ?
				if (update.containsKey(id)) {

					// Ajouter cette écriture au fichier
					writer.writeRecord(getEcriture2csv(update.get(id),
							reader.getHeaderCount()));

				} else {
					// Ni suppression, ni mise à jour: transcrire telle quelle
					writer.writeRecord(reader.getValues());

				}// if update
			}// while readRecord

			// Ajouter les nouvelles écritures
			for (Ecriture e : add.values()) {

				// Ecrire cette écriture
				writer.writeRecord(getEcriture2csv(e, reader.getHeaderCount()));
			}
			content = out.toCharArray();				// Récupérer le contenu
		} catch (IOException e) {
			MessagesFactory.getInstance().showErrorMessage(
					"Erreur pendant la génération de la sauvegarde CSV.");
		} finally {
			CsvDAO.close(reader, writer);				// Fermer les ressources
		}
	}// save

	@Override
	/** Rien à faire, les comptes étant référencés en dur uniquement par leur
	 * identifiant. */
	public void refreshCompte(Compte compte) {
	}
}
