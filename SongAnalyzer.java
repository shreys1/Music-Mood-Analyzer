import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.ArrayList;
import java.util.List;

/**
 * This class represents the core analysis logic for determining a song's mood.
 * In a real application, this would involve audio processing algorithms, 
 * but for this demo we use simple simulations.
 */
public class SongAnalyzer {
    
    // Features that determine a song's mood
    private static final String[] FEATURES = {
        "tempo", "key", "timbre", "rhythm", "energy", "vocals"
    };
    
    // Musical scales
    private static final String[] SCALES = {
        "C Major", "G Major", "D Major", "A Major", "E Major", "B Major", "F# Major", 
        "C# Major", "F Major", "Bb Major", "Eb Major", "Ab Major", "Db Major", "Gb Major",
        "A Minor", "E Minor", "B Minor", "F# Minor", "C# Minor", "G# Minor", "D# Minor", 
        "A# Minor", "D Minor", "G Minor", "C Minor", "F Minor", "Bb Minor", "Eb Minor"
    };
    
    // Major and minor scales separated for improved detection
    private static final String[] MAJOR_SCALES = {
        "C Major", "G Major", "D Major", "A Major", "E Major", "B Major", "F# Major", 
        "C# Major", "F Major", "Bb Major", "Eb Major", "Ab Major", "Db Major", "Gb Major"
    };
    
    private static final String[] MINOR_SCALES = {
        "A Minor", "E Minor", "B Minor", "F# Minor", "C# Minor", "G# Minor", "D# Minor", 
        "A# Minor", "D Minor", "G Minor", "C Minor", "F Minor", "Bb Minor", "Eb Minor"
    };
    
    // Circle of fifths for scale detection (based on key "brightness")
    private static final String[] CIRCLE_OF_FIFTHS = {
        "F Minor", "C Minor", "G Minor", "D Minor", "A Minor", "E Minor", "B Minor", "F# Minor", 
        "C# Minor", "G# Minor", "D# Minor", "A# Minor", "Eb Minor", "Bb Minor", 
        "Gb Major", "Db Major", "Ab Major", "Eb Major", "Bb Major", "F Major", "C Major", 
        "G Major", "D Major", "A Major", "E Major", "B Major", "F# Major", "C# Major"
    };
    
    // Chord progression templates for different scales (I, IV, V, vi, etc.)
    private static final Map<String, String[]> SCALE_CHORD_FAMILIES = initChordFamilies();
    
    // Sample mood categories and their descriptions
    private static final Map<String, MoodProfile> MOOD_PROFILES = initMoodProfiles();
    
    // Song feature database (simulated)
    private static final Map<String, Map<String, Double>> SONG_FEATURES = initSongFeatures();
    
    /**
     * Analyzes a song file and returns its mood.
     * 
     * @param songFile The song file to analyze
     * @return A SongMood object containing the mood and its detailed description
     */
    public static SongAnalysis analyzeSong(File songFile) {
        if (songFile == null) {
            return new SongAnalysis("Unknown", "No song selected for analysis.", null, null, null);
        }
        
        String songName = songFile.getName().toLowerCase();
        
        // First check if this is a known song in our database
        for (String knownSong : SONG_FEATURES.keySet()) {
            if (songName.contains(knownSong.toLowerCase())) {
                Map<String, Double> features = SONG_FEATURES.get(knownSong);
                return determineFromFeatures(features, songFile);
            }
        }
        
        // If not found, simulate feature extraction and analysis
        // In a real app, this would be actual audio signal processing
        Map<String, Double> extractedFeatures = simulateFeatureExtraction(songFile);
        return determineFromFeatures(extractedFeatures, songFile);
    }
    
