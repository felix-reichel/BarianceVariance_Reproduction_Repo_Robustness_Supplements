import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;

public class MainNormalDistUnseeded {

    static int[] sampleSizes = {1000, 5000, 10000, 20000, 100000, 200000, 400000, 1000000, 5000000};

    public static void main(String[] args) throws IOException {
        int trials = 100;
        Random rand = new Random();

        FileWriter writer = new FileWriter("./variance_bariance_runtime.csv");
        writer.write("SampleSize,Trial,Estimator,RuntimeNs\n");

        for (int n : sampleSizes) {
            for (int t = 0; t < trials; t++) {
                double[] data = new double[n];
                for (int i = 0; i < n; i++) {
                    data[i] = rand.nextGaussian();
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
        System.out.println("Benchmark complete. Data written to variance_bariance_runtime.csv");
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
