package online.stworzgrafik.StworzGrafik.algorithm;

import online.stworzgrafik.StworzGrafik.employee.Employee;
import online.stworzgrafik.StworzGrafik.shift.Shift;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Validated
interface DailyShiftGeneratorAlgorithmService {
    Map<LocalDate, List<Shift>> generateDailyShifts(
            LocalDate date,
            Map<LocalDate, int[]> everyDayStoreDemandDraft,
            Map<LocalDate, Map<Employee, int[]>> monthlyEmployeesProposalShiftsByDate,

    )
}
