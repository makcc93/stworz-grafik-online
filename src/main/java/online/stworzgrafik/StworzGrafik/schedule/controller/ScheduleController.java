package online.stworzgrafik.StworzGrafik.schedule.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import online.stworzgrafik.StworzGrafik.schedule.DTO.CreateScheduleDTO;
import online.stworzgrafik.StworzGrafik.schedule.DTO.ResponseScheduleDTO;
import online.stworzgrafik.StworzGrafik.schedule.DTO.ScheduleSpecificationDTO;
import online.stworzgrafik.StworzGrafik.schedule.DTO.UpdateScheduleDTO;
import online.stworzgrafik.StworzGrafik.schedule.ScheduleService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
class ScheduleController {
    private final ScheduleService scheduleService;

    @PreAuthorize("@userAuthorizationService.hasAccessToStore(#storeId)")
    @GetMapping("/stores/{storeId}/schedules/{scheduleId}")
    ResponseEntity<ResponseScheduleDTO> getById(@PathVariable Long storeId,
                                                @PathVariable Long scheduleId){
        return ResponseEntity.ok(scheduleService.findById(storeId, scheduleId));
    }

    @PreAuthorize("@userAuthorizationService.hasAccessToStore(#storeId)")
    @GetMapping("/stores/{storeId}/schedules")
    ResponseEntity<Page<ResponseScheduleDTO>> getByCriteria(@PathVariable Long storeId,
                                                            ScheduleSpecificationDTO dto,
                                                            Pageable pageable){
        return ResponseEntity.ok(scheduleService.findByCriteria(storeId,dto,pageable));
    }

    @PreAuthorize("@userAuthorizationService.hasAccessToStore(#storeId)")
    @PostMapping("/stores/{storeId}/schedules")
    ResponseEntity<ResponseScheduleDTO> createSchedule(@PathVariable Long storeId,
                                                       @RequestBody @Valid CreateScheduleDTO dto){
        return ResponseEntity.status(HttpStatus.CREATED).body(scheduleService.createSchedule(storeId,dto));
    }

    @PreAuthorize("@userAuthorizationService.hasAccessToStore(#storeId)")
    @PatchMapping("/stores/{storeId}/schedules/{scheduleId}")
    ResponseEntity<ResponseScheduleDTO> updateSchedule(@PathVariable Long storeId,
                                                       @PathVariable Long scheduleId,
                                                       @RequestBody UpdateScheduleDTO dto){
        return ResponseEntity.ok(scheduleService.updateSchedule(storeId, scheduleId,dto));
    }

    @PreAuthorize("@userAuthorizationService.hasAccessToStore(#storeId)")
    @DeleteMapping("/stores/{storeId}/schedules/{scheduleId}")
    ResponseEntity<HttpStatus> deleteSchedule(@PathVariable Long storeId,
                                              @PathVariable Long scheduleId){
        scheduleService.deleteSchedule(storeId,scheduleId);

        return ResponseEntity.noContent().build();
    }
}
