package parallel;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static akka.pattern.PatternsCS.ask;

public class ProcessActor extends AbstractActor {
    private static final String DELIMITER = "/";
    private static final long TIMEOUT = 999999;
    private Integer processIndex;
    private Integer processesCount;
    private ActorRef root;
    private List<ActorRef> timePoints = new LinkedList<>();

    public ProcessActor(ActorRef root, Integer processIndex, Integer processesCount, List<Double> temperatures) {
        this.root = root;
        this.processIndex = processIndex;
        this.processesCount = processesCount;
        ActorRef firstTimePointRef = getContext().actorOf(TimePointActor.props(temperatures));
        timePoints.add(firstTimePointRef);
    }

    public static Props props(ActorRef root, Integer processIndex, Integer processesCount, List<Double> temperatures) {
        return Props.create(ProcessActor.class, root, processIndex, processesCount, temperatures);
    }

    private Integer getMaterialPointCount(ActorRef timePointRef) throws ExecutionException, InterruptedException {
        CompletableFuture<Object> future = ask(timePointRef, new TimePointActor.GetMaterialPointsCount(), TIMEOUT).toCompletableFuture();
        return (Integer) future.get();
    }

    private Double getFirstMaterialPointValue(ActorRef timePointRef) throws ExecutionException, InterruptedException {
        CompletableFuture<Object> future = ask(timePointRef, new TimePointActor.GetFirstMaterialPoint(), TIMEOUT).toCompletableFuture();
        return (Double) future.get();
    }

    private Double getLastMaterialPointValue(ActorRef timePointRef) throws ExecutionException, InterruptedException {
        CompletableFuture<Object> future = ask(timePointRef, new TimePointActor.GetLastMaterialPoint(), TIMEOUT).toCompletableFuture();
        return (Double) future.get();
    }

    private Double getMaterialPointValueByIndex(ActorRef timePointRef, Integer index) throws ExecutionException, InterruptedException {
        CompletableFuture<Object> future = ask(timePointRef, new TimePointActor.GetMaterialPointsByIndex(index), TIMEOUT).toCompletableFuture();
        return (Double) future.get();
    }

    private Double getAverage(Double v1, Double v2, Double v3) {
        System.out.println("v1=" + v1 + " v2=" + v2 + " v3=" + v3);
        return (v1 + v2 + v3) / 3;
    }

