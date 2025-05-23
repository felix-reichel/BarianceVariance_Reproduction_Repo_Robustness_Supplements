import org.junit.jupiter.api.Test;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;

public class GammaDistSeededBigEight {

    static int[] sampleSizes = {100, 500, 1000,2000,3000,5000}; // Smaller sample sizes for testing
    static int trials = 1000; // Reduced trials for performance
    static long seed = 42L;

    public static void main(String[] args) throws IOException {
        Random rand = new Random(seed);
        FileWriter writer = new FileWriter("./variance_estimators_gamma_big8_1ktrials_6samples.csv");
        writer.write("SampleSize,Trial,Estimator,RuntimeNs\n");

        for (int n : sampleSizes) {
            System.out.println("Running for sample size: " + n);
            for (int t = 0; t < trials; t++) {
                double[] data = new double[n];
                for (int i = 0; i < n; i++) {
                    data[i] = nextGamma(rand, 2.0, 2.0);
                }

                writer.write(n + "," + t + ",Unbiased," + time(() -> unbiasedVariance(data)) + "\n");
                writer.write(n + "," + t + ",Biased," + time(() -> biasedVariance(data)) + "\n");
                writer.write(n + "," + t + ",BarianceOpt," + time(() -> optimizedBariance(data)) + "\n");
                writer.write(n + "," + t + ",NaivePairwise," + time(() -> naivePairwiseVariance(data)) + "\n");
                writer.write(n + "," + t + ",UnbiasedPairwise," + time(() -> unbiasedPairwise(data)) + "\n");
                writer.write(n + "," + t + ",BiasedPairwise," + time(() -> biasedPairwise(data)) + "\n");
                writer.write(n + "," + t + ",PopulationVar," + time(() -> populationVariance(data)) + "\n");
                writer.write(n + "," + t + ",AltBariance," + time(() -> altBariance(data)) + "\n");
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

    public static double unbiasedVariance(double[] x) {
        int n = x.length;
        double mean = 0;
        for (double v : x) mean += v;
        mean /= n;

        double sumSq = 0;
        for (double v : x) sumSq += (v - mean) * (v - mean);
        return sumSq / (n - 1);
    }

    public static double biasedVariance(double[] x) {
        int n = x.length;
        double mean = 0;
        for (double v : x) mean += v;
        mean /= n;

        double sumSq = 0;
        for (double v : x) sumSq += (v - mean) * (v - mean);
        return sumSq / n;
    }

    public static double optimizedBariance(double[] x) {
        int n = x.length;
        double sum = 0, sumSq = 0;
        for (double v : x) {
            sum += v;
            sumSq += v * v;
        }
        return (2.0 * sumSq / (n - 1)) - (2.0 * sum * sum) / (n * (n - 1));
    }

    public static double altBariance(double[] x) {
        int n = x.length;
        double sum = 0, sumSq = 0;
        for (double v : x) {
            sum += v;
            sumSq += v * v;
        }
        return (sumSq - (sum * sum) / n) * 2 / (n - 1);
    }

    public static double naivePairwiseVariance(double[] x) {
        int n = x.length;
        double sum = 0;
        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                double diff = x[i] - x[j];
                sum += diff * diff;
            }
        }
        return sum * 2 / (n * (n - 1));
    }

    public static double unbiasedPairwise(double[] x) {
        int n = x.length;
        return naivePairwiseVariance(x);
    }

    public static double biasedPairwise(double[] x) {
        return naivePairwiseVariance(x) * ((double)(x.length - 1) / x.length);
    }

    public static double populationVariance(double[] x) {
        int n = x.length;
        double mean = 0;
        for (double v : x) mean += v;
        mean /= n;

        double sumSq = 0;
        for (double v : x) sumSq += (v - mean) * (v - mean);
        return sumSq / n;
    }

    // Gamma generator
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
        double[] testData = {2.0, 4.0, 4.0, 4.0, 5.0, 5.0, 7.0, 9.0}; // Known data
        int n = testData.length;

        double unbiased = unbiasedVariance(testData);
        double biased = biasedVariance(testData);
        double barOpt = optimizedBariance(testData);
        double altBar = altBariance(testData);
        double pairwise = naivePairwiseVariance(testData);
        double pairUnbiased = unbiasedPairwise(testData);
        double pairBiased = biasedPairwise(testData);
        double popVar = populationVariance(testData);

        System.out.println("=== Variance Estimator Test ===");
        System.out.println("Unbiased variance:       " + unbiased);
        System.out.println("Biased variance:         " + biased);
        System.out.println("Optimized barance:       " + barOpt);
        System.out.println("Alternative barance:     " + altBar);
        System.out.println("Naive pairwise:          " + pairwise);
        System.out.println("Unbiased pairwise:       " + pairUnbiased);
        System.out.println("Biased pairwise:         " + pairBiased);
        System.out.println("Population variance:     " + popVar);
        System.out.println("================================");

        assertApproximatelyEqual(unbiased, pairwise/2, "Unbiased vs Pairwise");
        assertApproximatelyEqual(biased, pairBiased/2, "Biased vs Pairwise");
        assertApproximatelyEqual(biased, popVar, "Biased vs Population");
        assertApproximatelyEqual(unbiased, barOpt/2, "Unbiased vs Optimized Barance");
        assertApproximatelyEqual(unbiased, altBar/2, "Unbiased vs Alt Barance");
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
