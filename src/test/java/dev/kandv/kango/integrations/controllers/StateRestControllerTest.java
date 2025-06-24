package dev.kandv.kango.integrations.controllers;

import dev.kandv.kango.KangoApplication;
import dev.kandv.kango.dtos.StateDTO;
import dev.kandv.kango.models.State;
import dev.kandv.kango.services.StateService;
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

import static dev.kandv.kango.controllers.StateRestController.*;
import static dev.kandv.kango.integrations.controllers.StateRestControllerUtils.actionCreateState;
import static dev.kandv.kango.integrations.controllers.StateRestControllerUtils.actionGetState;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;

import static dev.kandv.kango.controllers.ErrorMessagesRestControllers.STATE_NOT_FOUND;

@Testcontainers
@SpringBootTest(
        classes = KangoApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@ExtendWith(SpringExtension.class)
public class StateRestControllerTest {
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
    StateService stateService;

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
        this.stateService.removeState();
    }

    @Test
    void testCreateState(){
        given()
                .contentType(ContentType.JSON)
                .when()
                .post("/api/state")
                .then()
                .statusCode(201);
    }

    @Test
    void testGetState(){
        long stateId = actionCreateState();

        given()
                .when()
                .get("/api/state")
                .then()
                .statusCode(200)
                .body("id", equalTo((int) stateId));
    }

    @Test
    void testGetStateWithNoState(){

        given()
                .when()
                .get("/api/state")
                .then()
                .statusCode(404)
                .body("message", equalTo(STATE_NOT_FOUND));
    }

    @Test
    void testRemoveState() {
        actionCreateState();

        Response foundResponse = actionGetState();
        foundResponse.then().statusCode(200);

        given().when()
                .delete("/api/state")
                .then()
                .statusCode(200);

        Response notFoundResponse = actionGetState();
        notFoundResponse.then().statusCode(404);
    }

    @Test
    void testRemoveStateWithoutState() {
        given().when()
                .delete("/api/state")
                .then()
                .statusCode(404)
                .body("message", equalTo(STATE_NOT_FOUND));
    }

    @Test
    void testCheckStateWithTrue(){
        actionCreateState();

        given()
                .when()
                .get("/api/state/check")
                .then()
                .statusCode(204);
    }

    @Test
    void testCheckStateWithFalse(){
        given()
                .when()
                .get("/api/state/check")
                .then()
                .statusCode(404);
    }

    @Test
    void testUpdateFontSizeState(){
        long stateId = actionCreateState();
        State.FontSize newFontSize = State.FontSize.SMALL;
        StateDTO newStateDTO = new StateDTO();
        newStateDTO.setFontSize(newFontSize);

        given()
                .contentType(ContentType.JSON)
                .body(newStateDTO)
                .when()
                .put("/api/state/font-size")
                .then()
                .statusCode(200)
                .body("fontSize", equalTo(newFontSize.toString()))
                .body("id", equalTo((int) stateId));
    }

    @Test
    void testUpdateFontSizeStatedWithInvalidFontSize() {
        actionCreateState();
        StateDTO newStateDTO = new StateDTO();
        newStateDTO.setFontSize(null);

        given()
                .contentType(ContentType.JSON)
                .body(newStateDTO)
                .when()
                .put("/api/state/font-size")
                .then()
                .statusCode(400)
                .body("message", containsString(INVALID_FONT_SIZE_ERROR));
    }

    @Test
    void testUpdateFontSizeStateWitNoState() {
        State.FontSize newFontSize = State.FontSize.SMALL;
        StateDTO newStateDTO = new StateDTO();
        newStateDTO.setFontSize(newFontSize);

        given()
                .contentType(ContentType.JSON)
                .body(newStateDTO)
                .when()
                .put("/api/state/font-size")
                .then()
                .statusCode(404)
                .body("message", containsString(STATE_NOT_FOUND));
    }

    @Test
    void testUpdateLanguageState(){
        long stateId = actionCreateState();
        State.Language newLanguage = State.Language.ENGLISH;
        StateDTO newStateDTO = new StateDTO();
        newStateDTO.setLanguage(newLanguage);

        given()
                .contentType(ContentType.JSON)
                .body(newStateDTO)
                .when()
                .put("/api/state/language")
                .then()
                .statusCode(200)
                .body("language", equalTo(newLanguage.toString()))
                .body("id", equalTo((int) stateId));
    }

    @Test
    void testUpdateLanguageStatedWithInvalidLanguage() {
        actionCreateState();
        StateDTO newStateDTO = new StateDTO();
        newStateDTO.setLanguage(null);

        given()
                .contentType(ContentType.JSON)
                .body(newStateDTO)
                .when()
                .put("/api/state/language")
                .then()
                .statusCode(400)
                .body("message", containsString(INVALID_LANGUAGE_ERROR));
    }

    @Test
    void testUpdateLanguageStateWitNoState() {
        State.Language newLanguage = State.Language.ENGLISH;
        StateDTO newStateDTO = new StateDTO();
        newStateDTO.setLanguage(newLanguage);

        given()
                .contentType(ContentType.JSON)
                .body(newStateDTO)
                .when()
                .put("/api/state/language")
                .then()
                .statusCode(404)
                .body("message", containsString(STATE_NOT_FOUND));
    }

    @Test
    void testUpdateColorBlindState(){
        long stateId = actionCreateState();
        State.ColorBlind newColorBlind = State.ColorBlind.NONE;
        StateDTO newStateDTO = new StateDTO();
        newStateDTO.setColorBlind(newColorBlind);

        given()
                .contentType(ContentType.JSON)
                .body(newStateDTO)
                .when()
                .put("/api/state/color-blind")
                .then()
                .statusCode(200)
                .body("colorBlind", equalTo(newColorBlind.toString()))
                .body("id", equalTo((int) stateId));
    }

    @Test
    void testUpdateColorBlindStatedWithInvalidColorBlind() {
        actionCreateState();
        StateDTO newStateDTO = new StateDTO();
        newStateDTO.setColorBlind(null);

        given()
                .contentType(ContentType.JSON)
                .body(newStateDTO)
                .when()
                .put("/api/state/color-blind")
                .then()
                .statusCode(400)
                .body("message", containsString(INVALID_COLOR_BLIND_ERROR));
    }

    @Test
    void testUpdateColorBlindStateWitNoState() {
        State.ColorBlind newColorBlind = State.ColorBlind.NONE;
        StateDTO newStateDTO = new StateDTO();
        newStateDTO.setColorBlind(newColorBlind);

        given()
                .contentType(ContentType.JSON)
                .body(newStateDTO)
                .when()
                .put("/api/state/color-blind")
                .then()
                .statusCode(404)
                .body("message", containsString(STATE_NOT_FOUND));
    }
}
