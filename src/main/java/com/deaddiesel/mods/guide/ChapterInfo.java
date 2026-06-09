package com.deaddiesel.mods.guide;

public record ChapterInfo(String file, String name, boolean isSub) {

    public ChapterInfo {
        if (file == null || file.isBlank()) {
            throw new IllegalArgumentException("Chapter file cannot be null or empty");
        }
    }

    public String displayName() {
        return (name != null && !name.isBlank()) ? name : file;
    }
}