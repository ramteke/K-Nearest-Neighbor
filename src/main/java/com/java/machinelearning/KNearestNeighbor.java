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
                array[index++] = ((val - min) * 1.0) / ((max - min) * 1.0);
            }
            result.add(array);
        }

        return result;
    }

    private Map<Integer,String> categorizedCars(Map<Integer, List<Object>> inputData, int classificationRow) {
        Map<Integer,String> carModels = new HashMap<>();

        List<Object> list = inputData.get(classificationRow);
        int index = 0;
        for (Object obj : list) {
            carModels.put(index++, obj.toString());
        }

        return carModels;
    }

    //https://en.wikipedia.org/wiki/Euclidean_distance
    public double euclideanDistance(List<double[]> features, int sourceRowIndex) {
        double diffs[] = new double[features.size()];

        for (int i = 0; i < features.size(); i++) {
            for (int rowIndex = 0; rowIndex < features.get(i).length; rowIndex++) {
                diffs[i] = features.get(i)[rowIndex] - features.get(i)[sourceRowIndex];
            }
        }

        double SUM = 0.0;
        for ( double diff : diffs) {
            SUM = SUM + (diff*diff);
        }

        return  Math.sqrt(SUM);

    }

    public static void main(String args[]) throws Exception {
        KNearestNeighbor client = new KNearestNeighbor();

        //Step1: Read Features
        List<String> lines = client.readInput();
        Map<Integer, List<Object>> featureMap = client.getFeatures(lines);

        List<double[]> normalizedData = client.normalize(featureMap, new int[]{0, 1});
        Map<Integer, String> index2Categories = client.categorizedCars(featureMap, 2);


        double [] rowToDistance = new double[index2Categories.size()];
        for ( int i = 0; i < index2Categories.size(); i++) {
            double distance = client.euclideanDistance(normalizedData, i);
            rowToDistance[i] =distance;
        }

        int [] sortedIndexes = client.sortRows(rowToDistance);
        Map<String, Integer> categoryCount = new HashMap<>();
        for ( int i = sortedIndexes.length -1 ; i >= 0; i--) {

            if ( categoryCount.containsKey(sortedIndexes[i])) {
                int count = categoryCount.get( sortedIndexes[i]);
                count++;
                categoryCount.put( index2Categories.get((sortedIndexes[i])), count);
            } else {
                categoryCount.put( index2Categories.get((sortedIndexes[i])), 1);
            }
        }

        int maxCount = Integer.MIN_VALUE;
        String maxCountCar = null;
        for ( String str : categoryCount.keySet() ) {
            if  ( categoryCount.get(str) > maxCount) {
                maxCount = categoryCount.get(str);
                maxCountCar = str;
            }
        }

        System.out.println("Car Model: " + maxCountCar);

    }

    private int[] sortRows(double [] distances) {
        int sortedIndexs [] = new int[distances.length];
        Map<Double, List<Integer>> distances2indexMap = new HashMap<>();
        for (int i = 0; i < distances.length;i++) {

            if (! distances2indexMap.containsKey(distances[i]) ) {
                distances2indexMap.put(distances[i], new ArrayList<>());
            }
            List<Integer> indexes = distances2indexMap.get(distances[i]);
            indexes.add(i);
        }

        List<Double> distanceList = new ArrayList<Double>(distances2indexMap.keySet());
        Collections.sort(distanceList);
        int index = 0;
        for (Double val : distances) {
            List<Integer> indexs = distances2indexMap.get(val);
            for (int i : indexs) {
                sortedIndexs[index++] = i;
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
}
