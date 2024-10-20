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
import org.marcolore.datasetbuilderisw2.utility.ReleaseUtility;
import org.marcolore.datasetbuilderisw2.utility.TicketUtility;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {

    public static final String PROJECT_NAME = "BOOKKEEPER";
    private static final Logger logger = LoggerFactory.getLogger(Main.class);
    private static String initialPath;

    public static void main(String[] args) throws IOException, GitAPIException {

        takeCorrectPath();

        JiraController jiraController = new JiraController(PROJECT_NAME);

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
        String path = initialPath + PROJECT_NAME.toLowerCase();
        try (GitController gitController = new GitController(path)) {

            List<RevCommit> commitsList = gitController.getCommits(releases);
            logger.info("Retrieved {} commits", commitsList.size());
            logger.info("Actual number of releases {}", releases.size());

            // Now links the commits with the tickets
            TicketUtility.linkCommitTickets(tickets, commitsList);
            logger.info("Linked commits to tickets");
            //Now we take only the first half releases
            List<Release> releaseList = ReleaseUtility.removeHalfReleases(releases);
            //Now we need to extract our Java classes
            List<JavaClass> javaClassList = gitController.retrieveClasses(releaseList);
            logger.info("Extracted {} classes", javaClassList.size());
            //Now we need to compute the metrics
            MetricsCalculatorController metricsCalculator = new MetricsCalculatorController(javaClassList, commitsList, gitController);
            metricsCalculator.calculateMetrics();
        }

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