    /**
     * Analyzes a song's features to determine its mood, scale, and chord family.
     * 
     * @param features The extracted song features
     * @return The analyzed song information
     */
    private static SongAnalysis determineFromFeatures(Map<String, Double> features, File songFile) {
        // Calculate how closely the song matches each mood profile
        String bestMood = null;
        double bestScore = Double.NEGATIVE_INFINITY;
        
        for (Map.Entry<String, MoodProfile> entry : MOOD_PROFILES.entrySet()) {
            String mood = entry.getKey();
            MoodProfile profile = entry.getValue();
            
            // Simple similarity score calculation
            double score = calculateSimilarityScore(features, profile.getIdealFeatures());
            
            if (score > bestScore) {
                bestScore = score;
                bestMood = mood;
            }
        }
        
        // Determine scale based on key feature using improved detection
        String scale = determineScale(features, bestMood, songFile);
        
        // Get chord family for the determined scale
        String[] chordFamily = SCALE_CHORD_FAMILIES.getOrDefault(scale, 
                               SCALE_CHORD_FAMILIES.get("C Major")); // Default to C Major if not found
        
        // Generate a chord progression based on the scale and mood
        String chordProgression = generateChordProgression(scale, chordFamily, bestMood);
        
        // Return the best matching mood with scale and chord information
        MoodProfile matchedProfile = MOOD_PROFILES.get(bestMood);
        return new SongAnalysis(bestMood, matchedProfile.getDescription(), 
                               scale, chordFamily, chordProgression);
    }
    
    /**
     * Determines the musical scale of a song based on its features, mood, and filename.
     * Uses music theory principles for more accurate scale detection.
     */
    private static String determineScale(Map<String, Double> features, String mood, File songFile) {
        // Key value (0.0-1.0) represents position on the circle of fifths
        // 0.0 = most flat/minor keys, 1.0 = most sharp/major keys
        double keyValue = features.getOrDefault("key", 0.5);
        
        // Check if a scale is directly specified in the song name
        if (songFile != null) {
            String filename = songFile.getName().toLowerCase();
            
            // Check for direct key mentions in the filename
            for (String scale : SCALES) {
                if (filename.contains(scale.toLowerCase())) {
                    return scale; // Use the exact scale mentioned in the filename
                }
            }
            
            // Check for key mentions without specifying major/minor
            String[] keyNames = {"C", "C#", "Db", "D", "D#", "Eb", "E", "F", "F#", 
                               "Gb", "G", "G#", "Ab", "A", "A#", "Bb", "B"};
                               
            for (String keyName : keyNames) {
                // We need to check for the key name with word boundaries to avoid false positives
                // (e.g., "A" in "All" shouldn't match)
                if (filename.matches(".*\\b" + keyName + "\\b.*")) {
                    // If key is found but no major/minor specified, determine based on mood and key value
                    boolean isMajorKey = keyValue > 0.5;
                    
                    // Adjust based on mood
                    if (mood.equals("Happy") || mood.equals("Energetic")) {
                        isMajorKey = true;
                    } else if (mood.equals("Sad") || mood.equals("Angry")) {
                        isMajorKey = false;
                    }
                    
                    // Return the appropriate scale
                    return keyName + (isMajorKey ? " Major" : " Minor");
                }
            }
        }
        
        // Different moods tend to be associated with different scales
        // Adjust the key value based on the mood
        switch (mood) {
            case "Happy":
                keyValue = Math.min(1.0, keyValue + 0.2); // Bias toward major keys
                break;
            case "Sad":
                keyValue = Math.max(0.0, keyValue - 0.2); // Bias toward minor keys
                break;
            case "Energetic":
                keyValue = Math.min(1.0, keyValue + 0.1); // Slight bias toward major keys
                break;
            case "Calm":
                // No adjustment, can be either major or minor
                break;
            case "Angry":
                keyValue = Math.max(0.0, keyValue - 0.15); // Bias toward minor keys
                break;
            case "Romantic":
                // Can be major or minor depending on the type of romance
                break;
            case "Nostalgic":
                // No strong bias for nostalgic music
                break;
        }
        
        // Use the key value to select a position on the circle of fifths
        // This creates a music-theory-informed selection of key
        int scaleIndex = (int)(keyValue * (CIRCLE_OF_FIFTHS.length - 1));
        return CIRCLE_OF_FIFTHS[scaleIndex];
    }
    
