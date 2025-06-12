package dev.kandv.kango.integrations.controllers;

import dev.kandv.kango.KangoApplication;
import dev.kandv.kango.dtos.TagDTO;
import dev.kandv.kango.models.enums.Color;
import dev.kandv.kango.models.enums.Visibility;
import dev.kandv.kango.services.TagService;
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

import static dev.kandv.kango.controllers.TagRestController.*;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;

@Testcontainers
@SpringBootTest(
        classes = KangoApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@ExtendWith(SpringExtension.class)
public class TagRestControllerTest {

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
    TagService tagService;

    String label = "Example Label";
    Color color = Color.GREEN;
    Visibility visibility = Visibility.GLOBAL;

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
        this.tagService.removeAllTags();
    }

    @Test
    void testCreateTag(){
        TagDTO tagDTO = new TagDTO(this.label, this.color, this.visibility);

        given()
                .contentType(ContentType.JSON)
                .body(tagDTO)
        .when()
                .post("/api/tags")
        .then()
                .statusCode(201);
    }

    @Test
    void testCreateTagWithInvalidLabel(){
        TagDTO tagDTO = new TagDTO(null, this.color, this.visibility);

        given()
                .contentType(ContentType.JSON)
                .body(tagDTO)
                .when()
                .post("/api/tags")
                .then()
                .statusCode(400)
                .body("message", containsString(INVALID_LABEL));
    }

    @Test
    void testCreateTagWithInvalidColor(){
        TagDTO tagDTO = new TagDTO(this.label, null, this.visibility);

        given()
                .contentType(ContentType.JSON)
                .body(tagDTO)
                .when()
                .post("/api/tags")
                .then()
                .statusCode(400)
                .body("message", containsString(INVALID_COLOR));
    }

    @Test
    void testCreateTagWithInvalidVisibility(){
        TagDTO tagDTO = new TagDTO(this.label, this.color, null);

        given()
                .contentType(ContentType.JSON)
                .body(tagDTO)
                .when()
                .post("/api/tags")
                .then()
                .statusCode(400)
                .body("message", containsString(INVALID_VISIBILITY));
    }

    @Test
    void testGetSpecificTagById(){
        long tagId = actionCreateTag();

        given()
                .pathParams("id", tagId)
                .when()
                .get("/api/tags/{id}", tagId)
                .then()
                .statusCode(200)
                .body("id", equalTo((int) tagId))
                .body("label", equalTo(this.label));
    }

    @Test
    void testGetSpecificTagByIdWithInvalidId(){
        long tagId = 12345L;

        given()
                .pathParams("id", tagId)
                .when()
                .get("/api/tags/{id}", tagId)
                .then()
                .statusCode(404)
                .body("message", containsString(TAG_NOT_FOUND));
    }

    @Test
    void testGetSpecificTagByIdWithNullId() {
        given()
                .when()
                .get("/api/tags/")
                .then()
                .statusCode(404);
    }

    @Test
    void testGetGlobalTags() {
        long globalTagId1 = actionCreateTag("EXAMPLE GLOBAL TAG 1", Color.BLUE, Visibility.GLOBAL);
        long globalTagId2 = actionCreateTag("EXAMPLE GLOBAL TAG 2", Color.BLUE, Visibility.GLOBAL);
        long globalTagId3 = actionCreateTag("EXAMPLE GLOBAL TAG 3", Color.BLUE, Visibility.GLOBAL);

        given()
                .when()
                .get("/api/global-tags")
                .then()
                .statusCode(200)
                .body("size()", equalTo(3))
                .body("[0].id", equalTo((int) globalTagId1))
                .body("[1].id", equalTo((int) globalTagId2))
                .body("[2].id", equalTo((int) globalTagId3));
    }

    @Test
    void testGetGlobalTagsWithEmptyList() {
        given()
                .when()
                .get("/api/global-tags")
                .then()
                .statusCode(200)
                .body("size()", equalTo(0));
    }

    @Test
    void testRemoveTag() {
        long tagId = actionCreateTag();

        Response foundResponse = actionGetTagById(tagId);
        foundResponse.then().statusCode(200);

        given()
                .pathParams("id", tagId)
                .when()
                .delete("/api/tags/{id}", tagId)
                .then()
                .statusCode(200);

        Response notFoundResponse = actionGetTagById(tagId);
        notFoundResponse.then().statusCode(404);
    }

    @Test
    void testRemoveCardWithInvalidId() {
        long tagId = 12345L;

        given()
                .pathParams("id", tagId)
                .when()
                .delete("/api/tags/{id}", tagId)
                .then()
                .statusCode(404)
                .body("message", containsString(TAG_NOT_FOUND));
    }

    @Test
    void testUpdateTag(){
        long tagId = actionCreateTag();

        TagDTO tagDTO = new TagDTO("New Label", Color.PURPLE, this.visibility);

        given()
                .pathParams("id", tagId)
                .contentType(ContentType.JSON)
                .body(tagDTO)
        .when()
                .put("/api/tags/{id}", tagId)
        .then()
                .statusCode(200)
                .body("id", equalTo((int) tagId))
                .body("label", equalTo(tagDTO.getLabel()))
                .body("visibility", equalTo(this.visibility.toString()))
                .body("color", equalTo(tagDTO.getColor().toString()));

    }

    @Test
    void testUpdateTagWithInvalidId(){
        long tagId = 12345L;

        TagDTO tagDTO = new TagDTO("New Label", Color.PURPLE, this.visibility);

        given()
                .pathParams("id", tagId)
                .contentType(ContentType.JSON)
                .body(tagDTO)
                .when()
                .put("/api/tags/{id}", tagId)
                .then()
                .statusCode(404)
                .body("message", containsString(TAG_NOT_FOUND));
    }

    @Test
    void testUpdateTagWithInvalidColor(){
        long tagId = actionCreateTag();

        TagDTO tagDTO = new TagDTO("New Label", null, this.visibility);

        given()
                .pathParams("id", tagId)
                .contentType(ContentType.JSON)
                .body(tagDTO)
                .when()
                .put("/api/tags/{id}", tagId)
                .then()
                .statusCode(400)
                .body("message", containsString(INVALID_COLOR));
    }

    @Test
    void testUpdateTagWithInvalidLabel(){
        long tagId = actionCreateTag();

        TagDTO tagDTO = new TagDTO(null, Color.BLUE, this.visibility);

        given()
                .pathParams("id", tagId)
                .contentType(ContentType.JSON)
                .body(tagDTO)
                .when()
                .put("/api/tags/{id}", tagId)
                .then()
                .statusCode(400)
                .body("message", containsString(INVALID_LABEL));
    }

    long actionCreateTag() {
        return actionCreateTag(this.label, this.color, this.visibility);
    }

    long actionCreateTag(String label, Color color, Visibility visibility) {
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

    private Response actionGetTagById(long tagId) {
        return  given()
                .pathParams("id", tagId)
                .when()
                .get("/api/tags/{id}", tagId);
    }

}
