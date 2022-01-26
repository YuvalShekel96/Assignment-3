package bgu.spl.net.srv;

import bgu.spl.net.api.BidiMessagingProtocol;
import bgu.spl.net.api.MessageEncoderDecoder;

import java.util.function.Supplier;

public class TPCServer<T> extends BaseServer<T> {

    public TPCServer(int port,
                         Supplier<BidiMessagingProtocol<T>> protocolFactory,
                         Supplier<MessageEncoderDecoder<T>> encdecFactory) {
        super(port, protocolFactory, encdecFactory);

    }
    @Override
    protected void execute(BlockingConnectionHandler handler) {
        new Thread(handler).start();
    }
}
