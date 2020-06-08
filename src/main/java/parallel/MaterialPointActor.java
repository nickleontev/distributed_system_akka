package parallel;

import akka.actor.AbstractActor;
import akka.actor.Props;

public class MaterialPointActor extends AbstractActor {

    Double temperature;

    private MaterialPointActor(Double temperature) {
        this.temperature = temperature;
    }

    public static Props props(Double temperature) {
        return Props.create(MaterialPointActor.class, temperature);
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(SetTemperature.class, v -> {
                    this.temperature = v.temperature;
                })
                .match(GetTemperature.class, v -> {
                    getSender().tell(this.temperature, self());
                })
                .build();
    }

    interface Command {
    }

    public static final class SetTemperature implements Command {
        Double temperature;

        public SetTemperature(Double temperature) {
            this.temperature = temperature;
        }
    }

    public static final class GetTemperature implements Command {
    }
}
