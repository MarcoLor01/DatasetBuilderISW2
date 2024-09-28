package org.marcolore.bugginesspredictor.utility;

import org.json.JSONArray;
import org.marcolore.bugginesspredictor.model.Release;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Objects;

public class ReleaseUtility {

    public static Release getReleaseByDate(LocalDateTime date, ArrayList<Release> releases) {
        for (Release release : releases) {

            if (!release.getReleaseDate().isBefore(date)) {
                return release;
            }
        }

        return null;
    }

    public static ArrayList<Release> getAffectedRelease(ArrayList<Release> releases, JSONArray affectedVersion) {
            ArrayList<Release> affectedRelease = new ArrayList<>();

            for(int i = 0; i < affectedVersion.length(); i++){
                Integer versionId = Integer.parseInt(affectedVersion.getJSONObject(i).get("id").toString());
                Release release = checkIfAffected(releases, versionId);
                if (release != null){
                    affectedRelease.add(release);
                }
            }

        return affectedRelease;
    }

    public static Release checkIfAffected(ArrayList<Release> releases, Integer versionId) {
        boolean affected;

        for (Release release : releases) {
            if (Objects.equals(release.getIdRelease(), versionId)) {
                return release;
            }
        }
        return null;
    }

    public static Release getInjectedVersion(ArrayList<Release> releases){
        if (releases.size() > 0){
            return releases.get(0);
        } else{
            return null;
        }
    }

}