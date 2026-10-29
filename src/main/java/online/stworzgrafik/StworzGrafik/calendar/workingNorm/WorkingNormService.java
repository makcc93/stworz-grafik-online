package online.stworzgrafik.StworzGrafik.calendar.workingNorm;

import online.stworzgrafik.StworzGrafik.calendar.workingNorm.DTO.WorkingNormInitializationResponse;
import online.stworzgrafik.StworzGrafik.calendar.workingNorm.DTO.WorkingNormRequest;
import online.stworzgrafik.StworzGrafik.calendar.workingNorm.DTO.WorkingNormResponse;

import java.util.List;

public interface WorkingNormService {
    WorkingNormResponse update(int year, int month, WorkingNormRequest dto);
    WorkingNormResponse resetToDefault(int year, int month);
    WorkingNormInitializationResponse initializeDefaults();
    WorkingNormResponse findByYearMonth(int year, int month);
    List<WorkingNormResponse> findAllByYear(int year);
    int getHours(int year, int month);
}