package com.GitTimeTraveler.service;

import java.time.LocalDate;

/**
 * Service interface for Git operations
 */
public interface GitService {
    
    /**
     * Generate commit commands for a pattern
     */
    String generateCommitCommands(int[][] patternData, int year, String text);
    
    /**
     * Create real commits for a pattern
     */
    void createRealCommits(int[][] patternData, int year, String text, 
                          ProgressCallback progressCallback, CompletionCallback completionCallback);
    
    /**
     * Get commits for a specific year
     */
    String getCommitsByYear(int year) throws Exception;
    
    /**
     * Delete commits for a specific year
     */
    int deleteCommitsByYear(int year) throws Exception;
    
    /**
     * Save commit commands to batch file
     */
    void saveCommitCommandsToFile(String commands, LocalDate firstSunday) throws Exception;
    
    /**
     * Progress callback interface
     */
    interface ProgressCallback {
        void onProgress(int current, int total, String message);
    }
    
    /**
     * Completion callback interface
     */
    interface CompletionCallback {
        void onSuccess(String message);
        void onError(String error);
    }
}
