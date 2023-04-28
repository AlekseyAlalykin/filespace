package org.filespace.services.threads;

public abstract class CustomThread extends Thread {
    private static int threadInitNumber = 0;

    protected static int nextThreadNum(){
        return threadInitNumber++;
    }

    public CustomThread(String name) {
        super(name);
    }
}
