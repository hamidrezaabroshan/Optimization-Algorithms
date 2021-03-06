package problem_modelings.base.plain_old_data;

import main.model.Graph;
import main.model.Vertex;

import java.util.List;

public abstract class BaseMultiControllerMultiSinkProblemModelingPlainOldData extends BaseProblemModelingPlainOldData {

    // Problem Specifications
    public List<Vertex> candidateSinks;             // AS
    public List<Vertex> candidateControllers;       // AC

    public int sensorSinkMaxDistance;               // Lmax
    public int sensorControllerMaxDistance;         // LPrimeMax

    public boolean[][] sinkYSpinVariables;          // SY (Y Spin Variable)
    public boolean[][] controllerYSpinVariables;    // SYPrime (Y Spin Variable)

    // Solution Spin Variables
    public boolean[] sinkXSpinVariables;            // SX (X Spin Variable)
    public boolean[] controllerXSpinVariables;      // SXPrime (X Spin Variable)

    // Temp Spin Variables
    public boolean[] tempSinkXSpinVariables;        // SX (X Spin Variable)
    public boolean[] tempControllerXSpinVariables;  // SXPrime (X Spin Variable)

    public int maxSinkCoverage;                     // K
    public int maxControllerCoverage;               // KPrime
    public int maxSinkLoad;                         // W
    public int maxControllerLoad;                   // WPrime
    public int costSink;
    public int costController;
    public float costReductionFactor;

    public BaseMultiControllerMultiSinkProblemModelingPlainOldData(
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
            int costController,
            float costReductionFactor
    ) {
        super(graph);
        this.candidateSinks = candidateSinks;
        this.candidateControllers = candidateControllers;
        this.sensorSinkMaxDistance = sensorSinkMaxDistance;
        this.sensorControllerMaxDistance = sensorControllerMaxDistance;
        this.sinkYSpinVariables = sinkYSpinVariables;
        this.controllerYSpinVariables = controllerYSpinVariables;
        this.sinkXSpinVariables = new boolean[candidateSinks.size()];
        this.controllerXSpinVariables = new boolean[candidateControllers.size()];
        this.tempSinkXSpinVariables = new boolean[candidateSinks.size()];
        this.tempControllerXSpinVariables = new boolean[candidateControllers.size()];
        this.maxSinkCoverage = maxSinkCoverage;
        this.maxControllerCoverage = maxControllerCoverage;
        this.maxSinkLoad = maxSinkLoad;
        this.maxControllerLoad = maxControllerLoad;
        this.costSink = costSink;
        this.costController = costController;
        this.costReductionFactor = costReductionFactor;
    }
}
