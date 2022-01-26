package bgu.spl.net.srv;

import bgu.spl.net.api.BidiMessagingProtocol;
import bgu.spl.net.api.User;
import bgu.spl.net.api.Users;

public class ConnectionsImpl<T> implements Connections<T> {
    Users myUsers;
    private static class ConnectionsHolder{
        private static ConnectionsImpl instance=new ConnectionsImpl();
    }
    public static ConnectionsImpl getInstance(){
        return ConnectionsHolder.instance;

    }
    private ConnectionsImpl(){
        myUsers=Users.getInstance();
    }

    @Override
    public boolean send(int connectionId, T msg) {
        BidiMessagingProtocol handlerToSend=myUsers.getConnectionHandlerById(connectionId);
        if(handlerToSend != null) {
            handlerToSend.getHandler().send(msg);
            return true;
        }
        return false;
    }

    @Override
    public void broadcast(T msg) {
        for(User u: myUsers.getAllUsers()){
            send(myUsers.getConnectionIdByUser(u),msg);
        }
    }

    @Override
    public void disconnect(int connectionId) {
        send(connectionId,(T)"1003");
        myUsers.disconnectUser(myUsers.getUserByConnectionId(connectionId));
    }
}
