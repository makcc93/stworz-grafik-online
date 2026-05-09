package online.stworzgrafik.StworzGrafik.billing;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import online.stworzgrafik.StworzGrafik.billing.DTO.BillingPeriodConfigRequest;
import online.stworzgrafik.StworzGrafik.billing.DTO.BillingPeriodConfigResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/billing-period-config")
@RequiredArgsConstructor
public class BillingPeriodConfigController {

    private final BillingPeriodConfigService service;

    @GetMapping
    public ResponseEntity<List<BillingPeriodConfigResponse>> getAll() {
        return ResponseEntity.ok(service.getAll());
    }

    @PutMapping("/{id}")
    public ResponseEntity<BillingPeriodConfigResponse> update(
            @PathVariable Long id,
            @RequestBody @Valid BillingPeriodConfigRequest request
    ) {
        return ResponseEntity.ok(service.update(id, request));
    }
}
