package dev.kandv.kango.integrations.controllers;

import dev.kandv.kango.dtos.TagDTO;
import dev.kandv.kango.models.enums.Color;
import dev.kandv.kango.models.enums.Visibility;
import io.restassured.http.ContentType;
import io.restassured.response.Response;

import static io.restassured.RestAssured.given;

public class TagRestControllerUtils {

    static String label = "Example Label";
    static Color color = Color.GREEN;
    static Visibility visibility = Visibility.GLOBAL;

    static long actionCreateTag() {
        return actionCreateTag(label, color, visibility);
    }

    static long actionCreateTag(String label, Color color, Visibility visibility) {
        TagDTO tagDTO = new TagDTO(label, color, visibility);

        return ((Integer)
                given()
                        .contentType(ContentType.JSON)
                        .body(tagDTO)
                        .when()
                        .post("/api/tags")
                        .then()
                        .statusCode(201)
                        .extract()
                        .path("id")).longValue();
    }

    static Response actionGetTagById(long tagId) {
        return  given()
                .pathParams("id", tagId)
                .when()
                .get("/api/tags/{id}", tagId);
    }

}