    /**
     * Maps a chord name to its constituent notes.
     * Properly handles major, minor, and diminished chords including flat notes.
     * 
     * @param chord The chord name (e.g., "C", "Dm", "G7", "Bdim")
     * @return String representing the notes in the chord
     */
    public static String getChordNotes(String chord) {
        // Major chords
        Map<String, String> chordNotes = new HashMap<>();
        
        // Major chords
        chordNotes.put("C", "C  E  G");
        chordNotes.put("C#", "C#  F  G#");
        chordNotes.put("Db", "Db  F  Ab");
        chordNotes.put("D", "D  F#  A");
        chordNotes.put("D#", "D#  G  A#");
        chordNotes.put("Eb", "Eb  G  Bb");
        chordNotes.put("E", "E  G#  B");
        chordNotes.put("F", "F  A  C");
        chordNotes.put("F#", "F#  A#  C#");
        chordNotes.put("Gb", "Gb  Bb  Db");
        chordNotes.put("G", "G  B  D");
        chordNotes.put("G#", "G#  C  D#");
        chordNotes.put("Ab", "Ab  C  Eb");
        chordNotes.put("A", "A  C#  E");
        chordNotes.put("A#", "A#  D  F");
        chordNotes.put("Bb", "Bb  D  F");
        chordNotes.put("B", "B  D#  F#");
        chordNotes.put("Cb", "B  D#  F#"); // Cb = B
        
        // Minor chords
        chordNotes.put("Cm", "C  Eb  G");
        chordNotes.put("C#m", "C#  E  G#");
        chordNotes.put("Dbm", "Db  E  Ab");
        chordNotes.put("Dm", "D  F  A");
        chordNotes.put("D#m", "D#  F#  A#");
        chordNotes.put("Ebm", "Eb  Gb  Bb");
        chordNotes.put("Em", "E  G  B");
        chordNotes.put("Fm", "F  Ab  C");
        chordNotes.put("F#m", "F#  A  C#");
        chordNotes.put("Gbm", "Gb  Bbb  Db"); // Bbb = A
        chordNotes.put("Gm", "G  Bb  D");
        chordNotes.put("G#m", "G#  B  D#");
        chordNotes.put("Abm", "Ab  Cb  Eb"); // Cb = B
        chordNotes.put("Am", "A  C  E");
        chordNotes.put("A#m", "A#  C#  E#"); // E# = F
        chordNotes.put("Bbm", "Bb  Db  F");
        chordNotes.put("Bm", "B  D  F#");
        chordNotes.put("Cbm", "B  D  F#"); // Cb = B
        
        // Diminished chords
        chordNotes.put("Cdim", "C  Eb  Gb");
        chordNotes.put("C#dim", "C#  E  G");
        chordNotes.put("Dbdim", "Db  Fb  Abb"); // Fb = E, Abb = G
        chordNotes.put("Ddim", "D  F  Ab");
        chordNotes.put("D#dim", "D#  F#  A");
        chordNotes.put("Ebdim", "Eb  Gb  Bbb"); // Bbb = A
        chordNotes.put("Edim", "E  G  Bb");
        chordNotes.put("Fdim", "F  Ab  Cb"); // Cb = B
        chordNotes.put("F#dim", "F#  A  C");
        chordNotes.put("Gbdim", "Gb  Bbb  Dbb"); // Bbb = A, Dbb = C
        chordNotes.put("Gdim", "G  Bb  Db");
        chordNotes.put("G#dim", "G#  B  D");
        chordNotes.put("Abdim", "Ab  Cb  Ebb"); // Cb = B, Ebb = D
        chordNotes.put("Adim", "A  C  Eb");
        chordNotes.put("A#dim", "A#  C#  E");
        chordNotes.put("Bbdim", "Bb  Db  Fb"); // Fb = E
        chordNotes.put("Bdim", "B  D  F");
        chordNotes.put("Cbdim", "Cb  Ebb  Gbb"); // Cb = B, Ebb = D, Gbb = F
        
        // Handle the case where we're being passed a chord with digits (e.g. C7)
        String chordRoot = chord;
        if (chord.matches(".*\\d.*")) {
            chordRoot = chord.replaceAll("\\d", "");
        }
        
        // Check if we have this chord in our map
        if (chordNotes.containsKey(chordRoot)) {
            return chordNotes.get(chordRoot);
        }
        
        // If we don't have a direct mapping, try to use music theory principles
        // Extract the root note and quality
        String rootNote = "";
        String quality = "";
        
        if (chordRoot.endsWith("m")) {
            rootNote = chordRoot.substring(0, chordRoot.length() - 1);
            quality = "m";
        } else if (chordRoot.endsWith("dim")) {
            rootNote = chordRoot.substring(0, chordRoot.length() - 3);
            quality = "dim";
        } else {
            rootNote = chordRoot;
            quality = "";  // Major
        }
        
        // Attempt to look up the chord
        String lookupKey = rootNote + quality;
        if (chordNotes.containsKey(lookupKey)) {
            return chordNotes.get(lookupKey);
        }
        
        // If we still can't find it, return a reasonable fallback
        return chord + " (notes unknown)";
    }
    
