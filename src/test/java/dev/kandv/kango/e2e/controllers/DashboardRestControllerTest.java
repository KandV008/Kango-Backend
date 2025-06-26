package dev.kandv.kango.e2e.controllers;

import dev.kandv.kango.KangoApplication;
import dev.kandv.kango.dtos.DashboardDTO;
import dev.kandv.kango.models.Tag;
import dev.kandv.kango.models.enums.CardType;
import dev.kandv.kango.models.enums.Color;
import dev.kandv.kango.models.utils.AttachedFile;
import dev.kandv.kango.services.DashboardService;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.ArrayList;

import static dev.kandv.kango.controllers.DashboardRestController.INVALID_DASHBOARD_NAME;
import static dev.kandv.kango.controllers.ErrorMessagesRestControllers.*;
import static dev.kandv.kango.e2e.controllers.CardRestControllerUtils.actionCreateCard;
import static dev.kandv.kango.e2e.controllers.CardRestControllerUtils.actionGetSpecificCardById;
import static dev.kandv.kango.e2e.controllers.DashboardRestControllerUtils.*;
import static dev.kandv.kango.e2e.controllers.TableRestControllerUtils.actionCreateTable;
import static dev.kandv.kango.e2e.controllers.TagRestControllerUtils.actionCreateTag;
import static dev.kandv.kango.services.DashboardService.*;
import static dev.kandv.kango.services.ErrorMessagesServices.*;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;

