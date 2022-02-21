package com.cup;


import org.apache.commons.math3.linear.MatrixUtils;

import org.apache.commons.math3.linear.RealMatrix;

import java.util.*;

public class AntColony {
    private final static int numberOfAnts = 10;
    private final static int beta = 2;
    private final static double alpha = 0.1d;
    private final static double rho = 0.1d;
    private final static double q0 = 0.9;

    private final Random random;

    public ArrayList<Integer> bestWalk;
    public double bestLength = 0;

    private ArrayList<Integer> iterationWalk;
    private double iterationLength = Integer.MAX_VALUE;

    RealMatrix distanceMatrix;
    RealMatrix invertedDistanceMatrix;
    RealMatrix pheromones;

    private double tau0;
    private int dimension;

    double bestKnown;

    public AntColony(long seed) {
        this.random = new Random(seed);
    }

    public static void main(String[] args) {
        AntColony antColony = new AntColony(45454L);
        antColony.setupColony("u1060.tsp");
        antColony.runAntColony();
    }

    public void runAntColony() {
        var start = System.currentTimeMillis();
        var elapsed = 0L;
        var end = start + 180000;
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
                System.out.println(bestLength);
                if (bestLength == bestKnown) {
                    break;
                }
            }
            for (int i = 0; i < dimension; i++) {
                var s = bestWalk.get(i);
                var e = bestWalk.get(i + 1);
                this.pheromones.setEntry(s, e, this.pheromones.getEntry(s, e) * (1.0D - alpha) + alpha * (1.0D / bestLength));
            }
            var t2 = System.currentTimeMillis();
            elapsed = (t2 - t1);
            start += elapsed;

        }
        System.out.println(bestLength);
    }

    private ArrayList<Integer> twoOpt(ArrayList<Integer> sol) {
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

    private void ant(int initialPosition) {
        var solution = new ArrayList<Integer>(dimension + 1);
        var solutionLength = 0;
        solution.add(initialPosition);
        var pos = initialPosition;
        while (solution.size() < dimension) {
            var argmax = new double[dimension];
            double sum = 0;
            for (int i = 0; i < dimension; i++) {
                double x = this.pheromones.getEntry(pos, i) * Math.pow(this.invertedDistanceMatrix.getEntry(pos, i), beta);
                argmax[i] = x;
                sum += x;
            }
            for (Integer i : solution) {
                var x = argmax[i];
                sum -= x;
                argmax[i] = 0.d;
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
                    break;
                }
            }
            solutionLength += this.distanceMatrix.getEntry(pos, next);
            solution.add(next);
            this.pheromones.setEntry(pos, next, (1.0D - rho) * this.pheromones.getEntry(pos, next) + rho * tau0);
            pos = next;

        }
        solutionLength += this.distanceMatrix.getEntry(initialPosition, solution.get(dimension - 1));
        solution.add(initialPosition);
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

    public void setupColony(String title) {
        Setup setup = new Setup("problems/" + title);
        setup.setupMatrix();
        this.distanceMatrix = setup.getDistanceMatrix();
        this.dimension = setup.getDimension();
        nearestNeighbour();
        this.tau0 = 1.0D / (dimension * bestLength);
        setInvertedDistanceMatrix();
        setPheromonesLevels();
        bestKnown = setup.bestKnown;
        System.out.println(bestLength);
    }

    public void setInvertedDistanceMatrix() {
        double[][] matrix = new double[dimension][dimension];
        for (int i = 0; i < dimension; i++) {
            for (int j = i; j < dimension; j++) {
                double d = Math.pow(invertDouble(this.distanceMatrix.getEntry(i, j)), beta);
                matrix[i][j] = d;
                matrix[j][i] = d;
            }
        }
        this.invertedDistanceMatrix = MatrixUtils.createRealMatrix(matrix);
    }

    public void setPheromonesLevels() {
        final double[][] matrix = new double[dimension][dimension];
        for (int i = 0; i < dimension; i++) {
            for (int j = i; j < dimension; j++) {
                matrix[i][j] = tau0;
                matrix[j][i] = tau0;
            }
        }
        this.pheromones = MatrixUtils.createRealMatrix(matrix);
    }

    public void nearestNeighbour() {
        bestWalk = new ArrayList<>(dimension + 1);
        int i = random.nextInt(0, dimension);
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
            bestLength += minWeight;
            bestWalk.add(next);
            curr = next;
        }
        bestLength += this.distanceMatrix.getEntry(i, curr);
        bestWalk.add(i);

    }

    public static boolean findDuplicates(ArrayList<Integer> x) {
        for (int i = 1; i < x.size() - 1; i++) {
            for (int j = i + 1; j < x.size() - 1; j++) {
                if (Objects.equals(x.get(i), x.get(j))) {
                    System.out.println("idx " + i + " " + j + " : " + x.get(i));
                    System.out.println(x);
                    System.out.println("error with size " + x.size());
                    return true;
                }
            }
        }
        return false;
    }

    public double getLength(ArrayList<Integer> list) {
        double tmp = 0;
        for (int i = 0; i < dimension; i++) {
            tmp += this.distanceMatrix.getEntry(list.get(i), list.get(i + 1));
        }
        return tmp;
    }

    private double invertDouble(double distance) {
        if (distance == 0)
            return 0.d;
        else
            return 1.0d / distance;
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


}