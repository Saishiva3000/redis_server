package yoo_chill;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Connection {
    
    private SocketChannel channel;
    private ByteBuffer readBuffer = ByteBuffer.allocate(8196);
    private Queue<ByteBuffer> pendingBuffers = new ConcurrentLinkedQueue<>();
    private State state = State.READ_LENGTH;
    private String response;
    private int length = 0;
    private int offset = 0;

    public SocketChannel getChannel(){
        return this.channel;
    }

    public void setChannel(SocketChannel channel){
        this.channel= channel;
    }

    public ByteBuffer getByteByffer(){
        return this.readBuffer;
    }

    public void setByteBuffer(ByteBuffer buffer){
        this.readBuffer= buffer;
    }

    public Queue<ByteBuffer> getPendingBuffers(){
        return this.pendingBuffers;
    }

    public void setState(State state){
        this.state = state;
    }

    public State getState(){
        return this.state;
    }

    public int getLength(){
        return length;
    }

    public void setLength(int len){
        this.length = len;
    }

    public int getOffset(){
        return offset;
    }

    public void setOffset(int len){
        this.offset = len;
    }

    public void addBuffer(ByteBuffer buffer){
        pendingBuffers.add(buffer);
    }

    public enum State {
        READ_LENGTH,
        READ_RESPONSECODE,
        READ_BODY
    }

    public String getResponse(){
        return response;
    }

    public void  setResponse(String res){
        this.response = res;
    }

}   
