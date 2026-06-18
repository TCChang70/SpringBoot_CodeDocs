package com.example;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class EmployeeApiTest {

    private static String jwtToken;
    private static int createdId;

    @BeforeAll
    static void setup() {
        RestAssured.baseURI  = "http://localhost";
        RestAssured.port     = 8080;
        RestAssured.basePath = "/jaxrs-demo/api";
    }

    @Test
    @Order(1)
    void testLogin() {
        jwtToken = given()
            .contentType(ContentType.JSON)
            .body("{\"username\":\"admin\",\"password\":\"admin123\"}")
        .when()
            .post("/auth/login")
        .then()
            .statusCode(200)
            .body("token",    notNullValue())
            .body("username", equalTo("admin"))
            .body("role",     equalTo("ADMIN"))
            .extract()
            .path("token");
    }

    @Test
    @Order(2)
    void testGetAllEmployees() {
        given()
            .header("Authorization", "Bearer " + jwtToken)
        .when()
            .get("/employees")
        .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("$", not(empty()))
            .header("X-Total-Count", notNullValue());
    }

    @Test
    @Order(3)
    void testCreateEmployee() {
        String newEmp = """
            {
              "name": "Test User",
              "email": "testuser@example.com",
              "department": "QA",
              "salary": 60000
            }
            """;

        Response response = given()
            .header("Authorization", "Bearer " + jwtToken)
            .contentType(ContentType.JSON)
            .body(newEmp)
        .when()
            .post("/employees")
        .then()
            .statusCode(201)
            .header("Location", containsString("/api/employees/"))
            .body("name",  equalTo("Test User"))
            .body("email", equalTo("testuser@example.com"))
            .extract().response();

        createdId = response.path("id");
    }

    @Test
    @Order(4)
    void testGetEmployeeById() {
        given()
            .header("Authorization", "Bearer " + jwtToken)
        .when()
            .get("/employees/" + createdId)
        .then()
            .statusCode(200)
            .body("id",   equalTo(createdId))
            .body("name", equalTo("Test User"));
    }

    @Test
    @Order(5)
    void testUpdateEmployee() {
        String updateBody = """
            {
              "name": "Updated User",
              "email": "testuser@example.com",
              "department": "QA",
              "salary": 65000
            }
            """;

        given()
            .header("Authorization", "Bearer " + jwtToken)
            .contentType(ContentType.JSON)
            .body(updateBody)
        .when()
            .put("/employees/" + createdId)
        .then()
            .statusCode(200)
            .body("name",   equalTo("Updated User"))
            .body("salary", equalTo(65000.0f));
    }

    @Test
    @Order(6)
    void testDeleteEmployee() {
        given()
            .header("Authorization", "Bearer " + jwtToken)
        .when()
            .delete("/employees/" + createdId)
        .then()
            .statusCode(204);
    }

    @Test
    @Order(7)
    void testGetDeletedEmployee() {
        given()
            .header("Authorization", "Bearer " + jwtToken)
        .when()
            .get("/employees/" + createdId)
        .then()
            .statusCode(404);
    }

    @Test
    @Order(8)
    void testUnauthorizedAccess() {
        given()
        .when()
            .get("/employees")
        .then()
            .statusCode(401);
    }

    @Test
    @Order(9)
    void testCreateWithEmptyName() {
        given()
            .header("Authorization", "Bearer " + jwtToken)
            .contentType(ContentType.JSON)
            .body("{\"name\":\"\",\"email\":\"x@example.com\",\"department\":\"IT\",\"salary\":50000}")
        .when()
            .post("/employees")
        .then()
            .statusCode(400);
    }
}
