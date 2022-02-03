package com.cup;

import org.apache.commons.math3.linear.RealMatrix;

import java.util.*;

public class IteratedLocalSearch {
    private final Random random;
    private double bestKnow;
    private ArrayList<Integer> bestWalk;
    private double temperature;
    private RealMatrix distanceMatrix;
    private int dimension;

    public IteratedLocalSearch(long seed) {
        this.random = new Random();
        this.random.setSeed(seed);
    }

    private void initializeMatrices(String title) {
        Setup setup = new Setup("AI_cup_2021_problems/" + title);
        setup.setupMatrix();
        this.dimension = setup.getDimension();
        this.distanceMatrix = setup.getDistanceMatrix();

    }

    private void generateRandomSolutionDummy() {
        this.bestWalk = new ArrayList<>(dimension);
        for (int i = 0; i < dimension; i++) {
            bestWalk.add(i);
        }
        bestWalk.add(0);
    }

    private void generateRandomSolutionWithNearestNeighbour() {

        var i = random.nextInt(0, dimension);
        bestWalk = new ArrayList<>(dimension);
        bestWalk.add(i);
        int curr = i;
        while (bestWalk.size() < dimension) {
            var candidate = this.distanceMatrix.getRow(curr);
            double minWeight = Integer.MAX_VALUE;
            int next = -1;
            for (int j = 0; j < dimension; j++) {
                if (minWeight > candidate[j] && !bestWalk.contains(j)) {
                    minWeight = candidate[j];
                    next = j;
                }
            }
            bestWalk.add(next);
            curr = next;
        }
        bestWalk.add(i);
    }

    public static void main(String[] args) {
        IteratedLocalSearch it = new IteratedLocalSearch(18L);
        it.execute("ch130.tsp");
    }

    public double execute(String title) {
        initializeMatrices(title);
        generateRandomSolutionWithNearestNeighbour();
        setTemperature();
        long start = System.currentTimeMillis();
        long end = start + 180 * 1000;
        long finish = 0;
        while (start + finish < end) {
            var begin = System.currentTimeMillis();
            var B = fourOpt(bestWalk);
            var A = localOpt(B);
            var delta = getCostPath(A) - getCostPath(bestWalk);
            if (delta <= 0) {
                bestWalk = A;
            } else if (random.nextDouble(0, 1) < Math.exp(-delta / temperature)) {
                bestWalk = A;
            }
            temperature = temperature * 0.95;
            finish = System.currentTimeMillis() - begin;
            start += finish;
        }
        return getCostPath(bestWalk);

    }

    private double getCostPath(ArrayList<Integer> walk) {
        var i = walk.size();
        double toRet = 0;
        for (int j = 0; j < i - 1; j++) {
            toRet += this.distanceMatrix.getEntry(walk.get(j), walk.get(j + 1));
        }
        return toRet;
    }

    private ArrayList<Integer> localOpt(ArrayList<Integer> sol) {
        int size = dimension + 1;
        double bestGain;
        double imp = 1;
        int best_i = 0;
        int best_j = 0;
        while (imp != 0) {
            imp = 0;
            for (int i = 1; i < size - 2; i++) {
                bestGain = 0;
                for (int j = i + 1; j < size - 1; j++) {
                    var gain = this.distanceMatrix.getEntry(sol.get(i), sol.get(i - 1)) + this.distanceMatrix.getEntry(sol.get(j + 1), sol.get(j)) -
                            this.distanceMatrix.getEntry(sol.get(i), sol.get(j + 1)) - this.distanceMatrix.getEntry(sol.get(i - 1), sol.get(j));
                    if (gain > bestGain) {
                        bestGain = gain;
                        best_i = i;
                        best_j = j;
                    }
                }
                if (bestGain > 0) {
                    sol = swap(sol, best_i, best_j);
                    imp = 1;
                }
            }
        }
        return sol;
    }

    private ArrayList<Integer> fourOpt(ArrayList<Integer> sol) {
        var len = dimension + 1;
        var toRet = new ArrayList<Integer>(len);
        int a = random.nextInt(1, (len / 4));
        int b = a + random.nextInt(1, (len / 4));
        int c = b + random.nextInt(1, (len / 4));
        for (int i = 0; i < a; i++) {
            toRet.add(sol.get(i));
        }
        for (int i = c; i < len - 1; i++) {
            toRet.add(sol.get(i));
        }
        for (int i = b; i < c; i++) {
            toRet.add(sol.get(i));
        }
        for (int i = a; i < b; i++) {
            toRet.add(sol.get(i));
        }
        toRet.add(sol.get(0));
        return toRet;


    }

    public ArrayList<Integer> swap(ArrayList<Integer> list, int i, int j) {
        ArrayList<Integer> newList = new ArrayList<>();
        int size = list.size();
        for (int k = 0; k <= i - 1; k++) {
            newList.add(list.get(k));
        }

        int invert = 0;
        for (int k = i; k <= j; k++) {
            newList.add(list.get(j - invert));
            invert += 1;
        }

        for (int k = j + 1; k < size; k++) {
            newList.add(list.get(k));
        }
        return newList;
    }

    public void setTemperature() {
        int ALPHA = 100;
        this.temperature = 1.5D * ALPHA * getCostPath(bestWalk) / Math.sqrt(dimension);
    }

    public boolean containsDuplicates(ArrayList<Integer> walk) {
        for (int i = 0; i < walk.size(); i++) {
            for (int j = i + 1; j < walk.size(); j++) {
                if (Objects.equals(walk.get(i), walk.get(j))) {
                    if (!(i == 0 && j == walk.size() - 1)) {
                        System.out.println("duplicate found at " + i + " " + j + " with values " + walk.get(i) + " " + walk.get(j));
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
