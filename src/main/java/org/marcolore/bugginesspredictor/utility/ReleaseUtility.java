package org.marcolore.bugginesspredictor.utility;

import org.json.JSONArray;
import org.marcolore.bugginesspredictor.model.Release;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public class ReleaseUtility {

    private ReleaseUtility() {
        throw new IllegalStateException("Utility class");
    }

    public static Release getReleaseByDate(LocalDateTime date, List<Release> releases) {
        for (Release release : releases) {

            if (!release.getReleaseDate().isBefore(date)) {
                return release;
            }
        }

        return null;
    }

    public static List<Release> getAffectedRelease(List<Release> releases, JSONArray affectedVersion) {
        ArrayList<Release> affectedRelease = new ArrayList<>();

        for (int i = 0; i < affectedVersion.length(); i++) {
            int versionId = affectedVersion.getJSONObject(i).getInt("id");

            for (Release release : releases) {
                if (Objects.equals(release.getIdRelease(), versionId)) {
                    affectedRelease.add(release);
                    break;
                }
            }
        }

        return affectedRelease;
    }


    public static Release getInjectedVersion(List<Release> releases){

        if (!releases.isEmpty() && releases.get(0) != null){
            releases.sort(Comparator.comparing(Release::getReleaseDate));
            return releases.get(0);
        } else{
            return null;
        }

    }

    public static Integer matchCommitsRelease(LocalDateTime date, List<Release> releases) {

        int releaseInd = 0;
        for (int i = 0; i<releases.size(); i++){

            if (date.isBefore(releases.get(i).getReleaseDate())) {
                releaseInd = releases.get(i).getId();
                break;
            }

            //If the commit is after the last release, associate it with the last release
            if(date.isAfter(releases.get(releases.size()-1).getReleaseDate())) {
                releaseInd = releases.get(releases.size()-1).getId();
            }
        }

        return releaseInd;

    }
}