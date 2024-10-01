package org.marcolore.bugginesspredictor.utility;

import org.marcolore.bugginesspredictor.model.Release;
import org.marcolore.bugginesspredictor.model.Ticket;

import java.time.LocalDateTime;
import java.util.ArrayList;

public class TicketUtility {

    public static void checkTicketValidityAndCreate(ArrayList<Ticket> tickets, String key, Release injectedVersion, Release openingVersion, Release fixedVersion, LocalDateTime creationDate, LocalDateTime resolutionDate, ArrayList<Release> affectedRelease) {
        if ((openingVersion == null || fixedVersion == null)
                || (openingVersion.getReleaseDate().isAfter(fixedVersion.getReleaseDate()))
                || injectedVersion != null && injectedVersion.getReleaseDate().isEqual(fixedVersion.getReleaseDate())) {

            return;
        }

        if (injectedVersion != null && injectedVersion.getReleaseDate().isAfter(fixedVersion.getReleaseDate())
                || (injectedVersion != null && injectedVersion.getReleaseDate().isAfter(openingVersion.getReleaseDate()))){
            injectedVersion = null;
        }

        tickets.add(new Ticket(key, creationDate, openingVersion, fixedVersion, injectedVersion, resolutionDate, affectedRelease));
    }
}