    /**
     * Generates a believable chord progression based on the scale, chord family, and mood.
     * Uses common chord progressions in music theory.
     */
    private static String generateChordProgression(String scale, String[] chordFamily, String mood) {
        // Common chord progression patterns (using Roman numeral notation)
        // Different moods often use different chord progressions
        String[] pattern;
        
        // Select a chord progression that fits the mood
        switch (mood) {
            case "Happy":
                // Happy progressions often use I-IV-V or I-V-vi-IV
                pattern = new String[]{"I", "V", "vi", "IV"};
                break;
            case "Sad":
                // Sad progressions often use minor keys with i-VI-III-VII or i-iv-v
                pattern = scale.contains("Minor") ? 
                    new String[]{"i", "VI", "III", "VII"} : 
                    new String[]{"vi", "IV", "I", "V"};
                break;
            case "Energetic":
                // Energetic songs often use strong progressions like I-V-vi-IV or IV-I-V-vi
                pattern = new String[]{"IV", "I", "V", "vi"};
                break;
            case "Calm":
                // Calm songs might use gentler progressions like I-vi-IV-V
                pattern = new String[]{"I", "vi", "IV", "V"};
                break;
            case "Angry":
                // Angry songs often use minor progressions or power chord movements
                pattern = scale.contains("Minor") ?
                    new String[]{"i", "VII", "VI", "v"} :
                    new String[]{"vi", "V", "IV", "I"};
                break;
            case "Romantic":
                // Romantic songs often use emotional progressions
                pattern = new String[]{"I", "vi", "ii", "V"};
                break;
            case "Nostalgic":
                // Nostalgic songs might use classic progressions like I-vi-IV-V (50s progression)
                pattern = new String[]{"I", "vi", "IV", "V"};
                break;
            default:
                // Default to a common progression
                pattern = new String[]{"I", "IV", "V", "I"};
        }
        
        // Map Roman numerals to actual chords in the scale
        StringBuilder progression = new StringBuilder();
        for (int i = 0; i < pattern.length; i++) {
            String numeral = pattern[i];
            String chord = mapNumeralToChord(numeral, chordFamily, scale);
            
            progression.append(chord);
            if (i < pattern.length - 1) {
                progression.append(" → ");
            }
        }
        
        return progression.toString();
    }
    
    /**
     * Maps a Roman numeral to the corresponding chord in a chord family.
     * Now supports both major and minor scales properly.
     */
    private static String mapNumeralToChord(String numeral, String[] chordFamily, String scale) {
        boolean isMinorScale = scale.contains("Minor");
        
        switch (numeral) {
            // Major scale degree mapping
            case "I": return chordFamily[0];
            case "ii": return chordFamily[1];
            case "iii": return chordFamily[2];
            case "IV": return chordFamily[3];
            case "V": return chordFamily[4];
            case "vi": return chordFamily[5];
            case "vii°": return chordFamily[6];
            
            // Minor scale degree mapping (different numerals)
            case "i": return isMinorScale ? chordFamily[0] : chordFamily[5];
            case "III": return isMinorScale ? chordFamily[2] : chordFamily[0];
            case "iv": return isMinorScale ? chordFamily[3] : chordFamily[1];
            case "v": return isMinorScale ? chordFamily[4] : chordFamily[2];
            case "VI": return isMinorScale ? chordFamily[5] : chordFamily[3];
            case "VII": return isMinorScale ? chordFamily[6] : chordFamily[4];
            
            default: return chordFamily[0];
        }
    }
    
    /**
     * Calculates how similar a song's features are to an ideal mood profile.
     * 
     * @param songFeatures The song's features
     * @param idealFeatures The ideal features for a mood
     * @return A similarity score
     */
    private static double calculateSimilarityScore(Map<String, Double> songFeatures, 
                                                 Map<String, Double> idealFeatures) {
        double score = 0.0;
        
        for (String feature : FEATURES) {
            if (songFeatures.containsKey(feature) && idealFeatures.containsKey(feature)) {
                // Calculate the distance (lower is better)
                double distance = Math.abs(songFeatures.get(feature) - idealFeatures.get(feature));
                // Convert to similarity (higher is better)
                score += (1.0 - (distance / 1.0));
            }
        }
        
        return score;
    }
    
