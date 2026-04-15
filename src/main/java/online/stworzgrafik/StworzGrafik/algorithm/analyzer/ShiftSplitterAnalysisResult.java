package online.stworzgrafik.StworzGrafik.algorithm.analyzer;

public record ShiftSplitterAnalysisResult(int monthlyMaxWorkingDays, int employeeLowestValueOfWorkingDays)  implements ScheduleAnalysisResult{
        public boolean hasProblem() { return employeeLowestValueOfWorkingDays >= monthlyMaxWorkingDays - 2;}
}