    public void calculateNext() throws ExecutionException, InterruptedException {
        System.out.println(self().path().toString());
        ActorRef lastTimePointRef = timePoints.get(timePoints.size() - 1);
        int size = getMaterialPointCount(lastTimePointRef);
        Double[] temperatures = new Double[size];

        //if process is first
        if (processIndex == 0) {
            temperatures[0] = getFirstMaterialPointValue(lastTimePointRef);

            for (int i = 0; i < size - 1; i++) {
                if (i != size - 2) {
                    Double point1 = getMaterialPointValueByIndex(lastTimePointRef, i);
                    System.out.println("point1=" + point1);
                    Double point2 = getMaterialPointValueByIndex(lastTimePointRef, i + 1);
                    System.out.println("point2=" + point2);
                    Double point3 = getMaterialPointValueByIndex(lastTimePointRef, i + 2);
                    System.out.println("point3=" + point3);
                    temperatures[i + 1] = getAverage(point1, point2, point3);
                } else {
                    Double point1 = getMaterialPointValueByIndex(lastTimePointRef, i);
                    System.out.println("point11=" + point1);
                    Double point2 = getMaterialPointValueByIndex(lastTimePointRef, i + 1);
                    System.out.println("point22=" + point2);
                    //!!!
                    ActorRef nextProcess = getNext();
                    ActorRef timePointRefOfNextProcess = getTimePointRefByIndex(nextProcess, timePoints.size() - 1);
                    Double point3 = getFirstMaterialPointValue(timePointRefOfNextProcess);
                    System.out.println("point33=" + point3);
                    //i+1 = nextPoints.length - 1 = last
                    temperatures[i + 1] = getAverage(point1, point2, point3);
                }
            }
            //if process is last
        } else if (processIndex == processesCount - 1) {
            temperatures[size - 1] = getLastMaterialPointValue(lastTimePointRef);
            for (int i = 0; i <= size - 2; i++) {
                if (i == 0) {
                    //!!!
                    ActorRef previousProcess = getPrevious();
                    ActorRef timePointRefOfPreviousProcess = getTimePointRefByIndex(previousProcess, timePoints.size() - 1);
                    Double point1 = getLastMaterialPointValue(timePointRefOfPreviousProcess);
                    //--
                    Double point2 = getMaterialPointValueByIndex(lastTimePointRef, i);
                    Double point3 = getMaterialPointValueByIndex(lastTimePointRef, i + 1);
                    temperatures[i] = getAverage(point1, point2, point3);
                } else {
                    Double point1 = getMaterialPointValueByIndex(lastTimePointRef, i - 1);
                    Double point2 = getMaterialPointValueByIndex(lastTimePointRef, i);
                    Double point3 = getMaterialPointValueByIndex(lastTimePointRef, i + 1);
                    temperatures[i] = getAverage(point1, point2, point3);
                }
            }
            //if process between first and last
        } else {
            for (int i = 0; i < size; i++) {
                if (i == 0) {
                    ActorRef previousProcess = getPrevious();
                    ActorRef timePointRefOfPreviousProcess = getTimePointRefByIndex(previousProcess, timePoints.size() - 1);
                    Double point1 = getLastMaterialPointValue(timePointRefOfPreviousProcess);
                    Double point2 = getMaterialPointValueByIndex(lastTimePointRef, i);
                    Double point3 = getMaterialPointValueByIndex(lastTimePointRef, i + 1);
                    temperatures[i] = getAverage(point1, point2, point3);
                } else if (i == size - 1) {
                    Double point1 = getMaterialPointValueByIndex(lastTimePointRef, i - 1);
                    Double point2 = getMaterialPointValueByIndex(lastTimePointRef, i);
                    ActorRef nextProcess = getNext();
                    ActorRef timePointRefOfNextProcess = getTimePointRefByIndex(nextProcess, timePoints.size() - 1);
                    Double point3 = getFirstMaterialPointValue(timePointRefOfNextProcess);
                    temperatures[i] = getAverage(point1, point2, point3);
                } else {
                    Double point1 = getMaterialPointValueByIndex(lastTimePointRef, i - 1);
                    Double point2 = getMaterialPointValueByIndex(lastTimePointRef, i);
                    Double point3 = getMaterialPointValueByIndex(lastTimePointRef, i + 1);
                    temperatures[i] = getAverage(point1, point2, point3);
                }
            }
        }

        ActorRef nextTimePointRef = getContext()
                .actorOf(TimePointActor.props(Arrays.asList(temperatures)), processIndex + DELIMITER + "0");
        timePoints.add(nextTimePointRef);
    }


    private ActorRef getPrevious() throws ExecutionException, InterruptedException {
        CompletableFuture<Object> future = ask(getContext().getParent(), new RootActor.GetProcessByIndex(processIndex - 1), TIMEOUT).toCompletableFuture();
        return (ActorRef) future.get();
    }

    private ActorRef getNext() throws ExecutionException, InterruptedException {
        CompletableFuture<Object> future = ask(getContext().getParent(), new RootActor.GetProcessByIndex(processIndex + 1), TIMEOUT).toCompletableFuture();
        return (ActorRef) future.get();
    }

    private ActorRef getTimePointRefByIndex(ActorRef process, Integer index) throws ExecutionException, InterruptedException {
        CompletableFuture<Object> future = ask(process, new ProcessActor.GetTimePointRefByIndex(index), TIMEOUT).toCompletableFuture();
        return (ActorRef) future.get();
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(CalculateNext.class, v -> {
                    calculateNext();
                })
                .match(GetTimePointRefByIndex.class, v -> {
                    if (v.index >= 0 && v.index < timePoints.size()) {
                        getSender().tell(this.timePoints.get(v.index), self());
                    }
                })
                .match(Print.class, v -> {
                    getSender().tell(print(), self());
                })
                .build();
    }

    private interface Command {
    }

    public static final class CalculateNext implements Command {
    }

    public static final class GetTimePointRefByIndex implements Command {
        final Integer index;

        public GetTimePointRefByIndex(Integer index) {
            this.index = index;
        }
    }

    public static final class Print implements Command {
    }

    public String print() throws ExecutionException, InterruptedException {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < timePoints.size(); i++) {
            builder.append("t")
                    .append(i)
                    .append("=")
                    .append(getValue(timePoints.get(i)));
            if (i != timePoints.size() - 1) {
                builder.append(",");
            }
        }
        return builder.toString();
    }

    private String getValue(ActorRef ref) throws ExecutionException, InterruptedException {
        CompletableFuture<Object> future = ask(ref, new TimePointActor.Print(), TIMEOUT).toCompletableFuture();
        return (String) future.get();
    }
}
