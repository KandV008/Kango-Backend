package dev.kandv.kango.integrations.services;

import dev.kandv.kango.KangoApplication;
import dev.kandv.kango.models.Card;
import dev.kandv.kango.models.Dashboard;
import dev.kandv.kango.models.Table;
import dev.kandv.kango.models.Tag;
import dev.kandv.kango.models.enums.CardType;
import dev.kandv.kango.models.enums.Color;
import dev.kandv.kango.models.enums.Visibility;
import dev.kandv.kango.services.CardService;
import dev.kandv.kango.services.DashboardService;
import dev.kandv.kango.services.TableService;
import dev.kandv.kango.services.TagService;
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

import static org.assertj.core.api.Assertions.assertThat;

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
        this.tableService.removeAllTables();
        this.cardService.removeAllCards();
        this.tagService.removeAllTags();
    }

    @Test
    void testCreateDashboard(){
        Dashboard expectedDashboard = this.dashboardService.createDashboard(this.dashboard);

        assertThat(expectedDashboard.getName()).isEqualTo(this.dashboard.getName());
        assertThat(expectedDashboard.getAttachedAttachedFiles()).hasSize(0);
        assertThat(expectedDashboard.getTableList()).hasSize(0);
        assertThat(expectedDashboard.getTagList()).hasSize(0);
        assertThat(expectedDashboard.getTemplateCardList()).hasSize(0);

    }
}


