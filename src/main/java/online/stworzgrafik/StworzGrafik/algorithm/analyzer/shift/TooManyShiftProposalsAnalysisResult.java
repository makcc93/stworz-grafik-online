package online.stworzgrafik.StworzGrafik.algorithm.analyzer.shift;

import java.util.Arrays;

public record TooManyShiftProposalsAnalysisResult(int[] originalDailyDraft, int[] proposalsCount) implements ScheduleAnalysisResult{
    public boolean hasProblem(){
        if (Arrays.stream(originalDailyDraft).sum() < 1) return false;

        for (int indexHour = 0; indexHour < originalDailyDraft.length; indexHour++) {
            if (originalDailyDraft[indexHour] < proposalsCount[indexHour]) return true;
        }

        return false;
    }
}
