package bgu.spl.net.api;


import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

public class Commands {
    public static Commands getInstance() {
        return CommandsSingilton.Commans;
    }

    private static class CommandsSingilton {
        private static Commands Commans = new Commands();
    }

    private Users users = Users.getInstance();
    private String[] banned = {"shit", "Morty", "hell"};

    public void REGISTER(String Username, String Password, String Birthday, int id) {
        User U1 = new User(Username, Password, Birthday);
        if (users.isRegistered(Username)) {
            ERROR("01", id);
        } else {
            users.addToAllUsers(U1.getName(), U1);
            ACK("01", id, null);
        }

    }

    public void LOGIN(String username, String Password, byte Capcha, int id) {
        if ((Capcha != 1) || (!users.isRegistered(username)) || (!users.getUser(username).getPassword().equals(Password))
                || (users.isLoggedIn(username))) {
            ERROR("02", id);
        } else {
            ACK("02", id, null);
            users.loginUser(username, id);
            User loggedInUser = users.getUser(username);
            while (!loggedInUser.Note.isEmpty()) {
                users.getconnections().send(id, loggedInUser.Note.poll());

            }
        }
    }


    public void LOGOUT(int id) {
        Date d = new Date();
        User U1 = users.getUserByConnectionId(id);
        if (U1 == null || !users.isLoggedIn(U1.getName())) {
            ERROR("03", id);
        } else {
            U1.logout(d);
            users.getconnections().disconnect(id);
            ACK("03", id, null);
        }

    }

    public void FOLLOW(byte b, String username, int id) {
        User sendingUser = users.getUserByConnectionId(id);
        if (!users.isRegistered(username) || sendingUser == null || users.getUser(username).IsBlocked(sendingUser) || b != 0 && b != 1) {
            ERROR("04", id);
        } else {
            User userToFollow = users.getUser(username);
            //b==0 => follow
            if (b == 1) {
                if (userToFollow.isFollowedBy(sendingUser) || sendingUser.isFollowing(userToFollow)) {
                    ERROR("04", id);
                } else {
                    sendingUser.startFollowing(userToFollow);
                    userToFollow.addFollower(sendingUser);
                    ACK("04", id, null);
                }

            }// B==1 => unfollow
            else {
                if (!userToFollow.isFollowedBy(sendingUser) || !sendingUser.isFollowing(userToFollow)) {
                    ERROR("04", id);
                } else {
                    sendingUser.stopFollowing(userToFollow);
                    userToFollow.removeFollower(sendingUser);
                    ACK("04", id, null);
                }

            }
        }
    }


    public void POST(String post, int id) {
        User U1 = users.getUserByConnectionId(id);
        if (U1 != null && users.isLoggedIn(U1.getName())) {
            U1.Addtonumofpost();
            Iterator<User> iter = U1.getAllFollowers().iterator();
            while (iter.hasNext()) {
                User toSendUser = iter.next();
                if (!toSendUser.IsBlocked(U1)) {
                    NOTIFICATION("091" + U1.getName()
                            + "\0" + post, users.getConnectionIdByUser(toSendUser), toSendUser);
                }

            }
            for (int j = 0; j < post.length(); j++) {
                if (post.charAt(j) == '@') {
                    j = j + 1;
                    String nameofUser = "";
                    while (j < post.length() && post.charAt(j) != ' ') {
                        nameofUser = nameofUser + post.charAt(j);
                        j = j + 1;
                    }
                    User toSendUser = users.getUser(nameofUser);
                    if (toSendUser != null && !toSendUser.IsBlocked(U1)) {
                        NOTIFICATION("091" + nameofUser + "\0" + post,
                                users.getConnectionIdByUser(toSendUser), toSendUser);
                    }
                }
            }
            ACK("05", id, null);
            users.addMessage(post);
        } else {
            ERROR("05", id);
        }
    }

