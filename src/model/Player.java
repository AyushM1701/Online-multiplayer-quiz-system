package model;

/**
 * Represents a connected quiz player with a name and running score.
 */
public class Player {
    private String name;
    private int score;

    public Player() {}

    public Player(String name) {
        if (name == null || name.trim().isEmpty())
            throw new IllegalArgumentException("Player name cannot be empty");
        this.name  = name.trim();
        this.score = 0;
    }

    // ── Score helpers ─────────────────────────────────────────────────────────
    public void incrementScore() { score++; }
    public void resetScore()     { score = 0; }

    // ── Getters / Setters ─────────────────────────────────────────────────────
    public String getName()      { return name; }
    public int    getScore()     { return score; }
    public void   setName(String name)  { this.name = name; }
    public void   setScore(int score)   { this.score = score; }

    @Override
    public String toString() {
        return "Player{name='" + name + "', score=" + score + "}";
    }
}