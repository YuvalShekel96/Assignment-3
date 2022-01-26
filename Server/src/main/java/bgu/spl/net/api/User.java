package bgu.spl.net.api;


import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

public class User {
    private String UserName,Password;
    private String  BirthDay;
    private Date logoutdate;
    private List<User> following= Collections.synchronizedList(new LinkedList<>());
    private List<User> followers= Collections.synchronizedList(new LinkedList<>());
    public List<User> Blocked=Collections.synchronizedList(new LinkedList<>());
    public ConcurrentLinkedQueue<String> Note=new ConcurrentLinkedQueue<>();
    private short numofpost;


    public User(String UserName, String Password, String  Birthday)
    {
        numofpost=0;
        this.Password=Password;
        this.UserName=UserName;
        this.BirthDay=Birthday;
        logoutdate=null;
    }
    public void Addtopost(String post)
    {
        this.Note.add(post);
    }
    public ConcurrentLinkedQueue<String> getNOte()
    {
        return this.Note;
    }
    public void Addtonumofpost()
    {
        this.numofpost++;
    }
    public  short GetnumofPOst()
    {
        return this.numofpost;
    }
    public String getName()
    {
        return this.UserName;
    }
    public String getPassword()
    {
        return this.Password;
    }
    public void addFollower(User newFollower){
        followers.add(newFollower);
    }
    public void removeFollower(User userToRemove){
        followers.remove(userToRemove);
    }
    public void startFollowing(User userToFollow){following.add(userToFollow);}
    public void stopFollowing(User userToRemove){following.remove(userToRemove);}
    public boolean isFollowing(User userToCheck){return following.contains(userToCheck);}
    public boolean isFollowedBy(User userToCheck){return followers.contains(userToCheck);}
    public List<User> getAllFollowers(){return  new LinkedList<>(followers);}
    public List<User> getFollowing(){return new LinkedList<>(following);}
    public short getNumOfFollowers(){return (short)followers.size();}
    public short getNumOfFollowing(){return (short)following.size();}

    // todo add function that return the age of the user

    public String UserToString()
    {
        return "User"+ " the date of birthday is :"+this.BirthDay+"THe name of the User is :"+this.UserName+"the password is :"+this.Password;

    }
    public void logout(Date d)
    {

        this.logoutdate=d;
    }
    public short getAge()
    {
        DateTimeFormatter formatter=DateTimeFormatter.ofPattern("dd-MM-yyyy");
        short age=(short) Period.between(LocalDate.parse(BirthDay, formatter),LocalDate.now()).getYears();
        return age;
    }
    public boolean IsBlocked(User u1)
    {
        return Blocked.contains(u1);
    }
    public void blockUser(User toBlock){
        followers.remove(toBlock);
        following.remove(toBlock);
        Blocked.add(toBlock);
    }
    public Date getLogoutdate(){return logoutdate;}

}
