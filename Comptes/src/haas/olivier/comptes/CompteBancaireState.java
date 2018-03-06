package haas.olivier.comptes;

import haas.olivier.comptes.dao.DAOFactory;
import haas.olivier.comptes.dao.SuiviDAO;
import haas.olivier.util.Month;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Date;

/**
 * L'état d'un compte bancaire.
 * <p>
 * Les comptes bancaires ont la particularité que si aucune opération n'est
 * enregistrée au titre d'un mois, le solde du compte est celui du mois
 * précédent (et non zéro).<br>
 * De plus, les comptes bancaires doivent gérer la distinction entre les soldes
 * théoriques, qui s'appuient sur les montants des opérations passées au cours
 * du mois, et les soldes à vue qui ne sont influencés que par les dates de
 * pointage.
 * 
 * @author Olivier HAAS
 */
class CompteBancaireState implements CompteState {

	/**
	 * Format de rendu des numéros de compte.
	 * <p>
	 * Cet objet n'est pas synchronisé et ne doit donc pas être accédé de
	 * manière statique.
	 */
	private final NumberFormat format = NumberFormat.getInstance();

	/**
	 * Le type de compte.
	 */
	private TypeCompte type;
	
	/**
	 * Numéro de compte.
	 */
	private Long numero;

	/**
	 * Construit un état de compte bancaire.
	 * 
	 * @param type	Le type de compte.
	 * @param old	L'ancien état du compte. La nouvelle instance reprendra le
	 * 				même numéro de compte, s'il existe.
	 */
	public CompteBancaireState(TypeCompte type, CompteState old) {
		this.type = type;
		this.numero = (old == null) ? null : old.getNumero();
	}
	
	@Override
	public TypeCompte getType() {
		return type;
	}

	@Override
	public Long getNumero() {
		return numero;
	}

	@Override
	public void setNumero(Long numero) {
		this.numero = numero;
	}

	/**
	 * Retourne le numéro de compte avec un séparateur de milliers.
	 */
	// TODO Méthode inutilisée ?
	public synchronized String getFormattedNumero() {
		return numero == null ? "" : format.format(numero);
	}

	/**
	 * @return	Le solde du mois, à défaut le dernier solde connu avant ce mois,
	 * 			ou zéro s'il n'y a aucun solde à ce mois ni avant.
	 */
	@Override
	public BigDecimal getSuivi(Compte compte, SuiviDAO dao, Month month) {
		BigDecimal solde = null;
		Month m = month;
		Date ouverture = compte.getOuverture();
	
		/* Remonter mois par mois jusqu'à la date d'ouverture si besoin */
		while (!m.before(ouverture)) {
			solde = dao.get(compte, m);
			if (solde != null)
				return solde;
			
			m = m.getPrevious();
		}
	
		/* Valeur par défaut */
		return BigDecimal.ZERO;
	}

	@Override
	public void addHistorique(Compte compte, Month month, BigDecimal delta)
			throws IOException {
		addSuivi(compte, DAOFactory.getFactory().getHistoriqueDAO(), month,
				delta);
	}
	
	@Override
	public void addPointage(Compte compte, Month month, BigDecimal delta)
			throws IOException {
		addSuivi(compte, DAOFactory.getFactory().getSoldeAVueDAO(), month,
				delta);
	}
	
	/**
	 * Modifie le suivi du compte au titre d'un mois.
	 * 
	 * @param compte	Le compte.
	 * @param suivi		Le suivi à modifier.
	 * @param month		Le mois au titre duquel modifier le suivi.
	 * @param delta		Le montant à ajouter au suivi du mois.
	 * 
	 * @throws IOException
	 */
	private void addSuivi(Compte compte, SuiviDAO suivi, Month month,
			BigDecimal delta) throws IOException {
		BigDecimal newSolde = suivi.get(compte, month).add(delta);
		suivi.set(compte, month, newSolde);
	}

	@Override
	public int getViewSign(Compte compte, Compte debit, Compte credit) {
		if (compte == credit)
			return 1; 
		if (compte == debit)
			return -1;
		return 0;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		
		if (!(obj instanceof CompteBancaireState))
			return false;
		
		CompteBancaireState compteBancaireState = (CompteBancaireState) obj;
		
		/* Comparer les types */
		if (type != compteBancaireState.type)
			return false;
		
		/* Comparer les numéros */
		if (numero == null) {
			return compteBancaireState.numero == null;
		} else {
			return numero.equals(compteBancaireState.numero);
		}
	}
	
	@Override
	public int hashCode() {
		int h = type.hashCode();
		if (numero != null)
			h = h*17 + numero.hashCode();
		return h;
	}

	/**
	 * @return	Le numéro du compte, précédé d'une espace. Si le numéro de
	 * 			compte est <code>null</code>, renvoie une chaîne vide.
	 */
	@Override
	public synchronized String toString() {
		return numero == null ? "" : " n°" + format.format(numero);
	}
}
