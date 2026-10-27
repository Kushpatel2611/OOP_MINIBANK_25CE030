package service;

public class TransactionBuffer {
    private static final int CAPACITY = 5;
    private final String[] buffer = new String[CAPACITY];
    private int head;
    private int tail;
    private int count;

    public synchronized void add(String transaction) throws InterruptedException {
        while (count == CAPACITY) {
            wait();
        }

        buffer[tail] = transaction;
        tail = (tail + 1) % CAPACITY;
        count++;
        System.out.println("Produced: " + transaction);
        notifyAll();
    }

    public synchronized String take() throws InterruptedException {
        while (count == 0) {
            wait();
        }

        String transaction = buffer[head];
        head = (head + 1) % CAPACITY;
        count--;
        System.out.println("Consumed: " + transaction);
        notifyAll();
        return transaction;
    }
}
