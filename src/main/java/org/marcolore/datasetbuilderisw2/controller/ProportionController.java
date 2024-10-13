package org.marcolore.datasetbuilderisw2.controller;

import org.marcolore.datasetbuilderisw2.enums.ProjectsNames;
import org.marcolore.datasetbuilderisw2.model.Release;
import org.marcolore.datasetbuilderisw2.model.Ticket;
import org.marcolore.datasetbuilderisw2.utility.TicketUtility;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ProportionController {
    //We define a constant, if the number of tickets with IV is < 5, we do a Cold Start
    private static final Integer COLD_START_THRESHOLD = 5;

    private ProportionController() {
    }


    public static void insertIV(List<Ticket> tickets, List<Release> releases) throws IOException {
        ArrayList<Ticket> ticketWithInjectedV = new ArrayList<>();
        float pColdStart = pColdStart();
        for (Ticket ticket : tickets) {
            if (ticket.getInjectedVersion() != null) {
                TicketUtility.setAV(ticket, releases); //Set the affected versions
                ticketWithInjectedV.add(ticket); //If the injected version is not null, we use the ticket for calculate the proportion
            }
            else{
                calculateProportionMethod(ticketWithInjectedV, ticket, releases, pColdStart);
            } //If the injected version is null, we calculate the proportion for this ticket
        }
    }

    private static void calculateProportionMethod(List<Ticket> ticketsWithIV, Ticket ticketWithoutIV, List<Release> releases, float pColdStart) {
        //Calculate the proportion
        float p;
        if(ticketsWithIV.size() < COLD_START_THRESHOLD) {
            p = pColdStart;
        } else{
            p = pIncrementProportion(ticketsWithIV); //Now we have the medium value for p
        }
        //Now we have the p value, and we can set the injected version and the affected versions
        TicketUtility.setIV(ticketWithoutIV, releases, p);
        TicketUtility.setAV(ticketWithoutIV, releases);

    }

    private static float pIncrementProportion(List<Ticket> ticketsWithIV){

        ArrayList<Float> proportionValue = new ArrayList<>();
        float pSum = 0;

        for (Ticket ticket : ticketsWithIV) {
            proportionValue.add(computeP(ticket)); //Compute the p value for each ticket
        }


        for (Float pValue : proportionValue) {
            pSum += pValue;
        }

        //Calculating the medium value
        return proportionValue.isEmpty() ? 0 : pSum / proportionValue.size();
    }

    private static float computeP(Ticket ticket){

        // Proportion formula used: p = (FV-IV)/(FV-OV)
        //If OV == FV, denominator is 1

        Integer openingVersionIndex = ticket.getOpeningVersion().getId();
        Integer fixedVersionIndex = ticket.getFixedVersion().getId();
        Integer injectedVersionIndex = ticket.getInjectedVersion().getId();
        int denominator = (fixedVersionIndex - openingVersionIndex) == 0 ? 1 : (fixedVersionIndex - openingVersionIndex);

        return (float) (fixedVersionIndex - injectedVersionIndex) / denominator;
    }

    private static float pColdStart() throws IOException {

        ArrayList<Float> projectValues = new ArrayList<>();

        for(ProjectsNames project : ProjectsNames.values()) { //We want to calculate p for all projects
            JiraController jiraController = new JiraController(project.name());
            List<Release> releases = jiraController.getReleaseInfo(); //Get all releases
            List<Ticket> tickets = jiraController.retrieveTickets(releases); //Get all tickets
            tickets.removeIf(ticket -> ticket.getInjectedVersion() == null); //Now we have only fixed tickets
            projectValues.add(pIncrementProportion(tickets));
        }

        if (projectValues.isEmpty()) {
            return 0;
        }

        Collections.sort(projectValues);

        int size = projectValues.size();
        float pColdStart;

        if (size % 2 == 1) {
            pColdStart = projectValues.get(size / 2);
        } else {
            pColdStart = (projectValues.get((size / 2) - 1) + projectValues.get(size / 2)) / 2;
        }

        return pColdStart;
    }
}
