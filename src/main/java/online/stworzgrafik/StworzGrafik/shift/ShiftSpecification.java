package online.stworzgrafik.StworzGrafik.shift;

import org.springframework.data.jpa.domain.Specification;

import java.time.LocalTime;

class ShiftSpecification {
    static Specification<Shift> hasStartHour(LocalTime startHour){
        if (startHour == null) return null;

        return (root, query, criteriaBuilder) ->
            criteriaBuilder.equal(root.get("startHour"),startHour);
    }

    static Specification<Shift> hasEndHour(LocalTime endHour){
        if (endHour == null) return null;

        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("endHour"),endHour);
    }
}
