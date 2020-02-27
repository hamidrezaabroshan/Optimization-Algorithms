package main;

import base_algorithms.Cost;
import base_algorithms.Cuckoo.CuckooAlgorithm;
import base_algorithms.Cuckoo.CuckooModelingInterface;
import base_algorithms.Cuckoo.CuckooPlainOldData;
import base_algorithms.quantum_annealing.QAAlgorithm;
import base_algorithms.quantum_annealing.QAModelingInterface;
import base_algorithms.quantum_annealing.QAPlainOldData;
import base_algorithms.simulated_annealing.SAAlgorithm;
import base_algorithms.simulated_annealing.SAModelingInterface;
import base_algorithms.simulated_annealing.SAPlainOldData;
import main.model.Graph;
import main.model.Vertex;
import problem_modelings.budget_constrained_lmax_optimization.algorithms.QABudgetConstrainedLmaxOptimizationModeling;
import problem_modelings.budget_constrained_lmax_optimization.algorithms.SABudgetConstrainedLmaxOptimizationModeling;
import problem_modelings.budget_constrained_lmax_optimization.algorithms.cuckoo.CuckooBudgetConstrainedLmaxOptimizationModeling;
import problem_modelings.budget_constrained_lmax_optimization.model_specifications.BudgetConstrainedLmaxOptimizationModelingPlainOldData;
import problem_modelings.cost_optimization.algorithms.QACostOptimizationModeling;
import problem_modelings.cost_optimization.algorithms.SACostOptimizationModeling;
import problem_modelings.cost_optimization.algorithms.cuckoo.CuckooCostOptimizationModeling;
import problem_modelings.cost_optimization.model_specifications.CostOptimizationModelingPlainOldData;

import java.util.*;

import static main.Utils.readObjectFromFile;

public class FactoryClient {

    private List<Vertex> candidateSinks = new ArrayList<>();            // AS
    private List<Vertex> candidateControllers = new ArrayList<>();      // AC
    private Graph graph;

    private int[][] controllerY;                    // YPrime (Y Number of Hops)
    private boolean[][] sinkYSpinVariables;         // SY (Y Spin Variable)
    private boolean[][] controllerYSpinVariables;   // SYPrime (Y Spin Variable)
    private int[][] distances;
    private int[][] sensorToSensorWorkload;         // w[sensors][sensors]

    public static void main(String[] args) throws Exception {
        FactoryClient client = new FactoryClient();

        if (Parameters.Common.MODEL_NO == ModelNoEnum.COST_OPTIMIZATION) {
            client.executeAlgorithmsOnFirstModel();
        } else if (Parameters.Common.MODEL_NO == ModelNoEnum.BUDGET_CONSTRAINED_LMAX_OPTIMIZATION) {
            client.executeAlgorithmsOnBudgetConstrainedModel();
        } else if (Parameters.Common.MODEL_NO == ModelNoEnum.BUDGET_CONSTRAINED_CONTROLLER_OVERHEAD) {
            client.executeAlgorithmsOnBudgetConstrainedModel();
        }
    }

