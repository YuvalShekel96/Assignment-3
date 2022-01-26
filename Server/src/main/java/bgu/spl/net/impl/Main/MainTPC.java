package bgu.spl.net.impl.Main;

import bgu.spl.net.api.MessageEncoderDecoderImpl;
import bgu.spl.net.api.Users;
import bgu.spl.net.impl.Bidi.BidiProtocol;
import bgu.spl.net.srv.TPCServer;

public class MainTPC {
    public static void main(String[] args) {
        Users users = Users.getInstance();
        TPCServer<String> baseServer = new TPCServer<>(Integer.decode(args[0]).intValue(),
                () -> new BidiProtocol(users),
                () -> new MessageEncoderDecoderImpl());
        baseServer.serve();
    }
}
