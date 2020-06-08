package sequential;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class TimePoint {
    List<MaterialPoint> materialPoints = new LinkedList<>();

    public TimePoint(Double[] temperatureArray) {
        for (int i = 0; i < temperatureArray.length; i++) {
            this.materialPoints.add(new MaterialPoint(temperatureArray[i]));
        }
    }

    public TimePoint(MaterialPoint[] points) {
        materialPoints.addAll(Arrays.asList(points));
    }

    public void addMaterialPoint(MaterialPoint point) {
        materialPoints.add(point);
    }

    public List<MaterialPoint> getMaterialPoints() {
        return materialPoints;
    }

    public int materialPointsCount() {
        return materialPoints.size();
    }


    public MaterialPoint getFirstMaterialPoint() {
        return materialPoints.get(0);
    }

    public MaterialPoint getLastMaterialPoint() {
        return materialPoints.get(materialPoints.size() - 1);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("{");
        for (int i = 0; i < materialPoints.size(); i++) {
            NumberFormat formatter = new DecimalFormat("#0.00");
            builder.append(formatter.format(materialPoints.get(i).getTemperature()));
            if (i != materialPoints.size() - 1)
                builder.append("; ");
        }
        builder.append("}");
        return builder.toString();
    }
}
