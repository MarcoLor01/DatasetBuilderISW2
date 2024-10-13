package org.marcolore.datasetbuilderisw2.controller;

import org.eclipse.jgit.revwalk.RevCommit;
import org.marcolore.datasetbuilderisw2.Main;
import org.marcolore.datasetbuilderisw2.model.JavaClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;

public class MetricsCalculatorController {

    private static final Logger logger = LoggerFactory.getLogger(MetricsCalculatorController.class);
    private final List<JavaClass> javaClassList;
    private final List<RevCommit> commitList;


    public MetricsCalculatorController(List<JavaClass> listOfClasses, List<RevCommit> listOfCommit) {
        javaClassList = listOfClasses;
        commitList = listOfCommit;
    }

    public void calculateMetrics(){
        calculateLoc();
        calculateNumberOfAuthors();
    }

    private void calculateNumberOfAuthors() {
        for(JavaClass javaClass : javaClassList){
            List<RevCommit> listOfCommit = javaClass.getListOfCommit();
            List<String> authorNames = new ArrayList<>();
            for (RevCommit revCommit : listOfCommit) {
                String authorName = revCommit.getAuthorIdent().getName();
                if(!authorNames.contains(authorName)){
                    authorNames.add(authorName);
                }
            }
            javaClass.setAuthorsNumber(authorNames.size());
        }
    }

    private void calculateLoc() {

        for (JavaClass javaClass : javaClassList) {
            int totalLoc = 0;
            String content = javaClass.getFileContent();
            String[] lines = content.split("\n");

            for (String line : lines) {
                String trimmedLine = line.trim();
                if (!trimmedLine.isEmpty() && !trimmedLine.startsWith("//") && !trimmedLine.startsWith("/*")) {
                    totalLoc++;
                }
            }
            javaClass.setLoc(totalLoc);
        }
    }
}

