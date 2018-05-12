package haas.olivier.comptes.gui.settings;

import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.Map.Entry;

import javax.swing.table.AbstractTableModel;

import haas.olivier.util.Month;

/**
 * Un <code>TableModel</code> permettant de saisir des valeurs en fonction des
 * mois.
 * <p>
 * Elle peut servir pour la table des jours ou la table des montants.
 */
@SuppressWarnings("serial")
class PlannerTableModel extends AbstractTableModel {

	/**
	 * La classe des valeurs.
	 */
	private final Class<?> valueClass;
	
	/**
	 * Le titre de la colonne des valeurs.
	 */
	private final String titre;
	
	/**
	 * Les valeurs pour chaque mois.
	 */
	private SortedMap<Month,Object> map = new TreeMap<Month,Object>();
	
	/**
	 * Le mois figurant actuellement dans la première ligne.<br>
	 * La première ligne est une ligne de saisie d'un nouveau mois.
	 */
	private Month moisSaisi = null;
	
	/**
	 * La valeur figurant actuellement dans la première ligne.<br>
	 * La première ligne est une ligne de saisie d'un nouveau mois.
	 */
	private Object valeurSaisie = null;
	
	/**
	 * Crée un nouveau modèle de table de deux colonnes permettant de saisir un
	 * mois dans la première colonne et une valeur dans la deuxième.
	 * 
	 * @param valueClass	Le type de valeurs attendu.
	 * @param titre			Le titre à donner à la colonne des valeurs.
	 */
	public PlannerTableModel(Class<?> valueClass, String titre) {
		this.valueClass = valueClass;
		this.titre = titre;
	}
	
	@Override
	public String getColumnName(int columnIndex) {
		return (columnIndex == 0) ? "Mois" : titre;
	}

	@Override
	public Class<?> getColumnClass(int columnIndex) {
		return (columnIndex == 0) ? Month.class : valueClass;
	}

	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		return true;
	}

	@Override
	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
		
		// Récupérer la saisie dans le type adéquat (ou null)
		Month month = null;
		Object valeur = null;
		if (columnIndex == 0 && aValue instanceof Month) {
			month = (Month) aValue;
		} else if (columnIndex == 1 && valueClass.isInstance(aValue)) {
			valeur = aValue;
		}
		
		// Modifier les données
		if (rowIndex == 0) {						// Ligne de saisie ?
			if (columnIndex == 0) {
				moisSaisi = month;
			} else if (columnIndex == 1) {
				valeurSaisie = valeur;
			}
			
			// Si toutes les données sont saisies
			if (moisSaisi != null && valeurSaisie != null) {
				map.put(moisSaisi, valeurSaisie);
				moisSaisi = null;
				valeurSaisie = null;
			}
			
		} else {
			Entry<Month,Object> entry = getEntryAt(rowIndex-1);
			if (columnIndex == 0) {
				map.remove(entry.getKey());
				if (month != null) {
					map.put(month, entry.getValue()); 
				}
			} else if (columnIndex == 1) {
				if (valeur == null) {				// Saisie de valeur effacée
					map.remove(entry.getKey());
				} else {
					map.put(entry.getKey(), valeur);
				}
			}
		}
		fireTableDataChanged();
	}

	/**
	 * Retourne les données de la ligne spécifiée de la map.
	 * 
	 * @param index	Index de la map interne (et non du modèle).
	 */
	private Entry<Month,Object> getEntryAt(int index) {
		return map.entrySet().stream().skip(index).findFirst().get();
	}

	@Override
	public int getRowCount() {
		return map.size()+1;		// La taille de la map + une ligne de saisie
	}

	@Override
	public int getColumnCount() {
		return 2;
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		
		// Ligne de saisie
		if (rowIndex == 0) {
			// Le mois ou le jour saisi
			return (columnIndex == 0) ? moisSaisi : valeurSaisie;
		}
		
		// L'entrée de la ligne (n-1 à cause de la ligne de saisie)
		Entry<Month,Object> entry = getEntryAt(rowIndex-1);
		
		// Retourner le mois ou le jour du mois, suivant la colonne
		return (columnIndex == 0) ? entry.getKey() : entry.getValue();
	}
	
	/**
	 * Renvoie la <code>Map</code> actuelle.
	 */
	public Map<Month,Object> getMap() {
		return map;
	}
	
	/**
	 * Remplace les données actuelles par celles qui sont contenues dans la Map
	 * spécifiée.
	 * 
	 * @param jours	La Map contenant les valeurs à utiliser. L'objet fourni en
	 * 				paramètre n'est pas modifié. Par contrat, cette	Map ne doit
	 * 				contenir comme valeurs que des objets de la	classe fournie
	 * 				au constructeur. Sinon, le comportement est indéfini.
	 */
	public void setMap(Map<Month,? extends Object> jours) {
		map = (jours == null)
				? new TreeMap<>()
				: new TreeMap<>(jours);					// Nouvelles données
		fireTableDataChanged();							// Avertir du changement
	}
}// class PlannerTableModel