package sequential;

import java.util.LinkedList;
import java.util.List;

public class Process {
    private Root parent;
    private List<TimePoint> timePoints = new LinkedList<>();

    public Process(Root parent, Double[] temperatureArray) {
        this.parent = parent;
        TimePoint firstTimePoint = new TimePoint(temperatureArray);
        timePoints.add(firstTimePoint);
    }

    public MaterialPoint getFirstMaterialPointByTimePointIndex(int index) {
        return this.timePoints.get(index).getFirstMaterialPoint();
    }

    public MaterialPoint getLastMaterialPointByTimePointIndex(int index) {
        return this.timePoints.get(index).getLastMaterialPoint();
    }

    public List<TimePoint> getTimePoints() {
        return timePoints;
    }

    public TimePoint calculateNext(TimePoint previousTP) {
        MaterialPoint[] nextPoints = new MaterialPoint[previousTP.materialPointsCount()];

        List<MaterialPoint> previousMaterialPoints = previousTP.getMaterialPoints();

        //if process is first
        if (getPrevious() == null && getNext() != null) {
            nextPoints[0] = new MaterialPoint(previousTP.getFirstMaterialPoint().getTemperature());

            for (int i = 0; i < previousMaterialPoints.size() - 1; i++) {
                if (i != previousMaterialPoints.size() - 2) {
                    MaterialPoint point1 = previousMaterialPoints.get(i);
                    MaterialPoint point2 = previousMaterialPoints.get(i + 1);
                    MaterialPoint point3 = previousMaterialPoints.get(i + 2);
                    nextPoints[i + 1] = new MaterialPoint(getAverage(point1, point2, point3));
                } else {
                    MaterialPoint point1 = previousMaterialPoints.get(i);
                    MaterialPoint point2 = previousMaterialPoints.get(i + 1);
                    MaterialPoint point3 = getNext().getFirstMaterialPointByTimePointIndex(timePoints.indexOf(previousTP));
                    //i+1 = nextPoints.length - 1 = last
                    nextPoints[i + 1] = new MaterialPoint(getAverage(point1, point2, point3));
                }

            }
            //if process is last
        } else if (getNext() == null) {
            nextPoints[nextPoints.length - 1] = new MaterialPoint(previousTP.getLastMaterialPoint().getTemperature());
            for (int i = 0; i <= previousMaterialPoints.size() - 2; i++) {
                if (i == 0) {
                    MaterialPoint point1 = getPrevious().getLastMaterialPointByTimePointIndex(timePoints.indexOf(previousTP));
                    MaterialPoint point2 = previousMaterialPoints.get(i);
                    MaterialPoint point3 = previousMaterialPoints.get(i + 1);
                    nextPoints[i] = new MaterialPoint(getAverage(point1, point2, point3));
                } else {
                    MaterialPoint point1 = previousMaterialPoints.get(i - 1);
                    MaterialPoint point2 = previousMaterialPoints.get(i);
                    MaterialPoint point3 = previousMaterialPoints.get(i + 1);
                    nextPoints[i] = new MaterialPoint(getAverage(point1, point2, point3));
                }
            }
            //if process between first and last
        } else {
            for (int i = 0; i < previousMaterialPoints.size(); i++) {
                if (i == 0) {
                    MaterialPoint point1 = getPrevious().getLastMaterialPointByTimePointIndex(timePoints.indexOf(previousTP));
                    MaterialPoint point2 = previousMaterialPoints.get(i);
                    MaterialPoint point3 = previousMaterialPoints.get(i + 1);
                    nextPoints[i] = new MaterialPoint(getAverage(point1, point2, point3));
                } else if (i == nextPoints.length - 1) {
                    MaterialPoint point1 = previousMaterialPoints.get(i - 1);
                    MaterialPoint point2 = previousMaterialPoints.get(i);
                    MaterialPoint point3 = getNext().getFirstMaterialPointByTimePointIndex(timePoints.indexOf(previousTP));
                    nextPoints[i] = new MaterialPoint(getAverage(point1, point2, point3));
                } else {
                    MaterialPoint point1 = previousMaterialPoints.get(i - 1);
                    MaterialPoint point2 = previousMaterialPoints.get(i);
                    MaterialPoint point3 = previousMaterialPoints.get(i + 1);
                    nextPoints[i] = new MaterialPoint(getAverage(point1, point2, point3));
                }
            }
        }

        return new TimePoint(nextPoints);
    }

    private Double getAverage(MaterialPoint point1, MaterialPoint point2, MaterialPoint point3) {

//        if (point1 == null || point2 == null || point3 == null) {
//        }
        return (point1.getTemperature() + point2.getTemperature() + point3.getTemperature()) / 3;
    }

    private Process getPrevious() {
        int current = parent.getProcesses().indexOf(this);

        if (current != 0) {
            return parent.getProcesses().get(current - 1);
        } else return null;
    }

    private Process getNext() {
        int current = parent.getProcesses().indexOf(this);

        if (current < parent.getProcesses().size() - 1) {
            return parent.getProcesses().get(current + 1);
        } else return null;
    }
}
