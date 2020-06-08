package parallel;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static akka.pattern.PatternsCS.ask;

public class Main {
    public static void main(String[] args) throws InterruptedException {

        ActorSystem system = ActorSystem.create("test-system");

        Integer numberOfProcesses = 3;
        Integer timePointsCount = 4;
        Double[][] temperatureArray = {
                {8.3, 6.2, 4.1, 5.6},
                {3.8, 2.4, 1.2, 6.3},
                {4.4, 8.2, 9.1, 6.6}
        };

        List<List<Double>> listOfLists = Arrays.stream(Objects.requireNonNull(temperatureArray)).map(row -> {
            return Arrays.asList((row != null) ? row : new Double[0]);
        }).collect(Collectors.toList());

        TemperaturesPOJO temperatures = new TemperaturesPOJO(listOfLists);

//        ActorRef root = system.actorOf(RootActor.props(numberOfProcesses, timePointsCount, temperatures));
//        root.tell(new RootActor.RunProcesses(), ActorRef.noSender());
//        Thread.sleep(10000);
//        System.out.println("tell print!");
//        root.tell(new RootActor.Print(), ActorRef.noSender());
//
//        System.out.println("");

        ActorRef root = system.actorOf(RootActor.props(numberOfProcesses, timePointsCount, temperatures));

        CompletableFuture<Object> future = ask(root, new RootActor.RunProcesses(), 3000000).toCompletableFuture();
        future.thenAccept(v -> {
                    if (future.isDone()) {
                        System.out.println("tell print!");
                        root.tell(new RootActor.Print(), ActorRef.noSender());
                    }
                }
        );
    }
}
