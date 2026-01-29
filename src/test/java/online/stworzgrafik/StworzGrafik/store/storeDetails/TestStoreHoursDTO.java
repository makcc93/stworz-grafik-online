package online.stworzgrafik.StworzGrafik.store.storeDetails;

import online.stworzgrafik.StworzGrafik.store.storeDetails.DTO.StoreHoursDTO;

import java.time.LocalTime;

public class TestStoreHoursDTO {
        private LocalTime mondayOpen = LocalTime.of(8, 0);
        private LocalTime mondayClose = LocalTime.of(20, 0);
        private LocalTime tuesdayOpen = LocalTime.of(8, 0);
        private LocalTime tuesdayClose = LocalTime.of(20, 0);
        private LocalTime wednesdayOpen = LocalTime.of(8, 0);
        private LocalTime wednesdayClose = LocalTime.of(20, 0);
        private LocalTime thursdayOpen = LocalTime.of(8, 0);
        private LocalTime thursdayClose = LocalTime.of(20, 0);
        private LocalTime fridayOpen = LocalTime.of(8, 0);
        private LocalTime fridayClose = LocalTime.of(20, 0);
        private LocalTime saturdayOpen = LocalTime.of(9, 0);
        private LocalTime saturdayClose = LocalTime.of(18, 0);
        private LocalTime sundayOpen = LocalTime.of(10, 0);
        private LocalTime sundayClose = LocalTime.of(16, 0);

        public TestStoreHoursDTO withMondayOpen (LocalTime mondayOpen){
            this. mondayOpen = mondayOpen;
            return this;
        }

    public TestStoreHoursDTO withMondayClose(LocalTime mondayClose) {
        this.mondayClose = mondayClose;
        return this;
    }

    public TestStoreHoursDTO withTuesdayOpen(LocalTime tuesdayOpen) {
        this.tuesdayOpen = tuesdayOpen;
        return this;
    }

    public TestStoreHoursDTO withTuesdayClose(LocalTime tuesdayClose) {
        this.tuesdayClose = tuesdayClose;
        return this;
    }

    public TestStoreHoursDTO withWednesdayOpen(LocalTime wednesdayOpen) {
        this.wednesdayOpen = wednesdayOpen;
        return this;
    }

    public TestStoreHoursDTO withWednesdayClose(LocalTime wednesdayClose) {
        this.wednesdayClose = wednesdayClose;
        return this;
    }

    public TestStoreHoursDTO withThursdayOpen(LocalTime thursdayOpen) {
        this.thursdayOpen = thursdayOpen;
        return this;
    }

    public TestStoreHoursDTO withThursdayClose(LocalTime thursdayClose) {
        this.thursdayClose = thursdayClose;
        return this;
    }

    public TestStoreHoursDTO withFridayOpen(LocalTime fridayOpen) {
        this.fridayOpen = fridayOpen;
        return this;
    }

    public TestStoreHoursDTO withFridayClose(LocalTime fridayClose) {
        this.fridayClose = fridayClose;
        return this;
    }

    public TestStoreHoursDTO withSaturdayOpen(LocalTime saturdayOpen) {
        this.saturdayOpen = saturdayOpen;
        return this;
    }

    public TestStoreHoursDTO withSaturdayClose(LocalTime saturdayClose) {
        this.saturdayClose = saturdayClose;
        return this;
    }

    public TestStoreHoursDTO withSundayOpen(LocalTime sundayOpen) {
        this.sundayOpen = sundayOpen;
        return this;
    }

    public TestStoreHoursDTO withSundayClose(LocalTime sundayClose) {
        this.sundayClose = sundayClose;
        return this;
    }

    public StoreHoursDTO build(){
        return new StoreHoursDTO(
                mondayOpen,
                 mondayClose,
                 tuesdayOpen,
                 tuesdayClose,
                 wednesdayOpen,
                 wednesdayClose,
                 thursdayOpen,
                 thursdayClose,
                 fridayOpen,
                 fridayClose,
                 saturdayOpen,
                 saturdayClose,
                 sundayOpen,
                 sundayClose
        );
    }
}
