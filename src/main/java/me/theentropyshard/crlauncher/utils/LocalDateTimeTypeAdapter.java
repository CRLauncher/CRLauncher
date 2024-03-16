package me.theentropyshard.crlauncher.utils;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.time.LocalDateTime;

public class LocalDateTimeTypeAdapter extends TypeAdapter<LocalDateTime> {
    public LocalDateTimeTypeAdapter() {

    }

    @Override
    public void write(JsonWriter writer, LocalDateTime dateTime) throws IOException {
        writer.value(dateTime.toString());
    }

    @Override
    public LocalDateTime read(JsonReader reader) throws IOException {
        return LocalDateTime.parse(reader.nextString());
    }
}
