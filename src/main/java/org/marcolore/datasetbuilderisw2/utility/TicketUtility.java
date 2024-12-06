package org.marcolore.datasetbuilderisw2.utility;

import org.eclipse.jgit.revwalk.RevCommit;
import org.marcolore.datasetbuilderisw2.model.Release;
import org.marcolore.datasetbuilderisw2.model.Ticket;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

public class TicketUtility {

    private TicketUtility() {
        throw new IllegalStateException("Utility class");
    }


    public static Ticket checkTicketValidityAndCreate(String key, List<Release> releases, Release injectedVersion, Release openingVersion, Release fixedVersion, List<Release> affectedRelease) {

        if ((openingVersion == null || fixedVersion == null)
                || (openingVersion.getReleaseDate().isAfter(fixedVersion.getReleaseDate()))
                || (openingVersion.getReleaseDate().isEqual(releases.get(0).getReleaseDate()))
                || (openingVersion.getOldIdRelease().equals(releases.get(0).getOldIdRelease()))) {
            return null;
        }

        if (injectedVersion != null && injectedVersion.getReleaseDate().isAfter(fixedVersion.getReleaseDate())
                || (injectedVersion != null && injectedVersion.getReleaseDate().isAfter(openingVersion.getReleaseDate()))) {
            injectedVersion = null;
            affectedRelease = null;
        }
        return new Ticket(key, openingVersion, fixedVersion, injectedVersion, affectedRelease);
    }

    public static void setAV(Ticket ticket, List<Release> releases) {

        Integer injectedVersion = ticket.getInjectedVersion().getId();
        Integer fixedVersion = ticket.getFixedVersion().getId();
        ticket.setAffectedReleases(new ArrayList<>());

        for (Release release : releases) {
            if (release.getId() >= injectedVersion && release.getId() < fixedVersion) {
                ticket.addAffectedReleases(release);
            }
        }
    }

    public static void setIV(Ticket ticketWithoutIV, List<Release> releases, float p) {
        int iv;

        // If OV == FV, I assume that FV-OV = 1
        if (Objects.equals(ticketWithoutIV.getOpeningVersion().getId(), ticketWithoutIV.getFixedVersion().getId())
                && ticketWithoutIV.getInjectedVersion() == null) {
            iv = (int) (ticketWithoutIV.getFixedVersion().getId() - p);
        } else {
            // Formula: IV = FV - ((FV - OV) * p)
            iv = (int) (ticketWithoutIV.getFixedVersion().getId()
                    - ((ticketWithoutIV.getFixedVersion().getId() - ticketWithoutIV.getOpeningVersion().getId()) * p));
        }

        if (iv < 1) { //Index not valid
            iv = 1;
        }

        ticketWithoutIV.setInjectedVersion(releases.get(iv - 1));
    }

    public static void linkCommitTickets(List<Ticket> tickets, List<RevCommit> commits) {
        for (Ticket ticket : tickets) {
            String key = ticket.getKey();

            for (RevCommit commit : commits) {

                LocalDateTime commitDate = commit.getCommitterIdent().getWhen().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
                LocalDate commitLocalDate = commitDate.toLocalDate();

                    if (existsLinkMessageCommit(commit.getFullMessage(), key) && !commitLocalDate.isAfter(ticket.getResolutionDate().toLocalDate())
                            && !commitLocalDate.isBefore(ticket.getCreationDate().toLocalDate())) {
                        ticket.getCommitList().add(commit);
                    }
                }

        }
        tickets.removeIf(ticket -> ticket.getCommitList().isEmpty());
    }

    private static boolean existsLinkMessageCommit(String message, String ticketID) {
        return message.contains(ticketID + "\n") || message.contains(ticketID + " ") || message.contains(ticketID + ":")
                || message.contains(ticketID + ".") || message.contains(ticketID + "/") || message.endsWith(ticketID) ||
                message.contains(ticketID + "]") || message.contains(ticketID + "_") || message.contains(ticketID + "-") || message.contains(ticketID + ")");
    }

    public static List<Ticket> checkFixedVersion(List<Ticket> tickets, int testRelease) {
        List<Ticket> trainingTickets = new ArrayList<>(tickets);
        trainingTickets.removeIf(ticket ->
                Optional.ofNullable(ticket.getFixedVersion())
                        .map(release -> release.getId() >= testRelease)
                        .orElse(false)
        );
        return trainingTickets;
    }

}



