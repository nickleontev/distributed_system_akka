package sequential;

public class ProcessService {
    public static void print(Root root) {
        StringBuilder builder = new StringBuilder();
        builder.append("Number Of Processes=")
                .append(root.getNumberOfProcesses())
                .append("\n")
                .append("Time Points Count=")
                .append(root.getTimePointsCount())
                .append("\n\n");


        for (int i = 0; i < root.getTimePointsCount(); i++) {
            builder.append("t")
                    .append(i)
                    .append(" |");
            for (int j = 0; j < root.getNumberOfProcesses(); j++) {
                Process process = root.getProcesses().get(j);
                TimePoint timePoint = process.getTimePoints().get(i);
                builder.append(timePoint.toString())
                        .append("|");
            }
            builder.append("\n");
        }

        System.out.println(builder.toString());
    }
}
