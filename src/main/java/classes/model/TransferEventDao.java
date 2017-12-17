package classes.model;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import org.bson.Document;
import com.mongodb.MongoClient;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;

public class TransferEventDao {
	
	public static final String HOST = "localhost";
	public static final Integer PORT = 27017;
	public static final String DATABASE = "TokenTransfering";
	public static final String COLLECTION = "QtumTransfers";
	
	private static final String KEY_TX_HASH = "txHash";
	private static final String KEY_BLOCK_NUMBER = "blockNumber";
	private static final String KEY_CREATION_DATE = "creationDate";
	private static final String KEY_SENDER = "sender";
	private static final String KEY_RECEIVER = "receiver";
	private static final String KEY_QUANTITY = "quantity";
	
	private MongoClient mongoClient;
	
	public TransferEventDao() {
		ServerAddress mongoAddress = new ServerAddress(HOST, PORT);
		this.mongoClient = new MongoClient(mongoAddress);
	}
	
	public void add(TransferEvent transferEvent) {
		MongoCollection<Document> collection = loadCollection();
		Document transferDocument = new Document();
		transferDocument.append(KEY_TX_HASH, transferEvent.getTransactionHash())
					 .append(KEY_BLOCK_NUMBER, transferEvent.getBlockNumber())
					 .append(KEY_CREATION_DATE, transferEvent.getCreationDate())
					 .append(KEY_SENDER, transferEvent.getSender())
					 .append(KEY_RECEIVER, transferEvent.getReceiver())
					 .append(KEY_QUANTITY, transferEvent.getQuantity());
		collection.insertOne(transferDocument);
	}
	
	public TransferEvent getByTxHash(String txHash) {
		MongoCollection<Document> collection = loadCollection();
		Document document = collection.find(Filters.eq(KEY_TX_HASH, txHash)).first();
		
		if(document == null) {
			return null;
		}
		
		TransferEvent transferEvent = documentToTransferEvent(document);	
		return transferEvent;
	}
	
	public List<TransferEvent> getAll() {
		MongoCollection<Document> collection = loadCollection();
		List<TransferEvent> transferEvents = new ArrayList<>();
		collection.find()
				.into(new ArrayList<Document>())
				.forEach(document -> {
					TransferEvent transferEvent = documentToTransferEvent(document);
					transferEvents.add(transferEvent);
				});
		return transferEvents;
	}
	
	public Double getTransfersCapacity() {
		MongoCollection<Document> collection = loadCollection();
		long transfersCapacity = collection.count();
		return new Double(transfersCapacity);
	}
	
	public Double getTransferMinValue() {
		MongoCollection<Document> collection = loadCollection();
		Document document = collection.find().sort(new Document(KEY_QUANTITY, 1)).first();
		
		if(document == null) {
			return null;
		}
		
		Double transferMinValue = document.getDouble(KEY_QUANTITY);
		return transferMinValue;
	}
	
	public Double getTransferMaxValue() {
		MongoCollection<Document> collection = loadCollection();
		Document document = collection.find().sort(new Document(KEY_QUANTITY, -1)).first();
		
		if(document == null) {
			return null;
		}
		
		Double transferMaxValue = document.getDouble(KEY_QUANTITY);
		return transferMaxValue;
	}
	
	public boolean isExist(TransferEvent event) {
		if(getByTxHash(event.getTransactionHash()) == null)
			return false;
		
		return true;
	}
	
	private MongoCollection<Document> loadCollection() {
		MongoDatabase database = mongoClient.getDatabase(DATABASE);
		return database.getCollection(COLLECTION);
	}
	
	private TransferEvent documentToTransferEvent(Document document) {
		long time = document.getDate(KEY_CREATION_DATE).getTime();
		
		TransferEvent transferEvent = new TransferEvent();
		transferEvent.setTransactionHash(document.getString(KEY_TX_HASH));
		transferEvent.setBlockNumber(document.getLong(KEY_BLOCK_NUMBER));
		transferEvent.setCreationDate(new Timestamp(time));
		transferEvent.setSender(document.getString(KEY_SENDER));
		transferEvent.setReceiver(document.getString(KEY_RECEIVER));
		transferEvent.setQuantity(document.getDouble(KEY_QUANTITY));
		
		return transferEvent;
	}
}