package haas.olivier.comptes.dao0.csv;

import java.io.CharArrayWriter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.csvreader.CsvReader;
import com.csvreader.CsvWriter;

import haas.olivier.comptes.Compte;
import haas.olivier.comptes.CompteBancaire;
import haas.olivier.comptes.MessagesFactory;
import haas.olivier.util.Month;
import haas.olivier.comptes.Permanent;
import haas.olivier.comptes.dao.CompteDAO;
import haas.olivier.comptes.dao.DAOFactory;
import haas.olivier.comptes.dao.buffer.BufferablePermanentDAO;

public class CsvPermanentDAO implements BufferablePermanentDAO {

	/**
	 * Une classe interne d'exceptions permettant de signaler qu'une méthode a
	 * terminé le traitement du fichier
	 */
	private class TermineException extends Exception {
		private static final long serialVersionUID = 7958102813337303893L;
	}

	/** Contenu du fichier CSV. */
	char[] content;

	// Numéros de colonne des champs
	private int colId, colNom, colLibelle, colTiers, colDebit, colCredit,
			colPointer, colDepend, colTaux, colMois, colJour, colMontant;

	/**
	 * Construit un DAO pour les objets Permanent.
	 */
	public CsvPermanentDAO() {
		setContent(null);
	}
	
	/**
	 * Définit le contenu CSV à utiliser.
	 *
	 * @param content	Le contenu CSV à lire.
	 */
	protected void setContent(char[] content) {
		if (content != null) {
			this.content = content;						// Contenu spécifié
		} else {
			// Créer un contenu minimal s'il n'existe pas
			String[] record =							// En-têtes de base
				{ "id", "nom", "libelle", "tiers", "debit", "credit", "pointer",
					"depend", "taux", "mois", "jour", "montant" };
			CharArrayWriter out = new CharArrayWriter();// Écriture mémoire
			CsvWriter writer = CsvDAO.getWriter(out);	// Flux CSV
			try {
				writer.writeRecord(record);				// On écrit
			} catch (IOException e) {
				e.printStackTrace();
			}
			this.content = out.toCharArray();			// Récupérer le contenu
			writer.close();
		}// if null
	}// constructeur

	/**
	 * Instancie un Permanent à partir de l'enregistrement courant du CsvReader.
	 * 
	 * @throws IOException
	 *             Si le fichier est illisible
	 * @throws TermineException
	 *             S'il n'y a plus rien à lire
	 */
	private Permanent getPojo(CsvReader reader) throws IOException,
			TermineException {

		Permanent permanent = null;

		try {
			// Y a-t-il encore des données ?
			if (reader.getColumnCount() == 0) {
				// On a fini:
				throw new TermineException();
			}

			// Retourner l'instance existante de ce Permanent s'il y en a une
			Integer id = Integer.valueOf(reader.get(colId));
			if (Permanent.instances.containsKey(id)) { // L'instance existe

				// Déplacer le curseur du CsvReader jusqu'à l'item suivant
				while (Integer.valueOf(reader.get(colId)).equals(id)// Même item
						&& reader.readRecord()) { // Fichier pas terminé
				} // Avancer le curseur plus loin

				// Retourner l'instance existante
				return Permanent.instances.get(id);
			}

			// Récupérer les données génériques de l'objet
			String nom = reader.get(colNom);
			String libelle = reader.get(colLibelle);
			String tiers = reader.get(colTiers);
			boolean pointer = (reader.get(colPointer) != "");

			// Récupérer les comptes
			CompteDAO cDAO = DAOFactory.getFactory().getCompteDAO(); // DAO
			Compte debit = cDAO.get(Integer.parseInt(reader.get(colDebit)));
			Compte credit = cDAO.get(Integer.parseInt(reader.get(colCredit)));

			// Récupérer les données pour une dépendance
			String txtDepend = reader.get(colDepend);
			String txtTaux = reader.get(colTaux);

			// Récupérer les jours et les montants
			Map<Month, Integer> jours = new HashMap<Month, Integer>();
			Map<Month, BigDecimal> montants = new HashMap<Month, BigDecimal>();
			do {
				/*
				 * La ligne fait-elle toujours partie de cet objet, ou s'agit-il
				 * déjà du suivant ?
				 */
				if (!id.equals(Integer.valueOf(reader.get(colId)))) {
					break; // Non: on arrête
				}

				/*
				 * Y a-t-il un mois spécifié sur cette ligne ? (en principe il y
				 * en a toujours...)
				 */
				if (reader.get(colMois) == "") {
					continue; // Non: passer à la ligne suivante
				}

				// Le mois
				Month mois = new Month(CsvDAO.DF.parse(reader.get(colMois)));

				// Ajouter la date s'il y en a une
				if (reader.get(colJour) != "") {
					jours.put(mois, Integer.valueOf(reader.get(colJour)));
				}

				// Ajouter le montant s'il y en a un (attention aux virgules)
				if (reader.get(colMontant) != "") {
					montants.put(mois, new BigDecimal(reader.get(colMontant)
							.replace(',', '.')));
				}
			} while (reader.readRecord()); // Ligne suivante

			// Déterminer le type de Permanent
			if (!montants.isEmpty()) { // Montants prédéfinis
				permanent = new Permanent(id, nom, debit, credit, jours,
						montants);

			} else if (txtDepend != "" && txtTaux != "") { // Dépendance
				permanent = new Permanent(id, nom, debit, credit, jours,
				/*
				 * Permanent dont dépend celui-ci (appel récursif) On ne peut
				 * pas appeler l'instance DAOFactory car certaines
				 * implémentations font appel à un buffer, ce qui peut créer ici
				 * une boucle infinie sur la méthode getAll()
				 */
						get(Integer.parseInt(txtDepend)),
				
				// Taux (attention aux virgules)
						new BigDecimal(txtTaux.replace(',', '.')));

			} else if (debit instanceof CompteBancaire) { // Compte à solder
				permanent = new Permanent(id, nom, (CompteBancaire) debit,
						credit, jours);

			} else {
				MessagesFactory.getInstance().showErrorMessage(
						"Type indéterminé d'opération permanente: n°" + id);
				throw new IOException();
			}

			permanent.libelle = libelle;
			permanent.tiers = tiers;
			permanent.pointer = pointer;

			return permanent;

		} catch (NumberFormatException e) {
			MessagesFactory.getInstance().showErrorMessage(
					"Nombre illisible");
			throw new IOException();

		} catch (IOException e) {
			MessagesFactory.getInstance().showErrorMessage(
					"Erreur de lecture pendant la récupération d'une "
							+ "opération permanente");
			throw e;

		} catch (ParseException e) {
			MessagesFactory.getInstance().showErrorMessage(
					"Mois illisible à l'item " + reader.get(colId));
			throw new IOException();
		}
	}

