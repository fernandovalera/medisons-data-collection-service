package com.medisons.dbm;

public class SignalScoreRowListItem {

    private final String name;
    private final SignalScoreRow score;

    public SignalScoreRowListItem(String name, SignalScoreRow score) {
        this.name = name;
        this.score = score;
    }

    public String getName() {
        return name;
    }

    public SignalScoreRow getScore() {
        return score;
    }
}
