package org.marcolore.bugginesspredictor;

import org.marcolore.bugginesspredictor.controller.JiraExtractor;
import org.marcolore.bugginesspredictor.model.Release;
import org.marcolore.bugginesspredictor.model.Ticket;

import java.io.IOException;
import java.util.ArrayList;

public class Main {

    static String projectName = "BOOKKEEPER";

    public static void main(String[] args) throws IOException {
        JiraExtractor jiraExtractor = new JiraExtractor();
        ArrayList<Release> releases = jiraExtractor.getReleaseInfo(projectName);
        System.out.println("Releases complete");
        ArrayList<Ticket> tickets = jiraExtractor.retrieveTickets(releases, projectName);
        System.out.println("Tickets complete");
    }
}