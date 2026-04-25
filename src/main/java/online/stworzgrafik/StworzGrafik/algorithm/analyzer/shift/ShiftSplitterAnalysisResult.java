package online.stworzgrafik.StworzGrafik.algorithm.analyzer.shift;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public record ShiftSplitterAnalysisResult(int monthlyMaxWorkingDays, int employeeLowestValueOfWorkingDays)  implements ScheduleAnalysisResult{
        public boolean hasProblem() {
            return employeeLowestValueOfWorkingDays < monthlyMaxWorkingDays - 2;
        }
}
