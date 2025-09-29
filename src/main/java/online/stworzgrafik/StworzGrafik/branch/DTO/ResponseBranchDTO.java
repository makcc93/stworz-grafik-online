package online.stworzgrafik.StworzGrafik.branch.DTO;

public record ResponseBranchDTO(
        Long id,
        String name,
        boolean isEnable,
        Long regionId,
        String regionName
) {}
