package quantum.anealing;

import quantum.anealing.graph.Vertex;
import quantum.anealing.graph.Graph;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import javafx.util.Pair;
import quantum.anealing.dijkstra.DijkstraAlgorithm;

public class QuantumAnealing {

    // Problem Specifications
    private final Graph graph;

    private final List<Vertex> candidateSinks;            // AS
    private final List<Vertex> candidateControllers;      //AC

    private final int sensorSinkMaxDistance;              // Lmax
    private final int sensorControllerMaxDistance;        // LPrimeMax

    private final boolean[][] sinkYSpinVariables;           // SY (Y Spin Variable)
    private final boolean[][] controllerYSpinVariables;     // SYPrime (Y Spin Variable)

    // Solution Spin Variables
    private boolean[] sinkXSpinVariables;             // SX (X Spin Variable)
    private boolean[] controllerXSpinVariables;       // SXPrime (X Spin Variable)
    private final boolean[][] replicasOfSinkXSpinVariables;
    private final boolean[][] replicasOfControllerXSpinVariables;

    // Temp Spin Variables
    private boolean[] tempSinkXSpinVariables;           // SX (X Spin Variable)           
    private boolean[] tempControllerXSpinVariables;     // SXPrime (X Spin Variable)

    private final int maxSinkCoverage;          // K
    private final int maxControllerCoverage;    // KPrime
    private final int maxSinkLoad;          // W
    private final int maxControllerLoad;    // WPrime
    private final int costSink;
    private final int costController;
    private final float costReductionFactor;
    private final int trotterReplicas;   // P
    private float temperature;       // T
    private final int monteCarloSteps;
    private float tunnlingField;
    private final float tunnlingFiledFinal;
    private final float tunnlingFiledEvaporation;
    private final float coolingRate = 1;

    private Pair<Double, Double> prevEnergyPair;

    public QuantumAnealing(
            Graph graph,
            List candidateSinks,
            List candidateControllers,
            int sensorSinkMaxDistance,
            int sensorControllerMaxDistance,
            int maxSinkCovrage,
            int maxControllerCoverage,
            int maxSinkLoad,
            int maxControllerLoad,
            int costSink,
            int costController,
            float costReductionFactor,
            int trotterReplicas,
            float temperature,
            int monteCarloSteps,
            float tunnlingFieldInitial,
            float tunnlingFieldFinal,
            float tunnlingFieldEvaporation
    ) {
        this.controllerYSpinVariables = new boolean[graph.getVertexes().size()][candidateControllers.size()];
        this.sinkYSpinVariables = new boolean[graph.getVertexes().size()][candidateSinks.size()];
        this.tempControllerXSpinVariables = new boolean[candidateControllers.size()];
        this.tempSinkXSpinVariables = new boolean[candidateSinks.size()];
        this.sinkXSpinVariables = new boolean[candidateSinks.size()];
        this.controllerXSpinVariables = new boolean[candidateControllers.size()];
        this.replicasOfSinkXSpinVariables = new boolean[trotterReplicas][candidateSinks.size()];
        this.replicasOfControllerXSpinVariables = new boolean[trotterReplicas][candidateControllers.size()];

        this.graph = graph;
        this.candidateSinks = candidateSinks;
        this.candidateControllers = candidateControllers;
        this.sensorSinkMaxDistance = sensorSinkMaxDistance;
        this.sensorControllerMaxDistance = sensorControllerMaxDistance;

        this.maxSinkCoverage = maxSinkCovrage;
        this.maxControllerCoverage = maxControllerCoverage;
        this.maxSinkLoad = maxSinkLoad;
        this.maxControllerLoad = maxControllerLoad;
        this.costSink = costSink;
        this.costController = costController;
        this.costReductionFactor = costReductionFactor;
        this.trotterReplicas = trotterReplicas;
        this.temperature = temperature;
        this.monteCarloSteps = monteCarloSteps;
        this.tunnlingField = tunnlingFieldInitial;
        this.tunnlingFiledFinal = tunnlingFieldFinal;
        this.tunnlingFiledEvaporation = tunnlingFieldEvaporation;

        initializeSpinVariables();

        printProblemSpecifications();
    }

