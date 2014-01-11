package timesparser;

import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import junit.framework.TestCase;
import timesparser.TimeDescriptionParser.Comma;
import timesparser.TimeDescriptionParser.Day;
import timesparser.TimeDescriptionParser.DayRange;
import timesparser.TimeDescriptionParser.ParseResult;
import timesparser.TimeDescriptionParser.TimeDescriptionElement;
import timesparser.TimeDescriptionParser.TimeRange;

public class TimeDescriptionParserUnitTest extends TestCase {
	public void testParseSentence() {
		String testSentence = "Mon-Sun 11-3pm, something irrelevant, Tue 9am - 11am";
		List<TimeDescriptionElement> expectedParse = Arrays.asList(
			new DayRange(Calendar.MONDAY, Calendar.SUNDAY),
			new TimeRange(new LocalTime(11,0), new LocalTime(15,0)),
			new Comma(),
			new Comma(),
			new Day(Calendar.TUESDAY),
			new TimeRange(new LocalTime(9,0), new LocalTime(11,0))
		);

		assertEquals(expectedParse, TimeDescriptionParser.parse(testSentence));
	}

	public void testDayParsePositive() {
		String sentence = "tue starts";
		ParseResult<Day> result = Day.parse(sentence).iterator().next();
		assertEquals(" starts", result.remainder);
		assertEquals(Integer.valueOf(Calendar.TUESDAY), result.element.day);
	}

	public void testDayParseNegative() {
		String sentence = "thursty times";
		assertFalse(Day.parse(sentence).isKnown());
	}

	public void testDayRangeParsePositive() {
		String sentence = "thur- sun starts";
		ParseResult<DayRange> result = DayRange.parse(sentence).iterator().next();
		assertEquals(" starts", result.remainder);
		assertEquals(Integer.valueOf(Calendar.THURSDAY), result.element.startDay);
		assertEquals(Integer.valueOf(Calendar.SUNDAY), result.element.endDay);
	}

	public void testDayRangeParseNegative() {
		String sentence = "starts";
		assertFalse(DayRange.parse(sentence).isKnown());
	}

	public void testDayRangeParseDaily() {
		String sentence = "daily starts";
		ParseResult<DayRange> result = DayRange.parse(sentence).iterator().next();
		assertEquals(" starts", result.remainder);
		assertEquals(Integer.valueOf(Calendar.MONDAY), result.element.startDay);
		assertEquals(Integer.valueOf(Calendar.SUNDAY), result.element.endDay);
	}

	public void testTimeRangeParseFullySpecified() {
		String sentence = "9:30am- 2pm starts";
		ParseResult<TimeRange> result = TimeRange.parse(sentence).iterator().next();
		assertEquals(" starts", result.remainder);
		assertEquals(new LocalTime(9, 30), result.element.startTime);
		assertEquals(new LocalTime(14, 0), result.element.endTime);
	}

	public void testTimeRangeParseInferAm() {
		String sentence = "9:30 -2pm starts";
		ParseResult<TimeRange> result = TimeRange.parse(sentence).iterator().next();
		assertEquals(" starts", result.remainder);
		assertEquals(new LocalTime(9, 30), result.element.startTime);
		assertEquals(new LocalTime(14, 0), result.element.endTime);
	}

	public void testTimeRangeParseInferAfternoonPm() {
		String sentence = "1- 2pm starts";
		ParseResult<TimeRange> result = TimeRange.parse(sentence).iterator().next();
		assertEquals(" starts", result.remainder);
		assertEquals(new LocalTime(13, 0), result.element.startTime);
		assertEquals(new LocalTime(14, 0), result.element.endTime);
	}

	public void testTimeRangeParseInferNightPm() {
		String sentence = "11- 2am starts";
		ParseResult<TimeRange> result = TimeRange.parse(sentence).iterator().next();
		assertEquals(" starts", result.remainder);
		assertEquals(new LocalTime(23, 0), result.element.startTime);
		assertEquals(new LocalTime(2, 0), result.element.endTime);
	}

	public void testTimeRangeParseNoon() {
		String sentence = "12 - 1pm starts";
		ParseResult<TimeRange> result = TimeRange.parse(sentence).iterator().next();
		assertEquals(" starts", result.remainder);
		assertEquals(new LocalTime(12, 0), result.element.startTime);
		assertEquals(new LocalTime(13, 0), result.element.endTime);
	}
}