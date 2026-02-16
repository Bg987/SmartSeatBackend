package com.example.SmartSeatBackend.service;

import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;


@RequiredArgsConstructor
@Service
public class CollegeService {



    public void exelProcess(MultipartFile file) throws IOException {
        XSSFWorkbook workbook = new XSSFWorkbook(file.getInputStream());
        DataFormatter formatter = new DataFormatter();
        for(Sheet sheet : workbook) {
            System.out.println(sheet.getSheetName());

            for(Row row : sheet) {
                for(Cell cell : row){
                    String cellValue = formatter.formatCellValue(cell);
                    System.out.println(cellValue + "\t");
                }
            }
        }
    }
}