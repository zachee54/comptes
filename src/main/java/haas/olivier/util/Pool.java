/*
 * Copyright (c) 2018 Olivier HAAS - Tous droits réservés
 */
package haas.olivier.util;

import java.lang.ref.WeakReference;
import java.util.WeakHashMap;

/**
 * Un pool de valeurs réutilisables.<br>
 * Pattern Flyweight.
 * <p>
 * Cette classe permet d'utiliser une instance unique pour des valeurs utilisées
 * fréquemment dans l'application.<br>
 * Lorsqu'un objet n'est plus utilisé (collecté par le ramasse-miettes), il est
 * supprimé du pool.
 * <p>
 * Cette classe ne peut être utilisée qu'avec des objets immuables, ou en tout
 * cas non modifiés pendant leur utilisation. Sinon, cela aurait pour
 * conséquence non seulement de perturber le stockage par clé de hachage dans le
 * pool, mais aussi cela rendrait visibles les modifications faites par un objet
 * à tous les objets utilisant l'instance modifiée.
 * <p>
 * La classe est thread-safe.
 *
 * @author Olivier HAAS
 */
public class Pool {

	/**
	 * La collection des objets stockés dans le pool.
	 */
	private WeakHashMap<Object, WeakReference<Object>> valeurs =
			new WeakHashMap<Object, WeakReference<Object>>();
	
	/**
	 * Renvoie l'objet unique, parmi les objets du pool, égal à un objet donné.
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
	public synchronized <T> T get(T t) {
		WeakReference<Object> reference = valeurs.get(t);
		if (reference != null) {				// Un objet égal a été inséré
			Object value = reference.get();
			if (value != null) {				// et n'a pas été collecté
				return (T) value;				// Renvoyer l'objet connu
			}
		}
		
		/* Objet non trouvé : l'insérer dans le pool et le renvoyer */
		valeurs.put(t, new WeakReference<Object>(t));
		return t;
	}
}
