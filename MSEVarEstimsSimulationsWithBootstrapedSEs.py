
import numpy as np
import pandas as pd
from scipy.stats import t

np.random.seed(42)

# Params
n = 5
true_variance = 10
num_simulations = 10000
n_bootstrap = 200
a_values = np.arange(3.5, 9.0, 0.5)

X = np.random.normal(0, np.sqrt(true_variance), size=(num_simulations, n))
X_bar = X.mean(axis=1)
squared_deviations = (X - X_bar[:, None]) ** 2
sum_squared = squared_deviations.sum(axis=1)

results = []

for a in a_values:
    est_var = sum_squared / a
    bias = np.mean(est_var) - true_variance
    bias_sq = bias ** 2
    variance = np.var(est_var, ddof=1)
    mse = bias_sq + variance

    # Bootstrap resampling
    boot_est_vars = []
    for _ in range(n_bootstrap):
        indices = np.random.choice(num_simulations, size=num_simulations, replace=True)
        sample = X[indices]
        sample_means = sample.mean(axis=1, keepdims=True)
        sample_vars = ((sample - sample_means) ** 2).sum(axis=1) / a
        boot_est_vars.append(sample_vars)
    boot_est_vars = np.array(boot_est_vars)  # Shape: (n_bootstrap, num_simulations)

    boot_means = boot_est_vars.mean(axis=1)
    boot_bias_sq = (boot_means - true_variance) ** 2
    boot_variance = np.var(boot_est_vars, axis=1, ddof=1)
    boot_mse = boot_bias_sq + boot_variance

    # Compute 95% CI using t-distribution
    def mean_ci(data):
        mean = np.mean(data)
        se = np.std(data, ddof=1) / np.sqrt(len(data))
        h = se * t.ppf(0.975, df=len(data)-1)
        return mean, (mean - h, mean + h)

    _, bias_sq_ci = mean_ci(boot_bias_sq)
    _, variance_ci = mean_ci(boot_variance)
    _, mse_ci = mean_ci(boot_mse)

    results.append({
        "a": a,
        "Bias^2": bias_sq,
        "Bias^2 CI": bias_sq_ci,
        "Variance": variance,
        "Variance CI": variance_ci,
        "MSE": mse,
        "MSE CI": mse_ci
    })

df_results_boot = pd.DataFrame(results)
import ace_tools as tools; tools.display_dataframe_to_user(name="Bootstrapped CIs Simulation Results", dataframe=df_results_boot)




   a    Bias^2                                      Bias^2 CI   Variance  \
0  3.5  1.977055        (1.9661874888911155, 2.033167512692707)  61.985197   
1  4.0  0.000387  (0.0045682236757243685, 0.006766427021929488)  47.457417   
2  4.5  1.273752       (1.2609443374792748, 1.2993371517020835)  37.497218   
3  5.0  4.063230         (4.015783393958433, 4.078094934106376)  30.372747   
4  5.5  7.516298         (7.475019715072768, 7.542442926060553)  25.101444   

                               Variance CI        MSE  \
0   (61.84827818682507, 62.19164811114051)  63.962253   
1   (47.255933477966565, 47.5145057207897)  47.457804   
2  (37.34379610414534, 37.568698677107136)  38.770970   
3   (30.29161673354343, 30.47121326124039)  34.435976   
4  (25.07214855110326, 25.201163467175277)  32.617741   

                                     MSE CI  
0     (63.82596051516996, 64.2133207843794)  
1   (47.262009838414585, 47.51976401103934)  
2  (38.634891460853495, 38.837884809580345)  
3   (34.356176965754756, 34.50053135709388)  
4   (32.592212140981395, 32.69856251843046)  





