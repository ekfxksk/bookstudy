package com.kyu.websocket;

import com.kyu.db.Database;
import com.kyu.model.User;
import com.kyu.util.HttpRequestUtils;
import com.kyu.util.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class RequestHandler extends Thread {

    private static final Logger log = LoggerFactory.getLogger(RequestHandler.class);

    private Socket connection;

    RequestHandler(Socket connection) {
        this.connection = connection;
    }

    public void run() {
        log.debug("New Client Connect! Connected IP : {} , Port : {}", connection.getInetAddress(), connection.getPort());

        try(InputStream in = connection.getInputStream();
            OutputStream out = connection.getOutputStream()) {
            BufferedReader br = new BufferedReader(new InputStreamReader(in, "UTF-8"));
            String line = br.readLine();

            if(line == null) return;
            String url = HttpRequestUtils.getUrl(line);

            Map<String, String> headres = new HashMap<>();

            while(!"".equals(line)) {
                log.debug("header : {}", line);
                line = br.readLine();
                String[] headerTokens = line.split(": ");
                if(headerTokens.length == 2) headres.put(headerTokens[0], headerTokens[1]);
            }


            if("/user/create".equals(url)) {
                //int index = url.indexOf("?");
                //String requsetPath = url.substring(0, index);
                //String queryString = url.substring(index + 1);
                //Map<String, String> params = HttpRequestUtils.parseQueryString(queryString);
                //User user = new User(params.get("userId"), params.get("password"), params.get("name") , params.get("email"));

                String requestBody = IOUtils.readData(br, Integer.parseInt(headres.get("Content-Length")));
                log.debug("Request Body : {}", requestBody);
                Map<String, String> params = HttpRequestUtils.parseQueryString(requestBody);
                User user = new User(params.get("userId"), params.get("password"), params.get("name") , params.get("email"));
                Database.addUser(user);
                log.debug("User id {}", user);

                DataOutputStream dos = new DataOutputStream(out);
                response302Header(dos);
            } else if("/user/login".equals(url)) {
                String requestBody = IOUtils.readData(br, Integer.parseInt(headres.get("Content-Length")));
                log.debug("Request Body : {}", requestBody);
                Map<String, String> params = HttpRequestUtils.parseQueryString(requestBody);
                User user = Database.findUserById(params.get("userId"));
                log.debug("User id {}, password{} ", user.getUserId(), user.getPassword());

                if (user == null) {
                    log.debug("User Not Found!");
                    DataOutputStream dos = new DataOutputStream(out);
                    response302Header(dos);
                } else if (user.getUserId().equals(params.get("password"))) {
                    log.debug("login Success");
                    DataOutputStream dos = new DataOutputStream(out);
                    response302HeaderWithCookie(dos, "login=true");
                } else {
                    log.debug("login Password Error");
                    DataOutputStream dos = new DataOutputStream(out);
                    response302Header(dos);
                }
            } else if("/user/list".equals(url)) {
                if (!isLogin(headres.get("Cookie"))) {
                    DataOutputStream dos = new DataOutputStream(out);
                    response302Header(dos);
                    return;
                }

                Collection<User> users = Database.findAll();
                StringBuilder sb = new StringBuilder();
                sb.append("<table border='1'>");
                for (User user : users) {
                    sb.append("<tr>");
                    sb.append("<td>" + user.getUserId() + "</td>");
                    sb.append("<td>" + user.getName() + "</td>");
                    sb.append("<td>" + user.getEmail() + "</td>");
                    sb.append("</tr>");
                }
                sb.append("</table>");
                byte[] body = sb.toString().getBytes();
                DataOutputStream dos = new DataOutputStream(out);
                response200Header(dos, body.length);
                responseBody(dos, body);

            } else if(url.endsWith(".css")) {
                DataOutputStream dos = new DataOutputStream(out);
                byte[] body = Files.readAllBytes(new File("./webapp" + url).toPath()); //"Hello World".getBytes(StandardCharsets.UTF_8);
                response200HeaderWithCss(dos, body.length);
                responseBody(dos, body);
            } else {
                DataOutputStream dos = new DataOutputStream(out);
                byte[] body = Files.readAllBytes(new File("./webapp" + url).toPath()); //"Hello World".getBytes(StandardCharsets.UTF_8);
                response200Header(dos, body.length);
                responseBody(dos, body);
            }
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void response302HeaderWithCookie(DataOutputStream dos, String cookie) {
        try {
            dos.writeBytes("HTTP/1.1 302 Found \r\n");
            dos.writeBytes("Location: /index.html \r\n");
            dos.writeBytes("Set-Cookie: " + cookie);
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void response302Header(DataOutputStream dos) {
        try {
            dos.writeBytes("HTTP/1.1 302 Found \r\n");
            dos.writeBytes("Location: /index.html \r\n");
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void response200Header(DataOutputStream dos, int lengthOfBodyContent) {
        try {
            dos.writeBytes("HTTP/1.1 200 OK \r\n");
            dos.writeBytes("Content-Type: text/html;charset=utf-8 \r\n");
            dos.writeBytes("Content-Length: " + lengthOfBodyContent + "\r\n");
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void response200HeaderWithCss(DataOutputStream dos, int lengthOfBodyContent) {
        try {
            dos.writeBytes("HTTP/1.1 200 OK \r\n");
            dos.writeBytes("Content-Type: text/css;charset=utf-8 \r\n");
            dos.writeBytes("Content-Length: " + lengthOfBodyContent + "\r\n");
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void responseBody(DataOutputStream dos, byte[] body) {
        try {
            dos.write(body, 0, body.length);
            dos.writeBytes("\r\n");
            dos.flush();
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private boolean isLogin(String line) {
        String[] headerTokens = line.split(";");
        Map<String, String> cookies = HttpRequestUtils.parseCookies(headerTokens[0].trim());
        String value = cookies.get("login");
        if (value == null) {
            return false;
        }
        return Boolean.parseBoolean(value);
    }
}