    private void executeAlgorithmsOnBudgetConstrainedModel() throws Exception {
        LineChartEx chartEx = new LineChartEx();

        double cuckooPotentialEnergy = 0;
        double cuckoolMaxCost = 0;
        double cuckooSummationOfLMaxCost = 0;
        double cuckooSynchronizationOverheadCost = 0;
        double cuckooSynchronizationDelayCost = 0;
        double cuckooReliabilityCost = 0;
        double cuckooLoadBalancingCost = 0;
        double cuckooKineticEnergy = 0;
        double cuckooBudgetCostEnergy = 0;

        double qaPotentialEnergy = 0;
        double qalMaxCost = 0;
        double qaSummationOfLMaxCost = 0;
        double qaSynchronizationOverheadCost = 0;
        double qaSynchronizationDelayCost = 0;
        double qaReliabilityCost = 0;
        double qaLoadBalancingCost = 0;
        double qaKineticEnergy = 0;
        double qaBudgetCostEnergy = 0;

        double saPotentialEnergy = 0;
        double salMaxCost = 0;
        double saSummationOfLMaxCost = 0;
        double saSynchronizationOverheadCost = 0;
        double saSynchronizationDelayCost = 0;
        double saReliabilityCost = 0;
        double saLoadBalancingCost = 0;
        double saKineticEnergy = 0;
        double saBudgetCostEnergy = 0;

        retrieveVariablesFromFile();

        BudgetConstrainedLmaxOptimizationModelingPlainOldData budgetConstrainedLmaxOptimizationModelingPlainOldData = new BudgetConstrainedLmaxOptimizationModelingPlainOldData(
                graph,
                candidateControllers,
                controllerY,
                main.Parameters.Common.SENSOR_CONTROLLER_MAX_DISTANCE,
                main.Parameters.Common.MAX_CONTROLLER_COVERAGE,
                main.Parameters.Common.MAX_CONTROLLER_LOAD,
                main.Parameters.Common.COST_CONTROLLER,
                (candidateControllers.size() / 3) * Parameters.Common.COST_CONTROLLER,
                distances,
                sensorToSensorWorkload
        );

        // Cuckoo algorithm initialization
        CuckooPlainOldData cuckooPlainOldData = new CuckooPlainOldData();
        CuckooModelingInterface cuckooModelingInterface = new CuckooBudgetConstrainedLmaxOptimizationModeling(budgetConstrainedLmaxOptimizationModelingPlainOldData, cuckooPlainOldData);
        CuckooAlgorithm cuckooAlgorithm = new CuckooAlgorithm(cuckooModelingInterface);

        // QA algorithm initialization
        QAPlainOldData qaPlainOldData = new QAPlainOldData(
                main.Parameters.QuantumAnnealing.TROTTER_REPLICAS,
                main.Parameters.QuantumAnnealing.TEMPERATURE,
                main.Parameters.QuantumAnnealing.MONTE_CARLO_STEP,
                main.Parameters.QuantumAnnealing.TUNNELING_FIELD_INITIAL,
                main.Parameters.QuantumAnnealing.TUNNELING_FIELD_FINAL,
                main.Parameters.QuantumAnnealing.TUNNELING_FIELD_EVAPORATION
        );
        QAModelingInterface qaModelingInterface = new QABudgetConstrainedLmaxOptimizationModeling(budgetConstrainedLmaxOptimizationModelingPlainOldData, qaPlainOldData);
        QAAlgorithm qaAlgorithm = new QAAlgorithm(qaModelingInterface);

        // SA algorithm initialization
        SAPlainOldData saPlainOldData = new SAPlainOldData(
                Parameters.SimulatedAnnealing.TEMPERATURE_INITIAL,
                Parameters.SimulatedAnnealing.TEMPERATURE_FINAL,
                Parameters.SimulatedAnnealing.TEMPERATURE_COOLING_RATE,
                Parameters.SimulatedAnnealing.MONTE_CARLO_STEP
        );
        SAModelingInterface saModelingInterface = new SABudgetConstrainedLmaxOptimizationModeling(budgetConstrainedLmaxOptimizationModelingPlainOldData, saPlainOldData);
        SAAlgorithm saAlgorithm = new SAAlgorithm(saModelingInterface);

        // Cuckoo execution
        Date cuckooTimeA = new Date();
        for (int i = 0; i < main.Parameters.Common.SIMULATION_COUNT; i++) {
            Cost cuckooCost = cuckooAlgorithm.execute();

            cuckooPotentialEnergy += cuckooCost.getPotentialEnergy();
            cuckoolMaxCost += cuckooCost.getlMaxCost();
            cuckooSummationOfLMaxCost += cuckooCost.getSummationOfLMaxCost();
            cuckooSynchronizationOverheadCost += cuckooCost.getSynchronizationOverheadCost();
            cuckooSynchronizationDelayCost += cuckooCost.getSynchronizationDelayCost();
            cuckooReliabilityCost += cuckooCost.getReliabilityCost();
            cuckooLoadBalancingCost += cuckooCost.getLoadBalancingCost();
            cuckooKineticEnergy += cuckooCost.getKineticEnergy();
            cuckooBudgetCostEnergy += cuckooCost.getBudgetCostEnergy();

            chartEx.addToCuckooSeries(i + 1, cuckooCost.getPotentialEnergy());
            printResults(cuckooCost, OptimizationAlgorithmsEnum.CUCKOO);
        }
        Date cuckooTimeB = new Date();

        // QA execution
        Date quantumTimeA = new Date();
        for (int i = 0; i < main.Parameters.Common.SIMULATION_COUNT; i++) {
            Cost qaCost = qaAlgorithm.execute();

            qaPotentialEnergy += qaCost.getPotentialEnergy();
            qalMaxCost += qaCost.getlMaxCost();
            qaSummationOfLMaxCost += qaCost.getSummationOfLMaxCost();
            qaSynchronizationOverheadCost += qaCost.getSynchronizationOverheadCost();
            qaSynchronizationDelayCost += qaCost.getSynchronizationDelayCost();
            qaReliabilityCost += qaCost.getReliabilityCost();
            qaLoadBalancingCost += qaCost.getLoadBalancingCost();
            qaKineticEnergy += qaCost.getKineticEnergy();
            qaBudgetCostEnergy += qaCost.getBudgetCostEnergy();

            chartEx.addToQASeries(i + 1, qaCost.getPotentialEnergy());
            printResults(qaCost, OptimizationAlgorithmsEnum.QUANTUM_ANNEALING);
        }
        Date quantumTimeB = new Date();

        // SA execution
        Date simulatedTimeA = new Date();
        for (int i = 0; i < main.Parameters.Common.SIMULATION_COUNT; i++) {
            Cost saCost = saAlgorithm.execute();

            saPotentialEnergy += saCost.getPotentialEnergy();
            salMaxCost += saCost.getlMaxCost();
            saSummationOfLMaxCost += saCost.getSummationOfLMaxCost();
            saSynchronizationOverheadCost += saCost.getSynchronizationOverheadCost();
            saSynchronizationDelayCost += saCost.getSynchronizationDelayCost();
            saReliabilityCost += saCost.getReliabilityCost();
            saLoadBalancingCost += saCost.getLoadBalancingCost();
            saKineticEnergy += saCost.getKineticEnergy();
            saBudgetCostEnergy += saCost.getBudgetCostEnergy();

            chartEx.addToSASeries(i + 1, saCost.getPotentialEnergy());
            printResults(saCost, OptimizationAlgorithmsEnum.SIMULATED_ANNEALING);
        }
        Date simulatedTimeB = new Date();

        // Create results map
        Map<String, Map<String, Double>> algorithmResultsMap = new HashMap<>();

        Map<String, Double> cuckooMap = new HashMap<>();
        cuckooMap.put(Parameters.ResultInfoConstants.START_TIME, (double) cuckooTimeA.getTime());
        cuckooMap.put(Parameters.ResultInfoConstants.END_TIME, (double) cuckooTimeB.getTime());
        cuckooMap.put(Parameters.ResultInfoConstants.POTENTIAL_ENERGY, cuckooPotentialEnergy);
        cuckooMap.put(Parameters.ResultInfoConstants.KINETIC_ENERGY, cuckooKineticEnergy);
        cuckooMap.put(Parameters.ResultInfoConstants.LMAX, cuckoolMaxCost);
        cuckooMap.put(Parameters.ResultInfoConstants.SUMMATION_OF_LMAX, cuckooSummationOfLMaxCost);
        cuckooMap.put(Parameters.ResultInfoConstants.SYNC_OVERHEAD_COST, cuckooSynchronizationOverheadCost);
        cuckooMap.put(Parameters.ResultInfoConstants.SYNC_DELAY_COST, cuckooSynchronizationDelayCost);
        cuckooMap.put(Parameters.ResultInfoConstants.RELIABILITY_COST, cuckooReliabilityCost);
        cuckooMap.put(Parameters.ResultInfoConstants.LOAD_BALANCING_COST, cuckooLoadBalancingCost);
        cuckooMap.put(Parameters.ResultInfoConstants.BUDGET_COST_ENERGY, cuckooBudgetCostEnergy);

        algorithmResultsMap.put(OptimizationAlgorithmsEnum.CUCKOO.name(), cuckooMap);

        Map<String, Double> qaMap = new HashMap<>();
        qaMap.put(Parameters.ResultInfoConstants.START_TIME, (double) quantumTimeA.getTime());
        qaMap.put(Parameters.ResultInfoConstants.END_TIME, (double) quantumTimeB.getTime());
        qaMap.put(Parameters.ResultInfoConstants.POTENTIAL_ENERGY, qaPotentialEnergy);
        qaMap.put(Parameters.ResultInfoConstants.KINETIC_ENERGY, qaKineticEnergy);
        qaMap.put(Parameters.ResultInfoConstants.LMAX, qalMaxCost);
        qaMap.put(Parameters.ResultInfoConstants.SUMMATION_OF_LMAX, qaSummationOfLMaxCost);
        qaMap.put(Parameters.ResultInfoConstants.SYNC_OVERHEAD_COST, qaSynchronizationOverheadCost);
        qaMap.put(Parameters.ResultInfoConstants.SYNC_DELAY_COST, qaSynchronizationDelayCost);
        qaMap.put(Parameters.ResultInfoConstants.RELIABILITY_COST, qaReliabilityCost);
        qaMap.put(Parameters.ResultInfoConstants.LOAD_BALANCING_COST, qaLoadBalancingCost);
        qaMap.put(Parameters.ResultInfoConstants.BUDGET_COST_ENERGY, qaBudgetCostEnergy);

        algorithmResultsMap.put(OptimizationAlgorithmsEnum.QUANTUM_ANNEALING.name(), qaMap);

        Map<String, Double> saMap = new HashMap<>();
        saMap.put(Parameters.ResultInfoConstants.START_TIME, (double) simulatedTimeA.getTime());
        saMap.put(Parameters.ResultInfoConstants.END_TIME, (double) simulatedTimeB.getTime());
        saMap.put(Parameters.ResultInfoConstants.POTENTIAL_ENERGY, saPotentialEnergy);
        saMap.put(Parameters.ResultInfoConstants.KINETIC_ENERGY, saKineticEnergy);
        saMap.put(Parameters.ResultInfoConstants.LMAX, salMaxCost);
        saMap.put(Parameters.ResultInfoConstants.SUMMATION_OF_LMAX, saSummationOfLMaxCost);
        saMap.put(Parameters.ResultInfoConstants.SYNC_OVERHEAD_COST, saSynchronizationOverheadCost);
        saMap.put(Parameters.ResultInfoConstants.SYNC_DELAY_COST, saSynchronizationDelayCost);
        saMap.put(Parameters.ResultInfoConstants.RELIABILITY_COST, saReliabilityCost);
        saMap.put(Parameters.ResultInfoConstants.LOAD_BALANCING_COST, saLoadBalancingCost);
        saMap.put(Parameters.ResultInfoConstants.BUDGET_COST_ENERGY, saBudgetCostEnergy);

        algorithmResultsMap.put(OptimizationAlgorithmsEnum.SIMULATED_ANNEALING.name(), saMap);

        printResultsSummary(chartEx, algorithmResultsMap);
    }

