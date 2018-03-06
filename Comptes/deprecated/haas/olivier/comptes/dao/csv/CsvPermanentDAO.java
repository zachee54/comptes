package haas.olivier.comptes.dao.csv;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.ParseException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import com.csvreader.CsvReader;
import com.csvreader.CsvWriter;

import haas.olivier.comptes.Compte;
import haas.olivier.comptes.CompteBancaire;
import haas.olivier.comptes.Permanent;
import haas.olivier.comptes.PermanentFixe;
import haas.olivier.comptes.PermanentProport;
import haas.olivier.comptes.PermanentSoldeur;
import haas.olivier.comptes.dao.CompteDAO;
import haas.olivier.comptes.dao.cache.CachePermanentDAO;
import haas.olivier.comptes.info.MessagesFactory;
import haas.olivier.util.Month;

/** Une classe rafistolée pour permettre la compatibilité descendante.
 * <p>
 * Un objet d'accès aux opérations permanentes au format CSV.
 * <p>
 * Sachant que certaines opérations permanentes peuvent dépendre d'une autre
 * opération permanente, elles sont enregistrées de façon à ce que l'opération
 * dépendante soit lue après l'opération dont elle dépend.<br>
 * Corrélativement, le <code>DAOFactory</code> doit être fourni à
 * l'instanciation de l'objet actuel, et doit rendre disponibles les instances
 * <code>Permanent</code> au fur et à mesure de leur instanciation, de façon à
 * ce que l'objet actuel puisse accéder à l'opération dont dépend éventuellement
 * l'objet en train d'être instancié.
 * <p>
 * NB: Le contrat interdit de créer des boucles entre opérations permanentes, ce
 * qui n'aurait d'ailleurs aucun sens.
 * 
 * @author Olivier HAAS
 */
class CsvPermanentDAO extends AbstractCsvLayer<Permanent> {
	
	/** Les noms des champs CSV. */
	private static final String HEADER_ID = "id", HEADER_NOM = "nom",
			HEADER_LIBELLE = "libelle", HEADER_TIERS = "tiers",
			HEADER_DEBIT = "debit", HEADER_CREDIT = "credit",
			HEADER_POINTER = "pointer", HEADER_DEPEND = "depend",
			HEADER_TAUX = "taux", HEADER_MOIS = "mois", HEADER_JOUR = "jour",
			HEADER_MONTANT = "montant";
	
	/** Les en-têtes standard. */
	private static final String[] STANDARD_HEADERS = {
		HEADER_ID, HEADER_NOM, HEADER_LIBELLE, HEADER_TIERS, HEADER_DEBIT,
		HEADER_CREDIT, HEADER_POINTER, HEADER_DEPEND, HEADER_TAUX, HEADER_MOIS,
		HEADER_JOUR, HEADER_MONTANT
	};
	
