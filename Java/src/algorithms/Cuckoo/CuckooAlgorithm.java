package algorithms.Cuckoo;

import algorithms.Cuckoo.model.Cuckoo;
import main.LineChartEx;
import main.Parameters;

import java.util.Collections;
import java.util.List;

public class CuckooAlgorithm {

    private CuckooModelingInterface cuckooModelingInterface;
    private CuckooPlainOldData cuckooPlainOldData;
    private final LineChartEx lineChartEx;

    public CuckooAlgorithm(CuckooModelingInterface cuckooModelingInterface) {
        this.cuckooModelingInterface = cuckooModelingInterface;
        this.cuckooPlainOldData = cuckooModelingInterface.getData();
        lineChartEx = new LineChartEx();
    }

    public double execute() {
        cuckooPlainOldData.matureCuckoos.clear();
        for (int step = 0; step < Parameters.Cuckoo.MONTE_CARLO_STEP; step++) {
            for (int i = 0; i < Parameters.Cuckoo.POPULATION; i++) {
                cuckooPlainOldData.matureCuckoos.add(cuckooModelingInterface.generateInitialRandomCuckoos());
            }

            for (int i = 0; i < 10; i++) {
                for (Cuckoo matureCuckoo : cuckooPlainOldData.matureCuckoos) {
                    try {
                        cuckooPlainOldData.eggs.addAll(cuckooModelingInterface.generateEggs(matureCuckoo));
                    } catch (Exception ignored) {

                    }
                }
                for (Cuckoo cuckoo : cuckooPlainOldData.eggs) {
                    cuckoo.setCost(cuckooModelingInterface.calculateCost(cuckoo.getCuckooDataAndBehaviour()));
                }
                Collections.sort(cuckooPlainOldData.eggs, new CuckooComparator());

                int highSubListBound = cuckooPlainOldData.eggs.size() - (int) (cuckooPlainOldData.eggs.size() * Parameters.Cuckoo.EGG_KILLING_RATE);
                List<Cuckoo> cuckooList = cuckooPlainOldData.eggs.subList(0, highSubListBound);

                cuckooPlainOldData.matureCuckoos.addAll(cuckooList);
                cuckooPlainOldData.eggs.clear();

                if (cuckooPlainOldData.matureCuckoos.size() > Parameters.Cuckoo.MAX_CUCKOO_NUMBERS) {
                    Collections.sort(cuckooPlainOldData.matureCuckoos, new CuckooComparator());
                    cuckooPlainOldData.matureCuckoos = cuckooPlainOldData.matureCuckoos.subList(0, Parameters.Cuckoo.MAX_CUCKOO_NUMBERS);
                }

            }
        }

        return cuckooPlainOldData.matureCuckoos.get(0).getCost();
    }
}