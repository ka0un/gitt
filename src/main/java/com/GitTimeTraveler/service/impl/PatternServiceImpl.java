package com.GitTimeTraveler.service.impl;

import java.awt.Color;

import com.GitTimeTraveler.ml.PatternGenerator;
import com.GitTimeTraveler.service.PatternService;

/**
 * Implementation of PatternService for pattern management and ML operations
 */
public class PatternServiceImpl implements PatternService {
    
    private static final int GRID_ROWS = 7;
    private static final int GRID_COLS = 53;
    
    @Override
    public int[][] generateMLPattern(String text, int year) {
        if (text == null || text.trim().isEmpty()) {
            return createEmptyPattern();
        }
        
        return PatternGenerator.generatePattern(text, year);
    }
    
    @Override
    public int[][] generateCustomPattern(String text, int year, double density, double symmetry, double continuity) {
        if (text == null || text.trim().isEmpty()) {
            text = "OPTIMIZED"; // Default text for optimization
        }
        
        return PatternGenerator.generateCustomPattern(text, year, density, symmetry, continuity);
    }
    
    @Override
    public void clearPattern(int[][] patternData) {
        for (int row = 0; row < GRID_ROWS; row++) {
            for (int col = 0; col < GRID_COLS; col++) {
                patternData[row][col] = 0;
            }
        }
    }
    
    @Override
    public int getActiveCellsCount(int[][] patternData) {
        int count = 0;
        for (int row = 0; row < GRID_ROWS; row++) {
            for (int col = 0; col < GRID_COLS; col++) {
                if (patternData[row][col] > 0) {
                    count++;
                }
            }
        }
        return count;
    }
    
    @Override
    public void updateCellIntensity(int[][] patternData, int row, int col, int intensity) {
        if (isValidPosition(row, col)) {
            patternData[row][col] = Math.max(0, Math.min(6, intensity));
        }
    }
    
    @Override
    public int getCellIntensity(int[][] patternData, int row, int col) {
        if (isValidPosition(row, col)) {
            return patternData[row][col];
        }
        return 0;
    }
    
    @Override
    public Color getIntensityColor(int intensity) {
        if (intensity == 0) {
            return Color.WHITE;
        }
        
        // Create green colors with different intensities
        // Base green: RGB(34, 197, 94) - GitHub's green
        // Intensity 1: Very light green
        // Intensity 6: Full GitHub green
        float factor = intensity / 6.0f;
        
        int red = (int) (255 - (255 - 34) * factor);
        int green = (int) (255 - (255 - 197) * factor);
        int blue = (int) (255 - (255 - 94) * factor);
        
        return new Color(red, green, blue);
    }
    
    private boolean isValidPosition(int row, int col) {
        return row >= 0 && row < GRID_ROWS && col >= 0 && col < GRID_COLS;
    }
    
    private int[][] createEmptyPattern() {
        return new int[GRID_ROWS][GRID_COLS];
    }
}