    @SuppressWarnings("unused")
    private void executeAlgorithmsOnFirstModel() throws Exception {
        LineChartEx chartEx = new LineChartEx();
        double cuckooEnergySum = 0;
        double qaEnergySum = 0;
        double saEnergySum = 0;

        retrieveVariablesFromFile();

        CostOptimizationModelingPlainOldData costOptimizationModelingPlainOldData = new CostOptimizationModelingPlainOldData(
                graph,
                candidateSinks,
                candidateControllers,
                sinkYSpinVariables,
                controllerYSpinVariables,
                main.Parameters.Common.SENSOR_SINK_MAX_DISTANCE,
                main.Parameters.Common.SENSOR_CONTROLLER_MAX_DISTANCE,
                main.Parameters.Common.MAX_SINK_COVERAGE,
                main.Parameters.Common.MAX_CONTROLLER_COVERAGE,
                main.Parameters.Common.MAX_SINK_LOAD,
                main.Parameters.Common.MAX_CONTROLLER_LOAD,
                main.Parameters.Common.COST_SINK,
                main.Parameters.Common.COST_CONTROLLER,
                main.Parameters.Common.COST_REDUCTION_FACTOR
        );

        Date cuckooTimeA = new Date();

        CuckooPlainOldData cuckooPlainOldData = new CuckooPlainOldData();

        CuckooModelingInterface cuckooModelingInterface = new CuckooCostOptimizationModeling(costOptimizationModelingPlainOldData, cuckooPlainOldData);

        CuckooAlgorithm cuckooAlgorithm = new CuckooAlgorithm(cuckooModelingInterface);

        for (int i = 0; i < main.Parameters.Common.SIMULATION_COUNT; i++) {
            Cost cuckooPotentialEnergy = cuckooAlgorithm.execute();
            chartEx.addToCuckooSeries(i + 1, cuckooPotentialEnergy.getPotentialEnergy());
            cuckooEnergySum += cuckooPotentialEnergy.getPotentialEnergy();
            System.out.println("Cuckoo Energy: " + cuckooPotentialEnergy.getPotentialEnergy());
        }

        Date cuckooTimeB = new Date();

        Date quantumTimeA = new Date();

        QAPlainOldData qaPlainOldData = new QAPlainOldData(
                main.Parameters.QuantumAnnealing.TROTTER_REPLICAS,
                main.Parameters.QuantumAnnealing.TEMPERATURE,
                main.Parameters.QuantumAnnealing.MONTE_CARLO_STEP,
                main.Parameters.QuantumAnnealing.TUNNELING_FIELD_INITIAL,
                main.Parameters.QuantumAnnealing.TUNNELING_FIELD_FINAL,
                main.Parameters.QuantumAnnealing.TUNNELING_FIELD_EVAPORATION
        );

        QAModelingInterface qaModelingInterface = new QACostOptimizationModeling(
                costOptimizationModelingPlainOldData,
                qaPlainOldData
        );

        QAAlgorithm qaAlgorithm = new QAAlgorithm(qaModelingInterface);

        for (int i = 0; i < main.Parameters.Common.SIMULATION_COUNT; i++) {
            Cost qaCost = qaAlgorithm.execute();
            chartEx.addToQASeries(i + 1, qaCost.getPotentialEnergy());
            qaEnergySum += qaCost.getPotentialEnergy();
            System.out.println("QA Energy: " + qaCost.getPotentialEnergy());
        }

        Date quantumTimeB = new Date();

        Date simulatedTimeA = new Date();

        SAPlainOldData saPlainOldData = new SAPlainOldData(
                Parameters.SimulatedAnnealing.TEMPERATURE_INITIAL,
                Parameters.SimulatedAnnealing.TEMPERATURE_FINAL,
                Parameters.SimulatedAnnealing.TEMPERATURE_COOLING_RATE,
                Parameters.SimulatedAnnealing.MONTE_CARLO_STEP
        );

        SAModelingInterface saModelingInterface = new SACostOptimizationModeling(
                costOptimizationModelingPlainOldData,
                saPlainOldData
        );


        SAAlgorithm saAlgorithm = new SAAlgorithm(saModelingInterface);

        for (int i = 0; i < main.Parameters.Common.SIMULATION_COUNT; i++) {
            Cost saExecuteResult = saAlgorithm.execute();
            chartEx.addToSASeries(i + 1, saExecuteResult.getPotentialEnergy());
            saEnergySum += saExecuteResult.getPotentialEnergy();
            System.out.println("SA Energy: " + saExecuteResult.getPotentialEnergy());
        }


        Date simulatedTimeB = new Date();

        // TODO: Print Results Using printResultsSummary Method
    }

