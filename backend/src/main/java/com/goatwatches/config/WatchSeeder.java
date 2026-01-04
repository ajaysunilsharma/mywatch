package com.goatwatches.config;

import com.goatwatches.entity.Watch;
import com.goatwatches.repository.WatchRepository;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;

import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

@Component
public class WatchSeeder implements CommandLineRunner {

    private final WatchRepository watchRepository;
    private final Random random = new Random();

    @Value("${app.seeder.allowed-brands:}")
    private String[] allowedBrands;

    @Value("${app.seeder.enabled:true}")
    private boolean seederEnabled;

    public WatchSeeder(WatchRepository watchRepository) {
        this.watchRepository = watchRepository;
    }

    @Override
    public void run(String... args) {
        if(!seederEnabled){
            System.out.println("Seeding is disabled via property");
            return;
        }
        seedWatches();
    }

    private void seedWatches() {

        try {
            System.out.println("Seeding database from watches_new.csv...");
            ClassPathResource resource = new ClassPathResource("watches_new.csv");
            
            // Skip header row
            CSVReader csvReader = new CSVReaderBuilder(new InputStreamReader(resource.getInputStream()))
                    .withSkipLines(1) 
                    .build();

            List<String[]> records = csvReader.readAll();
            String[] modelTypes = {"Chronograph", "Diver", "Pilot", "Field", "Dress", "GMT", "Tourbillon", "Calendar", "Sport", "Automatic", "Quartz"};
            String[] adjectives = {"Classic", "Modern", "Vintage", "Royal", "Grand", "Super", "Pro", "Master", "Elite", "Heritage"};

            for (String[] record : records) {
                // CSV Format: URL, Label
                if (record.length < 2) continue;
                
                String imageUrl = record[0];
                String brand = record[1];

                // Filter by allowed brands if property is set
                if (allowedBrands != null && allowedBrands.length > 0 && !allowedBrands[0].isEmpty()) {
                    boolean isAllowed = Arrays.stream(allowedBrands)
                            .map(String::trim)
                            .anyMatch(b -> b.equalsIgnoreCase(brand.trim()));
                    if (!isAllowed) continue;
                }
                
                // Generate Random Data
                String model = adjectives[random.nextInt(adjectives.length)] + " " + modelTypes[random.nextInt(modelTypes.length)];
                
                // Extract reference from URL (id parameter) to be deterministic
                String ref;
                int idIndex = imageUrl.indexOf("id=");
                if (idIndex != -1) {
                    int start = idIndex + 3;
                    int end = imageUrl.indexOf("&", start);
                    ref = (end != -1) ? imageUrl.substring(start, end) : imageUrl.substring(start);
                } else {
                    ref = imageUrl.length() > 20 ? imageUrl.substring(imageUrl.length() - 20) : imageUrl;
                }

                int year = 1980 + random.nextInt(44); // 1980 - 2023
                String price = "$" + (500 + random.nextInt(49500));
                String desc = "A stunning " + year + " " + brand + " " + model + ". Features exquisite craftsmanship and timeless design.";

                if (!watchRepository.existsByReferenceNumber(ref)) {
                    Watch watch = new Watch();
                    watch.setBrand(brand);
                    watch.setModel(model);
                    watch.setReferenceNumber(ref);
                    watch.setYear(year);
                    watch.setPrice(price);
                    watch.setThumbnailUrl(imageUrl);
                    watch.setDescription(desc);
                    watch.setCreatedBy("System Seeder");
                    
                    watchRepository.save(watch);
                }
            }
            System.out.println("Seeding completed. Added " + records.size() + " watches.");
        } catch (Exception e) {
            System.err.println("Failed to seed watches: " + e.getMessage());
            e.printStackTrace();
        }
    }
}