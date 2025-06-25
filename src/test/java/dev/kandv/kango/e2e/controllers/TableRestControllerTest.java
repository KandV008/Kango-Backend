package dev.kandv.kango.e2e.controllers;

import dev.kandv.kango.KangoApplication;
import dev.kandv.kango.dtos.CardDTO;
import dev.kandv.kango.dtos.TableDTO;
import dev.kandv.kango.models.enums.CardListSort;
import dev.kandv.kango.models.enums.CardType;
import dev.kandv.kango.services.CardService;
import dev.kandv.kango.services.TableService;
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
import java.util.List;

import static dev.kandv.kango.controllers.ErrorMessagesRestControllers.TABLE_NOT_FOUND;
import static dev.kandv.kango.controllers.TableRestController.*;
import static dev.kandv.kango.e2e.controllers.CardRestControllerUtils.actionCreateCard;
import static dev.kandv.kango.e2e.controllers.TableRestControllerUtils.*;
import static dev.kandv.kango.services.ErrorMessagesServices.NOT_FOUND_CARD_WITH_ID_ERROR;
import static dev.kandv.kango.services.ErrorMessagesServices.NOT_FOUND_TABLE_WITH_ID_ERROR;
import static dev.kandv.kango.services.TableService.*;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;

@Testcontainers
@SpringBootTest(
        classes = KangoApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@ExtendWith(SpringExtension.class)
public class TableRestControllerTest {

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
    CardService cardService;
    @Autowired
    TableService tableService;

    String name = TableRestControllerUtils.name;
    List<CardDTO> cards;

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
        this.cards = new ArrayList<>();
    }

    @AfterEach
    void afterEach() {
        RestAssured.reset();
        this.tableService.removeAllTables();
        this.cardService.removeAllCards();
    }

    @Test
    void testCreateTable() {
        TableDTO tableDTO = new TableDTO(this.name, this.cards);

        given()
                .contentType(ContentType.JSON)
                .body(tableDTO)
        .when()
                .post("/api/tables")
        .then()
                .statusCode(201);
    }

    @Test
    void testCreateTableWithCardList(){
        CardDTO cardDTO = new CardDTO("Example Card", CardType.GLOBAL_TEMPLATE);
        this.cards.add(cardDTO);
        TableDTO tableDTO = new TableDTO(this.name, this.cards);

        given()
        .contentType(ContentType.JSON)
                .body(tableDTO)
                .when()
                .post("/api/tables")
                .then()
                .statusCode(201)
                .body("cardList.size()", equalTo(1));
    }

    @Test
    void testCreateTableWithInvalidName() {
        TableDTO tableDTO = new TableDTO("", this.cards);

        given()
                .contentType(ContentType.JSON)
                .body(tableDTO)
                .when()
                .post("/api/tables")
                .then()
                .statusCode(400)
                .body("message", containsString(INVALID_NAME));
    }

    @Test
    void testCreateTableWithInvalidCardList() {
        this.cards.add(null);
        TableDTO tableDTO = new TableDTO(this.name, this.cards);

        given()
                .contentType(ContentType.JSON)
                .body(tableDTO)
                .when()
                .post("/api/tables")
                .then()
                .statusCode(400)
        .body("message", containsString(NULL_CARD_IN_CARD_LIST));
    }

    @Test
    void testCreateTableWithNullName() {
        TableDTO tableDTO = new TableDTO(null, this.cards);

        given()
                .contentType(ContentType.JSON)
                .body(tableDTO)
                .when()
                .post("/api/tables")
                .then()
                .statusCode(400)
                .body("message", containsString(INVALID_NAME));
    }

    @Test
    void testCreateTableWithNullCardList() {
        TableDTO tableDTO = new TableDTO(this.name, null);

        given()
                .contentType(ContentType.JSON)
                .body(tableDTO)
                .when()
                .post("/api/tables")
                .then()
                .statusCode(400)
                .body("message", containsString(INVALID_CARD_LIST));
    }

    @Test
    void testGetSpecificTableById(){
        long tableId = actionCreateTable();

        given()
                .pathParams("id", tableId)
                .when()
                .get("/api/tables/{id}", tableId)
                .then()
                .statusCode(200)
                .body("id", equalTo((int) tableId))
                .body("name", equalTo(this.name));
    }

    @Test
    void testGetSpecificTableByIdWithInvalidId(){
        long tableId = 12345L;

        given()
                .pathParams("id", tableId)
                .when()
                .get("/api/tables/{id}", tableId)
                .then()
                .statusCode(404)
                .body("message", containsString(TABLE_NOT_FOUND));
    }

    @Test
    void testGetSpecificTableByIdWithNullId() {
        given()
                .when()
                .get("/api/tables/")
                .then()
                .statusCode(404);
    }

    @Test
    void testUpdateNameTable(){
        long tableId = actionCreateTable();
        String newName = "New Name";
        TableDTO newTitleDTO = new TableDTO();
        newTitleDTO.setName(newName);

        given()
                .pathParams("id", tableId)
                .contentType(ContentType.JSON)
                .body(newTitleDTO)
                .when()
                .put("/api/tables/{id}/name", tableId)
                .then()
                .statusCode(200)
                .body("name", equalTo(newName))
                .body("id", equalTo((int) tableId));
    }

    @Test
    void testUpdateNameTableWithInvalidTitle() {
        long tableId = actionCreateTable();
        TableDTO newTitleDTO = new TableDTO();
        newTitleDTO.setName("");

        given()
                .pathParams("id", tableId)
                .contentType(ContentType.JSON)
                .body(newTitleDTO)
                .when()
                .put("/api/tables/{id}/name", tableId)
                .then()
                .statusCode(400)
                .body("message", containsString(INVALID_NAME));
    }

    @Test
    void testUpdateNameTableWithNullTitle() {
        long tableId = actionCreateTable();
        TableDTO newTitleDTO = new TableDTO();
        newTitleDTO.setName(null);

        given()
                .pathParams("id", tableId)
                .contentType(ContentType.JSON)
                .body(newTitleDTO)
                .when()
                .put("/api/tables/{id}/name", tableId)
                .then()
                .statusCode(400)
                .body("message", containsString(INVALID_NAME));
    }

    @Test
    void testUpdateNameTableWithInvalidId() {
        long tableId = 12345L;
        String newName = "New Name";
        TableDTO newTitleDTO = new TableDTO();
        newTitleDTO.setName(newName);

        given()
                .pathParams("id", tableId)
                .contentType(ContentType.JSON)
                .body(newTitleDTO)
                .when()
                .put("/api/tables/{id}/name", tableId)
                .then()
                .statusCode(404)
                .body("message", containsString(TABLE_NOT_FOUND));
    }

    @Test
    void testRemoveTable() {
        long tableId = actionCreateTable();

        Response foundResponse = actionGetSpecificTableById(tableId);
        foundResponse.then().statusCode(200);

        given()
                .pathParams("id", tableId)
                .when()
                .delete("/api/tables/{id}", tableId)
                .then()
                .statusCode(200);

        Response notFoundResponse = actionGetSpecificTableById(tableId);
        notFoundResponse.then().statusCode(404);
    }

    @Test
    void testRemoveTableWithInvalidId() {
        long tableId = 12345L;

        given()
                .pathParams("id", tableId)
                .when()
                .delete("/api/tables/{id}", tableId)
                .then()
                .body("message", containsString(TABLE_NOT_FOUND));
    }

    @Test
    void testAddCardToTable() {
        long tableId = actionCreateTable();
        long cardId = actionCreateCard();

        actionAddCardToTable(tableId, cardId);
    }

    @Test
    void testAddCardToTableWithInvalidTableId() {
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
    void testAddCardToTableWithInvalidCardId() {
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
    void testRemoveCardFromTable() {
        long tableId = actionCreateTable();
        long cardId = actionCreateCard();

        actionAddCardToTable(tableId, cardId);

        given()
                .contentType(ContentType.JSON)
                .body(cardId)
                .pathParams("id", tableId)
                .when()
                .delete("/api/tables/{id}/cards")
                .then()
                .statusCode(200)
                .body("cardList.size()", equalTo(0));
    }

    @Test
    void testRemoveCardFromTableWithInvalidTableId() {
        long tableId = 12345L;
        long cardId = actionCreateCard();

        given()
                .contentType(ContentType.JSON)
                .body(cardId)
                .pathParams("id", tableId)
                .when()
                .delete("/api/tables/{id}/cards")
                .then()
                .statusCode(404)
                .body("message", containsString(NOT_FOUND_TABLE_WITH_ID_ERROR));
    }

    @Test
    void testRemoveCardFromTableWithInvalidCardId() {
        long tableId = actionCreateTable();
        long cardId = 12345L;

        given()
                .contentType(ContentType.JSON)
                .body(cardId)
                .pathParams("id", tableId)
                .when()
                .delete("/api/tables/{id}/cards")
                .then()
                .statusCode(404)
                .body("message", containsString(NOT_FOUND_CARD_WITH_ID_ERROR));
    }

    @Test
    void testRemoveCardFromTableWithNoCardIdList() {
        long tableId = actionCreateTable();
        long cardId = actionCreateCard();

        given()
                .contentType(ContentType.JSON)
                .body(cardId)
                .pathParams("id", tableId)
                .when()
                .delete("/api/tables/{id}/cards")
                .then()
                .statusCode(404)
                .body("message", containsString(NOT_FOUND_CARD_IN_THE_TABLE_ERROR));
    }

    @Test
    void testSortCardListFromTable(){
        long tableId = actionCreateTable();
        CardListSort cardListSort = CardListSort.BY_ID_REVERSE;

        long cardListId1 = actionCreateCard("Example Card 1", CardType.NORMAL);
        long cardListId2 = actionCreateCard("Example Card 2", CardType.NORMAL);

        actionAddCardToTable(tableId, cardListId1);
        actionAddCardToTable(tableId, cardListId2, 2);

        given()
        .contentType(ContentType.JSON)
                .pathParams("id", tableId)
                .queryParam("sort", cardListSort.name())
                .when()
                .put("/api/tables/{id}/sort")
                .then()
                .statusCode(200)
                .body("cardList.size()", equalTo(2))
                .body("cardList.get(0).id", equalTo((int) cardListId2))
                .body("cardList.get(1).id", equalTo((int) cardListId1));

    }

    @Test
    void testSortCardListFromTableWithInvalidId(){
        long tableId = 12345L;
        CardListSort cardListSort = CardListSort.BY_ID_REVERSE;

        given()
                .contentType(ContentType.JSON)
                .pathParams("id", tableId)
                .queryParam("sort", cardListSort.name())
                .when()
                .put("/api/tables/{id}/sort")
                .then()
                .statusCode(404)
                .body("message", containsString(TABLE_NOT_FOUND));
    }

    @Test
    void testSortCardListFromTableWithInvalidSort(){
        long tableId = actionCreateTable();

        long cardListId1 = actionCreateCard("Example Card 1", CardType.NORMAL);
        long cardListId2 = actionCreateCard("Example Card 2", CardType.NORMAL);

        actionAddCardToTable(tableId, cardListId1);
        actionAddCardToTable(tableId, cardListId2, 2);


        given()
                .contentType(ContentType.JSON)
                .pathParams("id", tableId)
                .queryParam("sort", "")
                .when()
                .put("/api/tables/{id}/sort")
                .then()
                .statusCode(400);
    }

    @Test
    void testUpdateCardPositionFromTable() {
        long tableId = actionCreateTable();
        String cardTitle =  "This is the updated card";
        long cardId1 = actionCreateCard(cardTitle, CardType.NORMAL);
        long cardId2 = actionCreateCard();

        int newPosition = 1;

        actionAddCardToTable(tableId, cardId1);
        actionAddCardToTable(tableId, cardId2, 2);

        given()
                .contentType(ContentType.JSON)
                .pathParams("tableId", tableId)
                .pathParams("cardId", cardId1)
                .queryParam("position", newPosition)
                .when()
                .put("/api/tables/{tableId}/cards/{cardId}/position")
                .then()
                .statusCode(200)
                .body("cardList.size()", equalTo(2))
                .body("cardList.get(0).title", equalTo(CardRestControllerUtils.cardTitle))
                .body("cardList.get(1).title", equalTo(cardTitle));
    }

    @Test
    void testUpdateCardPositionFromTableWithInvalidTableId() {
        long tableId = 12345L;
        long cardId = actionCreateCard();
        int newPosition = 1;

        given()
                .contentType(ContentType.JSON)
                .pathParams("tableId", tableId)
                .pathParams("cardId", cardId)
                .queryParam("position", newPosition)
                .when()
                .put("/api/tables/{tableId}/cards/{cardId}/position")
                .then()
                .statusCode(404)
                .body("message", containsString(NOT_FOUND_TABLE_WITH_ID_ERROR));
    }

    @Test
    void testUpdateCardPositionFromTableWithInvalidCardId() {
        long tableId = actionCreateTable();
        long cardId = 12345L;
        int newPosition = 1;

        given()
                .contentType(ContentType.JSON)
                .pathParams("tableId", tableId)
                .pathParams("cardId", cardId)
                .queryParam("position", newPosition)
                .when()
                .put("/api/tables/{tableId}/cards/{cardId}/position")
                .then()
                .statusCode(404)
                .body("message", containsString(NOT_FOUND_CARD_WITH_ID_ERROR));
    }

    @Test
    void testUpdateCardPositionFromTableWithNoCardIdList() {
        long tableId = actionCreateTable();
        long cardId = actionCreateCard();
        int newPosition = 1;

        given()
                .contentType(ContentType.JSON)
                .pathParams("tableId", tableId)
                .pathParams("cardId", cardId)
                .queryParam("position", newPosition)
                .when()
                .put("/api/tables/{tableId}/cards/{cardId}/position")
                .then()
                .statusCode(404)
                .body("message", containsString(NOT_FOUND_CARD_IN_THE_TABLE_ERROR));
    }

    @Test
    void testMoveCardFromTableToAnotherTable() {
        long tableId1 = actionCreateTable("Table 1", new ArrayList<>());
        long tableId2 = actionCreateTable("Table 2", new ArrayList<>());
        long cardId1 = actionCreateCard();
        int newPosition = 0;

        actionAddCardToTable(tableId1, cardId1);

        given()
        .contentType(ContentType.JSON)
                .pathParams("tableId", tableId1)
                .pathParams("cardId", cardId1)
                .queryParam("newTable", tableId2)
                .queryParam("position", newPosition)
                .when()
                .put("/api/tables/{tableId}/cards/{cardId}")
                .then()
                .statusCode(200);

        Response responseTable1 = actionGetSpecificTableById(tableId1);
        responseTable1.then()
                .statusCode(200)
                .body("cardList.size()", equalTo(0));

        Response responseTable2 = actionGetSpecificTableById(tableId2);
        responseTable2.then()
                .statusCode(200)
                .body("cardList.size()", equalTo(1));

    }

    @Test
    void testMoveCardFromTableWithInvalidOriginTableId() {
        long tableId1 = 12345L;
        long tableId2 = actionCreateTable("Table 2", new ArrayList<>());
        long cardId = actionCreateCard();
        int newPosition = 0;

        given()
                .contentType(ContentType.JSON)
                .pathParams("tableId", tableId1)
                .pathParams("cardId", cardId)
                .queryParam("newTable", tableId2)
                .queryParam("position", newPosition)
                .when()
                .put("/api/tables/{tableId}/cards/{cardId}")
                .then()
                .statusCode(404)
                .body("message", containsString(NOT_FOUND_TABLE_WITH_ID_ERROR));
    }

    @Test
    void testMoveCardFromTableWithInvalidCardId() {
        long tableId1 = actionCreateTable("Table 1", new ArrayList<>());
        long tableId2 = actionCreateTable("Table 2", new ArrayList<>());
        long cardId = 12345L;
        int newPosition = 0;

        given()
                .contentType(ContentType.JSON)
                .pathParams("tableId", tableId1)
                .pathParams("cardId", cardId)
                .queryParam("newTable", tableId2)
                .queryParam("position", newPosition)
                .when()
                .put("/api/tables/{tableId}/cards/{cardId}")
                .then()
                .statusCode(404)
                .body("message", containsString(NOT_FOUND_CARD_WITH_ID_ERROR));
    }

    @Test
    void testMoveCardFromTableWithInvalidDestinyTableId() {
        long tableId1 = actionCreateTable("Table 1", new ArrayList<>());
        long tableId2 = 12345L;
        long cardId = actionCreateCard();
        int newPosition = 0;

        given()
                .contentType(ContentType.JSON)
                .pathParams("tableId", tableId1)
                .pathParams("cardId", cardId)
                .queryParam("newTable", tableId2)
                .queryParam("position", newPosition)
                .when()
                .put("/api/tables/{tableId}/cards/{cardId}")
                .then()
                .statusCode(404)
                .body("message", containsString(NOT_FOUND_TABLE_WITH_ID_ERROR));
    }

    @Test
    void testMoveCardFromTableToAnotherTableWithNoCardIdList() {
        long tableId1 = actionCreateTable("Table 1", new ArrayList<>());
        long tableId2 = actionCreateTable("Table 2", new ArrayList<>());
        long cardId1 = actionCreateCard();
        int newPosition = 1;

        given()
                .contentType(ContentType.JSON)
                .pathParams("tableId", tableId1)
                .pathParams("cardId", cardId1)
                .queryParam("newTable", tableId2)
                .queryParam("position", newPosition)
                .when()
                .put("/api/tables/{tableId}/cards/{cardId}")
                .then()
                .statusCode(404)
                .body("message", containsString(NOT_FOUND_CARD_IN_THE_TABLE_ERROR));
    }

    @Test
    void testMoveCardListFromTableToAnotherTable(){
        long tableId1 = actionCreateTable("Table 1", new ArrayList<>());
        long tableId2 = actionCreateTable("Table 2", new ArrayList<>());
        long cardId1 = actionCreateCard("Example Card 1", CardType.NORMAL);
        long cardId2 = actionCreateCard("Example Card 2", CardType.NORMAL);

        actionAddCardToTable(tableId1, cardId1);
        actionAddCardToTable(tableId1, cardId2, 2);

        given()
                .contentType(ContentType.JSON)
                .pathParams("tableId", tableId1)
                .queryParam("newTable", tableId2)
                .when()
                .put("/api/tables/{tableId}/cards")
                .then()
                .statusCode(200);

        Response responseTable1 = actionGetSpecificTableById(tableId1);
        responseTable1.then()
                .statusCode(200)
                .body("cardList.size()", equalTo(0));

        Response responseTable2 = actionGetSpecificTableById(tableId2);
        responseTable2.then()
                .statusCode(200)
                .body("cardList.size()", equalTo(2));

    }

    @Test
    void testMoveCardListFromTableWithInvalidOriginTableId() {
        long tableId1 = 12345L;
        long tableId2 = actionCreateTable("Table 2", new ArrayList<>());

        given()
                .contentType(ContentType.JSON)
                .pathParams("tableId", tableId1)
                .queryParam("newTable", tableId2)
                .when()
                .put("/api/tables/{tableId}/cards")
                .then()
                .statusCode(404)
                .body("message", containsString(NOT_FOUND_TABLE_WITH_ID_ERROR));
    }

    @Test
    void testMoveCardListFromTableWithInvalidDestinyTableId() {
        long tableId1 = actionCreateTable("Table 1", new ArrayList<>());
        long tableId2 = 12345L;

        given()
                .contentType(ContentType.JSON)
                .pathParams("tableId", tableId1)
                .queryParam("newTable", tableId2)
                .when()
                .put("/api/tables/{tableId}/cards")
                .then()
                .statusCode(404)
                .body("message", containsString(NOT_FOUND_TABLE_WITH_ID_ERROR));
    }

    @Test
    void testCopyCardListFromTableToAnotherTable(){
        long tableId1 = actionCreateTable("Table 1", new ArrayList<>());
        long tableId2 = actionCreateTable("Table 2", new ArrayList<>());
        long cardId1 = actionCreateCard("Example Card 1", CardType.NORMAL);
        long cardId2 = actionCreateCard("Example Card 2", CardType.NORMAL);

        actionAddCardToTable(tableId1, cardId1);
        actionAddCardToTable(tableId1, cardId2, 2);

        given()
                .contentType(ContentType.JSON)
                .pathParams("tableId", tableId1)
                .queryParam("newTable", tableId2)
                .when()
                .put("/api/tables/{tableId}/cards/copy")
                .then()
                .statusCode(200);

        Response responseTable1 = actionGetSpecificTableById(tableId1);
        responseTable1.then()
                .statusCode(200)
                .body("cardList.size()", equalTo(2));

        Response responseTable2 = actionGetSpecificTableById(tableId2);
        responseTable2.then()
                .statusCode(200)
                .body("cardList.size()", equalTo(2));

    }

    @Test
    void testCopyCardListFromTableWithInvalidOriginTableId() {
        long tableId1 = 12345L;
        long tableId2 = actionCreateTable("Table 2", new ArrayList<>());

        given()
                .contentType(ContentType.JSON)
                .pathParams("tableId", tableId1)
                .queryParam("newTable", tableId2)
                .when()
                .put("/api/tables/{tableId}/cards/copy")
                .then()
                .statusCode(404)
                .body("message", containsString(NOT_FOUND_TABLE_WITH_ID_ERROR));
    }

    @Test
    void testCopyCardListFromTableWithInvalidDestinyTableId() {
        long tableId1 = actionCreateTable("Table 1", new ArrayList<>());
        long tableId2 = 12345L;

        given()
                .contentType(ContentType.JSON)
                .pathParams("tableId", tableId1)
                .queryParam("newTable", tableId2)
                .when()
                .put("/api/tables/{tableId}/cards/copy")
                .then()
                .statusCode(404)
                .body("message", containsString(NOT_FOUND_TABLE_WITH_ID_ERROR));
    }

}
