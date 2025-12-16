package com.example.ogani.service;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.util.List;

import com.example.ogani.entity.Order;
import com.example.ogani.model.report.DashboardReportRequest;

public interface ReportService {
    ByteArrayOutputStream generateDashboardReport(String title,
                                                  String exportedBy,
                                                  LocalDateTime exportedAt,
                                                  List<Order> orders);

    ByteArrayOutputStream generateDashboardReportFromStats(DashboardReportRequest payload);
}


