package yoo_chill;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Scanner;

public class client {

    static Scanner in = new Scanner(System.in);

    public static void event_loop(SelectionKey key) throws IOException{
        Selector selector = (Selector)key.selector();
        SocketChannel channel = (SocketChannel)key.channel();
         while(true){
            selector.select();

            Iterator<SelectionKey> it = selector.selectedKeys().iterator();
            if(it.hasNext()){

                SelectionKey key1 = it.next();
                it.remove();

                if(key.isWritable()){
                    writeProcess(key1);
                }
                if(key.isReadable()){
                    parser(key1);
                }

            }
         }
    }

    private static void parser(SelectionKey key) throws IOException{
        System.out.println("read");
        SocketChannel client = (SocketChannel)key.channel();
        ByteBuffer buffer = ByteBuffer.allocate(8196);
        while(true){

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

            if(buffer.position() < 4){
                continue;
            }
            buffer.flip();
            Integer length = buffer.getInt();
            
            int offset = 0;

            while(offset < length){
                int bytesRead = client.read(buffer);
                System.out.println(new String(buffer.array(),StandardCharsets.UTF_8));

                if(bytesRead == 0){
                    key.interestOps(
                        key.interestOps() | SelectionKey.OP_READ
                    );
                    return;
                }

                if(bytesRead == -1){
                    key.interestOps(
                        (key.interestOps() | SelectionKey.OP_WRITE) & ~SelectionKey.OP_READ
                    );
                    return;
                }

                offset+=bytesRead;
            }
        }
        
    }

    public static ByteBuffer msgConverter(String msg){

        byte[] bytes = msg.getBytes(StandardCharsets.UTF_8);

        ByteBuffer buffer = ByteBuffer.allocate(bytes.length + 4);

        buffer.putInt(msg.length());
        buffer.put(bytes);
        
        return buffer;
    }

    public static void writeProcess(SelectionKey key) throws IOException{

        String msg = in.next();
        SocketChannel channel = (SocketChannel)key.channel();

        ByteBuffer buffer = msgConverter(msg);
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
                key.interestOps() | SelectionKey.OP_READ 
            );

            key.interestOps(
                key.interestOps() & ~SelectionKey.OP_WRITE
            );
        }
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
