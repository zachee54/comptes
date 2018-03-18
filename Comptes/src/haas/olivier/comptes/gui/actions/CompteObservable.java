package haas.olivier.comptes.gui.actions;

import haas.olivier.comptes.Compte;
import haas.olivier.util.Observable;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.IOException;
import java.util.logging.Logger;

import javax.swing.JComboBox;

/**
 * Un observable de changements de compte.
 * <p>
 * L'interface utilisateur étant cloisonnée en plusieurs objets gérant chacun
 * les comptes d'un certain type, l'observable gère ses données dans les
 * instances, et non de manière statique.
 * 
 * @author Olivier HAAS
 */
public class CompteObservable extends Observable<CompteObserver>
implements ItemListener {

	/**
	 * Compte actuel.
	 */
	private Compte compte;
	
	/**
	 * Le composant habituel pour sélection par l'utilisateur.
	 */
	private JComboBox<Compte> comboBox;

	/**
	 * Construit un observable qui écoute une liste déroulante de
	 * <code>Compte</code>s.
	 * 
	 * @param comboBox
	 *            Une liste déroulante indiquant le compte sélectionné.
	 */
	public CompteObservable(JComboBox<Compte> comboBox) {
		this.comboBox = comboBox;

		// Récupérer le compte de départ
		compte = comboBox.getItemAt(comboBox.getSelectedIndex());

		// Ecouter les changements
		comboBox.addItemListener(this);
	}

	/**
	 * Renvoie le compte actuel.
	 * <p>
	 * Cette méthode est utile lors de l'instanciation d'objets qui ont besoin
	 * de connaître le compte sans qu'il n'y ait d'événement.
	 */
	public Compte getCompte() {
		return compte;
	}

	/**
	 * Modifie le compte cible.
	 * <p>
	 * En pratique, cette méthode modifie le compte sélectionné dans la
	 * <code>JComboBox</code>. Elle permet ainsi de modifier le compte cible
	 * autrement que par l'action de l'utilisateur.
	 */
	public void setCompte(Compte newCompte) {
		comboBox.setSelectedItem(newCompte);	// Changer la JComboBox
	}

	/**
	 * Récupère le compte sélectionné par la liste déroulante, le mémorise et 
	 * notifie les observateurs.
	 */
	@Override
	public void itemStateChanged(ItemEvent e) {
		
		// Seulement pour le nouveau compte (pas pour l'ancien)
		if (e.getStateChange() == ItemEvent.SELECTED) {
			try {
				compte = (Compte) e.getItem();
				
			} catch (ClassCastException e1) {
				Logger.getLogger(getClass().getName()).severe(
						"L'objet sélectionné n'est pas un compte");
			}

			// Notifier les observateurs
			for (CompteObserver observer : observers) {
				try {
					observer.compteChanged(compte);
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		} 
	}
}