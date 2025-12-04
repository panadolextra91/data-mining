## Football (Soccer) Match Outcome Prediction – Java Project

**Goal**: Predict match outcome (Home Win, Draw, Away Win) on the European Soccer Database and compare:
- **Betting odds** as predictors
- **Team attributes** (aggression, passing, shooting, defence, build-up speed)
- **Recent form** (rolling last-5-match goals for/against, goal diff, win rate)
- **Combined & enhanced feature sets** (odds + attributes + form + ratios/differences)

---

### 1. Introduction

This project applies data mining and machine learning techniques to predict football (soccer) match outcomes (Home Win, Draw, Away Win) using the European Soccer Database (Kaggle). The work focuses on comparing three types of information as predictors:

- **Betting odds** from bookmakers.
- **Team attributes** (aggression, passing, shooting, defence, build-up speed).
- **Recent team form** (rolling last‑5‑match statistics).

In classification problems, a model learns a mapping from feature vectors \(x\) to discrete labels \(y\). Here, labels are match outcomes: `home`, `draw`, `away`. We use:

- **Logistic Regression (multiclass)** from the Smile Java library as the main interpretable classifier.
- **RandomForest** from Weka as a powerful ensemble model and as the official 10‑fold cross‑validated baseline.

We also conduct **sequence mining** on per‑team sequences of results (`W`, `D`, `L`) to discover frequent patterns (e.g. winning or losing streaks).

**Objectives:**

- Build a complete Java data mining pipeline (data access, preprocessing, feature engineering, modeling, evaluation, visualization).
- Compare predictive power of odds, team attributes, form, and combined feature sets.
- Achieve high accuracy and robust evaluation via 10‑fold cross‑validation and sequence analysis.

Dataset used: **European Soccer Database (Kaggle, `database.sqlite`)**.

---

### 2. Data Pre‑Processing

#### 2.1 Raw Data Overview

- **Source**: Kaggle European Soccer Database (`database.sqlite`).
- **Key tables**:
  - `Match`: match results, team IDs, Bet365 odds (`B365H`, `B365D`, `B365A`), date.
  - `Team_Attributes`: team-level attributes (build-up play, chance creation, defence).
- **Instances**:
  - ~25,000 European league matches (20,000 sampled and ordered by date).
- **Initial attributes used per match**:
  - `home_team_goal`, `away_team_goal` → label.
  - `B365H`, `B365D`, `B365A` → betting odds.
  - Team attributes (for both home and away):
    - `buildUpPlaySpeed`
    - `chanceCreationPassing`
    - `chanceCreationShooting`
    - `defencePressure`
    - `defenceAggression`
  - Date and team IDs used for rolling form features.

#### 2.2 Data Cleaning

Within `MatchRepository` (SQL + Java):

- **Filtering missing data**:
  - Only matches with non-null Bet365 odds:  
    `WHERE m.B365H IS NOT NULL AND m.B365D IS NOT NULL AND m.B365A IS NOT NULL`.
  - Require non-null key team attributes:  
    `AND th.buildUpPlaySpeed IS NOT NULL AND ta.buildUpPlaySpeed IS NOT NULL`.

- **Handling missing attributes**:
  - Use `COALESCE(attr, 50.0)` for team attributes to replace nulls with a neutral mid‑range value (0–100 scale).

- **Duplicates**:
  - Not observed in the `Match` primary key; no explicit deduplication needed.

#### 2.3 Data Transformation

All transformation logic is in Java:

- **Label encoding**:
  - Outcome classes:
    - Home Win → `0`
    - Draw → `1`
    - Away Win → `2`

- **Team attributes**:
  - For each team (home/away), we construct:
    - `overall` ≈ `buildUpPlaySpeed + chanceCreationPassing + defencePressure`
    - `aggression` = `defenceAggression`
    - `passing` = `chanceCreationPassing`
    - `shooting` = `chanceCreationShooting`
    - `defence` = `defencePressure`
    - `buildUpSpeed` = `buildUpPlaySpeed`

