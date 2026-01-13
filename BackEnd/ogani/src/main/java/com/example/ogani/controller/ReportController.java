package com.example.ogani.controller;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.ogani.entity.Order;
import com.example.ogani.service.OrderService;
import com.example.ogani.service.ReportService;
import com.example.ogani.model.report.DashboardReportRequest;

@RestController
@RequestMapping("/api/report")
@CrossOrigin(origins = {"http://localhost:5361"}, maxAge = 3600)
public class ReportController {


    private OrderService orderService;

    @Autowired
    private ReportService reportService;

    @GetMapping("/dashboard")
    public ResponseEntity<byte[]> exportDashboard(@RequestParam(name = "title", required = false, defaultValue = "Báo cáo tổng quan hệ thống") String title,
                                                  @RequestParam(name = "exportedBy", required = false, defaultValue = "admin") String exportedBy) {
        List<Order> orders = orderService.getList();
        ByteArrayOutputStream out = reportService.generateDashboardReport(title, exportedBy, LocalDateTime.now(), orders);
        byte[] bytes = out.toByteArray();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
        headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=bao_cao_dashboard.xlsx");
        headers.setContentLength(bytes.length);
        return ResponseEntity.ok().headers(headers).body(bytes);
    }

    @PostMapping("/dashboard-sync")
    public ResponseEntity<byte[]> exportDashboardSync(@RequestBody DashboardReportRequest payload) {
        ByteArrayOutputStream out = reportService.generateDashboardReportFromStats(payload);
        byte[] bytes = out.toByteArray();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
        headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=bao_cao_dashboard.xlsx");
        headers.setContentLength(bytes.length);
        return ResponseEntity.ok().headers(headers).body(bytes);
    }
}


