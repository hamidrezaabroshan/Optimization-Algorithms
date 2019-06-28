package main;

public class Parameters {

    public static class QuantumAnnealing {
        public static final int TROTTER_REPLICAS = 100;             // P
        public static final float TEMPERATURE = 200f;               // T
        public static final int MONTE_CARLO_STEP = 110;             // M
        public static final float TUNNELING_FIELD_INITIAL = 1f;
        public static final float TUNNELING_FIELD_FINAL = .5f;
        public static final float TUNNELING_FIELD_EVAPORATION = .9f;
    }

    public static class SimulatedAnnealing {
        public static final float TEMPERATURE_INITIAL = 100;                // T Initial
        public static final float TEMPERATURE_FINAL = 1;                    // T Final
        public static final float TEMPERATURE_COOLING_RATE = .75f;          // T Cooling Rate
        public static final int MONTE_CARLO_STEP = 50;                      // M
    }

    public static class Cuckoo {
        public static final int POPULATION = 400;      // Npop
        public static final int MONTE_CARLO_STEP = 5;
        public static final int MAX_EGG_NUMBER = 201;
        public static final int MIN_EGG_NUMBER = 150;
        public static final double EGG_KILLING_RATE = .001;
        public static final int MAX_CUCKOO_NUMBERS = 10000;
    }

    public static class Common {
        public static final float COST_REDUCTION_FACTOR = 0.75f;
        public static final int SINK_LOAD = 10;                            // w
        public static final int CONTROLLER_LOAD = 10;                      // wPrime
        public static final int SENSOR_SINK_MAX_DISTANCE = 3;              // Lmax
        public static final int SENSOR_CONTROLLER_MAX_DISTANCE = 2;        // LPrimeMax
        public static final int GRAPH_SIZE = 4;
        public static final int SIMULATION_COUNT = 20;
        public static final int MAX_SINK_COVERAGE = 6;                      // k
        public static final int MAX_CONTROLLER_COVERAGE = 6;                // kPrime
        public static final int MAX_SINK_LOAD = 30;                         // W
        public static final int MAX_CONTROLLER_LOAD = 30;                   // WPrime
        public static final int COST_SINK = 1;
        public static final int COST_CONTROLLER = 3;
    }
}