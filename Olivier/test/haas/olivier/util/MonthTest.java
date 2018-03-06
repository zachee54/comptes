package haas.olivier.util;

import static org.junit.Assert.*;

import haas.olivier.util.Month;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class MonthTest {

	private static DateFormat parser;
	private Month month;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		parser = new SimpleDateFormat("dd/MM/yy");
		new SimpleDateFormat("dd/MM/yy hh:mm:ss.SSS");
	}

	@Before
	public void setUp() throws Exception {
		month = new Month(parser.parse("31/03/13"));
	}

	@Test
	public void testMonth() {
		Month month1 = new Month();
		Month month2 = new Month(new Date());
		assertEquals(month1, month2);
	}

	@Test
	public void testMonthDate() throws ParseException {
		Date dateBefore = parser.parse("01/02/13");
		Date dateAfter = parser.parse("01/04/13");
		assertTrue(month.before(dateAfter));
		assertTrue(month.after(dateBefore));
	}

	@Test
	public void testMonthDateBissextile() throws ParseException {
		Month month = new Month(parser.parse("29/02/12"));
		Date date = parser.parse("01/02/12");
		assertTrue(month.includes(date));
	}

	@Test
	public void testMonthLong() throws ParseException {
		Date date = parser.parse("31/03/13");
		long time = date.getTime();
		Month month1 = new Month(time);
		Month month2 = new Month(date);
		assertEquals(month1, month2);
	}

	@Test
	public void testGetYear() {
		assertEquals(2013, month.getYear());
		assertEquals(2012, month.getTranslated(-3).getYear());
		assertEquals(2014, month.getTranslated(10).getYear());
	}
	
	@Test
	public void testGetNumInYear() {
		assertEquals(3, month.getNumInYear());
		assertEquals(12, month.getTranslated(-3).getNumInYear());
		assertEquals(1, month.getTranslated(10).getNumInYear());
	}
	
	@Test
	public void testGetTranslatedinPast() throws ParseException {
		Month month2 = new Month(parser.parse("30/11/2012"));
		assertEquals(month.getTranslated(-4), month2);
	}

	@Test
	public void testGetTranslatedinFuture() throws ParseException {
		Month month3 = new Month(parser.parse("30/04/2015"));
		assertEquals(month.getTranslated(25), month3);
	}

	@Test
	public void testGetNext() throws ParseException {
		Month month2 = new Month(parser.parse("15/04/13"));
		assertEquals(month.getNext(), month2);
	}

	@Test
	public void testGetPrevious() throws ParseException {
		Month month2 = new Month(parser.parse("03/02/13"));
		assertEquals(month.getPrevious(), month2);
	}

	@Test
	public void testIncludesDate() throws ParseException {
		assertTrue(month.includes(parser.parse("14/03/13")));
		assertFalse(month.includes(parser.parse("28/02/13")));
		assertFalse(month.includes(parser.parse("15/04/13")));
		assertFalse(month.includes(parser.parse("31/03/14")));
	}

	@Test
	public void testAfterDate() throws ParseException {
		assertTrue(month.after(parser.parse("15/02/13")));
	}

	@Test
	public void testNotAfterSameDate() throws ParseException {
		assertFalse(month.after(parser.parse("04/03/13")));
	}

	@Test
	public void testNotAfterDate() throws ParseException {
		assertFalse(month.after(parser.parse("18/07/13")));
	}

	@Test
	public void testBeforeDate() throws ParseException {
		assertTrue(month.before(parser.parse("03/04/13")));
	}

	@Test
	public void testNotBeforeSameDate() throws ParseException {
		assertFalse(month.before(parser.parse("01/03/13")));
	}

	@Test
	public void testNotBeforeDate() throws ParseException {
		assertFalse(month.before(parser.parse("04/02/13")));
	}

	@Test
	public void testToString() {
		assertEquals("mars 2013", month.toString());
	}
}
