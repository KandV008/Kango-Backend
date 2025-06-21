package dev.kandv.kango.integrations.services;

import dev.kandv.kango.KangoApplication;
import dev.kandv.kango.models.Card;
import dev.kandv.kango.models.Dashboard;
import dev.kandv.kango.models.Table;
import dev.kandv.kango.models.Tag;
import dev.kandv.kango.models.enums.CardType;
import dev.kandv.kango.models.enums.Color;
import dev.kandv.kango.models.enums.Visibility;
import dev.kandv.kango.models.utils.AttachedFile;
import dev.kandv.kango.services.CardService;
import dev.kandv.kango.services.DashboardService;
import dev.kandv.kango.services.TableService;
import dev.kandv.kango.services.TagService;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.NoSuchElementException;

import static dev.kandv.kango.services.DashboardService.*;
import static dev.kandv.kango.services.TableService.INVALID_ELEMENT_ERROR;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Testcontainers
@SpringBootTest(classes = KangoApplication.class)
@ExtendWith(SpringExtension.class)
public class DashboardServiceTest {

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

    @Autowired
    private DashboardService dashboardService;

    @Autowired
    private TableService tableService;

    @Autowired
    private CardService cardService;

    @Autowired
    private TagService tagService;

    Dashboard dashboard;
    Table table;
    Card card;
    Tag tag;

    @BeforeAll
    static void beforeAll(){
        postgreSQLContainer.start();
    }

    @AfterAll
    static void afterAll(){
        postgreSQLContainer.stop();
    }

    @BeforeEach
    void beforeEach(){
        this.dashboard = new Dashboard("EXAMPLE DASHBOARD");
        this.table = new Table("EXAMPLE TABLE");
        this.card = new Card("EXAMPLE CARD", CardType.LOCAL_TEMPLATE);
        this.tag = new Tag(Color.ORANGE, Visibility.LOCAL);
    }

    @AfterEach
    void afterEach(){
        this.dashboardService.removeAllDashboards();
        this.tableService.removeAllTables();
        this.cardService.removeAllCards();
        this.tagService.removeAllTags();
    }

    @Test
    void testCreateDashboard(){
        Dashboard expectedDashboard = this.dashboardService.createDashboard(this.dashboard);

        assertThat(expectedDashboard.getName()).isEqualTo(this.dashboard.getName());
        assertThat(expectedDashboard.getAttachedFiles()).hasSize(0);
        assertThat(expectedDashboard.getTableList()).hasSize(0);
        assertThat(expectedDashboard.getTagList()).hasSize(0);
        assertThat(expectedDashboard.getTemplateCardList()).hasSize(0);
    }

    @Test
    void testCreateInvalidDashboard(){
        IllegalArgumentException illegalArgumentException = assertThrows(IllegalArgumentException.class, () -> this.dashboardService.createDashboard(null));

        assertThat(illegalArgumentException.getMessage()).isEqualTo(INVALID_DASHBOARD_CREATION_ERROR + null);
    }

    @Test
    void testGetSpecificDashboardById(){
        Dashboard expectedDashboard = this.dashboardService.createDashboard(this.dashboard);

        Dashboard resultDashboard = this.dashboardService.getSpecificDashboardById(expectedDashboard.getId());

        assertThat(resultDashboard).isEqualTo(expectedDashboard);
    }

    @Test
    void testNotFoundSpecificDashboardById(){
        Card resultCard = this.cardService.getSpecificCardById(12345L);

        assertThat(resultCard).isNull();
    }

    @Test
    void testDeleteDashboard(){
        Dashboard expectedDashboard = this.dashboardService.createDashboard(this.dashboard);

        this.dashboardService.removeDashboardById(expectedDashboard.getId());

        Dashboard resultDashboard = this.dashboardService.getSpecificDashboardById(expectedDashboard.getId());
        assertThat(resultDashboard).isNull();
    }

