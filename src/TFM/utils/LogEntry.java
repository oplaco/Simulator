package TFM.utils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

public class LogEntry {
    private long timestamp;
    private Map<String, Double> attributes;

    public LogEntry(long timestamp) {
        this.timestamp = timestamp;
        this.attributes = new HashMap<>();
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setAttribute(String name, double value) {
        attributes.put(name, value);
    }

    public double getAttribute(String name) {
        return attributes.getOrDefault(name, Double.NaN);
    }

    public Map<String, Double> getAttributes() {
        return new HashMap<>(attributes);
    }

    @Override
    public String toString() {
        StringJoiner sj = new StringJoiner(",");
        sj.add(String.valueOf(timestamp));
        for (Map.Entry<String, Double> entry : attributes.entrySet()) {
            sj.add(entry.getKey() + "=" + entry.getValue());
        }
        return sj.toString();
    }

    public String toCsv() {
        StringJoiner sj = new StringJoiner(",");
        sj.add(String.valueOf(timestamp));
        for (Double value : attributes.values()) {
            sj.add(String.valueOf(value));
        }
        return sj.toString();
    }

    public static String getCsvHeader(List<LogEntry> logEntries) {
        if (logEntries.isEmpty()) {
            return "Timestamp";
        }
        LogEntry sampleEntry = logEntries.get(0);
        StringJoiner sj = new StringJoiner(",");
        sj.add("Timestamp");
        for (String key : sampleEntry.getAttributes().keySet()) {
            sj.add(key);
        }
        return sj.toString();
    }

}
