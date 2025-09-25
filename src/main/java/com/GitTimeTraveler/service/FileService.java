package com.GitTimeTraveler.service;

import java.util.Map;

/**
 * Service interface for file operations and pattern persistence
 */
public interface FileService {
    
    /**
     * Save pattern to file
     */
    void savePattern(String patternName, String text, int year, int[][] patternData) throws Exception;
    
    /**
     * Load pattern from file
     */
    SavedPattern loadPattern(String patternName) throws Exception;
    
    /**
     * Load all patterns from file
     */
    Map<String, SavedPattern> loadAllPatterns() throws Exception;
    
    /**
     * Get all available pattern names
     */
    String[] getAvailablePatternNames() throws Exception;
    
    /**
     * Delete pattern from file
     */
    void deletePattern(String patternName) throws Exception;
    
    /**
     * Saved pattern data structure
     */
    class SavedPattern {
        public final String name;
        public final String text;
        public final int year;
        public final int[][] pattern;
        
        public SavedPattern(String name, String text, int year, int[][] pattern) {
            this.name = name;
            this.text = text;
            this.year = year;
            this.pattern = new int[7][53];
            for (int i = 0; i < 7; i++) {
                for (int j = 0; j < 53; j++) {
                    this.pattern[i][j] = pattern[i][j];
                }
            }
        }
    }
}
