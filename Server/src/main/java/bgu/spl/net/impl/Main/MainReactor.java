package bgu.spl.net.impl.Main;

import bgu.spl.net.api.*;
import bgu.spl.net.impl.Bidi.BidiProtocol;
import bgu.spl.net.srv.Connections;
import bgu.spl.net.srv.ConnectionsImpl;
import bgu.spl.net.srv.Reactor;

public class MainReactor {
    public static void main(String[] args)
    {
        Users social = Users.getInstance();
        Reactor<String> reactor = new Reactor<>(Integer.decode(args[1]).intValue(),
                Integer.decode(args[0]).intValue(),
                () -> new BidiProtocol(social),
                () -> new MessageEncoderDecoderImpl());
        reactor.serve();
    }
}
