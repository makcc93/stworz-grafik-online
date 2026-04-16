package online.stworzgrafik.StworzGrafik.algorithm.analyzer;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public record ShiftSplitterAnalysisResult(int monthlyMaxWorkingDays, int employeeLowestValueOfWorkingDays)  implements ScheduleAnalysisResult{
        public boolean hasProblem() {
            boolean problem = employeeLowestValueOfWorkingDays < monthlyMaxWorkingDays - 2;
            log.info("has problem? = {}",problem );

            return problem;
        }
}
