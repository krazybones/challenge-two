package net.intelie.challenges;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class EventStoreImpl implements EventStore {

	// Creating a thread-safe Event List
	final List<Event> events = Collections.synchronizedList(new ArrayList());

	@Override
	public void insert(Event event) {
		events.add(event);
	}

	@Override
	public void removeAll(String type) {
		int size = events.size();
		// Using Collection.removeIf, java 8 solution, to remove all references equals
		// type
		// The iime complexity is O(n)
		events.removeIf(event -> event.getType().equals(type));
	}

	@Override
	public EventIterator query(String type, LocalDateTime startTime, LocalDateTime endTime) {

		// Using Streams to filter by fields
		return new EventIteratorImpl(Collections.synchronizedList(events.stream()
				.filter(event -> event.getType().equals(type))
				.filter(event -> event.getTimestamp().isEqual(startTime) || event.getTimestamp().isAfter(startTime))
				.filter(event -> event.getTimestamp().isBefore(endTime)).collect(Collectors.toList())));
	}

	@Override
	public List<Event> getEvents() {
		return events;
	}
}
