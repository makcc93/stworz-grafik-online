package online.stworzgrafik.StworzGrafik.dataBuilderForTests.shift.shiftTypeConfig;

import online.stworzgrafik.StworzGrafik.shift.shiftTypeConfig.ShiftCode;
import online.stworzgrafik.StworzGrafik.shift.shiftTypeConfig.ShiftTypeConfig;

import java.math.BigDecimal;

public class TestShiftTypeConfigBuilder {
    private Long id = 1L;
    private ShiftCode code = ShiftCode.WORK;
    private String namePl = "PRACA";
    private BigDecimal defaultHours = BigDecimal.valueOf(8L);
    private Boolean countsAsWork = true;

    public TestShiftTypeConfigBuilder withId(Long id){
        this.id = id;
        return this;
    }

    public TestShiftTypeConfigBuilder withCode(ShiftCode code){
        this.code = code;
        return this;
    }

    public TestShiftTypeConfigBuilder withNamePl(String namePl){
        this.namePl = namePl;
        return this;
    }

    public TestShiftTypeConfigBuilder withDefaultHours(BigDecimal defaultHours){
        this.defaultHours = defaultHours;
        return this;
    }

    public TestShiftTypeConfigBuilder withCountsAsWork(Boolean countsAsWork){
        this.countsAsWork = countsAsWork;
        return this;
    }

    public ShiftTypeConfig build(){
        return new ShiftTypeConfig(
                id,
                code,
                namePl,
                defaultHours,
                countsAsWork
        );
    }

}
