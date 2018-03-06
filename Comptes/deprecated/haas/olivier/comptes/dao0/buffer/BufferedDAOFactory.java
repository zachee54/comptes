package haas.olivier.comptes.dao0.buffer;

import haas.olivier.comptes.MessagesFactory;
import haas.olivier.comptes.dao.DAOFactory;

import java.io.IOException;

/**
 * Un DAOFactory implémentant un buffer.
 * Les modifications sont traitées par lots et enregistrées sur demande expresse
 * de l'utilisateur.
 * Cette implémentation nécessite une sous-couche DAO acceptant les traitements
 * pas lots (interface BufferableDAOFactory).
 * 
 * @author Olivier HAAS
 */
public class BufferedDAOFactory extends DAOFactory {

	// Sous-couche DAO
	private BufferableDAOFactory dao;

	// Les BufferedDAO
	private BufferedCompteDAO compteDAO;
	private BufferedEcritureDAO ecritureDAO;
	private BufferedPermanentDAO permanentDAO;
	private BufferedSuiviDAO historiqueDAO;
	private BufferedSuiviDAO soldesDAO;
	private BufferedSuiviDAO moyenneDAO;

	/** Drapeau indiquant que les données générales du modèle ont été modifiées
	 * depuis la dernière sauvegarde.<br>
	 * En pratique, il s'agit des propriétés des diagrammes.
	 */
	private boolean hasChanged = false;
	
	public BufferedDAOFactory(BufferableDAOFactory dao) {
		this.dao = dao;
		
		// Fabriquer les instances DAO implémentant un buffer pour chaque type
		compteDAO = new BufferedCompteDAO(dao.getCompteDAO());
		ecritureDAO = new BufferedEcritureDAO(dao.getEcritureDAO());
		permanentDAO = new BufferedPermanentDAO(dao.getPermanentDAO());
		historiqueDAO = new BufferedSuiviDAO(dao.getHistoriqueDAO());
		soldesDAO = new BufferedSuiviDAO(dao.getSoldeAVueDAO());
		moyenneDAO = new BufferedSuiviDAO(dao.getMoyenneDAO());
	}// constructeur

	@Override
	public void load() throws IOException {
		dao.load();					// Charger les données dans la sous-couche
	}// load

	@Override
	public BufferedCompteDAO getCompteDAO() {
		return compteDAO;
	}

	@Override
	public BufferedEcritureDAO getEcritureDAO() {
		return ecritureDAO;
	}

	@Override
	public BufferedPermanentDAO getPermanentDAO() {
		return permanentDAO;
	}

	@Override
	public BufferedSuiviDAO getHistoriqueDAO() {
		return historiqueDAO;
	}

	@Override
	public BufferedSuiviDAO getSoldeAVueDAO() {
		return soldesDAO;
	}

	@Override
	public BufferedSuiviDAO getMoyenneDAO() {
		return moyenneDAO;
	}
	
	@Override
	public String getProperty(String prop) {
		String value = dao.getProperty(prop);
		return value == null ? "" : value;
	}
	
	@Override
	public void setProperty(String prop, String value) {
		dao.setProperty(prop, value);
		hasChanged = true;
	}

	@Override
	public boolean mustBeSaved() {

		// Dès qu'un des DAO a besoin d'être sauvegardé
		return hasChanged || compteDAO.mustBeSaved()
				|| ecritureDAO.mustBeSaved() || permanentDAO.mustBeSaved()
				|| historiqueDAO.mustBeSaved() || soldesDAO.mustBeSaved()
				|| moyenneDAO.mustBeSaved();
	}

	/** Sauvegarde les données contenues dans les buffer.
	 */
	@Override
	public void save() throws IOException {
		MessagesFactory messages = MessagesFactory.getInstance();

		// Transférer chaque buffer
		messages.showInformationMessage("Sauvegarder les comptes...");
		compteDAO.flush();
		messages.showInformationMessage("Sauvegarder les écritures...");
		ecritureDAO.flush();
		messages.showInformationMessage("Sauvegarder les permanents...");
		permanentDAO.flush();
		messages.showInformationMessage("Sauvegarder les historiqes...");
		historiqueDAO.flush();
		messages.showInformationMessage("Sauvegarder les soldes à vue...");
		soldesDAO.flush();
		messages.showInformationMessage("Sauvegarder les moyennes...");
		moyenneDAO.flush();
		messages.showInformationMessage("Terminé.");
		
		// Sauvegarder l'ensemble
		dao.save();
		hasChanged = false;
	}// save

	@Override
	public String getName() {
		return "Buffer " + dao.getName();
	}

	@Override
	public String getSource() {
		return dao.getSource();
	}

	@Override
	public String getSourceFullName() {
		return dao.getSourceFullName();
	}

	@Override
	public void erase() {
		dao.erase();
		try {
			ecritureDAO.erase();
			compteDAO.erase();
			permanentDAO.erase();
			historiqueDAO.erase();
			soldesDAO.erase();
			moyenneDAO.erase();
		} catch (NullPointerException e) {
			// Si rien n'a encore été défini, ce n'est pas grave
		}
	}// erase
}
