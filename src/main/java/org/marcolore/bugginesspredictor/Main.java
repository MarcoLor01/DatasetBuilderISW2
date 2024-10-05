package org.marcolore.bugginesspredictor;

import org.marcolore.bugginesspredictor.controller.JiraController;
import org.marcolore.bugginesspredictor.controller.ProportionController;
import org.marcolore.bugginesspredictor.model.Release;
import org.marcolore.bugginesspredictor.model.Ticket;
import org.marcolore.bugginesspredictor.utility.TicketUtility;

import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Logger;

public class Main {

    public final static String PROJECT_NAME = "BOOKKEEPER";
    private static final Logger logger = Logger.getLogger(Main.class.getName());

    public static void main(String[] args) throws IOException {

        JiraController jiraController = new JiraController(PROJECT_NAME);
        ArrayList<Release> releases = jiraController.getReleaseInfo(); //Get all releases
        logger.info(String.format("Retrieved %d releases", releases.size()));
        ArrayList<Ticket> tickets = jiraController.retrieveTickets(releases); //Get all tickets
        logger.info(String.format("Retrieved %d tickets", tickets.size()));

        //Now we have all the tickets and releases, but many tickets are without an injected version
        ProportionController.insertIV(tickets, releases);
        TicketUtility.checkTicketValidity(tickets, releases);
        //Tickets are now both correct and complete
        logger.info(String.format("Validated %d tickets", tickets.size()));
    }
}
