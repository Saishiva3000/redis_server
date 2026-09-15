package yoo_chill;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import yoo_chill.Connection;
public class server {

    ExecutorService executor = Executors.newFixedThreadPool(5);
    
    public void start(){
        Thread thread = new Thread(()->{
        try {
            ServerSocketChannel channel = ServerSocketChannel.open();
            channel.configureBlocking(false);
            channel.bind(new InetSocketAddress("localhost",4050));
            Selector selector = Selector.open();
            channel.register(selector,SelectionKey.OP_ACCEPT);

            while(true){
                selector.select();

                Set<SelectionKey> set = selector.selectedKeys();
                Iterator<SelectionKey> it = set.iterator();
                while(it.hasNext()){

                    SelectionKey key = it.next();
                    it.remove();

                    if(key.isAcceptable()){

                        System.out.println("created socket");

                        ServerSocketChannel server = (ServerSocketChannel)key.channel();
                        SocketChannel client = server.accept();
                        client.configureBlocking(false);
                        Connection connection = new Connection();
                        SelectionKey k = client.register(selector, SelectionKey.OP_READ);
                        connection.setChannel(client);
                        k.attach(connection);

                    }
                    if(key.isReadable()){
                        System.out.println("read");
                        SocketChannel client = (SocketChannel)key.channel();
                         key.interestOps(
                            key.interestOps() & ~SelectionKey.OP_READ
                        );
                        try {
                            readProcess(key, client);
                            selector.wakeup();
                            System.err.println("now it's write time");
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        
                    }
                    
                    if(key.isWritable()){
                        writeProcess(key);   
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    });

    thread.start();
    }

    public void readProcess(SelectionKey key , SocketChannel client) throws IOException{
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

    public void writeProcess(SelectionKey key) throws IOException{
        System.out.println("write");
        SocketChannel client = (SocketChannel)key.channel();
        Connection connection = (Connection) key.attachment();
        Queue<ByteBuffer> penBuffers = connection.getPendingBuffers();
        while(!penBuffers.isEmpty()){
            ByteBuffer buffer = penBuffers.peek();

            String msg = new String(buffer.array(),StandardCharsets.UTF_8);

            System.out.println(msg);
            buffer.flip();

            while(buffer.hasRemaining()){

                int write = client.write(buffer);

                if(write == 0){
                    key.interestOps(
                            key.interestOps() | SelectionKey.OP_WRITE
                        );
                    return; 
                }
                if(write == -1){
                    key.interestOps(
                        (key.interestOps() | SelectionKey.OP_READ) & ~SelectionKey.OP_WRITE
                    );
                    return;
                }
            }
            if(!buffer.hasRemaining()){
                penBuffers.poll();
            }
        }
        client.close();
    }

    public void parser(SelectionKey key , ByteBuffer buffer){
        
    }

    public static void main(String[] args) {
        server server = new server();
        server.start();
    }

}
