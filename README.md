# Music Mood Analyzer

A Java application that analyzes music files and determines their mood based on audio characteristics.

## Overview

This application allows users to:
- Select music files from their computer
- Analyze the mood of the selected songs
- View detailed descriptions of the mood analysis
- See a visual representation of the song's audio features

## Features

- User-friendly GUI built with Java Swing
- Audio mood classification into categories like Happy, Sad, Energetic, etc.
- Visual representation of audio features (tempo, key, timbre, rhythm, energy, vocals)
- Support for common audio formats (MP3, WAV, OGG, FLAC)

## How It Works

The Music Mood Analyzer uses an algorithm that:
1. Analyzes audio features (simulated in this demo version)
2. Compares these features to predefined mood profiles
3. Determines the closest matching mood
4. Provides a detailed description of the mood characteristics

## Project Structure

- `MusicMoodAnalyzer.java`: Main application class that handles the GUI
- `SongAnalyzer.java`: Contains the logic for analyzing songs and determining moods
- `README.md`: This documentation file

## How to Run

1. Make sure you have Java JDK installed (version 8 or higher)
2. Compile the Java files:
   ```
   javac *.java
   ```
3. Run the application:
   ```
   java MusicMoodAnalyzer
   ```

## Usage

1. Launch the application
2. Click "Browse Song" to select an audio file from your computer
3. Click "Analyze Mood" to process the selected song
4. View the detected mood and its description
5. Examine the visual representation of audio features

## Note

This is a demonstration project. In a real-world application, actual audio analysis algorithms would be implemented to extract features from the audio files. The current implementation simulates this process based on filenames.

## Future Enhancements

- Implement actual audio analysis algorithms
- Add playlist management features
- Add music recommendation based on mood
- Build a mood-based music library organization system
- Add visualization of audio waveforms
- Include mood history tracking

## Author - Shrey Salunke 

Created as a college mini-project. 