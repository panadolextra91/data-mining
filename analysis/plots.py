import os

import matplotlib.pyplot as plt
import pandas as pd
import seaborn as sns


def load_metrics(csv_path: str) -> pd.DataFrame:
    """Load metrics CSV produced by the Java pipeline."""
    if not os.path.exists(csv_path):
        raise FileNotFoundError(f"Metrics file not found: {csv_path}. Run the Java pipeline first.")
    df = pd.read_csv(csv_path)
    return df


def plot_accuracy(df: pd.DataFrame, output_path: str) -> None:
    """Plot accuracy by model type and feature set."""
    plt.figure(figsize=(10, 6))
    # Combine modelType and featureSet into a single label for readability
    df = df.copy()
    df["model_feat"] = df["modelType"] + " - " + df["featureSet"]
    df_sorted = df.sort_values("accuracy", ascending=False)
    sns.barplot(data=df_sorted, x="accuracy", y="model_feat", palette="viridis")
    plt.xlabel("Accuracy")
    plt.ylabel("Model / Feature Set")
    plt.title("Model Accuracy Comparison")
    plt.xlim(0, 1.0)
    plt.tight_layout()
    plt.savefig(output_path, dpi=200)
    plt.close()


def plot_confusion_matrices(df: pd.DataFrame, output_dir: str) -> None:
    """Generate heatmap images for confusion matrices of each model."""
    os.makedirs(output_dir, exist_ok=True)
    class_labels = ["Home Win", "Draw", "Away Win"]

    for _, row in df.iterrows():
        cm = [
            [row["cm_hh"], row["cm_hd"], row["cm_ha"]],
            [row["cm_dh"], row["cm_dd"], row["cm_da"]],
            [row["cm_ah"], row["cm_ad"], row["cm_aa"]],
        ]
        plt.figure(figsize=(4, 3))
        sns.heatmap(
            cm,
            annot=True,
            fmt=".0f",
            cmap="Blues",
            xticklabels=class_labels,
            yticklabels=class_labels,
        )
        plt.title(f"Confusion Matrix: {row['modelType']} - {row['featureSet']}")
        plt.ylabel("True")
        plt.xlabel("Predicted")
        plt.tight_layout()
        fname = f"cm_{row['modelType']}_{row['featureSet']}.png".replace(" ", "_")
        plt.savefig(os.path.join(output_dir, fname), dpi=200)
        plt.close()


def main():
    metrics_path = os.path.join("target", "metrics.csv")
    df = load_metrics(metrics_path)

    # Plot accuracy comparison
    acc_path = os.path.join("analysis", "accuracy_comparison.png")
    plot_accuracy(df, acc_path)
    print(f"Saved accuracy comparison plot to {acc_path}")

    # Plot confusion matrices for each model
    cm_dir = os.path.join("analysis", "confusion_matrices")
    plot_confusion_matrices(df, cm_dir)
    print(f"Saved confusion matrix plots to {cm_dir}")


if __name__ == "__main__":
    main()


