package org.app.f1.service;

import lombok.extern.slf4j.Slf4j;
import org.app.f1.customredis.RespUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;
import java.io.*;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.ToDoubleFunction;

@Slf4j
@Component
public class CustomRedisService {

    private static final int MAX_CHUNK_BYTES = 16000;
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final String host;
    private final int port;
    private final int timeoutMs;
    private long ttl = -1;

    private Socket socket;
    private BufferedInputStream in;

    public CustomRedisService(
            @Value("${custom.redis.host:127.0.0.1}") String host,
            @Value("${custom.redis.port:6379}") int port,
            @Value("${custom.redis.timeout-ms:2000}") int timeoutMs) {
        this.host = host;
        this.port = port;
        this.timeoutMs = timeoutMs;
        connectSocket();
    }

    public CustomRedisService expireAfter(long duration, TimeUnit unit) {
        this.ttl = unit.toSeconds(duration);
        return this;
    }

    private synchronized void connectSocket() {
        try {
            this.socket = new Socket(host, port);
            this.socket.setSoTimeout(timeoutMs);
            this.socket.setKeepAlive(true);
            this.in = new BufferedInputStream(socket.getInputStream(), 65536);
            log.info("Connected to redis at {}:{}", host, port);
        } catch (IOException e) {
            log.error("failed to connect to redis at {}:{}: {}", host, port, e.getMessage());
            this.socket = null;
            this.in = null;
        }
    }

    synchronized void reconnect() {
        close();
        connectSocket();
    }

    public void close() {
        try {
            if (in != null) in.close();
            if (socket != null) socket.close();
            log.info("Socket closed");
        } catch (IOException e) {
            log.warn("error closing redis socket: {}", e.getMessage());
        } finally {
            socket = null;
            in = null;
        }
    }

    synchronized void sendRedis(byte[] cmd, List<Object> out) {
        if (socket == null || socket.isClosed() || !socket.isConnected()) {
            reconnect();
        }
        if (socket == null) {
            log.error("redis unavailable, dropping command");
            return;
        }

        try {
            socket.getOutputStream().write(cmd);
            socket.getOutputStream().flush();

            Object reply = RespUtil.decodeResp(socket.getInputStream());
            out.add(reply);
        } catch (IOException e) {
            log.error("redis I/O error, reconnecting: {}", e.getMessage());
            retryBackoff(cmd, out);
        }
    }

    private void retryBackoff(byte[] cmd, List<Object> out) {
        log.info("retry");
        int maxAttempts = 5;
        int delay = 50;
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            reconnect();
            if (socket == null) {
                sleepQuietly(delay);
                delay *= 2;
                continue;
            }
            try {
                Thread.sleep(delay);
                socket.getOutputStream().write(cmd);
                socket.getOutputStream().flush();

                Object reply = RespUtil.decodeResp(socket.getInputStream());
                out.add(reply);
                return;
            } catch (Exception e) {
                log.error("redis I/O error on retry {}/{}: {}", attempt, maxAttempts, e.getMessage());
                sleepQuietly(delay);
                delay *= 2;
            }
        }
        log.error("redis unavailable after {} attempts, dropping command", maxAttempts);
    }

    private static void sleepQuietly(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void sendChunk(String fn, String key, List<String[]> items, List<Integer> itemBytes, List<Object> out) {
        List<String> args = new ArrayList<>(List.of(fn, key));
        int baseBytes = encodedSize(fn, key);
        int bytes = baseBytes;

        for (int i = 0; i < items.size(); i++) {
            String[] pair = items.get(i);
            int pairBytes = itemBytes.get(i);
            if (pairBytes + bytes > MAX_CHUNK_BYTES && args.size() > 2) {
                byte[] resp = RespUtil.encodeArray(args);
                sendRedis(resp, out);

                args.clear();
                args.addAll(List.of(fn, key));
                bytes = baseBytes;
            }
            args.add(pair[0]);
            args.add(pair[1]);
            bytes += pairBytes;
        }
        if (args.size() > 2) sendRedis(RespUtil.encodeArray(args), out);
    }

    public <T> void redisSortedSetAdd(List<T> stream, String key, ToDoubleFunction<T> scoreFn) {
        List<String[]> items = new ArrayList<>();
        List<Integer> itemBytes = new ArrayList<>();
        List<Object> out = new ArrayList<>();
        for (T o : stream) {
            String scoreStr = String.valueOf(scoreFn.applyAsDouble(o));
            String member = MAPPER.writeValueAsString(o);
            items.add(new String[]{scoreStr, member});
            itemBytes.add(encodedSize(scoreStr, member));
        }
        sendChunk("ZADD", key, items, itemBytes, out);
        setTtl(key);
    }

    private void setTtl(String key) {
        List<Object> out = new ArrayList<>();
       List<String> args = List.of("EXPIRE", key, String.valueOf(ttl));
       sendRedis(RespUtil.encodeArray(args), out);
    }

    public <T> void redisStringSet(String key, T o) {
        List<Object> out = new ArrayList<>();
        List<String> args = List.of(
                "SET", key, MAPPER.writeValueAsString(o)
        );
        sendRedis(RespUtil.encodeArray(args), out);
        setTtl(key);
    }

    public List<Object> redisStringGet(String key) {
        List<Object> out = new ArrayList<>();
        List<String> args = List.of(
                "GET", key
        );
        sendRedis(RespUtil.encodeArray(args), out);
        return out;
    }

    public List<Object> redisSortedSetRangeBy(String key, double start, double end) {
        String startStr = String.valueOf(start);
        String endStr = String.valueOf(end);
        List<Object> out = new ArrayList<>();

        sendChunk("ZRANGEBYSCORE", key,
                Collections.singletonList(new String[]{startStr, endStr}),
                Collections.singletonList(encodedSize(startStr, endStr)), out);
        return out;
    }

    public void redisDel(String key) {
        List<Object> out = new ArrayList<>();
        List<String> args = List.of(
                "DEL", key
        );
        sendRedis(RespUtil.encodeArray(args), out);
    }

    private int encodedSize(String... args) {
        int bytes = 0;
        for (String arg : args) {
            if (arg == null) continue;
            int len = arg.length();
            for (int i = 0; i < len; i++) {
                char c = arg.charAt(i);
                if (c < 0x80) {
                    bytes += 1;
                } else if (c < 0x800) {
                    bytes += 2;
                } else if (Character.isHighSurrogate(c)) {
                    bytes += 4;
                    i++;
                } else {
                    bytes += 3;
                }
            }
        }
        return bytes;
    }
}
