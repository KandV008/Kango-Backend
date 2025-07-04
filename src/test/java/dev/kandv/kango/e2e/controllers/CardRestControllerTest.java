package dev.kandv.kango.e2e.controllers;

import dev.kandv.kango.KangoApplication;
import dev.kandv.kango.dtos.CardDTO;
import dev.kandv.kango.models.Tag;
import dev.kandv.kango.models.enums.CardType;
import dev.kandv.kango.models.enums.Color;
import dev.kandv.kango.models.utils.AttachedFile;
import dev.kandv.kango.models.utils.Check;
import dev.kandv.kango.services.CardService;
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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import static dev.kandv.kango.controllers.CardRestController.*;
import static dev.kandv.kango.controllers.ErrorMessagesRestControllers.*;
import static dev.kandv.kango.e2e.controllers.CardRestControllerUtils.actionCreateCard;
import static dev.kandv.kango.e2e.controllers.CardRestControllerUtils.actionGetSpecificCardById;
import static dev.kandv.kango.e2e.controllers.TagRestControllerUtils.actionCreateTag;
import static dev.kandv.kango.models.Card.NOT_FOUND_CHECK_ERROR;
import static dev.kandv.kango.services.CardService.NOT_FOUND_ELEMENT_IN_CARD_ERROR;
import static dev.kandv.kango.services.ErrorMessagesServices.NOT_FOUND_CARD_WITH_ID_ERROR;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@Testcontainers
@SpringBootTest(
        classes = KangoApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@ExtendWith(SpringExtension.class)
class CardRestControllerTest {

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

    String cardTitle = CardRestControllerUtils.cardTitle;
    CardType cardType = CardRestControllerUtils.cardType;

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
        this.cardService.removeAllCards();
    }

    @Test
    void testCreateCard(){
        CardDTO cardDTO = new CardDTO(this.cardTitle, this.cardType);

        given()
            .contentType(ContentType.JSON)
            .body(cardDTO)
        .when()
                .post("/api/cards")
        .then()
                .statusCode(201);
    }

    @Test
    void testCreateCardWithInvalidTitle() {
        CardDTO dto = new CardDTO("", this.cardType);

        given()
                .contentType(ContentType.JSON)
                .body(dto)
        .when()
                .post("/api/cards")
        .then()
                .statusCode(400)
                .body("message", containsString(INVALID_TITLE));

    }

    @Test
    void testCreateCardWithInvalidCardType() {
        String invalidJson = """
            {
                "title": "Invalid Enum",
                "cardType": "INVALID_TYPE"
            }
        """;

        given()
                .contentType(ContentType.JSON)
                .body(invalidJson)
        .when()
                .post("/api/cards")
        .then()
                .statusCode(400);
    }

    @Test
    void testCreateCardWithNullTitle() {
        CardDTO dto = new CardDTO(null, this.cardType);

        given()
                .contentType(ContentType.JSON)
                .body(dto)
        .when()
                .post("/api/cards")
        .then()
                .statusCode(400)
                .body("message", containsString(INVALID_TITLE));
    }

    @Test
    void testCreateCardWithNullCardType() {
        CardDTO cardDTO = new CardDTO(this.cardTitle, null);

        given()
                .contentType(ContentType.JSON)
                .body(cardDTO)
        .when()
                .post("/api/cards")
        .then()
                .statusCode(400)
                .body("message", containsString(INVALID_CARD_TYPE));
    }

    @Test
    void testCreateCardUsingATemplate(){
        long cardId = actionCreateCard("EXAMPLE TEMPLATE CARD", CardType.GLOBAL_TEMPLATE);

        given()
                .pathParams("id", cardId)
                .when()
                .post("/api/cards/{id}/copy", cardId)
                .then()
                .statusCode(201);
    }

    @Test
    void testCreateCardUsingATemplateWithNotFoundCard(){
        long cardId = 12345L;

        given()
                .pathParams("id", cardId)
                .when()
                .post("/api/cards/{id}/copy", cardId)
                .then()
                .statusCode(404)
                .body("message", containsString(CARD_NOT_FOUND));
    }

    @Test
    void testGetSpecificCardById(){
        long cardId = actionCreateCard();

        given()
                .pathParams("id", cardId)
        .when()
                .get("/api/cards/{id}", cardId)
        .then()
                .statusCode(200)
                .body("id", equalTo((int) cardId))
                .body("title", equalTo(cardTitle));
    }

    @Test
    void testGetSpecificCardByIdWithInvalidId(){
        long cardId = 12345L;

        given()
                .pathParams("id", cardId)
        .when()
                .get("/api/cards/{id}", cardId)
        .then()
                .statusCode(404)
                .body("message", containsString(CARD_NOT_FOUND));
    }

    @Test
    void testGetSpecificCardByIdWithNullId() {
        given()
        .when()
                .get("/api/cards/")
        .then()
                .statusCode(404);
    }

    @Test
    void testRemoveCard() {
        long cardId = actionCreateCard();

        Response foundResponse = actionGetSpecificCardById(cardId);
        foundResponse.then().statusCode(200);

        given()
                .pathParams("id", cardId)
        .when()
                .delete("/api/cards/{id}", cardId)
        .then()
                .statusCode(200);

        Response notFoundResponse = actionGetSpecificCardById(cardId);
        notFoundResponse.then().statusCode(404);
    }

    @Test
    void testRemoveCardWithInvalidId() {
        long cardId = 12345L;

        given()
                .pathParams("id", cardId)
        .when()
                .delete("/api/cards/{id}", cardId)
        .then()
                .statusCode(404)
                .body("message", containsString(CARD_NOT_FOUND));
    }

    @Test
    void testGetGlobalTemplateCards() {
        long templateCardId1 = actionCreateCard("EXAMPLE GLOBAL TEMPLATE 1", CardType.GLOBAL_TEMPLATE);
        long templateCardId2 = actionCreateCard("EXAMPLE GLOBAL TEMPLATE 2", CardType.GLOBAL_TEMPLATE);
        long templateCardId3 = actionCreateCard("EXAMPLE GLOBAL TEMPLATE 3", CardType.GLOBAL_TEMPLATE);

        given()

        .when()
                .get("/api/global-template-cards")
        .then()
                .statusCode(200)
                .body("size()", equalTo(3))
                .body("[0].id", equalTo((int) templateCardId1))
                .body("[1].id", equalTo((int) templateCardId2))
                .body("[2].id", equalTo((int) templateCardId3));
    }

    @Test
    void testGetGlobalTemplateCardsWithEmptyList() {
        given()
        .when()
                .get("/api/global-template-cards")
        .then()
                .statusCode(200)
                .body("size()", equalTo(0));
    }

    @Test
    void testUpdateTitleCard(){
        long cardId = actionCreateCard();
        String newTitle = "New Title";
        CardDTO newTitleDTO = new CardDTO();
        newTitleDTO.setTitle(newTitle);

        given()
                .pathParams("id", cardId)
                .contentType(ContentType.JSON)
                .body(newTitleDTO)
        .when()
                .put("/api/cards/{id}/title", cardId)
        .then()
                .statusCode(200)
                .body("title", equalTo(newTitle))
                .body("id", equalTo((int) cardId));
    }

    @Test
    void testUpdateTitleCardWithInvalidTitle() {
        long cardId = actionCreateCard();
        CardDTO newTitleDTO = new CardDTO();
        newTitleDTO.setTitle("");

        given()
                .pathParams("id", cardId)
                .contentType(ContentType.JSON)
                .body(newTitleDTO)
        .when()
                .put("/api/cards/{id}/title", cardId)
        .then()
                .statusCode(400)
                .body("message", containsString(INVALID_TITLE));
    }

    @Test
    void testUpdateTitleCardWithNullTitle() {
        long cardId = actionCreateCard();
        CardDTO newTitleDTO = new CardDTO();
        newTitleDTO.setTitle(null);

        given()
                .pathParams("id", cardId)
                .contentType(ContentType.JSON)
                .body(newTitleDTO)
        .when()
                .put("/api/cards/{id}/title", cardId)
        .then()
                .statusCode(400)
                .body("message", containsString(INVALID_TITLE));
    }

    @Test
    void testUpdateTitleCardWithInvalidId() {
        long cardId = 12345L;
        CardDTO newTitleDTO = new CardDTO();
        newTitleDTO.setTitle("New Title");

        given()
                .pathParams("id", cardId)
                .contentType(ContentType.JSON)
                .body(newTitleDTO)
        .when()
                .put("/api/cards/{id}/title", cardId)
        .then()
                .statusCode(404)
                .body("message", containsString(CARD_NOT_FOUND));
    }

    @Test
    void testUpdateDescriptionCard(){
        long cardId = actionCreateCard();
        String newDescription = "New Description";
        CardDTO newDescriptionDTO = new CardDTO();
        newDescriptionDTO.setDescription(newDescription);

        given()
                .pathParams("id", cardId)
                .contentType(ContentType.JSON)
                .body(newDescriptionDTO)
        .when()
                .put("/api/cards/{id}/description", cardId)
        .then()
                .statusCode(200)
                .body("description", equalTo(newDescription))
                .body("id", equalTo((int) cardId));
    }

    @Test
    void testUpdateDescriptionCardWithInvalidDescription() {
        long cardId = actionCreateCard();
        String newDescription = "";
        CardDTO newDescriptionDTO = new CardDTO();
        newDescriptionDTO.setDescription(newDescription);

        given()
                .pathParams("id", cardId)
                .contentType(ContentType.JSON)
                .body(newDescriptionDTO)
        .when()
                .put("/api/cards/{id}/description", cardId)
        .then()
                .statusCode(400)
                .body("message", containsString(INVALID_DESCRIPTION));

    }

    @Test
    void testUpdateDescriptionCardWithNullDescription() {
        long cardId = actionCreateCard();
        CardDTO newDescriptionDTO = new CardDTO();
        newDescriptionDTO.setDescription(null);

        given()
                .pathParams("id", cardId)
                .contentType(ContentType.JSON)
                .body(newDescriptionDTO)
        .when()
                .put("/api/cards/{id}/description", cardId)
        .then()
                .statusCode(400)
                .body("message", containsString(INVALID_DESCRIPTION));

    }

    @Test
    void testUpdateDescriptionCardWithInvalidId() {
        long cardId = 12345L;
        CardDTO newDescriptionDTO = new CardDTO();
        newDescriptionDTO.setDescription("Example description");

        given()
                .pathParams("id", cardId)
                .contentType(ContentType.JSON)
                .body(newDescriptionDTO)
        .when()
                .put("/api/cards/{id}/description", cardId)
        .then()
                .statusCode(404)
                .body("message", containsString(CARD_NOT_FOUND));

    }

    @Test
    void testUpdateColorCard(){
        long cardId = actionCreateCard();
        Color newColor = Color.RED;
        CardDTO newColorDTO = new CardDTO();
        newColorDTO.setColor(newColor);

        given()
        .pathParams("id", cardId)
                .contentType(ContentType.JSON)
                .body(newColorDTO)
        .when()
                .put("/api/cards/{id}/color", cardId)
        .then()
                .statusCode(200)
                .body("color", equalTo(newColor.toString()))
                .body("id", equalTo((int) cardId));
    }

    @Test
    void testUpdateColorCardWithInvalidColor() {
        long cardId = actionCreateCard();
        CardDTO newColorDTO = new CardDTO();
        newColorDTO.setColor(null);

        given()
                .pathParams("id", cardId)
                .contentType(ContentType.JSON)
                .body(newColorDTO)
                .when()
                .put("/api/cards/{id}/color", cardId)
                .then()
                .statusCode(400)
                .body("message", containsString(INVALID_COLOR));
    }

    @Test
    void testUpdateColorCardWithInvalidId() {
        long cardId = 12345L;
        Color newColor = Color.RED;
        CardDTO newColorDTO = new CardDTO();
        newColorDTO.setColor(newColor);

        given()
                .pathParams("id", cardId)
                .contentType(ContentType.JSON)
                .body(newColorDTO)
                .when()
                .put("/api/cards/{id}/color", cardId)
                .then()
                .statusCode(404)
                .body("message", containsString(CARD_NOT_FOUND));
    }

    @Test
    void testUpdateDeadLineCard(){
        long cardId = actionCreateCard();
        Date deadline = new Date();
        CardDTO newDeadlineDTO = new CardDTO();
        newDeadlineDTO.setDeadLine(deadline);

        SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        isoFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        String formattedDeadline = isoFormat.format(deadline);

        given()
                .pathParams("id", cardId)
                .contentType(ContentType.JSON)
                .body(newDeadlineDTO)
        .when()
                .put("/api/cards/{id}/deadline", cardId)
        .then()
                .statusCode(200)
                .body("deadLine", startsWith(formattedDeadline))
                .body("id", equalTo((int) cardId));
    }

    @Test
    void testUpdateDeadLineCardWithInvalidDeadline() {
        long cardId = actionCreateCard();
        CardDTO newDeadlineDTO = new CardDTO();
        newDeadlineDTO.setDeadLine(null);

        given()
                .pathParams("id", cardId)
                .contentType(ContentType.JSON)
                .body(newDeadlineDTO)
                .when()
                .put("/api/cards/{id}/deadline", cardId)
                .then()
                .statusCode(400)
                .body("message", containsString(INVALID_DEAD_LINE));
    }

    @Test
    void testUpdateDeadLineCardWithInvalidId() {
        long cardId = 12345L;
        Date deadline = new Date();
        CardDTO newDeadlineDTO = new CardDTO();
        newDeadlineDTO.setDeadLine(deadline);

        given()
                .pathParams("id", cardId)
                .contentType(ContentType.JSON)
                .body(newDeadlineDTO)
                .when()
                .put("/api/cards/{id}/deadline", cardId)
                .then()
                .statusCode(404)
                .body("message", containsString(CARD_NOT_FOUND));
    }

    @Test
    void testAttachFileToCard(){
        long cardId = actionCreateCard();
        AttachedFile attachedFile = new AttachedFile("example.png", "/example.png");

        this.actionAttachFileToCard(cardId, attachedFile);
    }

    @Test
    void testAttachFileToCardWithInvalidAttachedFile() {
        long cardId = actionCreateCard();
        AttachedFile attachedFile = new AttachedFile("", "");

        given()
                .pathParams("id", cardId)
                .contentType(ContentType.JSON)
                .body(attachedFile)
                .when()
                .post("/api/cards/{id}/attached-files", cardId)
                .then()
                .statusCode(400)
                .body("message", containsString(INVALID_ATTACHED_FILE));
    }

    @Test
    void testAttachFileToCardWithInvalidId() {
        long cardId = 12345L;
        AttachedFile attachedFile = new AttachedFile("example.png", "/example.png");

        given()
                .pathParams("id", cardId)
                .contentType(ContentType.JSON)
                .body(attachedFile)
                .when()
                .post("/api/cards/{id}/attached-files", cardId)
                .then()
                .statusCode(404)
                .body("message", containsString(CARD_NOT_FOUND));
    }

    @Test
    void testDetachFileFromCard(){
        long cardId = actionCreateCard();
        AttachedFile attachedFile = new AttachedFile("example.png", "/example.png");

        this.actionAttachFileToCard(cardId, attachedFile);

        given()
        .pathParams("id", cardId)
                .contentType(ContentType.JSON)
                .body(attachedFile)
                .when()
                .delete("/api/cards/{id}/attached-files", cardId)
                .then()
                .statusCode(200)
                .body("id", equalTo((int) cardId))
                .body("attachedFiles.size()", equalTo(0));
    }

    @Test
    void testDetachFileToCardWithInvalidAttachedFile() {
        long cardId = actionCreateCard();
        AttachedFile attachedFile = new AttachedFile("", "");

        given()
                .pathParams("id", cardId)
                .contentType(ContentType.JSON)
                .body(attachedFile)
                .when()
                .delete("/api/cards/{id}/attached-files", cardId)
                .then()
                .statusCode(400)
                .body("message", containsString(INVALID_ATTACHED_FILE));
    }

    @Test
    void testDetachFileToCardWithInexistentAttachedFile() {
        long cardId = actionCreateCard();
        AttachedFile attachedFile = new AttachedFile("example.png", "/example.png");

        given()
                .pathParams("id", cardId)
                .contentType(ContentType.JSON)
                .body(attachedFile)
                .when()
                .delete("/api/cards/{id}/attached-files", cardId)
                .then()
                .statusCode(404)
                .body("message", containsString(NOT_FOUND_ELEMENT_IN_CARD_ERROR));
    }

    @Test
    void testDetachFileFromCardWithInvalidId() {
        long cardId = 12345L;
        AttachedFile attachedFile = new AttachedFile("example.png", "/example.png");

        given()
                .pathParams("id", cardId)
                .contentType(ContentType.JSON)
                .body(attachedFile)
                .when()
                .delete("/api/cards/{id}/attached-files", cardId)
                .then()
                .statusCode(404)
                .body("message", containsString(NOT_FOUND_CARD_WITH_ID_ERROR));
    }

    @Test
    void testAddCheckToCard(){
        long cardId = actionCreateCard();
        Check check = new Check("Example Label", false);

        this.actionAddCheckToCard(cardId, check);
    }

    @Test
    void testAddCheckToCardWithInvalidCheck() {
        long cardId = actionCreateCard();
        Check attachedFile = new Check("", false);

        given()
                .pathParams("id", cardId)
                .contentType(ContentType.JSON)
                .body(attachedFile)
                .when()
                .post("/api/cards/{id}/checks", cardId)
                .then()
                .statusCode(400)
                .body("message", containsString(INVALID_CHECK));
    }

    @Test
    void testAddCheckToCardWithInvalidId() {
        long cardId = 12345L;
        Check attachedFile = new Check("Example", false);

        given()
                .pathParams("id", cardId)
                .contentType(ContentType.JSON)
                .body(attachedFile)
                .when()
                .post("/api/cards/{id}/checks", cardId)
                .then()
                .statusCode(404)
                .body("message", containsString(CARD_NOT_FOUND));
    }

    @Test
    void testRemoveCheckFromCard(){
        long cardId = actionCreateCard();
        Check check = new Check("Example Label", false);

        this.actionAddCheckToCard(cardId, check);

        given()
                .pathParams("id", cardId)
                .contentType(ContentType.JSON)
                .body(check)
                .when()
                .delete("/api/cards/{id}/checks", cardId)
                .then()
                .statusCode(200)
                .body("id", equalTo((int) cardId))
                .body("checks.size()", equalTo(0));
    }

    @Test
    void testRemoveCheckFromCardWithInvalidCheck() {
        long cardId = actionCreateCard();
        Check check = new Check("", false);

        given()
                .pathParams("id", cardId)
                .contentType(ContentType.JSON)
                .body(check)
                .when()
                .delete("/api/cards/{id}/checks", cardId)
                .then()
                .statusCode(400)
                .body("message", containsString(INVALID_CHECK));
    }

    @Test
    void testRemoveCheckFromCardWithInexistentCheck() {
        long cardId = actionCreateCard();
        Check check = new Check("Example", false);

        given()
                .pathParams("id", cardId)
                .contentType(ContentType.JSON)
                .body(check)
                .when()
                .delete("/api/cards/{id}/checks", cardId)
                .then()
                .statusCode(404)
                .body("message", containsString(NOT_FOUND_ELEMENT_IN_CARD_ERROR));
    }

    @Test
    void testRemoveCheckFromCardWithInvalidId() {
        long cardId = 12345L;
        Check check = new Check("Example", false);

        given()
                .pathParams("id", cardId)
                .contentType(ContentType.JSON)
                .body(check)
                .when()
                .delete("/api/cards/{id}/checks", cardId)
                .then()
                .statusCode(404)
                .body("message", containsString(NOT_FOUND_CARD_WITH_ID_ERROR));
    }

    @Test
    void testUpdateCheckFromCard(){
        long cardId = actionCreateCard();
        Check check = new Check("Example Label", false);
        this.actionAddCheckToCard(cardId, check);

        check.setChecked(true);
        check.setPosition(0);

        given()
        .pathParams("id", cardId)
                .contentType(ContentType.JSON)
                .body(check)
                .when()
                .put("/api/cards/{id}/checks", cardId)
                .then()
                .statusCode(200)
                .body("id", equalTo((int) cardId))
                .body("checks.size()", equalTo(1));
    }

    @Test
    void testUpdateCheckFromCardWithInexistentCheck() {
        long cardId = actionCreateCard();
        Check check = new Check("Example Label", false);

        given()
                .pathParams("id", cardId)
                .contentType(ContentType.JSON)
                .body(check)
                .when()
                .put("/api/cards/{id}/checks", cardId)
                .then()
                .statusCode(404)
                .body("message", containsString(NOT_FOUND_CHECK_ERROR));
    }

    @Test
    void testUpdateCheckFromCardWithInvalidCheck() {
        long cardId = actionCreateCard();
        Check check = new Check("", false);

        given()
                .pathParams("id", cardId)
                .contentType(ContentType.JSON)
                .body(check)
                .when()
                .put("/api/cards/{id}/checks", cardId)
                .then()
                .statusCode(400)
                .body("message", containsString(INVALID_CHECK));
    }

    @Test
    void testUpdateCheckFromCardWithInvalidId() {
        long cardId = 12345L;
        Check check = new Check("Example", false);

        given()
                .pathParams("id", cardId)
                .contentType(ContentType.JSON)
                .body(check)
                .when()
                .put("/api/cards/{id}/checks", cardId)
                .then()
                .statusCode(404)
                .body("message", containsString(NOT_FOUND_CARD_WITH_ID_ERROR));
    }

    @Test
    void testAddTagToCard(){
        long cardId = actionCreateCard();
        Tag tag = new Tag("Example Label", Color.BLUE);
        long tagId = actionCreateTag(tag.getLabel(), tag.getColor(), tag.getVisibility());

        this.actionAddTagToCard(cardId, tagId);
    }

    @Test
    void testAddTagToCardWithInvalidTag() {
        long cardId = actionCreateCard();
        long tagId = 12345L;

        given()
                .pathParams("id", cardId)
                .contentType(ContentType.JSON)
                .body(tagId)
                .when()
                .post("/api/cards/{id}/tags", cardId)
                .then()
                .statusCode(404)
                .body("message", containsString(TAG_NOT_FOUND));
    }

    @Test
    void testAddTagToCardWithInvalidId() {
        long cardId = 12345L;
        Tag tag = new Tag("Example Label", Color.BLUE);
        long tagId = actionCreateTag(tag.getLabel(), tag.getColor(), tag.getVisibility());

        given()
                .pathParams("id", cardId)
                .contentType(ContentType.JSON)
                .body(tagId)
                .when()
                .post("/api/cards/{id}/tags", cardId)
                .then()
                .statusCode(404)
                .body("message", containsString(CARD_NOT_FOUND));
    }

    @Test
    void testRemoveTagFromCard(){
        long cardId = actionCreateCard();
        Tag tag = new Tag("Example Label", Color.BLUE);
        long tagId = actionCreateTag(tag.getLabel(), tag.getColor(), tag.getVisibility());

        this.actionAddTagToCard(cardId, tagId);

        given()
                .pathParams("id", cardId)
                .contentType(ContentType.JSON)
                .body(tagId)
                .when()
                .delete("/api/cards/{id}/tags", cardId)
                .then()
                .statusCode(200)
                .body("id", equalTo((int) cardId))
                .body("tagList.size()", equalTo(0));
    }

    @Test
    void testRemoveTagFromCardWithInvalidTag() {
        long cardId = actionCreateCard();
        long tagId = 12345L;

        given()
                .pathParams("id", cardId)
                .contentType(ContentType.JSON)
                .body(tagId)
                .when()
                .delete("/api/cards/{id}/tags", cardId)
                .then()
                .statusCode(404)
                .body("message", containsString(TAG_NOT_FOUND));
    }

    @Test
    void testRemoveTagFromCardWithInexistentTag() {
        long cardId = actionCreateCard();
        Tag tag = new Tag("Example Label", Color.BLUE);
        long tagId = actionCreateTag(tag.getLabel(), tag.getColor(), tag.getVisibility());

        given()
                .pathParams("id", cardId)
                .contentType(ContentType.JSON)
                .body(tagId)
                .when()
                .delete("/api/cards/{id}/tags", cardId)
                .then()
                .statusCode(404)
                .body("message", containsString(NOT_FOUND_ELEMENT_IN_CARD_ERROR));
    }

    @Test
    void testRemoveTagFromCardWithInvalidId() {
        long cardId = 12345L;
        Tag tag = new Tag("Example Label", Color.BLUE);
        long tagId = actionCreateTag(tag.getLabel(), tag.getColor(), tag.getVisibility());

        given()
                .pathParams("id", cardId)
                .contentType(ContentType.JSON)
                .body(tagId)
                .when()
                .delete("/api/cards/{id}/tags", cardId)
                .then()
                .statusCode(404)
                .body("message", containsString(NOT_FOUND_CARD_WITH_ID_ERROR));
    }

    private void actionAttachFileToCard(long cardId, AttachedFile attachedFile) {
        given()
                .pathParams("id", cardId)
                .contentType(ContentType.JSON)
                .body(attachedFile)
                .when()
                .post("/api/cards/{id}/attached-files", cardId)
                .then()
                .statusCode(201)
                .body("id", equalTo((int) cardId))
                .body("attachedFiles.size()", equalTo(1));
    }

    private void actionAddCheckToCard(long cardId, Check check) {
        given()
                .pathParams("id", cardId)
                .contentType(ContentType.JSON)
                .body(check)
                .when()
                .post("/api/cards/{id}/checks", cardId)
                .then()
                .statusCode(201)
                .body("id", equalTo((int) cardId))
                .body("checks.size()", equalTo(1));
    }

    private void actionAddTagToCard(long cardId, long tagId) {
        given()
                .pathParams("id", cardId)
                .contentType(ContentType.JSON)
                .body(tagId)
                .when()
                .post("/api/cards/{id}/tags", cardId)
                .then()
                .statusCode(201)
                .body("id", equalTo((int) cardId))
                .body("tagList.size()", equalTo(1));
    }

}
