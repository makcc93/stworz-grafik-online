package online.stworzgrafik.StworzGrafik.algorithm.analyzer.rest;

import online.stworzgrafik.StworzGrafik.algorithm.ScheduleGeneratorContext;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class RestAnalyzer {
    private Map<RestAnalyzeType, RestAnalyzerStrategy> strategies;

    public RestAnalyzer(List<RestAnalyzerStrategy> strategies){
        this.strategies = strategies.stream()
                .collect(
                        Collectors.toMap(
                                RestAnalyzerStrategy::getSupportedType,
                                s -> s
                        )
                );
    }

    public void analyzeAndResolve(ScheduleGeneratorContext context, RestAnalyzeType type){
        RestAnalyzerStrategy restAnalyzerStrategy = strategies.get(type);

        RestAnalyzerResult result = restAnalyzerStrategy.analyze(context);

        if (restAnalyzerStrategy.hasProblem(result)){
            restAnalyzerStrategy.resolve(context,result);
        }
    }
}
