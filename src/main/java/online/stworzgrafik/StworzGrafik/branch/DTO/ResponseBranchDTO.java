package online.stworzgrafik.StworzGrafik.branch.DTO;

public record ResponseBranchDTO(
        Long id,
        String name,
        boolean enable,
        Long regionId,
        String regionName
) {}
