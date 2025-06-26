package dev.kandv.kango.e2e.controllers;

import dev.kandv.kango.dtos.CardDTO;
import dev.kandv.kango.dtos.TableDTO;
import io.restassured.http.ContentType;
import io.restassured.response.Response;

import java.util.ArrayList;
import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

public class TableRestControllerUtils {

    static String name = "Example Table";
    static List<CardDTO> cards = new ArrayList<>();

    static long actionCreateTable() {
        return actionCreateTable(name, cards);
    }

    static long actionCreateTable(String name, List<CardDTO> cards) {
        TableDTO tableDTO = new TableDTO(name, cards);

        return ((Integer)
                given()
                        .contentType(ContentType.JSON)
                        .body(tableDTO)
                        .when()
                        .post("/api/tables")
                        .then()
                        .statusCode(201)
                        .extract()
                        .path("id")).longValue();
    }

    static Response actionGetSpecificTableById(Long id){
        return  given()
                .pathParams("id", id)
                .when()
                .get("/api/tables/{id}", id);
    }

    static void actionAddCardToTable(Long tableId, Long cardId) {
        actionAddCardToTable(tableId, cardId, 1);
    }

    static void actionAddCardToTable(Long tableId, Long cardId, int amount) {
        given()
                .contentType(ContentType.JSON)
                .body(cardId)
                .pathParams("id", tableId)
                .when()
                .post("/api/tables/{id}/cards")
                .then()
                .statusCode(201)
                .body("cardList.size()", equalTo(amount));
    }
}
