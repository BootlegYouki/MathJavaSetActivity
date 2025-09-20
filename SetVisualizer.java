import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;

public class SetVisualizer extends JFrame {
    private JPanel mainPanel, inputPanel, diagramPanel, resultPanel;
    private JLabel titleLabel, numSetsLabel, operationLabel;
    private JComboBox<Integer> numSetsComboBox;
    private JComboBox<String> operationComboBox;
    private JButton calculateButton;
    private JTextArea resultArea;
    private List<JTextField> setFields;
    private Set<String>[] sets;
    private Set<String> resultSet;
    private String currentOperation;

    // Venn diagram layout parameters
    private int vennRadius2 = 100; // radius for 2-set
    private int vennRadius3 = 100;  // radius for 3-set
    private int vennOffset2 = 10;  // horizontal offset for 2-set
    private int vennOffset3 = 10;  // offset for 3-set
    private int vennDiagramXOffset = -40; // horizontal offset for the entire diagram
    private int vennValueXOffset = 55;
    
    // Data structure to hold elements for each region of the Venn diagram
    private Map<String, Set<String>> vennRegions;
    
    public SetVisualizer() {
        setTitle("Set Operations Visualizer with Venn Diagrams");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 1000);
        setLocationRelativeTo(null);
        
        // Initialize components
        mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        inputPanel = new JPanel(new GridBagLayout());
        inputPanel.setBorder(BorderFactory.createTitledBorder("Input"));
        
        diagramPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                drawVennDiagram(g);
            }
        };
        diagramPanel.setPreferredSize(new Dimension(1000, 300)); // Further increased width and height for full Venn diagram visibility
        diagramPanel.setBorder(BorderFactory.createTitledBorder("Venn Diagram"));
        
        resultPanel = new JPanel(new BorderLayout());
        resultPanel.setBorder(BorderFactory.createTitledBorder("Result"));
        
        titleLabel = new JLabel("Set Operations Visualizer", JLabel.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        
        numSetsLabel = new JLabel("Number of Sets:");
        operationLabel = new JLabel("Operation:");
        
        Integer[] setOptions = {2, 3};
        numSetsComboBox = new JComboBox<>(setOptions);
        numSetsComboBox.addActionListener(e -> updateSetInputs());
        
        String[] operations = {"Union", "Intersection", "Difference", "Symmetric Difference"};
        operationComboBox = new JComboBox<>(operations);
        
        calculateButton = new JButton("Calculate");
        calculateButton.addActionListener(e -> calculateResult());
        
        resultArea = new JTextArea(8, 40);
        resultArea.setEditable(false);
        resultArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
        JScrollPane scrollPane = new JScrollPane(resultArea);
        
        setFields = new ArrayList<>();
        sets = new HashSet[3]; // Maximum 3 sets
        resultSet = new HashSet<>();
        currentOperation = "Union";
        vennRegions = new HashMap<>();
        
        // Layout components
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        gbc.gridx = 0; gbc.gridy = 0;
        inputPanel.add(numSetsLabel, gbc);
        
        gbc.gridx = 1; gbc.gridy = 0;
        inputPanel.add(numSetsComboBox, gbc);
        
        gbc.gridx = 0; gbc.gridy = 1;
        inputPanel.add(operationLabel, gbc);
        
        gbc.gridx = 1; gbc.gridy = 1;
        inputPanel.add(operationComboBox, gbc);
        
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(calculateButton);
        
        mainPanel.add(titleLabel, BorderLayout.NORTH);
        mainPanel.add(inputPanel, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        add(mainPanel, BorderLayout.NORTH);
        add(diagramPanel, BorderLayout.CENTER);
        add(resultPanel, BorderLayout.SOUTH);
        
        resultPanel.add(scrollPane, BorderLayout.CENTER);
        
        // Initialize set inputs
        updateSetInputs();
    }
    
    private void updateSetInputs() {
        // Remove existing set input fields
        for (Component comp : inputPanel.getComponents()) {
            if (comp instanceof JLabel && ((JLabel)comp).getText().startsWith("Set")) {
                inputPanel.remove(comp);
            }
        }
        
        for (JTextField field : setFields) {
            inputPanel.remove(field);
        }
        setFields.clear();
        
        int numSets = (Integer) numSetsComboBox.getSelectedItem();
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        for (int i = 0; i < numSets; i++) {
            char setName = (char) ('A' + i);
            JLabel setLabel = new JLabel("Set " + setName + " (comma separated):");
            JTextField setField = new JTextField(20);
            
            gbc.gridx = 0; gbc.gridy = i + 2;
            inputPanel.add(setLabel, gbc);
            
            gbc.gridx = 1; gbc.gridy = i + 2;
            inputPanel.add(setField, gbc);
            
            setFields.add(setField);
        }
        
        // Refresh the UI
        inputPanel.revalidate();
        inputPanel.repaint();
    }
    
    private void calculateResult() {
        try {
            int numSets = (Integer) numSetsComboBox.getSelectedItem();
            currentOperation = (String) operationComboBox.getSelectedItem();
            
            // Parse the sets
            for (int i = 0; i < numSets; i++) {
                String text = setFields.get(i).getText();
                String[] elements = text.split(",");
                sets[i] = new HashSet<>();
                for (String element : elements) {
                    String trimmed = element.trim();
                    if (!trimmed.isEmpty()) {
                        sets[i].add(trimmed);
                    }
                }
            }
            
            // Clear any remaining sets
            for (int i = numSets; i < 3; i++) {
                sets[i] = null;
            }
            
            // Calculate Venn diagram regions
            calculateVennRegions(numSets);
            
            // Perform the operation
            resultSet.clear();
            
            switch (currentOperation) {
                case "Union":
                    for (int i = 0; i < numSets; i++) {
                        if (sets[i] != null) {
                            resultSet.addAll(sets[i]);
                        }
                    }
                    break;
                    
                case "Intersection":
                    if (numSets > 0 && sets[0] != null) {
                        resultSet.addAll(sets[0]);
                        for (int i = 1; i < numSets; i++) {
                            if (sets[i] != null) {
                                resultSet.retainAll(sets[i]);
                            }
                        }
                    }
                    break;
                    
                case "Difference":
                    if (numSets >= 2 && sets[0] != null && sets[1] != null) {
                        resultSet.addAll(sets[0]);
                        resultSet.removeAll(sets[1]);
                    } else if (numSets >= 1 && sets[0] != null) {
                        resultSet.addAll(sets[0]);
                    }
                    break;
                    
                case "Symmetric Difference":
                    if (numSets >= 2 && sets[0] != null && sets[1] != null) {
                        // For two sets: (A ∪ B) \ (A ∩ B)
                        Set<String> union = new HashSet<>(sets[0]);
                        union.addAll(sets[1]);
                        
                        Set<String> intersection = new HashSet<>(sets[0]);
                        intersection.retainAll(sets[1]);
                        
                        resultSet.addAll(union);
                        resultSet.removeAll(intersection);
                    } else if (numSets >= 1 && sets[0] != null) {
                        resultSet.addAll(sets[0]);
                    }
                    break;
            }
            
            // Display the result
            StringBuilder sb = new StringBuilder();
            sb.append("Operation: ").append(currentOperation).append("\n\n");
            
            for (int i = 0; i < numSets; i++) {
                char setName = (char) ('A' + i);
                sb.append("Set ").append(setName).append(": ").append(sets[i]).append("\n");
            }
            
            sb.append("\nResult: ");
            if (resultSet.isEmpty()) {
                sb.append("None");
            } else {
                sb.append(resultSet);
            }
            resultArea.setText(sb.toString());
            
            // Update the Venn diagram
            diagramPanel.repaint();
            
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error in input: " + ex.getMessage(), 
                                         "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void calculateVennRegions(int numSets) {
        vennRegions.clear();
        
        if (numSets == 2) {
            // Calculate regions for 2-set Venn diagram
            Set<String> onlyA = new HashSet<>(sets[0]);
            onlyA.removeAll(sets[1]);
            vennRegions.put("onlyA", onlyA);
            
            Set<String> onlyB = new HashSet<>(sets[1]);
            onlyB.removeAll(sets[0]);
            vennRegions.put("onlyB", onlyB);
            
            Set<String> intersectionAB = new HashSet<>(sets[0]);
            intersectionAB.retainAll(sets[1]);
            vennRegions.put("intersectionAB", intersectionAB);
            
        } else if (numSets == 3) {
            // Calculate regions for 3-set Venn diagram
            Set<String> onlyA = new HashSet<>(sets[0]);
            onlyA.removeAll(sets[1]);
            onlyA.removeAll(sets[2]);
            vennRegions.put("onlyA", onlyA);
            
            Set<String> onlyB = new HashSet<>(sets[1]);
            onlyB.removeAll(sets[0]);
            onlyB.removeAll(sets[2]);
            vennRegions.put("onlyB", onlyB);
            
            Set<String> onlyC = new HashSet<>(sets[2]);
            onlyC.removeAll(sets[0]);
            onlyC.removeAll(sets[1]);
            vennRegions.put("onlyC", onlyC);
            
            Set<String> intersectionAB = new HashSet<>(sets[0]);
            intersectionAB.retainAll(sets[1]);
            intersectionAB.removeAll(sets[2]);
            vennRegions.put("intersectionAB", intersectionAB);
            
            Set<String> intersectionAC = new HashSet<>(sets[0]);
            intersectionAC.retainAll(sets[2]);
            intersectionAC.removeAll(sets[1]);
            vennRegions.put("intersectionAC", intersectionAC);
            
            Set<String> intersectionBC = new HashSet<>(sets[1]);
            intersectionBC.retainAll(sets[2]);
            intersectionBC.removeAll(sets[0]);
            vennRegions.put("intersectionBC", intersectionBC);
            
            Set<String> intersectionABC = new HashSet<>(sets[0]);
            intersectionABC.retainAll(sets[1]);
            intersectionABC.retainAll(sets[2]);
            vennRegions.put("intersectionABC", intersectionABC);
        }
    }
    
    private void drawVennDiagram(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        int width = diagramPanel.getWidth();
        int height = diagramPanel.getHeight();
        
        // Clear the background
        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, width, height);
        
        int numSets = (Integer) numSetsComboBox.getSelectedItem();
        
        if (numSets == 2) {
            drawTwoSetVennDiagram(g2d, width, height);
        } else if (numSets == 3) {
            drawThreeSetVennDiagram(g2d, width, height);
        }
    }
    
    private void drawTwoSetVennDiagram(Graphics2D g2d, int width, int height) {
    int centerX = width / 2 + vennDiagramXOffset;
    int centerY = height / 2;
    int radius = vennRadius2;
        
        // Draw circles
        Color color1 = new Color(255, 0, 0, 128); // Semi-transparent red
        Color color2 = new Color(0, 0, 255, 128); // Semi-transparent blue
        
        // Draw set A
        g2d.setColor(color1);
        g2d.fillOval(centerX - radius - vennOffset2, centerY - radius, radius * 2, radius * 2);
        
        // Draw set B
        g2d.setColor(color2);
        g2d.fillOval(centerX + vennOffset2, centerY - radius, radius * 2, radius * 2);
        
        // Draw outlines
        g2d.setColor(Color.BLACK);
        g2d.drawOval(centerX - radius - vennOffset2, centerY - radius, radius * 2, radius * 2);
        g2d.drawOval(centerX + vennOffset2, centerY - radius, radius * 2, radius * 2);
        
        // Draw labels
        g2d.setFont(new Font("Arial", Font.BOLD, 16));
        g2d.drawString("A", centerX - radius - vennOffset2 + radius - 13, centerY - radius - 15);
        g2d.drawString("B", centerX + vennOffset2 + radius - 5, centerY - radius - 15);
        
        // Draw elements in each region
        g2d.setColor(Color.BLACK);
        g2d.setFont(new Font("Arial", Font.PLAIN, 12));
        
        // Only in A
        String onlyA = String.join(", ", vennRegions.getOrDefault("onlyA", new HashSet<>()));
        if (!onlyA.isEmpty()) {
            drawWrappedText(g2d, onlyA, centerX - radius + 10 + vennValueXOffset, centerY - 10, 100);
        }
        
        // Only in B
        String onlyB = String.join(", ", vennRegions.getOrDefault("onlyB", new HashSet<>()));
        if (!onlyB.isEmpty()) {
            drawWrappedText(g2d, onlyB, centerX + radius - 20 + vennValueXOffset, centerY - 10, 100);
        }
        
        // In both A and B
        String intersectionAB = String.join(", ", vennRegions.getOrDefault("intersectionAB", new HashSet<>()));
        if (!intersectionAB.isEmpty()) {
            drawWrappedText(g2d, intersectionAB, centerX - 5 + vennValueXOffset, centerY - 10, 100);
        }
    }
    
    private void drawThreeSetVennDiagram(Graphics2D g2d, int width, int height) {
    int centerX = width / 2 + vennDiagramXOffset;
    int centerY = height / 2;
    int radius = vennRadius3;
        
        // Draw circles
        Color color1 = new Color(255, 0, 0, 128); // Semi-transparent red
        Color color2 = new Color(0, 0, 255, 128); // Semi-transparent blue
        Color color3 = new Color(0, 150, 0, 128); // Semi-transparent green
        
        g2d.setColor(color1);
        g2d.fillOval(centerX - radius - vennOffset3, centerY - radius, radius * 2, radius * 2);

        // Draw set B (right)
        g2d.setColor(color2);
        g2d.fillOval(centerX + vennOffset3, centerY - radius, radius * 2, radius * 2);

        // Draw set C (bottom)
        g2d.setColor(color3);
        g2d.fillOval(centerX - 50, centerY + vennOffset3, radius * 2, radius * 2);

        // Draw outlines
        g2d.setColor(Color.BLACK);
        g2d.drawOval(centerX - radius - vennOffset3, centerY - radius, radius * 2, radius * 2);
        g2d.drawOval(centerX + vennOffset3, centerY - radius, radius * 2, radius * 2);
        g2d.drawOval(centerX - 50, centerY + vennOffset3, radius * 2, radius * 2);
                
        // Draw labels
        g2d.setFont(new Font("Arial", Font.BOLD, 16));
        g2d.drawString("A", centerX - radius - vennOffset3 + radius - 8, centerY - radius - 15);
        g2d.drawString("B", centerX + vennOffset3 + radius - 8, centerY - radius - 15);
        g2d.drawString("C", centerX + 45, centerY + vennOffset3 + radius * 2 + 20);
        
        // Draw elements in each region
        g2d.setColor(Color.BLACK);
        g2d.setFont(new Font("Arial", Font.PLAIN, 12));
        
        // Only in A
        String onlyA = String.join(", ", vennRegions.getOrDefault("onlyA", new HashSet<>()));
        if (!onlyA.isEmpty()) {
            drawWrappedText(g2d, onlyA, centerX - radius + 50, centerY - 20, 80);
        }
        
        // Only in B
        String onlyB = String.join(", ", vennRegions.getOrDefault("onlyB", new HashSet<>()));
        if (!onlyB.isEmpty()) {
            drawWrappedText(g2d, onlyB, centerX + radius + 30, centerY - 20, 80);
        }
        
        // Only in C
        String onlyC = String.join(", ", vennRegions.getOrDefault("onlyC", new HashSet<>()));
        if (!onlyC.isEmpty()) {
            drawWrappedText(g2d, onlyC, centerX + 45, centerY + radius + 45, 80);
        }
        
        // In A and B only
        String intersectionAB = String.join(", ", vennRegions.getOrDefault("intersectionAB", new HashSet<>()));
        if (!intersectionAB.isEmpty()) {
            drawWrappedText(g2d, intersectionAB, centerX + 50, centerY - 30, 80);
        }
        
        // In A and C only
        String intersectionAC = String.join(", ", vennRegions.getOrDefault("intersectionAC", new HashSet<>()));
        if (!intersectionAC.isEmpty()) {
            drawWrappedText(g2d, intersectionAC, centerX - 10, centerY + 70, 80);
        }
        
        // In B and C only
        String intersectionBC = String.join(", ", vennRegions.getOrDefault("intersectionBC", new HashSet<>()));
        if (!intersectionBC.isEmpty()) {
            drawWrappedText(g2d, intersectionBC, centerX + 100, centerY + 70, 80);
        }
        
        // In A, B, and C
        String intersectionABC = String.join(", ", vennRegions.getOrDefault("intersectionABC", new HashSet<>()));
        if (!intersectionABC.isEmpty()) {
            drawWrappedText(g2d, intersectionABC, centerX + 50, centerY + 40, 80);
        }
    }
    
    private void drawWrappedText(Graphics2D g2d, String text, int x, int y, int maxWidth) {
        FontMetrics fm = g2d.getFontMetrics();
        String[] words = text.split(",\\s*");
        List<String> lines = new ArrayList<>();

        StringBuilder currentLine = new StringBuilder();
        int count = 0;
        for (int i = 0; i < words.length; i++) {
            if (count > 0) {
                currentLine.append(",");
            }
            currentLine.append(words[i]);
            count++;
            if (count == 5 || i == words.length - 1) {
                lines.add(currentLine.toString());
                currentLine = new StringBuilder();
                count = 0;
            }
        }

        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            int lineWidth = fm.stringWidth(line);
            g2d.drawString(line, x - lineWidth / 2, y + i * fm.getHeight());
        }
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            SetVisualizer visualizer = new SetVisualizer();
            visualizer.setVisible(true);
        });
    }
}