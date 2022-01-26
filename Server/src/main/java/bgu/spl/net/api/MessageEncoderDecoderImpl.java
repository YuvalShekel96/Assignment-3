package bgu.spl.net.api;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class MessageEncoderDecoderImpl implements MessageEncoderDecoder<String>{
    private byte[] bb=new byte[1<<10];
    private int len =0;

    public String decodeNextByte(byte nextByte)
    {
        if (nextByte==';')
        {
            String ans=popString();
            return ans;
        }
        pushByte(nextByte);
        // the message not end yet
        return null;
    }
    private void pushByte(byte nextByte)
    {
        if (len>=bb.length)
        {
            bb= Arrays.copyOf(bb,len*2);
        }
        bb[len++]=nextByte;
    }
    public String popString (){
        String res=new String(bb,0,len,StandardCharsets.UTF_8);
        len=0;
        return res;
    }
    public byte[] encode(String msg)
    {
        msg=msg+';';
        if(msg.substring(0,2).equalsIgnoreCase("09")){
            return encodeNotification(msg);
        }
        else if ( msg.substring(0,2).equalsIgnoreCase("10")) {
            return encodeAck(msg);
        }
        else {
            return encodeError(msg);
        }
    }

    private byte[] encodeError(String msg) {
        short messageOp=0;
        if (msg.substring(2,4).equalsIgnoreCase("01"))
            messageOp=1;
        else if (msg.substring(2,4).equalsIgnoreCase("02"))
            messageOp=2;
        else if (msg.substring(2,4).equalsIgnoreCase("03"))
            messageOp=3;
        else if (msg.substring(2,4).equalsIgnoreCase("04"))
            messageOp=4;
        else if (msg.substring(2,4).equalsIgnoreCase("05"))
            messageOp=5;
        else if (msg.substring(2,4).equalsIgnoreCase("06"))
            messageOp=6;
        else if (msg.substring(2,4).equalsIgnoreCase("07"))
            messageOp=7;
        else if (msg.substring(2,4).equalsIgnoreCase("08"))
            messageOp=8;
        else if (msg.substring(2,4).equalsIgnoreCase("12"))
            messageOp=12;

        ByteBuffer ansBuff=ByteBuffer.allocate(5);
        ansBuff.put(shortToBytes((short) 11)).put(shortToBytes( messageOp)).put(";".getBytes(StandardCharsets.UTF_8));
        return ansBuff.array();
    }

    private byte[] encodeAck(String msg) {
        short messageOp=0;
        switch (msg.substring(2,4)){
            case "01":
                messageOp=1;
                break;
            case "02":
                messageOp=2;
                break;

            case "03":
                messageOp=3;
                break;
            case "04":
                messageOp=4;
                break;
            case "05":
                messageOp=5;
                break;
            case "06":
                messageOp=6;
                break;
            case "07":
                messageOp=7;
                break;
            case "08":
                messageOp=8;
                break;
            case "12":
                messageOp=12;
                break;
        }

        if(messageOp == 8 || messageOp ==7){
            byte[] optional=msg.substring(4).getBytes(StandardCharsets.UTF_8);
            ByteBuffer OpsBuff=ByteBuffer.allocate(4+optional.length);
            OpsBuff.put(shortToBytes((short) 10)).put(shortToBytes( messageOp)).put(optional);
            return OpsBuff.array();
        }
        ByteBuffer OpsBuff=ByteBuffer.allocate(5);
        OpsBuff.put(shortToBytes((short) 10)).put(shortToBytes( messageOp)).put(";".getBytes(StandardCharsets.UTF_8));
        return OpsBuff.array();
    }

    public byte[] shortToBytes(short num)
    {
        byte[] bytesArr = new byte[2];
        bytesArr[0] = (byte)((num >> 8) & 0xFF);
        bytesArr[1] = (byte)(num & 0xFF);
        return bytesArr;
    }
    private byte[] encodeNotification(String msg){
        byte postOrPM;
        if(msg.charAt(2)=='1')
            postOrPM=1;
        else
            postOrPM=0;
        String content=msg.substring(3);
        byte[] contentBytes=content.getBytes(StandardCharsets.UTF_8);

        ByteBuffer ansBuff= ByteBuffer.allocate(3+contentBytes.length);
        ansBuff.put(shortToBytes((short) 9)).put(postOrPM).put(contentBytes);
        return ansBuff.array();
    }



}