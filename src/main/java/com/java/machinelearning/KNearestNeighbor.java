package com.java.machinelearning;

/**
 * Created by skynet
 */

import java.io.*;
import java.util.*;

public class KNearestNeighbor {

    //We will only normalize non-string columns and convert string to range format
    public List<double[]> normalize(Map<Integer, List<Object>> inputData, int toNormalizeRows[]) {
        List<double[]> result = new ArrayList<>();

        //Now normalize each feature..min-max normalization
        for (Integer i : toNormalizeRows) {
            List<Double> list = new ArrayList<>();
            for (Object obj : inputData.get(i)) {
                list.add(Double.parseDouble(obj.toString()));
            }
            double max = Collections.max(list);
            double min = Collections.min(list);

            double[] array = new double[list.size()];
            int index = 0;
            for (Double val : list) {
                array[index++] = (val - min) / (max - min);
            }
            result.add(array);
        }

        return result;
    }

    private Map<Integer, String> categorizedCars(Map<Integer, List<Object>> inputData, int classificationRow) {
        Map<Integer, String> carModels = new HashMap<>();

        List<Object> list = inputData.get(classificationRow);
        int index = 0;
        for (int i = 0; i < list.size() - 1; i++) {
            carModels.put(index++, list.get(i).toString());
        }

        return carModels;
    }

    //https://en.wikipedia.org/wiki/Euclidean_distance
    public double euclideanDistance(List<double[]> features, int sourceRowIndex, int toClassifyRow) {
        double diffs[] = new double[features.size()];

        for (int i = 0; i < features.size(); i++) {
            diffs[i] = features.get(i)[sourceRowIndex] - features.get(i)[toClassifyRow];
        }

        double SUM = 0.0;
        for (int i = 0; i < diffs.length - 1; i++) {
            SUM = SUM + (diffs[i] * diffs[i]);
        }

        return Math.sqrt(SUM);

    }


    private int[] sortRows(double[] distances) {

        Map<Double, List<Integer>> distances2indexMap = new HashMap<>();
        for (int i = 0; i < distances.length; i++) {

            if (!distances2indexMap.containsKey(distances[i])) {
                distances2indexMap.put(distances[i], new ArrayList<>());
            }
            List<Integer> indexes = distances2indexMap.get(distances[i]);
            indexes.add(i);
        }

        List<Double> sortedDistances = new ArrayList<Double>(distances2indexMap.keySet());
        Collections.sort(sortedDistances);

        int sortedIndexs[] = new int[distances.length];
        int counter = 0;
        for (Double val : sortedDistances) {
            List<Integer> indexs = distances2indexMap.get(val);
            for (int i : indexs) {
                sortedIndexs[counter] = i;
                counter++;
            }
        }
        return sortedIndexs;
    }

    public Map<Integer, List<Object>> getFeatures(List<String> lines) throws Exception {
        Map<Integer, List<Object>> featureMap = new HashMap<>();

        for (String line : lines) {
            String split[] = line.split(",");
            for (int i = 0; i < split.length; i++) {
                List<Object> feature = featureMap.get(i);
                if (feature == null) {
                    feature = new LinkedList<>();
                    featureMap.put(i, feature);
                }
                Object val = null;
                if (isString(split[i].trim())) {
                    val = split[i].trim();
                } else {
                    val = Integer.parseInt(split[i].trim());
                }
                feature.add(val);
            }
        }

        return featureMap;

    }

    private static List<String> readInput() throws IOException {
        ClassLoader classloader = Thread.currentThread().getContextClassLoader();
        InputStream is = classloader.getResourceAsStream("input.txt");

        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        List<String> lines = new ArrayList<String>();
        String line;
        while ((line = reader.readLine()) != null) {
            lines.add(line);
        }
        reader.close();
        is.close();
        return lines;
    }

    private boolean isString(String str) {
        if (str == null) return false;
        for (int i = 0; i < str.length(); i++) {
            if (str.charAt(i) < '0' && str.charAt(i) > '9')
                return false;
        }
        return true;
    }


    public static void main(String args[]) throws Exception {
        int K = 4;  // INPUT

        KNearestNeighbor client = new KNearestNeighbor();

        List<String> lines = client.readInput();
        Map<Integer, List<Object>> featureMap = client.getFeatures(lines);

        List<double[]> normalizedData = client.normalize(featureMap, new int[]{0, 1});
        Map<Integer, String> index2Categories = client.categorizedCars(featureMap, 2);

        int toClassifyRow = normalizedData.get(0).length - 1;    // INPUT

        double[] rowToDistance = new double[index2Categories.size()];
        for (int row = 0; row < index2Categories.size(); row++) {
            double distance = client.euclideanDistance(normalizedData, row, toClassifyRow);
            rowToDistance[row] = distance;
        }

        int[] sortedIndexes = client.sortRows(rowToDistance);
        Map<String, Integer> categoryCount = new HashMap<>();

        for (int i = 0; i < K; i++) {
            int index = sortedIndexes[i];
            String carType = index2Categories.get(index);
            if (categoryCount.containsKey(carType)) {
                int count = categoryCount.get(carType);
                count++;
                categoryCount.put(carType, count);
            } else {
                categoryCount.put(carType, 1);
            }
        }

        int maxCount = Integer.MIN_VALUE;
        String maxCountCarType = null;
        for (String carType : categoryCount.keySet()) {
            if (categoryCount.get(carType) > maxCount) {
                maxCount = categoryCount.get(carType);
                maxCountCarType = carType;
            }
        }

        System.out.println("Car Model: " + maxCountCarType);

    }
}
