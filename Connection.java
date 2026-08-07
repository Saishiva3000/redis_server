package yoo_chill;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Connection {
    
    private SocketChannel channel;
    private ByteBuffer buffer = ByteBuffer.allocate(8196);
    private Queue<ByteBuffer> pendingBuffers = new ConcurrentLinkedQueue<>();

    public SocketChannel getChannel(){
        return this.channel;
    }

    public void setChannel(SocketChannel channel){
        this.channel= channel;
    }

    public ByteBuffer getByteByffer(){
        return this.buffer;
    }

    public void setByteBuffer(ByteBuffer buffer){
        this.buffer= buffer;
    }

    public Queue<ByteBuffer> getPendingBuffers(){
        return this.pendingBuffers;
    }

}   
