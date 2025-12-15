package online.stworzgrafik.StworzGrafik.draft.controller;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@NoArgsConstructor
@Getter
@Setter
public class DraftSearchCriteria {
//    private Integer year;
//    private Integer month;
//    private Integer day;
//
//    private Integer startYear;
//    private Integer startMonth;
//    private Integer startDay;
//    private Integer endYear;
//    private Integer endMonth;
//    private Integer endDay;
    private LocalDate startDate;
    private LocalDate endDate;
}
