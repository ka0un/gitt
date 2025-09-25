package com.GitTimeTraveler;

import javax.swing.*;

import com.GitTimeTraveler.service.*;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDate;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Simple GitHub Contribution Graph Generator
 * GUI to design patterns, then generate commits
 */
public class SimpleGitHubApp extends JFrame {
    
    private static final int GRID_ROWS = 7;    // GitHub days (Sun-Sat)
    private static final int GRID_COLS = 53;   // GitHub weeks
    private static final int CELL_SIZE = 12;   // Pixel size for each cell
    
    private JLabel[][] gridCells;
    private int[][] patternData; // Changed from boolean to int for intensity levels (0-6)
    private JTextField yearInput;
    private JTextField textInput;
    private JTextArea outputArea;
    private JSlider intensitySlider;
    private JLabel intensityLabel;
    
    // Service instances
    private final GitService gitService;
    private final PatternService patternService;
    private final FileService fileService;
    private final UIService uiService;
    
    public SimpleGitHubApp() {
        // Initialize services
        this.gitService = ServiceFactory.getGitService();
        this.patternService = ServiceFactory.getPatternService();
        this.fileService = ServiceFactory.getFileService();
        this.uiService = ServiceFactory.getUIService();
        
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
        
        // Input components with modern styling using UIService
        yearInput = uiService.createStyledTextField("2024", 8);
        textInput = uiService.createStyledTextField("PASINDU SAMPATH", 20);
        outputArea = uiService.createStyledTextArea(20, 50);
        
        // Intensity slider with modern styling
        intensitySlider = uiService.createStyledSlider(0, 6, 1, "Intensity");
        intensityLabel = uiService.createStyledLabel("Intensity: 1");
        intensityLabel.setFont(new Font("Arial", Font.BOLD, 11));
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
        
        JLabel yearLabel = uiService.createStyledLabel("Year:");
        inputPanel.add(yearLabel);
        inputPanel.add(yearInput);
        
        JLabel textLabel = uiService.createStyledLabel("Text:");
        inputPanel.add(textLabel);
        inputPanel.add(textInput);
        
        inputPanel.add(uiService.createSpacer(20));
        inputPanel.add(intensityLabel);
        inputPanel.add(intensitySlider);
        
        topPanel.add(inputPanel, BorderLayout.NORTH);
        
        // Button sections
        JPanel buttonPanel = new JPanel(new BorderLayout());
        buttonPanel.setBackground(new Color(248, 249, 250));
        
        // ML Features section
        JPanel mlPanel = uiService.createStyledPanel("🤖 ML Features", new Color(34, 139, 34));
        mlPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 5));
        
        JButton mlGenerateButton = uiService.createStyledButton("ML Generate", new Color(34, 139, 34), "AI-powered pattern creation");
        mlGenerateButton.addActionListener(e -> mlGeneratePattern());
        mlPanel.add(mlGenerateButton);
        
        JButton optimizeButton = uiService.createStyledButton("Optimize Pattern", new Color(139, 69, 19), "Fine-tune with sliders");
        optimizeButton.addActionListener(e -> optimizePattern());
        mlPanel.add(optimizeButton);
        
        // Core Features section
        JPanel corePanel = uiService.createStyledPanel("⚡ Core Features", new Color(59, 130, 246));
        corePanel.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 5));
        
        JButton generateButton = uiService.createStyledButton("Generate Commits", new Color(34, 197, 94), "Create batch file");
        generateButton.addActionListener(e -> generateCommits());
        corePanel.add(generateButton);
        
        JButton createCommitsButton = uiService.createStyledButton("Create Real Commits", new Color(59, 130, 246), "Direct git commits");
        createCommitsButton.addActionListener(e -> createRealCommits());
        corePanel.add(createCommitsButton);
        
        JButton clearButton = uiService.createStyledButton("Clear Grid", new Color(156, 163, 175), "Reset pattern");
        clearButton.addActionListener(e -> clearGrid());
        corePanel.add(clearButton);
        
        // Pattern Management section
        JPanel patternPanel = uiService.createStyledPanel("💾 Pattern Management", new Color(168, 85, 247));
        patternPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 5));
        
        JButton saveButton = uiService.createStyledButton("Save Pattern", new Color(168, 85, 247), "Save current design");
        saveButton.addActionListener(e -> savePattern());
        patternPanel.add(saveButton);
        
        JButton loadButton = uiService.createStyledButton("Load Pattern", new Color(245, 158, 11), "Load saved design");
        loadButton.addActionListener(e -> loadPattern());
        patternPanel.add(loadButton);
        
        // Commit Management section
        JPanel commitPanel = uiService.createStyledPanel("🗑️ Commit Management", new Color(220, 38, 127));
        commitPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 5));
        
        JButton viewCommitsButton = uiService.createStyledButton("View Commits", new Color(220, 38, 127), "View commits by year");
        viewCommitsButton.addActionListener(e -> viewCommitsByYear());
        commitPanel.add(viewCommitsButton);
        
        JButton deleteCommitsButton = uiService.createStyledButton("Delete Commits", new Color(185, 28, 28), "Delete commits by year");
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
        
        JLabel gridTitle = uiService.createStyledLabel("🎨 Design your pattern by clicking on the grid (cycle through intensity levels 0-6)");
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
        
        JLabel outputTitle = uiService.createStyledLabel("📄 Generated Commits & Git Commands");
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
            int currentIntensity = patternService.getCellIntensity(patternData, row, col);
            int newIntensity = (currentIntensity + 1) % 7; // 0,1,2,3,4,5,6,0...
            patternService.updateCellIntensity(patternData, row, col, newIntensity);
            
            updateCellAppearance(row, col);
        }
    }
    
    private void clearGrid() {
        patternService.clearPattern(patternData);
        for (int row = 0; row < GRID_ROWS; row++) {
            for (int col = 0; col < GRID_COLS; col++) {
                updateCellAppearance(row, col);
            }
        }
    }
    
    private void generateCommits() {
        try {
            int year = Integer.parseInt(yearInput.getText());
            String text = textInput.getText().toUpperCase();
            
            // Use GitService to generate commit commands
            String output = gitService.generateCommitCommands(patternData, year, text);
            outputArea.setText(output);
            
            // Calculate first Sunday for file saving
            LocalDate jan1 = LocalDate.of(year, 1, 1);
            int dayOfWeek = jan1.getDayOfWeek().getValue();
            int daysToFirstSunday = (7 - dayOfWeek) % 7;
            LocalDate firstSunday = jan1.plusDays(daysToFirstSunday);
            if (firstSunday.getYear() < year) {
                firstSunday = firstSunday.plusWeeks(1);
            }
            
            // Save to file using GitService
            gitService.saveCommitCommandsToFile(output, firstSunday);
            
            int commitCount = patternService.getActiveCellsCount(patternData);
            uiService.showMessageDialog(this, 
                "Generated " + commitCount + " commits!\n" +
                "Commands saved to git_commands.bat\n" +
                "Run the .bat file to create the commits.", 
                "Success", JOptionPane.INFORMATION_MESSAGE);
                
        } catch (NumberFormatException e) {
            uiService.showMessageDialog(this, "Please enter a valid year (e.g., 2024)", "Invalid Year", JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) {
            uiService.showMessageDialog(this, "Error saving file: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    
    private void createRealCommits() {
        String yearText = yearInput.getText().trim();
        String text = textInput.getText().trim();
        
        if (yearText.isEmpty() || text.isEmpty()) {
            uiService.showMessageDialog(this, "Please enter both year and text!", "Input Required", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int year;
        try {
            year = Integer.parseInt(yearText);
        } catch (NumberFormatException e) {
            uiService.showMessageDialog(this, "Please enter a valid year!", "Invalid Year", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        if (year < 2000 || year > LocalDate.now().getYear()) {
            uiService.showMessageDialog(this, "Please enter a year between 2000 and " + LocalDate.now().getYear(), "Invalid Year", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // Create progress dialog using UIService
        JDialog progressDialog = uiService.createProgressDialog(this, "Creating Commits", "Initializing git repository...");
        
        // Use GitService to create real commits
        gitService.createRealCommits(patternData, year, text, 
            (current, total, message) -> {
                SwingUtilities.invokeLater(() -> {
                    if (current == 0 && total == 100) {
                        // Initial progress
                        progressDialog.setVisible(true);
                    } else {
                        uiService.updateProgressDialog(progressDialog, current, total, message);
                    }
                });
            },
            new GitService.CompletionCallback() {
                @Override
                public void onSuccess(String message) {
                    SwingUtilities.invokeLater(() -> {
                        outputArea.setText(message);
                        progressDialog.dispose();
                        uiService.showMessageDialog(SimpleGitHubApp.this, 
                            "Real commits created successfully!\n" +
                            "Check git log to see the commits.\n" +
                            "Push to GitHub to see the contribution graph!", 
                            "Success", JOptionPane.INFORMATION_MESSAGE);
                    });
                }
                
                @Override
                public void onError(String error) {
                    SwingUtilities.invokeLater(() -> {
                        progressDialog.dispose();
                        uiService.showMessageDialog(SimpleGitHubApp.this, error, "Error", JOptionPane.ERROR_MESSAGE);
                    });
                }
            });
    }
    
    private void updateCellAppearance(int row, int col) {
        JLabel cell = gridCells[row][col];
        int intensity = patternService.getCellIntensity(patternData, row, col);
        
        if (intensity == 0) {
            // Cell is inactive - make it white
            cell.setBackground(Color.WHITE);
            cell.setText("");
            cell.setForeground(Color.BLACK);
        } else {
            // Cell is active with intensity level
            Color intensityColor = patternService.getIntensityColor(intensity);
            cell.setBackground(intensityColor);
            cell.setText("██");
            cell.setForeground(Color.WHITE);
        }
    }
    
    private void savePattern() {
        String yearText = yearInput.getText().trim();
        String text = textInput.getText().trim();
        
        if (yearText.isEmpty() || text.isEmpty()) {
            uiService.showMessageDialog(this, "Please enter both year and text before saving!", "Input Required", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int year;
        try {
            year = Integer.parseInt(yearText);
        } catch (NumberFormatException e) {
            uiService.showMessageDialog(this, "Please enter a valid year!", "Invalid Year", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        String patternName = uiService.showInputDialog(this, "Enter a name for this pattern:", "Save Pattern");
        if (patternName == null || patternName.trim().isEmpty()) {
            return;
        }
        
        try {
            // Use FileService to save pattern
            fileService.savePattern(patternName.trim(), text, year, patternData);
            uiService.showMessageDialog(this, "Pattern '" + patternName + "' saved successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            
        } catch (Exception e) {
            uiService.showMessageDialog(this, "Error saving pattern: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void loadPattern() {
        try {
            // Use FileService to get available patterns
            String[] patternNames = fileService.getAvailablePatternNames();
            
            if (patternNames.length == 0) {
                uiService.showMessageDialog(this, "No saved patterns found!", "No Patterns", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            
            // Create pattern selection dialog
            String selectedPattern = (String) JOptionPane.showInputDialog(this, 
                "Select a pattern to load:", "Load Pattern", 
                JOptionPane.QUESTION_MESSAGE, null, patternNames, patternNames[0]);
            
            if (selectedPattern != null) {
                // Use FileService to load pattern
                FileService.SavedPattern pattern = fileService.loadPattern(selectedPattern);
                
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
                
                uiService.showMessageDialog(this, "Pattern '" + selectedPattern + "' loaded successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            }
            
        } catch (Exception e) {
            uiService.showMessageDialog(this, "Error loading patterns: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    
    /**
     * Generate pattern using Machine Learning
     */
    private void mlGeneratePattern() {
        String text = textInput.getText().trim();
        String yearText = yearInput.getText().trim();
        
        if (text.isEmpty()) {
            uiService.showMessageDialog(this, "Please enter text to generate pattern!", "Input Required", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        final int year = Integer.parseInt(yearText);
        final String finalText = text;
        
        // Show progress dialog using UIService
        JDialog progressDialog = uiService.createProgressDialog(this, "Generating ML Pattern", "Generating pattern with ML...");
        
        // Generate pattern in background thread using PatternService
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(() -> {
            try {
                SwingUtilities.invokeLater(() -> progressDialog.setVisible(true));
                
                // Generate pattern using PatternService
                int[][] mlPattern = patternService.generateMLPattern(finalText, year);
                
                SwingUtilities.invokeLater(() -> {
                    // Apply the generated pattern
                    for (int row = 0; row < GRID_ROWS; row++) {
                        for (int col = 0; col < GRID_COLS; col++) {
                            patternData[row][col] = mlPattern[row][col];
                            updateCellAppearance(row, col);
                        }
                    }
                    
                    progressDialog.dispose();
                    
                    uiService.showMessageDialog(this, 
                        "ML Pattern generated successfully!\n" +
                        "Text: " + finalText + "\n" +
                        "Year: " + year + "\n" +
                        "Active cells: " + patternService.getActiveCellsCount(patternData), 
                        "ML Generation Complete", JOptionPane.INFORMATION_MESSAGE);
                });
                
            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> {
                    progressDialog.dispose();
                    uiService.showMessageDialog(this, "Error generating ML pattern: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
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
        
        // Optimization parameters using UIService
        JPanel paramsPanel = new JPanel(new GridLayout(4, 2, 5, 5));
        
        JSlider densitySlider = uiService.createStyledSlider(0, 100, 50, "Density");
        JSlider symmetrySlider = uiService.createStyledSlider(0, 100, 50, "Symmetry");
        JSlider continuitySlider = uiService.createStyledSlider(0, 100, 50, "Continuity");
        
        paramsPanel.add(uiService.createStyledLabel("Density:"));
        paramsPanel.add(densitySlider);
        paramsPanel.add(uiService.createStyledLabel("Symmetry:"));
        paramsPanel.add(symmetrySlider);
        paramsPanel.add(uiService.createStyledLabel("Continuity:"));
        paramsPanel.add(continuitySlider);
        
        JButton optimizeButton = uiService.createStyledButton("Optimize Pattern", new Color(139, 69, 19), "Apply optimization");
        optimizeButton.addActionListener(e -> {
            double density = densitySlider.getValue() / 100.0;
            double symmetry = symmetrySlider.getValue() / 100.0;
            double continuity = continuitySlider.getValue() / 100.0;
            
            // Generate optimized pattern using PatternService
            int[][] optimizedPattern = patternService.generateCustomPattern(finalText, year, density, symmetry, continuity);
            
            // Apply the optimized pattern
            for (int row = 0; row < GRID_ROWS; row++) {
                for (int col = 0; col < GRID_COLS; col++) {
                    patternData[row][col] = optimizedPattern[row][col];
                    updateCellAppearance(row, col);
                }
            }
            
            optimizationDialog.dispose();
            
            uiService.showMessageDialog(this, 
                "Pattern optimized successfully!\n" +
                "Density: " + String.format("%.1f", density) + "\n" +
                "Symmetry: " + String.format("%.1f", symmetry) + "\n" +
                "Continuity: " + String.format("%.1f", continuity), 
                "Optimization Complete", JOptionPane.INFORMATION_MESSAGE);
        });
        
        JButton cancelButton = uiService.createStyledButton("Cancel", new Color(156, 163, 175), "Cancel optimization");
        cancelButton.addActionListener(e -> optimizationDialog.dispose());
        
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(optimizeButton);
        buttonPanel.add(cancelButton);
        
        optimizationPanel.add(uiService.createStyledLabel("Adjust optimization parameters:"), BorderLayout.NORTH);
        optimizationPanel.add(paramsPanel, BorderLayout.CENTER);
        optimizationPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        optimizationDialog.add(optimizationPanel);
        optimizationDialog.setVisible(true);
    }
    
    /**
     * View commits by year
     */
    private void viewCommitsByYear() {
        // Show year selection dialog using UIService
        String yearText = uiService.showInputDialog(this, "Enter year to view commits:", "View Commits by Year");
        
        if (yearText == null || yearText.trim().isEmpty()) {
            return;
        }
        
        int year;
        try {
            year = Integer.parseInt(yearText.trim());
        } catch (NumberFormatException e) {
            uiService.showMessageDialog(this, "Please enter a valid year!", "Invalid Year", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // Show progress dialog using UIService
        JDialog progressDialog = uiService.createProgressDialog(this, "Loading Commits", "Loading commits for year " + year + "...");
        
        // Load commits in background thread using GitService
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(() -> {
            try {
                SwingUtilities.invokeLater(() -> progressDialog.setVisible(true));
                
                // Get commits for the year using GitService
                String commits = gitService.getCommitsByYear(year);
                
                SwingUtilities.invokeLater(() -> {
                    progressDialog.dispose();
                    showCommitsDialog(year, commits);
                });
                
            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> {
                    progressDialog.dispose();
                    uiService.showMessageDialog(this, "Error loading commits: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
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
        // Show year selection dialog using UIService
        String yearText = uiService.showInputDialog(this, "Enter year to delete commits:", "Delete Commits by Year");
        
        if (yearText == null || yearText.trim().isEmpty()) {
            return;
        }
        
        int year;
        try {
            year = Integer.parseInt(yearText.trim());
        } catch (NumberFormatException e) {
            uiService.showMessageDialog(this, "Please enter a valid year!", "Invalid Year", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // Confirm deletion using UIService
        boolean confirmed = uiService.showConfirmationDialog(this, "Confirm Deletion", 
            "Are you sure you want to delete ALL commits for year " + year + "?\n\n" +
            "This action cannot be undone!");
        
        if (!confirmed) {
            return;
        }
        
        // Show progress dialog using UIService
        JDialog progressDialog = uiService.createProgressDialog(this, "Deleting Commits", "Deleting commits for year " + year + "...");
        
        // Delete commits in background thread using GitService
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(() -> {
            try {
                SwingUtilities.invokeLater(() -> progressDialog.setVisible(true));
                
                // Delete commits for the year using GitService
                int deletedCount = gitService.deleteCommitsByYear(year);
                
                SwingUtilities.invokeLater(() -> {
                    progressDialog.dispose();
                    uiService.showMessageDialog(this, 
                        "Successfully deleted " + deletedCount + " commits for year " + year + "!", 
                        "Deletion Complete", JOptionPane.INFORMATION_MESSAGE);
                });
                
            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> {
                    progressDialog.dispose();
                    uiService.showMessageDialog(this, "Error deleting commits: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                });
            }
            return null;
        });
        
        executor.shutdown();
    }
    
    
    /**
     * Show commits in a dialog
     */
    private void showCommitsDialog(int year, String commits) {
        JDialog commitsDialog = new JDialog(this, "Commits for Year " + year, true);
        commitsDialog.setSize(800, 600);
        commitsDialog.setLocationRelativeTo(this);
        
        JPanel mainPanel = new JPanel(new BorderLayout());
        
        // Use UIService to create styled text area
        JTextArea commitsArea = uiService.createStyledTextArea(20, 50);
        commitsArea.setText(commits);
        commitsArea.setFont(new Font("Consolas", Font.PLAIN, 11));
        commitsArea.setBackground(new Color(248, 249, 250));
        
        JScrollPane scrollPane = new JScrollPane(commitsArea);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(209, 213, 219), 1));
        
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton closeButton = uiService.createStyledButton("Close", new Color(156, 163, 175), "Close dialog");
        closeButton.addActionListener(e -> commitsDialog.dispose());
        buttonPanel.add(closeButton);
        
        JButton deleteButton = uiService.createStyledButton("Delete These Commits", new Color(185, 28, 28), "Delete all shown commits");
        deleteButton.addActionListener(e -> {
            commitsDialog.dispose();
            // Show confirmation dialog using UIService
            boolean confirmed = uiService.showConfirmationDialog(this, "Confirm Deletion", 
                "Are you sure you want to delete ALL commits for year " + year + "?\n\n" +
                "This action cannot be undone!");
            
            if (confirmed) {
                // Show progress dialog using UIService
                JDialog progressDialog = uiService.createProgressDialog(this, "Deleting Commits", "Deleting commits for year " + year + "...");
                
                // Delete commits in background thread using GitService
                ExecutorService executor = Executors.newSingleThreadExecutor();
                executor.submit(() -> {
                    try {
                        SwingUtilities.invokeLater(() -> progressDialog.setVisible(true));
                        
                        // Delete commits for the year using GitService
                        int deletedCount = gitService.deleteCommitsByYear(year);
                        
                        SwingUtilities.invokeLater(() -> {
                            progressDialog.dispose();
                            uiService.showMessageDialog(this, 
                                "Successfully deleted " + deletedCount + " commits for year " + year + "!", 
                                "Deletion Complete", JOptionPane.INFORMATION_MESSAGE);
                        });
                        
                    } catch (Exception ex) {
                        SwingUtilities.invokeLater(() -> {
                            progressDialog.dispose();
                            uiService.showMessageDialog(this, "Error deleting commits: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
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
