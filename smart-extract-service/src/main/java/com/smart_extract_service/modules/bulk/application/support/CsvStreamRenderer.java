package com.smart_extract_service.modules.bulk.application.support;

import com.smart_extract_service.modules.bulk.application.spi.BulkCell;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.List;

public final class CsvStreamRenderer implements AutoCloseable {

    private static final char SEP = ';';
    private static final String CRLF = "\r\n";

    private final Writer writer;

    public CsvStreamRenderer(OutputStream out) {
        this.writer = new BufferedWriter(
                new OutputStreamWriter(out, StandardCharsets.UTF_8), 1 << 16);
        raw("\uFEFF");
    }

    public void comment(String text) {
        raw("# " + text + CRLF);
    }

    public void headers(List<String> headers) {
        row(headers.stream().map(BulkCell::text).toList());
    }


    public void row(List<BulkCell> cells) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < cells.size(); i++) {
            if (i > 0) sb.append(SEP);
            sb.append(escape(cells.get(i).asFormattedString()));
        }
        raw(sb.append(CRLF).toString());
    }

    public void flush() {
        try {
            writer.flush();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public void close() {
        try {
            writer.flush();
            writer.close();
        } catch (IOException e) {
            // Ignora: OutputStream já fechado pelo container.
        }
    }

    private String escape(String value) {
        if (value == null) return "";
        boolean needs = value.indexOf(SEP) >= 0 || value.indexOf('"') >= 0
                || value.indexOf('\n') >= 0 || value.indexOf('\r') >= 0;
        if (!needs) return value;
        return '"' + value.replace("\"", "\"\"") + '"';
    }

    private void raw(String s) {
        try {
            writer.write(s);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}