package edu.mtisw.autofixbackend.controllers;

import edu.mtisw.autofixbackend.dtos.MatrixReport;
import edu.mtisw.autofixbackend.dtos.ReportR1;
import edu.mtisw.autofixbackend.dtos.ReportR3Row;
import edu.mtisw.autofixbackend.services.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

// HU4 (R1): USER y ADMIN.   HU5..HU7 (R2, R3, R4): solo ADMIN
@RestController
@RequestMapping("/api/v1/reports")
public class ReportController {
    @Autowired
    ReportService reportService;

    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @GetMapping("/r1")
    public ResponseEntity<ReportR1> reportR1(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
                                             @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(reportService.getReportR1(from, to));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/r2")
    public ResponseEntity<MatrixReport> reportR2(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
                                                 @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(reportService.getReportR2(from, to));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/r3")
    public ResponseEntity<List<ReportR3Row>> reportR3(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
                                                      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(reportService.getReportR3(from, to));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/r4")
    public ResponseEntity<MatrixReport> reportR4(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
                                                 @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(reportService.getReportR4(from, to));
    }
}
