package com.GitTimeTraveler.service.impl;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.GitTimeTraveler.service.GitService;

/**
 * Implementation of GitService for handling git operations
 */
public class GitServiceImpl implements GitService {
    
    private static final int GRID_ROWS = 7;
    private static final int GRID_COLS = 53;
    
    @Override
    public String generateCommitCommands(int[][] patternData, int year, String text) {
        StringBuilder output = new StringBuilder();
        output.append("GitHub Contribution Graph Generator\n");
        output.append("Year: ").append(year).append("\n");
        output.append("Text: ").append(text).append("\n");
        output.append("Pattern: ").append(getActiveCells(patternData)).append(" active cells\n\n");
        
        // Generate commit commands
        output.append("Git Commands to Run:\n");
        output.append("===================\n\n");
        
        // Calculate the actual start date for GitHub contribution graph
        LocalDate firstSunday = calculateFirstSunday(year);
        
        output.append("GitHub contribution graph starts from: ").append(firstSunday.format(DateTimeFormatter.ISO_LOCAL_DATE)).append("\n");
        output.append("Grid mapping: Week 0 = ").append(firstSunday.format(DateTimeFormatter.ISO_LOCAL_DATE)).append("\n\n");
        
        int commitCount = 0;
        
        for (int week = 0; week < GRID_COLS; week++) {
            for (int day = 0; day < GRID_ROWS; day++) {
                int intensity = patternData[day][week];
                if (intensity > 0) {
                    LocalDate commitDate = firstSunday.plusWeeks(week).plusDays(day);
                    
                    // Skip if date is in the future or before the year
                    if (commitDate.isAfter(LocalDate.now()) || commitDate.getYear() != year) {
                        continue;
                    }
                    
                    // Create multiple commits based on intensity level
                    for (int i = 0; i < intensity; i++) {
                        output.append("git commit --date=\"").append(commitDate.format(DateTimeFormatter.ISO_LOCAL_DATE))
                              .append(" 12:00:00\" -m \"Commit for ").append(text).append(" pattern (intensity ").append(intensity).append(")\"\n");
                        commitCount++;
                    }
                }
            }
        }
        
        output.append("\nTotal commits to generate: ").append(commitCount).append("\n");
        output.append("\nTo run all commands, save this output to a .bat file and execute it.\n");
        
        return output.toString();
    }
    
    @Override
    public void createRealCommits(int[][] patternData, int year, String text, 
                                 ProgressCallback progressCallback, CompletionCallback completionCallback) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(() -> {
            try {
                // Calculate the actual start date for GitHub contribution graph
                LocalDate firstSunday = calculateFirstSunday(year);
                
                // Create a simple text file to commit
                Path commitFile = Paths.get("contribution_pattern.txt");
                
                // Initialize git repository if not exists
                progressCallback.onProgress(0, 100, "Initializing git repository...");
                ProcessBuilder gitInit = new ProcessBuilder("git", "init");
                gitInit.directory(Paths.get(".").toFile());
                Process initProcess = gitInit.start();
                initProcess.waitFor();
                
                // Count total commits first
                int totalCommits = calculateTotalCommits(patternData, year, firstSunday);
                
                progressCallback.onProgress(0, totalCommits, "Creating commits...");
                
                int commitCount = 0;
                StringBuilder output = new StringBuilder();
                output.append("Creating real commits for GitHub contribution graph...\n");
                output.append("GitHub contribution graph starts from: ").append(firstSunday.format(DateTimeFormatter.ISO_LOCAL_DATE)).append("\n");
                output.append("Total commits to create: ").append(totalCommits).append("\n\n");
                
                for (int week = 0; week < GRID_COLS; week++) {
                    for (int day = 0; day < GRID_ROWS; day++) {
                        int intensity = patternData[day][week];
                        if (intensity > 0) {
                            LocalDate commitDate = firstSunday.plusWeeks(week).plusDays(day);
                            
                            if (commitDate.isAfter(LocalDate.now()) || commitDate.getYear() != year) {
                                continue;
                            }
                            
                            // Create multiple commits based on intensity level
                            for (int i = 0; i < intensity; i++) {
                                // Update progress
                                progressCallback.onProgress(commitCount + 1, totalCommits, 
                                    "Creating commit " + (commitCount + 1) + " of " + totalCommits + "...");
                                
                                // Create or modify the file
                                String content = "GitHub Contribution Pattern\n" +
                                               "Text: " + text + "\n" +
                                               "Date: " + commitDate.format(DateTimeFormatter.ISO_LOCAL_DATE) + "\n" +
                                               "Week: " + week + ", Day: " + day + "\n" +
                                               "Intensity: " + intensity + "\n" +
                                               "Commit #" + (commitCount + 1) + "\n";
                                
                                Files.write(commitFile, content.getBytes());
                                
                                // Add the file
                                ProcessBuilder gitAdd = new ProcessBuilder("git", "add", "contribution_pattern.txt");
                                gitAdd.directory(Paths.get(".").toFile());
                                Process addProcess = gitAdd.start();
                                addProcess.waitFor();
                                
                                // Commit with specific date
                                String commitMessage = "Commit for " + text + " pattern (intensity " + intensity + ") - " + commitDate.format(DateTimeFormatter.ISO_LOCAL_DATE);
                                ProcessBuilder gitCommit = new ProcessBuilder("git", "commit", 
                                    "--date=" + commitDate.format(DateTimeFormatter.ISO_LOCAL_DATE) + " 12:00:00",
                                    "-m", commitMessage);
                                gitCommit.directory(Paths.get(".").toFile());
                                Process commitProcess = gitCommit.start();
                                commitProcess.waitFor();
                                
                                output.append("Created commit for ").append(commitDate.format(DateTimeFormatter.ISO_LOCAL_DATE)).append(" (intensity ").append(intensity).append(")\n");
                                commitCount++;
                            }
                        }
                    }
                }
                
                output.append("\nTotal commits created: ").append(commitCount).append("\n");
                output.append("Check your git log: git log --oneline\n");
                output.append("Push to GitHub to see the contribution graph!\n");
                
                completionCallback.onSuccess(output.toString());
                
            } catch (Exception e) {
                completionCallback.onError("Error creating commits: " + e.getMessage());
            }
        });
        
