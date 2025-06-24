package dev.kandv.kango.integrations.controllers;

import dev.kandv.kango.dtos.DashboardDTO;
import io.restassured.http.ContentType;
import io.restassured.response.Response;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

public class DashboardRestControllerUtils {

    static String dashboardName = "Example Dashboard";

    static long actionCreateDashboard() {
        return actionCreateDashboard(dashboardName);
    }

    static long actionCreateDashboard(String name) {
        DashboardDTO dashboardDTO = new DashboardDTO(name);

        return ((Integer)
                given()
                        .contentType(ContentType.JSON)
                        .body(dashboardDTO)
                        .when()
                        .post("/api/dashboards")
                        .then()
                        .statusCode(201)
                        .extract()
                        .path("id")).longValue();
    }

    static Response actionGetSpecificDashboardById(Long id){
        return  given()
                .pathParams("id", id)
                .when()
                .get("/api/dashboards/{id}", id);
    }

    static void actionAddTableToDashboard(Long dashboardId, Long tableId) {
        actionAddTableToDashboard(dashboardId, tableId, 1);
    }

    static void actionAddTableToDashboard(Long dashboardId, Long tableId, int amount) {
        given()
                .contentType(ContentType.JSON)
                .body(tableId)
                .pathParams("id", dashboardId)
                .when()
                .post("/api/dashboards/{id}/tables", dashboardId)
                .then()
                .statusCode(201)
                .body("tableList.size()", equalTo(amount));
    }

    static void actionAddTemplateCardToDashboard(Long dashboardId, Long cardId) {
        actionAddTemplateCardToDashboard(dashboardId, cardId, 1);
    }

    static void actionAddTemplateCardToDashboard(Long dashboardId, Long cardId, int amount) {
        given()
                .contentType(ContentType.JSON)
                .body(cardId)
                .pathParams("id", dashboardId)
                .when()
                .post("/api/dashboards/{id}/template-cards")
                .then()
                .statusCode(201)
                .body("templateCardList.size()", equalTo(amount));
    }


}
