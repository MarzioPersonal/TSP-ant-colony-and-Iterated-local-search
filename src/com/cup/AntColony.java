package com.cup;

import java.io.IOException;
import java.util.*;

public class AntColony {
    private final static int numberOfAnts = 10;
    private final static int beta = 2;
    private final static double alpha = 0.1d;
    private final static double rho = 0.1d;
    private final static double q0 = 0.9;

    private final Random random;

    public int[] bestWalk;
    public int bestLength = 0;

    private int[] iterationWalk;
    private int iterationLength = Integer.MAX_VALUE;

    int[] distanceMatrix;
    double[] invertedDistanceMatrix;
    double[] pheromones;
    private double tau0;
    private int dimension;

    double bestKnown;

    public AntColony(long seed) {
        this.random = new Random(seed);
    }

    public static void main(String[] args) throws IOException {
        System.out.println("Running Ant colony");
        AntColony antColony = new AntColony(12L);
        System.out.println("Setting up colony");
        antColony.setupColony("u1060.tsp");
        System.out.println("Running...");
        antColony.runAntColony();
    }

    public void runAntColony() {
        var start = System.currentTimeMillis();
        var elapsed = 0L;
        var end = start + (180*1000);
        while (start + elapsed < end) {
            var t1 = System.currentTimeMillis();
            iterationLength = Integer.MAX_VALUE;
            for (int i = 0; i < numberOfAnts; i++) {
                int initialPosition = random.nextInt(0, dimension);
                ant(initialPosition);
            }
            var a = twoOpt(iterationWalk);
            var len = getLength(a);
            if (len < bestLength) {
                bestWalk = a;
                bestLength = len;
            }
            for (int i = 0; i < dimension; i++) {
                var s = bestWalk[i];
                var e = bestWalk[i + 1];
                this.pheromones[s*dimension + e] = this.pheromones[s*dimension+e] * (1.0D - alpha) + alpha * (1.0D / bestLength);
            }
            var t2 = System.currentTimeMillis();
            elapsed = (t2 - t1);
            start += elapsed;
        }
        System.out.println(bestLength);
        System.out.println(percentageChange(bestLength));
    }

    private double percentageChange(int x) {
        double nominator = (x - bestKnown);
        return nominator / bestKnown * 100.;
    }

    private int[] twoOpt(int[] sol) {
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

    private void ant(int initialPosition) {
        var solution = new int[dimension + 1];
        int solutionLength = 0;
        int position =1;
        solution[0] = initialPosition;
        int pos = initialPosition;
        while (position < dimension) {
            var argmax = new double[dimension];
            double sum = 0;
            for (int i = 0; i < dimension; i++) {
                double x = this.pheromones[pos*dimension+ i] * Math.pow(this.invertedDistanceMatrix[pos*dimension+ i], beta);
                argmax[i] = x;
                sum += x;
            }
            for(int i =0; i < position; i++){
                var x = argmax[solution[i]];
                sum -= x;
                argmax[solution[i]] = 0.d;
            }
            int next = 0;
            double r = random.nextDouble(0, 1);
            if (r <= q0) {
                double max = -1;
                for (int i = 0; i < dimension; i++) {
                    if (max < argmax[i]) {
                        max = argmax[i];
                        next = i;
                    }
                }
            } else {
                double[] prob = new double[dimension];
                double sumIt = 0;
                double probMax = -1;
                int probMax_i = -1;
                for (int i = 0; i < dimension; i++) {
                    double x = argmax[i] / sum;
                    prob[i] = x;
                    if (probMax < x) {
                        probMax = x;
                        probMax_i = i;
                    }
                    sumIt += x;
                }
                try {
                    sumIt -= probMax;
                    if (sumIt <= 0) {
                        next = probMax_i;
                    } else {
                        prob[probMax_i] = 0;
                        next = RouletteWheel(prob, sumIt);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    System.exit(1);
                }
            }
            solutionLength += this.distanceMatrix[pos*dimension+ next];
            solution[position++] =next;
            this.pheromones[pos*dimension+ next] =  (1.0D - rho) * this.pheromones[pos*dimension+ next] + rho * tau0;
            pos = next;

        }
        solutionLength += this.distanceMatrix[initialPosition *dimension+solution[dimension - 1]];
        solution[dimension] = initialPosition;
        if (solutionLength < iterationLength) {
            iterationWalk = solution;
            iterationLength = solutionLength;
        }

    }

    public int RouletteWheel(double[] set, double sum) throws Exception {
        var partialSum = new double[dimension];
        double remove = 0.d;
        for (int i = 0; i < dimension; i++) {
            partialSum[i] = sum - remove;
            remove += set[i];
        }
        if (remove <= 0) {
            throw new Exception("remove is zero");
        }
        double choice = random.nextDouble(0, sum);
        for (int i = 0; i < dimension; i++) {
            if (i + 1 < dimension && partialSum[i] >= choice && choice > partialSum[i + 1]) {
                return i;
            }
            if (i + 1 == dimension && partialSum[i] >= choice && choice >= 0.d) {
                return i;
            }

        }
        throw new Exception();
    }

    public void setupColony(String title) throws IOException {
        Setup setup = new Setup("problems/" + title);
        setup.setupMatrix();
        this.distanceMatrix = setup.getDistanceMatrix();
        this.dimension = setup.getDimension();
        nearestNeighbour();
        this.tau0 = 1.0D / (dimension * bestLength);
        setInvertedDistanceMatrix();
        setPheromonesLevels();
        bestKnown = setup.bestKnown;
    }

    public void setInvertedDistanceMatrix() {
        this.invertedDistanceMatrix = new double[dimension*dimension];
        for (int i = 0; i < dimension; i++) {
            for (int j = i; j < dimension; j++) {
                double d = Math.pow(invertDouble(this.distanceMatrix[i*dimension +j]), beta);
                invertedDistanceMatrix[i*dimension+j] = d;
                invertedDistanceMatrix[j*dimension+i] = d;
            }
        }
    }

    public void setPheromonesLevels() {
        this.pheromones =  new double[dimension*dimension];
        for (int i = 0; i < dimension; i++) {
            for (int j = i; j < dimension; j++) {
                pheromones[i*dimension+j] = tau0;
                pheromones[j*dimension+i] = tau0;
            }
        }
    }


    private boolean contains(int[] array, int value, int pos) {
        for(int i= 0; i < pos; i++) {
            if(array[i] == value) {
                return true;
            }
        }
        return false;
    }

    public void nearestNeighbour() {
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
            bestLength += minWeight;
            bestWalk[pos++] = next;
            curr = next;
        }
        bestLength += this.distanceMatrix[curr*dimension+i];
        bestWalk[pos] = i;
    }

    public int getLength(int[] list) {
        int tmp = 0;
        for (int i = 0; i < dimension; i++) {
            tmp += this.distanceMatrix[list[i]*dimension + list[i+1]];
        }
        return tmp;
    }

    private double invertDouble(double distance) {
        if (distance == 0)
            return 0.d;
        else
            return 1.0d / distance;
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
}