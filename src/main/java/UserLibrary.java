import static spark.Spark.get;
import static spark.Spark.post;
import static spark.Spark.put;

import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import lombok.Data;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

public class UserLibrary {

    private static final int HTTP_BAD_REQUEST = 400;
    private static final int HTTP_USER_NOT_FOUND = 404;
    private static final int HTTP_USER_EXISTS = 409;
    private static Map users = new HashMap<>();
    
    interface Validable {
        boolean isValid();
        boolean userExists();
    }

    @Data
    static class NewUserPayload {
    	private int id;
        private String firstName;
        private String middleName;
        private String lastName;
        private int age;
        private char gender;
        private String phone;
        private String zip;
        
        public boolean isValid() {
            return id!=0 && age > 0 && (gender=='M'||gender=='F');
        }
        
        public boolean userExists(){
        	return users.containsKey(id);
        }
    }
    
    public static class Model {
        
        @Data
        class User {
            private int id;
            private String firstName;
            private String middleName;
            private String lastName;
            private int age;
            private char gender;
            private String phone;
            private String zip;
        }
        
        public int createUser(NewUserPayload creation){
            User user = new User();
            user.setId(creation.getId());
            user.setFirstName(creation.getFirstName());
            user.setMiddleName(creation.getMiddleName());
            user.setLastName(creation.getLastName());
            user.setAge(creation.getAge());
            user.setGender(creation.getGender());
            user.setPhone(creation.getPhone());
            user.setZip(creation.getZip());
            users.put(user.getId(), user);
            return user.getId();
        }
        
        public int updateUser(NewUserPayload creation){
            User user = (User) users.get(creation.getId());
            user.setId(creation.getId());
            user.setFirstName(creation.getFirstName());
            user.setMiddleName(creation.getMiddleName());
            user.setLastName(creation.getLastName());
            user.setAge(creation.getAge());
            user.setGender(creation.getGender());
            user.setPhone(creation.getPhone());
            user.setZip(creation.getZip());
            users.put(user.getId(), user);
            return user.getId();
        }
        
        public List getAllUsers(){
            return (List) users.keySet().stream().sorted().map((id) -> users.get(id)).collect(Collectors.toList());
        }
    }

    public static String dataToJson(Object data) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.enable(SerializationFeature.INDENT_OUTPUT);
            StringWriter sw = new StringWriter();
            mapper.writeValue(sw, data);
            return sw.toString();
        } catch (IOException e){
            throw new RuntimeException("IOException from a StringWriter?");
        }
    }
    
    public static void main( String[] args) {
        Model model = new Model();
        
        // insert a user (using HTTP post method)
        post("/users", (request, response) -> {
            try {
                ObjectMapper mapper = new ObjectMapper();
                NewUserPayload creation = mapper.readValue(request.body(), NewUserPayload.class);
                if (!creation.isValid()) {
                    response.status(HTTP_BAD_REQUEST);
                    return "";
                }
                if(creation.userExists()){
                	response.status(HTTP_USER_EXISTS);
                    return "";
                }
                int id = model.createUser(creation);
                response.status(200);
                response.type("application/json");
                return id;
            } catch (JsonParseException jpe) {
                response.status(HTTP_BAD_REQUEST);
                return "";
            }
        });
        
     // update a user (using HTTP post method)
        put("/users", (request, response) -> {
            try {
                ObjectMapper mapper = new ObjectMapper();
                NewUserPayload creation = mapper.readValue(request.body(), NewUserPayload.class);
                if (!creation.userExists()) {
                    response.status(HTTP_USER_NOT_FOUND);
                    return "";
                }
                int id = model.updateUser(creation);
                response.status(200);
                response.type("application/json");
                return id;
            } catch (JsonParseException jpe) {
                response.status(HTTP_BAD_REQUEST);
                return "";
            }
        });
        
        // get all users (using HTTP get method)
        get("/users", (request, response) -> {
            response.status(200);
            response.type("application/json");
            return dataToJson(model.getAllUsers());
        });
    }
}