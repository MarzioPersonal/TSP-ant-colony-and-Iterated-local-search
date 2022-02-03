package com.cup;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.*;

public class BenchIteratedSearch {
    private final ExecutorService ex = Executors.newFixedThreadPool(10);

    public void execute() throws ExecutionException, InterruptedException {
        String[] titles = {
                "ch130.tsp",
                "d198.tsp",
                "eil76.tsp",
                "fl1577.tsp",
                "kroA100.tsp",
                "lin318.tsp",
                "pcb442.tsp",
                "pr439.tsp",
                "rat783.tsp",
                "u1060.tsp"
        };
        double min = Integer.MAX_VALUE;
        int len = titles.length;
        while (true) {
            var seed = new Random().nextLong();
            System.out.println("seed " + seed);
            List<toRun> jobs = new ArrayList<>(len);
            List<Future<Double>> futures = new ArrayList<>(len);
            for (int i = 0; i < len; i++) {
                jobs.add(new toRun(titles[i], seed));
            }

            for (int i = 0; i < len; i++) {
                futures.add(ex.submit(jobs.get(i)));
            }

            double[] results = new double[10];
            for (int i = 0; i < 10; i++) {
                double result = futures.get(i).get();
                results[i] = result;
            }

            var err = checkError(results);
            if (err < min) {
                System.out.println(Arrays.toString(results));
                min = err;
            }
            System.out.println(min);


        }

    }

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        BenchIteratedSearch benchIteratedSearch = new BenchIteratedSearch();
        benchIteratedSearch.execute();
    }

    static class toRun implements Callable<Double> {
        private final String title;
        private final long seed;

        public toRun(String title, long seed) {
            this.title = title;
            this.seed = seed;
        }

        @Override
        public Double call() throws Exception {
            IteratedLocalSearch it = new IteratedLocalSearch(seed);
            return it.execute(title);
        }
    }

    public double checkError(double[] results) {
        double result = 0;
        result += ((results[0] - 6110.d) / (6110.d)) * 100;
        result += ((results[1] - 15780.d) / (15780.d)) * 100;
        result += ((results[2] - 538.d) / (538.d)) * 100;
        result += ((results[3] - 22249.d) / (22249.d)) * 100;
        result += ((results[4] - 21282.d) / (21282.d)) * 100;
        result += ((results[5] - 42029.d) / (42029.d)) * 100;
        result += ((results[6] - 50778.d) / (50778.d)) * 100;
        result += ((results[7] - 107217.d) / (107217.d)) * 100;
        result += ((results[8] - 8806) / (8806)) * 100;
        result += ((results[9] - 224094.d) / (224094.d)) * 100;
        return result / 10.d;
    }

    public void verifySolution() throws ExecutionException, InterruptedException {
        String[] titles = {
                "ch130.tsp",
                "d198.tsp",
                "eil76.tsp",
                "fl1577.tsp",
                "kroA100.tsp",
                "lin318.tsp",
                "pcb442.tsp",
                "pr439.tsp",
                "rat783.tsp",
                "u1060.tsp"
        };
        double min = Integer.MAX_VALUE;
        int len = titles.length;
        var seed = 5906447272222300000L;
        System.out.println("seed " + seed);
        List<toRun> jobs = new ArrayList<>(len);
        List<Future<Double>> futures = new ArrayList<>(len);
        for (int i = 0; i < len; i++) {
            jobs.add(new toRun(titles[i], seed));
        }

        for (int i = 0; i < len; i++) {
            futures.add(ex.submit(jobs.get(i)));
        }

        double[] results = new double[10];
        for (int i = 0; i < 10; i++) {
            double result = futures.get(i).get();
            results[i] = result;
        }

        var err = checkError(results);
        System.out.println(Arrays.toString(results));
        System.out.println(min);

    }
}
