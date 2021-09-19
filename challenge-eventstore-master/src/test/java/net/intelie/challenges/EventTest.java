package net.intelie.challenges;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.stream.Stream;

import org.junit.Test;

import io.github.benas.randombeans.EnhancedRandomBuilder;
import io.github.benas.randombeans.FieldDefinitionBuilder;
import io.github.benas.randombeans.api.EnhancedRandom;

public class EventTest {

	@Test
	public void givenEvent_whenSaveItAndFindIt_thenSameEvent() {

		EventStore eventStore = new EventStoreImpl();

		EnhancedRandom enhancedRandom = EnhancedRandomBuilder.aNewEnhancedRandomBuilder()
				.dateRange(LocalDate.now().minusDays(1), LocalDate.now())
				.exclude(FieldDefinitionBuilder.field().named("id").get()).exclude(Serializable.class)
				.randomize(FieldDefinitionBuilder.field().named("type").ofType(String.class).get(),
						new EventTypeRandomizer())
				.build();

		Event event = enhancedRandom.nextObject(Event.class);

		eventStore.insert(event);

		assertThat(eventStore.getEvents()).isNotNull();

		Event savedEvent = eventStore.getEvents().get(0);

		assertThat(savedEvent).isNotNull();
		assertThat(event.getTimestamp()).isEqualTo(savedEvent.getTimestamp());
		assertThat(event.getType()).isEqualTo(savedEvent.getType());
	}

	@Test
	public void givenSomeEvents_whenDeleteAllEventsByType_thenOnlyOthers() {

		EventStore eventStore = new EventStoreImpl();

		EnhancedRandom enhancedRandom = EnhancedRandomBuilder.aNewEnhancedRandomBuilder()
				.dateRange(LocalDate.now().minusDays(1), LocalDate.now())
				.exclude(FieldDefinitionBuilder.field().named("id").get()).exclude(Serializable.class)
				.randomize(FieldDefinitionBuilder.field().named("type").ofType(String.class).get(),
						new EventTypeRandomizer())
				.build();

		for (int i = 0; i < 100; i++) {
			eventStore.insert(enhancedRandom.nextObject(Event.class));
		}

		assertThat(eventStore.getEvents().size()).isEqualTo(100);

		eventStore.removeAll(EventType.CHANGE_STATUS);

		assertThat(eventStore.getEvents().size()).isLessThan(100);

	}

	@Test
	public void givenALargeDatabaseEvents_whenSearchEventsByTypeBetweenDates_thenEventIteratorIsNotNullAndIsPossibleRemoveElements()
			throws Exception {

		EventStore eventStore = new EventStoreImpl();

		EnhancedRandom enhancedRandom = EnhancedRandomBuilder.aNewEnhancedRandomBuilder()
				.dateRange(LocalDate.now().minusDays(1), LocalDate.now())
				.exclude(FieldDefinitionBuilder.field().named("id").get()).exclude(Serializable.class)
				.randomize(FieldDefinitionBuilder.field().named("type").ofType(String.class).get(),
						new EventTypeRandomizer())
				.build();

		Stream<Event> events = enhancedRandom.objects(Event.class, 10000);

		events.forEach(event -> eventStore.insert(event));

		assertThat(eventStore.getEvents()).isNotNull();

		assertThat(eventStore.getEvents().size()).isEqualTo(10000);

		EventIterator eventIterator = eventStore.query(EventType.LOCK_STATUS, LocalDateTime.now().minusDays(2),
				LocalDateTime.now());

		assertThat(eventIterator).isNotNull();

		while (eventIterator.moveNext()) {
			Event event = eventIterator.current();
			eventIterator.remove();
		}

		eventIterator.close();

		eventIterator.remove();

		assertThat(eventIterator.moveNext()).isFalse();

	}

	@Test
	public void givenEventStoreWith1Event_whenQueryByEventAndGetOne_thenSameEvent() {

		EventStore eventStore = new EventStoreImpl();

		EnhancedRandom enhancedRandom = EnhancedRandomBuilder.aNewEnhancedRandomBuilder()
				.dateRange(LocalDate.now().minusDays(1), LocalDate.now())
				.exclude(FieldDefinitionBuilder.field().named("id").get()).exclude(Serializable.class)
				.randomize(FieldDefinitionBuilder.field().named("type").ofType(String.class).get(),
						new EventTypeRandomizer())
				.build();

		Event event = enhancedRandom.nextObject(Event.class);

		eventStore.insert(event);

		assertThat(eventStore.getEvents()).isNotNull();

		EventIterator eventIterator = eventStore.query(event.getType(), event.getTimestamp(),
				LocalDateTime.now().plusDays(1));
		assertThat(eventIterator.moveNext()).isTrue();
		assertThat(eventIterator.current()).isNotNull();

	}

	@Test
	public void givenAEmptyEventStore_whenTryRemoveActualItem_thenDoNothing() throws Exception {
		EventStore eventStore = new EventStoreImpl();
		EventIterator eventIterator = eventStore.query(EventType.LOCK_STATUS, LocalDateTime.now().minusDays(2),
				LocalDateTime.now());
		eventIterator.remove();
	}

	@Test
	public void givenAEmptyEventStore_whenTryGetActualItem_thenNull() throws Exception {
		EventStore eventStore = new EventStoreImpl();
		EventIterator eventIterator = eventStore.query(EventType.LOCK_STATUS, LocalDateTime.now().minusDays(2),
				LocalDateTime.now());
		assertThat(eventIterator.current()).isNull();
	}

	@Test
	public void givenAEventStoreWith100Events_whenTryGetActualItemAfterMoveNextAndRemove_thenNull() throws Exception {
		EventStore eventStore = new EventStoreImpl();

		EnhancedRandom enhancedRandom = EnhancedRandomBuilder.aNewEnhancedRandomBuilder()
				.dateRange(LocalDate.now().minusDays(1), LocalDate.now())
				.exclude(FieldDefinitionBuilder.field().named("id").get()).exclude(Serializable.class)
				.randomize(FieldDefinitionBuilder.field().named("type").ofType(String.class).get(),
						new EventTypeRandomizer())
				.build();

		Stream<Event> events = enhancedRandom.objects(Event.class, 100);

		events.forEach(event -> eventStore.insert(event));

		assertThat(eventStore.getEvents()).isNotNull();

		EventIterator eventIterator = eventStore.query(EventType.LOCK_STATUS, LocalDateTime.now().minusDays(2),
				LocalDateTime.now());

		assertThat(eventIterator).isNotNull();

		while (eventIterator.moveNext()) {
			Event event = eventIterator.current();
			eventIterator.remove();
			assertThat(eventIterator.current()).isNull();
		}

		assertThat(eventIterator.moveNext()).isFalse();

		eventIterator.close();

		eventIterator.remove();

		assertThat(eventIterator.moveNext()).isFalse();

	}
}
