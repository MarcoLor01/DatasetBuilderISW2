package org.marcolore.datasetbuilderisw2.controller;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.revwalk.RevCommit;
import org.marcolore.datasetbuilderisw2.model.JavaClass;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MetricsCalculatorController {

    private final List<JavaClass> javaClassList;
    private final List<RevCommit> commitList;
    private final GitController gitController;


    public MetricsCalculatorController(List<JavaClass> listOfClasses, List<RevCommit> listOfCommit, GitController controller) {
        javaClassList = listOfClasses;
        commitList = listOfCommit;
        gitController = controller;
    }

    public void calculateMetrics() throws GitAPIException, IOException {
        calculateLoc();
        calculateNumberOfAuthors();
        calculateNumberOfRevisions();
        calculateLocFromCommitMeasures();
        calculateNumberFix();
        calculateInstabilityClass();
    }

    private void calculateInstabilityClass() {
        //TODO: Implement this method
    }

    private void calculateLocFromCommitMeasures() throws IOException, GitAPIException {
        for(JavaClass javaClass : javaClassList){
            List<RevCommit> commits = javaClass.getListOfCommit();
            for(RevCommit commit : commits){
                if(commit.getParentCount() == 0){ //Initial commit, skip
                    continue;
                }
                gitController.calculateLocMeasures(commit, commit.getParent(0), javaClass);

            }
            if(!commits.isEmpty()) {
                javaClass.setAverageAddedLines(javaClass.getTotalAddedLines() / commits.size());
                javaClass.setAverageChurn(javaClass.getTotalChurn() / commits.size());
            }

        }

    }

    private void calculateNumberFix() {
        for (JavaClass javaClass : javaClassList) {
            int count = 0;
            for (RevCommit commit : javaClass.getListOfCommit()) {
                if (commitList.contains(commit)) {
                    count++;
                }
            }
            javaClass.setNumberFix(count);
        }
    }

    private void calculateNumberOfRevisions() {
        for(JavaClass javaClass : javaClassList){
            javaClass.setRevisionsNumber(javaClass.getListOfCommit().size());
        }
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

