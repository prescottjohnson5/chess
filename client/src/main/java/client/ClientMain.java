package client;

public class ClientMain {
    public static void main(String[] args) {
        String host = "localhost";
        int port = 8080;

        if (args.length == 1) {
            port = Integer.parseInt(args[0]);
        } else if (args.length >= 2) {
            host = args[0];
            port = Integer.parseInt(args[1]);
        }

        ServerFacade facade = new ServerFacade(host, port);
        new ChessClient(facade).run();
    }
}
