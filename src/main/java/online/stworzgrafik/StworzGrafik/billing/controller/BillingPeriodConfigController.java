package online.stworzgrafik.StworzGrafik.billing.controller;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import online.stworzgrafik.StworzGrafik.billing.BillingPeriodConfigService;
import online.stworzgrafik.StworzGrafik.billing.DTO.BillingPeriodConfigRequest;
import online.stworzgrafik.StworzGrafik.billing.DTO.BillingPeriodConfigResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Transactional
@RequestMapping("/api")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('ADMIN')")
public class BillingPeriodConfigController {
    private final BillingPeriodConfigService billingPeriodConfigService;

    @GetMapping("/billing-period")
    ResponseEntity<List<BillingPeriodConfigResponse>> getAll(){
        return ResponseEntity.ok(billingPeriodConfigService.getAll());
    }

    @GetMapping("/billing-period/{month}")
    ResponseEntity<Integer> getPeriodStartMonth(@PathVariable Integer month){
        return ResponseEntity.ok(billingPeriodConfigService.getPeriodStartMonth(month));
    }

    @GetMapping("/billing-period/{year}/{month}")
    ResponseEntity<List<Integer>> getPeriodStartMonth(@PathVariable Integer year,
                                                      @PathVariable Integer month){
        return ResponseEntity.ok(billingPeriodConfigService.getPeriodMonths(year,month));
    }

    @PostMapping("/billing-period")
    ResponseEntity<BillingPeriodConfigResponse> create(@RequestBody BillingPeriodConfigRequest dto){
        return ResponseEntity.ok(billingPeriodConfigService.create(dto));
    }

    @PatchMapping("/billing-period/{billingPeriodConfigId}")
    ResponseEntity<BillingPeriodConfigResponse> update(@PathVariable Long billingPeriodConfigId,
                                                       @RequestBody BillingPeriodConfigRequest dto){
        return ResponseEntity.ok(billingPeriodConfigService.update(billingPeriodConfigId,dto));
    }

    @DeleteMapping("/billing-period/{billingPeriodConfigId}")
    ResponseEntity<HttpStatus> delete(@PathVariable Long billingPeriodConfigId){
        billingPeriodConfigService.delete(billingPeriodConfigId);

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
