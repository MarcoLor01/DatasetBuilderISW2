package org.marcolore.bugginesspredictor.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;

public class Ticket {

    private String key;

    private LocalDateTime creationDate;

    private Release openingVersion;

    private Release fixedVersion;

    private Release injectedVersion;

    private LocalDateTime resolutionDate;

    private ArrayList<Release> affectedReleases;


    public Ticket(String key, LocalDateTime creationDate, Release openingVersion, Release fixedVersion, Release injectedVersion, LocalDateTime resolutionDate, ArrayList<Release> affectedReleases) {
        this.key = key;
        this.creationDate = creationDate;
        this.openingVersion = openingVersion;
        this.fixedVersion = fixedVersion;
        this.injectedVersion = injectedVersion;
        this.resolutionDate = resolutionDate;
        this.affectedReleases = affectedReleases;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public LocalDateTime getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(LocalDateTime creationDate) {
        this.creationDate = creationDate;
    }

    public Release getOpeningVersion() {
        return openingVersion;
    }

    public void setOpeningVersion(Release openingVersion) {
        this.openingVersion = openingVersion;
    }

    public Release getFixedVersion() {
        return fixedVersion;
    }

    public void setFixedVersion(Release fixedVersion) {
        this.fixedVersion = fixedVersion;
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

    public void setResolutionDate(LocalDateTime resolutionDate) {
        this.resolutionDate = resolutionDate;
    }

    public ArrayList<Release> getAffectedReleases() {
        return affectedReleases;
    }

    public void setAffectedReleases(ArrayList<Release> affectedReleases) {
        this.affectedReleases = affectedReleases;
    }
}
