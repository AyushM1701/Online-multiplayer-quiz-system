package model;

/**
 * Represents a quiz question with 4 options and a correct answer index.
 * answer is 0-indexed (0 = option1, 1 = option2, etc.)
 */
public class Question {
    private int id;
    private String questionText;
    private String[] options; // always length 4
    private int correctAnswer; // 0-indexed

    public Question() {}

    public Question(int id, String questionText, String[] options, int correctAnswer) {
        if (options == null || options.length != 4)
            throw new IllegalArgumentException("Must have exactly 4 options");
        this.id = id;
        this.questionText = questionText;
        this.options = options;
        this.correctAnswer = correctAnswer;
    }

    // ── Getters ──────────────────────────────────────────────────────────────
    public int getId()            { return id; }
    public String getQuestionText() { return questionText; }
    public String[] getOptions()  { return options; }
    public int getCorrectAnswer() { return correctAnswer; }

    // ── Setters ──────────────────────────────────────────────────────────────
    public void setId(int id)                       { this.id = id; }
    public void setQuestionText(String questionText) { this.questionText = questionText; }
    public void setOptions(String[] options)         { this.options = options; }
    public void setCorrectAnswer(int correctAnswer)  { this.correctAnswer = correctAnswer; }

    @Override
    public String toString() {
        return "Question{id=" + id + ", text='" + questionText + "', answer=" + correctAnswer + "}";
    }
}