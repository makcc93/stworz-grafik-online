package online.stworzgrafik.StworzGrafik.algorithm.analyzer;

public record TooManyProposalsAnalysisResult(int[] originalDailyDraft, int[] proposalsCount) implements ScheduleAnalysisResult{
    public boolean hasProblem(){
        for (int indexHour = 0; indexHour < originalDailyDraft.length; indexHour++) {
            if (originalDailyDraft[indexHour] < proposalsCount[indexHour]) return true;
        }

        return false;
    }
}
