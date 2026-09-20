import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class Serializer {

    private ByteArrayOutputStream baos = new ByteArrayOutputStream();
    private DataOutputStream out = new DataOutputStream(baos);

    public byte[] encode(Object value) throws IOException {
        if (value instanceof Integer i) {
            out.writeByte(Tag.INTEGER);
            out.writeInt(i);

        } else if (value instanceof String s) {
            byte[] bytes = s.getBytes(StandardCharsets.UTF_8);
            out.writeByte(Tag.STRING);
            out.writeInt(bytes.length);
            out.write(bytes);

        } else if (value instanceof Boolean) {
            out.writeByte(Tag.BOOLEAN);
            out.writeBoolean((boolean)value);

        } else if (value instanceof List<?> list) {
            out.writeByte(Tag.ARRAY);
            out.writeInt(list.size());
            for (Object element : list) {
                byte[] bytes = encode(element);
            }
        }
        out.flush();
        return baos.toByteArray();
    }
}
