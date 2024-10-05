package org.marcolore.bugginesspredictor.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Ticket {

    private String key;

    private final Release openingVersion;

    private final Release fixedVersion;

    private Release injectedVersion;

    private LocalDateTime creationDate = LocalDateTime.MIN;

    private LocalDateTime resolutionDate = LocalDateTime.MIN;

    private List<Release> affectedReleases;


    public Ticket(String key, Release openingVersion, Release fixedVersion, Release injectedVersion, List<Release> affectedReleases) {
        this.key = key;
        this.openingVersion = openingVersion;
        this.fixedVersion = fixedVersion;
        this.injectedVersion = injectedVersion;
        this.affectedReleases = affectedReleases;
    }

    public Release getOpeningVersion() {
        return openingVersion;
    }

    public Release getFixedVersion() {
        return fixedVersion;
    }

    public Release getInjectedVersion() {
        return injectedVersion;
    }

    public void setInjectedVersion(Release injectedVersion) {
        this.injectedVersion = injectedVersion;
    }

    public LocalDateTime getResolutionDate() {
        return resolutionDate;
    }


    public void setAffectedReleases(List<Release> affectedReleases) {
        this.affectedReleases = affectedReleases;
    }

    public void addAffectedReleases(Release affectedRelease) {
        if (this.affectedReleases != null) {
            this.affectedReleases.add(affectedRelease);
        } else {
            this.affectedReleases = new ArrayList<>();
            this.affectedReleases.add(affectedRelease);
        }
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public List<Release> getAffectedReleases() {
        return affectedReleases;
    }

    public LocalDateTime getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(LocalDateTime creationDate) {
        this.creationDate = creationDate;
    }

    public void setResolutionDate (LocalDateTime resolutionDate) {
        this.resolutionDate = resolutionDate;
    }

}
