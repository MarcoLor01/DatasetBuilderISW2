package org.marcolore.bugginesspredictor;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.revwalk.RevCommit;
import org.marcolore.bugginesspredictor.controller.GitController;
import org.marcolore.bugginesspredictor.controller.JiraController;
import org.marcolore.bugginesspredictor.controller.ProportionController;
import org.marcolore.bugginesspredictor.model.Release;
import org.marcolore.bugginesspredictor.model.Ticket;
import org.marcolore.bugginesspredictor.utility.TicketUtility;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {

    public static final String PROJECT_NAME = "BOOKKEEPER";
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) throws IOException, GitAPIException {

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
        String path = "C:\\Users\\HP\\" + PROJECT_NAME.toLowerCase();
        Path repoPath = Paths.get(path);

        List<RevCommit> commitsList = GitController.GetCommits(releases, repoPath);
        logger.info("Retrieved {} commits", commitsList.size());
        logger.info("Actual number of releases {}", releases.size());

        // Now links the commits with the tickets
        TicketUtility.linkCommitTickets(tickets, commitsList);
        logger.info("Linked commits to tickets");


    }

}
