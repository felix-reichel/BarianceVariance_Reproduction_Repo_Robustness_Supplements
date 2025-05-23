import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;

public class MainGammaDistSeeded {

    static int[] sampleSizes = {1000, 5000, 10000, 20000, 100000, 200000, 400000, 1000000, 5000000};

    public static void main(String[] args) throws IOException {
        int trials = 100;
        long seed = 42L;
        Random rand = new Random(seed);

        FileWriter writer = new FileWriter("./variance_bariance_runtime_gamma.csv");
        writer.write("SampleSize,Trial,Estimator,RuntimeNs\n");

        for (int n : sampleSizes) {
            for (int t = 0; t < trials; t++) {
                double[] data = new double[n];
                for (int i = 0; i < n; i++) {
                    data[i] = nextGamma(rand, 2.0, 2.0); // shape=2, scale=2
                }

                long start1 = System.nanoTime();
                double var = unbiasedVariance(data);
                long end1 = System.nanoTime();
                writer.write(n + "," + t + ",Unbiased," + (end1 - start1) + "\n");

                long start2 = System.nanoTime();
                double bar = optimizedBariance(data);
                long end2 = System.nanoTime();
                writer.write(n + "," + t + ",BarianceOpt," + (end2 - start2) + "\n");
            }
        }

        writer.close();
        System.out.println("Benchmark complete. Data written to variance_bariance_runtime_gamma.csv");
        reportSystemInfo();
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

    public static double optimizedBariance(double[] x) {
        int n = x.length;
        double sum = 0, sumSq = 0;
        for (double v : x) {
            sum += v;
            sumSq += v * v;
        }
        return (2.0 * sumSq / (n - 1)) - (2.0 * sum * sum) / (n * (n - 1));
    }

    // Gamma distribution using Marsaglia and Tsang's method for shape >= 1
    public static double nextGamma(Random rand, double shape, double scale) {
        if (shape < 1) {
            // Use Johnk's generator for shape < 1
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
                if (u < 1 - 0.0331 * x * x * x * x) return scale * d * v;
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
}
