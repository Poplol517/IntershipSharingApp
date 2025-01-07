package mdad.localdata.intershipsharingapp;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Message {
    private String userId;
    private String text, name;
    private String timestamp;  // Store timestamp as string

    // Constructor for initializing the message
    public Message(String userId, String text, String timestamp, String name) {
        this.userId = userId;
        this.text = text;
        this.timestamp = timestamp;
        this.name = name;
    }

    // Getter methods
    public String getUserId() {
        return userId;
    }

    public String getText() {
        return text;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public String getName() {
        return name;
    }

    // Method to convert timestamp string to LocalDateTime
    public LocalDateTime getLocalDateTime() {
        // Define the DateTimeFormatter matching your timestamp format
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        // Parse the timestamp string into a LocalDateTime object
        return LocalDateTime.parse(this.timestamp, formatter);
    }
}
