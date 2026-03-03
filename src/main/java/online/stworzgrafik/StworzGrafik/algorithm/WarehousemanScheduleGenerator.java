package online.stworzgrafik.StworzGrafik.algorithm;

import de.jollyday.HolidayManager;
import lombok.RequiredArgsConstructor;
import online.stworzgrafik.StworzGrafik.employee.Employee;
import online.stworzgrafik.StworzGrafik.employee.EmployeeEntityService;
import online.stworzgrafik.StworzGrafik.store.Store;
import online.stworzgrafik.StworzGrafik.store.StoreEntityService;
import online.stworzgrafik.StworzGrafik.store.delivery.StoreDelivery;
import online.stworzgrafik.StworzGrafik.store.delivery.StoreDeliveryService;
import online.stworzgrafik.StworzGrafik.store.delivery.StoreWeeklyDeliverySchedule;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalDate;

@Component
@RequiredArgsConstructor
public class WarehousemanScheduleGenerator {
    private final EmployeeEntityService employeeEntityService;
    private final StoreEntityService storeEntityService;
    private final StoreDeliveryService storeDeliveryService;
    private final HolidayManager holidayManager;

    public void generate(Long storeId, Integer year, Integer month){
        Store store = storeEntityService.getEntityById(storeId);
        StoreDelivery storeDelivery = store.getDelivery();
        Employee primaryEmployee = storeDelivery.getPrimaryEmployee();

        //todo
        //implementacja generowanie grafika magazyniera jesli istnieje
        //kolejne kwestie
        //draft na dzien tygodnia przeniesiony na wszystkie wystapienia danego dnia np. wszytskich poneidizalkow
        //podobnie tutaj generowanie grafika magazyniera np dla wszytskich podziedziakow, wtorkow w danym miesiacu
        StoreWeeklyDeliverySchedule storeWeeklyDeliverySchedule = storeDelivery.getStoreWeeklyDeliverySchedule();
        storeWeeklyDeliverySchedule
    }
}
