module comptes {
	exports haas.olivier.comptes.dao.xml;
	exports haas.olivier.comptes;
	exports haas.olivier.gui.table;
	exports haas.olivier.info;
	exports haas.olivier.comptes.dao.xml.jaxb.props;
	exports haas.olivier.comptes.dao.cache;
	exports haas.olivier.diagram;
	exports haas.olivier.gui;
	exports haas.olivier.comptes.gui.table;
	exports haas.olivier.gui.util;
	exports haas.olivier.comptes.dao;
	exports haas.olivier.comptes.dao.csv;
	exports haas.olivier.comptes.gui.diagram;
	exports haas.olivier.comptes.info;
	exports haas.olivier.comptes.dao.xml.jaxb.banq;
	exports haas.olivier.comptes.gui.settings;
	exports haas.olivier.comptes.gui;
	exports haas.olivier.comptes.dao.xml.jaxb.perm;
	exports com.csvreader;
	exports haas.olivier.comptes.gui.actions;
	exports haas.olivier.util;
	exports haas.olivier.autocompletion;
	exports haas.olivier.comptes.ctrl;

	requires java.desktop;
	requires java.logging;
	requires java.prefs;
	requires java.sql;
	requires java.xml;
	requires junit;
	requires java.xml.bind;
}