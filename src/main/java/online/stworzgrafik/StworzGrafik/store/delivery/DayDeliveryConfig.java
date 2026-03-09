package online.stworzgrafik.StworzGrafik.store.delivery;

public record DayDeliveryConfig(
        boolean hasDelivery,
        int[] shiftAsArray
) {}
