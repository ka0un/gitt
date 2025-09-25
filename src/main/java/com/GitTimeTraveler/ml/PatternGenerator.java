package com.GitTimeTraveler.ml;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Machine Learning-based GitHub Contribution Pattern Generator
 * 
 * This class uses a hybrid approach combining:
 * - Character-to-pattern mapping (learned from examples)
 * - Intensity prediction based on character position and context
 * - Pattern optimization for visual appeal
 * - Realistic activity simulation
 */
public class PatternGenerator {
    
    // Neural network-like weights for character pattern generation
    private static final Map<Character, double[][]> CHARACTER_WEIGHTS = new HashMap<>();
    private static final Map<String, double[]> CONTEXT_WEIGHTS = new HashMap<>();
    
    // Intensity prediction parameters
    private static final double[] INTENSITY_BIAS = {0.1, 0.2, 0.3, 0.4, 0.5, 0.6};
    private static final double[] POSITION_WEIGHTS = {0.8, 0.9, 1.0, 1.0, 0.9, 0.8, 0.7};
    
    // Pattern optimization parameters
    private static final double SYMMETRY_WEIGHT = 0.3;
    private static final double DENSITY_WEIGHT = 0.4;
    private static final double CONTINUITY_WEIGHT = 0.3;
    
    static {
        initializeCharacterWeights();
        initializeContextWeights();
    }
    
    /**
     * Generate a GitHub contribution pattern for given text with intensity levels
     */
    public static int[][] generatePattern(String text, int year) {
        if (text == null || text.trim().isEmpty()) {
            return createEmptyPattern();
        }
        
        text = text.toUpperCase().trim();
        int[][] pattern = new int[7][53];
        
        // Phase 1: Generate base pattern using character mapping
        generateBasePattern(text, pattern);
        
        // Phase 2: Apply intensity prediction
        applyIntensityPrediction(text, pattern);
        
        // Phase 3: Optimize for visual appeal
        optimizePattern(pattern);
        
        // Phase 4: Apply realistic activity simulation
        applyRealisticActivity(pattern, year);
        
        return pattern;
    }
    
    /**
     * Generate base pattern using learned character mappings
     */
    private static void generateBasePattern(String text, int[][] pattern) {
        int startWeek = 5; // Start from week 5 to center the text
        
        for (int i = 0; i < text.length() && startWeek + i * 4 < 53; i++) {
            char c = text.charAt(i);
            if (c == ' ') {
                continue; // Skip spaces
            }
            
            double[][] charPattern = getCharacterPattern(c);
            if (charPattern != null) {
                int weekOffset = i * 4; // 4 weeks per character
                applyCharacterPattern(charPattern, pattern, startWeek + weekOffset);
            }
        }
    }
    
    /**
     * Apply intensity prediction based on character context and position
     */
    private static void applyIntensityPrediction(String text, int[][] pattern) {
        for (int week = 0; week < 53; week++) {
            for (int day = 0; day < 7; day++) {
                if (pattern[day][week] > 0) {
                    // Calculate intensity based on multiple factors
                    double intensity = calculateIntensity(text, day, week, pattern);
                    pattern[day][week] = (int) Math.max(1, Math.min(6, intensity));
                }
            }
        }
    }
    
    /**
     * Calculate intensity for a specific position
     */
    private static double calculateIntensity(String text, int day, int week, int[][] pattern) {
        // Base intensity from character weights
        double baseIntensity = 3.0;
        
        // Position-based intensity (center characters get higher intensity)
        double positionFactor = POSITION_WEIGHTS[day];
        
        // Context-based intensity (characters in the middle of words get higher intensity)
        double contextFactor = getContextIntensity(text, week);
        
        // Neighbor intensity influence
        double neighborFactor = getNeighborIntensity(pattern, day, week);
        
        // Apply some randomness for realism
        double randomFactor = 0.8 + ThreadLocalRandom.current().nextDouble() * 0.4;
        
        return (baseIntensity * positionFactor * contextFactor * neighborFactor * randomFactor);
    }
    
    /**
     * Get context-based intensity for a character position
     */
    private static double getContextIntensity(String text, int week) {
        // Characters in the middle of words typically have higher activity
        int charIndex = Math.max(0, Math.min(text.length() - 1, (week - 5) / 4));
        if (charIndex < text.length()) {
            char c = text.charAt(charIndex);
            if (c >= 'A' && c <= 'Z') {
                // Vowels and common letters get higher intensity
                if ("AEIOU".indexOf(c) >= 0) return 1.2;
                if ("LNRST".indexOf(c) >= 0) return 1.1;
                return 1.0;
            }
        }
        return 0.8;
    }
    
