package com.sg2022.we_got_the_moves.db.converter;

import androidx.room.TypeConverter;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class TypeConverters {

    private final Gson gson = new Gson();

    @TypeConverter
    protected <T> String fromListToJSON(List<T> l) {
        return gson.toJson(l);
    }

    @TypeConverter
    protected <T> ArrayList<T> fromJSONToList(String s) {
        Type listType = new TypeToken<List<String>>() {
        }.getType();
        return gson.fromJson(s, listType);
    }
}