    @Test
    void testUpdateNameDashboard(){
        Dashboard exampleDashboard = this.dashboardService.createDashboard(this.dashboard);
        String newName = "New Title";

        this.dashboardService.updateName(exampleDashboard.getId(), newName);

        Dashboard resultDashboard = this.dashboardService.getSpecificDashboardById(exampleDashboard.getId());
        assertThat(resultDashboard.getName()).isEqualTo(newName);
        assertThat(resultDashboard).isEqualTo(exampleDashboard);
    }

    @Test
    void testUpdateNameDashboardWithInvalidId(){
        Long invalidId = 12345L;
        String newName = "New Title";

        NoSuchElementException exception = assertThrows(NoSuchElementException.class, () -> this.dashboardService.updateName(invalidId, newName));

        assertThat(exception.getMessage()).isEqualTo(DashboardService.NOT_FOUND_ID_ERROR + invalidId);
    }

    @Test
    void testUpdateNameDashboardWithNullId(){
        String newName = "New Title";

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> this.dashboardService.updateName(null, newName));

        assertThat(exception.getMessage()).isEqualTo(DashboardService.INVALID_ID_ERROR + null);
    }

    @Test
    @Transactional
    void testAddNewFileToDashboard(){
        Dashboard dashboard = this.dashboardService.createDashboard(this.dashboard);
        AttachedFile newAttachedFile = new AttachedFile("example.pdf", "/");

        this.dashboardService.attachFileToDashboard(dashboard.getId(), newAttachedFile);
        Dashboard resultCard = this.dashboardService.getSpecificDashboardById(dashboard.getId());

        assertThat(resultCard.getAttachedFiles()).hasSize(1);
        assertThat(resultCard.getAttachedFiles().getFirst()).isEqualTo(newAttachedFile);
    }

    @Test
    void testAddNewFileToCardWithInvalidId(){
        Long invalidId = 12345L;
        AttachedFile newAttachedFile = new AttachedFile("example.pdf", "/");

        NoSuchElementException exception = assertThrows(NoSuchElementException.class, () -> this.dashboardService.attachFileToDashboard(invalidId, newAttachedFile));

        assertThat(exception.getMessage()).isEqualTo(DashboardService.NOT_FOUND_ID_ERROR + invalidId);
    }

    @Test
    void testAddNewFileToCardWithNullId(){
        AttachedFile newAttachedFile = new AttachedFile("example.pdf", "/");

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> this.dashboardService.attachFileToDashboard(null, newAttachedFile));

        assertThat(exception.getMessage()).isEqualTo(DashboardService.INVALID_ID_ERROR + null);
    }

    @Test
    void testAddNewFileToCardWithNullValue(){
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> this.dashboardService.attachFileToDashboard(12345L, null));

        assertThat(exception.getMessage()).contains(INVALID_ELEMENT_ERROR);
    }

    @Test
    @Transactional
    void testRemoveFileFromCard(){
        Dashboard exampleDashboard = this.dashboardService.createDashboard(this.dashboard);
        AttachedFile newAttachedFile = new AttachedFile("example.pdf", "/");
        Long currentId = exampleDashboard.getId();

        this.dashboardService.attachFileToDashboard(currentId, newAttachedFile);
        Dashboard resultCard = this.dashboardService.getSpecificDashboardById(currentId);

        assertThat(resultCard.getAttachedFiles()).hasSize(1);

        this.dashboardService.detachFileFromDashboard(currentId, newAttachedFile);
        resultCard = this.dashboardService.getSpecificDashboardById(currentId);

        assertThat(resultCard.getAttachedFiles()).hasSize(0);
    }

    @Test
    void testRemoveFileFromCardWithInvalidId(){
        Long invalidId = 12345L;
        AttachedFile newAttachedFile = new AttachedFile("example.pdf", "/");

        NoSuchElementException exception = assertThrows(NoSuchElementException.class, () -> this.dashboardService.detachFileFromDashboard(invalidId, newAttachedFile));

        assertThat(exception.getMessage()).isEqualTo(DashboardService.NOT_FOUND_ID_ERROR + invalidId);
    }

    @Test
    void testRemoveFileFromCardWithNullId(){
        AttachedFile newAttachedFile = new AttachedFile("example.pdf", "/");

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> this.dashboardService.detachFileFromDashboard(null, newAttachedFile));

        assertThat(exception.getMessage()).isEqualTo(DashboardService.INVALID_ID_ERROR + null);
    }

    @Test
    void testRemoveFileFromCardWithNullValue(){
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> this.dashboardService.detachFileFromDashboard(12345L, null));

        assertThat(exception.getMessage()).contains(INVALID_ELEMENT_ERROR);
    }

    @Test
    @Transactional
    void testRemoveFileFromCardWithInvalidFile(){
        Dashboard exampleDashboard = this.dashboardService.createDashboard(this.dashboard);
        AttachedFile newAttachedFile = new AttachedFile("example.pdf", "/");

        NoSuchElementException exception = assertThrows(NoSuchElementException.class, () -> this.dashboardService.detachFileFromDashboard(exampleDashboard.getId(), newAttachedFile));

        assertThat(exception.getMessage()).contains(DashboardService.NOT_FOUND_ELEMENT_ERROR);
    }

    @Test
    @Transactional
    void testAddTagToDashboard(){
        Dashboard exampleDashboard = this.dashboardService.createDashboard(this.dashboard);
        Tag newTag = new Tag("Example Tag", Color.BLUE);
        Tag exampleTag = this.tagService.createTag(newTag);

        this.dashboardService.addTagToDashboard(exampleDashboard.getId(), exampleTag);

        Dashboard resultDashboard = this.dashboardService.getSpecificDashboardById(exampleDashboard.getId());

        assertThat(resultDashboard.getTagList()).hasSize(1);
        assertThat(resultDashboard.getTagList().getFirst().getColor()).isEqualTo(exampleTag.getColor());
    }

    @Test
    void testAddTagToDashboardWithInvalidId(){
        Long invalidId = 12345L;

        Tag newTag = new Tag("Example Tag", Color.BLUE);
        Tag exampleTag = this.tagService.createTag(newTag);

        NoSuchElementException exception = assertThrows(NoSuchElementException.class, () -> this.dashboardService.addTagToDashboard(invalidId, exampleTag));

        assertThat(exception.getMessage()).isEqualTo(DashboardService.NOT_FOUND_ID_ERROR + invalidId);
    }

    @Test
    void testAddTagToDashboardWithNullId(){
        Tag newTag = new Tag("Example Tag", Color.BLUE);
        Tag exampleTag = this.tagService.createTag(newTag);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> this.dashboardService.addTagToDashboard(null, exampleTag));

        assertThat(exception.getMessage()).isEqualTo(DashboardService.INVALID_ID_ERROR + null);
    }

    @Test
    void testAddTagToDashboardWithInvalidTag(){
        Dashboard exampleDashboard = this.dashboardService.createDashboard(this.dashboard);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> this.dashboardService.addTagToDashboard(exampleDashboard.getId(), null));

        assertThat(exception.getMessage()).contains(DashboardService.INVALID_ELEMENT_ERROR);
    }

    @Test
    @Transactional
    void testRemoveTagFromDashboard(){
        Dashboard exampleDashboard = this.dashboardService.createDashboard(this.dashboard);
        Tag newTag = new Tag("Example Tag", Color.BLUE);
        Tag exampleTag = this.tagService.createTag(newTag);

        this.dashboardService.addTagToDashboard(exampleDashboard.getId(), exampleTag);
        Dashboard resultDashboard = this.dashboardService.getSpecificDashboardById(exampleDashboard.getId());

        assertThat(resultDashboard.getTagList()).hasSize(1);

        this.dashboardService.removeTagFromDashboard(exampleDashboard.getId(), exampleTag);
        resultDashboard = this.dashboardService.getSpecificDashboardById(exampleDashboard.getId());

        assertThat(resultDashboard.getTagList()).isEmpty();
    }

    @Test
    void testRemoveTagFromDashboardWithInvalidId(){
        Long invalidId = 12345L;
        Tag newTag = new Tag("Example Tag", Color.BLUE);
        Tag exampleTag = this.tagService.createTag(newTag);

        NoSuchElementException exception = assertThrows(NoSuchElementException.class, () -> this.dashboardService.removeTagFromDashboard(invalidId, exampleTag));

        assertThat(exception.getMessage()).isEqualTo(DashboardService.NOT_FOUND_ID_ERROR + invalidId);
    }

    @Test
    void testRemoveTagFromDashboardWithNullId(){
        Tag newTag = new Tag("Example Tag", Color.BLUE);
        Tag exampleTag = this.tagService.createTag(newTag);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> this.dashboardService.removeTagFromDashboard(null, exampleTag));

        assertThat(exception.getMessage()).isEqualTo(DashboardService.INVALID_ID_ERROR + null);
    }

    @Test
    void testRemoveTagFromDashboardWithNullValue(){
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> this.dashboardService.removeTagFromDashboard(12345L, null));

        assertThat(exception.getMessage()).contains(DashboardService.INVALID_ELEMENT_ERROR);
    }

    @Test
    @Transactional
    void testRemoveTagFromDashboardWithInvalidTag(){
        Dashboard exampleDashboard = this.dashboardService.createDashboard(this.dashboard);
        Tag newTag = new Tag("Example Tag", Color.BLUE);
        Tag exampleTag = this.tagService.createTag(newTag);

        NoSuchElementException exception = assertThrows(NoSuchElementException.class, () -> this.dashboardService.removeTagFromDashboard(exampleDashboard.getId(), exampleTag));

        assertThat(exception.getMessage()).contains(DashboardService.NOT_FOUND_ELEMENT_ERROR);
    }

    @Test
    @Transactional
    void testAddTemplateCardToDashboard(){
        Dashboard exampleDashboard = this.dashboardService.createDashboard(this.dashboard);
        Card expectedCard = this.cardService.createCard(this.card);

        this.dashboardService.addTemplateCardToDashboard(exampleDashboard.getId(), expectedCard.getId());

        Dashboard result = this.dashboardService.getSpecificDashboardById(exampleDashboard.getId());
        assertThat(result.getTemplateCardList().size()).isEqualTo(1);
        assertThat(result.getTemplateCardList()).contains(expectedCard);
    }

    @Test
    void testAddTemplateCardToDashboardWithInvalidDashboardId(){
        Long invalidId = 12345L;
        Card expectedCard = this.cardService.createCard(this.card);

        NoSuchElementException exception = assertThrows(
                NoSuchElementException.class,
                () ->
                        this.dashboardService.addTemplateCardToDashboard(invalidId, expectedCard.getId())
        );

        assertThat(exception.getMessage()).contains(DashboardService.NOT_FOUND_ID_ERROR);
    }

    @Test
    void testAddTemplateCardToDashboardWithNullDashboardId(){
        Card expectedCard = this.cardService.createCard(this.card);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () ->
                        this.dashboardService.addTemplateCardToDashboard(null, expectedCard.getId())
        );

        assertThat(exception.getMessage()).contains(DashboardService.INVALID_ID_ERROR);
    }

    @Test
    void testAddTemplateCardToDashboardWithInvalidCardId(){
        Dashboard exampleDashboard = this.dashboardService.createDashboard(this.dashboard);
        Long invalidId = 12345L;

        NoSuchElementException exception = assertThrows(
                NoSuchElementException.class,
                () ->
                        this.dashboardService.addTemplateCardToDashboard(exampleDashboard.getId(), invalidId)
        );

        assertThat(exception.getMessage()).contains(DashboardService.NOT_FOUND_CARD_ERROR);
    }

    @Test
    void testAddTemplateCardToTableWithNullCardId(){
        Dashboard exampleDashboard = this.dashboardService.createDashboard(this.dashboard);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () ->
                        this.dashboardService.addTemplateCardToDashboard(exampleDashboard.getId(), null)
        );

        assertThat(exception.getMessage()).contains(DashboardService.INVALID_ELEMENT_ERROR);
    }

    @Test
    @Transactional
    void testRemoveTemplateCardFromDashboard(){
        Dashboard exampleDashboard = this.dashboardService.createDashboard(this.dashboard);
        Card expectedCard = this.cardService.createCard(this.card);

        this.dashboardService.addTemplateCardToDashboard(exampleDashboard.getId(), expectedCard.getId());
        Dashboard result = this.dashboardService.getSpecificDashboardById(exampleDashboard.getId());
        assertThat(result.getTemplateCardList()).hasSize(1);

        this.dashboardService.removeTemplateCardFromDashboard(exampleDashboard.getId(), expectedCard.getId());
        result = this.dashboardService.getSpecificDashboardById(exampleDashboard.getId());
        assertThat(result.getTemplateCardList()).hasSize(0);
    }

    @Test
    void testRemoveTemplateCardFromDashboardWithInvalidDashboardId(){
        Long invalidId = 12345L;
        Card expectedCard = this.cardService.createCard(this.card);

        NoSuchElementException exception = assertThrows(
                NoSuchElementException.class,
                () ->
                    this.dashboardService.removeTemplateCardFromDashboard(invalidId, expectedCard.getId())
        );

        assertThat(exception.getMessage()).contains(DashboardService.NOT_FOUND_ID_ERROR);
    }

    @Test
    void testRemoveTemplateCardFromDashboardWithNullDashboardId(){
        Card expectedCard = this.cardService.createCard(this.card);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () ->
                        this.dashboardService.removeTemplateCardFromDashboard(null, expectedCard.getId())
        );

        assertThat(exception.getMessage()).contains(DashboardService.INVALID_ID_ERROR);
    }

    @Test
    void testRemoveTemplateCardFromDashboardWithInvalidCardId(){
        Dashboard exampleDashboard = this.dashboardService.createDashboard(this.dashboard);
        Long invalidId = 12345L;

        NoSuchElementException exception = assertThrows(
                NoSuchElementException.class,
                () ->
                        this.dashboardService.removeTemplateCardFromDashboard(exampleDashboard.getId(), invalidId)
        );

        assertThat(exception.getMessage()).contains(DashboardService.NOT_FOUND_CARD_ERROR);
    }

    @Test
    void testRemoveTemplateCardFromDashboardWithNullCardId(){
        Dashboard exampleDashboard = this.dashboardService.createDashboard(this.dashboard);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () ->
                        this.dashboardService.removeTemplateCardFromDashboard(exampleDashboard.getId(), null)
        );

        assertThat(exception.getMessage()).contains(DashboardService.INVALID_ELEMENT_ERROR);
    }

    @Test
    @Transactional
    void testRemoveTemplateCardFromDashboardWithNoCards(){
        Dashboard exampleDashboard = this.dashboardService.createDashboard(this.dashboard);
        Card expectedCard = this.cardService.createCard(this.card);

        NoSuchElementException exception = assertThrows(
                NoSuchElementException.class,
                () ->
                        this.dashboardService.removeTemplateCardFromDashboard(exampleDashboard.getId(), expectedCard.getId())
        );

        assertThat(exception.getMessage()).contains(NOT_FOUND_CARD_IN_THE_DASHBOARD_ERROR);
    }

    @Test
    @Transactional
    void testAddTableToDashboard(){
        Dashboard exampleDashboard = this.dashboardService.createDashboard(this.dashboard);
        Table expectedTable = this.tableService.createTable(this.table);

        this.dashboardService.addTableToDashboard(exampleDashboard.getId(), expectedTable.getId());

        Dashboard result = this.dashboardService.getSpecificDashboardById(exampleDashboard.getId());
        assertThat(result.getTableList().size()).isEqualTo(1);
        assertThat(result.getTableList()).contains(expectedTable);
    }

    @Test
    void testAddTableToDashboardWithInvalidDashboardId(){
        Long invalidId = 12345L;
        Table expectedTable = this.tableService.createTable(this.table);

        NoSuchElementException exception = assertThrows(
                NoSuchElementException.class,
                () ->
                        this.dashboardService.addTableToDashboard(invalidId, expectedTable.getId())
        );

        assertThat(exception.getMessage()).contains(DashboardService.NOT_FOUND_ID_ERROR);
    }

    @Test
    void testAddTableToDashboardWithNullDashboardId(){
        Table expectedTable = this.tableService.createTable(this.table);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () ->
                        this.dashboardService.addTableToDashboard(null, expectedTable.getId())
        );

        assertThat(exception.getMessage()).contains(DashboardService.INVALID_ID_ERROR);
    }

    @Test
    void testAddTableToDashboardWithInvalidTableId(){
        Dashboard exampleDashboard = this.dashboardService.createDashboard(this.dashboard);
        Long invalidId = 12345L;

        NoSuchElementException exception = assertThrows(
                NoSuchElementException.class,
                () ->
                        this.dashboardService.addTableToDashboard(exampleDashboard.getId(), invalidId)
        );

        assertThat(exception.getMessage()).contains(DashboardService.NOT_FOUND_TABLE_ERROR);
    }

    @Test
    void testAddTableToTableWithNullCardId(){
        Dashboard exampleDashboard = this.dashboardService.createDashboard(this.dashboard);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () ->
                        this.dashboardService.addTableToDashboard(exampleDashboard.getId(), null)
        );

        assertThat(exception.getMessage()).contains(DashboardService.INVALID_ELEMENT_ERROR);
    }

    @Test
    @Transactional
    void testRemoveTableFromDashboard(){
        Dashboard exampleDashboard = this.dashboardService.createDashboard(this.dashboard);
        Table expectedTable = this.tableService.createTable(this.table);

        this.dashboardService.addTableToDashboard(exampleDashboard.getId(), expectedTable.getId());
        Dashboard result = this.dashboardService.getSpecificDashboardById(exampleDashboard.getId());
        assertThat(result.getTableList()).hasSize(1);

        this.dashboardService.removeTableFromDashboard(exampleDashboard.getId(), expectedTable.getId());
        result = this.dashboardService.getSpecificDashboardById(exampleDashboard.getId());
        assertThat(result.getTableList()).hasSize(0);
    }

    @Test
    void testRemoveTableFromDashboardWithInvalidDashboardId(){
        Long invalidId = 12345L;
        Table expectedTable = this.tableService.createTable(this.table);

        NoSuchElementException exception = assertThrows(
                NoSuchElementException.class,
                () ->
                        this.dashboardService.removeTableFromDashboard(invalidId, expectedTable.getId())
        );

        assertThat(exception.getMessage()).contains(DashboardService.NOT_FOUND_ID_ERROR);
    }

    @Test
    void testRemoveTableFromDashboardWithNullDashboardId(){
        Table expectedTable = this.tableService.createTable(this.table);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () ->
                        this.dashboardService.removeTableFromDashboard(null, expectedTable.getId())
        );

        assertThat(exception.getMessage()).contains(DashboardService.INVALID_ID_ERROR);
    }

    @Test
    void testRemoveTableFromDashboardWithInvalidTableId(){
        Dashboard exampleDashboard = this.dashboardService.createDashboard(this.dashboard);
        Long invalidId = 12345L;

        NoSuchElementException exception = assertThrows(
                NoSuchElementException.class,
                () ->
                        this.dashboardService.removeTableFromDashboard(exampleDashboard.getId(), invalidId)
        );

        assertThat(exception.getMessage()).contains(DashboardService.NOT_FOUND_TABLE_ERROR);
    }

    @Test
    void testRemoveTableFromDashboardWithNullTableId(){
        Dashboard exampleDashboard = this.dashboardService.createDashboard(this.dashboard);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () ->
                        this.dashboardService.removeTableFromDashboard(exampleDashboard.getId(), null)
        );

        assertThat(exception.getMessage()).contains(DashboardService.INVALID_ELEMENT_ERROR);
    }

    @Test
    @Transactional
    void testRemoveTableFromDashboardWithNoTables(){
        Dashboard exampleDashboard = this.dashboardService.createDashboard(this.dashboard);
        Table expectedTable = this.tableService.createTable(this.table);

        NoSuchElementException exception = assertThrows(
                NoSuchElementException.class,
                () ->
                        this.dashboardService.removeTableFromDashboard(exampleDashboard.getId(), expectedTable.getId())
        );

        assertThat(exception.getMessage()).contains(NOT_FOUND_TABLE_IN_THE_DASHBOARD_ERROR);
    }

}


