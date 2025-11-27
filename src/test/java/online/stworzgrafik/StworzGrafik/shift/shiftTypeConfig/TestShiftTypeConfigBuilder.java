package online.stworzgrafik.StworzGrafik.shift.shiftTypeConfig;

import java.math.BigDecimal;

public class TestShiftTypeConfigBuilder {
    private ShiftCode code = ShiftCode.WORK;
    private String namePl = "PRACA";
    private BigDecimal defaultHours = BigDecimal.valueOf(8L);
    private Boolean countsAsWork = true;

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
        return new ShiftTypeConfigBuilder().createShiftTypeConfig(
                code,
                namePl,
                defaultHours,
                countsAsWork
        );
    }
}
