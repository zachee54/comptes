package haas.olivier.comptes.dao0.buffer;

import haas.olivier.comptes.dao.AbstractDAO;

/**
 * Interface définissant les DAO qui supportent les traitements par lots.
 * Les objets implémentant cette interface peuvent être associés à un buffer
 * tel que BufferedDAOFactory.
 * 
 * @author Olivier Haas
 */
public interface BufferableDAOFactory extends AbstractDAO {

	/** Renvoie un DAO qui supporte les traitements par lots pour les
	 * comptes. */
	public BufferableCompteDAO getCompteDAO();
	
	/** Renvoie un DAO qui supporte les traitements par lots pour les
	 * écritures. */
	public BufferableEcritureDAO getEcritureDAO();

	/** Renvoie un DAO qui supporte les traitements par lots pour les
	 * historiques de comptes. */
	public BufferableSuiviDAO getHistoriqueDAO();

	/** Renvoie un DAO qui supporte les traitements par lots pour les soldes à
	 * vue. */
	public BufferableSuiviDAO getSoldeAVueDAO();

	/** Renvoie un DAO qui supporte les traitements par lots pour les moyennes
	 * glissantes. */
	public BufferableSuiviDAO getMoyenneDAO();

	/** Renvoie un DAO qui supporte les traitements par lots pour les opérations
	 * permanentes. */
	public BufferablePermanentDAO getPermanentDAO();
}
