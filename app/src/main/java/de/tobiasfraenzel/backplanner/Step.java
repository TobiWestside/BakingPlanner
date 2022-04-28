package de.tobiasfraenzel.backplanner;

public class Step {
    private String name;
    private int duration;
    private long id;

    public Step() {
        name = "";
        duration = 0;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }
}