	/** Sauvegarde les éléments.
	 * 
	 * @param permanents	Un itérateur des écritures permanentes à
	 * 						sauvegarder.
	 * 
	 * @param writer		Un flux CSV.
	 * 
	 * @throws IOException
	 */
	static void save(Iterator<Permanent> permanents, CsvWriter writer)
			throws IOException {
		
		// Écrire les en-têtes
		writer.writeRecord(STANDARD_HEADERS);
	
		// Écrire les éléments
		while (permanents.hasNext()) {
			Permanent p = permanents.next();			// Objet à écrire
			
			// Écrire les données générales sur la première ligne
			String[] record = new String[STANDARD_HEADERS.length];
			for (int i=0; i<record.length; i++) {
				String value = null;
				switch (STANDARD_HEADERS[i]) {
				case HEADER_NOM:	value = p.nom;						break;
				case HEADER_LIBELLE:value = p.libelle;					break;
				case HEADER_TIERS:	value = p.tiers;					break;
				case HEADER_DEBIT:	value = p.debit.getId().toString();	break;
				case HEADER_CREDIT: value = p.credit.getId().toString();break;
				case HEADER_POINTER:value = (p.pointer ? "1" : "");		break;
				case HEADER_DEPEND:
					if (p instanceof PermanentProport)
						// Identifiant de l'instance dont dépend celle-ci
						value = ((PermanentProport) p).dependance.id.toString();
					break;
	
				case HEADER_TAUX:
					if ((p instanceof PermanentProport))
						// Taux (attention aux virgules)
						value = ((PermanentProport) p).taux.toPlainString()
						.replace('.', ',');
					break;
					
				default:
				}// switch header
				
				// Affecter cette valeur
				record[i] = value;
			}// for colonne
	
			// Récupérer tous les mois des jours et des montants
			Set<Month> allMonths =							// Mois des jours
					new HashSet<Month>(p.jours.keySet());
			if (p instanceof PermanentFixe) {
				allMonths.addAll(							// Mois des montants
						((PermanentFixe) p).montants.keySet());
			}// if permanent fixe
	
			// Écrire les données spécifiques sur chaque ligne (dont la 1ère)
			for (Month month : allMonths) {
	
				// Parcourir les colonnes spécifiques
				for (int i=0; i<STANDARD_HEADERS.length; i++) {
					String value = null;
					switch (STANDARD_HEADERS[i]) {
					case HEADER_ID:
						value = p.id.toString();
						break;
						
					case HEADER_MOIS:
						value = CsvDAO.DF.format(month.getFirstDay());
						break;
						
					case HEADER_JOUR:
						Integer jour = p.jours.get(month);
						if (jour != null)
							value = jour.toString();
						break;
						
					case HEADER_MONTANT:
						// Si fixe + ce mois est prédéfini explicitement
						if (p instanceof PermanentFixe) {
							PermanentFixe pFixe = (PermanentFixe) p;
							if (pFixe.montants.containsKey(month)) {
								value = pFixe.getMontant(month).toPlainString()
										.replace('.', ',');
							}// if mois prédéfini
						}// if permanent fixe
						break;
					
					default:
					}// switch nom de colonne
					
					// Si non null (pour ne pas effacer les infos générales)
					if (value != null)
						record[i] = value;				// Affecter cette valeur
				}// for colonne
	
				// Ajouter cette ligne à la liste
				writer.writeRecord(record);
	
				// Réinitialiser une nouvelle ligne
				record = new String[STANDARD_HEADERS.length];
			}// for allMonths
		}// while permanents
	}// save

	/** Le cache d'opérations permanentes qui récupère les objets instanciés au
	 * fur et à mesure.
	 * <p>
	 * Il permet de récupérer cerataines instances créées précédemment afin de
	 * référencer les opérations dont dépend l'opération en cours
	 * d'instanciation.
	 */
	private final CachePermanentDAO cache;
	
	/** L'objet d'accès aux comptes. */
	private final CompteDAO cDAO;
	
	/** Construit un objet d'accès aux opérations permanentes au format CSV.
	 * 
	 * @param reader	Le lecteur CSV.
	 * 
	 * @param cache		Le cache des opérations permanentes stockant les
	 * 					<code>Permanent</code>s instanciés au fur et à mesure.
	 * 
	 * @param cDAO		L'objet d'accès aux comptes.
	 * 
	 * @throws IOException
	 */
	CsvPermanentDAO(CsvReader reader, CachePermanentDAO cache, CompteDAO cDAO)
			throws IOException {
		super(reader);
		this.cache = cache;
		this.cDAO = cDAO;
		reader.readRecord();
	}// constructeur

	@Override
	public boolean hasNext() {
		
		// Selon qu'il y a ou non un élément déjà prêt
		if (next == null) {					// Pas d'élément tout prêt
			try {
				/* S'il y a quelque chose à lire (reader non null), pas terminé
				 * (readRecord renvoie true) et pas fermé (sinon IOException) ?
				 */
				if (reader != null) {
					next = readNext(reader);// Charger l'élément suivant
				} else {					// Fin du fichier
					close();				// Fermer le flux
				}// if
				
				// Renvoyer le résultat (dire s'il y a un élément)
				return next != null;
				
			} catch (NumberFormatException e) {// Pb de format numérique
				MessagesFactory.getInstance().showErrorMessage(
						"Erreur de format numérique : " +
						e.getLocalizedMessage() +
						" (" + getClass().getName() + ")");
				
			} catch (ParseException e) {	// Erreur de parsage de date
				MessagesFactory.getInstance().showErrorMessage(
						"Erreur de format de date : " +
						e.getLocalizedMessage() +
						" (" + getClass().getName() + ")");
				
			} catch (IOException e) {		// Lecteur fermé ou autre erreur
				MessagesFactory.getInstance().showErrorMessage(
						"Erreur pendant la lecture : " +
						e.getLocalizedMessage() +
						" (" + getClass().getName() + ")");
			}// try
			
			// Code exécuté uniquement en cas d'exception
			close();						// Fermer les ressources
			return false;					// Impossible de charger l'élément
			
		} else {							// S'il y a un élément tout prêt
			return true;					// C'est oui
		}// if
	}// hasNext