- **Rolling form features** (per team, window = 5 matches):
  - For each match, before updating with that match:
    - `avgGoalsFor`, `avgGoalsAgainst`
    - `avgGoalDiff = avgGoalsFor – avgGoalsAgainst`
    - `winRate` where win=1, draw=0.5, loss=0
  - Defaults for teams with fewer than 5 prior matches:
    - `avgGoalsFor ≈ 1.4`, `avgGoalsAgainst ≈ 1.4`, `winRate ≈ 0.33`.

- **Feature sets (built in `FeatureBuilder`)**:
  - `Odds`:
    - `[homeOdds, drawOdds, awayOdds]` (3 dims).
  - `Team`:
    - `[homeOverall, awayOverall]` (2 dims).
  - `Aggression`, `Passing`, `Shooting`:
    - Each 2‑dim (home, away).
  - `ComprehensiveTeam`:
    - Home + away attributes (12 dims).
  - `Form`:
    - `[homeGF, homeGA, homeGD, homeWinRate, awayGF, awayGA, awayGD, awayWinRate]` (8 dims).
  - `Combined`:
    - Odds (3) + attributes (12) + form (8) = 23 dims.
  - `EnhancedCombined` (40 dims):
    - Raw odds + normalized odds (inverse probabilities).
    - Odds ratios and attribute/form differences and ratios (e.g. home/away overall).

- **Normalization**:
  - For each feature set:
    - Compute **min‑max** per feature on the **training set**.
    - Scale both train and test to [0, 1] using training min‑max.
  - Same normalization is applied to user inputs in interactive mode.

**Final cleaned dataset**: for each match, a feature vector of chosen dimension and label in {0,1,2}.

---

### 3. Classification / Prediction Algorithm

#### 3.1 Model Selection

- **Smile (Java) models**:
  - **Multiclass Logistic Regression**:
    - Main classifier for all feature sets (Odds, Team, Form, Combined, EnhancedCombined, etc.)
    - Pros: interpretable, fast, good probabilistic baseline.

- **Weka models**:
  - **RandomForest** (Weka `RandomForest`) on the `Combined` feature set (23 dims).
    - Trained/evaluated via **10‑fold cross‑validation** on the full 20,000‑match sample.
    - Satisfies the requirement to use Weka and k‑fold CV.

- **LDA (Smile)**:
  - Additional linear model on `Odds` features, mainly for comparison.

#### 3.2 Implementation Process

- **Data access**:
  - `SQLiteConnectionFactory` builds a JDBC URL from `Config.SQLITE_DB_PATH`.
  - `MatchRepository` executes SQL joins between `Match` and `Team_Attributes`, computes labels and rolling form features.

- **Smile models** (`ModelTrainer`):
  - `trainLogistic(name, xTrain, yTrain, xTest, yTest)`:
    - `LogisticRegression.fit(xTrain, yTrain)`.
    - Predictions on `xTest`.
    - Metrics via custom `ClassificationMetrics`.
  - `trainLDA(...)`:
    - `LDA.fit(xTrain, yTrain)` → predictions + metrics.

- **Weka integration**:
  - `ArffExporter` exports the full dataset (`records`) with **Combined** features to `target/soccer_combined.arff`.
  - `WekaRunner.runRandomForestOnCombined`:
    - Loads ARFF into `Instances`.
    - Sets class index to the last attribute.
    - Runs `RandomForest` with 10‑fold cross-validation:
      - `eval.crossValidateModel(rf, data, 10, new Random(1));`
    - Prints summary, per-class metrics, and confusion matrix.

#### 3.3 Results (Smile logistic, 80/20 split)

Key results on the 20,000‑match sample (16,000 train / 4,000 test):

- **Odds only (3 dims) – Logistic Regression**:
  - Accuracy ≈ **52.70%**.
  - Confusion: predicts many home wins correctly; draws are almost never predicted (F1 ≈ 0).

- **Combined (odds + attributes + form, 23 dims) – Logistic Regression**:
  - Accuracy ≈ **95.15%**.
  - Per-class F1:
    - Home Win: ≈ 0.97
    - Draw: ≈ 0.92
    - Away Win: ≈ 0.96

- **EnhancedCombined (40 dims) – Logistic Regression**:
  - Accuracy ≈ **95.13%** (very similar to Combined).

- **Form only (8 dims) – Logistic Regression**:
  - Accuracy ≈ **94.88%**.

