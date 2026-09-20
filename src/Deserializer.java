import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class Deserializer {

    private ByteArrayInputStream bais;
    private DataInputStream inputStream;

    public Deserializer(byte[] bytes){
        this.bais = new ByteArrayInputStream(bytes);
        this.inputStream = new DataInputStream(bais);
    }

    public Object decode() throws IOException {
        byte tag = inputStream.readByte();
        switch (tag){
            case Tag.STRING -> {
                int length = inputStream.readInt();
                byte[] str = inputStream.readNBytes(length);
                return new String(str, StandardCharsets.UTF_8);
            }
            case Tag.INTEGER -> {
                Integer value = inputStream.readInt();
                return value;
            }
            case Tag.BOOLEAN -> {
                Boolean value = inputStream.readBoolean();
                return value;
            }
            case Tag.ARRAY -> {
                int length = inputStream.readInt();
                List<Object> list = new ArrayList<>();
                for (int i = 0; i < length; i++) {
                    list.add(decode());
                }
                return list;
            }
            default -> {
                return null;
            }
        }
    }
}
