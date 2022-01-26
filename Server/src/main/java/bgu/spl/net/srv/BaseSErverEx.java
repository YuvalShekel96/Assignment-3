/*package bgu.spl.net.srv;

import bgu.spl.net.api.BidiMessagingProtocol;
import bgu.spl.net.api.MessageEncoderDecoder;

import java.util.function.Supplier;

public class BaseSErverEx  extends BaseServer<T>{
    public BaseSErverEx(int port, Supplier<BidiMessagingProtocol<T>> protocolFactory, Supplier<MessageEncoderDecoder<T>> encoderdecoderfac)
    {
        super(port,protocolFactory,encoderdecoderfac);
    }

    @Override
    protected void execute(BlockingConnectionHandler<T> handler) {

        new Thread(handler).start();
    }
}
*/