# Generate LaTeX table with CIs
latex_rows = []
for _, row in df_results_boot.iterrows():
    a = row["a"]
    bias2 = row["Bias^2"]
    bias2_ci = row["Bias^2 CI"]
    variance = row["Variance"]
    variance_ci = row["Variance CI"]
    mse = row["MSE"]
    mse_ci = row["MSE CI"]

    bold = ""
    if np.isclose(a, 4.0) or np.isclose(a, 5.0) or np.isclose(a, 6.0):
        bold = "\\textbf"

    row_latex = (
        f"{bold}{{{a:.1f}}} & "
        f"{bold}{{{bias2:.2f} [{bias2_ci[0]:.2f}, {bias2_ci[1]:.2f}]}} & "
        f"{bold}{{{variance:.2f} [{variance_ci[0]:.2f}, {variance_ci[1]:.2f}]}} & "
        f"{bold}{{{mse:.2f} [{mse_ci[0]:.2f}, {mse_ci[1]:.2f}]}} \\\\"
    )
    latex_rows.append(row_latex)

latex_table = r"""
\begin{table}[h]
\centering
\caption{Empirical Bias$^2$, Variance, and MSE with 95\% bootstrapped confidence intervals (200 resamples, seed=42). Bold rows indicate $a = n-1$, $n$, and $n+1$. Hardware: OMIT; Python 3.11.}
\label{tab:empirical-mse-ci-bootstrap}
\begin{tabular}{rccc}
\toprule
$a$ & Bias$^2$ [CI] & Variance [CI] & MSE [CI] \\
\midrule
""" + "\n".join(latex_rows) + r"""
\bottomrule
\end{tabular}
\end{table}
"""

# Generate plot with error bars
import matplotlib.pyplot as plt

a_vals = df_results_boot["a"]
mse_vals = df_results_boot["MSE"]
mse_ci = np.array(df_results_boot["MSE CI"].tolist())
mse_err = np.abs(mse_ci.T - mse_vals.values)

bias2_vals = df_results_boot["Bias^2"]
bias2_ci = np.array(df_results_boot["Bias^2 CI"].tolist())
bias2_err = np.abs(bias2_ci.T - bias2_vals.values)

var_vals = df_results_boot["Variance"]
var_ci = np.array(df_results_boot["Variance CI"].tolist())
var_err = np.abs(var_ci.T - var_vals.values)

plt.figure(figsize=(10, 6))
plt.errorbar(a_vals, mse_vals, yerr=mse_err, fmt='o-', label='MSE', capsize=4)
plt.errorbar(a_vals, bias2_vals, yerr=bias2_err, fmt='^--', label='Bias$^2$', capsize=4)
plt.errorbar(a_vals, var_vals, yerr=var_err, fmt='s--', label='Variance', capsize=4)

for a in [4.0, 5.0, 6.0]:
    plt.axvline(x=a, color='gray', linestyle=':', linewidth=1)
    plt.text(a, max(mse_vals) + 0.5, f'$a = {int(a)}$', rotation=90, verticalalignment='bottom')

plt.xlabel("Denominator $a$")
plt.ylabel("Value")
plt.title("Bias$^2$, Variance, and MSE with 95% CI\n(10,000 Simulations, 200 Bootstraps)")
plt.legend()
plt.grid(True)
plt.tight_layout()
plt.show()

latex_table


