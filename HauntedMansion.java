import java.util.*;
 
/**
 * Escape the Haunted (Silly) Mansion
 * A text adventure: help three embarrassed ghosts, collect their
 * Good Haunting Badges, and talk to Mr. Wobbles to unlock the front door.
 */
public class HauntedMansion {
 
    // ---------- Data ----------
 
    static class Room {
        final String description;
        final Map<String, String> exits = new LinkedHashMap<>();
        final List<String> items = new ArrayList<>();


    Room(String description) {
                this.description = description;
            }
        }
    
        private static final Set<String> FILLER =
                new HashSet<>(Arrays.asList("the", "a", "an", "my", "some", "please"));
        private static final Set<String> LINKERS =
                new HashSet<>(Arrays.asList("to", "at", "with", "on", "in", "into"));
        private static final List<String> GHOST_NAMES =
                Arrays.asList("bella", "harold", "shiverton", "wobbles");
        private static final Set<String> FIXTURES =
                new HashSet<>(Arrays.asList("sink", "piano", "fridge", "refrigerator", "closet", "door", "bookshelves"));
    
        private final Map<String, Room> rooms = new HashMap<>();
        private final List<String> inventory = new ArrayList<>();
        private final Scanner in = new Scanner(System.in);
    
        private String currentRoom = "foyer";
        private boolean running = true;
    
        // Puzzle state
        private boolean cupFilled = false;
        private boolean closetOpen = false;
        private boolean pianoOpen = false;
        private boolean flashlightWorks = false;
        private boolean helpedBella = false;
        private boolean helpedHarold = false;
        private boolean helpedShiverton = false;
    
        // ---------- Setup ----------
    
        private void buildWorld() {
            Room foyer = new Room("The foyer of Grumbleton Mansion. The big front door is locked tight.");
            foyer.exits.put("north", "hallway");
    
            Room hallway = new Room("A long, dim hallway with creaky floorboards. Doors lead everywhere.");
            hallway.exits.put("south", "foyer");
            hallway.exits.put("north", "library");
            hallway.exits.put("east", "ballroom");
            hallway.exits.put("west", "kitchen");
            hallway.exits.put("up", "attic");
            hallway.exits.put("down", "cellar");
    
            Room kitchen = new Room("A messy kitchen. There is a sink, and a refrigerator that hums suspiciously. "
                    + "A back door leads to the garden.");
            kitchen.exits.put("east", "hallway");
            kitchen.exits.put("north", "garden");
            kitchen.items.add("cup");
    
            Room library = new Room("A dusty library. Dust floats everywhere, and the bookshelves are furry with it.");
            library.exits.put("south", "hallway");
    
            Room ballroom = new Room("A grand ballroom with a shiny floor and a big grand piano in the corner.");
            ballroom.exits.put("west", "hallway");
    
            Room attic = new Room("A cramped attic full of old junk: broken toys, a headless mannequin, and a lot of cobwebs.");
            attic.exits.put("down", "hallway");
            attic.items.add("duster");
    
            Room cellar = new Room("A damp stone cellar. Now that you can see, it's mostly wine barrels and one very nervous knight.");
            cellar.exits.put("up", "hallway");
    
            Room garden = new Room("A tiny overgrown garden. A gnome statue stares at you. You are fairly sure it blinked.");
            garden.exits.put("south", "kitchen");
    
            rooms.put("foyer", foyer);
            rooms.put("hallway", hallway);
            rooms.put("kitchen", kitchen);
            rooms.put("library", library);
            rooms.put("ballroom", ballroom);
            rooms.put("attic", attic);
            rooms.put("cellar", cellar);
            rooms.put("garden", garden);
        }
    
        // ---------- Main loop ----------
    
        public void run() {
            buildWorld();
            println("=======================================");
            println("   ESCAPE THE HAUNTED (SILLY) MANSION");
            println("=======================================");
            println("You and your friends dared each other to enter the old Grumbleton Mansion.");
            println("The moment you step inside, the front door SLAMS shut and locks.");
            println("A ghost floats down the stairs and sighs.");
            println("\"Oh dear, not again. I'm Mr. Wobbles, the butler. The door only opens when");
            println("all three house ghosts have a Good Haunting Badge. They're having a rough week.\"");
            println("\n(Type 'help' to see commands.)\n");
            look();
    
            while (running) {
                System.out.print("\n> ");
                if (!in.hasNextLine()) {
                    break;
                }
                handle(in.nextLine());
            }
            println("\nThanks for playing!");
        }
    
        // ---------- Command parsing ----------
    
        private void handle(String line) {
            List<String> words = new ArrayList<>();
            for (String w : line.toLowerCase().trim().split("\\s+")) {
                if (!w.isEmpty() && !FILLER.contains(w)) {
                    words.add(w);
                }
            }
            if (words.isEmpty()) {
                println("Say something! (Type 'help' if you're stuck.)");
                return;
            }
    
            String verb = words.remove(0);
            if (verb.equals("pick") && !words.isEmpty() && words.get(0).equals("up")) {
                words.remove(0); // "pick up key" -> "take key"
            }
            String object = objectPhrase(words);
    
            switch (verb) {
                case "go":
                case "walk":
                case "move":
                    go(object.isEmpty() ? "" : object.split(" ")[0]);
                    break;
                case "look":
                case "l":
                    look();
                    break;
                case "take":
                case "get":
                case "grab":
                case "pick":
                    take(normalize(object));
                    break;
                case "drop":
                    drop(normalize(object));
                    break;
                case "inventory":
                case "inv":
                case "i":
                    showInventory();
                    break;
                case "use":
                    use(words);
                    break;
                case "give":
                    giveCommand(words);
                    break;
                case "talk":
                case "speak":
                case "chat":
                    talk(normalize(object));
                    break;
                case "open":
                    open(normalize(object));
                    break;
                case "fill":
                    fill(normalize(object));
                    break;
                case "eat":
                case "lick":
                    eat(normalize(object));
                    break;
                case "badges":
                    println("You have " + badgeCount() + " of 3 Good Haunting Badges.");
                    break;
                case "hint":
                    hint();
                    break;
                case "help":
                case "?":
                    showHelp();
                    break;
                case "quit":
                case "exit":
                    running = false;
                    break;
                default:
                    String dir = toDirection(verb);
                    if (dir != null) {
                        go(dir);
                    } else {
                        println("You try to \"" + verb + "\", but nothing happens. A ghost somewhere snickers.");
                    }
            }
        }
    

    