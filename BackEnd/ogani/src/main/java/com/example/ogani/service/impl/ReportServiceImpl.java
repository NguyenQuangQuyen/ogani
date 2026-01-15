package com.example.ogani.service.impl;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.util.*;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import com.example.ogani.entity.Order;
import com.example.ogani.model.report.DashboardReportRequest;
import com.example.ogani.service.ReportService;

@Service
public class ReportServiceImpl implements ReportService {

    @Override
    public ByteArrayOutputStream generateDashboardReport(
            String title,
            String exportedBy,
            LocalDateTime exportedAt,
            List<Order> orders) {

        try (XSSFWorkbook wb = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            /* ================== STYLES ================== */
            Font titleFont = wb.createFont();
            titleFont.setBold(true);
            titleFont.setFontHeightInPoints((short) 16);

            Font headerFont = wb.createFont();
            headerFont.setBold(true);

            CellStyle titleStyle = wb.createCellStyle();
            titleStyle.setFont(titleFont);
            titleStyle.setAlignment(HorizontalAlignment.CENTER);
            titleStyle.setVerticalAlignment(VerticalAlignment.CENTER);

            CellStyle headerStyle = wb.createCellStyle();
            headerStyle.setFont(headerFont);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);
            headerStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            headerStyle.setFillForegroundColor(IndexedColors.LIGHT_CORNFLOWER_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            setBorders(headerStyle);

            CellStyle labelStyle = wb.createCellStyle();
            labelStyle.setFont(headerFont);
            labelStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            labelStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            setBorders(labelStyle);

            CellStyle valueStyle = wb.createCellStyle();
            setBorders(valueStyle);

            DataFormat df = wb.createDataFormat();
            CellStyle currencyStyle = wb.createCellStyle();
            currencyStyle.setDataFormat(df.getFormat("#,##0\" VND\""));
            currencyStyle.setAlignment(HorizontalAlignment.RIGHT);
            setBorders(currencyStyle);

            CellStyle altRowStyle = wb.createCellStyle();
            altRowStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            altRowStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            setBorders(altRowStyle);

            /* ================== SHEET ================== */
            Sheet sheet = wb.createSheet("DASHBOARD");
            int r = 0;

            /* ================== TITLE ================== */
            Row titleRow = sheet.createRow(r++);
            createCell(titleRow, 0, safe(title), titleStyle);
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 5));
            titleRow.setHeightInPoints(28);

            /* ================== META ================== */
            r = writeKeyValue(sheet, r, "Ngày giờ xuất báo cáo",
                    exportedAt != null ? exportedAt.toString() : "", labelStyle, valueStyle);
            r = writeKeyValue(sheet, r, "Người xuất báo cáo",
                    exportedBy, labelStyle, valueStyle);
            r++;

            /* ================== STATISTICS ================== */
            long totalRevenue = 0;
            int totalOrders = 0;
            int paid = 0;
            int unpaid = 0;
            int totalProducts = 0;

            Map<String, Integer> productCount = new HashMap<>();
            Map<String, Long> productRevenue = new HashMap<>();

            if (orders != null && !orders.isEmpty()) {
                totalOrders = orders.size();

                for (Order o : orders) {
                    totalRevenue += o.getPrice() != null ? o.getPrice() : 0;
                    totalProducts += o.getQuantity() != null ? o.getQuantity() : 0;

                    if ("PAID".equalsIgnoreCase(o.getStatus())) {
                        paid++;
                    }

                    String productName = o.getProductName() == null ? "Unknown" : o.getProductName();
                    int qty = o.getQuantity() != null ? o.getQuantity() : 0;
                    long price = o.getPrice() != null ? o.getPrice() : 0;

                    productCount.put(productName, productCount.getOrDefault(productName, 0) + qty);
                    productRevenue.put(productName, productRevenue.getOrDefault(productName, 0L) + qty * price);
                }
                unpaid = totalOrders - paid;
            }

            String bestProduct = "";
            int bestQty = 0;
            for (Map.Entry<String, Integer> e : productCount.entrySet()) {
                if (e.getValue() > bestQty) {
                    bestQty = e.getValue();
                    bestProduct = e.getKey();
                }
            }

            /* ================== OVERVIEW ================== */
            Row revRow = sheet.createRow(r++);
            createCell(revRow, 0, "Tổng doanh thu", labelStyle);
            createNumberCell(revRow, 1, totalRevenue, currencyStyle);

            r = writeKeyValue(sheet, r, "Tổng số đơn hàng",
                    String.valueOf(totalOrders), labelStyle, valueStyle);

