package haas.olivier.comptes.dao0.serialize;

import haas.olivier.comptes.Compte;
import haas.olivier.comptes.Ecriture;
import haas.olivier.util.Month;
import haas.olivier.comptes.Permanent;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.TreeSet;

/** Un Memento qui permet d'encapsuler les données avant de les sérialiser.
 * L'intérêt de cette classe est de permettre la sérialisation des données dans
 * un seul objet, de manière à conserver les références entre les objets.
 * 
 * @author Olivier Haas
 */
class DAOMemento implements Serializable {
	private static final long serialVersionUID = -6379449029979749397L;
	
	HashSet<Compte> comptes;
	HashSet<Permanent> permanents;
	TreeSet<Ecriture> ecritures;
	Map<Month,Map<Integer,BigDecimal>> historique, soldes, moyennes;
	Properties diagramProperties;
}
