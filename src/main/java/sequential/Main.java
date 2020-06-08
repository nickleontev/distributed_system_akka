package sequential;

public class Main {
    public static void main(String[] args) {
        int numberOfProcesses = 3;
        int timePointsCount = 4;
        Double[][] temperatureArray = {
                {8.3, 6.2, 4.1, 5.6},
                {3.8, 2.4, 1.2, 6.3},
                {4.4, 8.2, 9.1, 6.6}
        };

        Root root = new Root(numberOfProcesses, timePointsCount, temperatureArray);
        root.runProcesses();
        ProcessService.print(root);
    }
}