    /**
     * Simulates feature extraction from an audio file.
     * In a real application, this would be actual audio analysis.
     */
    private static Map<String, Double> simulateFeatureExtraction(File songFile) {
        Map<String, Double> features = new HashMap<>();
        Random random = new Random(songFile.getName().hashCode()); // Use filename as seed for consistent results
        
        // Extract meaningful information from filename if possible
        String name = songFile.getName().toLowerCase();
        
        // Default feature values
        double tempo = 0.1 + 0.9 * random.nextDouble();  // 0.1-1.0 (slow to fast)
        double key = 0.3 + 0.7 * random.nextDouble();    // 0.3-1.0 (minor to major)
        double timbre = random.nextDouble();             // 0.0-1.0 (dark to bright)
        double rhythm = 0.2 + 0.8 * random.nextDouble(); // 0.2-1.0 (flowing to percussive)
        double energy = random.nextDouble();             // 0.0-1.0 (calm to energetic)
        double vocals = random.nextDouble();             // 0.0-1.0 (soft to powerful)
        
        // Try to extract more accurate values from filename
        // This simulates real audio analysis by using filename hints
        
        // Tempo detection
        if (name.contains("fast") || name.contains("energetic") || name.contains("dance")) {
            tempo = 0.7 + 0.3 * random.nextDouble(); // Fast tempo
        } else if (name.contains("slow") || name.contains("ballad") || name.contains("calm")) {
            tempo = 0.1 + 0.3 * random.nextDouble(); // Slow tempo
        }
        
        // Key detection (major/minor)
        if (name.contains("happy") || name.contains("major") || name.contains("bright")) {
            key = 0.7 + 0.3 * random.nextDouble(); // Major key
        } else if (name.contains("sad") || name.contains("minor") || name.contains("melancholy")) {
            key = 0.1 + 0.3 * random.nextDouble(); // Minor key
        }
        
        // Timbre detection
        if (name.contains("bright") || name.contains("clear") || name.contains("sharp")) {
            timbre = 0.7 + 0.3 * random.nextDouble(); // Bright timbre
        } else if (name.contains("dark") || name.contains("deep") || name.contains("mellow")) {
            timbre = 0.1 + 0.3 * random.nextDouble(); // Dark timbre
        }
        
        // Rhythm detection
        if (name.contains("beat") || name.contains("rhythm") || name.contains("percussion")) {
            rhythm = 0.7 + 0.3 * random.nextDouble(); // Percussive
        } else if (name.contains("flow") || name.contains("smooth") || name.contains("gentle")) {
            rhythm = 0.1 + 0.3 * random.nextDouble(); // Flowing
        }
        
        // Energy detection
        if (name.contains("energetic") || name.contains("powerful") || name.contains("intense")) {
            energy = 0.7 + 0.3 * random.nextDouble(); // High energy
        } else if (name.contains("calm") || name.contains("soft") || name.contains("gentle")) {
            energy = 0.1 + 0.3 * random.nextDouble(); // Low energy
        }
        
        // Vocals detection
        if (name.contains("vocal") || name.contains("voice") || name.contains("sing")) {
            vocals = 0.5 + 0.5 * random.nextDouble(); // Prominent vocals
        } else if (name.contains("instrumental") || name.contains("piano") || name.contains("guitar")) {
            vocals = 0.0 + 0.3 * random.nextDouble(); // Few or no vocals
        }
        
        // Assign the calculated features
        features.put("tempo", tempo);
        features.put("key", key);
        features.put("timbre", timbre);
        features.put("rhythm", rhythm);
        features.put("energy", energy);
        features.put("vocals", vocals);
        
        return features;
    }
    
