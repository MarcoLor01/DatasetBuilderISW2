package org.marcolore.datasetbuilderisw2.controller;

import org.marcolore.datasetbuilderisw2.utility.WekaUtility;
import weka.core.Instances;

public class WekaController {
    private String project;
    private int iteration;

    public WekaController(String project, int iteration) {
        this.project = project;
        this.iteration = iteration;
    }

    public void Classify(){

        for(int i = 1; i < iteration ; i++) {
            Instances trainingSet = WekaUtility.convertData(project, i, "Training");
            Instances testingSet = WekaUtility.convertData(project, i, "Testing");
        }
    }

}
