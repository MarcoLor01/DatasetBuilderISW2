package org.marcolore.datasetbuilderisw2.model;

import org.eclipse.jgit.revwalk.RevCommit;

import java.util.ArrayList;
import java.util.List;

public class JavaClass {
    private String className;
    private List<RevCommit> listOfCommit = new ArrayList<>();
    private Release release;
    private String fileContent;
    private int loc;
    private int authorsNumber;
    private int revisionsNumber;
    private int touchedLoc;
    private int totalAddedLines;
    private float averageAddedLines;
    private int maxAddedLines;
    private int totalChurn;
    private float averageChurn;
    private int maxChurn;
    private int numberFix;
    private int cyclomaticComplexity;
    private double daysBetweenCommits;
    private boolean buggy;
    private List<RevCommit> fixCommits = new ArrayList<>();

    public JavaClass(String className, Release release, String fileContent) {
        this.className = className;
        this.release = release;
        this.fileContent = fileContent;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public List<RevCommit> getListOfCommit() {
        return listOfCommit;
    }

    public void setListOfCommit(List<RevCommit> listOfCommit) {
        this.listOfCommit = listOfCommit;
    }

    public void addCommitToList(RevCommit commit) {
        this.listOfCommit.add(commit);
    }

    public Release getRelease() {
        return release;
    }

    public void setRelease(Release release) {
        this.release = release;
    }

    public String getFileContent() {
        return fileContent;
    }

    public void setFileContent(String fileContent) {
        this.fileContent = fileContent;
    }

    public int getLoc() {
        return loc;
    }

    public void setLoc(int loc) {
        this.loc = loc;
    }

    public int getAuthorsNumber() {
        return authorsNumber;
    }

    public void setAuthorsNumber(int authorsNumber) {
        this.authorsNumber = authorsNumber;
    }

    public int getRevisionsNumber() {
        return revisionsNumber;
    }

    public void setRevisionsNumber(int revisionsNumber) {
        this.revisionsNumber = revisionsNumber;
    }

    public int getTouchedLoc() {
        return touchedLoc;
    }

    public void setTouchedLoc(int touchedLoc) {
        this.touchedLoc += touchedLoc;
    }

    public int getTotalAddedLines() {
        return totalAddedLines;
    }

    public void setTotalAddedLines(int totalAddedLines) {
        this.totalAddedLines += totalAddedLines;
    }

    public float getAverageAddedLines() {
        return averageAddedLines;
    }

    public void setAverageAddedLines(float averageAddedLines) {
        this.averageAddedLines = averageAddedLines;
    }

    public int getMaxAddedLines() {
        return maxAddedLines;
    }

    public void setMaxAddedLines(int maxAddedLines) {
        this.maxAddedLines = maxAddedLines;
    }

    public int getTotalChurn() {
        return totalChurn;
    }

    public void setTotalChurn(int totalChurn) {
        this.totalChurn = totalChurn;
    }

    public float getAverageChurn() {
        return averageChurn;
    }

    public void setAverageChurn(float averageChurn) {
        this.averageChurn = averageChurn;
    }

    public int getMaxChurn() {
        return maxChurn;
    }

    public void setMaxChurn(int maxChurn) {
        this.maxChurn = maxChurn;
    }

    public int getNumberFix() {
        return numberFix;
    }

    public void setNumberFix(int numberFix) {
        this.numberFix = numberFix;
    }

    public int getCyclomaticComplexity() {
        return cyclomaticComplexity;
    }

    public void setCyclomaticComplexity(int cyclomaticComplexity) {
        this.cyclomaticComplexity = cyclomaticComplexity;
    }

    public double getDaysBetweenCommits() {
        return daysBetweenCommits;
    }

    public void setDaysBetweenCommits(double daysBetweenCommits) {
        this.daysBetweenCommits = daysBetweenCommits;
    }

    public boolean isBuggy() {
        return buggy;
    }

    public void setBuggy(boolean buggy) {
        this.buggy = buggy;
    }

    public List<RevCommit> getFixCommits() {
        return fixCommits;
    }

    public void setFixCommits(List<RevCommit> fixCommits) {
        this.fixCommits = fixCommits;
    }

    public void addFixCommit(RevCommit commit){
        fixCommits.add(commit);
    }
}
