package online.stworzgrafik.StworzGrafik.shift.shiftTypeConfig;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class ShiftTypeConfigServiceImpl implements ShiftTypeConfigService {
    private final ShiftTypeConfigRepository shiftTypeConfigRepository;

    public ShiftTypeConfigServiceImpl(ShiftTypeConfigRepository shiftTypeConfigRepository) {
        this.shiftTypeConfigRepository = shiftTypeConfigRepository;
    }

    @Override
    public ShiftTypeConfig findByCode(ShiftCode code){
        return shiftTypeConfigRepository.findByCode(code)
                .orElseThrow(() -> new EntityNotFoundException("Cannot find shift type config by code " + code));
    }

    @Override
    public BigDecimal getDefaultHours(ShiftCode code){
        return shiftTypeConfigRepository.getDefaultHours(code)
                .orElse(BigDecimal.ZERO);
    }

    @Override
    public Boolean countsAsWork(ShiftCode code){
        return shiftTypeConfigRepository.countsAsWork(code);
    }

    @Override
    public ShiftTypeConfig findById(Long shiftTypeConfigId) {
        return shiftTypeConfigRepository.findById(shiftTypeConfigId)
                .orElseThrow(() -> new EntityNotFoundException("Cannot find shift type config by id" + shiftTypeConfigId));
    }
}
