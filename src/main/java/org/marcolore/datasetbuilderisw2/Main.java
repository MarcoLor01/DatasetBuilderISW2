package org.marcolore.datasetbuilderisw2;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.revwalk.RevCommit;
import org.marcolore.datasetbuilderisw2.controller.GitController;
import org.marcolore.datasetbuilderisw2.controller.JiraController;
import org.marcolore.datasetbuilderisw2.controller.MetricsCalculatorController;
import org.marcolore.datasetbuilderisw2.controller.ProportionController;
import org.marcolore.datasetbuilderisw2.model.JavaClass;
import org.marcolore.datasetbuilderisw2.model.Release;
import org.marcolore.datasetbuilderisw2.model.Ticket;
import org.marcolore.datasetbuilderisw2.utility.ArffUtility;
import org.marcolore.datasetbuilderisw2.utility.CsvUtility;
import org.marcolore.datasetbuilderisw2.utility.ReleaseUtility;
import org.marcolore.datasetbuilderisw2.utility.TicketUtility;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {


    private static final Logger logger = LoggerFactory.getLogger(Main.class);
    private static String initialPath;

    public static void main(String[] args) throws GitAPIException, IOException {
        logger.info("Start Bookkeeper extraction");
        extractData("BOOKKEEPER");
        logger.info("Start Avro extraction");
        extractData("AVRO");
    }

    public static void extractData(String projectName) throws IOException, GitAPIException {

        takeCorrectPath();

        List<JavaClass> javaClassList;
        List<Release> releaseList;

        JiraController jiraController = new JiraController(projectName);

        List<Release> releases = jiraController.getReleaseInfo(); //Get all releases
        logger.info("Retrieved {} releases", releases.size());

        List<Ticket> tickets = jiraController.retrieveTickets(releases); //Get all tickets
        logger.info("Retrieved {} tickets", tickets.size());

        //Now we have all the tickets and releases, but many tickets are without an injected version
        ProportionController.insertIV(tickets, releases);
        TicketUtility.checkTicketValidity(tickets, releases);

        //Tickets are now both correct and complete
        logger.info("Validated {} tickets", tickets.size());

        // Setting the repo path
        String path = initialPath + projectName.toLowerCase();

        GitController gitController = new GitController(path);

        List<RevCommit> commitsList = gitController.getCommits(releases);
        logger.info("Retrieved {} commits", commitsList.size());
        logger.info("Actual number of releases {}", releases.size());

        // Now links the commits with the tickets
        TicketUtility.linkCommitTickets(tickets, commitsList);
        logger.info("Linked commits to tickets");

        //Now we take only the first half releases
        releaseList = ReleaseUtility.removeHalfReleases(releases);
        logger.info("Actual number of releases {}", releaseList.size());

        //Now we need to extract our Java classes
        javaClassList = gitController.retrieveClasses(releaseList);
        logger.info("Extracted {} classes", javaClassList.size());

        //Now we need to compute the metrics
        MetricsCalculatorController metricsCalculator = new MetricsCalculatorController(javaClassList, gitController);
        List<JavaClass> classWithMetrics = metricsCalculator.calculateMetrics();
        logger.info("Metrics Calculated");

        createCsvFiles(classWithMetrics, releaseList, tickets, gitController, metricsCalculator, projectName);

        }


    public static void createCsvFiles(List<JavaClass> javaClassList,
                                      List<Release> releaseList,
                                      List<Ticket> tickets,
                                      GitController gitController,
                                      MetricsCalculatorController metricsCalculatorController,
                                      String projectName)
            throws IOException {
        /* We need to create the training and the test set, we start (for test set)
        from release 3 to the last release who is the number tot_release / 2 */
        int firstRelease = 3;
        int numberOfTraining = 1;

        for(int i=firstRelease;i<=(releaseList.size());i++){
            //Now we use the first release as the test set
            List<JavaClass> trainingClassList = new ArrayList<>(javaClassList);

            ReleaseUtility.checkReleaseId(trainingClassList, i);
            List<Ticket> trainingTickets = TicketUtility.checkFixedVersion(tickets, i);
            List<JavaClass> testingClassList = ReleaseUtility.getJavaClassesFromRelease(javaClassList, i);

            setBuggyOrNot(trainingTickets, trainingClassList, gitController, releaseList);
            setBuggyOrNot(trainingTickets, testingClassList, gitController, releaseList);

            metricsCalculatorController.calculateNumberFix(trainingClassList);
            metricsCalculatorController.calculateNumberFix(testingClassList);

            writeDatasets(projectName, numberOfTraining, trainingClassList, testingClassList);

            numberOfTraining++;
        }

    }


    private static void writeDatasets(String projectName, int numberOfTraining, List<JavaClass> trainingClassList, List<JavaClass> testingClassList) throws IOException {
        CsvUtility.writeCsvFile(trainingClassList, "Training", numberOfTraining, projectName);
        CsvUtility.writeCsvFile(testingClassList, "Testing", numberOfTraining, projectName);
        ArffUtility.createArff(trainingClassList, "Training", numberOfTraining, projectName);
        ArffUtility.createArff(testingClassList, "Testing", numberOfTraining, projectName);
    }

    public static void setBuggyOrNot(List<Ticket> ticketList, List<JavaClass> allProjectClasses, GitController gitController, List<Release> releaseList) throws IOException {
        for (Ticket ticket : ticketList) {
            List<RevCommit> ticketCommitList = ticket.getCommitList();
            Release injectedVersion = ticket.getInjectedVersion();

            for (RevCommit commit : ticketCommitList) {
                int releaseOfCommitId = ReleaseUtility.matchCommitsRelease(commit.getCommitterIdent().getWhen().toInstant().
                        atZone(ZoneId.systemDefault()).toLocalDateTime(), releaseList);
                Release releaseOfCommit = ReleaseUtility.releaseFromId(releaseList, releaseOfCommitId);
                List<String> modifiedClassesNames = gitController.findTouchedClassFromCommit(commit);

                // Etichetta le classi come buggy se necessario
                for (String modifiedClass : modifiedClassesNames) {
                    labelBuggyClasses(modifiedClass, injectedVersion, releaseOfCommit, allProjectClasses, commit);
                }
            }
        }

    }

    private static void labelBuggyClasses(String modifiedClass, Release injectedVersion, Release fixedVersion, List<JavaClass> allProjectClasses, RevCommit commit) {

        for (JavaClass projectClass : allProjectClasses) {

            if (projectClass.getListOfCommit().contains(commit) && !projectClass.getFixCommits().contains(commit)) {
                projectClass.addFixCommit(commit);
            }
            if (isClassModifiedInBuggyVersion(projectClass, modifiedClass, injectedVersion, fixedVersion)) {
                projectClass.setBuggy(true);
            }
        }
    }

    private static boolean isClassModifiedInBuggyVersion(JavaClass projectClass, String modifiedClass, Release injectedVersion, Release fixedVersion) {
        return projectClass.getClassName().equals(modifiedClass) &&
                projectClass.getRelease().getId() < fixedVersion.getId() &&
                projectClass.getRelease().getId() >= injectedVersion.getId();
    }


    private static void takeCorrectPath() {
        Properties properties = new Properties();

        try (InputStream input = new FileInputStream("config.properties")) {
            properties.load(input);
            String pathChoice = properties.getProperty("path");
            String basePath = properties.getProperty("base.path");
            if ("1".equals(pathChoice)) {
                initialPath = basePath + "Utente\\";
            } else if ("2".equals(pathChoice)) {
                initialPath = basePath + "HP\\";
            }

        } catch (IOException ex) {
            logger.error("Error calculating path");
        }
    }

}
