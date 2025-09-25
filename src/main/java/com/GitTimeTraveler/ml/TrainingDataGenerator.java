package com.GitTimeTraveler.ml;

import java.io.*;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Training Data Generator for GitHub Contribution Pattern ML Model
 * 
 * This class generates training data by creating patterns for various text inputs
 * and learning optimal intensity distributions for different characters and contexts.
 */
public class TrainingDataGenerator {
    
    private static final String TRAINING_DATA_FILE = "training_data.txt";
    private static final String CHARACTER_PATTERNS_FILE = "character_patterns.txt";
    
    /**
     * Generate training data from a collection of text samples
     */
    public static void generateTrainingData(List<String> textSamples) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(TRAINING_DATA_FILE))) {
            writer.println("# GitHub Contribution Pattern Training Data");
            writer.println("# Format: TEXT|YEAR|PATTERN_DATA");
            writer.println();
            
            for (String text : textSamples) {
                generateTrainingSample(text, writer);
            }
            
            System.out.println("Training data generated: " + TRAINING_DATA_FILE);
            
        } catch (IOException e) {
            System.err.println("Error generating training data: " + e.getMessage());
        }
    }
    
    /**
     * Generate a single training sample
     */
    private static void generateTrainingSample(String text, PrintWriter writer) {
        int year = 2020 + ThreadLocalRandom.current().nextInt(5); // 2020-2024
        
        // Generate pattern with ML approach
        int[][] pattern = PatternGenerator.generatePattern(text, year);
        
        // Write training sample
        writer.println(text + "|" + year + "|" + patternToString(pattern));
    }
    
    /**
     * Convert pattern array to string format
     */
    private static String patternToString(int[][] pattern) {
        StringBuilder sb = new StringBuilder();
        for (int day = 0; day < 7; day++) {
            for (int week = 0; week < 53; week++) {
                sb.append(pattern[day][week]);
            }
            sb.append("\n");
        }
        return sb.toString().trim();
    }
    
    /**
     * Generate character-specific patterns for learning
     */
    public static void generateCharacterPatterns() {
        try (PrintWriter writer = new PrintWriter(new FileWriter(CHARACTER_PATTERNS_FILE))) {
            writer.println("# Character Pattern Learning Data");
            writer.println("# Format: CHARACTER|INTENSITY_DISTRIBUTION");
            writer.println();
            
            String alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
            
            for (char c : alphabet.toCharArray()) {
                generateCharacterPattern(c, writer);
            }
            
            System.out.println("Character patterns generated: " + CHARACTER_PATTERNS_FILE);
            
        } catch (IOException e) {
            System.err.println("Error generating character patterns: " + e.getMessage());
        }
    }
    
    /**
     * Generate pattern for a specific character
     */
    private static void generateCharacterPattern(char c, PrintWriter writer) {
        // Generate multiple samples for this character
        List<Integer> intensityDistribution = new ArrayList<>();
        
        for (int sample = 0; sample < 100; sample++) {
            String text = String.valueOf(c);
            int year = 2020 + ThreadLocalRandom.current().nextInt(5);
            int[][] pattern = PatternGenerator.generatePattern(text, year);
            
            // Collect intensity values
            for (int day = 0; day < 7; day++) {
                for (int week = 0; week < 53; week++) {
                    if (pattern[day][week] > 0) {
                        intensityDistribution.add(pattern[day][week]);
                    }
                }
            }
        }
        
        // Calculate intensity distribution
        int[] intensityCounts = new int[7]; // 0-6
        for (int intensity : intensityDistribution) {
            intensityCounts[intensity]++;
        }
        
        // Write character pattern
        writer.print(c + "|");
        for (int i = 0; i < intensityCounts.length; i++) {
            writer.print(intensityCounts[i]);
            if (i < intensityCounts.length - 1) {
                writer.print(",");
            }
        }
        writer.println();
    }
    
    /**
     * Generate sample training data
     */
    public static void generateSampleTrainingData() {
        List<String> sampleTexts = Arrays.asList(
            "HELLO", "WORLD", "GITHUB", "JAVA", "CODE", "PROGRAMMING",
            "PASINDU", "SAMPATH", "MACHINE", "LEARNING", "PATTERN",
            "CONTRIBUTION", "GRAPH", "GENERATOR", "INTENSITY",
            "NEURAL", "NETWORK", "ALGORITHM", "OPTIMIZATION",
            "SYMMETRY", "DENSITY", "CONTINUITY", "REALISTIC",
            "ACTIVITY", "SIMULATION", "PREDICTION", "CONTEXT"
        );
        
        generateTrainingData(sampleTexts);
        generateCharacterPatterns();
    }
    
    /**
     * Load training data from file
     */
    public static List<TrainingSample> loadTrainingData() {
        List<TrainingSample> samples = new ArrayList<>();
        
        try (Scanner scanner = new Scanner(new File(TRAINING_DATA_FILE))) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine().trim();
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }
                
                String[] parts = line.split("\\|");
                if (parts.length >= 3) {
                    String text = parts[0];
                    int year = Integer.parseInt(parts[1]);
                    int[][] pattern = stringToPattern(parts[2]);
                    
                    samples.add(new TrainingSample(text, year, pattern));
                }
            }
        } catch (FileNotFoundException e) {
            System.err.println("Training data file not found: " + TRAINING_DATA_FILE);
        }
        
        return samples;
    }
    
    /**
     * Convert string back to pattern array
     */
    private static int[][] stringToPattern(String patternString) {
        int[][] pattern = new int[7][53];
        String[] lines = patternString.split("\n");
        
        for (int day = 0; day < Math.min(7, lines.length); day++) {
            String line = lines[day];
            for (int week = 0; week < Math.min(53, line.length()); week++) {
                pattern[day][week] = Character.getNumericValue(line.charAt(week));
            }
        }
        
        return pattern;
    }
    
    /**
     * Training sample data structure
     */
    public static class TrainingSample {
        public final String text;
        public final int year;
        public final int[][] pattern;
        
        public TrainingSample(String text, int year, int[][] pattern) {
            this.text = text;
            this.year = year;
            this.pattern = pattern;
        }
        
        @Override
        public String toString() {
            return "TrainingSample{text='" + text + "', year=" + year + "}";
        }
    }
    
    /**
     * Main method for generating training data
     */
    public static void main(String[] args) {
        System.out.println("Generating training data for GitHub Contribution Pattern ML Model...");
        generateSampleTrainingData();
        System.out.println("Training data generation complete!");
    }
}
