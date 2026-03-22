package online.stworzgrafik.StworzGrafik.algorithm;

import online.stworzgrafik.StworzGrafik.algorithm.analyzer.AnalyzeType;
import online.stworzgrafik.StworzGrafik.algorithm.analyzer.ScheduleAnalyzer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Collections;

@ExtendWith(MockitoExtension.class)
class TooManyProposalsAnalysisStrategyTest {
    @InjectMocks
    private ScheduleAnalyzer analyzer;

    @Test
    void resolve_workingTest(){
        //given
        LocalDate date = LocalDate.now();
        int[] originalDailyDraft = {0,0,0,0,0,0,0,0,2,4,6,6,6,6,7,7,7,7,7,5,0,0,0,0};
        int[] proposalCount = {0,0,0,0,0,0,0,0,3,3,3,3,3,3,0,0,0,0,0,0,0,0,0,0};
        ScheduleGeneratorContext context = new TestScheduleGeneratorContext().build();

        //when
        analyzer.analyzeAndResolve(context,date, Collections.emptyList(),Collections.emptyList(), AnalyzeType.TOO_MANY_PROPOSALS);

        //then

    }

}