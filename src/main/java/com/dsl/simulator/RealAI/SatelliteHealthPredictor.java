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

@Slf4j
@Component
public class SatelliteHealthPredictor {

    private MultiLayerNetwork healthModel;
    private boolean isModelTrained = false;

    public void trainHealthModel() {
        log.info("🧠 Training satellite health prediction neural network...");

        // Network configuration - 6 input features → 3 hidden layers → 5 outputs (subsystem health)
        MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder()
                .seed(123)
                .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
                .updater(new Adam(0.001))
                .weightInit(WeightInit.XAVIER)
                .list()
                .layer(0, new DenseLayer.Builder()
                        .nIn(6)  // Input: power, thermal, fuel, altitude, solar_angle, battery
                        .nOut(50)
                        .activation(Activation.RELU)
                        .build())
                .layer(1, new DenseLayer.Builder()
                        .nIn(50)
                        .nOut(30)
                        .activation(Activation.RELU)
                        .build())
                .layer(2, new DenseLayer.Builder()
                        .nIn(30)
                        .nOut(20)
                        .activation(Activation.RELU)
                        .build())
                .layer(3, new OutputLayer.Builder(LossFunctions.LossFunction.MSE)
                        .nIn(20)
                        .nOut(5)  // Output: 5 subsystem health scores
                        .activation(Activation.SIGMOID)
                        .build())
                .build();

        healthModel = new MultiLayerNetwork(conf);
        healthModel.init();

        // Generate realistic training data based on ISS parameters
        DataSet trainingData = generateRealisticTrainingData();

        log.info("🎯 Training on {} data points with realistic satellite parameters", trainingData.numExamples());

        // Train the model
        for (int i = 0; i < 100; i++) {
            healthModel.fit(trainingData);

            if (i % 20 == 0) {
                double score = healthModel.score();
                log.info("Epoch {}: Training loss = {:.6f}", i, score);
            }
        }

        isModelTrained = true;
        log.info("✅ Health prediction model training completed!");
    }

    public Map<String, Double> predictSatelliteHealth(String satelliteId, double[] telemetryData) {
        if (!isModelTrained) {
            trainHealthModel();
        }

        // Convert telemetry to neural network input
        INDArray input = Nd4j.create(new double[][]{telemetryData});
        INDArray output = healthModel.output(input);

        // Map outputs to subsystem health scores
        Map<String, Double> healthScores = new HashMap<>();
        healthScores.put("Power System", output.getDouble(0));
        healthScores.put("Thermal Management", output.getDouble(1));
        healthScores.put("Propulsion", output.getDouble(2));
        healthScores.put("Attitude Control", output.getDouble(3));
        healthScores.put("Communication", output.getDouble(4));

        log.info("🔮 Neural network health prediction for {}: Power={:.1f}%, Thermal={:.1f}%, Propulsion={:.1f}%",
                satelliteId,
                healthScores.get("Power System") * 100,
                healthScores.get("Thermal Management") * 100,
                healthScores.get("Propulsion") * 100);

        return healthScores;
    }

    private DataSet generateRealisticTrainingData() {
        int numSamples = 1000;
        INDArray features = Nd4j.zeros(numSamples, 6);
        INDArray labels = Nd4j.zeros(numSamples, 5);

        for (int i = 0; i < numSamples; i++) {
            // Generate realistic satellite telemetry based on ISS patterns
            double power = 0.7 + Math.random() * 0.3;           // Power: 70-100%
            double thermal = -20 + Math.random() * 80;          // Temperature: -20°C to 60°C
            double fuel = 0.5 + Math.random() * 0.5;            // Fuel: 50-100%
            double altitude = 408 + Math.random() * 10;         // ISS altitude ±10km
            double solarAngle = Math.random() * 360;            // Solar panel angle
            double battery = 0.8 + Math.random() * 0.2;         // Battery: 80-100%

            // Set features
            features.putScalar(new int[]{i, 0}, power);
            features.putScalar(new int[]{i, 1}, thermal);
            features.putScalar(new int[]{i, 2}, fuel);
            features.putScalar(new int[]{i, 3}, altitude);
            features.putScalar(new int[]{i, 4}, solarAngle);
            features.putScalar(new int[]{i, 5}, battery);

            // Generate realistic health labels based on physics
            double powerHealth = Math.min(1.0, power + (solarAngle < 180 ? 0.1 : -0.1));
            double thermalHealth = 1.0 - Math.abs(thermal - 20) / 100;  // Optimal at 20°C
            double propulsionHealth = fuel * 0.9 + Math.random() * 0.1;
            double attitudeHealth = 0.85 + Math.random() * 0.15;
            double commHealth = Math.max(0.7, powerHealth - Math.random() * 0.1);

            labels.putScalar(new int[]{i, 0}, Math.max(0.5, powerHealth));
            labels.putScalar(new int[]{i, 1}, Math.max(0.5, thermalHealth));
            labels.putScalar(new int[]{i, 2}, Math.max(0.5, propulsionHealth));
            labels.putScalar(new int[]{i, 3}, Math.max(0.5, attitudeHealth));
            labels.putScalar(new int[]{i, 4}, Math.max(0.5, commHealth));
        }

        return new DataSet(features, labels);
    }

    public double getModelAccuracy() {
        if (!isModelTrained) return 0.0;

        // Generate test data and evaluate
        DataSet testData = generateRealisticTrainingData();
        INDArray predictions = healthModel.output(testData.getFeatures());

        // Calculate simple accuracy metric (within 10% tolerance)
        double accuracy = 0.0;
        int correct = 0;
        int total = testData.numExamples() * 5; // 5 outputs per example

        for (int i = 0; i < testData.numExamples(); i++) {
            for (int j = 0; j < 5; j++) {
                double predicted = predictions.getDouble(i, j);
                double actual = testData.getLabels().getDouble(i, j);
                if (Math.abs(predicted - actual) < 0.1) {
                    correct++;
                }
            }
        }

        return (double) correct / total;
    }
}
