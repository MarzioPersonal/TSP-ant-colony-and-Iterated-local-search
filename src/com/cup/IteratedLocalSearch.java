package com.cup;


import java.util.*;

public class IteratedLocalSearch {
    private final Random random;
    private double bestKnow;
    private int[] bestWalk;
    private double temperature;
    private double[] distanceMatrix;
    private int dimension;

    public IteratedLocalSearch(long seed) {
        this.random = new Random();
        this.random.setSeed(seed);
    }

    private void initializeMatrices(String title) {
        Setup setup = new Setup("problems/" + title);
        setup.setupMatrix();
        this.dimension = setup.getDimension();
        this.distanceMatrix = setup.getDistanceMatrix();
        this.bestKnow = setup.getBestKnown();
    }

    private void generateRandomSolutionDummy() {
        this.bestWalk = new int[dimension+1];
        for (int i = 0; i < dimension; i++) {
            bestWalk[i] = i;
        }
        bestWalk[dimension] = 0;
    }

    private boolean contains(int[] array, int value, int pos) {
        for(int i= 0; i < pos; i++) {
            if(array[i] == value) {
                return true;
            }
        }
        return false;
    }

    private void generateRandomSolutionWithNearestNeighbour() {
        bestWalk = new int[dimension+1];
        int i = random.nextInt(0, dimension);
        bestWalk[0] = i;
        int curr = i;
        int pos=1;
        while (pos < dimension) {
            double minWeight = Integer.MAX_VALUE;
            int next = -1;
            for (int j = 0; j < dimension; j++) {
                if (minWeight > this.distanceMatrix[curr*dimension+j] && !contains(bestWalk, j, pos)) {
                    minWeight = this.distanceMatrix[curr*dimension+j];
                    next = j;
                }
            }
            bestWalk[pos] = next;
            curr = next;
            pos+=1;
        }
        bestWalk[dimension] = i;
    }

    public static void main(String[] args) {
        IteratedLocalSearch it = new IteratedLocalSearch(5906447272222300000L);
        it.execute("u1060.tsp");
    }

    public void execute(String title) {
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
            var c = getCostPath(bestWalk);
            var delta = getCostPath(A) - c;
            if (delta <= 0) {
                bestWalk = A;
            } else if (random.nextDouble(0, 1) < Math.exp(-delta / temperature)) {
                bestWalk = A;
            }
            temperature *= 0.96d;
            finish = System.currentTimeMillis() - begin;
            start += finish;
        }
        System.out.println((getCostPath(bestWalk) - bestKnow) / bestKnow);
    }

    private double getCostPath(int[] walk) {
        double toRet = 0;
        for (int j = 0; j < dimension - 1; j++) {
            toRet += this.distanceMatrix[walk[j]*dimension+ walk[j + 1]];
        }
        return toRet;
    }

    private int[] localOpt(int[] sol) {
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
                    var gain = this.distanceMatrix[sol[i]*dimension+sol[i -1]] +
                            this.distanceMatrix[sol[j + 1]*dimension+ sol[j]] -
                            this.distanceMatrix[sol[i] *dimension+ sol[(j + 1)]] -
                            this.distanceMatrix[sol[i - 1]*dimension+ sol[j]];
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

    private int[] fourOpt(int[] sol) {
        var len = dimension + 1;
        var toRet = new int[len];
        int a = random.nextInt(1, (len / 4));
        int b = a + random.nextInt(1, (len / 4));
        int c = b + random.nextInt(1, (len / 4));
        int position = 0;
        for (int i = 0; i < a; i++) {
            toRet[position++] = sol[i];
        }
        for (int i = c; i < len - 1; i++) {
            toRet[position++] = sol[i];
        }
        for (int i = b; i < c; i++) {
            toRet[position++] = sol[i];
        }
        for (int i = a; i < b; i++) {
            toRet[position++] = sol[i];
        }
        toRet[position] = sol[0];
        return toRet;


    }

    public int[] swap(int[] list, int i, int j) {
        int[] newArr = new int[dimension+1];
        if (i - 1 + 1 >= 0) System.arraycopy(list, 0, newArr, 0, i - 1 + 1);

        int invert = 0;
        for (int k = i; k <= j; k++) {
            newArr[k] = list[j - invert];
            invert += 1;
        }

        if (dimension + 1 - (j + 1) >= 0) System.arraycopy(list, j + 1, newArr, j + 1, dimension + 1 - (j + 1));
        return newArr;
    }

    public void setTemperature() {
        int ALPHA = 100;
        this.temperature = 1.5D * ALPHA * getCostPath(bestWalk) / Math.sqrt(dimension);
    }

//    public boolean containsDuplicates(ArrayList<Integer> walk) {
//        for (int i = 0; i < walk.size(); i++) {
//            for (int j = i + 1; j < walk.size(); j++) {
//                if (Objects.equals(walk.get(i), walk.get(j))) {
//                    if (!(i == 0 && j == walk.size() - 1)) {
//                        System.out.println("duplicate found at " + i + " " + j + " with values " + walk.get(i) + " " + walk.get(j));
//                        return true;
//                    }
//                }
//            }
//        }
//        return false;
//    }
}
