package online.stworzgrafik.StworzGrafik.store.delivery;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lombok.RequiredArgsConstructor;

import java.time.DayOfWeek;
import java.util.Map;

@Converter
@RequiredArgsConstructor
class WeeklyScheduleConverter implements AttributeConverter<Map<DayOfWeek, DayDeliveryConfig>, String> {
    private final ObjectMapper mapper;

    @Override
    public String convertToDatabaseColumn(Map<DayOfWeek, DayDeliveryConfig> dayOfWeekDayDeliveryConfigMap) {
        try {
            return mapper.writeValueAsString(dayOfWeekDayDeliveryConfigMap);
        } catch (Exception e){
            throw new RuntimeException(e);
        }
    }

    @Override
    public Map<DayOfWeek, DayDeliveryConfig> convertToEntityAttribute(String databaseJson) {
        try{
            return mapper.readValue(databaseJson, new TypeReference<Map<DayOfWeek, DayDeliveryConfig>>() {});
        } catch (Exception e){
            throw new RuntimeException(e);
        }
    }
}