    /**
     * Initialize the chord families for different scales.
     */
    private static Map<String, String[]> initChordFamilies() {
        Map<String, String[]> chordFamilies = new HashMap<>();
        
        // Major scales and their chord families (I, ii, iii, IV, V, vi, vii°)
        chordFamilies.put("C Major", new String[]{"C", "Dm", "Em", "F", "G", "Am", "Bdim"});
        chordFamilies.put("G Major", new String[]{"G", "Am", "Bm", "C", "D", "Em", "F#dim"});
        chordFamilies.put("D Major", new String[]{"D", "Em", "F#m", "G", "A", "Bm", "C#dim"});
        chordFamilies.put("A Major", new String[]{"A", "Bm", "C#m", "D", "E", "F#m", "G#dim"});
        chordFamilies.put("E Major", new String[]{"E", "F#m", "G#m", "A", "B", "C#m", "D#dim"});
        chordFamilies.put("B Major", new String[]{"B", "C#m", "D#m", "E", "F#", "G#m", "A#dim"});
        chordFamilies.put("F# Major", new String[]{"F#", "G#m", "A#m", "B", "C#", "D#m", "E#dim"});
        chordFamilies.put("C# Major", new String[]{"C#", "D#m", "E#m", "F#", "G#", "A#m", "B#dim"});
        chordFamilies.put("F Major", new String[]{"F", "Gm", "Am", "Bb", "C", "Dm", "Edim"});
        chordFamilies.put("Bb Major", new String[]{"Bb", "Cm", "Dm", "Eb", "F", "Gm", "Adim"});
        chordFamilies.put("Eb Major", new String[]{"Eb", "Fm", "Gm", "Ab", "Bb", "Cm", "Ddim"});
        chordFamilies.put("Ab Major", new String[]{"Ab", "Bbm", "Cm", "Db", "Eb", "Fm", "Gdim"});
        chordFamilies.put("Db Major", new String[]{"Db", "Ebm", "Fm", "Gb", "Ab", "Bbm", "Cdim"});
        chordFamilies.put("Gb Major", new String[]{"Gb", "Abm", "Bbm", "Cb", "Db", "Ebm", "Fdim"});
        
        // Minor scales and their chord families (i, ii°, III, iv, v, VI, VII)
        // For proper harmonic/natural minor treatment
        chordFamilies.put("A Minor", new String[]{"Am", "Bdim", "C", "Dm", "Em", "F", "G"});
        chordFamilies.put("E Minor", new String[]{"Em", "F#dim", "G", "Am", "Bm", "C", "D"});
        chordFamilies.put("B Minor", new String[]{"Bm", "C#dim", "D", "Em", "F#m", "G", "A"});
        chordFamilies.put("F# Minor", new String[]{"F#m", "G#dim", "A", "Bm", "C#m", "D", "E"});
        chordFamilies.put("C# Minor", new String[]{"C#m", "D#dim", "E", "F#m", "G#m", "A", "B"});
        chordFamilies.put("G# Minor", new String[]{"G#m", "A#dim", "B", "C#m", "D#m", "E", "F#"});
        chordFamilies.put("D# Minor", new String[]{"D#m", "E#dim", "F#", "G#m", "A#m", "B", "C#"});
        chordFamilies.put("A# Minor", new String[]{"A#m", "B#dim", "C#", "D#m", "E#m", "F#", "G#"});
        chordFamilies.put("D Minor", new String[]{"Dm", "Edim", "F", "Gm", "Am", "Bb", "C"});
        chordFamilies.put("G Minor", new String[]{"Gm", "Adim", "Bb", "Cm", "Dm", "Eb", "F"});
        chordFamilies.put("C Minor", new String[]{"Cm", "Ddim", "Eb", "Fm", "Gm", "Ab", "Bb"});
        chordFamilies.put("F Minor", new String[]{"Fm", "Gdim", "Ab", "Bbm", "Cm", "Db", "Eb"});
        chordFamilies.put("Bb Minor", new String[]{"Bbm", "Cdim", "Db", "Ebm", "Fm", "Gb", "Ab"});
        chordFamilies.put("Eb Minor", new String[]{"Ebm", "Fdim", "Gb", "Abm", "Bbm", "Cb", "Db"});
        
        return chordFamilies;
    }
    
