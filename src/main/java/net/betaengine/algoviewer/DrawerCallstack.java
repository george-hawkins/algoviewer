package net.betaengine.algoviewer;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import com.google.common.collect.ImmutableList;

// Subclass of CallStack that invokes Drawer functionality on method entry and exit.
public class DrawerCallstack extends CallStack {
    private final AtomicReference<ImmutableList<?>> currentStack = new AtomicReference<>();
    private final AtomicBoolean entry = new AtomicBoolean();
    
    private Drawer drawer;

    @Override
    protected void onEnter(String methodName, ImmutableList<?> stackList) {
        entry.set(true);
        currentStack.set(stackList);
        drawer.drawEnter(methodName);
    }
    
    @Override
    protected void onExit(String methodName, ImmutableList<?> stackList, Object result) {
        entry.set(false);
        currentStack.set(stackList);
        drawer.drawExit(methodName, result);
    }
    
    public void setDrawer(Drawer drawer) {
        this.drawer = drawer;
    }
    
    public ImmutableList<?> getCurrentStack() { return currentStack.get(); }
    
    public boolean isEntry() { return entry.get(); }
}
