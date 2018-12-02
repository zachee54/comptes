package haas.olivier.comptes.ctrl;

import haas.olivier.comptes.Compte;
import haas.olivier.comptes.Ecriture;
import haas.olivier.comptes.dao.DAOFactory;
import haas.olivier.comptes.dao.EcritureDAO;
import haas.olivier.comptes.dao.SuiviDAO;
import haas.olivier.util.Month;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Deque;
import java.util.LinkedList;

/**
 * Le contrôleur d'écritures.
 * <p>
 * Il s'agit d'une classe statique dont le rôle est de modifier les écritures
 * en gardant la cohérence des suivis.<br>
 * Cette méthode fait appel à {@link haas.olivier.comptes.dao.EcritureDAO}, mais
 * se soucie en plus de recalculer les soldes en fonction des modifications
 * apportées aux écritures du modèle.
 * <p>
 * Cette classe doit être utilisée pour toutes les modifications d'écritures
 * initiées directement par l'utilisateur, par exemple pour insérer, modifier ou
 * supprimer une écriture.<br>
 * <i>A contrario</i>, on peut passer directement par le DAO quand il s'agit
 * seulement de transférer des données d'un DAO à l'autre, par exemple pour
 * changer le lieu ou le format de sauvegarde des données.
 *
 * @author Olivier HAAS
 */
public class EcritureController {

	/**
	 * Durée de la période à retenir pour les moyennes glissantes (en mois).
	 */
	private static final int DUREE = 12;

	private EcritureController() {
	}

	/**
	 * Insère une écriture dans le modèle de données en assurant la cohérence
	 * des données liées.<br>
	 * Il peut s'agir soit d'une nouvelle écriture, soit de la modification
	 * d'une écriture existante.
	 * <p>
	 * Cette méthode met à jour non seulement l'écriture elle-même, mais aussi
	 * les soldes de comptes : tous les soldes sont recalculés à partir du mois
	 * le plus ancien modifié.<br>
	 * En particulier, s'il s'agit de la mise à jour d'une écriture existante,
	 * le mois le plus ancien est soit celui de l'écriture d'origine, soit celui
	 * de l'écriture modifiée.
	 * 
	 * @param e	L'écriture à ajouter ou mettre à jour. Si elle contient un
	 * 			identifiant, alors l'écriture portant le même identifiant est
	 * 			mise à jour ; si son identifiant est <code>null</code>, alors
	 * 			elle est ajoutée au modèle et un identifiant lui est attribué
	 * 			par le modèle.
	 * 
	 * @throws IOException
	 */
	public static void insert(Ecriture e) throws IOException {
		
		// Selon que l'écriture a un identifiant existant dans le modèle, ou pas
		if (e.id == null
				|| DAOFactory.getFactory().getEcritureDAO().get(e.id) == null) {
			// Écriture à ajouter au modèle + mise à jour des suivis
			add(e);
			
		} else {
			
			// Écriture à mettre à jour dans le modèle + mise à jour des suivis
			update(e);
		}
	}
	
	/**
	 * Ajoute plusieurs écritures dans le modèle et met à jour les données de
	 * suivi après l'ensemble des ajouts.
	 * 
	 * @param ecritures	Les écritures à ajouter. Elles doivent avoir un
	 * 					identifiant <code>null</code>.
	 * 
	 * @throws IOException
	 */
	public static void add(Iterable<Ecriture> ecritures) throws IOException {
		EcritureDAO eDAO = DAOFactory.getFactory().getEcritureDAO();
		Month month = null;
		
		// Ajouter chaque écriture
		for (Ecriture e : ecritures) {
			
			// Trouver le mois le plus ancien
			if (month == null || month.after(e.date))
				month = Month.getInstance(e.date);
			
			// Ajouter l'écriture au modèle de données
			eDAO.add(e);
		}
		
		// Mettre à jour les données de suivi
		updateSuivis(month);
	}
	
