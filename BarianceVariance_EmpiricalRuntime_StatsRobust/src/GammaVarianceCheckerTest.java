import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Random;

public class GammaVarianceCheckerTest {

    @Test
    public void testBarianceApproximatelyTwiceUnbiasedVariance() {
        long seed = 42L;
        Random rand = new Random(seed);
        int n = 10000;  // Sample size
        int numChecks = 10;  // Number of tests
        double tolerance = 1e-12; // fails at < 1e-13

        for (int i = 0; i < numChecks; i++) {
            double[] data = new double[n];
            for (int j = 0; j < n; j++) {
                data[j] = nextGamma(rand, 2.0, 2.0); // shape=2, scale=2
            }

            double var = unbiasedVariance(data);
            double bar = optimizedBariance(data);
            double expected = 2 * var;
            double diff = Math.abs(bar - expected);

            assertTrue(diff < tolerance,
                    String.format("Check %d failed: Var = %.6f, Bar = %.6f, Expected 2*Var = %.6f, Diff = %.6e",
                            i + 1, var, bar, expected, diff));
        }
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
                if (u < 1 - 0.0331 * x * x * x * x) return scale * d * v;
                if (Math.log(u) < 0.5 * x * x + d * (1 - v + Math.log(v))) return scale * d * v;
            }
        }
    }
}
