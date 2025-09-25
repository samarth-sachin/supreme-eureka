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

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Slf4j
@Component
public class AnomalyDetectionNetwork {

    private MultiLayerNetwork autoencoderModel;
    private boolean isModelTrained = false;
    private double anomalyThreshold = 0.1;

    public void trainAnomalyDetector() {
        log.info("🚨 Training anomaly detection autoencoder...");

        // Autoencoder configuration: 8 → 4 → 2 → 4 → 8 (compression and reconstruction)
        MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder()
                .seed(456)
                .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
                .updater(new Adam(0.001))
                .weightInit(WeightInit.XAVIER)
                .list()
                .layer(0, new DenseLayer.Builder()
                        .nIn(8)   // Input: 8 sensor readings
                        .nOut(4)  // Compress to 4
                        .activation(Activation.RELU)
                        .build())
                .layer(1, new DenseLayer.Builder()
                        .nIn(4)
                        .nOut(2)  // Bottleneck: compress to 2
                        .activation(Activation.RELU)
                        .build())
                .layer(2, new DenseLayer.Builder()
                        .nIn(2)
                        .nOut(4)  // Decompress to 4
                        .activation(Activation.RELU)
                        .build())
                .layer(3, new OutputLayer.Builder(LossFunctions.LossFunction.MSE)
                        .nIn(4)
                        .nOut(8)  // Reconstruct to 8 outputs
                        .activation(Activation.SIGMOID)
                        .build())
                .build();

        autoencoderModel = new MultiLayerNetwork(conf);
        autoencoderModel.init();

        // Generate normal operational data for training
        DataSet trainingData = generateNormalOperationalData();

        log.info("🎯 Training autoencoder on {} normal operation patterns", trainingData.numExamples());

        // Train autoencoder to reconstruct normal patterns
        for (int i = 0; i < 150; i++) {
            autoencoderModel.fit(trainingData);

            if (i % 30 == 0) {
                double score = autoencoderModel.score();
                log.info("Epoch {}: Reconstruction loss = {:.6f}", i, score);
            }
        }

        // Calculate anomaly threshold from training data
        calculateAnomalyThreshold(trainingData);

        isModelTrained = true;
        log.info("✅ Anomaly detection model training completed! Threshold: {:.4f}", anomalyThreshold);
    }

    public List<String> detectAnomalies(String satelliteId, double[] sensorData) {
        if (!isModelTrained) {
            trainAnomalyDetector();
        }

        List<String> anomalies = new ArrayList<>();

        // Pass data through autoencoder
        INDArray input = Nd4j.create(new double[][]{sensorData});
        INDArray reconstructed = autoencoderModel.output(input);

        // Calculate reconstruction error
        INDArray error = input.sub(reconstructed);
        double reconstructionError = error.norm2Number().doubleValue() / sensorData.length;

        log.info("🔍 Anomaly detection for {}: Reconstruction error = {:.6f}, Threshold = {:.6f}",
                satelliteId, reconstructionError, anomalyThreshold);

        // Detect anomalies based on reconstruction error
        if (reconstructionError > anomalyThreshold) {
            // Analyze which sensors are most anomalous
            String[] sensorNames = {"Power", "Temperature", "Pressure", "Vibration",
                    "Radiation", "Magnetic", "Solar", "Battery"};

            for (int i = 0; i < sensorData.length; i++) {
                double sensorError = Math.abs(sensorData[i] - reconstructed.getDouble(0, i));
                if (sensorError > anomalyThreshold / 2) {
                    anomalies.add(String.format("ANOMALY_%s: %.3f deviation (expected: %.3f, actual: %.3f)",
                            sensorNames[i].toUpperCase(), sensorError, reconstructed.getDouble(0, i), sensorData[i]));
                }
            }

            if (anomalies.isEmpty()) {
                anomalies.add("GENERAL_ANOMALY: Reconstruction error exceeds threshold");
            }
        }

        return anomalies;
    }

