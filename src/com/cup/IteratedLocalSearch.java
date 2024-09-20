package com.cup;


import java.io.IOException;
import java.util.*;

public class IteratedLocalSearch {
    private final Random random;
    private int bestKnown;
    private int[] bestWalk;
    private int[] overallBestWalk;
    private int overallBestWalkCost = Integer.MAX_VALUE;
    private double temperature;
    private int[] distanceMatrix;
    private int dimension;

    public IteratedLocalSearch(long seed) {
        this.random = new Random();
        this.random.setSeed(seed);
    }

    private void initializeMatrices(String title) throws IOException {
        Setup setup = new Setup("problems/" + title);
        setup.setupMatrix();
        this.dimension = setup.getDimension();
        this.distanceMatrix = setup.getDistanceMatrix();
        this.bestKnown = setup.getBestKnown();
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
            int minWeight = Integer.MAX_VALUE;
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

    public static void main(String[] args) throws IOException {
        IteratedLocalSearch it = new IteratedLocalSearch(555);
        it.execute("u1060.tsp");
    }

    public void execute(String title) throws IOException {
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
            var Acost = getCostPath(A);
            var delta = Acost - c;
            if (delta <= 0) {
                bestWalk = A;
                if (Acost < overallBestWalkCost) {
                    overallBestWalkCost = Acost;
                    overallBestWalk = Arrays.copyOf(bestWalk, bestWalk.length);
                }
            } else if (random.nextDouble(0, 1) < Math.exp(-delta / temperature)) {
                bestWalk = A;
            }

            temperature *= 0.98d;
            finish = System.currentTimeMillis() - begin;
            start += finish;
        }
        System.out.println(getCostPath(overallBestWalk));
        System.out.println(percentageChange(overallBestWalk));
    }

    private double percentageChange(int[] x) {
        double nominator = (getCostPath(x) - bestKnown);
        return nominator / bestKnown * 100.;
    }

    private int getCostPath(int[] walk) {
        int toRet = 0;
        for (int j = 0; j < dimension - 1; j++) {
            toRet += this.distanceMatrix[walk[j]*dimension+ walk[j + 1]];
        }
        return toRet;
    }

    private int[] localOpt(int[] sol) {
        int size = dimension + 1;
        int bestGain = 1;
        int best_i = 0;
        int best_j = 0;
        while (bestGain > 0) {
            bestGain = 0;
            for (int i = 1; i < size - 2; i++) {
                for (int j = i + 1; j < size - 1; j++) {
                    int gain = this.distanceMatrix[sol[i-1] * dimension + sol[i]] +
                            this.distanceMatrix[sol[j] * dimension + sol[j + 1]] -
                            this.distanceMatrix[sol[i-1] * dimension + sol[j]] -
                            this.distanceMatrix[sol[i] * dimension + sol[j + 1]];

                    if (gain > bestGain) {
                        bestGain = gain;
                        best_i = i;
                        best_j = j;
                    }
                }
            }
            if (bestGain > 0) {
                sol = swap(sol, best_i, best_j);
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
        // Reverse the segment between i and j
        while (i < j) {
            int temp = list[i];
            list[i] = list[j];
            list[j] = temp;
            i++;
            j--;
        }
        return list;
    }

    public void setTemperature() {
        double ALPHA = 100.D;
        this.temperature = 1.5D * ALPHA * getCostPath(bestWalk) / Math.sqrt(dimension);
    }

}
