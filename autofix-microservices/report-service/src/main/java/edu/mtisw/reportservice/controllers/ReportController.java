package edu.mtisw.reportservice.controllers;
import edu.mtisw.reportservice.dtos.Report1Dto;
import edu.mtisw.reportservice.dtos.Report2Dto;
import edu.mtisw.reportservice.services.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
@RestController
@RequestMapping("/api/v1/reports")
public class ReportController {
    @Autowired private ReportService service;
    @GetMapping("/r1")
    public ResponseEntity<Report1Dto> report1(@RequestParam int month, @RequestParam int year) {
        return ResponseEntity.ok(service.report1(month, year));
    }
    @GetMapping("/r2")
    public ResponseEntity<Report2Dto> report2(@RequestParam int month, @RequestParam int year) {
        return ResponseEntity.ok(service.report2(month, year));
    }
}
