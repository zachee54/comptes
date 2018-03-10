package haas.olivier.comptes.dao.csv;

import haas.olivier.comptes.Ecriture;
import haas.olivier.comptes.EcritureMissingArgumentException;
import haas.olivier.comptes.InconsistentArgumentsException;
import haas.olivier.comptes.dao.CompteDAO;

import java.io.IOException;
import java.text.ParseException;
import java.util.Date;
import java.util.Iterator;

import com.csvreader.CsvReader;
import com.csvreader.CsvWriter;

/**
 * Un objet d'accès aux écritures au format CSV.
 * 
 * @author Olivier HAAS
 */
class CsvEcritureDAO extends AbstractCsvLayer<Ecriture> {

	/**
	 * Le nom du champ CSV contenant l'identifiant de l'écriture.
	 */
	private static final String HEADER_ID = "id";
	
	/**
	 * Le nom du champ CSV contenant la date de l'écriture.
	 */
	private static final String HEADER_DATE = "date";
	
	/**
	 * Le nom du champ CSV contenant la date de pointage.
	 */
	private static final String HEADER_POINTAGE = "pointage";
	
	/**
	 * Le nom du champ CSV contenant l'identifiant du compte débité.
	 */
	private static final String HEADER_DEBIT = "debit";
	
	/**
	 * Le nom du champ CSV contenant l'identifiant du compte crédité.
	 */
	private static final String HEADER_CREDIT = "credit";
	
	/**
	 * Le nom du champ CSV contenant le libellé de l'écriture.
	 */
	private static final String HEADER_LIB = "commentaire";
	
	/**
	 * Le nom du champ CSV contenant le nom du tiers.
	 */
	private static final String HEADER_TIERS = "tiers";
	
	/**
	 * Le nom du champ CSV contenant le numéro du chèque.
	 */
	private static final String HEADER_CHEQUE = "cheque";
	
	/**
	 * Le nom du champ CSV contenant le montant de l'écriture.
	 */
	private static final String HEADER_MONTANT = "montant";
	
	/**
	 * La disposition par défaut des colonnes.
	 */
	private static final String[] STANDARD_HEADERS =
		{HEADER_ID, HEADER_DATE, HEADER_POINTAGE, HEADER_DEBIT, HEADER_CREDIT,
				HEADER_LIB, HEADER_TIERS, HEADER_CHEQUE, HEADER_MONTANT};

	/**
	 * Sauvegarde les éléments.
	 * 
	 * @param elements	Un itérateur des écritures à sauvegarder.
	 * @param writer	Un flux CSV.
	 * 
	 * @throws IOException
	 */
	static void save(Iterator<Ecriture> elements, CsvWriter writer)
			throws IOException {
		
		// Ecrire les en-têtes
		writer.writeRecord(STANDARD_HEADERS);
		
		// Écrire les éléments
		while (elements.hasNext()) {
			Ecriture e = elements.next();
			for (String header : STANDARD_HEADERS) {
				String value = null;
				switch (header) {
				case HEADER_ID :	value = e.id.toString();			break;
				case HEADER_DEBIT :	value = e.debit.getId().toString();	break;
				case HEADER_CREDIT :value = e.credit.getId().toString();break;
				case HEADER_LIB :	value = e.libelle;					break;
				case HEADER_TIERS :	value = e.tiers;					break;
				case HEADER_MONTANT:value = e.montant.toPlainString();	break;
				case HEADER_DATE :
					if (e.date != null)
						value = CsvDAO.DF.format(e.date);
					break;
					
				case HEADER_POINTAGE :
					value = (e.pointage == null
					? "" : CsvDAO.DF.format(e.pointage));
					break;
					
				case HEADER_CHEQUE :
					value = (e.cheque == null
					? "" : e.cheque.toString());
					break;
					
				default:
					break;
				}
				writer.write(value);				// Écrire la valeur
			}
			writer.endRecord();						// Écrire la fin de ligne
		}
	}

	/**
	 * L'objet d'accès aux comptes.
	 */
	private final CompteDAO cDAO;
	
	/**
	 * Construit un objet d'accès aux écritures au format CSV.
	 * 
	 * @param reader	Le lecteur CSV à utiliser.
	 * @param cDAO		L'objet d'accès aux comptes. Il est utilisé pour
	 * 					référencer les objets <code>Compte</code> dans les
	 * 					instances <code>Ecriture</code>.
	 * 
	 * @throws IOException
	 */
	CsvEcritureDAO(CsvReader reader, CompteDAO cDAO) throws IOException {
		super(reader);
		this.cDAO = cDAO;
	}

	@Override
	protected Ecriture readNext(CsvReader reader)
			throws NumberFormatException, ParseException, IOException {
		
		// Valeur optionnelle : date de pointage
		String textPointage = reader.get(HEADER_POINTAGE);
		Date pointage = (textPointage == null || textPointage.isEmpty()
				? null								// Pas de pointage
				: CsvDAO.DF.parse(textPointage));	// Une date de pointage
		
		// Valeur optionnelle : numéro de chèque
		String textCheque = reader.get(HEADER_CHEQUE);
		Integer cheque = (textCheque == null || textCheque.isEmpty()
				? null								// Pas de numéro de chèque
				: Integer.parseInt(textCheque));	// Un numéro de chèque
		
		String idText = reader.get(HEADER_ID);
		try {
			return new Ecriture(
					Integer.parseInt(idText),
					CsvDAO.DF.parse(reader.get(HEADER_DATE)),
					pointage,
					cDAO.get(Integer.parseInt(reader.get(HEADER_DEBIT))),
					cDAO.get(Integer.parseInt(reader.get(HEADER_CREDIT))),
					CsvDAO.parseAmount(reader.get(HEADER_MONTANT)),
					reader.get(HEADER_LIB),
					reader.get(HEADER_TIERS),
					cheque);
			
		} catch (InconsistentArgumentsException
				|EcritureMissingArgumentException e) {
			throw new IOException(
					"Impossible d'instancier l'écriture n°" + idText, e);
		}
	}
	
}
