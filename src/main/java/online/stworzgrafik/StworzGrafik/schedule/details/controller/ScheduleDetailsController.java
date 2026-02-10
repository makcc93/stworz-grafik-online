package online.stworzgrafik.StworzGrafik.schedule.details.controller;

import lombok.RequiredArgsConstructor;
import online.stworzgrafik.StworzGrafik.schedule.details.DTO.CreateScheduleDetailsDTO;
import online.stworzgrafik.StworzGrafik.schedule.details.DTO.ResponseScheduleDetailsDTO;
import online.stworzgrafik.StworzGrafik.schedule.details.DTO.ScheduleDetailsSpecificationDTO;
import online.stworzgrafik.StworzGrafik.schedule.details.DTO.UpdateScheduleDetailsDTO;
import online.stworzgrafik.StworzGrafik.schedule.details.ScheduleDetails;
import online.stworzgrafik.StworzGrafik.schedule.details.ScheduleDetailsService;
import online.stworzgrafik.StworzGrafik.security.UserAuthorizationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
class ScheduleDetailsController {
    private final ScheduleDetailsService service;
    private final UserAuthorizationService userAuthorizationService;

    @PreAuthorize("@userAuthorizationService.hasAccessToStore(#storeId)")
    @GetMapping("/stores/{storeId}/schedules/{scheduleId}/details/{detailsId}")
    ResponseEntity<ResponseScheduleDetailsDTO> getById(@PathVariable Long storeId,
                                                       @PathVariable Long scheduleId,
                                                       @PathVariable Long detailsId){
        return ResponseEntity.ok(service.findById(storeId,scheduleId,detailsId));
    }

    @PreAuthorize("@userAuthorizationService.hasAccessToStore(#storeId)")
    @GetMapping("/stores/{storeId}/schedules/{scheduleId}/details")
    ResponseEntity<Page<ResponseScheduleDetailsDTO>> getByCriteria(@PathVariable Long storeId,
                                                                   @PathVariable Long scheduleId,
                                                                   ScheduleDetailsSpecificationDTO dto,
                                                                   Pageable pageable){
        return ResponseEntity.ok(service.findByCriteria(storeId,scheduleId,dto,pageable));
    }

    @PreAuthorize("@userAuthorizationService.hasAccessToStore(#storeId)")
    @PostMapping("/stores/{storeId}/schedules/{scheduleId}/details")
    ResponseEntity<ResponseScheduleDetailsDTO> createScheduleDetails(@PathVariable Long storeId,
                                                                     @PathVariable Long scheduleId,
                                                                     CreateScheduleDetailsDTO dto){
        return ResponseEntity.ok(service.createScheduleDetails(storeId,scheduleId,dto));
    }

    @PreAuthorize("@userAuthorizationService.hasAccessToStore(#storeId)")
    @PatchMapping("/stores/{storeId}/schedules/{scheduleId}/details/{detailsId}")
    ResponseEntity<ResponseScheduleDetailsDTO> updateScheduleDetails(@PathVariable Long storeId,
                                                                     @PathVariable Long scheduleId,
                                                                     @PathVariable Long detailsId,
                                                                     UpdateScheduleDetailsDTO dto){
        return ResponseEntity.ok(service.updateScheduleDetails(storeId,scheduleId,detailsId,dto));
    }

    @PreAuthorize("@userAuthorizationService.hasAccessToStore(#storeId)")
    @DeleteMapping("/stores/{storeId}/schedules/{scheduleId}/details/{detailsId}")
    ResponseEntity<HttpStatus> deleteScheduleDetails(@PathVariable Long storeId,
                                                     @PathVariable Long scheduleId,
                                                     @PathVariable Long detailsId){
        service.deleteScheduleDetails(storeId,scheduleId,detailsId);

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
