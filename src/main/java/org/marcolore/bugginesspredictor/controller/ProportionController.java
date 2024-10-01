package org.marcolore.bugginesspredictor.controller;

import org.marcolore.bugginesspredictor.model.Release;
import org.marcolore.bugginesspredictor.model.Ticket;

import java.util.ArrayList;

public class ProportionController {
    //We define a constant, if the number of tickets with IV is < 5, we do a Cold Start
    private static final Integer COLD_START_THRESHOLD = 5;

    private ProportionController() {
    }


    public static ArrayList<Ticket> insertIV(ArrayList<Ticket> tickets, ArrayList<Release> releases) {
        ArrayList<Ticket> ticketWithInjectedV = new ArrayList<>();
        for (Ticket ticket : tickets) {
            if (ticket.getInjectedVersion() != null) {
                ticketWithInjectedV.add(ticket); //If the injected version is not null, we use
            }                                    //the ticket for calculate the proportion
            else{
                calculateProportionMethod(ticketWithInjectedV, ticket, releases);
            } //If the injected version is null, we calculate the proportion for this ticket
        }
        return ticketWithInjectedV;
    }

    private static void calculateProportionMethod(ArrayList<Ticket> ticketsWithIV, Ticket ticketWithoutIV, ArrayList<Release> releases){
        //Calculate the proportion
        float p;
        if(ticketsWithIV.size() < COLD_START_THRESHOLD) {
            p = pColdStart(); //Implement it

        } else{
            p = pIncrementProportion(ticketsWithIV); //Now we have the medium value for p
        }
        //Now we have the p value, and we can set the injected version and the affected versions
        System.out.println("Proportion: " + p);
    }

    private static float pIncrementProportion(ArrayList<Ticket> ticketsWithIV){

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

    private static float pColdStart(){
        return 0;
    }
}
