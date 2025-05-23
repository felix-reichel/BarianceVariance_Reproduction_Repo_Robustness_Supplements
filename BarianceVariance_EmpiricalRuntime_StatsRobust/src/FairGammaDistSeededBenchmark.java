import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;

import org.junit.jupiter.api.Test;

public class FairGammaDistSeededBenchmark {

    static int[] sampleSizes = {100000};
    static int trials = 10000;
    static long seed = 42L;

    public static void main(String[] args) throws IOException {
        Random rand = new Random(seed);
        FileWriter writer = new FileWriter("./fair_variance_estimators_gamma_optimized.csv");
        writer.write("SampleSize,Trial,Estimator,RuntimeNs\n");

        for (int n : sampleSizes) {
            System.out.println("Running for sample size: " + n);
            for (int t = 0; t < trials; t++) {
                double[] data = new double[n];
                for (int i = 0; i < n; i++) {
                    data[i] = nextGamma(rand, 2.0, 2.0);
                }

                writer.write(n + "," + t + ",UnbiasedOptim," + time(() -> optimizedUnbiasedVariance(data)) + "\n");
                writer.write(n + "," + t + ",BiasedOptim," + time(() -> optimizedBiasedVariance(data)) + "\n");
                writer.write(n + "," + t + ",PopulationOptim," + time(() -> optimizedPopulationVariance(data)) + "\n");
                writer.write(n + "," + t + ",BarianceOptim," + time(() -> optimizedBariance(data)) + "\n");
                writer.write(n + "," + t + ",AltBarianceOptim," + time(() -> optimizedAltBariance(data)) + "\n");
            }
        }

        writer.close();
        System.out.println("Benchmark complete.");
        reportSystemInfo();
    }

    public static long time(Runnable r) {
        long start = System.nanoTime();
        r.run();
        return System.nanoTime() - start;
    }

    public static double optimizedUnbiasedVariance(double[] x) {
        int n = x.length;
        double mean = 0.0, m2 = 0.0;
        for (int i = 0; i < n; i++) {
            double delta = x[i] - mean;
            mean += delta / (i + 1);
            m2 += delta * (x[i] - mean);
        }
        return m2 / (n - 1);
    }

    public static double optimizedBiasedVariance(double[] x) {
        int n = x.length;
        double mean = 0.0, m2 = 0.0;
        for (int i = 0; i < n; i++) {
            double delta = x[i] - mean;
            mean += delta / (i + 1);
            m2 += delta * (x[i] - mean);
        }
        return m2 / n;
    }

    public static double optimizedPopulationVariance(double[] x) {
        return optimizedBiasedVariance(x);
    }

    public static double optimizedBariance(double[] x) {
        int n = x.length;
        double sum = 0.0, sumSq = 0.0;
        for (double v : x) {
            sum += v;
            sumSq += v * v;
        }
        return (2.0 * sumSq / (n - 1)) - (2.0 * sum * sum) / (n * (n - 1));
    }

    public static double optimizedAltBariance(double[] x) {
        int n = x.length;
        double sum = 0.0, sumSq = 0.0;
        for (double v : x) {
            sum += v;
            sumSq += v * v;
        }
        return (sumSq - (sum * sum) / n) * 2 / (n - 1);
    }


    public static double nextGamma(Random rand, double shape, double scale) {
        if (shape < 1) {
            double c = 1.0 / shape;
            double d = (1.0 - shape) * Math.pow(shape, shape / (1.0 - shape));
            while (true) {
                double u = rand.nextDouble();
                double v = rand.nextDouble();
                double z = -Math.log(u);
                if (v <= Math.pow(z, shape - 1) * Math.exp(-z)) {
                    return scale * z;
                }
            }
        } else {
            double d = shape - 1.0 / 3.0;
            double c = 1.0 / Math.sqrt(9.0 * d);
            while (true) {
                double x = rand.nextGaussian();
                double v = 1.0 + c * x;
                if (v <= 0) continue;
                v = v * v * v;
                double u = rand.nextDouble();
                if (u < 1 - 0.0331 * Math.pow(x, 4)) return scale * d * v;
                if (Math.log(u) < 0.5 * x * x + d * (1 - v + Math.log(v))) return scale * d * v;
            }
        }
    }

    public static void reportSystemInfo() {
        System.out.println("=== Java Runtime Benchmark Environment ===");
        System.out.println("OS: " + System.getProperty("os.name") + " " + System.getProperty("os.version"));
        System.out.println("Architecture: " + System.getProperty("os.arch"));
        System.out.println("Java Version: " + System.getProperty("java.version"));
        System.out.println("Available Processors: " + Runtime.getRuntime().availableProcessors());
        System.out.println("Max Memory (MB): " + Runtime.getRuntime().maxMemory() / (1024 * 1024));
        System.out.println("==========================================");
    }

    @Test
    public void testVarianceEstimators() {
        double[] testData = {2.0, 4.0, 4.0, 4.0, 5.0, 5.0, 7.0, 9.0};
        double unbiased = optimizedUnbiasedVariance(testData);
        double biased = optimizedBiasedVariance(testData);
        double popVar = optimizedPopulationVariance(testData);
        double barOpt = optimizedBariance(testData);
        double altBar = optimizedAltBariance(testData);

        assertApproximatelyEqual(biased, popVar, "Biased vs Population");
        assertApproximatelyEqual(unbiased, barOpt / 2, "Unbiased vs Optimized Barance");
        assertApproximatelyEqual(unbiased, altBar / 2, "Unbiased vs Alt Barance");
    }

    public static void assertApproximatelyEqual(double a, double b, String label) {
        double tol = 1e-10;
        if (Math.abs(a - b) > tol) {
            throw new AssertionError(label + " mismatch: " + a + " vs " + b);
        } else {
            System.out.println(label + " OK: " + a + " ≈ " + b);
        }
    }
}

