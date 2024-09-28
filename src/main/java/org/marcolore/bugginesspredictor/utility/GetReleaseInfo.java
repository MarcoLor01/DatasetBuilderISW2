package org.marcolore.bugginesspredictor.utility;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.marcolore.bugginesspredictor.model.Release;

import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.marcolore.bugginesspredictor.utility.JsonUtility.readJsonFromUrl;


public class GetReleaseInfo {
	
	   private static Map<LocalDateTime, String> releaseNames;
	   private static Map<LocalDateTime, String> releaseID;
	   private static List<LocalDateTime> releases;
	   public static Integer numVersions;

	public static void main(String[] args) throws IOException, JSONException {
		   
		   String projName ="BOOKKEEPER";
		 //Fills the arraylist with releases dates and orders them
		   //Ignores releases with missing dates
		   releases = new ArrayList<>();
		         int i;
		         String url = "https://issues.apache.org/jira/rest/api/2/project/" + projName;
		         JSONObject json = readJsonFromUrl(url);
		         JSONArray versions = json.getJSONArray("versions");
		         releaseNames = new HashMap<>();
		         releaseID = new HashMap<>();
		         for (i = 0; i < versions.length(); i++ ) {
		            String name = "";
		            String id = "";
		            if(versions.getJSONObject(i).has("releaseDate")) {
		               if (versions.getJSONObject(i).has("name"))
		                  name = versions.getJSONObject(i).get("name").toString();
		               if (versions.getJSONObject(i).has("id"))
		                  id = versions.getJSONObject(i).get("id").toString();
		            }
		         }
		         // order releases by date
        //@Override
        releases.sort(Comparator.naturalOrder());
		         if (releases.size() < 6)
		            return;
		         FileWriter fileWriter = null;
			 try {
                 String outname = projName + "VersionInfo.csv";
						    //Name of CSV for output
						    fileWriter = new FileWriter(outname);
		            fileWriter.append("Index,Version ID,Version Name,Date");
		            fileWriter.append("\n");
		            numVersions = releases.size();
		            for ( i = 0; i < releases.size(); i++) {
		               int index = i + 1;
		               fileWriter.append(Integer.toString(index));
		               fileWriter.append(",");
		               fileWriter.append(releaseID.get(releases.get(i)));
		               fileWriter.append(",");
		               fileWriter.append(releaseNames.get(releases.get(i)));
		               fileWriter.append(",");
		               fileWriter.append(releases.get(i).toString());
		               fileWriter.append("\n");
		            }

		         } catch (Exception e) {
		            System.out.println("Error in csv writer");
		            e.printStackTrace();
		         } finally {
		            try {
                        assert fileWriter != null;
                        fileWriter.flush();
		               fileWriter.close();
		            } catch (IOException e) {
		               System.out.println("Error while flushing/closing fileWriter !!!");
		               e.printStackTrace();
		            }
		         }
    }




	   


	
}