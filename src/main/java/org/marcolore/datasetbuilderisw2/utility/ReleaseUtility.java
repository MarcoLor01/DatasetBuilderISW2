package org.marcolore.datasetbuilderisw2.utility;

import org.json.JSONArray;
import org.marcolore.datasetbuilderisw2.model.JavaClass;
import org.marcolore.datasetbuilderisw2.model.Release;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class ReleaseUtility {

    private static final Logger logger = LoggerFactory.getLogger(ReleaseUtility.class);

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

    public static Release releaseFromId(List<Release> releases, int releaseId){
        Map<Integer, Release> releaseMap = releases.stream()
                .collect(Collectors.toMap(Release::getId, r -> r));

        return  releaseMap.get(releaseId);
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

    public static List<Release> removeHalfReleases(List<Release> releases) {
        int halfReleasesNumber = releases.size() / 2;
        List<Release> firstHalf = new ArrayList<>();
        for(int i=0; i< halfReleasesNumber; i++){
            firstHalf.add(releases.get(i));
        }

        return firstHalf;
    }

    public static void checkIfNewClassTouched(List<Release> releaseList){

        for(int i=0;i<releaseList.size();i++){
            List<JavaClass> classList = releaseList.get(i).getJavaClassList();
            if(classList.isEmpty() && i != 0){
                releaseList.get(i).setJavaClassList(releaseList.get(i-1).getJavaClassList());
                logger.info("Release {} have {0 class", i);
            } else if(classList.isEmpty()) {
                logger.error("First release have 0 class");
            } else{
                logger.info("Release {} have {} class", i, classList.size());
            }
        }
    }

}