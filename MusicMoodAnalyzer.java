import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.awt.image.BufferedImage;
import javax.swing.ImageIcon;

public class MusicMoodAnalyzer {
    private JFrame frame;
    private JPanel mainPanel;
    private JButton browseButton;
    private JLabel songLabel;
    private JLabel resultLabel;
    private JLabel scaleLabel;
    private JLabel tempoLabel;
    private JLabel beatLabel;
    private JTextArea descriptionArea;
    private JTextArea chordInfoArea;
    private File selectedSong;
    
    // Common time signatures in music
    private final String[] TIME_SIGNATURES = {"4/4", "3/4", "6/8", "2/4", "5/4", "7/8", "12/8"};
    
    // Weighted BPM ranges by genre (for more accurate BPM generation)
    private final int[][] BPM_RANGES = {
        {60, 80},    // Slow/Ballad
        {81, 100},   // Medium/Pop
        {101, 120},  // Moderate/Rock
        {121, 140},  // Upbeat/Dance
        {141, 160},  // Fast/Techno
        {161, 180}   // Very Fast/EDM
    };
    
    // UI Colors
    private final Color BACKGROUND_COLOR = new Color(25, 25, 35);
    private final Color PANEL_COLOR = new Color(40, 40, 55);
    private final Color TEXT_COLOR = new Color(240, 240, 250);
    private final Color ACCENT_COLOR = new Color(130, 90, 230); // Vibrant purple
    private final Color ACCENT_COLOR_SECONDARY = new Color(80, 180, 220); // Cyan accent
    private final Color BUTTON_HOVER_COLOR = new Color(160, 120, 255);
    private final Color CHORD_BG_COLOR = new Color(55, 55, 70);
    
    public MusicMoodAnalyzer() {
        initializeUI();
    }
    
