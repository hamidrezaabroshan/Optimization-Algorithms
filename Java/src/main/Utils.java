package main;

import java.io.*;
import java.util.LinkedList;
import java.util.List;

import main.dijkstra.DijkstraAlgorithm;
import main.model.Graph;
import main.model.Vertex;

public class Utils implements
        problem_modelings.budget_constrained_modeling.Utils,
        problem_modelings.first_modeling.Utils {

    public static String FILE_NAME_GRAPH = "Graph";
    public static String FILE_NAME_CANDIDATE_SINKS = "CandidateSinks";
    public static String FILE_NAME_CANDIDATE_CONTROLLERS = "CandidateControllers";
    public static String FILE_NAME_SINK_Y = "SinkYS";
    public static String FILE_NAME_SINK_Y_SPIN_VARIABLES = "SinkYSpinVariables";
    public static String FILE_NAME_CONTROLLER_Y = "ControllerY";
    public static String FILE_NAME_CONTROLLER_Y_SPIN_VARIABLES = "ControllerYSpinVariables";

    public static int getDistance(Graph graph, int firstNodeIndex, int secondNodeIndex) {
        DijkstraAlgorithm dijkstra = new DijkstraAlgorithm(graph);
        dijkstra.execute(graph.getVertexes().get(firstNodeIndex));
        LinkedList<Vertex> path = dijkstra.getPath(graph.getVertexes().get(secondNodeIndex));
        return (path == null) ? 0 : path.size() - 1;
    }

    public static boolean isDistanceFavorable(Graph graph, int firstNodeIndex, int secondNodeIndex, int maxDistance) {
        return getDistance(graph, firstNodeIndex, secondNodeIndex) <= maxDistance;
    }

    public static void initializeSpinVariables(
            Graph graph,
            List<Vertex> candidateSinks,
            List<Vertex> candidateControllers,
            int sensorSinkMaxDistance,
            int sensorControllerMaxDistance,
            int[][] sinkY, boolean[][] sinkYSpinVariables,
            int[][] controllerY, boolean[][] controllerYSpinVariables) {
        // --- Initialize Y and YPrime Spin Variables
        for (int i = 0; i < graph.getVertexes().size(); i++) {
            for (int j = 0; j < candidateSinks.size(); j++) {
                // The following line can be replaced with vertexIndex = i - but I preferred to write this in the following way for more readability
                int vertexIndex1 = graph.getVertexIndexById(graph.getVertexes().get(i).getId());
                int vertexIndex2 = graph.getVertexIndexById(candidateSinks.get(j).getId());
                sinkY[i][j] = getDistance(graph, vertexIndex1, vertexIndex2);
                sinkYSpinVariables[i][j] = Utils.isDistanceFavorable(graph, vertexIndex1, vertexIndex2, sensorSinkMaxDistance);
            }
            for (int j = 0; j < candidateControllers.size(); j++) {
                // The following line can be replaced with vertexIndex = i - but I preferred to write this in the following way for more readability
                int vertexIndex1 = graph.getVertexIndexById(graph.getVertexes().get(i).getId());
                int vertexIndex2 = graph.getVertexIndexById(candidateControllers.get(j).getId());
                controllerY[i][j] = getDistance(graph, vertexIndex1, vertexIndex2);
                controllerYSpinVariables[i][j] = Utils.isDistanceFavorable(graph, vertexIndex1, vertexIndex2, sensorControllerMaxDistance);
            }
        }
        // ---
    }

    public static void writeObjectToFile(Object object, String fileName) {
        try {
            File f = new File(fileName + ".txt");
            FileOutputStream fos;
            fos = new FileOutputStream(f);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(object);
            oos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Object readObjectFromFile(String fileName) {
        Object object = null;
        try {
            File f = new File(fileName + ".txt");
            FileInputStream fis = new FileInputStream(f);
            ObjectInputStream ois = new ObjectInputStream(fis);
            object = ois.readObject();
            ois.close();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return object;
    }
}
