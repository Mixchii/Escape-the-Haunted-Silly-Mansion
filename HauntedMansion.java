import java.util.*;
 
/**
 * Escape the Haunted (Silly) Mansion
 * A text adventure: help three embarrassed ghosts, collect their
 * Good Haunting Badges, and talk to Mr. Wobbles to unlock the front door.
 *

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
 
    /** Joins the words after the verb, dropping little linking words like "to" and "at". */
    private String objectPhrase(List<String> words) {
        List<String> kept = new ArrayList<>();
        for (String w : words) {
            if (!LINKERS.contains(w)) {
                kept.add(w);
            }
        }
        return String.join(" ", kept);
    }
 
    /** Turns phrases like "feather duster" or "hiccup harold" into one simple key. */
    private String normalize(String phrase) {
        phrase = phrase.trim();
        if (phrase.isEmpty()) {
            return "";
        }
        if (phrase.contains("achoo") || phrase.contains("bella")) {
            return "bella";
        }
        String[] parts = phrase.split(" ");
        String last = parts[parts.length - 1];
        if (last.equals("refrigerator")) {
            return "fridge";
        }
        if (last.equals("batteries") || last.equals("battery")) {
            return "batteries";
        }
        return last;
    }
 
    private String toDirection(String w) {
        switch (w) {
            case "n": case "north": return "north";
            case "s": case "south": return "south";
            case "e": case "east": return "east";
            case "w": case "west": return "west";
            case "u": case "up": return "up";
            case "d": case "down": return "down";
            default: return null;
        }
    }
 
    // ---------- Looking around ----------
 
    private boolean hasLight() {
        return flashlightWorks && inventory.contains("flashlight");
    }
 
    private void look() {
        Room room = rooms.get(currentRoom);
 
        if (currentRoom.equals("cellar") && !hasLight()) {
            println("It's too dark to see! Something is whimpering in the corner.");
            println("Exits: up");
            return;
        }
 
        println(room.description);
 
        switch (currentRoom) {
            case "foyer":
                println("Mr. Wobbles the butler hovers by the door, polishing a teaspoon that is already shiny.");
                break;
            case "hallway":
                if (!closetOpen) {
                    println("There is a small closet door in the wall.");
                }
                break;
            case "library":
                if (helpedBella) {
                    println("Achoo-bella floats happily between the sparkling-clean shelves.");
                } else {
                    println("Achoo-bella the ghost is sniffling, eyes watering, trying very hard not to sneeze.");
                }
                break;
            case "ballroom":
                if (helpedHarold) {
                    println("Hiccup Harold waltzes around with a big hiccup-free grin.");
                } else {
                    println("Hiccup Harold the ghost hovers here. HIC! Everything he holds goes flying.");
                }
                if (pianoOpen) {
                    println("The piano lid is open.");
                } else {
                    println("The piano lid is shut.");
                }
                break;
            case "cellar":
                if (helpedShiverton) {
                    println("Sir Shiverton stands tall, proudly holding his sword, and not even a little scared.");
                } else {
                    println("Sir Shiverton, a ghost knight, is hiding behind a barrel with his helmet over his eyes.");
                }
                break;
            default:
                break;
        }
 
        if (!room.items.isEmpty()) {
            println("You see: " + String.join(", ", room.items));
        }
        println("Exits: " + String.join(", ", room.exits.keySet()));
    }
 
    // ---------- Movement ----------
 
    private void go(String direction) {
        if (direction.isEmpty()) {
            println("Go where? (north, south, east, west, up, or down)");
            return;
        }
        String dir = toDirection(direction);
        if (dir == null) {
            println("\"" + direction + "\" isn't a direction. Try north, south, east, west, up, or down.");
            return;
        }
 
        Room room = rooms.get(currentRoom);
        String destination = room.exits.get(dir);
        if (destination == null) {
            println("You walk into the wall. The wall says \"ow.\"");
            return;
        }
 
        // Achoo-bella's sneeze blocks the library until she is helped
        if (destination.equals("library") && !helpedBella) {
            if (inventory.contains("duster")) {
                println("Achoo-bella winds up for a giant sneeze, but you hold up the feather duster.");
                println("She pauses. \"Is that... for me?\" (Try: use duster)");
            } else {
                println("You step into the library. Achoo-bella takes a deep breath...");
                println("A-A-A-ACHOOOOO! You are blown all the way back to the hallway!");
                println("Maybe something could clean the dust?");
                currentRoom = "hallway";
                look();
                return;
            }
        }
 
        currentRoom = destination;
        look();
    }
 
    // ---------- Items ----------
 
    private void take(String noun) {
        if (noun.isEmpty()) {
            println("Take what?");
            return;
        }
        if (GHOST_NAMES.contains(noun) || noun.equals("ghost")) {
            takeGhost(noun);
            return;
        }
        if (FIXTURES.contains(noun)) {
            println("You can't carry that. It's much too big, and it's attached to the house.");
            return;
        }
        Room room = rooms.get(currentRoom);
        if (room.items.remove(noun)) {
            inventory.add(noun);
            println("Taken: " + noun + ".");
        } else if (inventory.contains(noun)) {
            println("You already have the " + noun + ".");
        } else {
            println("There's no " + noun + " here to take.");
        }
    }
 
    private void takeGhost(String noun) {
        String g = resolveGhost(noun);
        if (g == null) {
            println("There's no ghost here to pick up.");
            return;
        }
        switch (g) {
            case "bella":
                println("You try to pick up Achoo-bella. Your hands go right through her. She giggles. It tickles.");
                break;
            case "harold":
                println("You try to pick up Hiccup Harold. HIC! He hiccups and you both fall over. Neither of you is hurt.");
                break;
            case "shiverton":
                println("You grab for Sir Shiverton. He yelps, \"Wh-what was that?!\" and hides behind a barrel.");
                break;
            default:
                println("Mr. Wobbles raises an eyebrow. \"I'm a butler, not luggage.\"");
        }
    }
 
    private void drop(String noun) {
        if (noun.isEmpty()) {
            println("Drop what?");
            return;
        }
        if (inventory.remove(noun)) {
            rooms.get(currentRoom).items.add(noun);
            println("Dropped: " + noun + ".");
        } else {
            println("You aren't carrying a " + noun + ".");
        }
    }
 
    private void showInventory() {
        if (inventory.isEmpty()) {
            println("You aren't carrying anything. Your pockets contain lint and bravery.");
            return;
        }
        List<String> shown = new ArrayList<>();
        for (String item : inventory) {
            if (item.equals("cup")) {
                shown.add(cupFilled ? "cup (full of water)" : "cup (empty)");
            } else if (item.equals("flashlight")) {
                shown.add(flashlightWorks ? "flashlight (working!)" : "flashlight (no batteries)");
            } else {
                shown.add(item);
            }
        }
        println("You are carrying: " + String.join(", ", shown));
        println("Badges earned: " + badgeCount() + " of 3");
    }
 
    // ---------- Actions ----------
 
    private void open(String noun) {
        switch (noun) {
            case "closet":
                if (!currentRoom.equals("hallway")) {
                    println("There's no closet here.");
                } else if (closetOpen) {
                    println("The closet is already open and empty. A moth waves hello.");
                } else {
                    closetOpen = true;
                    rooms.get("hallway").items.add("flashlight");
                    println("You open the closet. Inside, on a hook, is a flashlight!");
                }
                break;
            case "piano":
                if (!currentRoom.equals("ballroom")) {
                    println("There's no piano here.");
                } else if (!helpedHarold) {
                    println("You try to lift the piano lid. HIC! Harold hiccups so hard the lid slams shut again.");
                    println("Maybe he needs help first.");
                } else if (pianoOpen) {
                    println("The piano is already open.");
                } else {
                    pianoOpen = true;
                    rooms.get("ballroom").items.add("batteries");
                    println("The lid lifts easily. Inside, tucked among the strings, are some batteries!");
                }
                break;
            case "fridge":
                if (!currentRoom.equals("kitchen")) {
                    println("There's no refrigerator here.");
                } else {
                    println("The refrigerator says: \"Please stop letting the cold out.\"");
                }
                break;
            case "door":
                if (currentRoom.equals("foyer")) {
                    println("The front door is locked tight. Mr. Wobbles coughs politely. \"Badges, please.\"");
                } else {
                    println("The doors in this house are all open already. Mostly.");
                }
                break;
            case "":
                println("Open what?");
                break;
            default:
                println("You can't open that.");
        }
    }