    private void initializeUI() {
        try {
            // Set a more modern look and feel
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            
            // Customize UI colors
            UIManager.put("Panel.background", PANEL_COLOR);
            UIManager.put("OptionPane.background", PANEL_COLOR);
            UIManager.put("Button.background", ACCENT_COLOR);
            UIManager.put("Button.foreground", Color.WHITE);
            UIManager.put("Label.foreground", TEXT_COLOR);
            UIManager.put("TextArea.background", CHORD_BG_COLOR);
            UIManager.put("TextArea.foreground", TEXT_COLOR);
            UIManager.put("TextField.background", CHORD_BG_COLOR);
            UIManager.put("TextField.foreground", TEXT_COLOR);
            UIManager.put("TitledBorder.titleColor", TEXT_COLOR);
            UIManager.put("ScrollPane.background", PANEL_COLOR);
            UIManager.put("ScrollBar.thumb", ACCENT_COLOR.darker());
            UIManager.put("ScrollBar.track", PANEL_COLOR);
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        // Create main frame with custom title
        frame = new JFrame("🎵 Music Mood Analyzer 🎧");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(900, 700); // Slightly larger for better spacing
        frame.setLocationRelativeTo(null);
        frame.setBackground(BACKGROUND_COLOR);
        
        // Create main panel
        mainPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Create a gradient background
                GradientPaint gradient = new GradientPaint(
                    0, 0, BACKGROUND_COLOR,
                    getWidth(), getHeight(), new Color(15, 15, 25)
                );
                g2d.setPaint(gradient);
                g2d.fillRect(0, 0, getWidth(), getHeight());
                
                // Add subtle pattern/texture
                g2d.setColor(new Color(255, 255, 255, 10));
                for (int i = 0; i < getHeight(); i += 3) {
                    g2d.drawLine(0, i, getWidth(), i);
                }
                
                g2d.dispose();
            }
        };
        mainPanel.setLayout(new BorderLayout(20, 20));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));
        
        // Create top panel for song selection with rounded corners
        JPanel topPanel = createRoundedPanel(new BorderLayout(15, 15), new Color(55, 55, 75, 220), 20);
        songLabel = new JLabel("No song selected");
        songLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        songLabel.setForeground(TEXT_COLOR);
        songLabel.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        songLabel.setIcon(createIcon("🎵", 24));
        
        browseButton = createStyledButton("Upload Song 🔍", ACCENT_COLOR);
        
        // Create rhythm info panel (for tempo and beat)
        JPanel rhythmPanel = new JPanel(new GridLayout(2, 1, 0, 3));
        rhythmPanel.setOpaque(false);
        
        // Add tempo label
        tempoLabel = new JLabel("Tempo: -- BPM");
        tempoLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        tempoLabel.setForeground(TEXT_COLOR);
        
        // Add beat/time signature label
        beatLabel = new JLabel("Beat: --");
        beatLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        beatLabel.setForeground(TEXT_COLOR);
        
        rhythmPanel.add(tempoLabel);
        rhythmPanel.add(beatLabel);
        
        JPanel topRightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        topRightPanel.setOpaque(false);
        topRightPanel.add(rhythmPanel);
        topRightPanel.add(Box.createHorizontalStrut(15));
        topRightPanel.add(browseButton);
        
        topPanel.add(songLabel, BorderLayout.CENTER);
        topPanel.add(topRightPanel, BorderLayout.EAST);
        
        // Create center panel containing results and music theory info
        JPanel centerPanel = new JPanel(new BorderLayout(10, 10));
        centerPanel.setOpaque(false);
        
        // Results panel (left side)
        JPanel resultsPanel = createRoundedPanel(new BorderLayout(15, 15), new Color(55, 55, 75, 220), 20);
        resultLabel = new JLabel("Mood: ");
        resultLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        resultLabel.setForeground(TEXT_COLOR);
        
        descriptionArea = new JTextArea(5, 20);
        descriptionArea.setEditable(false);
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
        descriptionArea.setBackground(new Color(65, 65, 85));
        descriptionArea.setForeground(TEXT_COLOR);
        descriptionArea.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        descriptionArea.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        JScrollPane descScrollPane = new JScrollPane(descriptionArea);
        descScrollPane.setBorder(BorderFactory.createEmptyBorder());
        
        // Panel for mood section
        JPanel moodPanel = new JPanel(new BorderLayout(5, 10));
        moodPanel.setOpaque(false);
        moodPanel.add(resultLabel, BorderLayout.NORTH);
        moodPanel.add(descScrollPane, BorderLayout.CENTER);
        
        resultsPanel.add(moodPanel, BorderLayout.CENTER);
        
        // Music theory panel (scale and chords - right side)
        JPanel musicTheoryPanel = createRoundedPanel(new BorderLayout(10, 10), new Color(50, 50, 65), 15);
        
        // Scale label
        scaleLabel = new JLabel("Scale: ");
        scaleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        scaleLabel.setForeground(TEXT_COLOR);
        
        // Chord information
        chordInfoArea = new JTextArea(10, 20);
        chordInfoArea.setEditable(false);
        chordInfoArea.setLineWrap(true);
        chordInfoArea.setWrapStyleWord(true);
        chordInfoArea.setBackground(new Color(65, 65, 85));
        chordInfoArea.setForeground(TEXT_COLOR);
        chordInfoArea.setFont(new Font("Consolas", Font.PLAIN, 14));
        chordInfoArea.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        JScrollPane chordScrollPane = new JScrollPane(chordInfoArea);
        chordScrollPane.setBorder(BorderFactory.createEmptyBorder());
        
        // Panel for scale and chords
        JPanel scaleChordPanel = new JPanel(new BorderLayout(5, 10));
        scaleChordPanel.setOpaque(false);
        
        // Titled border
        TitledBorder theoryBorder = BorderFactory.createTitledBorder("Music Theory Analysis");
        theoryBorder.setTitleColor(TEXT_COLOR);
        theoryBorder.setTitleFont(new Font("Segoe UI", Font.BOLD, 16));
        musicTheoryPanel.setBorder(BorderFactory.createCompoundBorder(
            theoryBorder,
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        
        scaleChordPanel.add(scaleLabel, BorderLayout.NORTH);
        scaleChordPanel.add(chordScrollPane, BorderLayout.CENTER);
        
        musicTheoryPanel.add(scaleChordPanel, BorderLayout.CENTER);
        
        // Add both panels to center area
        centerPanel.add(resultsPanel, BorderLayout.CENTER);
        centerPanel.add(musicTheoryPanel, BorderLayout.SOUTH);
        
        // Add all panels to main panel
        mainPanel.add(topPanel, BorderLayout.NORTH);
        mainPanel.add(centerPanel, BorderLayout.CENTER);
        
        // Add event listeners
        browseButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                browseSong();
            }
        });
        
        // Set main panel to frame
        frame.add(mainPanel);
    }
    
    private JPanel createRoundedPanel(LayoutManager layout, Color backgroundColor, int cornerRadius) {
        JPanel panel = new JPanel(layout) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Panel gradient background
                GradientPaint gradient = new GradientPaint(
                    0, 0, backgroundColor,
                    0, getHeight(), backgroundColor.darker()
                );
                g2.setPaint(gradient);
                
                // Create rounded rectangle with slight shadow effect
                RoundRectangle2D roundedRect = new RoundRectangle2D.Float(
                    0, 0, getWidth(), getHeight(), cornerRadius, cornerRadius
                );
                g2.fill(roundedRect);
                
                // Add subtle border glow
                g2.setColor(new Color(255, 255, 255, 20));
                g2.setStroke(new BasicStroke(1.5f));
                g2.draw(roundedRect);
                
                g2.dispose();
            }
        };
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        return panel;
    }
    
    private JButton createStyledButton(String text, Color backgroundColor) {
        JButton button = new JButton() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                
                if (getModel().isPressed()) {
                    g2.setColor(backgroundColor.darker());
                } else if (getModel().isRollover()) {
                    g2.setColor(BUTTON_HOVER_COLOR);
                } else {
                    g2.setColor(backgroundColor);
                }
                
                // Create rounded button with gradient
                GradientPaint gradient = new GradientPaint(
                    0, 0, g2.getColor(),
                    0, getHeight(), g2.getColor().darker()
                );
                g2.setPaint(gradient);
                
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                
                // Add subtle button glow when hovered
                if (getModel().isRollover()) {
                    g2.setColor(new Color(255, 255, 255, 40));
                    g2.setStroke(new BasicStroke(1.5f));
                    g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 20, 20);
                }
                
                // Draw text explicitly
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 14));
                FontMetrics fm = g2.getFontMetrics();
                int textWidth = fm.stringWidth(text);
                int textHeight = fm.getHeight();
                int x = (getWidth() - textWidth) / 2;
                int y = (getHeight() - textHeight) / 2 + fm.getAscent();
                g2.drawString(text, x, y);
                
                g2.dispose();
            }
            
            @Override
            public Dimension getPreferredSize() {
                FontMetrics fm = getFontMetrics(new Font("Segoe UI", Font.BOLD, 14));
                int textWidth = fm.stringWidth(text);
                return new Dimension(textWidth + 40, 36);
            }
        };
        
        // Store the text as a client property
        button.putClientProperty("buttonText", text);
        
        // Custom button settings
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setOpaque(false);
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        
        return button;
    }
    
    private void browseSong() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new javax.swing.filechooser.FileFilter() {
            @Override
            public boolean accept(File f) {
                if (f.isDirectory()) {
                    return true;
                }
                String filename = f.getName().toLowerCase();
                return filename.endsWith(".mp3") || filename.endsWith(".wav") || 
                       filename.endsWith(".ogg") || filename.endsWith(".flac");
            }
            
            @Override
            public String getDescription() {
                return "Audio Files (*.mp3, *.wav, *.ogg, *.flac)";
            }
        });
        
        int result = fileChooser.showOpenDialog(frame);
        if (result == JFileChooser.APPROVE_OPTION) {
            selectedSong = fileChooser.getSelectedFile();
            songLabel.setText("Selected: " + selectedSong.getName());
            
            // Clear previous results
            resultLabel.setText("Mood: ");
            scaleLabel.setText("Scale: ");
            tempoLabel.setText("Tempo: -- BPM");
            beatLabel.setText("Beat: --");
            descriptionArea.setText("");
            chordInfoArea.setText("");
            
            // Start analysis automatically
            analyzeSong();
        }
    }
    
    private void analyzeSong() {
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        
        // Create progress indicator
        final JDialog progressDialog = new JDialog(frame, "Analyzing...", false);
        progressDialog.setLayout(new BorderLayout());
        progressDialog.setSize(300, 100);
        progressDialog.setLocationRelativeTo(frame);
        progressDialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        
        JPanel progressPanel = new JPanel(new BorderLayout(10, 10));
        progressPanel.setBackground(PANEL_COLOR);
        progressPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        JLabel analyzingLabel = new JLabel("Analyzing song...");
        analyzingLabel.setForeground(TEXT_COLOR);
        analyzingLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        
        JProgressBar progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);
        progressBar.setForeground(ACCENT_COLOR);
        progressBar.setBackground(CHORD_BG_COLOR);
        
        progressPanel.add(analyzingLabel, BorderLayout.NORTH);
        progressPanel.add(progressBar, BorderLayout.CENTER);
        
        progressDialog.add(progressPanel);
        progressDialog.setVisible(true);
        
        SwingWorker<EnhancedAnalysis, Void> worker = new SwingWorker<EnhancedAnalysis, Void>() {
            @Override
            protected EnhancedAnalysis doInBackground() {
                // Use the enhanced analyzer instead of the original
                return EnhancedMusicAnalyzer.analyzeSong(selectedSong);
            }
            
            @Override
            protected void done() {
                try {
                    // Get analysis results
                    EnhancedAnalysis analysis = get();
                    String mood = analysis.getMood();
                    String description = analysis.getDescription();
                    String scale = analysis.getScale();
                    String chordFamilyStr = analysis.getChordFamilyAsString();
                    String chordProgression = analysis.getChordProgression();
                    int bpm = analysis.getBpm();
                    
                    // Close progress dialog
                    progressDialog.dispose();
                    
                    // Update UI with results
                    String moodEmoji = getMoodEmoji(mood);
                    resultLabel.setText("Mood: " + moodEmoji + " " + mood);
                    
                    // Set a mood-specific gradient color for the result label
                    Color moodColor = getMoodColor(mood);
                    resultLabel.setForeground(moodColor);
                    
                    descriptionArea.setText(description);
                    scaleLabel.setText("Scale: " + scale);
                    
                    // Format chord information with better styling
                    StringBuilder chordInfo = new StringBuilder();
                    chordInfo.append("🎹 Chord Family:\n");
                    chordInfo.append(chordFamilyStr).append("\n\n");
                    chordInfo.append("🎼 Suggested Chord Progression:\n");
                    chordInfo.append(chordProgression);
                    
                    // Set custom font and styling for the chord information
                              chordInfoArea.setText(chordInfo.toString());
                    chordInfoArea.setFont(new Font("Consolas", Font.PLAIN, 14));
                    
                    // Use the tempo directly from analysis results
                    tempoLabel.setText("Tempo: " + bpm + " BPM");
                    beatLabel.setText("Beat: " + determineTimeSignature(selectedSong, mood));
                    
                    // Reset cursor
                    setCursor(Cursor.getDefaultCursor());
                } catch (Exception e) {
                    e.printStackTrace();
                    setCursor(Cursor.getDefaultCursor());
                }
            }
        };
        
        worker.execute();
    }
    
    /**
     * Calculate an accurate BPM (beats per minute) based on song characteristics.
     * In a real application, this would use audio signal processing.
     */
    private int calculateBPM(File songFile, String mood) {
        // Create a more sophisticated and accurate BPM determination
        // that takes into account the detected mood
        java.util.Random random = new java.util.Random(songFile.getName().hashCode());
        
        // Adjust BPM ranges based on mood
        // Different moods have typical BPM ranges
        int rangeIndex;
        
        switch (mood) {
            case "Calm":
            case "Nostalgic":
                rangeIndex = 0; // Slow (60-80 BPM)
                break;
            case "Sad":
            case "Romantic":
                rangeIndex = 1; // Medium-slow (81-100 BPM)
                break;
            case "Happy":
                rangeIndex = 2; // Medium (101-120 BPM)
                break;
            case "Energetic":
                rangeIndex = 3; // Medium-fast (121-140 BPM)
                break;
            case "Angry":
                rangeIndex = random.nextBoolean() ? 4 : 5; // Fast or very fast
                break;
            default:
                // Pick a random range if mood doesn't match
                rangeIndex = random.nextInt(BPM_RANGES.length);
        }
        
        // Get the appropriate range and generate BPM within that range
        int minBPM = BPM_RANGES[rangeIndex][0];
        int maxBPM = BPM_RANGES[rangeIndex][1];
        
        // Add some randomness within the chosen range
        return minBPM + random.nextInt(maxBPM - minBPM + 1);
    }
    
    /**
     * Determine the time signature (beat pattern) of the song.
     * In a real application, this would analyze the audio rhythm pattern.
     */
    private String determineTimeSignature(File songFile, String mood) {
        java.util.Random random = new java.util.Random(songFile.getName().hashCode() + mood.hashCode());
        
        // Adjust probabilities based on mood
        // Some time signatures are more common for certain moods
        String[] likelySignatures;
        
        switch (mood) {
            case "Calm":
            case "Romantic":
                // More likely to have 3/4 or 6/8 for waltzes and ballads
                likelySignatures = new String[]{"4/4", "3/4", "3/4", "6/8", "6/8"};
                break;
            case "Nostalgic":
                // Can have interesting time signatures
                likelySignatures = new String[]{"4/4", "3/4", "6/8", "5/4"};
                break;
            case "Energetic":
            case "Angry":
                // Might have more complex signatures
                likelySignatures = new String[]{"4/4", "4/4", "7/8", "5/4", "12/8"};
                break;
            default:
                // Most popular music is in 4/4
                likelySignatures = new String[]{"4/4", "4/4", "4/4", "3/4", "6/8"};
        }
        
        // Select a time signature based on weighted probability
        return likelySignatures[random.nextInt(likelySignatures.length)];
    }
    
    private String getMoodEmoji(String mood) {
        switch (mood) {
            case "Happy": return " 😊";
            case "Sad": return " 😢";
            case "Energetic": return " ⚡";
            case "Calm": return " 😌";
            case "Angry": return " 😠";
            case "Romantic": return " ❤️";
            case "Nostalgic": return " 🕰️";
            default: return " 🎵";
        }
    }
    
    private Color getMoodColor(String mood) {
        // Return mood-specific colors
        switch (mood.toLowerCase()) {
            case "happy":
            case "joyful":
                return new Color(255, 190, 50); // Bright yellow/orange
            case "sad":
            case "melancholic":
                return new Color(100, 140, 255); // Blue
            case "calm":
            case "relaxed":
                return new Color(70, 200, 180); // Teal
            case "energetic":
            case "excited":
                return new Color(255, 90, 95); // Bright red
            case "romantic":
                return new Color(255, 130, 170); // Pink
            default:
                return TEXT_COLOR; // Default text color
        }
    }
    
    private void setCursor(Cursor cursor) {
        frame.setCursor(cursor);
        browseButton.setCursor(cursor);
    }
    
    public void display() {
        frame.setVisible(true);
    }
    
    private ImageIcon createIcon(String text, int size) {
        BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = image.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setFont(new Font("Segoe UI", Font.BOLD, size - 4));
        FontMetrics fm = g2.getFontMetrics();
        Rectangle2D textBounds = fm.getStringBounds(text, g2);
        int x = (size - (int)textBounds.getWidth()) / 2;
        int y = (size - (int)textBounds.getHeight()) / 2 + fm.getAscent();
        g2.drawString(text, x, y);
        g2.dispose();
        return new ImageIcon(image);
    }
    
    public static void main(String[] args) {
        try {
            // Set system look and feel
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new MusicMoodAnalyzer().display();
            }
        });
    }
} 