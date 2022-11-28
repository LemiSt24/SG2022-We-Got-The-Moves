package com.sg2022.we_got_the_moves.db.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import javax.annotation.Nullable;

@Entity(tableName = "Exercise")
public class Exercise {

    @PrimaryKey(autoGenerate = true)
    public long id;

    @ColumnInfo(name = "name")
    public String name;

    @Nullable
    public String getTextInstruction() {
        return textInstruction;
    }

    public void setTextInstruction(@Nullable String textInstruction) {
        this.textInstruction = textInstruction;
    }

    @Nullable
    public String getVideoInstruction() {
        return videoInstruction;
    }

    public void setVideoInstruction(@Nullable String videoInstruction) {
        this.videoInstruction = videoInstruction;
    }

    @Nullable
    private String textInstruction;
    @Nullable
    private String videoInstruction;

    public Exercise(long id, String name, String textInstruction, String videoInstruction) {
        this.id = id;
        this.name = name;
        this.textInstruction = textInstruction;
        this.videoInstruction = videoInstruction;
    }
}