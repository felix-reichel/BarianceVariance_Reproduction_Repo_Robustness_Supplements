# Bariance and Variance Estimators: Reproduction Repository & Robustness Supplements // Speeding UP Your Sample Variance in Big-Data Environments!  
Deterministic Reproduction of Results from [https://doi.org/10.48550/arXiv.2503.22333](https://doi.org/10.48550/arXiv.2503.22333)

This repository contains a Python 3.11 script (executed in a virtualized cloud environment) for deterministic simulation of *a-denominator* based sample variance estimators. It computes Bias², Variance, and Mean Squared Error (MSE), alongside bootstrapped standard errors (SEs), as a robustness check in support of **Ch. 7** (Simulation Study on MSE across Denominator Values) in **v5**.  
Additionally, empirical runtime plots assess the performance of the Bariance estimator under varied data conditions, such as large-scale gamma-distributed samples, in a local Java SE 21 environment—supporting **Ch. 9** (Empirical Runtime Analysis) and **Appendix C** (Java Runtime Robustness Checks) of **v5**.

---

**Code**  
- `MSEVarEstimsSimulationsWithBootstrapedSEs.py`  
  Main simulation script. Evaluates multiple sample variance estimators using a fixed-seed normal distribution N(0,1). Calculates Bias², Variance, MSE, and bootstrapped confidence intervals. Python 3.11.

![variance_estimators_real_data_ci_plot](https://github.com/user-attachments/assets/9addc5dc-b3e4-4a73-a8d1-a2b3d1009572)

---

**Runtime Figures**

### Local Execution Environment:
- Java Runtime: SE 21  
- OS: macOS 13.0  
- Architecture: aarch64  
- Cores: 10 (single-threaded benchmark)  
- Max JVM Memory: 4096 MB  

These runtime plots replicate and extend the analysis in **Ch. 9** and **Appendix C** (Robustness Checks) of **v5**:

![ATT_Gamma_1k_trials](ATT_Gamma_1k_trials.png)

<!-- Images from the 'out/' folder -->
![coef_plot_runtime_1k_trials_iqr_removed](out/coef_plot_runtime_1k_trials_iqr_removed.png)  
![coef_plot_runtime_100k_gamma_trials](out/coef_plot_runtime_100k_gamma_trials.png)  
![coef_plot_runtime_100k_gamma_trialsv2](out/coef_plot_runtime_100k_gamma_trialsv2.png)  
![empRuntimeNormalUnseeded1_100trials](out/empRuntimeNormalUnseeded1_100trials.png)  
![empRuntimeNormalUnseeded2_100trials](out/empRuntimeNormalUnseeded2_100trials.png)  
![empRuntimeTestBigNormalUnseeded_100trials](out/empRuntimeTestBigNormalUnseeded_100trials.png)  
![empRuntimeTestSmallNormalUnseeded_100trials](out/empRuntimeTestSmallNormalUnseeded_100trials.png)  
![FAST_5_GAMMA_DIST_GUCCI_Plot](out/FAST_5_GAMMA_DIST_GUCCI_Plot.png)  
![FAST_5_GAMMA_Dist](out/FAST_5_GAMMA_Dist.png)  
![gamma_1ktrials_n10k_runtime_density](out/gamma_1ktrials_n10k_runtime_density.png)  
![Garbage_BigEightEstimatorsPlot](out/Garbage_BigEightEstimatorsPlot.png)  
![logLinearModelRegressions](out/logLinearModelRegressions.png)  
![LogLinearModelRuntimeRegressions](out/LogLinearModelRuntimeRegressions.png)  
![runtime_density_gamma_1k_grid_display__BEST_SO_FAR](out/runtime_density_gamma_1k_grid_display__BEST_SO_FAR.png)  
![runtime_density_gamma_100ktrials_grid](out/runtime_density_gamma_100ktrials_grid.png)  
![runtime_density_gamma_100ktrials_largest](out/runtime_density_gamma_100ktrials_largest.png)  
![runtime_density_gamma_highres_Seeded_with_mean_100trials](out/runtime_density_gamma_highres_Seeded_with_mean_100trials.png)

---

**Requirements**

### 🐍 Python 3.11+
Install dependencies via:

```bash
pip install numpy matplotlib seaborn scipy
```

### ♨️ Java 21+

SE 21
