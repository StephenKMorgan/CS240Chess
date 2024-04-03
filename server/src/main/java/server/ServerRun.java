package server;

public class ServerRun {
    //start up the server on localhost 4567
    public static void main(String[] args) {
        var database = new Server();
        database.run(4567);
    }
}
