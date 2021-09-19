package net.intelie.challenges;

import java.time.LocalDateTime;

/**
 * This is just an event stub, feel free to expand it if needed.
 */
public class Event {
	private String type;
	private LocalDateTime timestamp;

	public Event(String type, LocalDateTime timestamp) {
		this.type = type;
		this.timestamp = timestamp;
	}

	public String getType() {
		return type;
	}

	public LocalDateTime getTimestamp() {
		return timestamp;
	}

}
