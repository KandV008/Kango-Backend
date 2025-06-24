package dev.kandv.kango.integrations.controllers;

import dev.kandv.kango.dtos.CardDTO;
import dev.kandv.kango.models.enums.CardType;
import io.restassured.http.ContentType;
import io.restassured.response.Response;

import static io.restassured.RestAssured.given;

public class CardRestControllerUtils {

    static String cardTitle = "Example Title";
    static CardType cardType = CardType.NORMAL;

    static long actionCreateCard() {
        return actionCreateCard(cardTitle, cardType);
    }

    static long actionCreateCard(String title, CardType cardType) {
        CardDTO cardDTO = new CardDTO(title, cardType);

        return ((Integer)
                given()
                        .contentType(ContentType.JSON)
                        .body(cardDTO)
                        .when()
                        .post("/api/cards")
                        .then()
                        .statusCode(201)
                        .extract()
                        .path("id")).longValue();
    }

    static Response actionGetSpecificCardById(Long id){
        return  given()
                .pathParams("id", id)
                .when()
                .get("/api/cards/{id}", id);
    }

}
