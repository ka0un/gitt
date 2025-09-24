package com.funnygithub;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Simple GitHub Contribution Graph Generator
 * GUI to design patterns, then generate commits
 */
public class SimpleGitHubApp extends JFrame {
    
    // Pattern storage class
    private static class SavedPattern {
        String name;
        String text;
        int year;
        int[][] pattern;
        
        SavedPattern(String name, String text, int year, int[][] pattern) {
            this.name = name;
            this.text = text;
            this.year = year;
            this.pattern = new int[GRID_ROWS][GRID_COLS];
            for (int i = 0; i < GRID_ROWS; i++) {
                for (int j = 0; j < GRID_COLS; j++) {
                    this.pattern[i][j] = pattern[i][j];
                }
            }
        }
    }
    
    private static final int GRID_ROWS = 7;    // GitHub days (Sun-Sat)
    private static final int GRID_COLS = 53;   // GitHub weeks
    private static final int CELL_SIZE = 12;   // Pixel size for each cell
    private static final String PATTERNS_FILE = "saved_patterns.txt";
    
    private JLabel[][] gridCells;
    private int[][] patternData; // Changed from boolean to int for intensity levels (0-6)
    private JTextField yearInput;
    private JTextField textInput;
    private JTextArea outputArea;
    private JSlider intensitySlider;
    private JLabel intensityLabel;
    
    public SimpleGitHubApp() {
        initializeComponents();
        setupLayout();
        setupEventHandlers();
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setTitle("Simple GitHub Contribution Graph Generator");
        setSize(1200, 800);
        setLocationRelativeTo(null);
        setVisible(true);
    }
    
