package org.marcolore.bugginesspredictor.model;

import java.time.LocalDate;
import java.util.ArrayList;

public class Ticket {

    private String key;

    private LocalDate creationDate;

    private Release openingVersion;

    private Release fixedVersion;

    private Release injectedVersion;

    private LocalDate resolutionDate;

    private ArrayList<Release> affectedReleases;


    public Ticket(String key, LocalDate creationDate, Release openingVersion, Release fixedVersion, Release injectedVersion, LocalDate resolutionDate, ArrayList<Release> affectedReleases) {
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

    public LocalDate getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(LocalDate creationDate) {
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

    public LocalDate getResolutionDate() {
        return resolutionDate;
    }

    public void setResolutionDate(LocalDate resolutionDate) {
        this.resolutionDate = resolutionDate;
    }

    public ArrayList<Release> getAffectedReleases() {
        return affectedReleases;
    }

    public void setAffectedReleases(ArrayList<Release> affectedReleases) {
        this.affectedReleases = affectedReleases;
    }
}
