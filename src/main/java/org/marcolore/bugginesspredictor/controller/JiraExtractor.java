package org.marcolore.bugginesspredictor.controller;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.marcolore.bugginesspredictor.model.Release;
import org.marcolore.bugginesspredictor.model.Ticket;
import org.marcolore.bugginesspredictor.utility.TicketUtility;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import static org.marcolore.bugginesspredictor.utility.JsonUtility.readJsonFromUrl;
import static org.marcolore.bugginesspredictor.utility.ReleaseUtility.*;

public class JiraExtractor {



    public JiraExtractor() {
    }

    public ArrayList<Release> getReleaseInfo(String projectName) throws IOException {
        ArrayList<Release> releases = new ArrayList<>();

        String url = "https://issues.apache.org/jira/rest/api/2/project/" + projectName;
        JSONObject json = readJsonFromUrl(url);
        JSONArray versions = json.getJSONArray("versions");
        int i;
        for (i = 0; i < versions.length(); i++ ) {

            String id = "";
            String name = "";
            String releaseDate;
            JSONObject releaseJsonObject = versions.getJSONObject(i);

            if(releaseJsonObject.has("releaseDate")) {
                releaseDate = releaseJsonObject.get("releaseDate").toString();
                if (releaseJsonObject.has("name"))
                    name = releaseJsonObject.get("name").toString();
                if (releaseJsonObject.has("id"))
                    id = releaseJsonObject.get("id").toString();

                releases.add(new Release(id, name, releaseDate));
                System.out.printf("Release: %s\n Id: %s\n Name: %s\n", releaseDate, id, name);
            }
        }
        // Ordering releases based on the release date
        releases.sort(Comparator.comparing(Release::getReleaseDate));
        // Now we can set an id
        for (i = 0; i < releases.size(); i++) {
            releases.get(i).setId(++i); //First index is 1
        }
        return releases;
    }

    public ArrayList<Ticket> retrieveTickets(ArrayList<Release> releases, String projectName) throws IOException, JSONException {

        int j, i = 0, total;
        //Get JSON API for closed bugs w/ AV in the project
        ArrayList<Ticket> tickets = new ArrayList<>();
        int equalDate = 0;
        int opAfterFixed = 0;
        int injEqualOp = 0;
        int injAfterOp = 0;
        int notEqualDate = 0;
        do {
            //Only gets a max of 1000 at a time, so must do this multiple times if bugs > 1000
            j = i + 1000;
            String url = "https://issues.apache.org/jira/rest/api/2/search?jql=project=%22"
                    + projectName + "%22AND%22issueType%22=%22Bug%22AND(%22status%22=%22closed%22OR"
                    + "%22status%22=%22resolved%22)AND%22resolution%22=%22fixed%22&fields=key,resolutiondate,versions,created&startAt="
                    + i + "&maxResults=" + j;

            JSONObject json = readJsonFromUrl(url);
            JSONArray issues = json.getJSONArray("issues");
            total = json.getInt("total");

            for (; i < total && i < j; i++) {

                //Iterate through each bug and take info
                String key = issues.getJSONObject(i%1000).get("key").toString();
                JSONObject fields = issues.getJSONObject(i%1000).getJSONObject("fields");
                String creationDateString = fields.get("created").toString();
                String resolutionDateString = fields.get("resolutiondate").toString();
                LocalDateTime creationDate = LocalDate.parse(creationDateString.substring(0,10)).atStartOfDay();
                LocalDateTime resolutionDate = LocalDate.parse(resolutionDateString.substring(0,10)).atStartOfDay();
                JSONArray affectedVersionJson = fields.getJSONArray("versions");

                ArrayList<Release> affectedReleases = getAffectedRelease(releases, affectedVersionJson); //Check if affected version is in our list of releases
                Release openingVersion = getReleaseByDate(creationDate, releases); //When bug is recognized
                Release fixedVersion =  getReleaseByDate(resolutionDate, releases); //When bug is fixed
                Release injectedVersion = getInjectedVersion(affectedReleases); //When bug is injected

                if (openingVersion != null && fixedVersion != null && openingVersion.getReleaseDate().isEqual(fixedVersion.getReleaseDate())){
                    equalDate += 1;
                } else{
                    notEqualDate += 1;
                }
                if(openingVersion != null && fixedVersion != null && openingVersion.getReleaseDate().isAfter(fixedVersion.getReleaseDate())){
                    opAfterFixed += 1;
                }
                if (injectedVersion != null && openingVersion != null && injectedVersion.getReleaseDate().isEqual(openingVersion.getReleaseDate())){
                    injEqualOp += 1;
                }
                if (injectedVersion != null && openingVersion != null && injectedVersion.getReleaseDate().isAfter(openingVersion.getReleaseDate())){
                    injAfterOp += 1;
                }

                if (openingVersion != null) {
                    System.out.println("Opening: " + openingVersion.getReleaseDate());
                } else {
                    System.out.println("Opening version is null for key: " + key);
                }

                if (fixedVersion != null) {
                    System.out.println("Fixed:" + fixedVersion.getReleaseDate());
                } else {
                    System.out.println("Fixed version is null for key: " + key);
                }
                //Check if the ticket is valid, we want to:
                // 1. Discard the Injected Version if it is after or equal the Opening Version
                // 2. Discard the Ticket if the Injected Version is after or equal the Fixed Version
                TicketUtility.checkTicketValidity(tickets, key, injectedVersion, openingVersion, fixedVersion);

            }

        }while (i < total);
        System.out.println("\n Op == Fix: " + equalDate);
        System.out.println("\n Op != Fix: " + notEqualDate);
        System.out.println("\n Op > Fix: " + opAfterFixed);
        System.out.println("\n Inj == Op: " + injEqualOp);
        System.out.println("\n Inj > Op: " + injAfterOp);
        return tickets;
    }

}
