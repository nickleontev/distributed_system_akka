package parallel;

import java.io.Serializable;
import java.util.List;

public class TemperaturesPOJO implements Serializable {
    final List<List<Double>> listOfTemperatures;

    public TemperaturesPOJO(List<List<Double>> listOfTemperatures) {
        this.listOfTemperatures = listOfTemperatures;
    }

    public List<List<Double>> getListOfTemperatures() {
        return listOfTemperatures;
    }
}
