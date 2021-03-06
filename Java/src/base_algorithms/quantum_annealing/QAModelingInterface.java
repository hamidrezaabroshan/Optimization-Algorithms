package base_algorithms.quantum_annealing;

import base_algorithms.Cost;
import javafx.util.Pair;

public interface QAModelingInterface {

    void resetDynamicVariables();

    void generateReplicasOfSolutions();

    void generateInitialSpinVariablesAndEnergy() throws Exception;

    void getAReplica(int replicaNumber);

    void generateNeighbor();

    double getKineticEnergy(int currentReplicaNum);

    Cost calculateCost(int currentReplicaNum);

    double calculateEnergyFromCost(Cost energyPair) throws Exception;

    void acceptSolution();

    void printGeneratedSolution();

    QAPlainOldData getData();
}
