import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;

public class JsonPlaceholderApi {

    private static final String API_URL = "https://jsonplaceholder.typicode.com/users";

    public static void main(String[] args) throws Exception {
        // Example usage
        System.out.println("Create user:");
        String newUser = "{\"name\": \"New User\", \"username\": \"newuser\", \"email\": \"newuser@example.com\"}";
        System.out.println(createUser(newUser));

        System.out.println("Update user:");
        String updatedUser = "{\"id\": 1, \"name\": \"Updated User\", \"username\": \"updateduser\", \"email\": \"updateduser@example.com\"}";
        System.out.println(updateUser(1, updatedUser));

        System.out.println("Delete user:");
        System.out.println(deleteUser(1));

        System.out.println("Get all users:");
        System.out.println(getAllUsers());

        System.out.println("Get user by ID:");
        System.out.println(getUserById(1));

        System.out.println("Get user by username:");
        System.out.println(getUserByUsername("Bret"));

        System.out.println("Get comments for the last post of user:");
        getCommentsForLastPost(1);

        System.out.println("Get open tasks for user:");
        System.out.println(getOpenTasksForUser(1));
    }

    public static String createUser(String userJson) throws Exception {
        URL url = new URL(API_URL);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json; utf-8");
        connection.setRequestProperty("Accept", "application/json");
        connection.setDoOutput(true);

        try (OutputStream os = connection.getOutputStream()) {
            byte[] input = userJson.getBytes("utf-8");
            os.write(input, 0, input.length);
        }

        return getResponse(connection);
    }

    public static String updateUser(int userId, String userJson) throws Exception {
        URL url = new URL(API_URL + "/" + userId);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("PUT");
        connection.setRequestProperty("Content-Type", "application/json; utf-8");
        connection.setRequestProperty("Accept", "application/json");
        connection.setDoOutput(true);

        try (OutputStream os = connection.getOutputStream()) {
            byte[] input = userJson.getBytes("utf-8");
            os.write(input, 0, input.length);
        }

        return getResponse(connection);
    }

    public static String deleteUser(int userId) throws Exception {
        URL url = new URL(API_URL + "/" + userId);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("DELETE");

        return getResponse(connection);
    }

    public static String getAllUsers() throws Exception {
        URL url = new URL(API_URL);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");

        return getResponse(connection);
    }

    public static String getUserById(int userId) throws Exception {
        URL url = new URL(API_URL + "/" + userId);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");

        return getResponse(connection);
    }

    public static String getUserByUsername(String username) throws Exception {
        URL url = new URL(API_URL + "?username=" + username);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");

        return getResponse(connection);
    }

    public static void getCommentsForLastPost(int userId) throws Exception {
        URL postsUrl = new URL(API_URL + "/" + userId + "/posts");
        HttpURLConnection postsConnection = (HttpURLConnection) postsUrl.openConnection();
        postsConnection.setRequestMethod("GET");

        String postsResponse = getResponse(postsConnection);
        JSONArray posts = new JSONArray(postsResponse);
        int lastPostId = -1;

        for (int i = 0; i < posts.length(); i++) {
            JSONObject post = posts.getJSONObject(i);
            int postId = post.getInt("id");
            if (postId > lastPostId) {
                lastPostId = postId;
            }
        }

        if (lastPostId == -1) {
            System.out.println("No posts found for user with ID " + userId);
            return;
        }

        URL commentsUrl = new URL("https://jsonplaceholder.typicode.com/posts/" + lastPostId + "/comments");
        HttpURLConnection commentsConnection = (HttpURLConnection) commentsUrl.openConnection();
        commentsConnection.setRequestMethod("GET");

        String commentsResponse = getResponse(commentsConnection);
        String filename = "user-" + userId + "-post-" + lastPostId + "-comments.json";

        try (FileWriter fileWriter = new FileWriter(filename)) {
            fileWriter.write(commentsResponse);
        }

        System.out.println("Comments written to file: " + filename);
    }

    public static String getOpenTasksForUser(int userId) throws Exception {
        URL url = new URL(API_URL + "/" + userId + "/todos");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");

        String response = getResponse(connection);
        JSONArray todos = new JSONArray(response);
        List<JSONObject> openTasks = new ArrayList<>();

        for (int i = 0; i < todos.length(); i++) {
            JSONObject todo = todos.getJSONObject(i);
            if (!todo.getBoolean("completed")) {
                openTasks.add(todo);
            }
        }

        return openTasks.toString();
    }

    private static String getResponse(HttpURLConnection connection) throws Exception {
        int status = connection.getResponseCode();
        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String inputLine;
        StringBuffer content = new StringBuffer();
        while ((inputLine = in.readLine()) != null) {
            content.append(inputLine);
        }
        in.close();
        connection.disconnect();

        return content.toString();
    }
}