	/**
	 * Ajoute une nouvelle écriture dans le modèle de données et met à jour les
	 * suivis à partir du mois de l'écriture.
	 * 
	 * @param e	L'écriture à ajouter.
	 * 
	 * @throws IOException
	 */
	private static void add(Ecriture e) throws IOException {
		DAOFactory.getFactory().getEcritureDAO().add(e);// Ajouter l'écriture
		updateSuivis(Month.getInstance(e.date));		// Mettre à jour suivis
	}
	
	/**
	 * Met à jour une écriture dans le modèle, et met à jour en même temps les
	 * suivis à partir du mois de l'écriture la plus ancienne : cette écriture
	 * ou celle qu'elle remplace.
	 * 
	 * @param e	La nouvelle écriture.
	 * 
	 * @throws IOException
	 */
	private static void update(Ecriture e) throws IOException {
		
		// Le mois de l'écriture existante et de la nouvelle
		Month monthNew = Month.getInstance(e.date);		// Nouvelle écriture
		Month monthOld = Month.getInstance(				// Ancienne écriture
				DAOFactory.getFactory().getEcritureDAO().get(e.id).date);
		
		// Mettre à jour l'écriture
		DAOFactory.getFactory().getEcritureDAO().update(e);
		
		// Mettre à jour les suivis à partir du mois le plus ancien
		updateSuivis(monthNew.after(monthOld) ? monthOld : monthNew);
	}
	
	/**
	 * Supprime une écriture et met à jour les suivis à partir du mois de
	 * l'écriture supprimée.
	 * 
	 * @param id	L'identifiant de l'écriture à supprimer.
	 * 
	 * @throws IOException
	 */
	public static void remove(Integer id) throws IOException {
		
		// Le DAO des écritures
		EcritureDAO eDAO = DAOFactory.getFactory().getEcritureDAO();
		
		// Effectuer les modifications
		Ecriture e = eDAO.get(id);						// L'écriture à effacer
		eDAO.remove(id);								// Supprimer du modèle
		updateSuivis(Month.getInstance(e.date));		// Mettre à jour suivis
	}

	/**
	 * Met à jour l'historique, les soldes à vue et les moyennes des comptes à
	 * partir du mois spécifié.
	 * 
	 * @param debut	Le mois à partir duquel mettre à jour les suivis.
	 * 
	 * @throws IOException
	 */
	public static void updateSuivis(Month debut) throws IOException {
		EcritureDAO ecritureDAO = DAOFactory.getFactory().getEcritureDAO();

		// Effacer les données de suivi actuelles à compter du mois debut
		Compte.removeSuiviFrom(debut);
		
		// Mettre à jour l'historique
		updateHistoriqueAndEpargne(debut, ecritureDAO);

		/*
		 * Mettre à jour les pointages. On part du principe que le pointage
		 * intervenant APRÈS l'écriture elle-même, les pointages antérieurs à
		 * debut ne sont pas modifiés.
		 */
		updatePointages(debut, ecritureDAO);
		
		// Mettre à jour les moyennes
		for (Compte compte : DAOFactory.getFactory().getCompteDAO().getAll())
			updateMoyennes(compte, debut);
		updateMoyennes(Compte.COMPTE_EPARGNE, debut);	// Compte virtuel
	}
	
