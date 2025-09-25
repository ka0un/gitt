package com.GitTimeTraveler.service.impl;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import com.GitTimeTraveler.service.FileService;

/**
 * Implementation of FileService for file operations and pattern persistence
 */
public class FileServiceImpl implements FileService {
    
    private static final String PATTERNS_FILE = "saved_patterns.txt";
    private static final int GRID_ROWS = 7;
    private static final int GRID_COLS = 53;
    
    @Override
    public void savePattern(String patternName, String text, int year, int[][] patternData) throws Exception {
        // Load existing patterns
        Map<String, SavedPattern> patterns = loadAllPatterns();
        
        // Add new pattern
        patterns.put(patternName, new SavedPattern(patternName, text, year, patternData));
        
        // Save to file
        savePatternsToFile(patterns);
    }
    
    @Override
    public SavedPattern loadPattern(String patternName) throws Exception {
        Map<String, SavedPattern> patterns = loadAllPatterns();
        return patterns.get(patternName);
    }
    
    @Override
    public Map<String, SavedPattern> loadAllPatterns() throws IOException {
        Map<String, SavedPattern> patterns = new HashMap<>();
        Path filePath = Paths.get(PATTERNS_FILE);
        
        if (!Files.exists(filePath)) {
            return patterns;
        }
        
        try (Scanner scanner = new Scanner(filePath)) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine().trim();
                if (line.startsWith("PATTERN:")) {
                    String name = line.substring(8).trim();
                    String text = scanner.nextLine().trim();
                    int year = Integer.parseInt(scanner.nextLine().trim());
                    
                    int[][] pattern = new int[GRID_ROWS][GRID_COLS];
                    for (int i = 0; i < GRID_ROWS; i++) {
                        String patternLine = scanner.nextLine().trim();
                        for (int j = 0; j < GRID_COLS && j < patternLine.length(); j++) {
                            char c = patternLine.charAt(j);
                            if (c >= '0' && c <= '6') {
                                pattern[i][j] = c - '0';
                            } else {
                                pattern[i][j] = 0;
                            }
                        }
                    }
                    
                    patterns.put(name, new SavedPattern(name, text, year, pattern));
                }
            }
        }
        
        return patterns;
    }
    
    @Override
    public String[] getAvailablePatternNames() throws Exception {
        Map<String, SavedPattern> patterns = loadAllPatterns();
        return patterns.keySet().toArray(new String[0]);
    }
    
    @Override
    public void deletePattern(String patternName) throws Exception {
        Map<String, SavedPattern> patterns = loadAllPatterns();
        patterns.remove(patternName);
        savePatternsToFile(patterns);
    }
    
    private void savePatternsToFile(Map<String, SavedPattern> patterns) throws IOException {
        try (FileWriter writer = new FileWriter(PATTERNS_FILE)) {
            for (SavedPattern pattern : patterns.values()) {
                writer.write("PATTERN: " + pattern.name + "\n");
                writer.write(pattern.text + "\n");
                writer.write(pattern.year + "\n");
                
                for (int i = 0; i < GRID_ROWS; i++) {
                    for (int j = 0; j < GRID_COLS; j++) {
                        writer.write(String.valueOf(pattern.pattern[i][j]));
                    }
                    writer.write("\n");
                }
            }
        }
    }
}
