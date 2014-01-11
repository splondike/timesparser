This library parses free form time ranges (i.e. opening times) into something machine readable.

It can be used like this:

    Maybe<WeekIntervals> result = TimeExtractor.parseTimes("Mon-Fri 9-2pm");
    if (result.isKnown() {
       // Parse successful
       WeekIntervals intervals = result.iterator().next();
       Calendar now = Calendar.getInstance();
       if (intervals.contains(now)) {
          System.out.println("Venue open!");
       }
       else {
          System.out.println("Venue closed.");
       }
    }
    else {
       ...
    }

The library was originally made to parse free form open times supplied by the HappyCow web services.

Building
========
The library is built using maven. Once you've got that installed, just run mvn package and the library jar should appear in the target/ directory.
