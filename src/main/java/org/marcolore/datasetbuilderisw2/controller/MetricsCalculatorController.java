package org.marcolore.datasetbuilderisw2.controller;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.revwalk.RevCommit;
import org.marcolore.datasetbuilderisw2.model.JavaClass;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
        calculateCyclomaticComplexity();
        calculateTimeBetweenCommit();
    }

    private void calculateTimeBetweenCommit() {
        for (JavaClass javaClass : javaClassList) {
            List<RevCommit> listOfCommits = javaClass.getListOfCommit();
            listOfCommits.sort(Comparator.comparing(revCommit -> revCommit.getCommitterIdent().getWhen()));

            List<Long> commitDistances = new ArrayList<>();
            LocalDate previousCommitDate = null;


            for (RevCommit revCommit : listOfCommits) {
                Instant commitInstant = revCommit.getCommitterIdent().getWhen().toInstant();
                LocalDate commitDate = commitInstant.atZone(ZoneId.systemDefault()).toLocalDate();

                if (previousCommitDate != null) {
                    long daysBetween = java.time.Duration.between(previousCommitDate.atStartOfDay(), commitDate.atStartOfDay()).toDays();
                    commitDistances.add(daysBetween);
                }
                previousCommitDate = commitDate;
            }

            double averageDaysBetweenCommits = commitDistances.stream()
                    .mapToLong(Long::longValue)
                    .average()
                    .orElse(0.0);

            javaClass.setDaysBetweenCommits(averageDaysBetweenCommits);

        }
    }


    public static String removeComments(String code) {
        StringBuilder result = new StringBuilder();
        int length = code.length();
        int i = 0;

        while (i < length) {
            if (i < length - 1 && code.charAt(i) == '/' && code.charAt(i + 1) == '/') {
                i += 2;
                while (i < length && code.charAt(i) != '\n') {
                    i++;
                }
            } else if (i < length - 1 && code.charAt(i) == '/' && code.charAt(i + 1) == '*') {
                i += 2;
                while (i < length - 1 && !(code.charAt(i) == '*' && code.charAt(i + 1) == '/')) {
                    i++;
                }
                i += 2;
            } else {
                result.append(code.charAt(i));
                i++;
            }
        }

        return result.toString();
    }

    private void calculateCyclomaticComplexity() {

        String[] controlStructures = {
                "if\\s*\\(",      // if
                "else\\s*if\\s*\\(", // else if
                "for\\s*\\(",     // for
                "while\\s*\\(",   // while
                "do\\s*\\{",      // do-while
                "switch\\s*\\(",  // switch
                "case\\s",        // case
                "default\\s*:"    // default
        };

        String[] booleanConditions = {
                "&&",             // AND logic
                "\\|\\|"          // OR logic
        };

        String[] exceptionStructures = {
                "throw\\s",       // throw
                "throws\\s",      // throws
                "catch\\s*\\(",   // catch
                "finally\\s*\\{"  // finally
        };

        for(JavaClass javaClass : javaClassList) {
            String javaClassCode = removeComments(javaClass.getFileContent());
            int complexity = 1;

            for(String pattern : controlStructures){
                complexity += countMatches(javaClassCode, pattern);
            }
            for(String pattern : booleanConditions){
                complexity += countMatches(javaClassCode, pattern);
            }
            for(String pattern : exceptionStructures){
                complexity += countMatches(javaClassCode, pattern);
            }
            javaClass.setCyclomaticComplexity(complexity);

        }

    }

    public static int countMatches(String code, String regex){
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(code);
        int count = 0;

        while (matcher.find()){
            count++;
        }
        return count;
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
                javaClass.setAverageAddedLines((float) javaClass.getTotalAddedLines() / commits.size());
                javaClass.setAverageChurn((float) javaClass.getTotalChurn() / commits.size());
            }

        }

    }

    public void calculateNumberFix(List<JavaClass> javaClassList) {
        for (JavaClass javaClass : javaClassList) {
            javaClass.setNumberFix(javaClass.getFixCommits().size());
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
            String content = removeComments(javaClass.getFileContent());
            String[] lines = content.split("\n");

            for (String line : lines) {
                String trimmedLine = line.trim();
                if (!trimmedLine.isEmpty()) {
                    totalLoc++;
                }
            }
            javaClass.setLoc(totalLoc);
        }
    }
}

