package org.marcolore.bugginesspredictor.utility;

import org.marcolore.bugginesspredictor.model.Release;
import org.marcolore.bugginesspredictor.model.Ticket;

import java.util.ArrayList;

public class TicketUtility {

    public static void checkTicketValidity(ArrayList<Ticket> tickets, String key, Release injectedVersion, Release openingVersion, Release fixedVersion) {
        if (openingVersion == null || fixedVersion == null) {
            return;
        }
        if (injectedVersion != null && injectedVersion.getReleaseDate().isAfter(openingVersion.getReleaseDate())){
            injectedVersion = null;
        }
        if (injectedVersion != null){

        }
    }
}
