package com.GitTimeTraveler.service.impl;

import javax.swing.*;

import com.GitTimeTraveler.service.UIService;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Implementation of UIService for UI operations and styling
 */
public class UIServiceImpl implements UIService {
    
    @Override
    public JLabel createStyledLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Arial", Font.PLAIN, 11));
        label.setForeground(new Color(75, 85, 99));
        return label;
    }
    
    @Override
    public JButton createStyledButton(String text, Color backgroundColor, String tooltip) {
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
    
    @Override
    public Component createSpacer(int width) {
        return Box.createHorizontalStrut(width);
    }
    
    @Override
    public JTextField createStyledTextField(String defaultValue, int columns) {
        JTextField textField = new JTextField(defaultValue, columns);
        textField.setFont(new Font("Arial", Font.PLAIN, 11));
        textField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(209, 213, 219), 1),
            BorderFactory.createEmptyBorder(4, 8, 4, 8)
        ));
        return textField;
    }
    
    @Override
    public JTextArea createStyledTextArea(int rows, int columns) {
        JTextArea textArea = new JTextArea(rows, columns);
        textArea.setFont(new Font("Consolas", Font.PLAIN, 11));
        textArea.setEditable(false);
        textArea.setBorder(BorderFactory.createLineBorder(new Color(209, 213, 219), 1));
        textArea.setBackground(new Color(255, 255, 255));
        return textArea;
    }
    
    @Override
    public JSlider createStyledSlider(int min, int max, int value, String label) {
        JSlider slider = new JSlider(min, max, value);
        slider.setMajorTickSpacing((max - min) / 4);
        slider.setPaintTicks(true);
        slider.setPaintLabels(true);
        slider.setSnapToTicks(true);
        slider.setPreferredSize(new Dimension(150, 40));
        return slider;
    }
    
    @Override
    public JPanel createStyledPanel(String title, Color borderColor) {
        JPanel panel = new JPanel();
        panel.setBackground(new Color(248, 249, 250));
        panel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(borderColor, 2), 
            title, 
            0, 0, 
            new Font("Arial", Font.BOLD, 12), 
            borderColor
        ));
        return panel;
    }
    
    @Override
    public JDialog createProgressDialog(Component parent, String title, String message) {
        JDialog progressDialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(parent), title, true);
        progressDialog.setSize(400, 150);
        progressDialog.setLocationRelativeTo(parent);
        
        JPanel progressPanel = new JPanel(new BorderLayout());
        JLabel progressLabel = new JLabel(message, JLabel.CENTER);
        JProgressBar progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);
        
        progressPanel.add(progressLabel, BorderLayout.CENTER);
        progressPanel.add(progressBar, BorderLayout.SOUTH);
        progressDialog.add(progressPanel);
        
        return progressDialog;
    }
    
    @Override
    public void updateProgressDialog(JDialog dialog, int current, int total, String message) {
        SwingUtilities.invokeLater(() -> {
            JPanel panel = (JPanel) dialog.getContentPane().getComponent(0);
            JLabel label = (JLabel) panel.getComponent(0);
            JProgressBar progressBar = (JProgressBar) panel.getComponent(1);
            
            label.setText(message);
            progressBar.setIndeterminate(false);
            progressBar.setMaximum(total);
            progressBar.setValue(current);
        });
    }
    
    @Override
    public boolean showConfirmationDialog(Component parent, String title, String message) {
        int result = JOptionPane.showConfirmDialog(parent, message, title, 
            JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        return result == JOptionPane.YES_OPTION;
    }
    
    @Override
    public String showInputDialog(Component parent, String title, String message) {
        return JOptionPane.showInputDialog(parent, message, title, JOptionPane.QUESTION_MESSAGE);
    }
    
    @Override
    public void showMessageDialog(Component parent, String title, String message, int messageType) {
        JOptionPane.showMessageDialog(parent, message, title, messageType);
    }
}
