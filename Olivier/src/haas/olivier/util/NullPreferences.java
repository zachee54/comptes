/*
 * Copyright (c) 2018 Olivier HAAS - Tous droits réservés
 */
package haas.olivier.util;

import java.util.HashMap;
import java.util.Map;
import java.util.prefs.AbstractPreferences;
import java.util.prefs.BackingStoreException;

/**
 * De fausses préférences ne lisant pas les préférences sauvegardée et ne
 * sauvegardant rien.
 * <p>
 * Cette implémentation de {@link java.util.prefs.Preferences Preferences} peut
 * notamment être utilisée en environnement de test ou de développement, afin de
 * ne pas interférer avec les préférences du système.
 *
 * @author Olivier HAAS
 */
public class NullPreferences extends AbstractPreferences {
	
	/**
	 * La collection de clés et valeurs stockées.<br>
	 * Cette collection ne contient que les valeurs qui y ont été insérées
	 * depuis l'instanciation.
	 */
	private final Map<String, String> map = new HashMap<String, String>();
	
	/**
	 * Les noeuds fils, en fonction de leurs noms.
	 */
	private final Map<String, NullPreferences> children =
			new HashMap<String, NullPreferences>();

	/**
	 * Construit des préférences vides.
	 */
	public NullPreferences() {
		super(null, "");
	}

	@Override
	protected void putSpi(String key, String value) {
		map.put(key, value);
	}

	@Override
	protected String getSpi(String key) {
		return map.get(key);
	}

	@Override
	protected void removeSpi(String key) {
		map.remove(key);
	}

	@Override
	protected void removeNodeSpi() throws BackingStoreException {
		map.clear();
		children.clear();
	}

	@Override
	protected String[] keysSpi() throws BackingStoreException {
		return map.keySet().toArray(new String[map.size()]);
	}

	@Override
	protected String[] childrenNamesSpi() throws BackingStoreException {
		return children.keySet().toArray(new String[children.size()]);
	}

	@Override
	protected AbstractPreferences childSpi(String name) {
		if (!children.containsKey(name)) {
			children.put(name, new NullPreferences());
		}
		return children.get(name);
	}

	@Override
	protected void syncSpi() throws BackingStoreException {
		/* Pas de stockage persistant donc pas de synchronisation */
	}

	@Override
	protected void flushSpi() throws BackingStoreException {
		/* Pas de stockage persistant donc pas de flush */
	}
}