    private void printProblemSpecifications() {
        // Print graph
        graph.getVertexes().stream().forEach((vertex) -> {
            System.out.println("Vertex: " + vertex.toString());
        });

        System.out.println();

        graph.getEdges().stream().forEach((edge) -> {
            System.out.println("Edge: " + edge.toString());
        });

        System.out.println();

        // Print candidate sinks
        System.out.print("Candidate sink vertexes are: ");
        candidateSinks.stream().forEach((candidateSinkVertex) -> {
            System.out.print(candidateSinkVertex.toString() + ", ");
        });

        System.out.println();
        System.out.println();

        // Print candidate controllers
        System.out.print("Candidate controller vertexes are: ");
        candidateControllers.stream().forEach((candidateControllerVertex) -> {
            System.out.print(candidateControllerVertex.toString() + ", ");
        });

        System.out.println();
        System.out.println();

        System.out.println("Sink Y: ");

        for (int i = 0; i < graph.getVertexes().size(); i++) {
            for (int j = 0; j < candidateSinks.size(); j++) {
                System.out.print(sinkYSpinVariables[i][j] + ", ");
            }
            System.out.println();
        }

        System.out.println();
        System.out.println();

        System.out.println("Controller Y: ");

        for (int i = 0; i < graph.getVertexes().size(); i++) {
            for (int j = 0; j < candidateControllers.size(); j++) {
                System.out.print(controllerYSpinVariables[i][j] + ", ");
            }
            System.out.println();
        }
    }

    void execute() {
        // Genreate replicas (Fill replicasOfSinkXSpinVariables, replicasOfControllerXSpinVariables )
        generateReplicasOfSolutions();
        generateInitialSpinVariablesAndEnergy();

        int counter = 0;
        Pair<Double, Double> minEnergyPair = new Pair<>(Double.MAX_VALUE, Double.MAX_VALUE);
        // Do while tunnlig field is favorable
        do {
            // For each replica
            for (int ro = 0; ro < trotterReplicas; ro++) {
                tempSinkXSpinVariables = replicasOfSinkXSpinVariables[ro].clone();
                tempControllerXSpinVariables = replicasOfControllerXSpinVariables[ro].clone();
                //  For each montecarlo step
                for (int step = 0; step < monteCarloSteps; step++) {
                    counter++;
                    // Generate neighbor
                    generateNeighbour();
                    // Calculate energy of temp solution
                    Pair<Double, Double> energyPair = calculateEnergy(ro);
                    double energy = calculateEnergyFromPair(energyPair);
                    double prevEnergy = calculateEnergyFromPair(prevEnergyPair);
                    double minEnergy = calculateEnergyFromPair(minEnergyPair);
                    if (energy < minEnergy) {
                        minEnergyPair = energyPair;
                    }
                    if (energy < prevEnergy) {
                        // If energy has decreased: accept solution
                        prevEnergyPair = energyPair;
                        sinkXSpinVariables = tempSinkXSpinVariables.clone();
                        controllerXSpinVariables = tempControllerXSpinVariables.clone();
                    } else {
                        // Else with given probability decide to accept or not   
                        double baseProb = Math.exp((prevEnergy - energy) / temperature);
                        System.out.println("BaseProp " + baseProb);
                        double rand = Math.random();
                        if (rand < baseProb) {
                            prevEnergyPair = energyPair;
                            sinkXSpinVariables = tempSinkXSpinVariables.clone();
                            controllerXSpinVariables = tempControllerXSpinVariables.clone();
                        }
                    }
                    LineChartEx.addToSelectedEnergy(
                            counter,
                            calculateEnergyFromPair(prevEnergyPair),
                            energy,
                            calculateEnergyFromPair(minEnergyPair),
                            4
                    );
                    System.out.println("counter " + counter);
                    System.out.println("Selected Energy is " + calculateEnergyFromPair(prevEnergyPair));
                } // End of for
            } // End of for
            // Update tunnling field
            tunnlingField *= tunnlingFiledEvaporation;
            temperature *= coolingRate;
        } while (tunnlingField > tunnlingFiledFinal); // End of do while 

        // Final solution is in: sinkXSpinVariables and controllerXSpinVariables
        System.out.println("Counter: " + counter);
        System.out.println("Accepted Energy: " + calculateEnergyFromPair(prevEnergyPair));
        System.out.println("Min Energy: " + calculateEnergyFromPair(minEnergyPair));
        System.out.println("Final Temperature: " + temperature);
        LineChartEx.drawChart();
    }

