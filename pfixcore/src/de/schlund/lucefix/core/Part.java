package de.schlund.lucefix.core;


public class Part {
    String filename;
    String name;

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public Part(String filename, String name) {
        this.filename = filename;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}