\begin{table}[h]
\centering
\caption{Empirical Bias$^2$, Variance, and MSE with 95\% bootstrapped confidence intervals (200 resamples, seed=42). Bold rows indicate $a = n-1$, $n$, and $n+1$. Hardware: OMIT; 1GB RAM, Python 3.11.}
\label{tab:empirical-mse-ci-bootstrap}
\begin{tabular}{rccc}
\toprule
$a$ & Bias$^2$ [CI] & Variance [CI] & MSE [CI] \\
\midrule
{3.5} & {1.98 [1.97, 2.03]} & {61.99 [61.85, 62.19]} & {63.96 [63.83, 64.21]} \\
\textbf{4.0} & \textbf{0.00 [0.00, 0.01]} & \textbf{47.46 [47.26, 47.51]} & \textbf{47.46 [47.26, 47.52]} \\
{4.5} & {1.27 [1.26, 1.30]} & {37.50 [37.34, 37.57]} & {38.77 [38.63, 38.84]} \\
\textbf{5.0} & \textbf{4.06 [4.02, 4.08]} & \textbf{30.37 [30.29, 30.47]} & \textbf{34.44 [34.36, 34.50]} \\
{5.5} & {7.52 [7.48, 7.54]} & {25.10 [25.07, 25.20]} & {32.62 [32.59, 32.70]} \\
\textbf{6.0} & \textbf{11.20 [11.16, 11.24]} & \textbf{21.09 [21.05, 21.17]} & \textbf{32.29 [32.27, 32.36]} \\
{6.5} & {14.89 [14.81, 14.90]} & {17.97 [17.92, 18.03]} & {32.86 [32.79, 32.87]} \\
{7.0} & {18.46 [18.46, 18.56]} & {15.50 [15.42, 15.51]} & {33.96 [33.94, 34.01]} \\
{7.5} & {21.88 [21.85, 21.94]} & {13.50 [13.48, 13.55]} & {35.37 [35.37, 35.44]} \\
{8.0} & {25.10 [25.06, 25.17]} & {11.86 [11.82, 11.89]} & {36.96 [36.93, 37.01]} \\
{8.5} & {28.13 [28.09, 28.20]} & {10.51 [10.45, 10.52]} & {38.64 [38.59, 38.67]} \\
\bottomrule
\end{tabular}
\end{table}





\begin{figure}[h]
\centering
\begin{tikzpicture}
\begin{axis}[
    width=14cm,
    height=12cm,
    xlabel={Denominator $a$},
    ylabel={Value},
    title={MSE, Bias$^2$, and Variance of Variance Estimator},
    legend style={at={(0.01,0.98)}, anchor=north west},
    grid=major,
    ymin=0, ymax=50
]

\addplot+[mark=*, thick, blue] coordinates {
(3.5, 63.96) (4.0, 47.46) (4.5, 38.77) (5.0, 34.44)
(5.5, 32.62) (6.0, 32.29) (6.5, 32.86) (7.0, 33.96)
(7.5, 35.37) (8.0, 36.96) (8.5, 38.64)
};
\addlegendentry{MSE}

\addplot+[mark=triangle*, thick, red, dashed] coordinates {
(3.5, 1.98) (4.0, 0.00) (4.5, 1.27) (5.0, 4.06)
(5.5, 7.52) (6.0, 11.20) (6.5, 14.89) (7.0, 18.46)
(7.5, 21.88) (8.0, 25.10) (8.5, 28.13)
};
\addlegendentry{Bias$^2$}

\addplot+[mark=square*, thick, green!60!black, dashed] coordinates {
(3.5, 61.99) (4.0, 47.46) (4.5, 37.50) (5.0, 30.37)
(5.5, 25.10) (6.0, 21.09) (6.5, 17.97) (7.0, 15.50)
(7.5, 13.50) (8.0, 11.86) (8.5, 10.51)
};
\addlegendentry{Variance}

\addplot[dotted, thick, black] coordinates {(4, 0) (4, 70)};
\addlegendentry{Divide by $n{-}1$}

\addplot[dotted, thick, gray] coordinates {(5, 0) (5, 70)};
\addlegendentry{Divide by $n$}

\addplot[dotted, thick, gray!50!white] coordinates {(6, 0) (6, 70)};
\addlegendentry{Divide by $n{+}1$}

\end{axis}
\end{tikzpicture}
\caption{Empirical MSE, Bias$^2$, and Variance of the sample variance estimator for $a \in [3.5, 8.5]$ and $n=5$ using 10,000 simulations and 200 bootstraps. Minimum MSE occurs between $a=5.5$ and $a=6.5$.}
\end{figure}



