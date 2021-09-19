package net.intelie.challenges;

import io.github.benas.randombeans.api.Randomizer;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class EventTypeRandomizer implements Randomizer<String> {

    private List<String> types = Arrays.asList(EventType.READ_STATUS, EventType.CHANGE_STATUS, EventType.LOCK_STATUS);

    @Override
    public String getRandomValue() {
        return types.get(new Random().nextInt(types.size()));
    }

}
