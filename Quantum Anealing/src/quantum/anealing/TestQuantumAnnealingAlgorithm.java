package quantum.anealing;

import quantum.anealing.graph.Vertex;
import quantum.anealing.graph.Graph;
import quantum.anealing.graph.Edge;
import java.util.ArrayList;
import java.util.List;

public class TestQuantumAnnealingAlgorithm {

    private static final int VERTICES_COUNT = 10;
    private static final int SENSOR_SINK_MAX_DISTANCE = 5;              // Lmax
    private static final int SENSOR_CONTROLLER_MAX_DISTANCE = 5;        // LPrimeMax

    private static final List<Vertex> nodes = new ArrayList<>();        // V
    private static final List<Edge> edges = new ArrayList<>();          // E
    private List<Vertex> candidateSinks = new ArrayList<>();            // AS
    private List<Vertex> candidateControllers = new ArrayList<>();      //AC

    public static void main(String[] args) {
        TestQuantumAnnealingAlgorithm qaTest = new TestQuantumAnnealingAlgorithm();

        Graph graph = qaTest.initialize();
        QuantumAnealing qa = new QuantumAnealing(
                graph,
                qaTest.candidateSinks,
                qaTest.candidateControllers,
                SENSOR_SINK_MAX_DISTANCE,
                SENSOR_CONTROLLER_MAX_DISTANCE
        );
    }

    private Graph initialize() {
        Graph graph = initializeGraph();
        candidateSinks = graph.getVertexes();
        candidateControllers = graph.getVertexes();
        return graph;
    }
    
    private Graph initializeGraph() {

        for (int i = 0; i < VERTICES_COUNT + 1; i++) {
            Vertex location = new Vertex("Node_" + i, "Node_" + i);
            nodes.add(location);
        }

        addLane("Edge_0", 0, 1);
        addLane("Edge_1", 0, 2);
        addLane("Edge_2", 0, 4);
        addLane("Edge_3", 2, 6);
        addLane("Edge_4", 2, 7);
        addLane("Edge_5", 3, 7);
        addLane("Edge_6", 5, 8);
        addLane("Edge_7", 8, 9);
        addLane("Edge_8", 7, 9);
        addLane("Edge_9", 4, 9);
        addLane("Edge_10", 9, 10);
        addLane("Edge_11", 1, 10);

        Graph graph = new Graph(nodes, edges);
        return graph;
    }

    private void addLane(String laneId, int sourceLocNo, int destLocNo) {
        Edge lane1 = new Edge(laneId, nodes.get(sourceLocNo), nodes.get(destLocNo), 1);
        edges.add(lane1);
        Edge lane2 = new Edge(laneId, nodes.get(destLocNo), nodes.get(sourceLocNo), 1);
        edges.add(lane2);
    }

    private void addLane(String laneId, int sourceLocNo, int destLocNo, int duration) {
        Edge lane = new Edge(laneId, nodes.get(sourceLocNo), nodes.get(destLocNo), duration);
        edges.add(lane);
    }
}
