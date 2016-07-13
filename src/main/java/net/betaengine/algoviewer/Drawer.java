package net.betaengine.algoviewer;

import javax.swing.SwingUtilities;
import javax.swing.Timer;

public abstract class Drawer {
    private final Object drawLock = new Object();
    private final Timer timer = new Timer(200, e -> advance());
    
    private boolean enabled = false;
    private Object currentResult = null;
    
    public Drawer() {
        timer.setRepeats(false);
    }
    
    public void drawEnter(String methodName) {
        draw(methodName, null);
    }
    
    public void drawExit(String methodName, Object result) {
        draw(methodName, () -> { currentResult = result; });
    }
    
    // Currently methodName is ignored but it could e.g. be printed to a status line in the GUI.
    private void draw(String methodName, Runnable action) {
        synchronized (drawLock) {
            if (!enabled) {
                return;
            }
            
            if (action != null) {
                action.run();
            }
        
            SwingUtilities.invokeLater(this::drawRunner);
            wait(drawLock);
        }
    }
    
    private void drawRunner() {
        synchronized (drawLock) {
            doDraw(currentResult);

            timer.start();
        }
    }
    
    protected abstract void doDraw(Object currentResult);
    
    public void runAlgo(Runnable clearStack, Runnable finishedCallback) {
        currentResult = null;
        enabled = false;
    
        setupAlgo();
        
        clearStack.run();
        
        enabled = true;
        
        new Thread(() -> {
            runAlgo();
            SwingUtilities.invokeLater(finishedCallback);
        }).start();
    }
    
    protected abstract void setupAlgo();
    
    protected abstract void runAlgo();
    
    private void advance() {
        synchronized (drawLock) {
            drawLock.notify();
        }
    }
    
    private void wait(Object o) {
        try {
            o.wait();
        } catch (InterruptedException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
}

