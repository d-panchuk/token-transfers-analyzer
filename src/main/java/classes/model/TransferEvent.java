package classes.model;

import java.sql.Timestamp;

public class TransferEvent {

	private String transactionHash;
	private Long blockNumber;
	private Timestamp creationDate;
	private String sender;
	private String receiver;
	private Double quantity;

	public TransferEvent() {}

	public String getTransactionHash() {
		return transactionHash;
	}

	public void setTransactionHash(String transactionHash) {
		this.transactionHash = transactionHash;
	}

	public Long getBlockNumber() {
		return blockNumber;
	}

	public void setBlockNumber(Long blockNumber) {
		this.blockNumber = blockNumber;
	}

	public Timestamp getCreationDate() {
		return creationDate;
	}

	public void setCreationDate(Timestamp creationDate) {
		this.creationDate = creationDate;
	}

	public String getSender() {
		return sender;
	}

	public void setSender(String sender) {
		this.sender = sender;
	}

	public String getReceiver() {
		return receiver;
	}

	public void setReceiver(String receiver) {
		this.receiver = receiver;
	}

	public Double getQuantity() {
		return quantity;
	}

	public void setQuantity(Double quantity) {
		this.quantity = quantity;
	}

	@Override
	public String toString() {
		return "TransferEvent [transactionHash=" + transactionHash +
				", blockNumber=" + blockNumber +
				", creationDate=" + creationDate +
				", sender=" + sender +
				", receiver=" + receiver +
				", quantity=" + quantity + "]";
	}
}