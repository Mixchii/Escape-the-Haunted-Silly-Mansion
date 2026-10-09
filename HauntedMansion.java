import java.util.*;
 
/**
 * Escape the Haunted (Silly) Mansion
 * A text adventure: help three embarrassed ghosts, collect their
 * Good Haunting Badges, and talk to Mr. Wobbles to unlock the front door.
 *
 * Run from VS Code: open this file and press the Run button,
 * or in the terminal:  javac HauntedMansion.java  then  java HauntedMansion
 */
public class HauntedMansion {
 
    // ---------- Data ----------
 
    static class Room {
        final String description;
        final Map<String, String> exits = new LinkedHashMap<>();
        final List<String> items = new ArrayList<>();

 