	/** Convertit une opération permanente en contenu CSV. */
	private List<String[]> getPermanent2csv(Permanent p, int nbcols) {

		List<String[]> result = new ArrayList<String[]>(); // Résultat

		String[] record = new String[nbcols]; // 1ère ligne

		// Remplir la 1ère ligne avec les données génériques (sauf identifiant)
		record[colNom] = p.nom;
		record[colLibelle] = p.libelle;
		record[colTiers] = p.tiers;
		record[colDebit] = "" + p.debit.id;
		record[colCredit] = "" + p.credit.id;
		record[colPointer] = (p.pointer ? "1" : "");

		// + opération permanente dont dépend celle-ci (attention aux virgules)
		if (p.dependance != null && p.taux != null) {
			record[colDepend] = "" + p.dependance.id;
			record[colTaux] = p.taux.toString().replace('.', ',');
		}

		// Récupérer tous les mois des jours et des montants
		Set<Month> allMonths = new HashSet<Month>(p.jours.keySet());// des jours
		if (p.montants != null) {
			allMonths.addAll(p.montants.keySet()); // mois des montants
		}

		/*
		 * Ajouter les dates et montants en fonction de leur mois NB: à la
		 * première itération, on part du String[] record initialisé ci-dessus,
		 * avec les propriétés généralistes
		 */
		for (Month month : allMonths) {

			// Ajouter les propriétés à inscrire dans cette ligne
			record[colId] = "" + p.id;							// Identifiant
			record[colMois] =									// Mois
					CsvDAO.DF.format(month.getFirstDay());

			// Y a-t-il un jour spécifié pour ce mois ?
			if (p.jours.containsKey(month)) {

				// Jour(éventuellement null)
				record[colJour] = "" + p.jours.get(month);
			}

			// Montant
			if (p.montants != null) {

				// Y a-t-il un montant pour ce mois ?
				if (p.montants.containsKey(month)) {

					/*
					 * Le montant, avec une virgule en séparateur décimal, (ou
					 * null s'il n'y a pas de montant pour ce mois).
					 */
					record[colMontant] = p.montants.get(month).toString()
							.replace('.', ',');
				}
			}

			// Ajouter cette ligne à la liste
			result.add(record);

			// Réinitialiser une nouvelle ligne
			record = new String[nbcols];
		}// for allMonths
		return result;
	}// getPermanent2csv

