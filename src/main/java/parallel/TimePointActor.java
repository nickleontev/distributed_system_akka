package parallel;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static akka.pattern.PatternsCS.ask;

public class TimePointActor extends AbstractActor {
    List<ActorRef> materialPoints = new LinkedList<>();

    private TimePointActor(List<Double> temperatures) {
        for (int i = 0; i < temperatures.size(); i++) {
            ActorRef ref = getContext().actorOf(MaterialPointActor.props(temperatures.get(i)));
            this.materialPoints.add(ref);
        }
    }

    public static Props props(List<Double> temperatures) {
        return Props.create(TimePointActor.class, temperatures);
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(GetFirstMaterialPoint.class, v -> {
                    sendValueByIndex(0);
                })
                .match(GetLastMaterialPoint.class, v -> {
                    sendValueByIndex(materialPoints.size() - 1);
                })
                .match(GetMaterialPointsCount.class, v -> {
                    getSender().tell(this.materialPoints.size(), self());
                })
                .match(GetMaterialPointsByIndex.class, v -> {
                    if (v.index >= 0 && v.index < materialPoints.size()) {
                        sendValueByIndex(v.index);
                    }
                })
                .match(Print.class, v -> {
                    getSender().tell(print(), self());
                })
                .build();
    }

    private void sendValueByIndex(int index) throws ExecutionException, InterruptedException {
        ActorRef materialPointRef = this.materialPoints.get(index);
        CompletableFuture<Object> future = ask(materialPointRef, new MaterialPointActor.GetTemperature(), 999999).toCompletableFuture();
        Double value = (Double) future.get();
        getSender().tell(value, self());
    }

    private interface Command {
    }

    public static final class GetFirstMaterialPoint implements Command {
    }

    public static final class GetLastMaterialPoint implements Command {
    }

    public static final class GetMaterialPointsCount implements Command {
    }

    public static final class GetMaterialPointsByIndex implements Command {
        final Integer index;

        public GetMaterialPointsByIndex(Integer index) {
            this.index = index;
        }
    }

    public static final class Print implements Command {
    }

    public String print() throws ExecutionException, InterruptedException {
        StringBuilder builder = new StringBuilder();
        builder.append("{");
        for (int i = 0; i < materialPoints.size(); i++) {
            NumberFormat formatter = new DecimalFormat("#0.00");
            builder.append(formatter.format(getValue(materialPoints.get(i))));
            if (i != materialPoints.size() - 1)
                builder.append("; ");
        }
        builder.append("}");
        return builder.toString();
    }

    private Double getValue(ActorRef ref) throws ExecutionException, InterruptedException {
        CompletableFuture<Object> future = ask(ref, new MaterialPointActor.GetTemperature(), 999999).toCompletableFuture();
        return (Double) future.get();
    }
}
