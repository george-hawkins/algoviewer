package net.betaengine.immutableunion;

import java.util.ArrayDeque;
import java.util.Deque;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Multiset;
import com.google.common.collect.TreeMultiset;

public class CallStack {
    private final Deque<Object> stack = new ArrayDeque<>();
    private final Multiset<String> callCount = TreeMultiset.create();

    public void push(Object current, String methodName) {
        stack.push(current);
        
        callCount.add(methodName);
    }
    
    public void pop() {
        stack.pop();
    }
    
    public ImmutableList<?> copyStack() {
        return ImmutableList.copyOf(stack);
    }
    
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
