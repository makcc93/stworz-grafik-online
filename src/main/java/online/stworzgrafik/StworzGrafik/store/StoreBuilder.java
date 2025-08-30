package online.stworzgrafik.StworzGrafik.store;

import online.stworzgrafik.StworzGrafik.exception.ArgumentNullChecker;
import org.springframework.stereotype.Component;

import java.time.LocalTime;

@Component
public final class StoreBuilder {

    public Store createStore(
            String name,
            String storeCode,
            String location,
            BranchType branch,
            RegionType region,
            LocalTime openHour,
            LocalTime closeHour
    ){
        ArgumentNullChecker.checkAll(name,storeCode,location,branch,region,openHour,closeHour);
        validateHour(openHour,closeHour);

        return Store.builder()
                .name(name)
                .storeCode(storeCode)
                .location(location)
                .branch(branch)
                .region(region)
                .openForClientsHour(openHour)
                .closeForClientsHour(closeHour)
                .build();
    }

    private static void validateHour(LocalTime openHour, LocalTime closeHour) {
        if (closeHour.isBefore(openHour)){
            throw new IllegalArgumentException("Close hour cannot be before open hour");
        }
    }
}
