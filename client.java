package yoo_chill;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.Thread.State;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Queue;
import java.util.Scanner;

public class client {

    static Scanner in = new Scanner(System.in);
    static int MSG_MAX = 8196;

    public static void event_loop(SelectionKey key) throws IOException{
        Selector selector = (Selector)key.selector();
        SocketChannel channel = (SocketChannel)key.channel();
         while(true){
            selector.select();

            Iterator<SelectionKey> it = selector.selectedKeys().iterator();
            if(it.hasNext()){

                SelectionKey key1 = it.next();
                it.remove();

                if(key1.isWritable()){
                    writeProcess(key1);
                }
                if(key1.isReadable()){
                    readFull(key1);
                    String msg = parse(key1);
                    System.out.println(msg);
                }

            }
         }
    }

    private static void readFull(SelectionKey key) throws IOException{
        System.out.println("read");
        SocketChannel client = (SocketChannel)key.channel();
        Connection connection = (Connection)key.attachment();
        ByteBuffer buffer = connection.getByteByffer();
        if(connection.getState().equals(Connection.State.READ_LENGTH)){
            int lenRead = client.read(buffer);
            if(lenRead == -1){
                key.interestOps(
                    (key.interestOps() | SelectionKey.OP_WRITE) & ~SelectionKey.OP_READ
                );
                return;
            }

            if(lenRead == 0){
                key.interestOps(
                    key.interestOps() | SelectionKey.OP_READ
                );
                return;
            }

            if(buffer.position()<4){
                return ;
            }
            buffer.flip();
            int length = buffer.getInt();
            connection.setLength(length);
            connection.setOffset(connection.getOffset() + 4);
            buffer.compact();
            if(length<4 || length>8196){
                throw new Exception();
            }
            connection.setState(Connection.State.READ_RESPONSECODE);
        }
        if(connection.getState().equals(Connection.State.READ_RESPONSECODE)){
            int lenRead = client.read(buffer);
            if(lenRead == -1){
                key.interestOps(
                    (key.interestOps() | SelectionKey.OP_WRITE) & ~SelectionKey.OP_READ
                );
                return;
            }

            if(lenRead == 0){
                key.interestOps(
                    key.interestOps() | SelectionKey.OP_READ
                );
                return;
            }

            if(buffer.position()<4){
                return ;
            }
            buffer.flip();
            int resCode = buffer.getInt();
            connection.setOffset(connection.getOffset() + 4);
            buffer.compact();
            connection.setState(yoo_chill.Connection.State.READ_BODY);
        }

        while (connection.getOffset() < connection.getLength()) {
            int lenRead = client.read(buffer);
            if(lenRead == -1){
                key.interestOps(
                    (key.interestOps() | SelectionKey.OP_WRITE) & ~SelectionKey.OP_READ
                );
                return ;
            }
            if(lenRead == 0){
                key.interestOps(
                    key.interestOps() | SelectionKey.OP_READ
                );
                return ;
            }
            buffer.flip();
            ByteBuffer parseBuffer = ByteBuffer.wrap(buffer.array(), 0, lenRead);
            connection.addBuffer(buffer);
            buffer.clear();
            connection.setOffset(connection.getOffset() + lenRead);
        }
        
    }

    public static ByteBuffer formatter(String msg){
        String[] tokens = msg.trim().split("\\s+");
        byte[][] encodedArgs = new byte[tokens.length][];
        int bodylength = 4;

        for(int i=0;i<tokens.length;i++){
            encodedArgs[i] = tokens[i].getBytes(StandardCharsets.UTF_8);
            bodylength+= 4+encodedArgs[i].length;
        }

        ByteBuffer buffer = ByteBuffer.allocate(4+bodylength);
        buffer.putInt(bodylength);
        buffer.putInt(encodedArgs.length);
        
        for(byte[] args : encodedArgs){
            buffer.putInt(args.length);
            buffer.put(args);
        }
        return buffer;
    }

    public static void writeProcess(SelectionKey key) throws IOException{

        String msg = in.nextLine();
        SocketChannel channel = (SocketChannel)key.channel();

        ByteBuffer buffer = formatter(msg);
        buffer.flip();

        while(buffer.hasRemaining()){
            int write = channel.write(buffer);

            if(write == -1){
                key.interestOps(
                    (key.interestOps() | SelectionKey.OP_READ) & ~SelectionKey.OP_WRITE
                );
                return;
            }

            if(write == 0){
                key.interestOps(
                    (key.interestOps() | SelectionKey.OP_WRITE) & ~SelectionKey.OP_READ
                );
                return;
            }
        }
        if(!buffer.hasRemaining()){
            key.interestOps(
                (key.interestOps() | SelectionKey.OP_READ ) & ~SelectionKey.OP_WRITE
            );
            channel.shutdownOutput();
        }
    }

    public static  String parse(SelectionKey key){
        Connection connection = (Connection)key.attachment();
        Queue<ByteBuffer> penBuffers = connection.getPendingBuffers();
        StringBuilder builder = new StringBuilder();
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        ByteBuffer extraBuffer = ByteBuffer.allocate(MSG_MAX);
        int extraLength = 0;
        while(!penBuffers.isEmpty()){
            ByteBuffer buffer = penBuffers.peek();
            if(extraBuffer.position()>0){
                byte[] wordByte = new byte[extraLength];
                int index = 0;
                while (index < wordLength && buffer.hasRemaining()) {
                    wordByte[index] = buffer.get();
                    index++;
                }
                builder.append(new String(wordByte,StandardCharsets.UTF_8));
                builder.append(" ");
                extraBuffer.clear();
                extraLength = 0;
            }
            int wordLength = buffer.getInt();
            byte[] wordByte = new byte[wordLength];
            int index = 0;
            while (index < wordLength && buffer.hasRemaining()) {
                wordByte[index] = buffer.get();
                index++;
            }
            if(index < wordLength){
                extraBuffer.put(wordByte, 0, index);
                extraLength = wordLength - index - 1;
            }
            else{
                builder.append(new String(wordByte,StandardCharsets.UTF_8));
                builder.append(" ");
            }
        }
        return builder.toString();
    }

    public static void main(String[] args) throws IOException {
        SocketChannel channel = SocketChannel.open();
        Selector selector = Selector.open();
        channel.configureBlocking(false);
        channel.connect(new InetSocketAddress("localhost",4050));
        channel.register(selector,SelectionKey.OP_WRITE);
        while(!channel.finishConnect()){
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        event_loop(channel.keyFor(selector));
    }
}