- **ComprehensiveTeam only (12 dims, no odds, no form) – Logistic Regression**:
  - Accuracy ≈ **48.28%**.

- **LDA on Odds**:
  - Accuracy ≈ **52.48%**, comparable to logistic on Odds.

Figures (in `analysis/`):

- Accuracy comparison: `analysis/accuracy_comparison.png`
- Confusion matrices:
  - `analysis/confusion_matrices/cm_LogisticRegression_Odds.png`
  - `analysis/confusion_matrices/cm_LogisticRegression_Combined.png`
  - `analysis/confusion_matrices/cm_LogisticRegression_EnhancedCombined.png`
  - `analysis/confusion_matrices/cm_LogisticRegression_Form.png`
  - `analysis/confusion_matrices/cm_LogisticRegression_ComprehensiveTeam.png`

---

### 4. Improvement of Results

#### 4.1 Methodology

Starting from an **odds-only** baseline, several improvements were applied:

- **Additional features**:
  - Team attributes: aggression, passing, shooting, defence, build-up speed.
  - Form features: rolling averages over the last 5 matches for each team.
  - EnhancedCombined: added normalized odds, feature differences, and ratios (e.g. home/away overall).

- **Model choice**:
  - Kept Logistic Regression (Smile) for all feature sets for a consistent comparison.
  - Added Weka RandomForest with 10‑fold CV as a high‑capacity ensemble.

- **Sequence mining**:
  - Built per-team sequences of outcomes (`W`, `D`, `L`) ordered by date.
  - Mined frequent 3‑grams (n‑gram patterns) to characterize common streaks, complementing form features.

#### 4.2 Comparison of Results

**Baseline vs improved**:

- Odds-only Logistic Regression: **52.70%** accuracy.
- ComprehensiveTeam only: **48.28%** (team attributes alone worse than odds).
- Form only: **94.88%**.
- Combined (odds + attributes + form): **95.15%**.
- EnhancedCombined: **95.13%**.

This clearly shows:

- Betting odds by themselves are already much better than random guessing (≈33%), but:
- Adding **form** (and attributes) increases accuracy by roughly **+42 percentage points**, up to ~95%.
- Enhanced feature engineering (ratios/differences) gives marginal gains beyond Combined.

**Sequence mining** results (top 10 frequent 3‑gram patterns across all team sequences):

- `WWW` – 3677 occurrences  
- `LLL` – 3170  
- `LWL`, `WLW`, `LLW`, `WLL`, `LWW`, `WWL` – various mixed streaks  
- `LDL`, `DLL` – common draw/loss combinations

This confirms that win and loss streaks are common; the rolling form features used in the model essentially encode similar information numerically.

---

### 5. Model Evaluation

#### 5.1 Performance Metrics

**Smile Logistic Regression (80/20 split)**:

- For the **Combined** feature set:
  - Accuracy: **95.15%**
  - Per-class F1 (approximate):
    - Home Win: **0.97**
    - Draw: **0.92**
    - Away Win: **0.96**

- For the **Odds** feature set:
  - Accuracy: **52.70%**
  - F1(Home): ~0.67, F1(Draw)=0, F1(Away)~0.41.

Full metrics for all sets are logged and exported in `target/metrics.csv`, and summarized visually in `analysis/accuracy_comparison.png`.

**Weka RandomForest (Combined, 10‑fold cross‑validation, 20,000 instances)**:

- Correctly classified instances: **97.89%**.
- Weighted Avg metrics:
  - Precision ≈ 0.979
  - Recall ≈ 0.979
  - F‑Measure ≈ 0.979
  - ROC area ≈ 0.999
- Per-class F‑Measure:
  - Home: 0.982
  - Draw: 0.975
  - Away: 0.976
- Confusion matrix (a=home, b=draw, c=away):
  - home: 9372 correctly classified, 73→draw, 70→away.
  - draw: 4962 correctly classified, 95→home, 54→away.
  - away: 5243 correctly classified, 97→home, 34→draw.

#### 5.2 Analysis of Results

- **Odds vs attributes vs form**:
  - Odds-only and LDA Odds both around 52–53% → bookmaker odds encode non‑trivial information but are not sufficient for very high accuracy.
  - Team attributes alone are not predictive enough (≈48%).
  - Rolling form is extremely powerful; combined with odds and attributes it yields ~95%+ accuracy.

