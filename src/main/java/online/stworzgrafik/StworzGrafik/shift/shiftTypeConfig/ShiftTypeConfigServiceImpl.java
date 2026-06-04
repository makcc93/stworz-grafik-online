package online.stworzgrafik.StworzGrafik.shift.shiftTypeConfig;

import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import online.stworzgrafik.StworzGrafik.shift.shiftTypeConfig.DTO.ShiftTypeConfigRequest;
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

    @Override
    public ShiftTypeConfig saveByShiftCode(ShiftCode shiftCode) {
        return shiftTypeConfigRepository.save(ShiftTypeConfig.builder()
                .code(shiftCode)
                .build()
        );
    }

    @Override
    public ShiftTypeConfig save(ShiftTypeConfig shiftTypeConfig) {
        return shiftTypeConfigRepository.save(shiftTypeConfig);
    }

    @Override
    public ShiftTypeConfig create(ShiftTypeConfigRequest request) {
        if (shiftTypeConfigRepository.existsByCode(request.shiftCode())) throw new EntityExistsException("Shift type config with code " + request.shiftCode() + " already exist");

        ShiftTypeConfig shiftTypeConfig = ShiftTypeConfig.builder()
                .code(request.shiftCode())
                .namePl(request.namePl())
                .defaultHours(request.defaultHours())
                .countsAsWork(request.countsAsWork())
                .build();

        return shiftTypeConfigRepository.save(shiftTypeConfig);
    }
}
