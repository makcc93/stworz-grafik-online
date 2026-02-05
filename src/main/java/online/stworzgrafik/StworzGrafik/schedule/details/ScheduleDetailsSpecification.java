package online.stworzgrafik.StworzGrafik.schedule.details;

import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;

class ScheduleDetailsSpecification {
    static Specification<ScheduleDetails> hasScheduleId(Long scheduleId) {
        if (scheduleId == null) return null;

        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("schedule").get("id"), scheduleId);
    }

    static Specification<ScheduleDetails> hasId(Long scheduleDetailsId){
        if (scheduleDetailsId == null) return null;

        return (root,query,criteriaBuilder) ->
                criteriaBuilder.equal(root.get("id"),scheduleDetailsId);
    }

    static Specification<ScheduleDetails> hasEmployeeId(Long employeeId){
        if (employeeId == null) return null;

        return (root,query,criteriaBuilder) ->
                criteriaBuilder.equal(root.get("employee").get("id"),employeeId);
    }

    static Specification<ScheduleDetails> hasDate(LocalDate date){
        if (date == null) return null;

        return (root,query,criteriaBuilder) ->
                criteriaBuilder.equal(root.get("date"),date);
    }

    static Specification<ScheduleDetails> hasShift(Long shiftId){
        if (shiftId == null) return null;

        return (root,query,criteriaBuilder) ->
                criteriaBuilder.equal(root.get("shift").get("id"),shiftId);
    }

    static Specification<ScheduleDetails> hasShiftTypeConfig(Long shiftTypeConfigId){
        if (shiftTypeConfigId == null) return null;

        return (root,query,criteriaBuilder) ->
                criteriaBuilder.equal(root.get("shiftTypeConfig").get("id"),shiftTypeConfigId);
    }
}
