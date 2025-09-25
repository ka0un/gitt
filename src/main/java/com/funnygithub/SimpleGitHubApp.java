package com.funnygithub;

import com.funnygithub.ml.PatternGenerator;
import com.funnygithub.ml.TrainingDataGenerator;
import javax.swing.*;
import javax.swing.border.Border;
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
        setTitle("🤖 GitHub Contribution Graph Generator with ML Features");
        setSize(1400, 900);
        setLocationRelativeTo(null);
        setMinimumSize(new Dimension(1200, 700));
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
        
        // Input components with modern styling
        yearInput = new JTextField("2024", 8);
        yearInput.setFont(new Font("Arial", Font.PLAIN, 11));
        yearInput.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(209, 213, 219), 1),
            BorderFactory.createEmptyBorder(4, 8, 4, 8)
        ));
        
        textInput = new JTextField("PASINDU SAMPATH", 20);
        textInput.setFont(new Font("Arial", Font.PLAIN, 11));
        textInput.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(209, 213, 219), 1),
            BorderFactory.createEmptyBorder(4, 8, 4, 8)
        ));
        
        outputArea = new JTextArea(20, 50);
        outputArea.setFont(new Font("Consolas", Font.PLAIN, 11));
        outputArea.setEditable(false);
        
        // Intensity slider with modern styling
        intensitySlider = new JSlider(0, 6, 1);
        intensitySlider.setMajorTickSpacing(1);
        intensitySlider.setPaintTicks(true);
        intensitySlider.setPaintLabels(true);
        intensitySlider.setSnapToTicks(true);
        intensitySlider.setPreferredSize(new Dimension(150, 40));
        intensityLabel = new JLabel("Intensity: 1");
        intensityLabel.setFont(new Font("Arial", Font.BOLD, 11));
        intensityLabel.setForeground(new Color(75, 85, 99));
    }
    
    private void setupLayout() {
        setLayout(new BorderLayout());
        
        // Top panel with organized sections
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        topPanel.setBackground(new Color(248, 249, 250));
        
        // Input section
        JPanel inputPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        inputPanel.setBackground(new Color(248, 249, 250));
        
        JLabel yearLabel = createStyledLabel("Year:");
        inputPanel.add(yearLabel);
        inputPanel.add(yearInput);
        
        JLabel textLabel = createStyledLabel("Text:");
        inputPanel.add(textLabel);
        inputPanel.add(textInput);
        
        inputPanel.add(createSpacer(20));
        inputPanel.add(intensityLabel);
        inputPanel.add(intensitySlider);
        
        topPanel.add(inputPanel, BorderLayout.NORTH);
        
        // Button sections
        JPanel buttonPanel = new JPanel(new BorderLayout());
        buttonPanel.setBackground(new Color(248, 249, 250));
        
        // ML Features section
        JPanel mlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
        mlPanel.setBackground(new Color(248, 249, 250));
        mlPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(34, 139, 34), 2), 
            "🤖 ML Features", 
            0, 0, 
            new Font("Arial", Font.BOLD, 12), 
            new Color(34, 139, 34)
        ));
        
        JButton mlGenerateButton = createStyledButton("ML Generate", new Color(34, 139, 34), "AI-powered pattern creation");
        mlGenerateButton.addActionListener(e -> mlGeneratePattern());
        mlPanel.add(mlGenerateButton);
        
        JButton optimizeButton = createStyledButton("Optimize Pattern", new Color(139, 69, 19), "Fine-tune with sliders");
        optimizeButton.addActionListener(e -> optimizePattern());
        mlPanel.add(optimizeButton);
        
        // Core Features section
        JPanel corePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
        corePanel.setBackground(new Color(248, 249, 250));
        corePanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(59, 130, 246), 2), 
            "⚡ Core Features", 
            0, 0, 
            new Font("Arial", Font.BOLD, 12), 
            new Color(59, 130, 246)
        ));
        
        JButton generateButton = createStyledButton("Generate Commits", new Color(34, 197, 94), "Create batch file");
        generateButton.addActionListener(e -> generateCommits());
        corePanel.add(generateButton);
        
        JButton createCommitsButton = createStyledButton("Create Real Commits", new Color(59, 130, 246), "Direct git commits");
        createCommitsButton.addActionListener(e -> createRealCommits());
        corePanel.add(createCommitsButton);
        
        JButton clearButton = createStyledButton("Clear Grid", new Color(156, 163, 175), "Reset pattern");
        clearButton.addActionListener(e -> clearGrid());
        corePanel.add(clearButton);
        
        // Pattern Management section
        JPanel patternPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
        patternPanel.setBackground(new Color(248, 249, 250));
        patternPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(168, 85, 247), 2), 
            "💾 Pattern Management", 
            0, 0, 
            new Font("Arial", Font.BOLD, 12), 
            new Color(168, 85, 247)
        ));
        
        JButton saveButton = createStyledButton("Save Pattern", new Color(168, 85, 247), "Save current design");
        saveButton.addActionListener(e -> savePattern());
        patternPanel.add(saveButton);
        
        JButton loadButton = createStyledButton("Load Pattern", new Color(245, 158, 11), "Load saved design");
        loadButton.addActionListener(e -> loadPattern());
        patternPanel.add(loadButton);
        
        // Commit Management section
        JPanel commitPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
        commitPanel.setBackground(new Color(248, 249, 250));
        commitPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(220, 38, 127), 2), 
            "🗑️ Commit Management", 
            0, 0, 
            new Font("Arial", Font.BOLD, 12), 
            new Color(220, 38, 127)
        ));
        
        JButton viewCommitsButton = createStyledButton("View Commits", new Color(220, 38, 127), "View commits by year");
        viewCommitsButton.addActionListener(e -> viewCommitsByYear());
        commitPanel.add(viewCommitsButton);
        
        JButton deleteCommitsButton = createStyledButton("Delete Commits", new Color(185, 28, 28), "Delete commits by year");
        deleteCommitsButton.addActionListener(e -> deleteCommitsByYear());
        commitPanel.add(deleteCommitsButton);
        
        // Organize button sections
        JPanel leftButtons = new JPanel(new BorderLayout());
        leftButtons.add(mlPanel, BorderLayout.NORTH);
        leftButtons.add(corePanel, BorderLayout.CENTER);
        
        JPanel rightButtons = new JPanel(new BorderLayout());
        rightButtons.add(patternPanel, BorderLayout.NORTH);
        rightButtons.add(commitPanel, BorderLayout.CENTER);
        
        buttonPanel.add(leftButtons, BorderLayout.WEST);
        buttonPanel.add(rightButtons, BorderLayout.EAST);
        
        topPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        add(topPanel, BorderLayout.NORTH);
        
        // Center panel for grid
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBackground(new Color(255, 255, 255));
        
        JLabel gridTitle = createStyledLabel("🎨 Design your pattern by clicking on the grid (cycle through intensity levels 0-6)");
        gridTitle.setHorizontalAlignment(SwingConstants.CENTER);
        gridTitle.setBorder(BorderFactory.createEmptyBorder(10, 10, 5, 10));
        centerPanel.add(gridTitle, BorderLayout.NORTH);
        
        JPanel gridPanel = new JPanel(new GridLayout(GRID_ROWS, GRID_COLS, 1, 1));
        gridPanel.setBorder(BorderFactory.createLineBorder(new Color(209, 213, 219), 2));
        gridPanel.setBackground(new Color(255, 255, 255));
        
        for (int row = 0; row < GRID_ROWS; row++) {
            for (int col = 0; col < GRID_COLS; col++) {
                gridCells[row][col].setBorder(BorderFactory.createLineBorder(new Color(229, 231, 235), 1));
                gridPanel.add(gridCells[row][col]);
            }
        }
        
        JScrollPane gridScrollPane = new JScrollPane(gridPanel);
        gridScrollPane.setPreferredSize(new Dimension(900, 220));
        gridScrollPane.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        gridScrollPane.getViewport().setBackground(new Color(255, 255, 255));
        centerPanel.add(gridScrollPane, BorderLayout.CENTER);
        
        // Add day labels
        JPanel leftPanel = new JPanel(new GridLayout(7, 1));
        leftPanel.setBackground(new Color(248, 249, 250));
        leftPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 5));
        
        String[] dayLabels = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
        for (String day : dayLabels) {
            JLabel dayLabel = new JLabel(day, SwingConstants.CENTER);
            dayLabel.setFont(new Font("Arial", Font.BOLD, 11));
            dayLabel.setPreferredSize(new Dimension(45, CELL_SIZE));
            dayLabel.setForeground(new Color(75, 85, 99));
            leftPanel.add(dayLabel);
        }
        centerPanel.add(leftPanel, BorderLayout.WEST);
        
        add(centerPanel, BorderLayout.CENTER);
        
        // Bottom panel for output
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBackground(new Color(248, 249, 250));
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JLabel outputTitle = createStyledLabel("📄 Generated Commits & Git Commands");
        outputTitle.setHorizontalAlignment(SwingConstants.CENTER);
        bottomPanel.add(outputTitle, BorderLayout.NORTH);
        
        outputArea.setBorder(BorderFactory.createLineBorder(new Color(209, 213, 219), 1));
        outputArea.setBackground(new Color(255, 255, 255));
        outputArea.setFont(new Font("Consolas", Font.PLAIN, 11));
        
        JScrollPane outputScrollPane = new JScrollPane(outputArea);
        outputScrollPane.setPreferredSize(new Dimension(800, 200));
        outputScrollPane.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));
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
    
    /**
     * Create a styled label with modern appearance
     */
    private JLabel createStyledLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Arial", Font.PLAIN, 11));
        label.setForeground(new Color(75, 85, 99));
        return label;
    }
    
    /**
     * Create a styled button with modern appearance and tooltip
     */
    private JButton createStyledButton(String text, Color backgroundColor, String tooltip) {
        JButton button = new JButton(text);
        button.setBackground(backgroundColor);
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Arial", Font.BOLD, 11));
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setToolTipText(tooltip);
        button.setPreferredSize(new Dimension(120, 32));
        
        // Add hover effects
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(backgroundColor.darker());
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(backgroundColor);
            }
        });
        
        return button;
    }
    
    /**
     * Create a spacer component for layout
     */
    private Component createSpacer(int width) {
        return Box.createHorizontalStrut(width);
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
    
    /**
     * Generate pattern using Machine Learning
     */
    private void mlGeneratePattern() {
        String text = textInput.getText().trim();
        String yearText = yearInput.getText().trim();
        
        if (text.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter text to generate pattern!", "Input Required", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        final int year = Integer.parseInt(yearText);
        
        final String finalText = text;
        
        // Show progress dialog
        JDialog progressDialog = new JDialog(this, "Generating ML Pattern", true);
        progressDialog.setSize(300, 100);
        progressDialog.setLocationRelativeTo(this);
        
        JPanel progressPanel = new JPanel(new BorderLayout());
        JLabel progressLabel = new JLabel("Generating pattern with ML...", JLabel.CENTER);
        JProgressBar progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);
        
        progressPanel.add(progressLabel, BorderLayout.CENTER);
        progressPanel.add(progressBar, BorderLayout.SOUTH);
        progressDialog.add(progressPanel);
        
        // Generate pattern in background thread
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(() -> {
            try {
                SwingUtilities.invokeLater(() -> progressDialog.setVisible(true));
                
                // Generate pattern using ML
                int[][] mlPattern = PatternGenerator.generatePattern(finalText, year);
                
                SwingUtilities.invokeLater(() -> {
                    // Apply the generated pattern
                    for (int row = 0; row < GRID_ROWS; row++) {
                        for (int col = 0; col < GRID_COLS; col++) {
                            patternData[row][col] = mlPattern[row][col];
                            updateCellAppearance(row, col);
                        }
                    }
                    
                    progressDialog.dispose();
                    
                    JOptionPane.showMessageDialog(this, 
                        "ML Pattern generated successfully!\n" +
                        "Text: " + finalText + "\n" +
                        "Year: " + year + "\n" +
                        "Active cells: " + getActiveCells(), 
                        "ML Generation Complete", JOptionPane.INFORMATION_MESSAGE);
                });
                
            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> {
                    progressDialog.dispose();
                    JOptionPane.showMessageDialog(this, "Error generating ML pattern: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                });
            }
            return null;
        });
        
        executor.shutdown();
    }
    
    /**
     * Optimize current pattern using ML techniques
     */
    private void optimizePattern() {
        String text = textInput.getText().trim();
        String yearText = yearInput.getText().trim();
        
        if (text.isEmpty()) {
            text = "OPTIMIZED"; // Default text for optimization
        }
        
        final int year = Integer.parseInt(yearText);
        
        final String finalText = text;
        
        // Show optimization dialog
        JDialog optimizationDialog = new JDialog(this, "Pattern Optimization", true);
        optimizationDialog.setSize(400, 300);
        optimizationDialog.setLocationRelativeTo(this);
        
        JPanel optimizationPanel = new JPanel(new BorderLayout());
        
        // Optimization parameters
        JPanel paramsPanel = new JPanel(new GridLayout(4, 2, 5, 5));
        
        JSlider densitySlider = new JSlider(0, 100, 50);
        densitySlider.setMajorTickSpacing(25);
        densitySlider.setPaintTicks(true);
        densitySlider.setPaintLabels(true);
        
        JSlider symmetrySlider = new JSlider(0, 100, 50);
        symmetrySlider.setMajorTickSpacing(25);
        symmetrySlider.setPaintTicks(true);
        symmetrySlider.setPaintLabels(true);
        
        JSlider continuitySlider = new JSlider(0, 100, 50);
        continuitySlider.setMajorTickSpacing(25);
        continuitySlider.setPaintTicks(true);
        continuitySlider.setPaintLabels(true);
        
        paramsPanel.add(new JLabel("Density:"));
        paramsPanel.add(densitySlider);
        paramsPanel.add(new JLabel("Symmetry:"));
        paramsPanel.add(symmetrySlider);
        paramsPanel.add(new JLabel("Continuity:"));
        paramsPanel.add(continuitySlider);
        
        JButton optimizeButton = new JButton("Optimize Pattern");
        optimizeButton.addActionListener(e -> {
            double density = densitySlider.getValue() / 100.0;
            double symmetry = symmetrySlider.getValue() / 100.0;
            double continuity = continuitySlider.getValue() / 100.0;
            
            // Generate optimized pattern
            int[][] optimizedPattern = PatternGenerator.generateCustomPattern(finalText, year, density, symmetry, continuity);
            
            // Apply the optimized pattern
            for (int row = 0; row < GRID_ROWS; row++) {
                for (int col = 0; col < GRID_COLS; col++) {
                    patternData[row][col] = optimizedPattern[row][col];
                    updateCellAppearance(row, col);
                }
            }
            
            optimizationDialog.dispose();
            
            JOptionPane.showMessageDialog(this, 
                "Pattern optimized successfully!\n" +
                "Density: " + String.format("%.1f", density) + "\n" +
                "Symmetry: " + String.format("%.1f", symmetry) + "\n" +
                "Continuity: " + String.format("%.1f", continuity), 
                "Optimization Complete", JOptionPane.INFORMATION_MESSAGE);
        });
        
        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(e -> optimizationDialog.dispose());
        
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(optimizeButton);
        buttonPanel.add(cancelButton);
        
        optimizationPanel.add(new JLabel("Adjust optimization parameters:", JLabel.CENTER), BorderLayout.NORTH);
        optimizationPanel.add(paramsPanel, BorderLayout.CENTER);
        optimizationPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        optimizationDialog.add(optimizationPanel);
        optimizationDialog.setVisible(true);
    }
    
    /**
     * View commits by year
     */
    private void viewCommitsByYear() {
        // Show year selection dialog
        String yearText = JOptionPane.showInputDialog(this, 
            "Enter year to view commits:", "View Commits by Year", JOptionPane.QUESTION_MESSAGE);
        
        if (yearText == null || yearText.trim().isEmpty()) {
            return;
        }
        
        int year;
        try {
            year = Integer.parseInt(yearText.trim());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Please enter a valid year!", "Invalid Year", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // Show progress dialog
        JDialog progressDialog = new JDialog(this, "Loading Commits", true);
        progressDialog.setSize(400, 150);
        progressDialog.setLocationRelativeTo(this);
        
        JPanel progressPanel = new JPanel(new BorderLayout());
        JLabel progressLabel = new JLabel("Loading commits for year " + year + "...", JLabel.CENTER);
        JProgressBar progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);
        
        progressPanel.add(progressLabel, BorderLayout.CENTER);
        progressPanel.add(progressBar, BorderLayout.SOUTH);
        progressDialog.add(progressPanel);
        
        // Load commits in background thread
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(() -> {
            try {
                SwingUtilities.invokeLater(() -> progressDialog.setVisible(true));
                
                // Get commits for the year
                String commits = getCommitsByYear(year);
                
                SwingUtilities.invokeLater(() -> {
                    progressDialog.dispose();
                    showCommitsDialog(year, commits);
                });
                
            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> {
                    progressDialog.dispose();
                    JOptionPane.showMessageDialog(this, "Error loading commits: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                });
            }
            return null;
        });
        
        executor.shutdown();
    }
    
    /**
     * Delete commits by year
     */
    private void deleteCommitsByYear() {
        // Show year selection dialog
        String yearText = JOptionPane.showInputDialog(this, 
            "Enter year to delete commits:", "Delete Commits by Year", JOptionPane.QUESTION_MESSAGE);
        
        if (yearText == null || yearText.trim().isEmpty()) {
            return;
        }
        
        int year;
        try {
            year = Integer.parseInt(yearText.trim());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Please enter a valid year!", "Invalid Year", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // Confirm deletion
        int result = JOptionPane.showConfirmDialog(this, 
            "Are you sure you want to delete ALL commits for year " + year + "?\n\n" +
            "This action cannot be undone!", 
            "Confirm Deletion", 
            JOptionPane.YES_NO_OPTION, 
            JOptionPane.WARNING_MESSAGE);
        
        if (result != JOptionPane.YES_OPTION) {
            return;
        }
        
        // Show progress dialog
        JDialog progressDialog = new JDialog(this, "Deleting Commits", true);
        progressDialog.setSize(400, 150);
        progressDialog.setLocationRelativeTo(this);
        
        JPanel progressPanel = new JPanel(new BorderLayout());
        JLabel progressLabel = new JLabel("Deleting commits for year " + year + "...", JLabel.CENTER);
        JProgressBar progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);
        
        progressPanel.add(progressLabel, BorderLayout.CENTER);
        progressPanel.add(progressBar, BorderLayout.SOUTH);
        progressDialog.add(progressPanel);
        
        // Delete commits in background thread
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(() -> {
            try {
                SwingUtilities.invokeLater(() -> progressDialog.setVisible(true));
                
                // Delete commits for the year
                int deletedCount = deleteCommitsByYear(year);
                
                SwingUtilities.invokeLater(() -> {
                    progressDialog.dispose();
                    JOptionPane.showMessageDialog(this, 
                        "Successfully deleted " + deletedCount + " commits for year " + year + "!", 
                        "Deletion Complete", JOptionPane.INFORMATION_MESSAGE);
                });
                
            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> {
                    progressDialog.dispose();
                    JOptionPane.showMessageDialog(this, "Error deleting commits: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                });
            }
            return null;
        });
        
        executor.shutdown();
    }
    
    /**
     * Get commits for a specific year
     */
    private String getCommitsByYear(int year) throws Exception {
        StringBuilder output = new StringBuilder();
        output.append("Commits for year ").append(year).append(":\n");
        output.append("=".repeat(50)).append("\n\n");
        
        // Run git log command
        ProcessBuilder gitLog = new ProcessBuilder("git", "log", "--oneline", "--since=" + year + "-01-01", "--until=" + year + "-12-31");
        gitLog.directory(Paths.get(".").toFile());
        Process process = gitLog.start();
        
        // Read output
        try (Scanner scanner = new Scanner(process.getInputStream())) {
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
    
    /**
     * Delete commits for a specific year
     */
    private int deleteCommitsByYear(int year) throws Exception {
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
    
    /**
     * Show commits in a dialog
     */
    private void showCommitsDialog(int year, String commits) {
        JDialog commitsDialog = new JDialog(this, "Commits for Year " + year, true);
        commitsDialog.setSize(800, 600);
        commitsDialog.setLocationRelativeTo(this);
        
        JPanel mainPanel = new JPanel(new BorderLayout());
        
        JTextArea commitsArea = new JTextArea(commits);
        commitsArea.setFont(new Font("Consolas", Font.PLAIN, 11));
        commitsArea.setEditable(false);
        commitsArea.setBackground(new Color(248, 249, 250));
        
        JScrollPane scrollPane = new JScrollPane(commitsArea);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(209, 213, 219), 1));
        
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton closeButton = createStyledButton("Close", new Color(156, 163, 175), "Close dialog");
        closeButton.addActionListener(e -> commitsDialog.dispose());
        buttonPanel.add(closeButton);
        
        JButton deleteButton = createStyledButton("Delete These Commits", new Color(185, 28, 28), "Delete all shown commits");
        deleteButton.addActionListener(e -> {
            commitsDialog.dispose();
            // Show confirmation dialog
            int result = JOptionPane.showConfirmDialog(this, 
                "Are you sure you want to delete ALL commits for year " + year + "?\n\n" +
                "This action cannot be undone!", 
                "Confirm Deletion", 
                JOptionPane.YES_NO_OPTION, 
                JOptionPane.WARNING_MESSAGE);
            
            if (result == JOptionPane.YES_OPTION) {
                // Show progress dialog
                JDialog progressDialog = new JDialog(this, "Deleting Commits", true);
                progressDialog.setSize(400, 150);
                progressDialog.setLocationRelativeTo(this);
                
                JPanel progressPanel = new JPanel(new BorderLayout());
                JLabel progressLabel = new JLabel("Deleting commits for year " + year + "...", JLabel.CENTER);
                JProgressBar progressBar = new JProgressBar();
                progressBar.setIndeterminate(true);
                
                progressPanel.add(progressLabel, BorderLayout.CENTER);
                progressPanel.add(progressBar, BorderLayout.SOUTH);
                progressDialog.add(progressPanel);
                
                // Delete commits in background thread
                ExecutorService executor = Executors.newSingleThreadExecutor();
                executor.submit(() -> {
                    try {
                        SwingUtilities.invokeLater(() -> progressDialog.setVisible(true));
                        
                        // Delete commits for the year
                        int deletedCount = deleteCommitsByYear(year);
                        
                        SwingUtilities.invokeLater(() -> {
                            progressDialog.dispose();
                            JOptionPane.showMessageDialog(this, 
                                "Successfully deleted " + deletedCount + " commits for year " + year + "!", 
                                "Deletion Complete", JOptionPane.INFORMATION_MESSAGE);
                        });
                        
                    } catch (Exception ex) {
                        SwingUtilities.invokeLater(() -> {
                            progressDialog.dispose();
                            JOptionPane.showMessageDialog(this, "Error deleting commits: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                        });
                    }
                    return null;
                });
                
                executor.shutdown();
            }
        });
        buttonPanel.add(deleteButton);
        
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        commitsDialog.add(mainPanel);
        commitsDialog.setVisible(true);
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
