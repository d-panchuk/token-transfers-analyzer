package classes.controller;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterNumber;
import org.web3j.protocol.core.methods.response.EthBlock.TransactionObject;
import org.web3j.protocol.http.HttpService;
import classes.generated.Token;
import classes.model.TransferEvent;
import classes.model.TransferEventDao;

public class TokenController {
	
	private static final Logger logger = Logger.getLogger(TokenController.class.getName());
	
	private static final String INFURA_MAIN_URL = "https://mainnet.infura.io/jVGsDp6rOxvjykBrZ67d";
	
	private Web3j web3j;
	private Token ercToken;
	private TransferEventDao transferEventDao;
		
	public TokenController(String tokenContractAddress, TransferEventDao transferEventDao)  {
		this.transferEventDao = transferEventDao;
		web3j = Web3j.build(new HttpService(INFURA_MAIN_URL));
		ercToken = Token.load(
				tokenContractAddress, 
				web3j, 
				Credentials.create("0x00"), 
				Token.GAS_PRICE, 
				Token.GAS_LIMIT);
	}
	
	public void processTokenTransactions(long startBlock, long endBlock) 
			throws IOException, InterruptedException, ExecutionException {

		web3j.replayBlocksObservable(
				new DefaultBlockParameterNumber(startBlock), 
				new DefaultBlockParameterNumber(endBlock), 
				true)
					.doOnError(this::handleError)
					.doOnTerminate(this::exit)
					.subscribe(block -> {
						long currentBlock = block.getBlock().getNumber().longValue();
						if(currentBlock % 25 == 0) {
							logger.info(	(endBlock - currentBlock) + " blocks remaining.");
						}
						
						block.getBlock()
							.getTransactions()
							.stream()
							.map(transactionResult -> TransactionObject.class.cast(transactionResult))
							.filter(transaction -> { 
								return ercToken.getContractAddress().equals(transaction.getFrom())
										|| ercToken.getContractAddress().equals(transaction.getTo());
							})
							.forEach(transaction -> {
								web3j.ethGetTransactionReceipt(transaction.getHash())
									.sendAsync()
									.thenAccept(transactionReceipt -> {
										ercToken.getTransferEvents(transactionReceipt.getResult())
											.forEach(transferEvent -> {
												TransferEvent event = new TransferEvent();
												
												event.setTransactionHash(transactionReceipt.getResult().getTransactionHash());
												event.setBlockNumber(transactionReceipt.getResult().getBlockNumber().longValue());
												event.setCreationDate(new Timestamp(block.getBlock().getTimestamp().longValue() * 1000));
												event.setSender(transferEvent._from);
												event.setReceiver(transferEvent._to);
												event.setQuantity(transferEvent._value.doubleValue());
												
												if(!transferEventDao.isExist(event)) {
													transferEventDao.add(event);
												}
												
												//logger.info(event.toString());
									});
								});
							});
					});
	}
	
	public Map<String, Double> calculateStatistics() {
		Map<String, Double> membersBalances = new HashMap<>();
		
		// calculate members balances statistics
		transferEventDao.getAll().forEach(transferEvent -> {
			String recieverAddress = transferEvent.getReceiver();
			String senderAddress = transferEvent.getSender();
			double transferValue = transferEvent.getQuantity();
			
			Double finalRecieverBalance;
			if(membersBalances.containsKey(recieverAddress)) {
				double recieverBalance = membersBalances.get(recieverAddress).doubleValue();
				finalRecieverBalance = new Double(recieverBalance + transferValue);
			} else {
				finalRecieverBalance = new Double(transferValue);
			}
			membersBalances.put(recieverAddress, finalRecieverBalance);	
			
			Double finalSenderBalance;
			if(membersBalances.containsKey(senderAddress)) {
				double senderBalance = membersBalances.get(senderAddress).doubleValue();
				finalSenderBalance = new Double(senderBalance - transferValue);
			} else {
				finalSenderBalance = new Double(-transferValue);
			}
			membersBalances.put(senderAddress, finalSenderBalance);
		});
		
		// calculate token investments statistics
		double tokensCirculated = 0;
		int receivers = 0, senders = 0, gotZero = 0;
		for(Double memberBalance: membersBalances.values()) {
			double currentMemberBalance = memberBalance.doubleValue();
			if(currentMemberBalance > 0) {
				tokensCirculated += currentMemberBalance;
				receivers++;
			} else if (currentMemberBalance < 0) {
				senders++;
			} else {
				gotZero++;
			}
		}
		membersBalances.put("Investors capacity who got profit", new Double(receivers));
		membersBalances.put("Investors capacity who got loss", new Double(senders));
		membersBalances.put("Investors capacity who got zero", new Double(gotZero));
		membersBalances.put("Circulated tokens capacity", new Double(tokensCirculated));
		
		// calculate transfers statistics
		Double transfersCapacity = transferEventDao.getTransfersCapacity();
		Double transferMinValue = transferEventDao.getTransferMinValue();
		Double transferMaxValue = transferEventDao.getTransferMaxValue();
		membersBalances.put("Transfers capacity", new Double(transfersCapacity));
		membersBalances.put("Transfer min value", new Double(transferMinValue));
		membersBalances.put("Transfer max value", new Double(transferMaxValue));
		
		return membersBalances;
	}
	
	private void handleError(Throwable error) {
		logger.log(Level.SEVERE, error.getMessage());
		error.printStackTrace();
	}
	
	private void exit() {
		logger.log(Level.SEVERE, "Terminated.");
		Runtime.getRuntime().exit(0);
	}
}