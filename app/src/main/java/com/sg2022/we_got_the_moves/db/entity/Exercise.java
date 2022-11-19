package com.sg2022.we_got_the_moves.db.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "Exercise")
public class Exercise {

    @PrimaryKey(autoGenerate = true)
    public long id;

    @ColumnInfo(name = "name")
    public String name;

    public Exercise(long id, String name) {
        this.id = id;
        this.name = name;
    }
}