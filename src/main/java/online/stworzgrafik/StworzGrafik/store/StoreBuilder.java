package online.stworzgrafik.StworzGrafik.store;

import online.stworzgrafik.StworzGrafik.branch.Branch;
import org.springframework.stereotype.Component;

@Component
public final class StoreBuilder {

    public Store createStore(
            String name,
            String storeCode,
            String location,
            Branch branch
    ){
        return Store.builder()
                .name(name)
                .storeCode(storeCode)
                .location(location)
                .branch(branch)
                .build();

    }
}
