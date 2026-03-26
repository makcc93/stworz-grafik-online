package online.stworzgrafik.StworzGrafik.calendar.holidays;

import de.focus_shift.jollyday.core.HolidayCalendar;
import de.focus_shift.jollyday.core.HolidayManager;
import de.focus_shift.jollyday.core.ManagerParameters;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class HolidayConfig {

    @Bean
    public HolidayManager polishHolidayManager(){
        return HolidayManager.getInstance(ManagerParameters.create(HolidayCalendar.POLAND));
    }
}
