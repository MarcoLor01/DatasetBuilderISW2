package org.marcolore.bugginesspredictor;

import org.marcolore.bugginesspredictor.controller.JiraController;
import org.marcolore.bugginesspredictor.controller.ProportionController;
import org.marcolore.bugginesspredictor.model.Release;
import org.marcolore.bugginesspredictor.model.Ticket;
import org.marcolore.bugginesspredictor.utility.TicketUtility;

import java.io.IOException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {

    public static final String PROJECT_NAME = "BOOKKEEPER";
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) throws IOException {

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
    }
}