        executor.shutdown();
    }
    
    @Override
    public String getCommitsByYear(int year) throws Exception {
        StringBuilder output = new StringBuilder();
        output.append("Commits for year ").append(year).append(":\n");
        output.append("=".repeat(50)).append("\n\n");
        
        // Run git log command
        ProcessBuilder gitLog = new ProcessBuilder("git", "log", "--oneline", "--since=" + year + "-01-01", "--until=" + year + "-12-31");
        gitLog.directory(Paths.get(".").toFile());
        Process process = gitLog.start();
        
        // Read output
        try (var scanner = new java.util.Scanner(process.getInputStream())) {
            int commitCount = 0;
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                if (!line.trim().isEmpty()) {
                    output.append(line).append("\n");
                    commitCount++;
                }
            }
            
            output.append("\nTotal commits found: ").append(commitCount).append("\n");
        }
        
        process.waitFor();
        return output.toString();
    }
    
    @Override
    public int deleteCommitsByYear(int year) throws Exception {
        // Get all commits for the year first
        String commits = getCommitsByYear(year);
        String[] lines = commits.split("\n");
        int commitCount = 0;
        
        // Count commits (skip header lines)
        for (String line : lines) {
            if (line.trim().startsWith("commit ") || line.trim().matches("^[a-f0-9]{7,}.*")) {
                commitCount++;
            }
        }
        
        if (commitCount == 0) {
            return 0;
        }
        
        // Create a new branch without the commits
        String backupBranch = "backup-before-delete-" + System.currentTimeMillis();
        
        // Create backup branch
        ProcessBuilder createBackup = new ProcessBuilder("git", "branch", backupBranch);
        createBackup.directory(Paths.get(".").toFile());
        Process backupProcess = createBackup.start();
        backupProcess.waitFor();
        
        try {
            // Reset to before the year
            ProcessBuilder resetCommand = new ProcessBuilder("git", "reset", "--hard", "HEAD~" + commitCount);
            resetCommand.directory(Paths.get(".").toFile());
            Process resetProcess = resetCommand.start();
            resetProcess.waitFor();
            
            return commitCount;
            
        } catch (Exception e) {
            // Restore from backup if something goes wrong
            ProcessBuilder restoreCommand = new ProcessBuilder("git", "reset", "--hard", backupBranch);
            restoreCommand.directory(Paths.get(".").toFile());
            Process restoreProcess = restoreCommand.start();
            restoreProcess.waitFor();
            throw e;
        }
    }
    
    @Override
    public void saveCommitCommandsToFile(String commands, LocalDate firstSunday) throws IOException {
        try (FileWriter writer = new FileWriter("git_commands.bat")) {
            writer.write("@echo off\n");
            writer.write("echo Generating GitHub contribution graph...\n");
            writer.write("echo GitHub contribution graph starts from: ");
            writer.write(firstSunday.format(DateTimeFormatter.ISO_LOCAL_DATE));
            writer.write("\n");
            writer.write("echo.\n");
            
            // Extract commit commands from the full output
            String[] lines = commands.split("\n");
            boolean inCommandsSection = false;
            
            for (String line : lines) {
                if (line.equals("Git Commands to Run:")) {
                    inCommandsSection = true;
                    continue;
                }
                if (inCommandsSection && line.startsWith("git commit")) {
                    writer.write(line + "\n");
                }
            }
            
            writer.write("echo.\n");
            writer.write("echo Done! Check your GitHub contribution graph.\n");
            writer.write("pause\n");
        }
    }
    
    private LocalDate calculateFirstSunday(int year) {
        LocalDate jan1 = LocalDate.of(year, 1, 1);
        int dayOfWeek = jan1.getDayOfWeek().getValue(); // 1=Monday, 7=Sunday
        int daysToFirstSunday = (7 - dayOfWeek) % 7;
        LocalDate firstSunday = jan1.plusDays(daysToFirstSunday);
        
        // If first Sunday is in previous year, start from the first Sunday of current year
        if (firstSunday.getYear() < year) {
            firstSunday = firstSunday.plusWeeks(1);
        }
        
        return firstSunday;
    }
    
    private int calculateTotalCommits(int[][] patternData, int year, LocalDate firstSunday) {
        int totalCommits = 0;
        for (int week = 0; week < GRID_COLS; week++) {
            for (int day = 0; day < GRID_ROWS; day++) {
                int intensity = patternData[day][week];
                if (intensity > 0) {
                    LocalDate commitDate = firstSunday.plusWeeks(week).plusDays(day);
                    if (!commitDate.isAfter(LocalDate.now()) && commitDate.getYear() == year) {
                        totalCommits += intensity;
                    }
                }
            }
        }
        return totalCommits;
    }
    
    private int getActiveCells(int[][] patternData) {
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
}
