package net.betaengine.algoviewer;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.function.Supplier;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Multiset;
import com.google.common.collect.TreeMultiset;

public class CallStack implements Pusher {
    private final Deque<Object> stack = new ArrayDeque<>();
    private final Multiset<String> callCount = TreeMultiset.create();

    @Override
    public <T> T push(Object current, Supplier<T> s, String methodName) {
        try {
            stack.push(current);
            
            callCount.add(methodName);
            
            ImmutableList<?> stackList = ImmutableList.copyOf(stack);
            
            onEnter(methodName, stackList);
            T result = s.get();
            onExit(methodName, stackList, result);
            
            return result;
        } finally {
            stack.pop();
        }
    }

    protected void onEnter(String methodName, ImmutableList<?> stackList) { }
    
    protected void onExit(String methodName, ImmutableList<?> stackList, Object result) { }
    
    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("stack", stack)
                .add("callCount", callCount)
                .toString();
    }

    public void clear() {
        stack.clear();
        callCount.clear();
    }
}
