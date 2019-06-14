package quantum.anealing;

import main.model.Vertex;
import main.model.Graph;
import java.util.List;

public class TestQuantumAnnealingAlgorithm {

    private static final float COST_REDUCTION_FACTOR = 0.75f;
    private static final int TROTTER_REPLICAS = 50;     // P
    private static final float TEMPERATURE = 100f;         // T
    private static final int MONTE_CARLO_STEP = 50;   // M
    private static final float TUNNELING_FIELD_INITIAL = 1f;
    private static final float TUNNELING_FIELD_FINAL = .5f;
    private static final float TUNNELING_FIELD_EVAPORATION = .75f;

    QuantumAnnealing qa;

    public TestQuantumAnnealingAlgorithm(
            Graph graph,
            List<Vertex> candidateSinks,
            List<Vertex> candidateControllers,
            boolean[][] sinkYSpinVariables,
            boolean[][] controllerYSpinVariables,
            int sensorSinkMaxDistance,
            int sensorControllerMaxDistance,
            int maxSinkCoverage,
            int maxControllerCoverage,
            int maxSinkLoad,
            int maxControllerLoad,
            int costSink,
            int costController) {
        qa = new QuantumAnnealing(
                graph,
                candidateSinks,
                candidateControllers,
                sinkYSpinVariables,
                controllerYSpinVariables,
                sensorSinkMaxDistance,
                sensorControllerMaxDistance,
                maxSinkCoverage,
                maxControllerCoverage,
                maxSinkLoad,
                maxControllerLoad,
                costSink,
                costController,
                COST_REDUCTION_FACTOR,
                TROTTER_REPLICAS,
                TEMPERATURE,
                MONTE_CARLO_STEP,
                TUNNELING_FIELD_INITIAL,
                TUNNELING_FIELD_FINAL,
                TUNNELING_FIELD_EVAPORATION
        );
    }

    public double execute() {
        return qa.execute();
    }
}
