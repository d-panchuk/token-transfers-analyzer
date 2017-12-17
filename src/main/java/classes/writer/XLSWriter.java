package classes.writer;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class XLSWriter {

	public void generateTokenTransactionsReport(String filePath, Map<String, Double> statistics) {
		XSSFWorkbook workbook = new XSSFWorkbook();
		XSSFSheet sheet = workbook.createSheet("Token statistics");
		
		writeStatistics(sheet, statistics);
		
		try (FileOutputStream outputStream = new FileOutputStream(filePath)) {
			workbook.write(outputStream);
			workbook.close();
		} catch (IOException e) {
			throw new RuntimeException(e.getMessage());
		}
	}

	private void writeStatistics(XSSFSheet sheet, Map<String, Double> statistics) {
		int rowCounter = 0;
		Row row = sheet.createRow(rowCounter++);
		
		Cell keyCell = row.createCell(0);
		Cell valueCell = row.createCell(1);
		
		keyCell.setCellValue("Token holder address");
		valueCell.setCellValue("Token holder balance difference");
		
		Map<String, Double> stats = new TreeMap<>(
				(key1, key2) -> ((String) key1).compareTo((String) key2));
		
		for (Map.Entry<String, Double> entry : statistics.entrySet()) {
			row = sheet.createRow(rowCounter++);
			keyCell = row.createCell(0);
			valueCell = row.createCell(1);
			
			String currentKey = entry.getKey();
			Double currentValue = entry.getValue();
			
			if (currentKey.charAt(0) != '0') {
				stats.put(entry.getKey(), entry.getValue());
			} else {
				Double value = currentValue / Math.pow(10.f, 18);
				keyCell.setCellValue(currentKey);
				valueCell.setCellValue(value);
			}
		}
		
		rowCounter++;
		for (Map.Entry<String, Double> entry : stats.entrySet()) {
			row = sheet.createRow(rowCounter++);
			keyCell = row.createCell(0);
			valueCell = row.createCell(1);
			
			String currentKey = entry.getKey();
			Double currentValue = entry.getValue();
			
			keyCell.setCellValue(currentKey);
			
			 if (currentKey.charAt(0) == 'I' || currentKey.equals("Transfers capacity")) {
				Integer value = currentValue.intValue();
				valueCell.setCellValue(value);
			} else {
				Double value = currentValue / Math.pow(10.f, 18);
				valueCell.setCellValue(value);
			}
		}
	}
}