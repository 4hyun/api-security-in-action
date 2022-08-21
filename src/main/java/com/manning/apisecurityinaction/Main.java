package com.manning.apisecurityinaction;

import com.google.common.util.concurrent.*;
import com.manning.apisecurityinaction.controller.AuditController;
import com.manning.apisecurityinaction.controller.SpaceController;
import com.manning.apisecurityinaction.controller.UserController;

import org.dalesbred.Database;
import org.h2.jdbcx.JdbcConnectionPool;
import org.json.JSONException;
import org.json.JSONObject;

import java.nio.file.Files;
import java.nio.file.Paths;

import static spark.Spark.*;

import spark.*;

public class Main {

    public static void main(String... args) throws Exception {
        secure("localhost.p12", "changeit", null, null);
        Spark.staticFiles.location("/public");
        var datasource = JdbcConnectionPool.create("jdbc:h2:mem:natter", "natter", "password");
        var database = Database.forDataSource(datasource);
        createTables(database);
        datasource = JdbcConnectionPool.create("jdbc:h2:mem:natter", "natter_api_user", "password");
        database = Database.forDataSource(datasource);

        var spaceController = new SpaceController(database);
        var userController = new UserController(database);
        var auditController = new AuditController(database);

        var rateLimiter = RateLimiter.create(2.0d);
        before((request, response) -> {
            if (!rateLimiter.tryAcquire()) {
                response.header("Retry-After", "2");
                halt(429);
            }
        });

        before((request, response) -> {
            if (request.requestMethod().equals("POST") &&
                    !"application/json".equals(request.contentType())) {
                halt(415, new JSONObject().put(
                        "error", "Only application/json supported").toString());
            }
        });
        before(userController::authenticate);
        before(auditController::auditRequestStart);
        before("/spaces", userController::requireAuthentication);

        post("/spaces", spaceController::createSpace);
        post("/users", userController::registerUser);
        get("/logs", auditController::readAuditLog);

        after((request, response) -> response.type("application/json"));

        afterAfter(auditController::auditRequestEnd);
        afterAfter((request, response) -> {
            response.type("application/json;charset=utf-8");
            response.header("X-Content-Type-Options", "nosniff");
            response.header("X-Frame-Options", "DENY");
            response.header("X-XSS-Protection", "0");
            response.header("Cache-Control", "no-store");
            response.header("Content-Security-Policy",
                    "default-src 'none'; frame-ancestors 'none'; sandbox");
            response.header("Server", "");
        });

        internalServerError(new JSONObject().put("error", "internal server error").toString());
        notFound(new JSONObject().put("error", "not found").toString());

        exception(IllegalArgumentException.class, Main::badRequest);
        exception(JSONException.class, Main::badRequest);

    }

    private static void badRequest(Exception ex, Request request, Response response) {
        response.status(400);
        response.body("{\"error\": \"" + ex.getMessage() + "\"}");
    }

    private static void createTables(Database database) throws Exception {
        var path = Paths.get(Main.class.getResource("/schema.sql").toURI());
        database.update(Files.readString(path));
    }

}
