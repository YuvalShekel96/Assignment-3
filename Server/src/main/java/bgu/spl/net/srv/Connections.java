package bgu.spl.net.srv;

import java.io.IOException;

public interface Connections<T> {
    // todo what is send
    boolean send(int connectionId, T msg);

    void broadcast(T msg);

    void disconnect(int connectionId);
}
