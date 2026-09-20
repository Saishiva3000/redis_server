import java.util.List;

public class CommandExecutor {

    private IncrementalHashMap<String,String> hashMap = new IncrementalHashMap<>();
    public Object execute(List<String> command){
        String operation = command.get(0);

        return switch (operation){
            case "get" -> get(command);
            case "set"-> set(command);
            case "del"-> del(command);
            case "keys"-> keys(command);
            default -> {
                throw new IllegalArgumentException("");
            }
        };
    }

    public Object get(List<String> cmds){
        String key = cmds.get(1);
        return hashMap.get(key);
    }

    public Object del(List<String> cmds){
        String key = cmds.get(1);
        return hashMap.remove(key);
    }

    public Object set(List<String> cmds){
        String key = cmds.get(1);
        String value = cmds.get(2);
        return hashMap.put(key,value);
    }

    public Object keys(List<String> cmds){

    }
}
