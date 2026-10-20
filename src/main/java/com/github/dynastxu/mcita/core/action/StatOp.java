package com.github.dynastxu.mcita.core.action;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;

/**
 * A single mutation of one {@link ShotStat}. The JSON form mirrors what the source
 * Lua does, so imported data stays readable:
 * {@code "+3"}, {@code "*0.75"}, {@code "/2"}, {@code "=10"}, {@code "min:0"},
 * {@code "max:20"}, {@code "clamp:0:6"}.
 */
public record StatOp(Kind kind, float a, float b) {
    public enum Kind {ADD, MUL, DIV, SET, MIN, MAX, CLAMP}

    public static StatOp add(float value) {
        return new StatOp(Kind.ADD, value, 0f);
    }

    public static StatOp mul(float value) {
        return new StatOp(Kind.MUL, value, 0f);
    }

    public static StatOp div(float value) {
        return new StatOp(Kind.DIV, value, 0f);
    }

    public static StatOp set(float value) {
        return new StatOp(Kind.SET, value, 0f);
    }

    public static StatOp min(float value) {
        return new StatOp(Kind.MIN, value, 0f);
    }

    public static StatOp max(float value) {
        return new StatOp(Kind.MAX, value, 0f);
    }

    public static StatOp clamp(float low, float high) {
        return new StatOp(Kind.CLAMP, low, high);
    }

    public static final Codec<StatOp> CODEC = Codec.STRING.comapFlatMap(StatOp::parse, StatOp::format);

    public float apply(float current) {
        return switch (kind) {
            case ADD -> current + a;
            case MUL -> current * a;
            case DIV -> current / a;
            case SET -> a;
            case MIN -> Math.min(current, a);
            case MAX -> Math.max(current, a);
            case CLAMP -> Math.max(a, Math.min(b, current));
        };
    }

    private static DataResult<StatOp> parse(String text) {
        try {
            if (text.startsWith("+")) {
                return DataResult.success(add(Float.parseFloat(text.substring(1))));
            }
            if (text.startsWith("-")) {
                return DataResult.success(add(Float.parseFloat(text)));
            }
            if (text.startsWith("*")) {
                return DataResult.success(mul(Float.parseFloat(text.substring(1))));
            }
            if (text.startsWith("/")) {
                return DataResult.success(div(Float.parseFloat(text.substring(1))));
            }
            if (text.startsWith("=")) {
                return DataResult.success(set(Float.parseFloat(text.substring(1))));
            }
            if (text.startsWith("min:")) {
                return DataResult.success(min(Float.parseFloat(text.substring(4))));
            }
            if (text.startsWith("max:")) {
                return DataResult.success(max(Float.parseFloat(text.substring(4))));
            }
            if (text.startsWith("clamp:")) {
                String[] parts = text.substring(6).split(":");
                if (parts.length != 2) {
                    return DataResult.error(() -> "Invalid clamp op: " + text);
                }
                return DataResult.success(clamp(Float.parseFloat(parts[0]), Float.parseFloat(parts[1])));
            }
        } catch (NumberFormatException ignored) {
            // fall through to the error below
        }
        return DataResult.error(() -> "Invalid stat op: " + text);
    }

    private String format() {
        return switch (kind) {
            case ADD -> (a >= 0 ? "+" : "") + a;
            case MUL -> "*" + a;
            case DIV -> "/" + a;
            case SET -> "=" + a;
            case MIN -> "min:" + a;
            case MAX -> "max:" + a;
            case CLAMP -> "clamp:" + a + ":" + b;
        };
    }
}
