package sequential;

import java.util.LinkedList;
import java.util.List;

public class Root {
    private List<Process> processes = new LinkedList<>();
    private int numberOfProcesses;
    private int timePointsCount;
    private Double[][] temperatureArray;

    public List<Process> getProcesses() {
        return processes;
    }

    public Root(int numberOfProcesses, int timePointsCount, Double[][] temperatureArray) {
        this.numberOfProcesses = numberOfProcesses;
        this.timePointsCount = timePointsCount;
        this.temperatureArray = temperatureArray;

        for (int i = 0; i < numberOfProcesses; i++) {
            processes.add(new Process(this, temperatureArray[i]));
        }
    }

    public int getNumberOfProcesses() {
        return numberOfProcesses;
    }

    public int getTimePointsCount() {
        return timePointsCount;
    }

    public void runProcesses() {
        for (int j = 0; j < timePointsCount - 1; j++) {
            for (int i = 0; i < processes.size(); i++) {
                List<TimePoint> points = processes.get(i).getTimePoints();
                TimePoint last = points.get(points.size() - 1);
                TimePoint next = processes.get(i).calculateNext(last);
                points.add(next);
            }
        }
    }
}
