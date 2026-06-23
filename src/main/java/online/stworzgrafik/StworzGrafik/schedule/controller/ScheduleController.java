package online.stworzgrafik.StworzGrafik.schedule.controller;

import jakarta.validation.Valid;
import jdk.jfr.ContentType;
import lombok.RequiredArgsConstructor;
import online.stworzgrafik.StworzGrafik.fileExport.ExcelExport;
import online.stworzgrafik.StworzGrafik.fileExport.PdfExport;
import online.stworzgrafik.StworzGrafik.fileExport.r2.DTO.ExportUrlDTO;
import online.stworzgrafik.StworzGrafik.fileExport.r2.R2StorageService;
import online.stworzgrafik.StworzGrafik.schedule.DTO.CreateScheduleDTO;
import online.stworzgrafik.StworzGrafik.schedule.DTO.ResponseScheduleDTO;
import online.stworzgrafik.StworzGrafik.schedule.DTO.ScheduleSpecificationDTO;
import online.stworzgrafik.StworzGrafik.schedule.DTO.UpdateScheduleDTO;
import online.stworzgrafik.StworzGrafik.fileExport.ExcelExportFromDatabase;
import online.stworzgrafik.StworzGrafik.schedule.ScheduleService;
import online.stworzgrafik.StworzGrafik.schedule.generator.ScheduleGeneratorService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Random;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
class ScheduleController {
    private final ScheduleService scheduleService;
    private final ScheduleGeneratorService scheduleGeneratorService;
    private final ExcelExportFromDatabase excelExportFromDatabase;
    private final ExcelExport excelExport;
    private final PdfExport pdfExport;
    private final R2StorageService r2StorageService;

    private static final String XLSX_CONTENT_TYPE =
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
    private static final String PDF_CONTENT_TYPE = "application/pdf";


    @PreAuthorize("@userAuthorizationService.hasAccessToStore(#storeId)")
    @GetMapping("/stores/{storeId}/schedules/{scheduleId}")
    ResponseEntity<ResponseScheduleDTO> getById(@PathVariable Long storeId,
                                                @PathVariable Long scheduleId) {
        return ResponseEntity.ok(scheduleService.findById(storeId, scheduleId));
    }

    @PreAuthorize("@userAuthorizationService.hasAccessToStore(#storeId)")
    @GetMapping("/stores/{storeId}/schedules")
    ResponseEntity<Page<ResponseScheduleDTO>> getByCriteria(@PathVariable Long storeId,
                                                            ScheduleSpecificationDTO dto,
                                                            Pageable pageable) {
        return ResponseEntity.ok(scheduleService.findByCriteria(storeId, dto, pageable));
    }

    @PreAuthorize("@userAuthorizationService.hasAccessToStore(#storeId)")
    @PostMapping("/stores/{storeId}/schedules")
    ResponseEntity<ResponseScheduleDTO> createSchedule(@PathVariable Long storeId,
                                                       @RequestBody @Valid CreateScheduleDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(scheduleService.createSchedule(storeId, dto));
    }

    @PreAuthorize("@userAuthorizationService.hasAccessToStore(#storeId)")
    @PatchMapping("/stores/{storeId}/schedules/{scheduleId}")
    ResponseEntity<ResponseScheduleDTO> updateSchedule(@PathVariable Long storeId,
                                                       @PathVariable Long scheduleId,
                                                       @RequestBody UpdateScheduleDTO dto) {
        return ResponseEntity.ok(scheduleService.updateSchedule(storeId, scheduleId, dto));
    }

    @PreAuthorize("@userAuthorizationService.hasAccessToStore(#storeId)")
    @DeleteMapping("/stores/{storeId}/schedules/{scheduleId}")
    ResponseEntity<HttpStatus> deleteSchedule(@PathVariable Long storeId,
                                              @PathVariable Long scheduleId) {

        String folderPrefix = "exports/" + storeId + "/" + scheduleId + "/";
        r2StorageService.deleteFolderPrefix(folderPrefix);

        scheduleService.deleteSchedule(storeId, scheduleId);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("@userAuthorizationService.hasAccessToStore(#storeId)")
    @PostMapping("/stores/{storeId}/schedules/{scheduleId}/generate")
    ResponseEntity<Void> generateSchedule(@PathVariable Long storeId, @PathVariable Long scheduleId) {
        try {
            ResponseScheduleDTO schedule = scheduleService.findById(storeId, scheduleId);

            byte[] excelBytes = scheduleGeneratorService.generateSchedule(storeId, scheduleId);
            String excelFile = excelFilename(schedule);
            String excelKey = exportKey(storeId, scheduleId, excelFile);
            r2StorageService.uploadAndPresign(excelBytes, excelKey, XLSX_CONTENT_TYPE);

            byte[] pdfBytes = pdfExport.export(storeId, scheduleId);
            String pdfFile = pdfFilename(schedule);
            String pdfKey = exportKey(storeId, scheduleId, pdfFile);
            r2StorageService.uploadAndPresign(pdfBytes, pdfKey, PDF_CONTENT_TYPE);

            return ResponseEntity.ok().build();
        } catch (Exception e) {
            throw new RuntimeException("Błąd podczas generowania i zapisu plików grafiku: " + e.getMessage(), e);
        }
    }

    @PreAuthorize("@userAuthorizationService.hasAccessToStore(#storeId)")
    @GetMapping("/stores/{storeId}/schedules/{scheduleId}/export")
    ResponseEntity<ExportUrlDTO> exportSchedule(@PathVariable Long storeId, @PathVariable Long scheduleId) {
        ResponseScheduleDTO schedule = scheduleService.findById(storeId, scheduleId);
        String filename = excelFilename(schedule);
        String key = exportKey(storeId, scheduleId, filename);

        String url = r2StorageService.getPresignedUrl(key);
        return ResponseEntity.ok(new ExportUrlDTO(url, filename));
    }

    @PreAuthorize("@userAuthorizationService.hasAccessToStore(#storeId)")
    @GetMapping("/stores/{storeId}/schedules/{scheduleId}/exportPdf")
    ResponseEntity<ExportUrlDTO> exportPdfSchedule(@PathVariable Long storeId, @PathVariable Long scheduleId) {
        ResponseScheduleDTO schedule = scheduleService.findById(storeId, scheduleId);
        String filename = pdfFilename(schedule);
        String key = exportKey(storeId, scheduleId, filename);

        String url = r2StorageService.getPresignedUrl(key);
        return ResponseEntity.ok(new ExportUrlDTO(url, filename));
    }

    private static String excelFilename(ResponseScheduleDTO schedule) {
        return filenameBase(schedule) + ".xlsx";
    }

    private String pdfFilename(ResponseScheduleDTO schedule) {
        return filenameBase(schedule) + ".pdf";
    }

    private static String filenameBase(ResponseScheduleDTO schedule){
        return "Grafik_" +
                schedule.month() +
                "_" +
                schedule.year() +
                "_" +
                schedule.storeId() +
                "_" +
                schedule.id() +
                "_" +
                LocalDate.now() +
                "_" +
                schedule.scheduleStatusName();
    }

    private String exportKey(Long storeId, Long scheduleId, String filename) {
        return "exports/" + storeId + "/" + scheduleId + "/" + filename;
    }
}