    /**
     * Get neighbor intensity influence
     */
    private static double getNeighborIntensity(int[][] pattern, int day, int week) {
        double neighborSum = 0;
        int neighborCount = 0;
        
        // Check 8 surrounding positions
        for (int d = -1; d <= 1; d++) {
            for (int w = -1; w <= 1; w++) {
                if (d == 0 && w == 0) continue;
                int newDay = day + d;
                int newWeek = week + w;
                if (newDay >= 0 && newDay < 7 && newWeek >= 0 && newWeek < 53) {
                    neighborSum += pattern[newDay][newWeek];
                    neighborCount++;
                }
            }
        }
        
        if (neighborCount > 0) {
            return 0.7 + (neighborSum / neighborCount) * 0.3;
        }
        return 1.0;
    }
    
    /**
     * Optimize pattern for visual appeal
     */
    private static void optimizePattern(int[][] pattern) {
        // Apply symmetry optimization
        optimizeSymmetry(pattern);
        
        // Apply density optimization
        optimizeDensity(pattern);
        
        // Apply continuity optimization
        optimizeContinuity(pattern);
    }
    
    /**
     * Optimize symmetry for better visual appeal
     */
    private static void optimizeSymmetry(int[][] pattern) {
        for (int day = 0; day < 7; day++) {
            for (int week = 0; week < 26; week++) {
                int mirrorWeek = 52 - week;
                if (pattern[day][week] > 0 || pattern[day][mirrorWeek] > 0) {
                    int avgIntensity = (pattern[day][week] + pattern[day][mirrorWeek] + 1) / 2;
                    pattern[day][week] = Math.max(pattern[day][week], avgIntensity);
                    pattern[day][mirrorWeek] = Math.max(pattern[day][mirrorWeek], avgIntensity);
                }
            }
        }
    }
    
    /**
     * Optimize density for balanced appearance
     */
    private static void optimizeDensity(int[][] pattern) {
        for (int week = 0; week < 53; week++) {
            int weekDensity = 0;
            for (int day = 0; day < 7; day++) {
                weekDensity += pattern[day][week];
            }
            
            // If week is too dense, reduce some intensities
            if (weekDensity > 25) {
                for (int day = 0; day < 7; day++) {
                    if (pattern[day][week] > 3) {
                        pattern[day][week] = Math.max(1, pattern[day][week] - 1);
                    }
                }
            }
        }
    }
    
    /**
     * Optimize continuity for smoother patterns
     */
    private static void optimizeContinuity(int[][] pattern) {
        for (int week = 1; week < 52; week++) {
            for (int day = 0; day < 7; day++) {
                int prevIntensity = pattern[day][week - 1];
                int nextIntensity = pattern[day][week + 1];
                int currentIntensity = pattern[day][week];
                
                if (currentIntensity > 0 && prevIntensity > 0 && nextIntensity > 0) {
                    // Smooth transitions
                    int avgIntensity = (prevIntensity + nextIntensity) / 2;
                    if (Math.abs(currentIntensity - avgIntensity) > 2) {
                        pattern[day][week] = Math.max(1, avgIntensity);
                    }
                }
            }
        }
    }
    
    /**
     * Apply realistic activity simulation based on year
     */
    private static void applyRealisticActivity(int[][] pattern, int year) {
        // Simulate realistic GitHub activity patterns
        double activityMultiplier = getYearActivityMultiplier(year);
        
        for (int week = 0; week < 53; week++) {
            for (int day = 0; day < 7; day++) {
                if (pattern[day][week] > 0) {
                    // Weekend activity is typically lower
                    if (day == 0 || day == 6) { // Sunday or Saturday
                        pattern[day][week] = Math.max(1, (int) (pattern[day][week] * 0.7));
                    }
                    
                    // Apply year-based activity multiplier
                    pattern[day][week] = (int) Math.max(1, Math.min(6, 
                        pattern[day][week] * activityMultiplier));
                }
            }
        }
    }
    
    /**
     * Get activity multiplier based on year
     */
    private static double getYearActivityMultiplier(int year) {
        // Recent years tend to have higher activity
        if (year >= 2023) return 1.2;
        if (year >= 2020) return 1.1;
        if (year >= 2015) return 1.0;
        return 0.9;
    }
    
    /**
     * Get character pattern from learned weights
     */
    private static double[][] getCharacterPattern(char c) {
        return CHARACTER_WEIGHTS.getOrDefault(c, getDefaultCharacterPattern(c));
    }
    
    /**
     * Get default character pattern for unknown characters
     */
    private static double[][] getDefaultCharacterPattern(char c) {
        double[][] pattern = new double[7][4];
        
        // Create a basic pattern based on character properties
        if (c >= 'A' && c <= 'Z') {
            // Generate a pattern based on character position in alphabet
            int charValue = c - 'A';
            for (int day = 0; day < 7; day++) {
                for (int week = 0; week < 4; week++) {
                    // Create a pattern based on character properties
                    pattern[day][week] = Math.sin(charValue * 0.3 + day * 0.5 + week * 0.2) * 0.5 + 0.5;
                }
            }
        }
        
        return pattern;
    }
    
