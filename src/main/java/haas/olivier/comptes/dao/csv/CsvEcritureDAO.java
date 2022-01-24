/*
 * Copyright 2013-2018 Olivier HAAS. All rights reserved.
 */
package haas.olivier.comptes.dao.csv;

import haas.olivier.comptes.Compte;
import haas.olivier.comptes.Ecriture;
import haas.olivier.comptes.EcritureMissingArgumentException;
import haas.olivier.comptes.InconsistentArgumentsException;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;

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
	 * Le nom du champ CSV contenant la date de pointage au débit.
	 */
	private static final String HEADER_POINTAGE_DEBIT = "pointageDebit";
	
	/**
	 * Le nom du champ CSV contenant la date de pointage au crédit.
	 */
	private static final String HEADER_POINTAGE_CREDIT = "pointageCredit";
	
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
		{HEADER_ID, HEADER_DATE, HEADER_POINTAGE_DEBIT, HEADER_DEBIT, HEADER_CREDIT,
				HEADER_LIB, HEADER_TIERS, HEADER_CHEQUE, HEADER_MONTANT};

	/**
	 * Sauvegarde les éléments.
	 * 
	 * @param elements		Un itérateur des écritures à sauvegarder.
	 * @param writer		Un flux CSV.
	 * 
	 * @throws IOException
	 */
	static void save(Iterator<Ecriture> elements, CsvWriter writer)
			throws IOException {
		
		// Ecrire les en-têtes
		writer.writeRecord(STANDARD_HEADERS);
		
		// Écrire les éléments
		DateFormat dateFormat = CsvDAO.createDateFormat();
		while (elements.hasNext())
			writeEcriture(elements.next(), writer, dateFormat);
	}
	
	/**
	 * Écrit les caractéristiques d'une écriture dans une ligne CSV.
	 * 
	 * @param e				L'écriture à écrire.
	 * @param writer		Le flux d'écriture CSV
	 * @param dateFormat	Le format de date;
	 * 
	 * @throws IOException
	 */
	private static void writeEcriture(Ecriture e, CsvWriter writer,
			DateFormat dateFormat)
					throws IOException {
		for (String header : STANDARD_HEADERS) {
			String value = null;
			switch (header) {
			case HEADER_ID :	value = e.id.toString();			break;
			case HEADER_LIB :	value = e.libelle;					break;
			case HEADER_TIERS :	value = e.tiers;					break;
			case HEADER_MONTANT:value = e.montant.toPlainString();	break;
			
			case HEADER_DEBIT :
				value = Integer.toString(e.debit.getId());
				break;
				
			case HEADER_CREDIT :
				value = Integer.toString(e.credit.getId());
				break;
				
			case HEADER_DATE :
				if (e.date != null)
					value = dateFormat.format(e.date);
				break;
				
			case HEADER_POINTAGE_DEBIT :
				value = (e.pointageDebit == null
				? "" : dateFormat.format(e.pointageDebit));
				break;
				
			case HEADER_POINTAGE_CREDIT :
				value = (e.pointageCredit == null
				? "" : dateFormat.format(e.pointageCredit));
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

	/**
	 * L'objet d'accès aux comptes.
	 */
	private final Map<Integer, Compte> comptesById;
	
	/**
	 * Le format de date.
	 */
	private final DateFormat dateFormat = CsvDAO.createDateFormat();
	
	/**
	 * Construit un objet d'accès aux écritures au format CSV.
	 * 
	 * @param reader		Le lecteur CSV à utiliser.
	 * @param comptesById	Les comptes, classés par identifiant.
	 * 
	 * @throws IOException
	 */
	CsvEcritureDAO(CsvReader reader, Map<Integer, Compte> comptesById)
			throws IOException {
		super(reader);
		this.comptesById = comptesById;
	}

	@Override
	protected Ecriture readNext(CsvReader reader)
			throws ParseException, IOException {
		
		// Valeur optionnelle : dates de pointage
		Date pointageDebit = getOptionalDate(reader, HEADER_POINTAGE_DEBIT);
		Date pointageCredit = getOptionalDate(reader, HEADER_POINTAGE_CREDIT);
		
		// Valeur optionnelle : numéro de chèque
		String textCheque = reader.get(HEADER_CHEQUE);
		Integer cheque = (textCheque == null || textCheque.isEmpty()
				? null								// Pas de numéro de chèque
				: Integer.parseInt(textCheque));	// Un numéro de chèque
		
		String idText = reader.get(HEADER_ID);
		try {
			return new Ecriture(
					Integer.parseInt(idText),
					dateFormat.parse(reader.get(HEADER_DATE)),
					pointageDebit,
					pointageCredit,
					comptesById.get(
							Integer.parseInt(reader.get(HEADER_DEBIT))),
					comptesById.get(
							Integer.parseInt(reader.get(HEADER_CREDIT))),
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
	
	private Date getOptionalDate(CsvReader reader, String field)
			throws ParseException, IOException {
		String textPointage = reader.get(field);
		return (textPointage == null || textPointage.isEmpty()
				? null								// Pas de pointage
				: dateFormat.parse(textPointage));	// Une date de pointage
	}
	
}
