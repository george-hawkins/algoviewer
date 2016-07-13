package net.betaengine.algoviewer;

import java.util.function.Supplier;

public interface Pusher {
    <T> T push(Object source, Supplier<T> s, String methodName);
}