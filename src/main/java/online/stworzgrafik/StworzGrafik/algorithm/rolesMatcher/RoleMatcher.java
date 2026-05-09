package online.stworzgrafik.StworzGrafik.algorithm.rolesMatcher;

import online.stworzgrafik.StworzGrafik.algorithm.ScheduleGeneratorContext;

import java.time.LocalDate;

public interface RoleMatcher {
    void reassignRolesForMonth(ScheduleGeneratorContext context);
    void assignRolesForMonth(ScheduleGeneratorContext context);
    void match(ScheduleGeneratorContext context, LocalDate date);
}
