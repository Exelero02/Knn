import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

public class Knn {

    static class Instance {
        double[] features;
        double distance;
        String label;

        Instance(double[] features, double distance, String label) {
            this.features = features;
            this.distance = distance;
            this.label = label;
        }
    }

    public static List<Instance> loadDataset(String filename) throws IOException {
        List<Instance> data = new ArrayList<>();
        BufferedReader buff = new BufferedReader(new FileReader(filename));
        String line;
        while ((line = buff.readLine()) != null) {
            String[] instance = line.trim().split(",");
            double[] features = new double[instance.length - 1];
            for (int i = 0; i < instance.length - 1; i++) {
                features[i] = Double.parseDouble(instance[i]);
            }
            String label = instance[instance.length - 1];
            data.add(new Instance(features, 0.0, label));
        }
        buff.close();
        return data;
    }

    public static double euclideanDistance(double[] x1, double[] x2) {
        double distance = 0;
        for (int i = 0; i < x1.length; i++) {
            distance += Math.pow(x1[i] - x2[i], 2);
        }
        return Math.sqrt(distance);
    }

    public static List<Instance> getNeighbors(List<Instance> trainData, double[] testInstance, int k) {
        List<Instance> distances = new ArrayList<>();
        for (Instance trainInstance : trainData) {
            double dist = euclideanDistance(trainInstance.features, testInstance);
            distances.add(new Instance(trainInstance.features, dist, trainInstance.label));
        }
        distances.sort(Comparator.comparingDouble(a -> a.distance));
        return distances.subList(0, k);
    }

    public static String getResponse(List<Instance> neighbors) {
        Map<String, Integer> classVotes = new HashMap<>();
        for (Instance neighbor : neighbors) {
            classVotes.put(neighbor.label, classVotes.getOrDefault(neighbor.label, 0) + 1);
        }
        String maxLabel = null;
        int maxCount = 0;
        for (Map.Entry<String, Integer> entry : classVotes.entrySet()) {
            if (entry.getValue() > maxCount) {
                maxCount = entry.getValue();
                maxLabel = entry.getKey();
            }
        }
        return maxLabel;
    }

    public static void classify(List<Instance> testData, List<Instance> trainData, int k) {
        int correctPredictions = 0;
        int totalPredictions = 0;
        for (Instance testInstance : testData) {
            String actualLabel = testInstance.label;
            List<Instance> neighbors = getNeighbors(trainData, testInstance.features, k);
            String predictedLabel = getResponse(neighbors);
            System.out.println("Predicted: " + predictedLabel + ", Actual: " + actualLabel);
            if (predictedLabel.equals(actualLabel)) {
                correctPredictions++;
            }
            totalPredictions++;
        }
        double accuracy = (double) correctPredictions / totalPredictions;
        System.out.println("Accuracy: " + accuracy);
    }

    public static void main(String[] args) throws IOException {
        BufferedReader buff = new BufferedReader(new InputStreamReader(System.in));

        System.out.print("Enter the path to the training file: ");
        String trainingFile = buff.readLine();
        List<Instance> trainData = loadDataset(trainingFile);

        System.out.print("Enter the value of k: ");
        int k = Integer.parseInt(buff.readLine());

        label:
        while (true) {
            System.out.println("Choose an option:");
            System.out.println("a) Check all observations from the test set");
            System.out.println("b) Check one observation from the console");
            System.out.println("c) Change k");
            System.out.println("d) Exit");
            System.out.print("Option: ");
            String option = buff.readLine();

            switch (option) {
                case "a":
                    System.out.print("Enter the path to the test file: ");
                    String testFile = buff.readLine();
                    List<Instance> testData = loadDataset(testFile);
                    classify(testData, trainData, k);
                    break;
                case "b":
                    System.out.print("Enter the observation: ");
                    String testInstanceStr = buff.readLine();
                    String[] testInstanceSplit = testInstanceStr.split(",");
                    double[] testFeatures = new double[testInstanceSplit.length];
                    for (int i = 0; i < testInstanceSplit.length; i++) {
                        testFeatures[i] = Double.parseDouble(testInstanceSplit[i]);
                    }
                    String predictedLabel = getResponse(getNeighbors(trainData, testFeatures, k));
                    System.out.println("Predicted Label: " + predictedLabel);
                    break;
                case "c":
                    System.out.print("Enter the new value of k: ");
                    k = Integer.parseInt(buff.readLine());
                    break;
                case "d":
                    break label;
                default:
                    System.out.println("Invalid option");
                    break;
            }
        }

        buff.close();
    }
}