    public void PM(String tosend, String contant, String timeStamp, int id) {
        User sendingUser = users.getUserByConnectionId(id);
        User toSendUser = users.getUser(tosend);
        if (sendingUser == null || !users.isLoggedIn(sendingUser.getName()) ||
                toSendUser == null || !sendingUser.isFollowing(toSendUser) ||
                sendingUser.IsBlocked(toSendUser))
            ERROR("06", id);
        else {
            contant = filterPM(contant);
            NOTIFICATION("090" + users.getUserByConnectionId(id).getName() + "\0" + contant + " " + timeStamp, users.getConnectionIdByUser(toSendUser), toSendUser);
            ACK("06", id, null);
            users.addMessage(contant);
        }

    }

    public void LOGSTAT(int id) {
        User sendingUser = users.getUserByConnectionId(id);
        if (sendingUser != null && users.isLoggedIn(sendingUser.getName())) {
            String logstat = "";
            List<User> list = users.getAllCon();
            for (User u : list) {
                if (u != sendingUser && !u.IsBlocked(sendingUser)) {
                    String s = getStats(u);
                    ACK("07", id, s);
                }
            }

        } else {
            ERROR("07", id);
        }
    }

    public void STAT(List<String> str, int id) {
        boolean thereIsABlockedU=false;
        User sendingUser = users.getUserByConnectionId(id);
        if (sendingUser != null && users.isLoggedIn(sendingUser.getName())) {
            for (String blocked : str) {
                User u = users.getUser(blocked);
                if (u != null && u.IsBlocked(sendingUser)) {
                    thereIsABlockedU = true;
                    ERROR("07", id);
                    break;
                }
            }
            if (!thereIsABlockedU) {
                for (String username : str) {
                    String curUsername = username;
                    User u = users.getUser(curUsername);
                    if (u != null && !sendingUser.IsBlocked(u)) {
                        String s = getStats(u);
                        ACK("08", id, s);
                    }
                }
            }
        }else {
            ERROR("07", id);
        }
    }

    private void NOTIFICATION(String note, Integer id, User toSendUser) {
        if (users.isLoggedIn(toSendUser.getName())) {
            users.getconnections().send(id, note + '\0');
        } else {
            toSendUser.Note.add(note);
        }
    }

    private void ACK(String opcode, int id, String optinal) {
        if (optinal != null)
            users.getconnections().send(id, "10" + opcode + optinal);
        else
            users.getconnections().send(id, "10" + opcode);
    }

    private void ERROR(String opcode, int id) {
        users.getconnections().send(id, "11" + opcode);

    }

    public void BLOCK(String nameblocking, int id) {
        User blocking = users.getUserByConnectionId(id);
        User blocked = users.getUser(nameblocking);
        if (blocking == null || !users.isRegistered(nameblocking) || !users.isLoggedIn(blocking.getName()) || blocking.IsBlocked(blocked))
            ERROR("12", id);
        else {
            blocking.blockUser(blocked);
            blocked.blockUser(blocking);

        }
    }

    private byte[] shortToBytes(short num) {
        byte[] bytesArr = new byte[2];
        bytesArr[0] = (byte) ((num >> 8) & 0xFF);
        bytesArr[1] = (byte) (num & 0xFF);
        return bytesArr;
    }

    private String getStats(User u) {
        String age = new String(shortToBytes(u.getAge()), 0, 2, StandardCharsets.UTF_8);
        String numPost = new String(shortToBytes(u.GetnumofPOst()), 0, 2, StandardCharsets.UTF_8);
        String followers = new String(shortToBytes(u.getNumOfFollowers()), 0, 2, StandardCharsets.UTF_8);
        String following = new String(shortToBytes(u.getNumOfFollowing()), 0, 2, StandardCharsets.UTF_8);
        return age + numPost + followers + following;
    }

    public String filterPM(String message) {
        String ans = message + " ";
        for (String bannedWord : banned) {
            ans = ans.replaceAll(String.format("(%s)([. ,?!:]+)", bannedWord), "<filtered>$2");
        }
        return ans;

    }
}
