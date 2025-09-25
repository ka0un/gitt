package com.GitTimeTraveler.service;

/**
 * Service interface for pattern management and ML operations
 */
public interface PatternService {
    
    /**
     * Generate pattern using Machine Learning
     */
    int[][] generateMLPattern(String text, int year);
    
    /**
     * Generate pattern with custom parameters
     */
    int[][] generateCustomPattern(String text, int year, double density, double symmetry, double continuity);
    
    /**
     * Clear pattern data
     */
    void clearPattern(int[][] patternData);
    
    /**
     * Get active cells count
     */
    int getActiveCellsCount(int[][] patternData);
    
    /**
     * Update cell intensity
     */
    void updateCellIntensity(int[][] patternData, int row, int col, int intensity);
    
    /**
     * Get cell intensity
     */
    int getCellIntensity(int[][] patternData, int row, int col);
    
    /**
     * Apply intensity color mapping
     */
    java.awt.Color getIntensityColor(int intensity);
}
