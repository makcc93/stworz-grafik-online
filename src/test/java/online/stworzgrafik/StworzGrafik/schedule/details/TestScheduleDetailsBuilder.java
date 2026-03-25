package online.stworzgrafik.StworzGrafik.schedule.details;

import online.stworzgrafik.StworzGrafik.employee.Employee;
import online.stworzgrafik.StworzGrafik.employee.TestEmployeeBuilder;
import online.stworzgrafik.StworzGrafik.schedule.Schedule;
import online.stworzgrafik.StworzGrafik.schedule.TestScheduleBuilder;
import online.stworzgrafik.StworzGrafik.shift.Shift;
import online.stworzgrafik.StworzGrafik.shift.TestShiftBuilder;
import online.stworzgrafik.StworzGrafik.shift.shiftTypeConfig.ShiftTypeConfig;
import online.stworzgrafik.StworzGrafik.shift.shiftTypeConfig.TestShiftTypeConfigBuilder;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class TestScheduleDetailsBuilder {
    private Long id = null;
    private Schedule schedule = new TestScheduleBuilder().build();
    private Employee employee = new TestEmployeeBuilder().build();
    private LocalDate date = LocalDate.of(2021,11,21);
    private Shift shift = new TestShiftBuilder().build();
    private ShiftTypeConfig shiftTypeConfig = new TestShiftTypeConfigBuilder().build();
    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt = null;

    public TestScheduleDetailsBuilder withId(Long id) {
        this.id = id;
        return this;
    }

    public TestScheduleDetailsBuilder withSchedule(Schedule schedule) {
        this.schedule = schedule;
        return this;
    }

    public TestScheduleDetailsBuilder withEmployee(Employee employee) {
        this.employee = employee;
        return this;
    }

    public TestScheduleDetailsBuilder withDate(LocalDate date) {
        this.date = date;
        return this;
    }

    public TestScheduleDetailsBuilder withShift(Shift shift) {
        this.shift = shift;
        return this;
    }

    public TestScheduleDetailsBuilder withShiftTypeConfig(ShiftTypeConfig shiftTypeConfig) {
        this.shiftTypeConfig = shiftTypeConfig;
        return this;
    }

    public TestScheduleDetailsBuilder withCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
        return this;
    }

    public TestScheduleDetailsBuilder withUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
        return this;
    }

    public ScheduleDetails build() {
        ScheduleDetails details = new ScheduleDetails();
        details.setId(id);
        details.setSchedule(schedule);
        details.setEmployee(employee);
        details.setDate(date);
        details.setShift(shift);
        details.setShiftTypeConfig(shiftTypeConfig);
        details.setCreatedAt(createdAt);
        details.setUpdatedAt(updatedAt);
        return details;
    }
}
