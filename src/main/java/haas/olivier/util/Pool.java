/*
 * Copyright (c) 2018 Olivier HAAS - Tous droits réservés
 */
package haas.olivier.util;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/** Un pool de valeurs réutilisables.<br>
 * Pattern Flyweight.
 * <p>
 * Cette classe permet d'utiliser une instance unique pour des valeurs utilisées
 * fréquemment dans l'application.<br>
 * En principe, ce procédé n'a de sens que pour des valeurs immuables, ou en
 * tout cas qui ne sont pas modifiées pendant l'exécution. Sinon, les
 * changements effectués sur un objet pourraient se répercuter indument sur tous
 * les objets égaux à celui-ci et stockés dans le pool.
 * <p>
 * La classe est thread-safe.
 *
 * @author Olivier HAAS
 */
public class Pool {

	/** La collection des valeurs stockées dans le pool. */
	private Map<Object,Object> valeurs =
			Collections.synchronizedMap(new HashMap<Object,Object>());
	
	/** Renvoie l'objet unique, parmi les objets du pool, égal à un objet donné.
	 * <br>Si le pool ne contient pas d'objet égal à l'objet spécifié, alors cet
	 * objet est inséré dans le pool et renvoyé.
	 * <p>
	 * Le contrat garanti que les appels successifs à cette méthode, avec des
	 * arguments égaux au sens de <code>equals()</code>, renvoient toujours la
	 * même instance.
	 * <p>
	 * Bien sûr, les objets spécifiés doivent avoir une implémentation cohérente
	 * de <code>equals()</code>.
	 * 
	 * @param t	L'objet dont on veut l'instance égale dans le pool.
	 * 
	 * @return	Un objet unique égal à <code>t</code>, ou <code>t</code>
	 * 			lui-même.
	 */
	@SuppressWarnings("unchecked")
	public <T> T get(T t) {
		
		// Compléter si besoin
		if (!valeurs.containsKey(t))
			valeurs.put(t, t);
		
		// Récupérer la valeur
		return (T) valeurs.get(t);
	}// get
	
	/** Renvoie le nombre de valeurs actuellement dans le pool. */
	public int size() {
		return valeurs.size();
	}// size
}
