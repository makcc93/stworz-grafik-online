package online.stworzgrafik.StworzGrafik.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class IntArrayJsonConverter implements AttributeConverter<int[], String> {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(int[] ints) {
        if (ints == null){
            return null;
        }

        try {
            return objectMapper.writeValueAsString(ints);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to convert int[] to JSON", e);
        }
    }

    @Override
    public int[] convertToEntityAttribute(String json) {
        if (json == null){
            return null;
        }

        try {
            return objectMapper.readValue(json, int[].class);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to convert JSON to int[]", e);
        }
    }
}
