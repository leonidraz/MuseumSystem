package com.example.museumcatalog;

import com.example.museumcatalog.Models.ReportRow;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileOutputStream;
import java.nio.file.Path;
import java.util.List;

public class ReportExcelService {

    public enum ReportType {
        MOVEMENT,
        INCOMING
    }

    public static void export(List<ReportRow> data, Path path, ReportType type) throws Exception {
        Workbook wb = new XSSFWorkbook();
        Sheet sheet = wb.createSheet("Report");

        createHeader(sheet, type);

        int rowIndex = 1;
        String lastKey = "";

        for (ReportRow r : data) {

            Row row = sheet.createRow(rowIndex++);

            String currentKey = nvl(r.getName());
            boolean isNewGroup = !currentKey.equals(lastKey);

            if (isNewGroup) {
                row.createCell(0).setCellValue(nvl(r.getKp())); // KP может быть null
                row.createCell(1).setCellValue(currentKey);
                row.createCell(2).setCellValue(nvl(r.getDescription()));
                lastKey = currentKey;
            } else {
                row.createCell(0).setCellValue("");
                row.createCell(1).setCellValue("");
                row.createCell(2).setCellValue("");
            }

            row.createCell(3).setCellValue(
                    r.getDate() == null ? "" : r.getDate().toLocalDate().toString()
            );

            if (type == ReportType.MOVEMENT) {
                row.createCell(4).setCellValue(nvl(r.getDocument()));
                row.createCell(5).setCellValue(nvl(r.getStage()));
            }

            if (type == ReportType.INCOMING) {
                row.createCell(4).setCellValue(nvl(r.getOwner()));
            }
        }

        autoSize(sheet, type);
        try (FileOutputStream out = new FileOutputStream(path.toFile())) {
            wb.write(out);
        }
        wb.close();
    }

    private static void createHeader(Sheet sheet, ReportType type) {
        Row row = sheet.createRow(0);

        row.createCell(0).setCellValue("КП");
        row.createCell(1).setCellValue("Предмет");
        row.createCell(2).setCellValue("Описание");
        row.createCell(3).setCellValue("Дата");

        if (type == ReportType.MOVEMENT) {
            row.createCell(4).setCellValue("Документ");
            row.createCell(5).setCellValue("Этап");
        }

        if (type == ReportType.INCOMING) {
            row.createCell(4).setCellValue("Владелец");
        }
    }

    private static void autoSize(Sheet sheet, ReportType type) {
        int maxCols = (type == ReportType.MOVEMENT) ? 6 : 5;
        for (int i = 0; i < maxCols; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    private static String nvl(String s) {
        return s == null ? "" : s;
    }
}