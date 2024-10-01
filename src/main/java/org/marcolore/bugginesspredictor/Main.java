package org.marcolore.bugginesspredictor;

import org.marcolore.bugginesspredictor.controller.JiraController;
import org.marcolore.bugginesspredictor.controller.ProportionController;
import org.marcolore.bugginesspredictor.model.Release;
import org.marcolore.bugginesspredictor.model.Ticket;

import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Logger;

public class Main {

    public final static String PROJECT_NAME = "AVRO";
    private static final Logger logger = Logger.getLogger(Main.class.getName());

    public static void main(String[] args) throws IOException {

        JiraController jiraController = new JiraController();
        ArrayList<Release> releases = jiraController.getReleaseInfo(PROJECT_NAME); //Get all releases
        logger.info("Retrieved releases");
        ArrayList<Ticket> tickets = jiraController.retrieveTickets(releases, PROJECT_NAME); //Get all tickets
        logger.info("Retrieved tickets");

        //Now we have all the tickets and releases, but many tickets are without an injected version
        ArrayList<Ticket> ticketsCompleted = ProportionController.insertIV(tickets, releases);
    }
}