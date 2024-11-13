package org.marcolore.datasetbuilderisw2.model;

public class AcumeClass {
    int instanceId;
    double predictedClassProbability;
    String actualClassLabel;
    int size;

    public AcumeClass(int instanceId, int size, double predictedClassProbability, String actualClassLabel) {
        this.instanceId = instanceId;
        this.predictedClassProbability = predictedClassProbability;
        this.size = size;
        this.actualClassLabel = actualClassLabel;
    }

    public int getInstanceId() {
        return instanceId;
    }

    public double getPredictedClassProbability() {
        return predictedClassProbability;
    }

    public String getActualClassLabel() {
        return actualClassLabel;
    }

    public int getSize() {
        return size;
    }
}
