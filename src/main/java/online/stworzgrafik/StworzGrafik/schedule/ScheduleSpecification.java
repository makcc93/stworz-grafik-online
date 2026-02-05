package online.stworzgrafik.StworzGrafik.schedule;

import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;

class ScheduleSpecification {
    static Specification<Schedule> hasId(Long scheduleId) {
        if (scheduleId == null) return null;

        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("schedule").get("id"), scheduleId);
    }

    static Specification<Schedule> hasStoreId(Long storeId) {
        if (storeId == null) return null;

        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("store").get("id"), storeId);
    }

    static Specification<Schedule> hasYear(Integer year) {
        if (year == null) return null;

        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("year"), year);
    }

    static Specification<Schedule> hasMonth(Integer month) {
        if (month == null) return null;

        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("month"), month);
    }

    static Specification<Schedule> hasName(String name) {
        if (name == null) return null;

        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("name"), name);
    }

    static Specification<Schedule> isCreatedAt(LocalDateTime createdAt) {
        if (createdAt == null) return null;

        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("createdAt"), createdAt);
    }

    static Specification<Schedule> isCreatedBy(Long createdByUserId) {
        if (createdByUserId == null) return null;

        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("createdByUserId"), createdByUserId);
    }

    static Specification<Schedule> isUpdatedAt(LocalDateTime updatedAt) {
        if (updatedAt == null) return null;

        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("updatedAt"), updatedAt);
    }

    static Specification<Schedule> isUpdatedBy(Long updatedByUserId) {
        if (updatedByUserId == null) return null;

        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("updatedByUserId"), updatedByUserId);
    }

    static Specification<Schedule> hasScheduleStatusName(String scheduleStatusName) {
        if (scheduleStatusName == null) return null;

        return (root, query, criteriaBuilder) -> {
            ScheduleStatus scheduleStatus = ScheduleStatus.valueOf(scheduleStatusName);

            return criteriaBuilder.equal(root.get("scheduleStatus"), scheduleStatus);
        };
    }
}
