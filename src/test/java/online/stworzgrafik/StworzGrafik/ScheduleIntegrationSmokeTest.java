package online.stworzgrafik.StworzGrafik;

import online.stworzgrafik.StworzGrafik.store.Store;
import online.stworzgrafik.StworzGrafik.store.StoreEntityService;
import online.stworzgrafik.StworzGrafik.store.TestStoreBuilder;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;

class ScheduleIntegrationSmokeTest extends BaseIntegrationTest {
    @Autowired
    private StoreEntityService storeEntityService;

    @Test
    void shouldConnectToDatabaseAndSaveStore() {
        Store store = new TestStoreBuilder().build();
        Store saved = storeEntityService.saveEntity(store);

        assertThat(saved.getId()).isNotNull();
    }
}