@Testcontainers
@SpringBootTest(
        classes = KangoApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@ExtendWith(SpringExtension.class)
class DashboardRestControllerTest {

    @Container
    static PostgreSQLContainer<?> postgreSQLContainer =
            new PostgreSQLContainer<>("postgres:16-alpine")
                    .withDatabaseName("testdb")
                    .withUsername("test")
                    .withPassword("test");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgreSQLContainer::getJdbcUrl);
        registry.add("spring.datasource.username", postgreSQLContainer::getUsername);
        registry.add("spring.datasource.password", postgreSQLContainer::getPassword);
    }

    @LocalServerPort
    int port;
    @Autowired
    DashboardService dashboardService;

    String name = DashboardRestControllerUtils.dashboardName;

    @BeforeAll
    static void beforeAll(){
        postgreSQLContainer.start();
    }

    @AfterAll
    static void afterAll(){
        postgreSQLContainer.stop();
    }

    @BeforeEach
    void beforeEach() {
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = this.port;
    }

    @AfterEach
    void afterEach() {
        RestAssured.reset();
        this.dashboardService.removeAllDashboards();
    }

    @Test
    void testCreateDashboard(){
        DashboardDTO dashboardDTO = new DashboardDTO(this.name);

        given()
                .contentType(ContentType.JSON)
                .body(dashboardDTO)
                .when()
                .post("/api/dashboards")
                .then()
                .statusCode(201);
    }

    @Test
    void testCreateDashboardWithInvalidName() {
        DashboardDTO dashboardDTO = new DashboardDTO("");

        given()
                .contentType(ContentType.JSON)
                .body(dashboardDTO)
                .when()
                .post("/api/dashboards")
                .then()
                .statusCode(400)
                .body("message", containsString(INVALID_DASHBOARD_NAME));

    }

    @Test
    void testCreateDashboardWithNullName() {
        DashboardDTO dashboardDTO = new DashboardDTO(null);

        given()
                .contentType(ContentType.JSON)
                .body(dashboardDTO)
                .when()
                .post("/api/dashboards")
                .then()
                .statusCode(400)
                .body("message", containsString(INVALID_DASHBOARD_NAME));
    }

    @Test
    void testGetSpecificDashboardById(){
        long dashboardId = actionCreateDashboard();

        given()
                .pathParams("id", dashboardId)
                .when()
                .get("/api/dashboards/{id}", dashboardId)
                .then()
                .statusCode(200)
                .body("id", equalTo((int) dashboardId))
                .body("name", equalTo(this.name));
    }

    @Test
    void testGetSpecificDashboardByIdWithInvalidId(){
        long dashboardId = 12345L;

        given()
                .pathParams("id", dashboardId)
                .when()
                .get("/api/dashboards/{id}", dashboardId)
                .then()
                .statusCode(404)
                .body("message", containsString(DASHBOARD_NOT_FOUND));
    }

    @Test
    void testGetSpecificDashboardByIdWithNullId() {
        given()
                .when()
                .get("/api/dashboards/")
                .then()
                .statusCode(404);
    }

    @Test
    void testRemoveDashboard() {
        long dashboardId = actionCreateDashboard();

        Response foundResponse = actionGetSpecificDashboardById(dashboardId);
        foundResponse.then().statusCode(200);

        given()
                .pathParams("id", dashboardId)
                .when()
                .delete("/api/dashboards/{id}", dashboardId)
                .then()
                .statusCode(200);

        Response notFoundResponse = actionGetSpecificCardById(dashboardId);
        notFoundResponse.then().statusCode(404);
    }

    @Test
    void testRemoveDashboardWithInvalidId() {
        long cardId = 12345L;

        given()
                .pathParams("id", cardId)
                .when()
                .delete("/api/dashboards/{id}", cardId)
                .then()
                .statusCode(404)
                .body("message", containsString(DASHBOARD_NOT_FOUND));
    }

    @Test
    void testUpdateTitleCard(){
        long dashboardId = actionCreateDashboard();
        String newName = "New Name";
        DashboardDTO newDashboardDTO = new DashboardDTO(newName);

        given()
                .pathParams("id", dashboardId)
                .contentType(ContentType.JSON)
                .body(newDashboardDTO)
                .when()
                .put("/api/dashboards/{id}/name", dashboardId)
                .then()
                .statusCode(200)
                .body("name", equalTo(newName))
                .body("id", equalTo((int) dashboardId));
    }

    @Test
    void testUpdateNameDashboardWithInvalidName() {
        long dashboardId = actionCreateDashboard();
        DashboardDTO newDashboardDTO = new DashboardDTO("");

        given()
                .pathParams("id", dashboardId)
                .contentType(ContentType.JSON)
                .body(newDashboardDTO)
                .when()
                .put("/api/dashboards/{id}/name", dashboardId)
                .then()
                .statusCode(400)
                .body("message", containsString(INVALID_DASHBOARD_NAME));
    }

    @Test
    void testUpdateNameDashboardWithNullName() {
        long dashboardId = actionCreateDashboard();
        DashboardDTO newDashboardDTO = new DashboardDTO(null);

        given()
                .pathParams("id", dashboardId)
                .contentType(ContentType.JSON)
                .body(newDashboardDTO)
                .when()
                .put("/api/dashboards/{id}/name", dashboardId)
                .then()
                .statusCode(400)
                .body("message", containsString(INVALID_DASHBOARD_NAME));
    }

    @Test
    void testUpdateNameDashboardWithInvalidId() {
        long dashboardId = 12345L;
        String newName = "New Name";
        DashboardDTO newDashboardDTO = new DashboardDTO(newName);

        given()
                .pathParams("id", dashboardId)
                .contentType(ContentType.JSON)
                .body(newDashboardDTO)
                .when()
                .put("/api/dashboards/{id}/name", dashboardId)
                .then()
                .statusCode(404)
                .body("message", containsString(DASHBOARD_NOT_FOUND));
    }

    @Test
    void testAttachFileToCard(){
        long dashboardId = actionCreateDashboard();
        AttachedFile attachedFile = new AttachedFile("example.png", "/example.png");

        this.actionAttachFileToDashboard(dashboardId, attachedFile);
    }

    @Test
    void testAttachFileToDashboardWithInvalidAttachedFile() {
        long dashboardId = actionCreateDashboard();
        AttachedFile attachedFile = new AttachedFile("", "");

        given()
                .pathParams("id", dashboardId)
                .contentType(ContentType.JSON)
                .body(attachedFile)
                .when()
                .post("/api/dashboards/{id}/attached-files", dashboardId)
                .then()
                .statusCode(400)
                .body("message", containsString(INVALID_ATTACHED_FILE));
    }

    @Test
    void testAttachFileToDashboardWithInvalidId() {
        long dashboardId = 12345L;
        AttachedFile attachedFile = new AttachedFile("example.png", "/example.png");

        given()
                .pathParams("id", dashboardId)
                .contentType(ContentType.JSON)
                .body(attachedFile)
                .when()
                .post("/api/dashboards/{id}/attached-files", dashboardId)
                .then()
                .statusCode(404)
                .body("message", containsString(DASHBOARD_NOT_FOUND));
    }

    @Test
    void testDetachFileFromDashboard(){
        long dashboardId = actionCreateDashboard();
        AttachedFile attachedFile = new AttachedFile("example.png", "/example.png");

        this.actionAttachFileToDashboard(dashboardId, attachedFile);

        given()
                .pathParams("id", dashboardId)
                .contentType(ContentType.JSON)
                .body(attachedFile)
                .when()
                .delete("/api/dashboards/{id}/attached-files", dashboardId)
                .then()
                .statusCode(200)
                .body("id", equalTo((int) dashboardId))
                .body("attachedFiles.size()", equalTo(0));
    }

    @Test
    void testDetachFileFromDashboardWithInvalidAttachedFile() {
        long dashboardId = actionCreateDashboard();
        AttachedFile attachedFile = new AttachedFile("", "");

        given()
                .pathParams("id", dashboardId)
                .contentType(ContentType.JSON)
                .body(attachedFile)
                .when()
                .delete("/api/dashboards/{id}/attached-files", dashboardId)
                .then()
                .statusCode(400)
                .body("message", containsString(INVALID_ATTACHED_FILE));
    }

    @Test
    void testDetachFileFromDashboardWithInexistentAttachedFile() {
        long dashboardId = actionCreateDashboard();
        AttachedFile attachedFile = new AttachedFile("example.png", "/example.png");

        given()
                .pathParams("id", dashboardId)
                .contentType(ContentType.JSON)
                .body(attachedFile)
                .when()
                .delete("/api/dashboards/{id}/attached-files", dashboardId)
                .then()
                .statusCode(404)
                .body("message", containsString(NOT_FOUND_ELEMENT_ERROR_IN_DASHBOARD));
    }

    @Test
    void testDetachFileFromDashboardWithInvalidId() {
        long dashboardId = 12345L;
        AttachedFile attachedFile = new AttachedFile("example.png", "/example.png");

        given()
                .pathParams("id", dashboardId)
                .contentType(ContentType.JSON)
                .body(attachedFile)
                .when()
                .delete("/api/dashboards/{id}/attached-files", dashboardId)
                .then()
                .statusCode(404)
                .body("message", containsString(NOT_FOUND_DASHBOARD_WITH_ID_ERROR));
    }

    @Test
    void testAddTagToDashboard(){
        long dashboardId = actionCreateDashboard();
        Tag tag = new Tag("Example Label", Color.BLUE);
        long tagId = actionCreateTag(tag.getLabel(), tag.getColor(), tag.getVisibility());

        this.actionAddTagToDashboard(dashboardId, tagId);
    }

    @Test
    void testAddTagToDashboardWithInvalidTag() {
        long dashboardId = actionCreateDashboard();
        long tagId = 12345L;

        given()
                .pathParams("id", dashboardId)
                .contentType(ContentType.JSON)
                .body(tagId)
                .when()
                .post("/api/dashboards/{id}/tags", dashboardId)
                .then()
                .statusCode(404)
                .body("message", containsString(TAG_NOT_FOUND));
    }

    @Test
    void testAddTagToDashboardWithInvalidId() {
        long dashboardId = 12345L;
        dev.kandv.kango.models.Tag tag = new Tag("Example Label", Color.BLUE);
        long tagId = actionCreateTag(tag.getLabel(), tag.getColor(), tag.getVisibility());

        given()
                .pathParams("id", dashboardId)
                .contentType(ContentType.JSON)
                .body(tagId)
                .when()
                .post("/api/dashboards/{id}/tags", dashboardId)
                .then()
                .statusCode(404)
                .body("message", containsString(DASHBOARD_NOT_FOUND));
    }

    @Test
    void testRemoveTagFromDashboard(){
        long dashboardId = actionCreateDashboard();
        Tag tag = new Tag("Example Label", Color.BLUE);
        long tagId = actionCreateTag(tag.getLabel(), tag.getColor(), tag.getVisibility());

        this.actionAddTagToDashboard(dashboardId, tagId);

        given()
                .pathParams("id", dashboardId)
                .contentType(ContentType.JSON)
                .body(tagId)
                .when()
                .delete("/api/dashboards/{id}/tags", dashboardId)
                .then()
                .statusCode(200)
                .body("id", equalTo((int) dashboardId))
                .body("tagList.size()", equalTo(0));
    }

    @Test
    void testRemoveTagFromDashboardWithInvalidTag() {
        long dashboardId = actionCreateDashboard();
        long tagId = 12345L;

        given()
                .pathParams("id", dashboardId)
                .contentType(ContentType.JSON)
                .body(tagId)
                .when()
                .delete("/api/dashboards/{id}/tags", dashboardId)
                .then()
                .statusCode(404)
                .body("message", containsString(TAG_NOT_FOUND));
    }

    @Test
    void testRemoveTagFromDashboardWithInexistentTag() {
        long dashboardId = actionCreateDashboard();
        Tag tag = new Tag("Example Label", Color.BLUE);
        long tagId = actionCreateTag(tag.getLabel(), tag.getColor(), tag.getVisibility());

        given()
                .pathParams("id", dashboardId)
                .contentType(ContentType.JSON)
                .body(tagId)
                .when()
                .delete("/api/dashboards/{id}/tags", dashboardId)
                .then()
                .statusCode(404)
                .body("message", containsString(NOT_FOUND_ELEMENT_ERROR_IN_DASHBOARD));
    }

    @Test
    void testRemoveTagFromDashboardWithInvalidId() {
        long dashboardId = 12345L;
        Tag tag = new Tag("Example Label", Color.BLUE);
        long tagId = actionCreateTag(tag.getLabel(), tag.getColor(), tag.getVisibility());

        given()
                .pathParams("id", dashboardId)
                .contentType(ContentType.JSON)
                .body(tagId)
                .when()
                .delete("/api/dashboards/{id}/tags", dashboardId)
                .then()
                .statusCode(404)
                .body("message", containsString(NOT_FOUND_DASHBOARD_WITH_ID_ERROR));
    }

    @Test
    void testAddTableToDashboard() {
        long dashboardId = actionCreateDashboard();
        long tableId = actionCreateTable();

        given()
                .contentType(ContentType.JSON)
                .body(tableId)
                .pathParams("id", dashboardId)
                .when()
                .post("/api/dashboards/{id}/tables", dashboardId)
                .then()
                .statusCode(201)
                .body("tableList.size()", equalTo(1));    }

    @Test
    void testAddTableToDashboardWithInvalidTableId() {
        long tableId = 12345L;
        long cardId = actionCreateCard();

        given()
                .contentType(ContentType.JSON)
                .body(cardId)
                .pathParams("id", tableId)
                .when()
                .post("/api/tables/{id}/cards")
                .then()
                .statusCode(404)
                .body("message", containsString(NOT_FOUND_TABLE_WITH_ID_ERROR));
    }

    @Test
    void testAddTableToDashboardWithInvalidCardId() {
        long tableId = actionCreateTable();
        long cardId = 12345L;

        given()
                .contentType(ContentType.JSON)
                .body(cardId)
                .pathParams("id", tableId)
                .when()
                .post("/api/tables/{id}/cards")
                .then()
                .statusCode(404)
                .body("message", containsString(NOT_FOUND_CARD_WITH_ID_ERROR));
    }

    @Test
    void testRemoveTableFromDashboard() {
        long dashboardId = actionCreateDashboard();
        long tableId = actionCreateTable();

        actionAddTableToDashboard(dashboardId, tableId);

        given()
                .contentType(ContentType.JSON)
                .body(tableId)
                .pathParams("id", dashboardId)
                .when()
                .delete("/api/dashboards/{id}/tables")
                .then()
                .statusCode(200)
                .body("tableList.size()", equalTo(0));
    }

    @Test
    void testRemoveTableFromDashboardWithInvalidDashboardId() {
        long dashboardId = 12345L;
        long tableId = actionCreateTable();

        given()
                .contentType(ContentType.JSON)
                .body(tableId)
                .pathParams("id", dashboardId)
                .when()
                .delete("/api/dashboards/{id}/tables")
                .then()
                .statusCode(404)
                .body("message", containsString(NOT_FOUND_DASHBOARD_WITH_ID_ERROR));
    }

    @Test
    void testRemoveTableFromDashboardWithInvalidTableId() {
        long dashboardId = actionCreateDashboard();
        long tableId = 12345L;

        given()
                .contentType(ContentType.JSON)
                .body(tableId)
                .pathParams("id", dashboardId)
                .when()
                .delete("/api/dashboards/{id}/tables")
                .then()
                .statusCode(404)
                .body("message", containsString(NOT_FOUND_TABLE_WITH_ID_ERROR));
    }

    @Test
    void testRemoveTableFromDashboardWithNoTableIdList() {
        long dashboardId = actionCreateDashboard();
        long tableId = actionCreateTable();

        given()
                .contentType(ContentType.JSON)
                .body(tableId)
                .pathParams("id", dashboardId)
                .when()
                .delete("/api/dashboards/{id}/tables")
                .then()
                .statusCode(404)
                .body("message", containsString(NOT_FOUND_TABLE_IN_THE_DASHBOARD_ERROR));
    }

    @Test
    void testAddTemplateCardToDashboard() {
        String cardTitle = CardRestControllerUtils.cardTitle;
        long dashboardId = actionCreateDashboard();
        long cardId = actionCreateCard(cardTitle, CardType.LOCAL_TEMPLATE);

        given()
                .contentType(ContentType.JSON)
                .body(cardId)
                .pathParams("id", dashboardId)
                .when()
                .post("/api/dashboards/{id}/template-cards")
                .then()
                .statusCode(201)
                .body("templateCardList.size()", equalTo(1));
    }

    @Test
    void testAddTemplateCardToDashboardWithInvalidDashboardId() {
        long dashboardId = 12345L;
        String cardTitle = CardRestControllerUtils.cardTitle;
        long cardId = actionCreateCard(cardTitle, CardType.LOCAL_TEMPLATE);

        given()
                .contentType(ContentType.JSON)
                .body(cardId)
                .pathParams("id", dashboardId)
                .when()
                .post("/api/dashboards/{id}/template-cards")
                .then()
                .statusCode(404)
                .body("message", containsString(NOT_FOUND_DASHBOARD_WITH_ID_ERROR));
    }

    @Test
    void testAddTemplateCardToDashboardWithInvalidCardId() {
        long dashboardId = actionCreateDashboard();
        long cardId = 12345L;

        given()
                .contentType(ContentType.JSON)
                .body(cardId)
                .pathParams("id", dashboardId)
                .when()
                .post("/api/dashboards/{id}/template-cards")
                .then()
                .statusCode(404)
                .body("message", containsString(NOT_FOUND_CARD_WITH_ID_ERROR));
    }

    @Test
    void testRemoveTemplateCardFromDashboard() {
        String cardTitle = CardRestControllerUtils.cardTitle;
        long dashboardId = actionCreateDashboard();
        long cardId = actionCreateCard(cardTitle, CardType.LOCAL_TEMPLATE);

        actionAddTemplateCardToDashboard(dashboardId, cardId);

        given()
                .contentType(ContentType.JSON)
                .body(cardId)
                .pathParams("id", dashboardId)
                .when()
                .delete("/api/dashboards/{id}/template-cards")
                .then()
                .statusCode(200)
                .body("templateCardList.size()", equalTo(0));
    }

    @Test
    void testRemoveTemplateCardFromDashboardWithInvalidTableId() {
        long dashboardId = 12345L;
        String cardTitle = CardRestControllerUtils.cardTitle;
        long cardId = actionCreateCard(cardTitle, CardType.LOCAL_TEMPLATE);

        given()
                .contentType(ContentType.JSON)
                .body(cardId)
                .pathParams("id", dashboardId)
                .when()
                .delete("/api/dashboards/{id}/template-cards")
                .then()
                .statusCode(404)
                .body("message", containsString(NOT_FOUND_DASHBOARD_WITH_ID_ERROR));
    }

    @Test
    void testRemoveTemplateCardFromDashboardWithInvalidCardId() {
        long dashboardId = actionCreateDashboard();
        long cardId = 12345L;

        given()
                .contentType(ContentType.JSON)
                .body(cardId)
                .pathParams("id", dashboardId)
                .when()
                .delete("/api/dashboards/{id}/template-cards")
                .then()
                .statusCode(404)
                .body("message", containsString(NOT_FOUND_CARD_WITH_ID_ERROR));
    }

    @Test
    void testRemoveTemplateCardFromDashboardWithNoCardIdList() {
        String cardTitle = CardRestControllerUtils.cardTitle;
        long dashboardId = actionCreateDashboard();
        long cardId = actionCreateCard(cardTitle, CardType.LOCAL_TEMPLATE);

        given()
                .contentType(ContentType.JSON)
                .body(cardId)
                .pathParams("id", dashboardId)
                .when()
                .delete("/api/dashboards/{id}/template-cards")
                .then()
                .statusCode(404)
                .body("message", containsString(NOT_FOUND_CARD_IN_THE_DASHBOARD_ERROR));
    }

    @Test
    void testGetAllDashboards() {
        actionCreateDashboard("Example 1");
        actionCreateDashboard("Example 2");
        actionCreateDashboard("Example 3");

        given()
                .when()
                .get("api/dashboards")
                .then()
                .statusCode(200)
                .body("size()", equalTo(3));
    }

    @Test
    void testGetAllDashboardsWithNoDashboards() {
        given()
                .when()
                .get("api/dashboards")
                .then()
                .statusCode(200)
                .body("size()", equalTo(0));
    }

    @Test
    void testUpdateTablePositionFromDashboard() {
        long dashboardId = actionCreateDashboard();
        String tableName =  "This is the updated card";
        long tableId1 = actionCreateTable(tableName, new ArrayList<>());
        long tableId2 = actionCreateTable();

        int newPosition = 1;

        actionAddTableToDashboard(dashboardId, tableId1);
        actionAddTableToDashboard(dashboardId, tableId2, 2);

        given()
                .contentType(ContentType.JSON)
                .pathParams("dashboardId", dashboardId)
                .pathParams("tableId", tableId1)
                .queryParam("position", newPosition)
                .when()
                .put("/api/dashboards/{dashboardId}/tables/{tableId}/position")
                .then()
                .statusCode(200)
                .body("tableList.size()", equalTo(2))
                .body("tableList.get(0).name", equalTo(TableRestControllerUtils.name))
                .body("tableList.get(1).name", equalTo(tableName));
    }

    @Test
    void testUpdateTablePositionFromDashboardWithInvalidDashboardId() {
        long dashboardId = 12345L;
        long tableId = actionCreateTable();
        int newPosition = 1;

        given()
                .contentType(ContentType.JSON)
                .pathParams("dashboardId", dashboardId)
                .pathParams("tableId", tableId)
                .queryParam("position", newPosition)
                .when()
                .put("/api/dashboards/{dashboardId}/tables/{tableId}/position")
                .then()
                .statusCode(404)
                .body("message", containsString(NOT_FOUND_DASHBOARD_WITH_ID_ERROR));
    }

    @Test
    void testUpdateTablePositionFromDashboardWithInvalidTableId() {
        long dashboardId = actionCreateDashboard();
        long tableId = 12345L;
        int newPosition = 1;

        given()
                .contentType(ContentType.JSON)
                .pathParams("dashboardId", dashboardId)
                .pathParams("tableId", tableId)
                .queryParam("position", newPosition)
                .when()
                .put("/api/dashboards/{dashboardId}/tables/{tableId}/position")
                .then()
                .statusCode(404)
                .body("message", containsString(NOT_FOUND_TABLE_WITH_ID_ERROR));
    }

    @Test
    void testUpdateTablePositionFromDashboardWithNoTableList() {
        long dashboardId = actionCreateDashboard();
        long tableId = actionCreateTable();
        int newPosition = 1;

        given()
                .contentType(ContentType.JSON)
                .pathParams("dashboardId", dashboardId)
                .pathParams("tableId", tableId)
                .queryParam("position", newPosition)
                .when()
                .put("/api/dashboards/{dashboardId}/tables/{tableId}/position")
                .then()
                .statusCode(404)
                .body("message", containsString(NOT_FOUND_TABLE_IN_THE_DASHBOARD_ERROR));
    }

    private void actionAttachFileToDashboard(long dashboardId, AttachedFile attachedFile) {
        given()
                .pathParams("id", dashboardId)
                .contentType(ContentType.JSON)
                .body(attachedFile)
                .when()
                .post("/api/dashboards/{id}/attached-files", dashboardId)
                .then()
                .statusCode(201)
                .body("id", equalTo((int) dashboardId))
                .body("attachedFiles.size()", equalTo(1));
    }

    private void actionAddTagToDashboard(long dashboardId, long tagId) {
        given()
                .pathParams("id", dashboardId)
                .contentType(ContentType.JSON)
                .body(tagId)
                .when()
                .post("/api/dashboards/{id}/tags", dashboardId)
                .then()
                .statusCode(201)
                .body("id", equalTo((int) dashboardId))
                .body("tagList.size()", equalTo(1));
    }
}
