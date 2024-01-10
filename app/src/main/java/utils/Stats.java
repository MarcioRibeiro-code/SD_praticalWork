package utils;

public class Stats {
    private  int totalOfApprovedTasks=0;
    private  int totalOfRequestedTasks=0;
    private  int numberOfConnectedUsers=0;
    private  int numberOfChannels=0;

    //make this class fully synchronized
    public synchronized void incrementTotalOfApprovedTasks(){
        totalOfApprovedTasks++;
    }

    public synchronized void incrementTotalOfRequestedTasks(){
        totalOfRequestedTasks++;
    }

    public synchronized void incrementNumberOfConnectedUsers(){
        numberOfConnectedUsers++;
    }

    public synchronized void incrementNumberOfChannels(){
        numberOfChannels++;
    }

    public synchronized void decrementNumberOfConnectedUsers(){
        numberOfConnectedUsers--;
    }

    public synchronized void decrementNumberOfChannels(){
        numberOfChannels--;
    }

    public synchronized int getTotalOfApprovedTasks() {
        return totalOfApprovedTasks;
    }

    public synchronized int getTotalOfRequestedTasks() {
        return totalOfRequestedTasks;
    }

    public synchronized int getNumberOfConnectedUsers() {
        return numberOfConnectedUsers;
    }

    public synchronized int getNumberOfChannels() {
        return numberOfChannels;
    }

    public synchronized void resetStats(){
        totalOfApprovedTasks=0;
        totalOfRequestedTasks=0;
        numberOfConnectedUsers=0;
        numberOfChannels=0;
    }

    @Override
    public String toString() {
        return "Stats{" +
                "totalOfApprovedTasks=" + totalOfApprovedTasks +
                ", totalOfRequestedTasks=" + totalOfRequestedTasks +
                ", numberOfConnectedUsers=" + numberOfConnectedUsers +
                ", numberOfChannels=" + numberOfChannels +
                '}';
    }


}