    /**
     * Initialize the mood profiles with ideal feature values and descriptions.
     */
    private static Map<String, MoodProfile> initMoodProfiles() {
        Map<String, MoodProfile> profiles = new HashMap<>();
        
        // Happy mood
        Map<String, Double> happyFeatures = new HashMap<>();
        happyFeatures.put("tempo", 0.8);      // Fast tempo
        happyFeatures.put("key", 0.9);        // Major key
        happyFeatures.put("timbre", 0.8);     // Bright timbre
        happyFeatures.put("rhythm", 0.7);     // Bouncy rhythm
        happyFeatures.put("energy", 0.7);     // Energetic
        happyFeatures.put("vocals", 0.6);     // Clear, bright vocals
        
        profiles.put("Happy", new MoodProfile(
            happyFeatures,
            "This song has upbeat tempo, major keys, and bright tonality. It likely features positive lyrics and may make listeners feel joyful and uplifted."
        ));
        
        // Sad mood
        Map<String, Double> sadFeatures = new HashMap<>();
        sadFeatures.put("tempo", 0.3);        // Slow tempo
        sadFeatures.put("key", 0.3);          // Minor key
        sadFeatures.put("timbre", 0.3);       // Dark timbre
        sadFeatures.put("rhythm", 0.4);       // Flowing, less percussive
        sadFeatures.put("energy", 0.3);       // Low energy
        sadFeatures.put("vocals", 0.5);       // Emotional vocals
        
        profiles.put("Sad", new MoodProfile(
            sadFeatures,
            "This song has slower tempo, minor keys, and melancholic melodies. It may feature emotional lyrics about loss or heartbreak."
        ));
        
        // Energetic mood
        Map<String, Double> energeticFeatures = new HashMap<>();
        energeticFeatures.put("tempo", 0.9);      // Very fast tempo
        energeticFeatures.put("key", 0.6);        // Can be major or minor
        energeticFeatures.put("timbre", 0.7);     // Bright timbre
        energeticFeatures.put("rhythm", 0.9);     // Strong rhythmic elements
        energeticFeatures.put("energy", 0.9);     // High energy
        energeticFeatures.put("vocals", 0.8);     // Powerful vocals
        
        profiles.put("Energetic", new MoodProfile(
            energeticFeatures,
            "This song has fast tempo, strong beats, and dynamic range. It's designed to pump up listeners and may be good for workouts or dancing."
        ));
        
        // Calm mood
        Map<String, Double> calmFeatures = new HashMap<>();
        calmFeatures.put("tempo", 0.3);       // Slow tempo
        calmFeatures.put("key", 0.6);         // Can be major or minor
        calmFeatures.put("timbre", 0.5);      // Warm timbre
        calmFeatures.put("rhythm", 0.3);      // Smooth rhythm
        calmFeatures.put("energy", 0.2);      // Low energy
        calmFeatures.put("vocals", 0.3);      // Soft vocals
        
        profiles.put("Calm", new MoodProfile(
            calmFeatures,
            "This song has moderate to slow tempo, smooth transitions, and gentle instrumentation. It promotes relaxation and peaceful feelings."
        ));
        
        // Angry mood
        Map<String, Double> angryFeatures = new HashMap<>();
        angryFeatures.put("tempo", 0.7);      // Fast tempo
        angryFeatures.put("key", 0.3);        // Often minor key
        angryFeatures.put("timbre", 0.2);     // Dark/harsh timbre
        angryFeatures.put("rhythm", 0.8);     // Strong rhythmic elements
        angryFeatures.put("energy", 0.9);     // High energy
        angryFeatures.put("vocals", 0.9);     // Intense vocals
        
        profiles.put("Angry", new MoodProfile(
            angryFeatures,
            "This song has aggressive tones, may include distorted guitars, intense vocals, and powerful drums. It expresses frustration or defiance."
        ));
        
        // Romantic mood
        Map<String, Double> romanticFeatures = new HashMap<>();
        romanticFeatures.put("tempo", 0.5);   // Medium tempo
        romanticFeatures.put("key", 0.7);     // Often major key
        romanticFeatures.put("timbre", 0.6);  // Warm timbre
        romanticFeatures.put("rhythm", 0.5);  // Flowing rhythm
        romanticFeatures.put("energy", 0.5);  // Medium energy
        romanticFeatures.put("vocals", 0.6);  // Emotional vocals
        
        profiles.put("Romantic", new MoodProfile(
            romanticFeatures,
            "This song has flowing melodies, emotional delivery, and intimate sound. It often discusses themes of love and connection."
        ));
        
        // Nostalgic mood
        Map<String, Double> nostalgicFeatures = new HashMap<>();
        nostalgicFeatures.put("tempo", 0.4);  // Slower tempo
        nostalgicFeatures.put("key", 0.5);    // Can be major or minor
        nostalgicFeatures.put("timbre", 0.4); // Warmer, sometimes filtered timbre
        nostalgicFeatures.put("rhythm", 0.4); // Flowing rhythm
        nostalgicFeatures.put("energy", 0.4); // Medium-low energy
        nostalgicFeatures.put("vocals", 0.5); // Emotional vocals
        
        profiles.put("Nostalgic", new MoodProfile(
            nostalgicFeatures,
            "This song has elements that evoke memory and reflection. It may use vintage sounds or references to create a sense of looking back."
        ));
        
        return profiles;
    }
    
