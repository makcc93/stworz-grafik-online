package online.stworzgrafik.StworzGrafik.billing;

import online.stworzgrafik.StworzGrafik.billing.DTO.BillingPeriodConfigResponse;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
interface BillingPeriodConfigMapper {

    BillingPeriodConfigResponse toResponse(BillingPeriodConfig config);

    List<BillingPeriodConfigResponse> toResponseList(List<BillingPeriodConfig> configs);
}