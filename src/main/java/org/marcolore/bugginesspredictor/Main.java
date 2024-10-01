package org.marcolore.bugginesspredictor;

import org.marcolore.bugginesspredictor.controller.JiraExtractor;
import org.marcolore.bugginesspredictor.model.Release;
import org.marcolore.bugginesspredictor.model.Ticket;
import org.marcolore.bugginesspredictor.utility.TicketUtility;

import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.logging.ConsoleHandler;

public class Main {

    public final static String PROJECT_NAME = "BOOKKEEPER";
    private static final Logger logger = Logger.getLogger(Main.class.getName());

    public static void main(String[] args) throws IOException {

        JiraExtractor jiraExtractor = new JiraExtractor();
        ArrayList<Release> releases = jiraExtractor.getReleaseInfo(PROJECT_NAME); //Get all releases
        logger.info("Retrieved releases");
        ArrayList<Ticket> tickets = jiraExtractor.retrieveTickets(releases, PROJECT_NAME); //Get all tickets
        logger.info("Retrieved tickets");
        //Now we have all the tickets and releases, but many tickets are without an injected version
        ArrayList<Ticket> ticketsCompleted = TicketUtility.insertIV(tickets);
    }
}