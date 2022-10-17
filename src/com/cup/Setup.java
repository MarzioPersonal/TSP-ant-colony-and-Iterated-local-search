package com.cup;


import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.lang.Math;

class Setup {
    String name;
    int dimension;
    int bestKnown;
    final String path;
    double[] distanceMatrix;
    HashMap<Integer, ArrayList<Double>> map = new HashMap<>();

    public Setup(String path) {
        this.name = "";
        this.dimension = 0;
        this.bestKnown = 0;
        this.path = path;
    }

    public static void main(String[] args) {
    }

    public void setupMatrix() {
//        System.out.println(path);
        try {
            boolean skip = false;
            final BufferedReader in = new BufferedReader(
                    new InputStreamReader(new FileInputStream(path), StandardCharsets.UTF_8));
            String line;
            while (!Objects.equals(line = in.readLine(), "EOF")) {
                var l = line.split(" ");
                l = Arrays.stream(l).filter(value -> value.length() != 0).toArray(String[]::new);
                if (!skip) {

                    switch (l[0]) {
                        case "NAME:" -> this.setName(l[1]);
                        case "NAME" -> this.setName(l[2]);
                        case "DIMENSION:" -> this.setDistanceMatrix(Integer.parseInt(l[1]));
                        case "DIMENSION" -> this.setDistanceMatrix(Integer.parseInt(l[2]));
                        case "BEST_KNOWN:" -> this.setBestKnown(Integer.parseInt(l[1]));
                        case "BEST_KNOWN" -> this.setBestKnown(Integer.parseInt(l[2]));
                    }
                } else {
                    var index = Integer.parseInt(l[0]) - 1;
                    var list = new ArrayList<Double>();
                    list.add(convertToInt(l[1]));
                    list.add(convertToInt(l[2]));
                    this.map.put(index, list);

                }
                if (line.contains("NODE_COORD_SECTION")) {
                    skip = true;
                }


            }
            in.close();
            for (int i = 0; i < this.map.size(); i++) {
                for (int j = i; j < this.map.size(); j++) {
                    this.setDistanceBetweenCities(i, j);
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public double convertToInt(String s) {
        double toRet;
        try {
            toRet = Double.parseDouble(s);
        } catch (Exception e) {
            toRet = new BigDecimal(s).doubleValue();
        }
        return toRet;
    }

    public String getName() {
        return name;
    }

    public double[] getDistanceMatrix() {
        return distanceMatrix;
    }

    private void setName(String name) {
        this.name = name;
    }


    public int getDimension() {
        return dimension;
    }

    private void setDimension(int dimension) {
        this.dimension = dimension;
    }


    private void setBestKnown(int bestKnown) {
        this.bestKnown = bestKnown;
    }

    private void setDistanceMatrix(int dimension) {
        this.setDimension(dimension);
        this.distanceMatrix = new double[dimension * dimension];
    }

    private void setDistanceBetweenCities(int i, int j) {
        var coords_a = this.map.get(i);
        var coords_b = this.map.get(j);
        double dij = Math.round(Math.sqrt(
                Math.pow((coords_a.get(0) - coords_b.get(0)), 2) +
                        Math.pow((coords_a.get(1) - coords_b.get(1)), 2)
        ));
        this.distanceMatrix[i* dimension+ j] = dij;
        this.distanceMatrix[j*dimension + i] =  dij;

    }

    public void printDistanceMatrix() {
        for (int i = 0; i < dimension; i++) {
            for (int j = 0; j < dimension; j++) {
                System.out.print(distanceMatrix[i * dimension+ j] + " ");
            }
            System.out.println();
        }
    }


    public double getBestKnown() {
        return bestKnown;
    }
}
