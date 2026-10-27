package com.smart_extract_service.modules.bulk.application.support;

import com.smart_extract_service.modules.bulk.application.spi.BulkCell;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;

import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;


public final class ExcelStreamRenderer implements AutoCloseable {

    private final SXSSFWorkbook workbook;
    private final Sheet dataSheet;
    private final Sheet metaSheet;
    private int dataRowIdx = 0;
    private int metaRowIdx = 0;

    private final CellStyle headerStyle;
    private final CellStyle textStyle;
    private final CellStyle numberStyle;
    private final CellStyle percentStyle;
    private final CellStyle dateStyle;
    private final CellStyle dateTimeStyle;
    private final CellStyle metaStyle;

    private final OutputStream out;

    public ExcelStreamRenderer(OutputStream out) {
        this.out = out;

        this.workbook = new SXSSFWorkbook(500);
        this.dataSheet = workbook.createSheet("Dados");
        this.metaSheet = workbook.createSheet("Metadados");

        this.headerStyle = workbook.createCellStyle();
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerFont.setColor(IndexedColors.WHITE.getIndex());
        headerStyle.setFont(headerFont);
        headerStyle.setFillForegroundColor(IndexedColors.CORNFLOWER_BLUE.getIndex());
        headerStyle.setFillPattern(org.apache.poi.ss.usermodel.FillPatternType.SOLID_FOREGROUND);
        headerStyle.setAlignment(org.apache.poi.ss.usermodel.HorizontalAlignment.CENTER);

        this.textStyle = workbook.createCellStyle();

        this.numberStyle = workbook.createCellStyle();
        CreationHelper createHelper = workbook.getCreationHelper();
        DataFormat dataFormat = createHelper.createDataFormat();
        numberStyle.setDataFormat(dataFormat.getFormat("#,##0.00"));

        this.percentStyle = workbook.createCellStyle();
        percentStyle.setDataFormat(dataFormat.getFormat("0.00%"));

        this.dateStyle = workbook.createCellStyle();
        dateStyle.setDataFormat(dataFormat.getFormat("dd/mm/yyyy"));

        this.dateTimeStyle = workbook.createCellStyle();
        dateTimeStyle.setDataFormat(dataFormat.getFormat("dd/mm/yyyy hh:mm"));

        this.metaStyle = workbook.createCellStyle();
        Font metaFont = workbook.createFont();
        metaFont.setItalic(true);
        metaFont.setColor(IndexedColors.GREY_50_PERCENT.getIndex());
        metaStyle.setFont(metaFont);
    }

    public void comment(String text) {
        Row row = metaSheet.createRow(metaRowIdx++);
        Cell cell = row.createCell(0);
        cell.setCellValue(text);
        cell.setCellStyle(metaStyle);
    }

    public void headers(List<String> headers) {
        Row row = dataSheet.createRow(dataRowIdx++);
        for (int i = 0; i < headers.size(); i++) {
            Cell cell = row.createCell(i);
            cell.setCellValue(headers.get(i));
            cell.setCellStyle(headerStyle);
        }
        dataSheet.setAutoFilter(
                new org.apache.poi.ss.util.CellRangeAddress(0, 0, 0, headers.size() - 1));
        dataSheet.createFreezePane(0, 1);
    }

    public void row(List<BulkCell> cells) {
        Row row = dataSheet.createRow(dataRowIdx++);
        for (int i = 0; i < cells.size(); i++) {
            BulkCell cell = cells.get(i);
            Cell poiCell = row.createCell(i);
            applyCell(poiCell, cell);
        }
    }

    private void applyCell(Cell poiCell, BulkCell cell) {
        if (cell.getValue() == null) {
            poiCell.setCellType(CellType.BLANK);
            return;
        }

        switch (cell.getType()) {
            case TEXT -> {
                poiCell.setCellType(CellType.STRING);
                poiCell.setCellValue((String) cell.getValue());
                poiCell.setCellStyle(textStyle);
            }
            case NUMBER -> {
                BigDecimal bd = (BigDecimal) cell.getValue();
                poiCell.setCellType(CellType.NUMERIC);
                poiCell.setCellValue(bd.doubleValue());
                poiCell.setCellStyle(numberStyle);
            }
            case PERCENT -> {
                BigDecimal bd = (BigDecimal) cell.getValue();
                poiCell.setCellType(CellType.NUMERIC);
                poiCell.setCellValue(bd.divide(BigDecimal.valueOf(100)).doubleValue());
                poiCell.setCellStyle(percentStyle);
            }
            case DATE -> {
                LocalDate date = (LocalDate) cell.getValue();
                poiCell.setCellType(CellType.NUMERIC);
                poiCell.setCellValue(java.sql.Date.valueOf(date));
                poiCell.setCellStyle(dateStyle);
            }
            case DATETIME -> {
                LocalDateTime dt = (LocalDateTime) cell.getValue();
                poiCell.setCellType(CellType.NUMERIC);
                poiCell.setCellValue(java.sql.Timestamp.valueOf(dt));
                poiCell.setCellStyle(dateTimeStyle);
            }
            case BOOLEAN -> {
                poiCell.setCellType(CellType.BOOLEAN);
                poiCell.setCellValue((Boolean) cell.getValue());
                poiCell.setCellStyle(textStyle);
            }
        }
    }

    public void flush() {
        try {
            workbook.write(out);
            out.flush();
        } catch (IOException e) {
            throw new RuntimeException("Erro ao escrever Excel: " + e.getMessage(), e);
        }
    }

    @Override
    public void close() {
        try {
            workbook.close();
        } catch (IOException e) {
            // Ignora: o OutputStream já foi fechado pelo container
        }
    }
}