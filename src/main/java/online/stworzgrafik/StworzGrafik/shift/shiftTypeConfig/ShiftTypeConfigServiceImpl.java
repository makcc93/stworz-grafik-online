package online.stworzgrafik.StworzGrafik.shift.shiftTypeConfig;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Objects;

@Service
public class ShiftTypeConfigServiceImpl implements ShiftTypeConfigService {
    private final ShiftTypeConfigRepository shiftTypeConfigRepository;
    private final String nullMessageForShiftCode = "Shift code cannot be null";

    public ShiftTypeConfigServiceImpl(ShiftTypeConfigRepository shiftTypeConfigRepository) {
        this.shiftTypeConfigRepository = shiftTypeConfigRepository;
    }

    @Override
    public ShiftTypeConfig findByCode(ShiftCode code){

        Objects.requireNonNull(code, nullMessageForShiftCode);

        return shiftTypeConfigRepository.findByCode(code)
                .orElseThrow(() -> new EntityNotFoundException("Cannot find shift type config by code " + code));
    }

    @Override
    public BigDecimal getDefaultHours(ShiftCode code){
        Objects.requireNonNull(code, nullMessageForShiftCode);

        return shiftTypeConfigRepository.getDefaultHours(code);
    }

    @Override
    public Boolean countsAsWork(ShiftCode code){
        Objects.requireNonNull(code, nullMessageForShiftCode);

        return shiftTypeConfigRepository.countsAsWork(code);
    }
}
