package dev.kandv.kango.integrations.services;

import dev.kandv.kango.KangoApplication;
import dev.kandv.kango.models.Tag;
import dev.kandv.kango.models.enums.Color;
import dev.kandv.kango.models.enums.Visibility;
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

import java.util.List;

import static dev.kandv.kango.services.TagService.INVALID_TAG_CREATION_ERROR;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Testcontainers
@SpringBootTest(classes = KangoApplication.class)
@ExtendWith(SpringExtension.class)
public class TagServiceTest {

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
    private TagService tagService;

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
        this.tag = new Tag("Example Label", Color.PURPLE, Visibility.GLOBAL);
    }

    @AfterEach
    void afterEach(){
        this.tagService.removeAllTags();
    }

    @Test
    void testGetSpecificTagById(){
        Tag expectedTag = this.tagService.createTag(this.tag);

        Tag resultTag = this.tagService.getSpecificTagById(expectedTag.getId());

        assertThat(resultTag).isEqualTo(expectedTag);
    }

    @Test
    void testNotFoundSpecificTagById(){
        Tag resultTag = this.tagService.getSpecificTagById(12345L);

        assertThat(resultTag).isNull();
    }

    @Test
    void testCreateCard(){
        Tag expectedTag = this.tagService.createTag(this.tag);

        assertThat(expectedTag.getColor()).isEqualTo(this.tag.getColor());
        assertThat(expectedTag.getLabel()).isEqualTo(this.tag.getLabel());
        assertThat(expectedTag.getVisibility()).isEqualTo(this.tag.getVisibility());
    }

    @Test
    void testCreateInvalidCard(){
        IllegalArgumentException illegalArgumentException = assertThrows(IllegalArgumentException.class, () -> this.tagService.createTag(null));

        assertThat(illegalArgumentException.getMessage()).isEqualTo(INVALID_TAG_CREATION_ERROR + null);
    }

    @Test
    void testDeleteCard(){
        Tag expectedTag = this.tagService.createTag(this.tag);

        this.tagService.removeTagById(expectedTag.getId());

        Tag resultTag = this.tagService.getSpecificTagById(expectedTag.getId());
        assertThat(resultTag).isNull();
    }

    @Test
    void testGetAllGlobalTemplateCards(){
        Tag globalTag1 = new Tag("Example Label 1", Color.PURPLE, Visibility.GLOBAL);
        Tag globalTag2 = new Tag("Example Label 2", Color.PURPLE, Visibility.GLOBAL);
        Tag globalTag3 = new Tag("Example Label 3", Color.PURPLE, Visibility.GLOBAL);

        this.tagService.createTag(globalTag1);
        this.tagService.createTag(globalTag2);
        this.tagService.createTag(globalTag3);

        List<Tag> localTemplateCardList = this.tagService.getAllGlobalTags();

        assertThat(localTemplateCardList).hasSize(3);
        assertThat(localTemplateCardList.get(0)).isEqualTo(globalTag1);
        assertThat(localTemplateCardList.get(1)).isEqualTo(globalTag2);
        assertThat(localTemplateCardList.get(2)).isEqualTo(globalTag3);
    }

    @Test
    void testUpdateTag(){
        Tag expectedTag = this.tagService.createTag(this.tag);

        String newLabel = "New Label";
        expectedTag.setLabel(newLabel);
        Color newColor = Color.BLUE;
        expectedTag.setColor(newColor);

        this.tagService.updateTag(expectedTag.getId(), expectedTag);

        Tag resultTag = this.tagService.getSpecificTagById(expectedTag.getId());

        assertThat(resultTag.getLabel()).isEqualTo(newLabel);
        assertThat(resultTag.getColor()).isEqualTo(newColor);
    }

    @Test
    void testUpdateTagWithEmptyLabel(){
        Tag expectedTag = this.tagService.createTag(this.tag);

        String newLabel = "";
        expectedTag.setLabel(newLabel);
        Color newColor = Color.BLUE;
        expectedTag.setColor(newColor);

        this.tagService.updateTag(expectedTag.getId(), expectedTag);

        Tag resultTag = this.tagService.getSpecificTagById(expectedTag.getId());

        assertThat(resultTag.getLabel()).isEqualTo(newLabel);
        assertThat(resultTag.getColor()).isEqualTo(newColor);
    }

}
