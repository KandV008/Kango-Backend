package dev.kandv.kango.e2e.controllers;

import io.restassured.http.ContentType;
import io.restassured.response.Response;

import static io.restassured.RestAssured.given;

public class StateRestControllerUtils {

    static long actionCreateState() {
        return ((Integer)
                given()
                        .contentType(ContentType.JSON)
                        .when()
                        .post("/api/state")
                        .then()
                        .statusCode(201)
                        .extract()
                        .path("id")).longValue();
    }

    static Response actionGetState(){
        return  given()
                .when()
                .get("/api/state");
    }
}