    private void generateInitialSpinVariablesAndEnergy() {
        // --- Initialize temp lists to false
        for (int i = 0; i < candidateControllers.size(); i++) {
            controllerXSpinVariables[i] = false;
        }

        for (int i = 0; i < candidateSinks.size(); i++) {
            sinkXSpinVariables[i] = false;
        }

        tempControllerXSpinVariables = controllerXSpinVariables.clone();
        tempSinkXSpinVariables = sinkXSpinVariables.clone();
        Pair<Double, Double> energyPair = calculateEnergy(-1);
        prevEnergyPair = energyPair;
    }

    private int getDistance(int firstNodeIndex, int secondeNodeIndex) {
        DijkstraAlgorithm dijkstra = new DijkstraAlgorithm(graph);
        dijkstra.execute(graph.getVertexes().get(firstNodeIndex));
        LinkedList<Vertex> path = dijkstra.getPath(graph.getVertexes().get(secondeNodeIndex));
        return (path == null) ? 0 : path.size() - 1;
    }

    private boolean isDistanceFavorable(int firstNodeIndex, int secondNodeIndex, int maxDistance) {
        return getDistance(firstNodeIndex, secondNodeIndex) <= maxDistance;
    }

    private void printGeneratedSolution() {
        // --- Print temp lists
        System.out.println();
        System.out.println("Temp Sink X: ");
        for (int i = 0; i < tempSinkXSpinVariables.length; i++) {
            System.out.print(tempSinkXSpinVariables[i] + ", ");
        }

        System.out.println();
        System.out.println("Temp Controller X: ");
        for (int i = 0; i < tempControllerXSpinVariables.length; i++) {
            System.out.print(tempControllerXSpinVariables[i] + ", ");
        }

        System.out.println();
        System.out.println();
        // ---

    }

    private void generateNeighbour() {
        Random random = new Random();
        int randInt = random.nextInt(tempSinkXSpinVariables.length + tempControllerXSpinVariables.length);

        if (randInt < tempSinkXSpinVariables.length) {
            // Change randInt-th item in sink array
            boolean prevValue = tempSinkXSpinVariables[randInt];
            tempSinkXSpinVariables[randInt] = !prevValue;
        } else {
            // Change index-th item in controller array
            int index = randInt - (tempSinkXSpinVariables.length - 1) - 1;
            boolean prevValue = tempControllerXSpinVariables[index];
            tempControllerXSpinVariables[index] = !prevValue;
        }
        printGeneratedSolution();
    }

    private Pair<Double, Double> calculateEnergy(int currentReplicaNum) {
        int reliabilityEnergy = getReliabilityEnergy();
        double loadBalancingEnergy = getLoadBalancingEnergy();
        double costEnergy = getCostEnergy();
        double potentialEnergy = reliabilityEnergy + loadBalancingEnergy + costEnergy;
        double kineticEnergy = getKineticEnergy(currentReplicaNum);
        double energy = kineticEnergy + potentialEnergy;

        System.out.println("Reliability: " + reliabilityEnergy);
        System.out.println("Load Balancing: " + loadBalancingEnergy);
        System.out.println("Cost: " + costEnergy);
        System.out.println("Potential Cost: " + potentialEnergy);
        System.out.println("Energy Cost: " + energy);

        return new Pair<>(potentialEnergy, kineticEnergy);
    }

    private double getKineticEnergy(int currentReplicaNum) {
        if (currentReplicaNum + 1 >= trotterReplicas || currentReplicaNum < 0) {
            return 0;
        }

        // Calculate coupling among replicas
        float halfTemperature = temperature / 2;
        float angle = tunnlingField / (trotterReplicas * temperature);

        double coupling = -halfTemperature * Math.log(Math.tanh(angle));

        int sinkReplicaCoupling = 0;
        int controllerReplicaCoupling = 0;

        for (int i = 0; i < candidateSinks.size(); i++) {
            boolean areSpinVariablesTheSame = (replicasOfSinkXSpinVariables[currentReplicaNum][i] && replicasOfSinkXSpinVariables[currentReplicaNum + 1][i]);
            sinkReplicaCoupling = areSpinVariablesTheSame ? 1 : -1;
        }

        for (int i = 0; i < candidateControllers.size(); i++) {
            boolean areSpinVariablesTheSame
                    = (replicasOfControllerXSpinVariables[currentReplicaNum][i]
                    && replicasOfControllerXSpinVariables[currentReplicaNum + 1][i]);
            controllerReplicaCoupling = areSpinVariablesTheSame ? 1 : -1;
        }

        // Multiply sum of two final results with coupling
        return coupling * (sinkReplicaCoupling + controllerReplicaCoupling);
    }

