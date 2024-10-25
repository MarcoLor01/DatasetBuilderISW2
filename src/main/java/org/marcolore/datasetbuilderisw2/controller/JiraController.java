package org.marcolore.datasetbuilderisw2.controller;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.marcolore.datasetbuilderisw2.model.Release;
import org.marcolore.datasetbuilderisw2.model.Ticket;
import org.marcolore.datasetbuilderisw2.utility.TicketUtility;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import static org.marcolore.datasetbuilderisw2.utility.JsonUtility.readJsonFromUrl;
import static org.marcolore.datasetbuilderisw2.utility.ReleaseUtility.*;

public class JiraController {

    private final String projectName;

    public JiraController(String projectName) {
        this.projectName = projectName;
    }

    public List<Release> getReleaseInfo() throws IOException {
        ArrayList<Release> releases = new ArrayList<>();

        String url = "https://issues.apache.org/jira/rest/api/2/project/" + this.projectName;
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
            }
        }
        // Ordering releases based on the release date
        releases.sort(Comparator.comparing(Release::getReleaseDate));

        // Now we can set an id
        int k = 0;
        while(k<releases.size()){
            k += 1;
            releases.get(k-1).setId(k);
        }
        return releases;
    }

    public List<Ticket> retrieveTickets(List<Release> releases) throws IOException, JSONException {

        int j;
        int i = 0;
        int total;

        //Get JSON API for closed bugs w/ AV in the project
        ArrayList<Ticket> tickets = new ArrayList<>();

        do {
            //Only gets a max of 1000 at a time, so must do this multiple times if bugs > 1000
            j = i + 1000;
            String url = "https://issues.apache.org/jira/rest/api/2/search?jql=project=%22"
                    + this.projectName + "%22AND%22issueType%22=%22Bug%22AND(%22status%22=%22closed%22OR"
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

                List<Release> affectedReleases = getAffectedRelease(releases, affectedVersionJson); //Check if affected version is in our list of releases
                Release openingVersion = getReleaseByDate(creationDate, releases); //When bug is recognized
                Release fixedVersion =  getReleaseByDate(resolutionDate, releases); //When bug is fixed
                Release injectedVersion = getInjectedVersion(affectedReleases); //When bug is injected

                Ticket validatedTicket = TicketUtility.checkTicketValidityAndCreate(key, releases, injectedVersion,
                                                                    openingVersion, fixedVersion, affectedReleases);
                if (validatedTicket != null) {
                    validatedTicket.setCreationDate(creationDate);
                    validatedTicket.setResolutionDate(resolutionDate);
                    tickets.add(validatedTicket);
                }
            }

        }while (i < total);

        tickets.sort(Comparator.comparing(Ticket::getResolutionDate));
        return tickets;
    }

}
