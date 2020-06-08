package parallel;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static akka.pattern.PatternsCS.ask;

public class RootActor extends AbstractActor {

    private List<ActorRef> processes = new LinkedList<>();
    private Integer numberOfProcesses;
    private Integer timePointsCount;

    public RootActor(Integer numberOfProcesses, Integer timePointsCount, TemperaturesPOJO temp) {
        this.numberOfProcesses = numberOfProcesses;
        this.timePointsCount = timePointsCount;
        List<List<Double>> temperatures = temp.getListOfTemperatures();

        for (int i = 0; i < numberOfProcesses; i++) {
            ActorRef processRef = getContext().actorOf(ProcessActor.props(getSelf(), i, numberOfProcesses, temperatures.get(i)));
            processes.add(processRef);
        }
    }

    public static Props props(Integer numberOfProcesses, Integer timePointsCount, TemperaturesPOJO temperatures) {
        return Props.create(RootActor.class, numberOfProcesses, timePointsCount, temperatures);
    }

    private synchronized ActorRef getValue(Integer index) {
        return this.processes.get(index);
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(GetProcessByIndex.class, v -> {
                    System.out.println("Index="+v.index);
                    if (v.index >= 0 && v.index < processes.size()) {
                        getSender().tell(getValue(v.index), self());
                    }
                })
                .match(RunProcesses.class, v -> {
                    for (int j = 0; j < timePointsCount - 1; j++) {
                        CompletableFuture[] completableFutures = new CompletableFuture[processes.size()];
                        for (int i = 0; i < processes.size(); i++) {
                            ActorRef process = processes.get(i);
                            CompletableFuture<Object> future = ask(process, new ProcessActor.CalculateNext(), 999999).toCompletableFuture();
                            completableFutures[i] = future;
                             //Thread.sleep(100);
                            //  process.tell(new ProcessActor.CalculateNext(), self());
                            //  CompletableFuture.allOf()
                        }

                       // CompletableFuture.allOf(completableFutures).get();
                    }
                    System.out.println("DONE!");
                })
                .match(Print.class, v -> {
                    System.out.println(print());
                })
                .build();
    }

    private interface Command {
    }

    public static final class GetProcessByIndex implements Command {
        final Integer index;

        public GetProcessByIndex(Integer index) {
            this.index = index;
        }
    }

    public static final class RunProcesses implements Command {
    }

    public static final class Print implements Command {
    }

    public String print() throws ExecutionException, InterruptedException {
        StringBuilder builder = new StringBuilder();

        for (int i = 0; i < processes.size(); i++) {
            builder.append(getValue(processes.get(i)))
                    .append("\n");
        }

        return builder.toString();
    }

    private String getValue(ActorRef ref) throws ExecutionException, InterruptedException {
        CompletableFuture<Object> future = ask(ref, new ProcessActor.Print(), 10000).toCompletableFuture();
        return (String) future.get();
    }
}
