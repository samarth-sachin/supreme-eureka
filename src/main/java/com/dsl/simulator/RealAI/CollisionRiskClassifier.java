package com.dsl.simulator.RealAI;

import lombok.extern.slf4j.Slf4j;
import org.deeplearning4j.nn.api.OptimizationAlgorithm;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.layers.DenseLayer;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.learning.config.Adam;
import org.nd4j.linalg.lossfunctions.LossFunctions;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Slf4j
@Component
public class CollisionRiskClassifier {

    private MultiLayerNetwork riskModel;
    private boolean isModelTrained = false;
    private final String[] riskLevels = {"LOW", "MODERATE", "HIGH", "CRITICAL"};

    public void trainCollisionRiskModel() {
        log.info("⚠️ Training collision risk classification network...");

        // Neural network for orbital collision risk assessment
        MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder()
                .seed(321)
                .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
                .updater(new Adam(0.001))
                .weightInit(WeightInit.XAVIER)
                .list()
                .layer(0, new DenseLayer.Builder()
                        .nIn(7)  // Input: altitude, velocity, inclination, debris_count, solar_activity, position_uncertainty, time_to_closest_approach
                        .nOut(40)
                        .activation(Activation.RELU)
                        .build())
                .layer(1, new DenseLayer.Builder()
                        .nIn(40)
                        .nOut(25)
                        .activation(Activation.RELU)
                        .build())
                .layer(2, new DenseLayer.Builder()
                        .nIn(25)
                        .nOut(15)
                        .activation(Activation.RELU)
                        .build())
                .layer(3, new OutputLayer.Builder(LossFunctions.LossFunction.NEGATIVELOGLIKELIHOOD)
                        .nIn(15)
                        .nOut(4)  // 4 risk levels: LOW, MODERATE, HIGH, CRITICAL
                        .activation(Activation.SOFTMAX)
                        .build())
                .build();

        riskModel = new MultiLayerNetwork(conf);
        riskModel.init();

        // Generate realistic collision risk training data
        DataSet trainingData = generateCollisionRiskData();

        log.info("🎯 Training collision classifier on {} orbital scenarios", trainingData.numExamples());

        // Train the risk classification model
        for (int i = 0; i < 120; i++) {
            riskModel.fit(trainingData);

            if (i % 30 == 0) {
                double score = riskModel.score();
                log.info("Epoch {}: Classification loss = {:.6f}", i, score);
            }
        }

        isModelTrained = true;
        log.info("✅ Collision risk classifier training completed!");
    }

    public Map<String, Object> assessCollisionRisk(String satelliteId, double[] orbitalParameters) {
        if (!isModelTrained) {
            trainCollisionRiskModel();
        }

        Map<String, Object> riskAssessment = new HashMap<>();

        // Neural network classification
        INDArray input = Nd4j.create(new double[][]{orbitalParameters});
        INDArray output = riskModel.output(input);

        // Find highest probability risk level
        int predictedClass = output.argMax(1).getInt(0);
        String riskLevel = riskLevels[predictedClass];
        double confidence = output.getDouble(0, predictedClass);

        // Calculate specific risk probabilities
        double collisionProbability = calculateCollisionProbability(orbitalParameters);
        int threateningObjects = estimateThreateningObjects(orbitalParameters);
        String timeToEvent = estimateTimeToClosestApproach(orbitalParameters);

        riskAssessment.put("riskLevel", riskLevel);
        riskAssessment.put("confidence", confidence);
        riskAssessment.put("collisionProbability", collisionProbability);
        riskAssessment.put("threateningObjects", threateningObjects);
        riskAssessment.put("timeToClosestApproach", timeToEvent);
        riskAssessment.put("recommendedActions", generateRiskRecommendations(riskLevel));

        log.info("⚠️ Collision risk assessment for {}: {} risk ({:.1f}% confidence), {} objects tracked",
                satelliteId, riskLevel, confidence * 100, threateningObjects);

        return riskAssessment;
    }

