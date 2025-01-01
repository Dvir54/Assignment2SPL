package bgu.spl.mics.application.parse;
import bgu.spl.mics.application.objects.FusionSlam;
import bgu.spl.mics.application.objects.LandMark;
import bgu.spl.mics.application.objects.StatisticalFolder;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;


public class JsonOutput {

    public static void writeStatisticalFolderToJson(String outputFilePath) {
        StatisticalFolder folder = StatisticalFolder.getInstance();

        // Converts to json
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String jsonOutput = gson.toJson(folder);
        System.out.println(jsonOutput);

        //Writes to file
        try (FileWriter writer = new FileWriter(outputFilePath)) {
            writer.write(jsonOutput);
        } catch (IOException e) {
            System.err.println("Error writing JSON to file: " + e.getMessage());
        }
    }

    public static void writeLandMarksMapToJson(String outputFilePath) {
        List<LandMark> folder = FusionSlam.getInstance().getLandmarks();

        //Converts to json
//        Gson gson2 = new GsonBuilder().setPrettyPrinting().create();
        Gson gson2 = new Gson();
        String jsonOutput1 = gson2.toJson(folder);
        System.out.println(jsonOutput1);

        // writes to file
        try (FileWriter writer = new FileWriter(outputFilePath)) {
            writer.write(jsonOutput1);
        } catch (IOException e) {
            System.err.println("Error writing JSON to file: " + e.getMessage());
        }
    }
}