    private void initializeComponents() {
        // Initialize pattern data
        patternData = new int[GRID_ROWS][GRID_COLS];
        gridCells = new JLabel[GRID_ROWS][GRID_COLS];
        
        // Create grid cells
        for (int row = 0; row < GRID_ROWS; row++) {
            for (int col = 0; col < GRID_COLS; col++) {
                JLabel cell = new JLabel("", SwingConstants.CENTER);
                cell.setPreferredSize(new Dimension(CELL_SIZE, CELL_SIZE));
                cell.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
                cell.setBackground(Color.WHITE);
                cell.setOpaque(true);
                cell.setFont(new Font("Monospaced", Font.PLAIN, 8));
                
                // Add mouse listener for drawing
                cell.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mousePressed(MouseEvent e) {
                        toggleCell(cell);
                    }
                    
                    @Override
                    public void mouseEntered(MouseEvent e) {
                        if (SwingUtilities.isLeftMouseButton(e)) {
                            toggleCell(cell);
                        }
                    }
                });
                
                gridCells[row][col] = cell;
            }
        }
        
        // Input components
        yearInput = new JTextField("2024", 8);
        textInput = new JTextField("HELLO", 20);
        outputArea = new JTextArea(20, 50);
        outputArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        outputArea.setEditable(false);
        
        // Intensity slider
        intensitySlider = new JSlider(0, 6, 1);
        intensitySlider.setMajorTickSpacing(1);
        intensitySlider.setPaintTicks(true);
        intensitySlider.setPaintLabels(true);
        intensitySlider.setSnapToTicks(true);
        intensityLabel = new JLabel("Intensity: 1");
    }
    
    private void setupLayout() {
        setLayout(new BorderLayout());
        
        // Top panel for inputs
        JPanel topPanel = new JPanel(new FlowLayout());
        topPanel.add(new JLabel("Year:"));
        topPanel.add(yearInput);
        topPanel.add(new JLabel("Text:"));
        topPanel.add(textInput);
        topPanel.add(intensityLabel);
        topPanel.add(intensitySlider);
        
        JButton generateButton = new JButton("Generate Commits");
        generateButton.setBackground(new Color(34, 197, 94));
        generateButton.setForeground(Color.WHITE);
        generateButton.addActionListener(e -> generateCommits());
        topPanel.add(generateButton);
        
        JButton createCommitsButton = new JButton("Create Real Commits");
        createCommitsButton.setBackground(new Color(59, 130, 246));
        createCommitsButton.setForeground(Color.WHITE);
        createCommitsButton.addActionListener(e -> createRealCommits());
        topPanel.add(createCommitsButton);
        
        JButton clearButton = new JButton("Clear Grid");
        clearButton.addActionListener(e -> clearGrid());
        topPanel.add(clearButton);
        
        JButton saveButton = new JButton("Save Pattern");
        saveButton.setBackground(new Color(168, 85, 247));
        saveButton.setForeground(Color.WHITE);
        saveButton.addActionListener(e -> savePattern());
        topPanel.add(saveButton);
        
        JButton loadButton = new JButton("Load Pattern");
        loadButton.setBackground(new Color(245, 158, 11));
        loadButton.setForeground(Color.WHITE);
        loadButton.addActionListener(e -> loadPattern());
        topPanel.add(loadButton);
        
        add(topPanel, BorderLayout.NORTH);
        
        // Center panel for grid
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.add(new JLabel("Design your pattern by clicking on the grid:", SwingConstants.CENTER), BorderLayout.NORTH);
        
        JPanel gridPanel = new JPanel(new GridLayout(GRID_ROWS, GRID_COLS, 1, 1));
        for (int row = 0; row < GRID_ROWS; row++) {
            for (int col = 0; col < GRID_COLS; col++) {
                gridPanel.add(gridCells[row][col]);
            }
        }
        
        JScrollPane gridScrollPane = new JScrollPane(gridPanel);
        gridScrollPane.setPreferredSize(new Dimension(800, 200));
        centerPanel.add(gridScrollPane, BorderLayout.CENTER);
        
        // Add day labels
        JPanel leftPanel = new JPanel(new GridLayout(7, 1));
        String[] dayLabels = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
        for (String day : dayLabels) {
            JLabel dayLabel = new JLabel(day, SwingConstants.CENTER);
            dayLabel.setFont(new Font("Arial", Font.BOLD, 12));
            dayLabel.setPreferredSize(new Dimension(40, CELL_SIZE));
            leftPanel.add(dayLabel);
        }
        centerPanel.add(leftPanel, BorderLayout.WEST);
        
        add(centerPanel, BorderLayout.CENTER);
        
        // Bottom panel for output
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(new JLabel("Generated Commits:", SwingConstants.CENTER), BorderLayout.NORTH);
        JScrollPane outputScrollPane = new JScrollPane(outputArea);
        bottomPanel.add(outputScrollPane, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);
    }
    
    private void setupEventHandlers() {
        // Event handlers are set up in initializeComponents
        intensitySlider.addChangeListener(e -> {
            int intensity = intensitySlider.getValue();
            intensityLabel.setText("Intensity: " + intensity);
        });
    }
    
    private Color getIntensityColor(int intensity) {
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
    
    private void toggleCell(JLabel cell) {
        // Find the cell position
        int row = -1, col = -1;
        for (int r = 0; r < GRID_ROWS; r++) {
            for (int c = 0; c < GRID_COLS; c++) {
                if (gridCells[r][c] == cell) {
                    row = r;
                    col = c;
                    break;
                }
            }
        }
        
        if (row >= 0 && col >= 0) {
            // Cycle through intensity levels (0-6)
            int currentIntensity = patternData[row][col];
            int newIntensity = (currentIntensity + 1) % 7; // 0,1,2,3,4,5,6,0...
            patternData[row][col] = newIntensity;
            
            updateCellAppearance(row, col);
        }
    }
    
    private void clearGrid() {
        for (int row = 0; row < GRID_ROWS; row++) {
            for (int col = 0; col < GRID_COLS; col++) {
                patternData[row][col] = 0;
                updateCellAppearance(row, col);
            }
        }
    }
    
    private void generateCommits() {
        try {
            int year = Integer.parseInt(yearInput.getText());
            String text = textInput.getText().toUpperCase();
            
            StringBuilder output = new StringBuilder();
            output.append("GitHub Contribution Graph Generator\n");
            output.append("Year: ").append(year).append("\n");
            output.append("Text: ").append(text).append("\n");
            output.append("Pattern: ").append(getActiveCells()).append(" active cells\n\n");
            
            // Generate commit commands
            output.append("Git Commands to Run:\n");
            output.append("===================\n\n");
            
            // Calculate the actual start date for GitHub contribution graph
            // GitHub shows 53 weeks, starting from the first Sunday of the year
            LocalDate jan1 = LocalDate.of(year, 1, 1);
            int dayOfWeek = jan1.getDayOfWeek().getValue(); // 1=Monday, 7=Sunday
            int daysToFirstSunday = (7 - dayOfWeek) % 7;
            LocalDate firstSunday = jan1.plusDays(daysToFirstSunday);
            
            // If first Sunday is in previous year, start from the first Sunday of current year
            if (firstSunday.getYear() < year) {
                firstSunday = firstSunday.plusWeeks(1);
            }
            
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
            
            outputArea.setText(output.toString());
            
            // Save to file
            try (FileWriter writer = new FileWriter("git_commands.bat")) {
                writer.write("@echo off\n");
                writer.write("echo Generating GitHub contribution graph...\n");
                writer.write("echo GitHub contribution graph starts from: ");
                writer.write(firstSunday.format(DateTimeFormatter.ISO_LOCAL_DATE));
                writer.write("\n");
                writer.write("echo.\n");
                
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
                                writer.write("git commit --date=\"");
                                writer.write(commitDate.format(DateTimeFormatter.ISO_LOCAL_DATE));
                                writer.write(" 12:00:00\" -m \"Commit for ");
                                writer.write(text);
                                writer.write(" pattern (intensity ");
                                writer.write(String.valueOf(intensity));
                                writer.write(")\"\n");
                            }
                        }
                    }
                }
                
                writer.write("echo.\n");
                writer.write("echo Done! Check your GitHub contribution graph.\n");
                writer.write("pause\n");
            }
            
            JOptionPane.showMessageDialog(this, 
                "Generated " + commitCount + " commits!\n" +
                "Commands saved to git_commands.bat\n" +
                "Run the .bat file to create the commits.", 
                "Success", JOptionPane.INFORMATION_MESSAGE);
                
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Please enter a valid year (e.g., 2024)", "Invalid Year", JOptionPane.ERROR_MESSAGE);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error saving file: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private int getActiveCells() {
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
    
    private void createRealCommits() {
        String yearText = yearInput.getText().trim();
        String text = textInput.getText().trim();
        
        if (yearText.isEmpty() || text.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter both year and text!", "Input Required", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int year;
        try {
            year = Integer.parseInt(yearText);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Please enter a valid year!", "Invalid Year", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        if (year < 2000 || year > LocalDate.now().getYear()) {
            JOptionPane.showMessageDialog(this, "Please enter a year between 2000 and " + LocalDate.now().getYear(), "Invalid Year", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // Calculate the actual start date for GitHub contribution graph
        LocalDate jan1 = LocalDate.of(year, 1, 1);
        int dayOfWeek = jan1.getDayOfWeek().getValue(); // 1=Monday, 7=Sunday
        int daysToFirstSunday = (7 - dayOfWeek) % 7;
        LocalDate firstSunday = jan1.plusDays(daysToFirstSunday);
        
        if (firstSunday.getYear() < year) {
            firstSunday = firstSunday.plusWeeks(1);
        }
        
        final LocalDate finalFirstSunday = firstSunday;
        
        // Create progress dialog
        JDialog progressDialog = new JDialog(this, "Creating Commits", true);
        progressDialog.setSize(400, 150);
        progressDialog.setLocationRelativeTo(this);
        
        JPanel progressPanel = new JPanel(new BorderLayout());
        JLabel progressLabel = new JLabel("Initializing git repository...", JLabel.CENTER);
        JProgressBar progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);
        
        progressPanel.add(progressLabel, BorderLayout.CENTER);
        progressPanel.add(progressBar, BorderLayout.SOUTH);
        progressDialog.add(progressPanel);
        
        // Start the commit creation in a separate thread
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<Void> future = executor.submit(() -> {
            try {
                SwingUtilities.invokeLater(() -> progressDialog.setVisible(true));
                
                // Create a simple text file to commit
                Path commitFile = Paths.get("contribution_pattern.txt");
                
                // Initialize git repository if not exists
                SwingUtilities.invokeLater(() -> progressLabel.setText("Initializing git repository..."));
                ProcessBuilder gitInit = new ProcessBuilder("git", "init");
                gitInit.directory(Paths.get(".").toFile());
                Process initProcess = gitInit.start();
                initProcess.waitFor();
                
                // Count total commits first
                int totalCommits = 0;
                for (int week = 0; week < GRID_COLS; week++) {
                    for (int day = 0; day < GRID_ROWS; day++) {
                        int intensity = patternData[day][week];
                        if (intensity > 0) {
                            LocalDate commitDate = finalFirstSunday.plusWeeks(week).plusDays(day);
                            if (!commitDate.isAfter(LocalDate.now()) && commitDate.getYear() == year) {
                                totalCommits += intensity; // Add intensity level to total commits
                            }
                        }
                    }
                }
                
                final int finalTotalCommits = totalCommits;
                
                int commitCount = 0;
                StringBuilder output = new StringBuilder();
                output.append("Creating real commits for GitHub contribution graph...\n");
                output.append("GitHub contribution graph starts from: ").append(finalFirstSunday.format(DateTimeFormatter.ISO_LOCAL_DATE)).append("\n");
                output.append("Total commits to create: ").append(finalTotalCommits).append("\n\n");
                
                SwingUtilities.invokeLater(() -> {
                    progressBar.setIndeterminate(false);
                    progressBar.setMaximum(finalTotalCommits);
                    progressBar.setValue(0);
                    progressLabel.setText("Creating commits...");
                });
                
                for (int week = 0; week < GRID_COLS; week++) {
                    for (int day = 0; day < GRID_ROWS; day++) {
                        int intensity = patternData[day][week];
                        if (intensity > 0) {
                            LocalDate commitDate = finalFirstSunday.plusWeeks(week).plusDays(day);
                            
                            if (commitDate.isAfter(LocalDate.now()) || commitDate.getYear() != year) {
                                continue;
                            }
                            
                            // Create multiple commits based on intensity level
                            for (int i = 0; i < intensity; i++) {
                                // Update progress
                                final int currentCommit = commitCount + 1;
                                SwingUtilities.invokeLater(() -> {
                                    progressBar.setValue(currentCommit);
                                    progressLabel.setText("Creating commit " + currentCommit + " of " + finalTotalCommits + "...");
                                });
                                
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
                
                final int finalCommitCount = commitCount;
                SwingUtilities.invokeLater(() -> {
                    outputArea.setText(output.toString());
                    progressDialog.dispose();
                    
                    JOptionPane.showMessageDialog(this, 
                        "Created " + finalCommitCount + " real commits!\n" +
                        "Check git log to see the commits.\n" +
                        "Push to GitHub to see the contribution graph!", 
                        "Success", JOptionPane.INFORMATION_MESSAGE);
                });
                
            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> {
                    progressDialog.dispose();
                    JOptionPane.showMessageDialog(this, "Error creating commits: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                });
            }
            return null;
        });
        
        // Close progress dialog when task completes
        executor.submit(() -> {
            try {
                future.get(); // Wait for completion
            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> progressDialog.dispose());
            }
            executor.shutdown();
        });
    }
    
    private void updateCellAppearance(int row, int col) {
        JLabel cell = gridCells[row][col];
        int intensity = patternData[row][col];
        
        if (intensity == 0) {
            // Cell is inactive - make it white
            cell.setBackground(Color.WHITE);
            cell.setText("");
            cell.setForeground(Color.BLACK);
        } else {
            // Cell is active with intensity level
            Color intensityColor = getIntensityColor(intensity);
            cell.setBackground(intensityColor);
            cell.setText("██");
            cell.setForeground(Color.WHITE);
        }
    }
    
    private void savePattern() {
        String yearText = yearInput.getText().trim();
        String text = textInput.getText().trim();
        
        if (yearText.isEmpty() || text.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter both year and text before saving!", "Input Required", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int year;
        try {
            year = Integer.parseInt(yearText);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Please enter a valid year!", "Invalid Year", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        String patternName = JOptionPane.showInputDialog(this, "Enter a name for this pattern:", "Save Pattern", JOptionPane.QUESTION_MESSAGE);
        if (patternName == null || patternName.trim().isEmpty()) {
            return;
        }
        
        try {
            // Load existing patterns
            Map<String, SavedPattern> patterns = loadPatternsFromFile();
            
            // Add new pattern
            patterns.put(patternName.trim(), new SavedPattern(patternName.trim(), text, year, patternData));
            
            // Save to file
            savePatternsToFile(patterns);
            
            JOptionPane.showMessageDialog(this, "Pattern '" + patternName + "' saved successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error saving pattern: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void loadPattern() {
        try {
            Map<String, SavedPattern> patterns = loadPatternsFromFile();
            
            if (patterns.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No saved patterns found!", "No Patterns", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            
            // Create pattern selection dialog
            String[] patternNames = patterns.keySet().toArray(new String[0]);
            String selectedPattern = (String) JOptionPane.showInputDialog(this, 
                "Select a pattern to load:", "Load Pattern", 
                JOptionPane.QUESTION_MESSAGE, null, patternNames, patternNames[0]);
            
            if (selectedPattern != null) {
                SavedPattern pattern = patterns.get(selectedPattern);
                
                // Load pattern data
                for (int i = 0; i < GRID_ROWS; i++) {
                    for (int j = 0; j < GRID_COLS; j++) {
                        patternData[i][j] = pattern.pattern[i][j];
                        updateCellAppearance(i, j);
                    }
                }
                
                // Load year and text
                yearInput.setText(String.valueOf(pattern.year));
                textInput.setText(pattern.text);
                
                JOptionPane.showMessageDialog(this, "Pattern '" + selectedPattern + "' loaded successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            }
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error loading patterns: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private Map<String, SavedPattern> loadPatternsFromFile() throws IOException {
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
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new SimpleGitHubApp();
            }
        });
    }
}
