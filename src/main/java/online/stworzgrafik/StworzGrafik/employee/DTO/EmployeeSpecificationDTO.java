package online.stworzgrafik.StworzGrafik.employee.DTO;

public record EmployeeSpecificationDTO(
        Long storeId,
        Long id,
        String firstName,
        String lastName,
        Long sap,
        Long positionId,
        Boolean enable,
        Boolean canOperateCheckout,
        Boolean canOperateCredit,
        Boolean canOpenCloseStore,
        Boolean seller,
        Boolean manager
) {
}
