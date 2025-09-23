package online.stworzgrafik.StworzGrafik.dataFactory;

import online.stworzgrafik.StworzGrafik.branch.DTO.UpdateBranchDTO;

public class TestDataFactory {
    public static UpdateBranchDTO defaultUpdateBranchDTO(){
        return new UpdateBranchDTO("TESTNAME",true);
    }
}
