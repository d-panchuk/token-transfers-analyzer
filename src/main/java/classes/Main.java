package classes;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import classes.controller.TokenController;
import classes.model.TransferEventDao;
import classes.writer.XLSWriter;

public class Main {

	private static final String QTUM_TOKEN_ADDRESS = "0x9a642d6b3368ddc662ca244badf32cda716005bc";
	private static final long START_BLOCK = 4225038; // 9/1/2017 12:00:59 AM 
	private static final long END_BLOCK = 4326063; // 10/1/2017 12:00:56 AM
	private static final String FILE_PATH = "QtumStats [blocks " + START_BLOCK + "-" + END_BLOCK + "].xls";
	
	public static void main(String[] args) throws IOException, InterruptedException, ExecutionException {
		TransferEventDao transferEventDao = new TransferEventDao();
		
		TokenController tokenController = new TokenController(QTUM_TOKEN_ADDRESS, transferEventDao);
		tokenController.processTokenTransactions(START_BLOCK, END_BLOCK);
		
		Map<String, Double> statistics = tokenController.calculateStatistics();
		
		XLSWriter xlsWriter = new XLSWriter();
		xlsWriter.generateTokenTransactionsReport(FILE_PATH, statistics);
	}
}