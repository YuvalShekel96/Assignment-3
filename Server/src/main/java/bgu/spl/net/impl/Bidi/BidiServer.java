package bgu.spl.net.impl.Bidi;

import bgu.spl.net.api.MessageEncoderDecoderImpl;
import bgu.spl.net.api.Users;
import bgu.spl.net.srv.Reactor;

import java.util.Date;

public class BidiServer {
    // TODO i thing that this class begining all the classs
    public static void main(String[] args) {
        Users social = Users.getInstance();
        Reactor<String> reactor = new Reactor<>(Integer.decode(args[1]).intValue(),
                Integer.decode(args[0]).intValue(),
                () -> new BidiProtocol(social),
                () -> new MessageEncoderDecoderImpl());
        reactor.serve();

    /*
    byte[] bb=new byte[1<<10];
    byte b=0;
    bb[0]=b;
    String s=new String(bb,0,1, StandardCharsets.UTF_8);
    if(s.getBytes(StandardCharsets.UTF_8)[0]==b){
        System.out.println("it works");
    }*/
    }

}