            r = writeKeyValue(sheet, r, "Tỷ lệ đơn đã thanh toán (%)",
                    totalOrders == 0 ? "0" :
                            String.valueOf(Math.round(paid * 100.0 / totalOrders)),
                    labelStyle, valueStyle);

            r = writeKeyValue(sheet, r, "Tỷ lệ đơn chưa thanh toán (%)",
                    totalOrders == 0 ? "0" :
                            String.valueOf(Math.round(unpaid * 100.0 / totalOrders)),
                    labelStyle, valueStyle);

            r = writeKeyValue(sheet, r, "Sản phẩm bán chạy nhất",
                    bestProduct, labelStyle, valueStyle);

            r = writeKeyValue(sheet, r,
                    "Số lượng bán của sản phẩm bán chạy nhất",
                    String.valueOf(bestQty), labelStyle, valueStyle);

            r++;

            /* ================== TOP PRODUCTS ================== */
            Row section = sheet.createRow(r++);
            createCell(section, 0, "TOP SẢN PHẨM BÁN CHẠY", titleStyle);
            sheet.addMergedRegion(new CellRangeAddress(section.getRowNum(),
                    section.getRowNum(), 0, 5));

            Row header = sheet.createRow(r++);
            String[] cols = {"STT", "Tên sản phẩm", "Số lượng bán", "Doanh thu"};
            for (int i = 0; i < cols.length; i++) {
                createCell(header, i, cols[i], headerStyle);
            }

            List<Map.Entry<String, Integer>> sorted =
                    new ArrayList<>(productCount.entrySet());
            sorted.sort((e1, e2) -> e2.getValue().compareTo(e1.getValue()));

            int idx = 1;
            int rowIdx = 0;
            for (Map.Entry<String, Integer> e : sorted) {
                Row row = sheet.createRow(r++);
                CellStyle rowStyle = (rowIdx++ % 2 == 0) ? valueStyle : altRowStyle;

                createCell(row, 0, String.valueOf(idx++), rowStyle);
                createCell(row, 1, e.getKey(), rowStyle);
                createCell(row, 2, String.valueOf(e.getValue()), rowStyle);
                createNumberCell(row, 3, productRevenue.getOrDefault(e.getKey(), 0L), currencyStyle);
            }

            for (int c = 0; c < 8; c++) {
                sheet.autoSizeColumn(c);
            }

