package haas.olivier.info;

import static org.mockito.Mockito.*;

import java.util.logging.LogManager;

import haas.olivier.info.HandledException;
import haas.olivier.info.MessageHandler;
import haas.olivier.info.MessageListener;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class MessageHandlerTest {

	// Un Mock
	private MessageListener ml;
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
		ml = mock(MessageListener.class);
		MessageHandler.addMessageListener(ml);
	}// setUp

	@After
	public void tearDown() throws Exception {
		MessageHandler.removeMessageListener(ml);
	}

//	@Test
//	public void testRemoveMessageListener() {
//		
//		// Ajouter encore le Mock une ou deux fois (en plus de l'initialisation)
//		MessageHandler.addMessageListener(ml);
//		MessageHandler.addMessageListener(ml);
//		
//		// Méthode testée
//		MessageHandler.removeMessageListener(ml);
//		
//		// Lancer des notifications
//		try {
//			MessageHandler.notifyError("", new Exception());
//		} catch (HandledException e) {
//		}
//		MessageHandler.notifyInfo("");
//		MessageHandler.notifyWarning("", new Exception());
//		
//		// Vérifier que le Mock n'a pas été appelé
//		verifyZeroInteractions(ml);
//		
//		// Vérifier qu'un nouvel appel de la méthode testée ne plante pas
//		MessageHandler.removeMessageListener(ml);
//	}// testRemoveMessageListener

	@Test
	public void testNotifyError() {
		Exception e = new Exception();
		String s = "erreur";
		
		// Éviter l'affichage en console pendant le test
		LogManager.getLogManager().reset();
		
		// Méthode testée
		try {
			MessageHandler.notifyError(s, e);
			org.junit.Assert.fail("Pas d'HandledException levée");
		} catch (HandledException e1) {
		}
//		verify(ml).error(s, e);
	}

//	@Test
//	public void testNotifyWarning() {
//		Exception e = new Exception();
//		String s = "warning";
//		
//		// Méthode testée
//		MessageHandler.notifyWarning(s, e);
//		verify(ml).warning(s, e);
//	}// testNotifyWarning

//	@Test
//	public void testNotifyInfo() {
//		String s = "info";
//		MessageHandler.notifyInfo(s);
//		verify(ml).info(s);
//	}// testNotifyInfo

}
