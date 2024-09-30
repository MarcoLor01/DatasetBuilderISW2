package org.marcolore.bugginesspredictor;

import org.marcolore.bugginesspredictor.controller.JiraExtractor;
import org.marcolore.bugginesspredictor.model.Release;
import org.marcolore.bugginesspredictor.model.Ticket;

import java.io.IOException;
import java.util.ArrayList;

public class Main {

    public final static String PROJECT_NAME = "BOOKKEEPER";

    public static void main(String[] args) throws IOException {
        JiraExtractor jiraExtractor = new JiraExtractor();
        ArrayList<Release> releases = jiraExtractor.getReleaseInfo(PROJECT_NAME);

        for (Release release : releases) {
            System.out.println("Release: " + release.getId());
            System.out.println("Release Date: " + release.getReleaseDate());
        }

        System.out.println("Releases complete");
        ArrayList<Ticket> tickets = jiraExtractor.retrieveTickets(releases, PROJECT_NAME);
        System.out.println("Tickets complete"); //577
        System.out.println("List of tickets (number of tickets: " + tickets.size() + "):");
        for (Ticket ticket : tickets) {
            System.out.println("Key: " + ticket.getKey());
            System.out.println("Creation Date: " + ticket.getCreationDate());
            System.out.println("Opening Version: " + ticket.getOpeningVersion().getReleaseDate());
            System.out.println("Fixed Version: " + ticket.getFixedVersion().getReleaseDate());
            if (ticket.getInjectedVersion() != null){
                System.out.println("Injected Version" + ticket.getInjectedVersion().getReleaseDate());
            } else {
                System.out.println("Injected Version: null");
            }
        }
    }
}