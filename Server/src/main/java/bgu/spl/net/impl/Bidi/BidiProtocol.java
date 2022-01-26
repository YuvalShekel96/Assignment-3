package bgu.spl.net.impl.Bidi;
import java.util.*;

import bgu.spl.net.api.*;
import bgu.spl.net.srv.ConnectionHandler;
import bgu.spl.net.srv.Connections;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class BidiProtocol implements BidiMessagingProtocol<String> {
    Commands commands;
    int id;
    Connections connections;
    Users myUsers;
    AtomicBoolean terminate;
    ConnectionHandler myHandler;
    public BidiProtocol(Users _users){
        myUsers=_users;
    }
    @Override
    public void start(int connectionId, Connections<String> _connections) {
        commands=Commands.getInstance();
        id=connectionId;
        connections=_connections;
        myUsers.addToID_Handler(id,this);
        myUsers.SetConnectios(connections);
        terminate=new AtomicBoolean(false);
    }
    public void terminateProtocol()
    {
        terminate.set(true);
    }

    @Override
    public void process(String message) {
        String mess=message;
        byte[] OpCodeBytes=mess.substring(0,2).getBytes();
        if(mess.length()>2)
            mess=mess.substring(2);
        short opCode=bytesToShort(OpCodeBytes);
        switch (opCode) {
            case 1:
                String usernameReg = getNextString(mess);
                mess = cutString(mess);
                String passwordReg = getNextString(mess);
                mess = cutString(mess);
                String brithdayReg = getNextString(mess);
                commands.REGISTER(usernameReg, passwordReg, brithdayReg, id);
                break;
            case 2:
                String usernameLI = getNextString(mess);
                mess = cutString(mess);
                String passwordLI = getNextString(mess);
                mess = cutString(mess);
                byte[] captcha = mess.getBytes();
                commands.LOGIN(usernameLI, passwordLI, captcha[0], id);
                break;
            case 3:
                commands.LOGOUT(id);
                break;
            case 4:
                byte[] fol = mess.getBytes();
                String usernameToFol = getNextString(mess.substring(2));
                commands.FOLLOW(fol[0], usernameToFol, id);
                break;
            case 5:
                commands.POST(getNextString(mess), id);
                break;
            case 6:
                String usernameToPM = getNextString(mess);
                mess = cutString(mess);
                String content = getNextString(mess);
                mess = cutString(mess);
                String timeStamp = getNextString(mess);
                commands.PM(usernameToPM, content, timeStamp, id);
                break;
            case 7:
                commands.LOGSTAT(id);
                break;
            case 8:
                mess=getNextString(mess);
                String[] arrayOfUsernames = mess.split("\\|",0);
                List<String> listOfUsernames = Arrays.asList(arrayOfUsernames);
                commands.STAT(listOfUsernames, id);
                break;
            case 12:
                String userToBlock = getNextString(mess);
                commands.BLOCK(userToBlock, id);
        }

    }

    private String cutString(String mess) {
        int endIndex=mess.indexOf('\0');
        return mess.substring(endIndex+1);
    }

    private String getNextString(String mess) {
        int endIndex=mess.indexOf('\0');
        return mess.substring(0,endIndex);
    }

    @Override
    public boolean shouldTerminate() {
        return terminate.get();
    }
    public void setMyHandler(ConnectionHandler handler){myHandler=handler;}
    public ConnectionHandler getHandler(){return myHandler;}
    private short bytesToShort(byte[] byteArr)
    {
        short result = (short)((byteArr[0] & 0xff) << 8);
        result += (short)(byteArr[1] & 0xff);
        return result;
    }
}
