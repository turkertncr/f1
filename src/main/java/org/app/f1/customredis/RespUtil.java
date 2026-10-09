package org.app.f1.customredis;

import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public final class RespUtil {

    public record RedisError(String message) {}

    public static byte[] encodeArray(List<String> args) {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            out.write(("*" + args.size() + "\r\n").getBytes(StandardCharsets.UTF_8));
            for (String arg : args) {
                byte[] bytes = arg.getBytes(StandardCharsets.UTF_8);
                out.write(("$" + bytes.length + "\r\n").getBytes(StandardCharsets.UTF_8));
                out.write(bytes);
                out.write('\r');
                out.write('\n');
            }
            return out.toByteArray();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static Object decodeResp(InputStream in) throws IOException {
        int type = in.read();
        if (type == -1) throw new EOFException("redis closed the connection");

        String head = readLine(in);
        switch (type) {
            case '+':
                return head;
            case '-':
                return new RedisError(head);
            case ':':
                return Long.parseLong(head);
            case '$': {
                int len = Integer.parseInt(head);
                if (len < 0) return null;
                byte[] buf = in.readNBytes(len);
                if (buf.length < len) throw new EOFException("truncated bulk string");
                in.skipNBytes(2);
                return new String(buf, StandardCharsets.UTF_8);
            }
            case '*': {
                int n = Integer.parseInt(head);
                if (n < 0) return null;
                List<Object> items = new ArrayList<>(n);
                for (int i = 0; i < n; i++) items.add(decodeResp(in));
                return items;
            }
            default:
                throw new IOException("unknown RESP type byte: " + (char) type);
        }
    }

    private static String readLine(InputStream in) throws IOException {
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        int b;
        while ((b = in.read()) != -1) {
            if (b == '\r') {
                if (in.read() != '\n') throw new IOException("malformed RESP line terminator");
                return buf.toString(StandardCharsets.UTF_8);
            }
            buf.write(b);
        }
        throw new EOFException("redis closed the connection mid-reply");
    }
}