	/**
	 * Met à jour l'historique des comptes et le suivi de l'épargne depuis le
	 * mois spécifié.
	 * <p>
	 * Le suivi doit auparavant avoir été réinitialisé pour la période
	 * concernée.
	 * 
	 * @param debut			Le mois à partir duquel recalculer les soldes
	 * 						(inclus).
	 * 
	 * @param ecritureDAO	L'objet d'accès aux écritures.
	 * 
	 * @throws IOException
	 */
	private static void updateHistoriqueAndEpargne(Month debut,
			EcritureDAO ecritureDAO) throws IOException {
		Iterable<Ecriture> journal = ecritureDAO.getAllSince(debut);
		for (Ecriture e : journal) {
			Month mois = Month.getInstance(e.date);

			// Mettre à jour les soldes des comptes
			e.debit.addHistorique(mois, e.montant.negate());// Montant opposé
			e.credit.addHistorique(mois, e.montant);

			/*
			 * Si l'opération est un versement d'épargne, le comptabiliser.
			 * Attention : le compte d'épargne est un compte budgétaire, il
			 * retient l'opposé des montants qu'on lui donne !!
			 */
			switch (e.epargne) {
			case EPARGNE:
				Compte.COMPTE_EPARGNE.addHistorique(mois, e.montant.negate());
				break;
			case PRELEVEMENT:
				Compte.COMPTE_EPARGNE.addHistorique(mois, e.montant);
				break;
			case NEUTRE:
				break;
			}
		}
	}
	
	/**
	 * Met à jour les soldes à vue des comptes depuis le mois spécifié.
	 * <p>
	 * Le suivi doit auparavant avoir été réinitialisé pour la période
	 * concernée.
	 * 
	 * @param debut			Le mois à partir duquel mettre à jour les soldes à
	 * 						vue (inclus).
	 * 
	 * @param ecritureDAO	L'objet d'accès aux écritures.
	 * 
	 * @throws IOException
	 */
	private static void updatePointages(Month debut, EcritureDAO ecritureDAO)
			throws IOException {
		Iterable<Ecriture> journalPointages =
				ecritureDAO.getPointagesSince(debut);
		for (Ecriture e : journalPointages) {
			if (e.pointage != null) {
				// Pointée: mettre à jour les soldes à vue à la date de pointage
				Month mois = Month.getInstance(e.pointage);
				e.debit.addPointages(mois, e.montant.negate());
				e.credit.addPointages(mois, e.montant);
			}
		}
	}

	/**
	 * Recalcule les moyennes à partir du mois donné.
	 * <p>
	 * Cette méthode utilise l'historique des comptes. Elle ne consulte pas les
	 * écritures.
	 * 
	 * @param debut	Le mois à partir duquel mettre à jour toutes les moyennes.
	 */
	public static void updateMoyennes(Compte compte, Month debut) {
		
		// Ne concerne que les comptes budgétaires
		if (!compte.getType().isBudgetaire())
			return;

		SuiviDAO dao = DAOFactory.getFactory().getMoyenneDAO();

		// Trouver la période influencée par le mois cible
		Month premierMois = debut.getTranslated(-DUREE + 1);
		Month today = Month.getInstance();

		// File PEPS de montants monétaires sur 12 mois
		Deque<BigDecimal> queue = new LinkedList<>();

		// Remplir la file avec les soldes des n-1 mois qui précèdent
		for (Month month = premierMois;
				month.before(debut);
				month = month.getNext()) {
			queue.add(compte.getHistorique(month));
		}

		/*
		 * Calculer les moyennes glissantes.
		 * À chaque fois on rajoute un mois et on enlève le plus ancien.
		 */
		for (Month month = debut;
				!month.after(today);				// Jusqu'à aujourd'hui
				month = month.getNext()) {

			// Ajouter ce mois à la file
			queue.add(compte.getHistorique(month));

			// Calculer la moyenne sur n mois, arrondie au centième
			BigDecimal somme = BigDecimal.ZERO;
			for (BigDecimal montant : queue)
				somme = somme.add(montant);
			BigDecimal moy = somme.divide(new BigDecimal("" + DUREE), 2,
					RoundingMode.HALF_UP);
			
			/*
			 * Cela vaut-il la peine d'enregister cette moyenne ? Seulement si
			 * elle est différente de zéro, ou ou alors s'il y a une autre
			 * moyenne enregistrée
			 */
			if (moy.signum() != 0)
				dao.set(compte, month, moy);

			// Enlever le mois le plus ancien de la file
			queue.remove();
		}
	}
}