            wb.write(out);
            return out;

        } catch (Exception e) {
            throw new RuntimeException("Failed to generate dashboard report", e);
        }
    }

    @Override
    public ByteArrayOutputStream generateDashboardReportFromStats(DashboardReportRequest payload) {
        try (XSSFWorkbook wb = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            /* ================== STYLES ================== */
            Font titleFont = wb.createFont();
            titleFont.setBold(true);
            titleFont.setFontHeightInPoints((short) 16);

            Font headerFont = wb.createFont();
            headerFont.setBold(true);

            CellStyle titleStyle = wb.createCellStyle();
            titleStyle.setFont(titleFont);
            titleStyle.setAlignment(HorizontalAlignment.CENTER);
            titleStyle.setVerticalAlignment(VerticalAlignment.CENTER);

            CellStyle headerStyle = wb.createCellStyle();
            headerStyle.setFont(headerFont);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);
            headerStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            headerStyle.setFillForegroundColor(IndexedColors.LIGHT_CORNFLOWER_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            setBorders(headerStyle);

            CellStyle labelStyle = wb.createCellStyle();
            labelStyle.setFont(headerFont);
            labelStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            labelStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            setBorders(labelStyle);

            CellStyle valueStyle = wb.createCellStyle();
            setBorders(valueStyle);

            DataFormat df = wb.createDataFormat();
            CellStyle currencyStyle = wb.createCellStyle();
            currencyStyle.setDataFormat(df.getFormat("#,##0\" VND\""));
            currencyStyle.setAlignment(HorizontalAlignment.RIGHT);
            setBorders(currencyStyle);

            CellStyle altRowStyle = wb.createCellStyle();
            altRowStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            altRowStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            setBorders(altRowStyle);

            /* ================== SHEET ================== */
            Sheet sheet = wb.createSheet("DASHBOARD");
            int r = 0;

            /* ================== TITLE ================== */
            Row titleRow = sheet.createRow(r++);
            createCell(titleRow, 0, safe(payload.title), titleStyle);
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 5));
            titleRow.setHeightInPoints(28);

            /* ================== META ================== */
            r = writeKeyValue(sheet, r, "Ngày giờ xuất báo cáo",
                    LocalDateTime.now().toString(), labelStyle, valueStyle);
            r = writeKeyValue(sheet, r, "Người xuất báo cáo",
                    safe(payload.exportedBy), labelStyle, valueStyle);
            r++;

            /* ================== STATISTICS FROM PAYLOAD ================== */
            var stats = payload.statistics;
            if (stats != null) {
                // Tổng doanh thu
                Row revRow = sheet.createRow(r++);
                createCell(revRow, 0, "Tổng doanh thu", labelStyle);
                createNumberCell(revRow, 1, stats.totalRevenue, currencyStyle);

                // Tổng số đơn hàng
                r = writeKeyValue(sheet, r, "Tổng số đơn hàng",
                        stats.orderStatusCounts != null ? String.valueOf(stats.orderStatusCounts.total) : "0",
                        labelStyle, valueStyle);

                // Tỷ lệ đơn đã thanh toán
                int paidPercent = 0;
                int unpaidPercent = 0;
                if (stats.orderStatusCounts != null && stats.orderStatusCounts.total > 0) {
                    paidPercent = Math.round(stats.orderStatusCounts.paid * 100.0f / stats.orderStatusCounts.total);
                    unpaidPercent = Math.round(stats.orderStatusCounts.unpaid * 100.0f / stats.orderStatusCounts.total);
                }
                
               

                // Sản phẩm bán chạy nhất
                String bestProduct = "";
                int bestQty = 0;
                if (stats.bestSellingProduct != null) {
                    bestProduct = safe(stats.bestSellingProduct.name);
                    bestQty = stats.bestSellingProduct.quantity;
                }
                
                r = writeKeyValue(sheet, r, "Sản phẩm bán chạy nhất",
                        bestProduct, labelStyle, valueStyle);
                r = writeKeyValue(sheet, r, "Số lượng bán của sản phẩm bán chạy nhất",
                        String.valueOf(bestQty), labelStyle, valueStyle);

                r++;

                /* ================== TOP PRODUCTS ================== */
                if (stats.productSalesDistribution != null && !stats.productSalesDistribution.isEmpty()) {
                    Row section = sheet.createRow(r++);
                    createCell(section, 0, "TOP SẢN PHẨM BÁN CHẠY", titleStyle);
                    sheet.addMergedRegion(new CellRangeAddress(section.getRowNum(),
                            section.getRowNum(), 0, 3));

                    Row header = sheet.createRow(r++);
                    String[] cols = {"STT", "Tên sản phẩm", "Số lượng bán", "Tỷ lệ (%)"};
                    for (int i = 0; i < cols.length; i++) {
                        createCell(header, i, cols[i], headerStyle);
                    }

                    int idx = 1;
                    int rowIdx = 0;
                    for (var product : stats.productSalesDistribution) {
                        Row row = sheet.createRow(r++);
                        CellStyle rowStyle = (rowIdx++ % 2 == 0) ? valueStyle : altRowStyle;

                        createCell(row, 0, String.valueOf(idx++), rowStyle);
                        createCell(row, 1, safe(product.name), rowStyle);
                        createCell(row, 2, String.valueOf(product.quantity), rowStyle);
                        createCell(row, 3, product.percentage + "%", rowStyle);
                    }
                }
            }

            for (int c = 0; c < 8; c++) {
                sheet.autoSizeColumn(c);
            }

            wb.write(out);
            return out;

        } catch (Exception e) {
            throw new RuntimeException("Failed to generate dashboard report from stats", e);
        }
    }

    /* ================== HELPERS ================== */

    private void createCell(Row row, int col, String value, CellStyle style) {
        Cell c = row.createCell(col);
        c.setCellValue(value == null ? "" : value);
        if (style != null) c.setCellStyle(style);
    }

    private void createNumberCell(Row row, int col, long value, CellStyle style) {
        Cell c = row.createCell(col);
        c.setCellValue(value);
        if (style != null) c.setCellStyle(style);
    }

    private int writeKeyValue(
            Sheet sheet,
            int rowIdx,
            String key,
            String value,
            CellStyle keyStyle,
            CellStyle valueStyle) {

        Row row = sheet.createRow(rowIdx);
        createCell(row, 0, key, keyStyle);
        createCell(row, 1, value, valueStyle);
        return rowIdx + 1;
    }

    private void setBorders(CellStyle style) {
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
    }

    private String safe(String s) {
        return s == null ? "" : s;
    }
}
