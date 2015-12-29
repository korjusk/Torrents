package org.korjus.movietorrents;

public enum SortOrderEnum {
    DEFAULT, LATEST, TOP_RATED, MOST_SEEDED, COSTUME;

    // Using int to save enum state to settings
    // This helps to convert int back to Enum
    public static SortOrderEnum fromInteger(int x) {
        switch (x) {
            case 0:
                return DEFAULT;
            case 1:
                return LATEST;
            case 2:
                return TOP_RATED;
            case 3:
                return MOST_SEEDED;
            case 4:
                return COSTUME;
        }
        return null;
    }
}
