import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.HashMap;


public class Test {

    public static void main(String[] args) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();

        HashMap<String,Object> user = new HashMap<>();

        user.put("name","Pavlo");
        user.put("age", 24);
        user.put("city", "Kyiv");
        ArrayList<String> mas = new ArrayList<>();
        mas.add("lada");
        mas.add("kia");
        user.put("cars",mas);

        String answer = mapper.writeValueAsString(user);

        System.out.println(answer);
        //{, "surname": "Movchan", "age": 24, "city": "Kyiv", "cars": ["lada", "kia"]}

    }
}