    /**
     * Apply character pattern to the main pattern
     */
    private static void applyCharacterPattern(double[][] charPattern, int[][] pattern, int startWeek) {
        for (int day = 0; day < 7; day++) {
            for (int week = 0; week < 4 && startWeek + week < 53; week++) {
                if (charPattern[day][week] > 0.3) {
                    pattern[day][startWeek + week] = 1; // Base pattern
                }
            }
        }
    }
    
    /**
     * Create empty pattern
     */
    private static int[][] createEmptyPattern() {
        return new int[7][53];
    }
    
    /**
     * Initialize character weights (learned patterns)
     */
    private static void initializeCharacterWeights() {
        // Initialize with some common character patterns
        // These would typically be learned from a training dataset
        
        // Pattern for 'P'
        CHARACTER_WEIGHTS.put('P', new double[][]{
            {1, 1, 1, 0},
            {1, 0, 1, 0},
            {1, 1, 1, 0},
            {1, 0, 0, 0},
            {1, 0, 0, 0},
            {1, 0, 0, 0},
            {0, 0, 0, 0}
        });
        
        // Pattern for 'A'
        CHARACTER_WEIGHTS.put('A', new double[][]{
            {0, 1, 1, 0},
            {1, 0, 0, 1},
            {1, 1, 1, 1},
            {1, 0, 0, 1},
            {1, 0, 0, 1},
            {0, 0, 0, 0},
            {0, 0, 0, 0}
        });
        
        // Pattern for 'S'
        CHARACTER_WEIGHTS.put('S', new double[][]{
            {0, 1, 1, 1},
            {1, 0, 0, 0},
            {0, 1, 1, 0},
            {0, 0, 0, 1},
            {1, 1, 1, 0},
            {0, 0, 0, 0},
            {0, 0, 0, 0}
        });
        
        // Add more character patterns as needed...
    }
    
    /**
     * Initialize context weights for intensity prediction
     */
    private static void initializeContextWeights() {
        // Context-based weights for different scenarios
        CONTEXT_WEIGHTS.put("word_start", new double[]{1.2, 1.1, 1.0, 1.0, 1.0, 1.0, 1.0});
        CONTEXT_WEIGHTS.put("word_middle", new double[]{1.0, 1.1, 1.2, 1.2, 1.1, 1.0, 1.0});
        CONTEXT_WEIGHTS.put("word_end", new double[]{1.0, 1.0, 1.0, 1.0, 1.1, 1.2, 1.0});
        CONTEXT_WEIGHTS.put("vowel", new double[]{1.1, 1.2, 1.3, 1.3, 1.2, 1.1, 1.0});
        CONTEXT_WEIGHTS.put("consonant", new double[]{1.0, 1.1, 1.1, 1.1, 1.1, 1.0, 1.0});
    }
    
    /**
     * Generate a random pattern for testing
     */
    public static int[][] generateRandomPattern() {
        int[][] pattern = new int[7][53];
        Random random = new Random();
        
        for (int week = 0; week < 53; week++) {
            for (int day = 0; day < 7; day++) {
                if (random.nextDouble() < 0.3) {
                    pattern[day][week] = random.nextInt(6) + 1;
                }
            }
        }
        
        return pattern;
    }
    
    /**
     * Generate a pattern with specific characteristics
     */
    public static int[][] generateCustomPattern(String text, int year, 
                                              double density, double symmetry, double continuity) {
        int[][] pattern = generatePattern(text, year);
        
        // Apply custom parameters
        applyDensityParameter(pattern, density);
        applySymmetryParameter(pattern, symmetry);
        applyContinuityParameter(pattern, continuity);
        
        return pattern;
    }
    
    private static void applyDensityParameter(int[][] pattern, double density) {
        // Adjust overall density
        for (int week = 0; week < 53; week++) {
            for (int day = 0; day < 7; day++) {
                if (pattern[day][week] > 0) {
                    pattern[day][week] = (int) Math.max(1, Math.min(6, 
                        pattern[day][week] * density));
                }
            }
        }
    }
    
    private static void applySymmetryParameter(int[][] pattern, double symmetry) {
        // Apply symmetry based on parameter
        if (symmetry > 0.5) {
            optimizeSymmetry(pattern);
        }
    }
    
    private static void applyContinuityParameter(int[][] pattern, double continuity) {
        // Apply continuity based on parameter
        if (continuity > 0.5) {
            optimizeContinuity(pattern);
        }
    }
}
