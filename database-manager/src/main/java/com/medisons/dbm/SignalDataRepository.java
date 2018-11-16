package com.medisons.dbm;

import com.mongodb.client.MongoCollection;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import static com.mongodb.client.model.Filters.eq;

public class SignalDataRepository {

    private static final Logger LOG = Logger.getLogger(SignalDataRepository.class.getName());

    private final MongoCollection<Document> signalDataList;

    public SignalDataRepository(MongoCollection<Document> signalDataList) {
        this.signalDataList = signalDataList;
    }

    public SignalData findByName(String name) {
        Document doc = signalDataList.find(eq("name", new ObjectId(name))).first();
        return signalData(doc);
    }

    public List<SignalData> getAllSignalData() {
        List<SignalData> allSignalData = new ArrayList<>();
        for (Document doc : signalDataList.find()) {
            allSignalData.add(signalData(doc));
        }
        return allSignalData;
    }

    public void saveSignalData(SignalData signalData) {
        Document doc = new Document();
        doc.append("name", signalData.getName());
        doc.append("frequency", signalData.getFrequency());
        doc.append("timestamp", signalData.getTimestamp());
        doc.append("dataPoints", signalData.getDataPoints());
        signalDataList.insertOne(doc);
    }

    private SignalData signalData(Document doc) {
        LOG.info("Retrieving data from document");

        List<Double> dataPoints = new ArrayList<>();
        for (Object dataPoint : doc.get("dataPoints", List.class)) {
            LOG.info("data point: " + dataPoint);
            dataPoints.add((Double) dataPoint);
        }

        LOG.info("Retrieved double list");

        return new SignalData(
                doc.getString("name"),
                doc.getDouble("frequency"),
                doc.getString("timestamp"),
                dataPoints
        );
    }
}
