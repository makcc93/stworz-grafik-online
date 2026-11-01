package online.stworzgrafik.StworzGrafik.store.modificationHours;

public enum ShiftHourScope {
    START,
    END,
    BOTH;

    public boolean appliesToStart() {
        return this == START || this == BOTH;
    }

    public boolean appliesToEnd() {
        return this == END || this == BOTH;
    }

    public boolean overlaps(ShiftHourScope other) {
        return this == other || this == BOTH || other == BOTH;
    }
}