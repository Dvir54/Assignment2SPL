package bgu.spl.mics.application;
import bgu.spl.mics.application.parse.JSONInput;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.Reader;
import java.nio.file.Paths;

/**
 * The main entry point for the GurionRock Pro Max Ultra Over 9000 simulation.
 * <p>
 * This class initializes the system and starts the simulation by setting up
 * services, objects, and configurations.
 * </p>
 */
public class GurionRockRunner {

    /**
     * The main method of the simulation.
     * This method sets up the necessary components, parses configuration files,
     * initializes services, and starts the simulation.
     *
     * @param args Command-line arguments. The first argument is expected to be the path to the configuration file.
     */
    public static void main(String[] args) {
        System.out.println("Hello World!");

        // TODO: Parse configuration file.
        JSONInput input = null;
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        Reader reader = files.newBufferedReader(Paths.get(args[0]));
        input = gson.fromJson(reader, JSONInput.class);
        reader.close();
        // TODO: Initialize system components and services.
        // TODO: Start the simulation.
    }
}