    @SuppressWarnings("unchecked")
    private void retrieveVariablesFromFile() {
        if (Parameters.Common.MODEL_NO == ModelNoEnum.COST_OPTIMIZATION) {
            graph = (Graph) readObjectFromFile(Utils.FILE_NAME_GRAPH);
            candidateSinks = (List<Vertex>) readObjectFromFile(Utils.FILE_NAME_CANDIDATE_SINKS);
            candidateControllers = (List<Vertex>) readObjectFromFile(Utils.FILE_NAME_CANDIDATE_CONTROLLERS);
            sinkYSpinVariables = (boolean[][]) readObjectFromFile(Utils.FILE_NAME_SINK_Y_SPIN_VARIABLES);
            controllerYSpinVariables = (boolean[][]) readObjectFromFile(Utils.FILE_NAME_CONTROLLER_Y_SPIN_VARIABLES);
            distances = (int[][]) readObjectFromFile(Utils.FILE_NAME_DISTANCES);
        } else if (Parameters.Common.MODEL_NO == ModelNoEnum.BUDGET_CONSTRAINED_LMAX_OPTIMIZATION || Parameters.Common.MODEL_NO == ModelNoEnum.BUDGET_CONSTRAINED_CONTROLLER_OVERHEAD) {
            graph = (Graph) readObjectFromFile(Utils.FILE_NAME_GRAPH);
            candidateControllers = (List<Vertex>) readObjectFromFile(Utils.FILE_NAME_CANDIDATE_CONTROLLERS);
            controllerY = (int[][]) readObjectFromFile(Utils.FILE_NAME_CONTROLLER_Y);
            controllerY = (int[][]) readObjectFromFile(Utils.FILE_NAME_CONTROLLER_Y);
            distances = (int[][]) readObjectFromFile(Utils.FILE_NAME_DISTANCES);
            sensorToSensorWorkload = (int[][]) readObjectFromFile(Utils.FILE_NAME_SENSOR_TO_SENSOR_WORKLOAD);
        }
    }

