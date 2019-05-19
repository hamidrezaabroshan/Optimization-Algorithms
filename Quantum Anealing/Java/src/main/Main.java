package main;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import javafx.util.Pair;
import quantum.anealing.TestQuantumAnnealingAlgorithm;
import main.model.Edge;
import main.model.Graph;
import main.model.Vertex;
import simulated.anealing.TestSimulatedAnnealingAlgorithm;

public class Main {

    public static final boolean DO_PRINT_INSTANCES = false;
    public static final boolean DO_PRINT_STEPS = false;

    private static final int SIMULATIONS_COUNT = 20;
    private static final int SINK_LOAD = 10;   // w
    private static final int CONTROLLER_LOAD = 10;      // wPrime
    private static final int SENSOR_SINK_MAX_DISTANCE = 3;              // Lmax
    private static final int SENSOR_CONTROLLER_MAX_DISTANCE = 2;        // LPrimeMax
    private static final int MAX_SINK_COVERAGE = 6;             // k
    private static final int MAX_CONTROLLER_COVERAGE = 6;       // kPrime
    private static final int MAX_SINK_LOAD = 30;        // W
    private static final int MAX_CONTROLLER_LOAD = 30;  // WPrime
    private static final int COST_SINK = 1;
    private static final int COST_CONTROLLER = 3;

    private static final List<Vertex> nodes = new ArrayList<>();        // V
    private static final List<Edge> edges = new ArrayList<>();          // E
    private final List<Vertex> candidateSinks = new ArrayList<>();            // AS
    private final List<Vertex> candidateControllers = new ArrayList<>();      // AC

    public static void main(String[] args) {
        System.out.println("---------- Graph Size = 1 ----------");
        execute(1);
        System.out.println("---------- Graph Size = 2 ----------");
        execute(2);
        System.out.println("---------- Graph Size = 3 ----------");
        execute(3);
        System.out.println("---------- Graph Size = 4 ----------");
        execute(4);
    }

    private static Pair<Double, Double> execute(int graphSize) {

        Main m = new Main();
        Graph graph = m.initialize(graphSize);
        LineChartEx chartEx = new LineChartEx();
        double qaEnergySum = 0;
        double saEnergySum = 0;

        TestQuantumAnnealingAlgorithm qaTest = new TestQuantumAnnealingAlgorithm(
                graph,
                m.candidateSinks,
                m.candidateControllers,
                SENSOR_SINK_MAX_DISTANCE,
                SENSOR_CONTROLLER_MAX_DISTANCE,
                MAX_SINK_COVERAGE,
                MAX_CONTROLLER_COVERAGE,
                MAX_SINK_LOAD,
                MAX_CONTROLLER_LOAD,
                COST_SINK,
                COST_CONTROLLER);

        for (int i = 0; i < SIMULATIONS_COUNT; i++) {
            double qaPotentialEnergy = qaTest.execute();
            chartEx.addToQASeries(i + 1, qaPotentialEnergy);
            qaEnergySum += qaPotentialEnergy;
            System.out.println("QA Energy: " + qaPotentialEnergy);
        }

        TestSimulatedAnnealingAlgorithm saTest = new TestSimulatedAnnealingAlgorithm(
                graph,
                m.candidateSinks,
                m.candidateControllers,
                SENSOR_SINK_MAX_DISTANCE,
                SENSOR_CONTROLLER_MAX_DISTANCE,
                MAX_SINK_COVERAGE,
                MAX_CONTROLLER_COVERAGE,
                MAX_SINK_LOAD,
                MAX_CONTROLLER_LOAD,
                COST_SINK,
                COST_CONTROLLER);

        for (int i = 0; i < SIMULATIONS_COUNT; i++) {
            double saPotentialEnergy = saTest.execute();
            chartEx.addToSASeries(i + 1, saPotentialEnergy);
            saEnergySum += saPotentialEnergy;
            System.out.println("SA Energy: " + saPotentialEnergy);
        }

        chartEx.drawChart();
        double finalQAResult = qaEnergySum / SIMULATIONS_COUNT;
        double finalSAResult = saEnergySum / SIMULATIONS_COUNT;

        System.out.println("QA average potential energy is: " + finalQAResult);
        System.out.println("SA average potential energy is: " + finalSAResult);

        return new Pair<>(finalQAResult, finalSAResult);
    }

    public Graph initializeGraph(int graphSize) {
        int vertexCount = 0;
        int candidateSinksNumber;
        int candidateControllersNumber;
        ArrayList<Pair<String, Pair<Integer, Integer>>> edgesPairList = new ArrayList<>();
        if (graphSize == 1) {
            // Candidate Sink = 4 (20/5)
            // Candidate Controller = 2 (2/10)
            vertexCount = 20;
        } else if (graphSize == 2) {
            // Candidate Sink = 8 (40/5)
            // Candidate Controller = 4 (40/10)
            vertexCount = 40;
        } else if (graphSize == 3) {
            // Candidate Sink = 16 (80/5)
            // Candidate Controller = 8 (80/10)
            vertexCount = 80;
        } else if (graphSize == 4) {
            // Candidate Sink = 32 (160/5)
            // Candidate Controller = 16 (160/10)
            vertexCount = 160;
        }

        candidateSinksNumber = vertexCount / 5;
        candidateControllersNumber = vertexCount / 5;

        for (int i = 0; i < vertexCount; i++) {
            int ithNodeNeighborsCount = vertexCount / 2;
            Set<Integer> neighborsNumberSet = new HashSet<>();

            while (neighborsNumberSet.size() < ithNodeNeighborsCount) {
                int nextInt = new Random().nextInt(vertexCount);
                if (nextInt != i) {
                    neighborsNumberSet.add(nextInt);
                }
            }

            for (int neighborNumber : neighborsNumberSet) {
                edgesPairList.add(new Pair<>("Edge_" + i + "_To_" + neighborNumber, new Pair<>(i, neighborNumber)));
            }
        }

        for (int i = 0; i < vertexCount; i++) {
            Vertex location = new Vertex("Node_" + i, "Node_" + i, SINK_LOAD, CONTROLLER_LOAD);
            nodes.add(location);
        }

        edgesPairList.stream().forEach((edge) -> {
            addLane(edge.getKey(), edge.getValue().getKey(), edge.getValue().getValue());
        });

        Set<Integer> candidateSinksNumberSet = new HashSet<>();
        Set<Integer> candidateControllerNumberSet = new HashSet<>();

        while (candidateSinksNumberSet.size() < candidateSinksNumber) {
            int nextInt = new Random().nextInt(vertexCount);
            candidateSinksNumberSet.add(nextInt);
        }

        while (candidateControllerNumberSet.size() < candidateControllersNumber) {
            int nextInt = new Random().nextInt(vertexCount);
            candidateControllerNumberSet.add(nextInt);
        }

        candidateControllerNumberSet.stream().forEach((candidateControllerNumber) -> {
            candidateControllers.add(nodes.get(candidateControllerNumber));
        });

        candidateSinksNumberSet.stream().forEach((candidateSinkNumber) -> {
            candidateSinks.add(nodes.get(candidateSinkNumber));
        });

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

    private Graph initialize(int graphSize) {
        Graph graph = initializeGraph(graphSize);
        return graph;
    }
}