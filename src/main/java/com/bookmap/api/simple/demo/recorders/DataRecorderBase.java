package com.bookmap.api.simple.demo.recorders;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.TimeZone;
import java.util.stream.Collectors;

public abstract class DataRecorderBase {
    private BufferedWriter writer;
    StringBuilder builder = new StringBuilder();
    protected final String delimiter = ",";

    protected abstract String getFilename();

    protected void appendFirst(final StringBuilder s) {
    }

    protected void appendLast(final StringBuilder s) {
    }

    public void stop() {
        try {
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public static String getDateTime(long nanoseconds) {
        long millis = nanoseconds / 1_000_000L;
        long nanos = nanoseconds - 1_000_000L * millis;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd HH:mm:ss.SSS");
        sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
        String datetime = sdf.format(new Date(millis)) + String.format("%06d", nanos);
        return datetime;
    }
    
    protected void writeObjects(Object... objects) {
        builder.setLength(0);
        appendFirst(builder);
        if (objects.length > 0) {
            if (builder.length() > 0) {
                builder.append(delimiter);
            }
            builder.append(Arrays.stream(objects).map(Object::toString).collect(Collectors.joining(delimiter)));
        }
        appendLast(builder);
        try {
            if (writer == null) {
                writer = new BufferedWriter(new FileWriter(getFilename()));
            }
            if (builder.length() > 0) {
                writer.write(builder.toString());
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