    private int getReliabilityEnergy() {
        int sensorNumbers = getSensorsCount();
        return (maxSinkCoverage * sensorNumbers - totalCoverSinksScore())
                + (maxControllerCoverage * sensorNumbers - totalCoverControllersScore());
    }

    private float getLoadBalancingEnergy() {
        float sinksLoadBalancingEnergy = getSinksLoadBalancingEnergy();
        float controllersLoadBalancingEnergy = getControllersLoadBalancingEnergy();
        return sinksLoadBalancingEnergy + controllersLoadBalancingEnergy;
    }

    private float getCostEnergy() {
        for (int i = 0; i < candidateSinks.size(); i++) {
            for (int j = 0; j < candidateControllers.size(); j++) {
                if (tempSinkXSpinVariables[i] && tempControllerXSpinVariables[j]) {
                    float cost = (costSink + costController) * costReductionFactor;
                    return cost;
                } else if (tempSinkXSpinVariables[i]) {
                    return costSink;
                } else if (tempControllerXSpinVariables[j]) {
                    return costController;
                }
            }
        }
        return 0;
    }

    private int getSensorsCount() {
        int sensorCount = 0;
        for (int i = 0; i < graph.getVertexes().size(); i++) {
            if (!isNodeSelectedAsSinkOrController(graph.getVertexes().get(i).getId())) {
                sensorCount++;
            }
        }
        return sensorCount;
    }

    private int totalCoverSinksScore() {
        int score = 0;
        for (int i = 0; i < graph.getVertexes().size(); i++) {
            if (!isNodeSelectedAsSinkOrController(graph.getVertexes().get(i).getId())) {
                score += Math.min(maxSinkCoverage, coveredSinksCountByNode(i));
            }
        }
        return score;
    }

    private int totalCoverControllersScore() {
        int score = 0;
        for (int i = 0; i < graph.getVertexes().size(); i++) {
            if (!isNodeSelectedAsSinkOrController(graph.getVertexes().get(i).getId())) {
                score += Math.min(maxControllerCoverage, coveredControllersCountByNode(i));
            }
        }
        return score;
    }

    private int coveredSinksCountByNode(int nodeIndex) {
        int coveredSinks = 0;
        for (int j = 0; j < candidateSinks.size(); j++) {
            coveredSinks += (sinkYSpinVariables[nodeIndex][j] && tempSinkXSpinVariables[j]) ? 1 : 0;
        }
        return coveredSinks;
    }

    private int coveredNodesCountBySink(int sinkIndex) {
        int coveredSinks = 0;
        for (int j = 0; j < graph.getVertexes().size(); j++) {
            coveredSinks += (sinkYSpinVariables[j][sinkIndex] && tempSinkXSpinVariables[sinkIndex]) ? 1 : 0;
        }
        return coveredSinks;
    }

    private int coveredNodesCountByController(int controllerIndex) {
        int coveredControllers = 0;
        for (int j = 0; j < graph.getVertexes().size(); j++) {
            coveredControllers += (controllerYSpinVariables[j][controllerIndex]
                    && tempControllerXSpinVariables[controllerIndex]) ? 1 : 0;
        }
        return coveredControllers;
    }

    private int coveredControllersCountByNode(int nodeIndex) {
        int coveredControllers = 0;
        for (int j = 0; j < candidateControllers.size(); j++) {
            coveredControllers += (controllerYSpinVariables[nodeIndex][j] && tempControllerXSpinVariables[j]) ? 1 : 0;
        }
        return coveredControllers;
    }

    private void initializeSpinVariables() {
        // --- Initialize Y and YPrime Spin Variables
        for (int i = 0; i < graph.getVertexes().size(); i++) {
            for (int j = 0; j < candidateSinks.size(); j++) {
                sinkYSpinVariables[i][j] = false;
            }
        }

        for (int i = 0; i < graph.getVertexes().size(); i++) {
            for (int j = 0; j < candidateControllers.size(); j++) {
                controllerYSpinVariables[i][j] = false;
            }
        }

        for (int i = 0; i < graph.getVertexes().size(); i++) {
            for (int j = 0; j < candidateSinks.size(); j++) {
                // The following line can be replaced with vertexIndex = i - but I prefered to write this in the following way for more readability
                int vertexIndex1 = graph.getVertexIndexById(graph.getVertexes().get(i).getId());
                int vertexIndex2 = graph.getVertexIndexById(((Vertex) candidateSinks.get(j)).getId());
                sinkYSpinVariables[i][j] = isDistanceFavorable(vertexIndex1, vertexIndex2, sensorSinkMaxDistance);
            }
        }

        for (int i = 0; i < graph.getVertexes().size(); i++) {
            for (int j = 0; j < candidateControllers.size(); j++) {
                // The following line can be replaced with vertexIndex = i - but I prefered to write this in the following way for more readability
                int vertexIndex1 = graph.getVertexIndexById(graph.getVertexes().get(i).getId());
                int vertexIndex2 = graph.getVertexIndexById(((Vertex) candidateControllers.get(j)).getId());
                controllerYSpinVariables[i][j] = isDistanceFavorable(vertexIndex1, vertexIndex2, sensorControllerMaxDistance);
            }
        }
        // ---
    }

