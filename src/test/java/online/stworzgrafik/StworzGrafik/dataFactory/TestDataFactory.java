package online.stworzgrafik.StworzGrafik.dataFactory;

import online.stworzgrafik.StworzGrafik.branch.DTO.UpdateBranchDTO;
import online.stworzgrafik.StworzGrafik.shift.DTO.ResponseShiftDTO;
import online.stworzgrafik.StworzGrafik.store.DTO.UpdateStoreDTO;
import online.stworzgrafik.StworzGrafik.store.RegionType;
import org.springframework.cglib.core.Local;

import java.time.LocalTime;

public class TestDataFactory {
    public static UpdateBranchDTO defaultUpdateBranchDTO(){
        return new UpdateBranchDTO(
                "TESTNAME",
                true
        );
    }

    public static UpdateStoreDTO defaultUpdateStoreDTO(){
        return new UpdateStoreDTO(
                "RANDOMNAME",
                "AB",
                "RANDOMLOCATION",
                1L, RegionType.ZACHOD,
                true,
                null,
                LocalTime.of(9,0),
                LocalTime.of(20,0)
        );
    }

    public static ResponseShiftDTO defaultResponseShiftDTO(){
        return new ResponseShiftDTO(
                1L,
                LocalTime.of(10,0),
                LocalTime.of(21,0),
                11
        );
    }
}