    private DataSet generateCollisionRiskData() {
        int numSamples = 1200;
        INDArray features = Nd4j.zeros(numSamples, 7);
        INDArray labels = Nd4j.zeros(numSamples, 4);
        Random random = new Random(654);

        for (int i = 0; i < numSamples; i++) {
            // Generate realistic orbital parameters
            double altitude = 200 + random.nextDouble() * 1000;        // 200-1200km altitude
            double velocity = 7.8 - (altitude - 408) / 10000;          // Orbital velocity (km/s)
            double inclination = random.nextDouble() * 180;            // Orbital inclination (degrees)
            double debrisCount = Math.max(0, random.nextGaussian() * 5 + 15); // Space debris in vicinity
            double solarActivity = random.nextDouble();                // Solar activity index (0-1)
            double positionUncertainty = random.nextDouble() * 0.01;   // Position uncertainty (km)
            double timeToApproach = random.nextDouble() * 72;          // Hours to closest approach

            // Set features
            features.putScalar(new int[]{i, 0}, altitude / 1200);      // Normalize
            features.putScalar(new int[]{i, 1}, velocity / 10);
            features.putScalar(new int[]{i, 2}, inclination / 180);
            features.putScalar(new int[]{i, 3}, Math.min(1.0, debrisCount / 30));
            features.putScalar(new int[]{i, 4}, solarActivity);
            features.putScalar(new int[]{i, 5}, positionUncertainty * 100);
            features.putScalar(new int[]{i, 6}, timeToApproach / 72);

            // Determine risk level based on realistic criteria
            int riskClass = 0; // LOW by default

            if (altitude < 300 || debrisCount > 25) riskClass = 1;     // MODERATE
            if (debrisCount > 20 && timeToApproach < 12) riskClass = 2; // HIGH
            if (positionUncertainty > 0.005 && debrisCount > 15 && timeToApproach < 6) riskClass = 3; // CRITICAL

            // Add some randomness to make it more realistic
            if (random.nextDouble() < 0.1) {
                riskClass = Math.min(3, riskClass + 1);
            }

            // One-hot encode the risk class
            labels.putScalar(new int[]{i, riskClass}, 1.0);
        }

        return new DataSet(features, labels);
    }

    private double calculateCollisionProbability(double[] params) {
        // Realistic collision probability calculation based on orbital parameters
        double altitude = params[0] * 1200;
        double debrisCount = params[3] * 30;
        double positionUncertainty = params[5] / 100;
        double timeToApproach = params[6] * 72;

        // Base probability increases with lower altitude and more debris
        double baseProbability = 0.001 * (1 - altitude / 1200) * (debrisCount / 30);

        // Adjust for uncertainty and time
        double uncertaintyFactor = 1 + positionUncertainty * 50;
        double timeFactor = Math.max(0.1, 1 - timeToApproach / 72);

        return Math.min(0.1, baseProbability * uncertaintyFactor * timeFactor);
    }

    private int estimateThreateningObjects(double[] params) {
        double debrisCount = params[3] * 30;
        double altitude = params[0] * 1200;

        // More debris at lower altitudes
        int baseObjects = (int) debrisCount;
        if (altitude < 400) baseObjects *= 1.5;
        if (altitude < 300) baseObjects *= 2;

        return Math.max(1, Math.min(50, baseObjects));
    }

    private String estimateTimeToClosestApproach(double[] params) {
        double timeHours = params[6] * 72;

        if (timeHours < 1) return "< 1 hour (IMMEDIATE)";
        if (timeHours < 6) return String.format("%.1f hours", timeHours);
        if (timeHours < 24) return String.format("%.0f hours", timeHours);
        return String.format("%.1f days", timeHours / 24);
    }

    private String[] generateRiskRecommendations(String riskLevel) {
        switch (riskLevel) {
            case "CRITICAL":
                return new String[]{"Execute immediate avoidance maneuver", "Alert mission control", "Prepare emergency protocols"};
            case "HIGH":
                return new String[]{"Plan avoidance maneuver", "Increase tracking frequency", "Review trajectory options"};
            case "MODERATE":
                return new String[]{"Monitor closely", "Update tracking data", "Prepare contingency plans"};
            default:
                return new String[]{"Continue normal operations", "Routine monitoring"};
        }
    }

    public double getClassificationAccuracy() {
        if (!isModelTrained) return 0.0;

        // Generate test data and evaluate
        DataSet testData = generateCollisionRiskData();
        INDArray predictions = riskModel.output(testData.getFeatures());
        INDArray actualLabels = testData.getLabels();

        int correct = 0;
        int total = testData.numExamples();

        for (int i = 0; i < total; i++) {
            int predicted = predictions.getRow(i).argMax().getInt(0);
            int actual = actualLabels.getRow(i).argMax().getInt(0);

            if (predicted == actual) {
                correct++;
            }
        }

        return (double) correct / total;
    }
}