    /**
     * Initialize a sample database of songs and their pre-extracted features.
     * In a real application, this would come from a database or be computed.
     */
    private static Map<String, Map<String, Double>> initSongFeatures() {
        Map<String, Map<String, Double>> songFeatures = new HashMap<>();
        
        // Happy example song
        Map<String, Double> happySongFeatures = new HashMap<>();
        happySongFeatures.put("tempo", 0.85);
        happySongFeatures.put("key", 0.95);
        happySongFeatures.put("timbre", 0.8);
        happySongFeatures.put("rhythm", 0.75);
        happySongFeatures.put("energy", 0.8);
        happySongFeatures.put("vocals", 0.7);
        songFeatures.put("happy", happySongFeatures);
        
        // Sad example song
        Map<String, Double> sadSongFeatures = new HashMap<>();
        sadSongFeatures.put("tempo", 0.25);
        sadSongFeatures.put("key", 0.2);
        sadSongFeatures.put("timbre", 0.3);
        sadSongFeatures.put("rhythm", 0.3);
        sadSongFeatures.put("energy", 0.2);
        sadSongFeatures.put("vocals", 0.6);
        songFeatures.put("sad", sadSongFeatures);
        
        // More example songs...
        Map<String, Double> energeticSongFeatures = new HashMap<>();
        energeticSongFeatures.put("tempo", 0.95);
        energeticSongFeatures.put("key", 0.7);
        energeticSongFeatures.put("timbre", 0.8);
        energeticSongFeatures.put("rhythm", 0.9);
        energeticSongFeatures.put("energy", 0.95);
        energeticSongFeatures.put("vocals", 0.8);
        songFeatures.put("energetic", energeticSongFeatures);
        
        return songFeatures;
    }
    
    /**
     * Inner class to represent a mood profile with ideal feature values and description.
     */
    private static class MoodProfile {
        private final Map<String, Double> idealFeatures;
        private final String description;
        
        public MoodProfile(Map<String, Double> idealFeatures, String description) {
            this.idealFeatures = idealFeatures;
            this.description = description;
        }
        
        public Map<String, Double> getIdealFeatures() {
            return idealFeatures;
        }
        
        public String getDescription() {
            return description;
        }
    }
}

/**
 * Class to represent the result of a song analysis including mood, scale, and chord family.
 */
class SongAnalysis {
    private final String mood;
    private final String description;
    private final String scale;
    private final String[] chordFamily;
    private final String chordProgression;
    
    public SongAnalysis(String mood, String description, String scale, 
                        String[] chordFamily, String chordProgression) {
        this.mood = mood;
        this.description = description;
        this.scale = scale;
        this.chordFamily = chordFamily;
        this.chordProgression = chordProgression;
    }
    
    public String getMood() {
        return mood;
    }
    
    public String getDescription() {
        return description;
    }
    
    public String getScale() {
        return scale;
    }
    
    public String[] getChordFamily() {
        return chordFamily;
    }
    
    public String getChordProgression() {
        return chordProgression;
    }
    
    public String getChordFamilyAsString() {
        if (chordFamily == null) {
            return "";
        }
        
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < chordFamily.length; i++) {
            String chord = chordFamily[i];
            sb.append(chord);
            
            // Append the notes for each chord
            String notes = SongAnalyzer.getChordNotes(chord);
            if (!notes.isEmpty()) {
                sb.append(" (").append(notes).append(")");
            }
            
            if (i < chordFamily.length - 1) {
                sb.append("\n");
            }
        }
        return sb.toString();
    }
} 