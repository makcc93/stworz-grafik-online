package online.stworzgrafik.StworzGrafik.algorithm.analyzer.rest;

import online.stworzgrafik.StworzGrafik.algorithm.ScheduleGeneratorContext;

public interface RestAnalyzerStrategy {
    RestAnalyzeType getSupportedType();
    RestAnalyzerResult analyze(ScheduleGeneratorContext context);
    boolean hasProblem(RestAnalyzerResult result);
    void resolve(ScheduleGeneratorContext context, RestAnalyzerResult result);
}
