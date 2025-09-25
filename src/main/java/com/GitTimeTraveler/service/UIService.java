package com.GitTimeTraveler.service;

import javax.swing.*;
import java.awt.*;

/**
 * Service interface for UI operations and styling
 */
public interface UIService {
    
    /**
     * Create a styled label with modern appearance
     */
    JLabel createStyledLabel(String text);
    
    /**
     * Create a styled button with modern appearance and tooltip
     */
    JButton createStyledButton(String text, Color backgroundColor, String tooltip);
    
    /**
     * Create a spacer component for layout
     */
    Component createSpacer(int width);
    
    /**
     * Create a styled input field
     */
    JTextField createStyledTextField(String defaultValue, int columns);
    
    /**
     * Create a styled text area
     */
    JTextArea createStyledTextArea(int rows, int columns);
    
    /**
     * Create a styled slider with labels
     */
    JSlider createStyledSlider(int min, int max, int value, String label);
    
    /**
     * Create a styled panel with border
     */
    JPanel createStyledPanel(String title, Color borderColor);
    
    /**
     * Show progress dialog
     */
    JDialog createProgressDialog(Component parent, String title, String message);
    
    /**
     * Update progress dialog
     */
    void updateProgressDialog(JDialog dialog, int current, int total, String message);
    
    /**
     * Show confirmation dialog
     */
    boolean showConfirmationDialog(Component parent, String title, String message);
    
    /**
     * Show input dialog
     */
    String showInputDialog(Component parent, String title, String message);
    
    /**
     * Show message dialog
     */
    void showMessageDialog(Component parent, String title, String message, int messageType);
}
