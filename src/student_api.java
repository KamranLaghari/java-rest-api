
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.net.URLDecoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class student_api {

    static void handleStudents(HttpExchange exchange)
            throws IOException {

        try {
            String method = exchange.getRequestMethod();

            switch (method) {
                case "GET":
                    getStudents(exchange);
                    break;

                case "POST":
                    createStudent(exchange);
                    break;

                case "PUT":
                    updateStudent(exchange);
                    break;

                case "DELETE":
                    deleteStudent(exchange);
                    break;

                default:
                    sendResponse(exchange, 405,
                            "{\"error\":\"Method not allowed\"}");
            }

        } catch (IllegalArgumentException e) {
            sendResponse(exchange, 400,
                    "{\"error\":\"" + escapeJson(e.getMessage()) + "\"}");

        } catch (SQLException e) {
            e.printStackTrace();

            sendResponse(exchange, 500,
                    "{\"error\":\"Database error. Check server console.\"}");

        } catch (Exception e) {
            e.printStackTrace();

            sendResponse(exchange, 500,
                    "{\"error\":\"Internal server error\"}");
        }
    }

    // GET: Retrieve all students
    private static void getStudents(HttpExchange exchange)
            throws SQLException, IOException {

        String sql = "SELECT id, name, email, age FROM students";

        try (Connection con = student_db.getConnect();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            StringBuilder json = new StringBuilder("[");
            boolean first = true;

            while (rs.next()) {
                if (!first) {
                    json.append(",");
                }

                json.append("{")
                        .append("\"id\":").append(rs.getInt("id"))
                        .append(",\"name\":\"")
                        .append(escapeJson(rs.getString("name")))
                        .append("\",\"email\":\"")
                        .append(escapeJson(rs.getString("email")))
                        .append("\",\"age\":")
                        .append(rs.getInt("age"))
                        .append("}");

                first = false;
            }

            json.append("]");
            sendResponse(exchange, 200, json.toString());
        }
    }

    // POST: Create a student
    private static void createStudent(HttpExchange exchange)
            throws SQLException, IOException {

        String body = readBody(exchange);

        String name = getJsonString(body, "name");
        String email = getJsonString(body, "email");
        int age = getJsonInt(body, "age");

        validateStudent(name, email, age);

        String sql =
                "INSERT INTO students (name, email, age) VALUES (?, ?, ?)";

        try (Connection con = student_db.getConnect();
             PreparedStatement ps = con.prepareStatement(
                     sql, PreparedStatement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, name);
            ps.setString(2, email);
            ps.setInt(3, age);

            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    int id = keys.getInt(1);

                    String json = studentJson(id, name, email, age);
                    sendResponse(exchange, 201, json);
                } else {
                    sendResponse(exchange, 500,
                            "{\"error\":\"Student creation failed\"}");
                }
            }
        }
    }

    // PUT: Update a student using ?id=1
    private static void updateStudent(HttpExchange exchange)
            throws SQLException, IOException {

        int id = getId(exchange);

        String body = readBody(exchange);

        String name = getJsonString(body, "name");
        String email = getJsonString(body, "email");
        int age = getJsonInt(body, "age");

        validateStudent(name, email, age);

        String sql =
                "UPDATE students SET name = ?, email = ?, age = ? WHERE id = ?";

        try (Connection con = student_db.getConnect();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, name);
            ps.setString(2, email);
            ps.setInt(3, age);
            ps.setInt(4, id);

            int rows = ps.executeUpdate();

            if (rows == 0) {
                sendResponse(exchange, 404,
                        "{\"error\":\"Student not found\"}");
                return;
            }

            sendResponse(exchange, 200,
                    studentJson(id, name, email, age));
        }
    }

    // DELETE: Delete a student using ?id=1
    private static void deleteStudent(HttpExchange exchange)
            throws SQLException, IOException {

        int id = getId(exchange);

        String sql = "DELETE FROM students WHERE id = ?";

        try (Connection con = student_db.getConnect();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);

            int rows = ps.executeUpdate();

            if (rows == 0) {
                sendResponse(exchange, 404,
                        "{\"error\":\"Student not found\"}");
            } else {
                exchange.getResponseHeaders().set(
                        "Content-Type", "application/json; charset=UTF-8");

                exchange.sendResponseHeaders(204, -1);
                exchange.close();
            }
        }
    }

    // Read request body
    private static String readBody(HttpExchange exchange)
            throws IOException {

        String body = new String(
                exchange.getRequestBody().readAllBytes(),
                StandardCharsets.UTF_8);

        if (body.isBlank()) {
            throw new IllegalArgumentException(
                    "Request body is required");
        }

        return body;
    }

    // Read a string from a simple JSON request body
    private static String getJsonString(String body, String field) {

        Pattern pattern = Pattern.compile(
                "\"" + Pattern.quote(field)
                        + "\"\\s*:\\s*\"((?:\\\\.|[^\"\\\\])*)\"");

        Matcher matcher = pattern.matcher(body);

        if (!matcher.find()) {
            throw new IllegalArgumentException(
                    "Missing string field: " + field);
        }

        String value = matcher.group(1)
                .replace("\\\"", "\"")
                .replace("\\\\", "\\");

        if (value.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    field + " cannot be empty");
        }

        return value;
    }

    // Read an integer from a JSON request body
    private static int getJsonInt(String body, String field) {

        Pattern pattern = Pattern.compile(
                "\"" + Pattern.quote(field)
                        + "\"\\s*:\\s*(-?\\d+)");

        Matcher matcher = pattern.matcher(body);

        if (!matcher.find()) {
            throw new IllegalArgumentException(
                    "Missing integer field: " + field);
        }

        try {
            return Integer.parseInt(matcher.group(1));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                    "Invalid integer field: " + field);
        }
    }

    private static void validateStudent(
            String name, String email, int age) {

        if (name.length() > 100) {
            throw new IllegalArgumentException(
                    "Name must be at most 100 characters");
        }

        if (email.length() > 150
                || !email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            throw new IllegalArgumentException(
                    "Enter a valid email address");
        }

        if (age < 1) {
            throw new IllegalArgumentException(
                    "Age must be at least 1");
        }
    }

    private static int getId(HttpExchange exchange) {

        String query = exchange.getRequestURI().getRawQuery();

        if (query != null) {
            for (String parameter : query.split("&")) {
                String[] pair = parameter.split("=", 2);

                if (pair.length == 2 && pair[0].equals("id")) {
                    try {
                        int id = Integer.parseInt(
                                URLDecoder.decode(
                                        pair[1],
                                        StandardCharsets.UTF_8));

                        if (id > 0) {
                            return id;
                        }
                    } catch (NumberFormatException ignored) {
                        // Return the error below.
                    }

                    break;
                }
            }
        }

        throw new IllegalArgumentException(
                "Provide a valid student ID, e.g. ?id=1");
    }

    private static String studentJson(
            int id, String name, String email, int age) {

        return "{"
                + "\"id\":" + id
                + ",\"name\":\"" + escapeJson(name) + "\""
                + ",\"email\":\"" + escapeJson(email) + "\""
                + ",\"age\":" + age
                + "}";
    }

    private static String escapeJson(String value) {
        return value.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    private static void sendResponse(
            HttpExchange exchange,
            int status,
            String response) throws IOException {

        byte[] bytes = response.getBytes(StandardCharsets.UTF_8);

        exchange.getResponseHeaders().set(
                "Content-Type", "application/json; charset=UTF-8");

        exchange.sendResponseHeaders(status, bytes.length);

        try (var output = exchange.getResponseBody()) {
            output.write(bytes);
        }
    }
}
