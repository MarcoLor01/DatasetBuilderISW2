package org.marcolore.datasetbuilderisw2.utility;

import org.eclipse.jgit.revwalk.RevCommit;
import org.marcolore.datasetbuilderisw2.controller.GitController;
import org.marcolore.datasetbuilderisw2.model.JavaClass;
import org.marcolore.datasetbuilderisw2.model.Release;
import org.marcolore.datasetbuilderisw2.model.Ticket;

import java.io.IOException;
import java.util.List;

public class ClassUtility {

    private ClassUtility() {
    }

    public static void setBuggyOrNot(List<Ticket> ticketList, List<JavaClass> allProjectClasses, GitController gitController, List<Release> releaseList) throws IOException {

        for(JavaClass projectClass : allProjectClasses){
            projectClass.setBuggy(false);

        }


        for (Ticket ticket : ticketList) {
            List<RevCommit> ticketCommitList = ticket.getCommitList();
            Release injectedVersion = ticket.getInjectedVersion();

            for (RevCommit commit : ticketCommitList) {

                Release releaseOfCommit = GitController.findReleaseFromCommit(commit, releaseList);

                List<String> modifiedClassesNames = gitController.findTouchedClassFromCommit(commit);

                for (String modifiedClass : modifiedClassesNames) {
                    labelBuggyClasses(modifiedClass, injectedVersion, releaseOfCommit, allProjectClasses, commit);
                }
            }
        }

    }

    private static void labelBuggyClasses(String modifiedClass, Release injectedVersion, Release fixedVersion, List<JavaClass> allProjectClasses, RevCommit commit) {

        for (JavaClass projectClass : allProjectClasses) {

            if (projectClass.getListOfCommit().contains(commit) && !projectClass.getFixCommits().contains(commit)) {
                projectClass.addFixCommit(commit);
            }
            if (isClassModifiedInBuggyVersion(projectClass, modifiedClass, injectedVersion, fixedVersion)) {
                projectClass.setBuggy(true);
            }
        }

    }

    private static boolean isClassModifiedInBuggyVersion(JavaClass projectClass, String modifiedClass, Release injectedVersion, Release fixedVersion) {
        return projectClass.getClassName().equals(modifiedClass) &&
                projectClass.getRelease().getId() < fixedVersion.getId() &&
                projectClass.getRelease().getId() >= injectedVersion.getId();
    }
}
