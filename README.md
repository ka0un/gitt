# Simple GitHub Contribution Graph Generator

A simple GUI application to design patterns and generate GitHub contribution graphs.

![GitHub Contribution Graph Generator](image.png)

## Features

- **Visual Design**: Click on a 7×53 grid to design your pattern
- **Real Git Commits**: Creates actual git commits with proper dates
- **Pattern Storage**: Save and load your favorite patterns
- **Progress Tracking**: Real-time progress with threading
- **Simple Interface**: No complex dependencies or service layers
- **Ready to Use**: Generates batch files for immediate execution

## How to Use

1. **Run the Application**:
   ```bash
   simple-github.bat
   ```

2. **Design Your Pattern**:
   - Click on the 7×53 grid to toggle cells (green = active, white = inactive)
   - Design any pattern you want (letters, shapes, etc.)

3. **Set Parameters**:
   - Enter the year (e.g., 2024)
   - Enter the text (e.g., HELLO)

4. **Choose Your Option**:
   - **"Generate Commits"**: Creates `git_commands.bat` file
   - **"Create Real Commits"**: Creates actual git commits directly
   - **"Save Pattern"**: Save your design with a name
   - **"Load Pattern"**: Load previously saved patterns

5. **Execute**:
   - For batch file: Run `git_commands.bat` to create the commits
   - For real commits: Commits are created automatically with progress tracking
   - Check your GitHub contribution graph!

## Files

- `SimpleGitHubApp.java` - Main application (single file)
- `simple-github.bat` - Run script
- `git_commands.bat` - Generated git commands (created after use)
- `saved_patterns.txt` - Saved pattern storage (created after use)
- `contribution_pattern.txt` - File used for commits (created during commit generation)

## Requirements

- Java 8 or higher
- Git installed

## Advanced Features

### Pattern Storage
- Save your favorite patterns with custom names
- Load patterns instantly from the dropdown
- Patterns are stored in `saved_patterns.txt`

### Real Git Commits
- Creates actual git commits (not just commands)
- Uses proper GitHub contribution graph dates
- Respects your existing git configuration
- Progress tracking with threading

### GitHub Integration
- Commits appear on your GitHub contribution graph
- Proper date mapping (starts from first Sunday of year)
- Uses your existing git identity
- Ready to push to GitHub

## No Dependencies

This application has no external dependencies - just pure Java!