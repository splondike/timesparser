package timesparser;

import java.util.Collections;
import java.util.Iterator;

/**
 * A typesafe version of something which may be null. Use like this:
 * 
 * Maybe<Integer> maybe = ...;
 * Integer default = 1234;
 * Integer value = maybe.otherwise(default);
 * 
 * or
 * 
 * if (maybe.isKnown()) {
 *   Integer value = maybe.iterator().next();
 * }
 * else {
 *   // Failure handling code
 * }
 * 
 * @see Edited this one to remove Guava dependencies: https://github.com/npryce/maybe-java/
 */
/* package */ abstract class Maybe<T> implements Iterable<T> {
    public abstract boolean isKnown();
    public abstract T otherwise(T defaultValue);
    public abstract Maybe<T> otherwise(Maybe<T> maybeDefaultValue);
    
    public static <T> Maybe<T> unknown() {
        return new Maybe<T>() {
            @Override
            public boolean isKnown() {
                return false;
            }

            public Iterator<T> iterator() {
                return Collections.<T>emptyList().iterator();
            }

            @Override
            public T otherwise(T defaultValue) {
                return defaultValue;
            }

            @Override
            public Maybe<T> otherwise(Maybe<T> maybeDefaultValue) {
                return maybeDefaultValue;
            }

            @Override
            public String toString() {
                return "unknown";
            }

            @Override
            public boolean equals(Object obj) {
                return false;
            }

            @Override
            public int hashCode() {
                return 0;
            }
        };
    }

    public static <T> Maybe<T> definitely(final T theValue) {
        return new DefiniteValue<T>(theValue);
    }

    private static class DefiniteValue<T> extends Maybe<T> {
        private final T theValue;

        public DefiniteValue(T theValue) {
            this.theValue = theValue;
        }

        @Override
        public boolean isKnown() {
            return true;
        }

        public Iterator<T> iterator() {
            return Collections.singleton(theValue).iterator();
        }

        @Override
        public T otherwise(T defaultValue) {
            return theValue;
        }

        @Override
        public Maybe<T> otherwise(Maybe<T> maybeDefaultValue) {
            return this;
        }

        @Override
        public String toString() {
            return "definitely " + theValue.toString();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            DefiniteValue<?> that = (DefiniteValue<?>) o;

            return theValue.equals(that.theValue);

        }

        @Override
        public int hashCode() {
            return theValue.hashCode();
        }
    }
}