    private DataSet generateNormalOperationalData() {
        int numSamples = 800;
        INDArray features = Nd4j.zeros(numSamples, 8);
        Random random = new Random(789);

        for (int i = 0; i < numSamples; i++) {
            // Generate realistic "normal" sensor patterns for ISS-like satellite
            double power = 0.85 + random.nextGaussian() * 0.05;        // Power: ~85% ±5%
            double temp = 20 + random.nextGaussian() * 5;              // Temp: ~20°C ±5°C
            double pressure = 1013 + random.nextGaussian() * 10;       // Pressure variation
            double vibration = 0.02 + random.nextGaussian() * 0.005;   // Low vibration
            double radiation = 0.1 + random.nextGaussian() * 0.02;     // Background radiation
            double magnetic = 0.5 + random.nextGaussian() * 0.1;       // Earth's magnetic field
            double solar = 0.9 + random.nextGaussian() * 0.05;         // Solar panel efficiency
            double battery = 0.88 + random.nextGaussian() * 0.03;      // Battery level

            // Normalize to 0-1 range
            features.putScalar(new int[]{i, 0}, Math.max(0.1, Math.min(1.0, power)));
            features.putScalar(new int[]{i, 1}, (temp + 50) / 100);                    // -50 to 50°C → 0-1
            features.putScalar(new int[]{i, 2}, Math.max(0.1, Math.min(1.0, pressure / 1100)));
            features.putScalar(new int[]{i, 3}, Math.max(0.0, Math.min(1.0, vibration * 20)));
            features.putScalar(new int[]{i, 4}, Math.max(0.0, Math.min(1.0, radiation * 5)));
            features.putScalar(new int[]{i, 5}, Math.max(0.1, Math.min(1.0, magnetic)));
            features.putScalar(new int[]{i, 6}, Math.max(0.1, Math.min(1.0, solar)));
            features.putScalar(new int[]{i, 7}, Math.max(0.1, Math.min(1.0, battery)));
        }

        // For autoencoders, labels are the same as features (reconstruction task)
        return new DataSet(features, features);
    }

    private void calculateAnomalyThreshold(DataSet normalData) {
        // Calculate 95th percentile of reconstruction errors on normal data
        double[] errors = new double[normalData.numExamples()];

        for (int i = 0; i < normalData.numExamples(); i++) {
            INDArray sample = normalData.getFeatures().getRow(i);
            INDArray reconstructed = autoencoderModel.output(sample);
            INDArray error = sample.sub(reconstructed);
            errors[i] = error.norm2Number().doubleValue() / 8; // Normalize by feature count
        }

        // Sort errors and take 95th percentile
        java.util.Arrays.sort(errors);
        anomalyThreshold = errors[(int)(errors.length * 0.95)];
    }

    public double getDetectionAccuracy() {
        if (!isModelTrained) return 0.0;

        // Test on known normal and anomalous patterns
        int correctDetections = 0;
        int totalTests = 100;
        Random random = new Random(999);

        for (int i = 0; i < totalTests; i++) {
            // Half normal, half anomalous test cases
            boolean isAnomaly = i >= totalTests / 2;
            double[] testData = new double[8];

            if (isAnomaly) {
                // Generate anomalous data
                for (int j = 0; j < 8; j++) {
                    testData[j] = random.nextDouble() * (random.nextBoolean() ? 0.3 : 1.7); // Outside normal range
                }
            } else {
                // Generate normal data
                testData[0] = 0.85 + random.nextGaussian() * 0.05;
                testData[1] = (20 + random.nextGaussian() * 5 + 50) / 100;
                // ... (similar for other sensors)
                for (int j = 2; j < 8; j++) {
                    testData[j] = 0.5 + random.nextGaussian() * 0.1;
                }
            }

            List<String> detectedAnomalies = detectAnomalies("TEST", testData);
            boolean detected = !detectedAnomalies.isEmpty();

            if (detected == isAnomaly) {
                correctDetections++;
            }
        }

        return (double) correctDetections / totalTests;
    }
}