	@Override
	protected Permanent readNext(CsvReader reader)
			throws NumberFormatException, ParseException, IOException {

		// Traiter le cas où le curseur est déjà en fin de fichier (rafistolage)
		if (reader.getRawRecord().isEmpty())
			return null;
		
		// Récupérer les données génériques de l'objet
		Integer id = Integer.valueOf(reader.get(HEADER_ID));
		String nom = reader.get(HEADER_NOM);
		String libelle = reader.get(HEADER_LIBELLE);
		String tiers = reader.get(HEADER_TIERS);
		boolean pointer = !reader.get(HEADER_POINTER).trim().isEmpty();

		// Récupérer les comptes
		Compte debit = cDAO.get(Integer.parseInt(reader.get(HEADER_DEBIT)));
		Compte credit = cDAO.get(Integer.parseInt(reader.get(HEADER_CREDIT)));

		// Récupérer les données pour une dépendance
		String txtDepend = reader.get(HEADER_DEPEND);
		String txtTaux = reader.get(HEADER_TAUX);

		// Récupérer les jours et les montants
		Map<Month, Integer> jours = new HashMap<Month, Integer>();
		Map<Month, BigDecimal> montants = new HashMap<Month, BigDecimal>();
		do {
			/* La ligne fait-elle toujours partie de cet objet, ou s'agit-il
			 * déjà du suivant ?
			 */
			if (!id.equals(Integer.valueOf(reader.get(HEADER_ID))))
				break;						// Non: on arrête

			/* Y a-t-il un mois spécifié sur cette ligne ? (en principe il y
			 * en a toujours...)
			 */
			String txtMois = reader.get(HEADER_MOIS);	// Texte CSV du mois
			if (txtMois.isEmpty()) {
				continue;					// Non: passer à la ligne suivante
			}// if pas mois

			// Le mois
			Month mois = new Month(CsvDAO.DF.parse(txtMois));

			// Ajouter la date s'il y en a une
			String txtJour = reader.get(HEADER_JOUR);
			if (!txtJour.isEmpty()) {
				jours.put(mois, Integer.valueOf(txtJour));
			}// if jour

			// Ajouter le montant s'il y en a un (attention aux virgules)
			String txtMontant = reader.get(HEADER_MONTANT);
			if (!txtMontant.isEmpty())
				montants.put(mois,CsvDAO.parseAmount(txtMontant));
			
		} while (reader.readRecord());					// Ligne suivante

		// Déterminer le type de Permanent
		if (!montants.isEmpty()) {						// Montants prédéfinis
			return new PermanentFixe(
					id,
					nom,
					debit,
					credit,
					libelle,
					tiers,
					pointer,
					jours,
					montants);

		} else if (!txtDepend.isEmpty()
				&& !txtTaux.isEmpty()) {				// Dépendance
			return new PermanentProport(
					id,
					nom,
					debit,
					credit,
					libelle,
					tiers,
					pointer,
					jours,
					cache.get(Integer.parseInt(txtDepend)),		// Dépendance
					new BigDecimal(txtTaux.replace(',', '.')));	// Taux

		} else if (debit instanceof CompteBancaire) {	// Compte à solder
			return new PermanentSoldeur(
					id,
					nom,
					(CompteBancaire) debit,				// Le compte à solder
					credit,
					libelle,
					tiers,
					pointer,
					jours);

		} else {
			return null;
//			MessagesFactory.getInstance().showErrorMessage(
//					"Type indéterminé d'opération permanente: n°" + id);
//			throw new IOException();
		}// if type de permanent
	}// getNext

}