    private boolean isNodeSelectedAsSinkOrController(String id) {
        boolean isSinkOrController = false;
        for (int i = 0; i < tempSinkXSpinVariables.length; i++) {
            boolean tempSinkXSpinVariable = tempSinkXSpinVariables[i];
            if (tempSinkXSpinVariable) {
                String sinkId = candidateSinks.get(i).getId();
                if (sinkId.equals(id)) {
                    return true;
                }
            }
        }

        for (int i = 0; i < tempControllerXSpinVariables.length; i++) {
            boolean tempControllerXSpinVariable = tempControllerXSpinVariables[i];
            if (tempControllerXSpinVariable) {
                String sinkId = candidateControllers.get(i).getId();
                if (sinkId.equals(id)) {
                    return true;
                }
            }
        }
        return isSinkOrController;
    }

    private float getSinksLoadBalancingEnergy() {
        float totalSinkLoadEnergy = 0;
        for (int j = 0; j < candidateSinks.size(); j++) {
            float totalLoadToJthSink = calculateLoadToJthSink(j);
            float bestSinkLoad = maxSinkLoad / (maxSinkCoverage - 1);
            totalSinkLoadEnergy += Math.max(0, totalLoadToJthSink - bestSinkLoad);
        }
        return totalSinkLoadEnergy;
    }

    private float getControllersLoadBalancingEnergy() {
        float totalControllerLoadEnergy = 0;
        for (int j = 0; j < candidateControllers.size(); j++) {
            float totalLoadToJthController = calculateLoadToJthController(j);
            float bestControllerLoad = maxControllerLoad / (maxControllerCoverage - 1);
            totalControllerLoadEnergy += Math.max(0, totalLoadToJthController - bestControllerLoad);
        }
        return totalControllerLoadEnergy;
    }

    // j is sink's index in candidateSinks (Not graph node index)
    private float calculateLoadToJthSink(int j) {
        float totalLoadToJthSink = 0;
        for (int i = 0; i < graph.getVertexes().size(); i++) {
            Vertex graphNode = graph.getVertexes().get(i);
            if (!isNodeSelectedAsSinkOrController(graphNode.getId())) {
                boolean condition = sinkYSpinVariables[i][j] && tempSinkXSpinVariables[j];
                if (condition) {
                    totalLoadToJthSink += (float) graphNode.getSinkLoad() / coveredSinksCountByNode(i);
                }
            }
        }
        return totalLoadToJthSink;
    }

    // j is controller's index in candidateControllers (Not graph node index)
    private float calculateLoadToJthController(int j) {
        float totalLoadToJthController = 0;
        for (int i = 0; i < graph.getVertexes().size(); i++) {
            Vertex graphNode = graph.getVertexes().get(i);
            if (!isNodeSelectedAsSinkOrController(graphNode.getId())) {
                boolean condition = controllerYSpinVariables[i][j] && tempControllerXSpinVariables[j];
                if (condition) {
                    totalLoadToJthController += (float) graphNode.getControllerLoad() / coveredControllersCountByNode(i);
                }
            }
        }
        return totalLoadToJthController;
    }

    private void generateReplicasOfSolutions() {
        for (int i = 0; i < trotterReplicas; i++) {
            // --- Select random configuration for replicas
            for (int j = 0; j < candidateSinks.size(); j++) {
                double probabilityOfOne = Math.random();
                replicasOfSinkXSpinVariables[i][j] = probabilityOfOne < .5;
            }
            for (int j = 0; j < candidateSinks.size(); j++) {
                double probabilityOfOne = Math.random();
                replicasOfControllerXSpinVariables[i][j] = probabilityOfOne < .5;
            }
        }
    }

    private double calculateEnergyFromPair(Pair<Double, Double> energyPair) {
        return energyPair.getKey() + energyPair.getValue();
    }
}