    private void printResultsSummary(LineChartEx chartEx, Map<String, Map<String, Double>> algorithmResultInfo) {
        chartEx.drawChart();
        System.out.println();

        List<OptimizationAlgorithmsEnum> optimizationAlgorithmsEnums = Arrays.asList(OptimizationAlgorithmsEnum.values());

        for (OptimizationAlgorithmsEnum optimizationAlgorithm : optimizationAlgorithmsEnums) {
            Map<String, Double> algorithmInfoPair = algorithmResultInfo.get(optimizationAlgorithm.name());
            System.out.println(optimizationAlgorithm.name() + " average potential energy is: " + algorithmInfoPair.get(Parameters.ResultInfoConstants.POTENTIAL_ENERGY) / Parameters.Common.SIMULATION_COUNT);
            System.out.println(optimizationAlgorithm.name() + " average kinetic energy is: " + algorithmInfoPair.get(Parameters.ResultInfoConstants.KINETIC_ENERGY) / Parameters.Common.SIMULATION_COUNT);
            System.out.println(optimizationAlgorithm.name() + " average lmax is: " + algorithmInfoPair.get(Parameters.ResultInfoConstants.LMAX) / Parameters.Common.SIMULATION_COUNT);
            System.out.println(optimizationAlgorithm.name() + " average summation of lmax is: " + algorithmInfoPair.get(Parameters.ResultInfoConstants.SUMMATION_OF_LMAX) / Parameters.Common.SIMULATION_COUNT);
            System.out.println(optimizationAlgorithm.name() + " average sync overhead is: " + algorithmInfoPair.get(Parameters.ResultInfoConstants.SYNC_OVERHEAD_COST) / Parameters.Common.SIMULATION_COUNT);
            System.out.println(optimizationAlgorithm.name() + " average sync delay is: " + algorithmInfoPair.get(Parameters.ResultInfoConstants.SYNC_DELAY_COST) / Parameters.Common.SIMULATION_COUNT);
            System.out.println(optimizationAlgorithm.name() + " average reliability cost is: " + algorithmInfoPair.get(Parameters.ResultInfoConstants.RELIABILITY_COST) / Parameters.Common.SIMULATION_COUNT);
            System.out.println(optimizationAlgorithm.name() + " average load balancing cost is: " + algorithmInfoPair.get(Parameters.ResultInfoConstants.LOAD_BALANCING_COST) / Parameters.Common.SIMULATION_COUNT);
            System.out.println(optimizationAlgorithm.name() + " average budget cost is: " + algorithmInfoPair.get(Parameters.ResultInfoConstants.BUDGET_COST_ENERGY) / Parameters.Common.SIMULATION_COUNT);
            System.out.println(optimizationAlgorithm.name() + " average time is: " + (algorithmInfoPair.get(Parameters.ResultInfoConstants.END_TIME) - algorithmInfoPair.get(Parameters.ResultInfoConstants.START_TIME)) / main.Parameters.Common.SIMULATION_COUNT);
            System.out.println();
        }
    }

    private void printResults(Cost cost, OptimizationAlgorithmsEnum optimizationAlgorithm) throws Exception {
        System.out.println(optimizationAlgorithm.name() + " Total Cost: " + cost.getPotentialEnergy());
        System.out.println(optimizationAlgorithm.name() + " LMax: " + cost.getlMaxCost());
        System.out.println(optimizationAlgorithm.name() + " Summation of LMax Cost: " + cost.getSummationOfLMaxCost());
        System.out.println(optimizationAlgorithm.name() + " Sync Overhead Cost: " + cost.getSynchronizationOverheadCost());
        System.out.println(optimizationAlgorithm.name() + " Sync Delay Cost: " + cost.getSynchronizationDelayCost());
        System.out.println(optimizationAlgorithm.name() + " Reliability Cost: " + cost.getReliabilityCost());
        System.out.println(optimizationAlgorithm.name() + " Load Cost: " + cost.getLoadBalancingCost());
        System.out.println(optimizationAlgorithm.name() + " Kinetic Cost: " + cost.getKineticEnergy());
        System.out.println(optimizationAlgorithm.name() + " Budget Cost: " + cost.getBudgetCostEnergy());
    }
}