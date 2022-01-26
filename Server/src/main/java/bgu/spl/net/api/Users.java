package bgu.spl.net.api;

import bgu.spl.net.srv.Connections;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class Users {
    private  ConcurrentHashMap<Integer, User> map_ConnectionID_User;//todo changed to <Integer,User>
    private  ConcurrentHashMap<User, Integer> map_User_ConnectionId;
    private ConcurrentHashMap<Integer, BidiMessagingProtocol> map_Id_Handler;
    private  ConcurrentHashMap<String, User> AllUsers;
    private  ConcurrentHashMap<String, User> ConnectedUsers;
    private Connections myConniction=null;
    private List<String> allMessages;

    private static class UsersSingilton {
        private static Users users = new Users();
    }
    public void SetConnectios(Connections c)
    {
        if (myConniction==null)
            myConniction=c;
    }
     public Connections getconnections()
     {
         return myConniction;
     }
    private Users(){
        map_ConnectionID_User=new ConcurrentHashMap<>();
        map_User_ConnectionId=new ConcurrentHashMap<>();
        map_Id_Handler=new ConcurrentHashMap<>();
        AllUsers=new ConcurrentHashMap<>();
        ConnectedUsers=new ConcurrentHashMap<>();
        allMessages= Collections.synchronizedList(new LinkedList<>());
    }

    public static Users getInstance() {
        return UsersSingilton.users;
    }

    public boolean isLoggedIn(String username){
        return ConnectedUsers.get(username) != null;
    }
    public boolean isRegistered(String username){
        return AllUsers.get(username) != null;
    }
    public boolean addToConnectionId_User(int id,User u1){
        if(map_ConnectionID_User.get(id)==null){
            map_ConnectionID_User.put(id,u1);
            return true;
        }return false;
    }
    public boolean addToID_Handler(int id,BidiMessagingProtocol handler){
        if(map_Id_Handler.get(id)==null){
            map_Id_Handler.put(id,handler);
            return true;
        }return false;
    }
    public boolean addToAllUsers(String username,User u1) {
            AllUsers.put(username, u1);
            return true;
    }
    public User getUserByConnectionId(int id){
        return map_ConnectionID_User.get(id);
    }

    public User removeUserByConnectionId(int id) {
        return map_ConnectionID_User.remove(id);
    }

    public Integer getConnectionIdByUser(User u1){
        return map_User_ConnectionId.get(u1);
    }
    public Integer removeConnectionIdByUser(User u1){
        return map_User_ConnectionId.remove(u1);
    }
    public BidiMessagingProtocol getConnectionHandlerById(int id){
        return map_Id_Handler.get(id);
    }
    public BidiMessagingProtocol removeConnectionHandlerById(int id){
        return map_Id_Handler.remove(id);
    }
    public User getUser(String username){
        return AllUsers.get(username);
    }
    public User getConnectedUser(String username){
        return ConnectedUsers.get(username);
    }
    public User disconnectUser(User u1){
        synchronized (ConnectedUsers) {
            if (u1 != null) {
                removeConnectionHandlerById(getConnectionIdByUser(u1)).terminateProtocol();
                removeUserByConnectionId(removeConnectionIdByUser(u1));
                ConnectedUsers.remove(u1.getName());

            } return u1;
        }
    }
    public List<User> getAllUsers(){
        List<User> allUsersList=new LinkedList<>();
        synchronized (AllUsers){
            AllUsers.forEach((username,user)->allUsersList.add(user));
            return allUsersList;
        }
    }
    public List<User> getAllCon(){
        List<User> list=new LinkedList<>();
        synchronized (list)
        {
            ConnectedUsers.forEach((username,user)->list.add(user));
            return list;
        }
    }
    public void loginUser(String username,int id){
        synchronized (ConnectedUsers) {
            User u = AllUsers.get(username);
            ConnectedUsers.put(username, u);
            map_User_ConnectionId.put(u, id);
            map_ConnectionID_User.put(id, u);
        }
    }
    public boolean ConnectedE (String name )
    {
        return this.ConnectedUsers.contains(name);
    }
    public void addToConnectedUsers (String name,User user)
    {
        ConnectedUsers.put(name,user);
    }
    public void addMessage(String msg){allMessages.add(msg);}
}