- **Class balance**:
  - Draw is the hardest class: in odds-only models, it is almost never predicted.
  - In Combined/EnhancedCombined, Draw F1 exceeds 0.92, meaning the improved features and models handle draws much better.

- **RandomForest vs Logistic Regression**:
  - RF (Weka) reaches ~97.9% CV accuracy vs ~95% for single-split logistic regression, suggesting non‑linear interactions and higher‑order effects are present.
  - However, both agree on the importance of form and odds.

- **Sequence patterns**:
  - Frequent `WWW` and `LLL` patterns and mixed `WLW/LWL` sequences corroborate that teams experience runs of good or bad form; the rolling statistics capture this and make the models highly accurate.

---

### 6. Conclusions

- The project successfully implemented a Java-based data mining pipeline that:
  - Reads and preprocesses the European Soccer Database.
  - Engineers rich features from betting odds, team attributes, and recent form.
  - Trains and evaluates multiple classifiers (Smile Logistic Regression, LDA, Weka RandomForest).
  - Performs sequence mining over match outcomes.

- **Key findings**:
  - **Form + odds** are far more predictive than either team attributes or odds alone.
  - Logistic Regression with the Combined feature set achieves ≈**95%** accuracy, while Weka RandomForest with 10‑fold CV achieves ≈**97.9%**.
  - Sequence mining reveals common winning and losing streaks, which align with the strong contribution of form features.

- **Limitations / future work**:
  - Use more rigorous **time-aware evaluation** (train on earlier seasons, test on later seasons) instead of a random 80/20 split.
  - Incorporate **player-level attributes**, injuries, and league context.
  - Explore additional models (Gradient Boosted Trees, XGBoost4J, deep learning) and automated hyperparameter tuning.
  - Integrate the best model into a REST service or simple prediction web UI.

---

### 7. References

- **Dataset**
  - Hugo Mathien, *European Soccer Database*, Kaggle.  
    Dataset: `https://www.kaggle.com/datasets/hugomathien/soccer`

- **Libraries / Tools**
  - Smile (Statistical Machine Intelligence and Learning Engine): `https://haifengl.github.io/`
  - Weka: `https://www.cs.waikato.ac.nz/ml/weka/`
  - SQLite JDBC Driver: `https://github.com/xerial/sqlite-jdbc`
  - Python, pandas, matplotlib, seaborn for plotting.

- **Additional**
  - Course materials for IT160IU – Data Mining.
  - Online tutorials on multiclass logistic regression, RandomForest, and sequence mining (general references).

---

### 8. How to Build and Run the Code

#### 8.1 Prerequisites
- Java 17+
- Maven 3+
- Python 3 (for plots) with `pandas`, `matplotlib`, `seaborn` (optional, for analysis).
- Download the Kaggle dataset (European Soccer Database) and place `database.sqlite` at the project root.

#### 8.2 Configure the SQLite path
Edit `Config.java`:

```java
public static final String SQLITE_DB_PATH = "/absolute/path/to/database.sqlite";
```

Or override at runtime:

```bash
mvn exec:java -Dexec.mainClass="com.example.soccer.Main" \
  -Dsoccer.db.path=/absolute/path/to/database.sqlite
```

#### 8.3 Build and run

```bash
cd /Users/huynhngocanhthu/data-mining
mvn clean package
mvn exec:java -Dexec.mainClass="com.example.soccer.Main"
```

This will:

- Train all Smile models (Logistic Regression and LDA) on multiple feature sets.
- Export `target/soccer_combined.arff` for Weka.
- Run Weka RandomForest with 10‑fold cross-validation on Combined features.
- Run sequence mining for 3-gram patterns of W/D/L.
- Export metrics to `target/metrics.csv`.
- Enter interactive prediction mode.

#### 8.4 Generate plots (optional)

```bash
cd /Users/huynhngocanhthu/data-mining
python analysis/plots.py
```

This produces:

- Accuracy comparison: `analysis/accuracy_comparison.png`
- Confusion matrices: `analysis/confusion_matrices/*.png`
