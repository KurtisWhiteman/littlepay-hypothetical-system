package com.hypothetical.littlepay;

import com.hypothetical.littlepay.model.*;
import com.hypothetical.littlepay.service.TapService;
import com.hypothetical.littlepay.service.TripService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.*;
import java.util.List;

@SpringBootApplication
public class LittlePayApplication {

	private static TapService tapService;
	private static TripService tripService;

	public LittlePayApplication(TapService tapService, TripService tripService) {
		this.tapService = tapService;
		this.tripService = tripService;
	}

	/**
	 * @author Kurtis Whiteman
	 *
	 * Thank you for the opportunity. I hope you like my program :)
	 *
	 */
	public static void main(String[] args) throws IOException {
		SpringApplication.run(LittlePayApplication.class, args);
		System.out.println("Application started");

		// Moved filepath in here for better testing purposes
		String filePath = "src/main/resources/input/taps.csv";

		// Converts file data to list of Tap Objects
		System.out.println("Importing local 'trips.csv' file");
		List<TapRaw> taps = tapService.importTapsFile(filePath);

		// Determine Trips from Tap data
		System.out.println("Converting Taps to Trips");
		List<Trip> trips = tripService.tapsToTrips(taps);

		// Write Trips to local file (resources/output/trips.csv)
		System.out.println("Exporting Trips into local 'trips.csv' file");
		tripService.produceTripsCSV(trips);
		System.out.println("Finished.");
	}

}