	/**
	 * Ouvre une ressource en lecture sur le fichier CSV des opérations
	 * permanentes. Les index de colonnes colId, colNom, etc, cont initialisés.
	 * 
	 * @return un CsvReader prêt à l'emploi.
	 * @throws IOException
	 * @throws FileNotFoundException
	 */
	private CsvReader getReader() throws FileNotFoundException, IOException {

		CsvReader reader = CsvDAO.getReader(content);

		// Récupérer les numéros de colonnes
		colId = reader.getIndex("id");
		colNom = reader.getIndex("nom");
		colLibelle = reader.getIndex("libelle");
		colTiers = reader.getIndex("tiers");
		colDebit = reader.getIndex("debit");
		colCredit = reader.getIndex("credit");
		colPointer = reader.getIndex("pointer");
		colDepend = reader.getIndex("depend");
		colTaux = reader.getIndex("taux");
		colMois = reader.getIndex("mois");
		colJour = reader.getIndex("jour");
		colMontant = reader.getIndex("montant");

		return reader;
	}// getReader

	/** Récupère une instance Permanent dans le fichier CSV.
	 * Utile uniquement pour instancier les Permanents qui dépendent d'un autre
	 * Permanent sans provoquer une boucle infinie sur getAll().
	 * En effet, si le DAO est utilisé avec une sur-couche, la fonction get(id)
	 * appelée par DAOFactory génère un appel à getAll(), qui peut elle-même
	 * rappeler get(id)...
	 * @param id	L'identifiant du Permanent
	 * @return		Le Permanent
	 * @throws IOException
	 */
	private Permanent get(int id) throws IOException {

		/*
		 * Si l'opération permanente est déjà instanciée, retourner l'instance
		 * existante
		 */
		if (Permanent.instances.containsKey(id)) {
			return Permanent.instances.get(id);
		}

		Permanent permanent = null;
		CsvReader reader = null;

		try {
			// Ouvrir le fichier
			reader = getReader();

			// Chercher ligne par ligne
			while (reader.readRecord()) {
				if (Integer.parseInt(reader.get(colId)) == id) {

					// Identifiant trouvé: générer le POJO
					permanent = getPojo(reader);
				}
			}

		} catch (IOException e) {
			MessagesFactory.getInstance().showErrorMessage(
					"Impossible de lire l'opération permanent n°" + id);
			throw e;

		} catch (TermineException e) {
			// Ne devrait jamais arriver. Faire suivre une exception quand même!
			throw new IOException();

		} finally {
			// Fermer le fichier
			if (reader != null) {
				reader.close();
			}
		}// try

		// Si l'opértion n'existe pas, lever une exception
		if (permanent == null) {
			throw new IOException();
		}

		return permanent;
	}// get

	@Override
	public Set<Permanent> getAll() throws IOException {

		Set<Permanent> set = new HashSet<Permanent>();
		CsvReader reader = null;

		try {
			// Ouvrir le fichier
			reader = getReader();

			reader.readRecord(); // Se positionner sur la première ligne

			// Récupérer chaque ligne jusqu'à ce qu'il n'y ait plus rien à lire
			while (true) {
				set.add(getPojo(reader)); // Stocker le Pojo suivant
			}

		} catch (TermineException e) {
			// C'est fini, tout va bien
			return set;

		} catch (IOException e) {
			MessagesFactory.getInstance().showErrorMessage(
					"Impossible de récupérer les opérations permanentes");
			throw e;

		} finally {
			// Fermer le fichier
			if (reader != null) {
				reader.close();
			}
		}// try
	}// getAll

	@Override
	public void save(Map<Integer, Permanent> add,
			Map<Integer, Permanent> update, Set<Integer> remove)
			throws IOException {

		// S'il n'y a pas de changements, ne rien faire !
		if (add.size() + update.size() + remove.size() == 0) {
			return;
		}
		
		CsvReader reader = getReader();
		CharArrayWriter out = new CharArrayWriter();
		CsvWriter writer = CsvDAO.getWriter(out);
		
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

					// Ajouter cette opération permanente dans le fichier
					for (String[] record : getPermanent2csv(update.get(id),
							reader.getHeaderCount())) {
						writer.writeRecord(record);
					}

				} else {

					// Ni suppression, ni mise à jour: transcrire tel quel
					writer.writeRecord(reader.getValues());

				}// if update
			}// while readRecord

			// Ajouter les nouvelles opérations permanentes
			for (Permanent p : add.values()) {

				// Ecrire cette opération (plusieurs lignes)
				for (String[] record : getPermanent2csv(p,
						reader.getHeaderCount())) {

					writer.writeRecord(record);
				}
			}// for add
			content = out.toCharArray();				// Récupérer le contenu
		} catch(IOException e) {
			MessagesFactory.getInstance().showErrorMessage(
					"Erreur pendant la génération de la sauvegarde CSV.");
		} finally {
			CsvDAO.close(reader, writer);				// Fermer les ressources
		}
	}// save
}