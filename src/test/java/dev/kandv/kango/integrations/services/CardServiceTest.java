package dev.kandv.kango.integrations.services;

import dev.kandv.kango.KangoApplication;
import dev.kandv.kango.models.Card;
import dev.kandv.kango.models.enums.CardType;
import dev.kandv.kango.models.enums.Color;
import dev.kandv.kango.models.utils.AttachedFile;
import dev.kandv.kango.models.utils.Check;
import dev.kandv.kango.services.CardService;
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

import java.util.Date;
import java.util.List;
import java.util.NoSuchElementException;

import static dev.kandv.kango.models.Card.NOT_FOUND_CHECK_ERROR;
import static dev.kandv.kango.services.CardService.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Testcontainers
@SpringBootTest(classes = KangoApplication.class)
@ExtendWith(SpringExtension.class)
public class CardServiceTest {

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
    private CardService cardService;

    Card card;

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
        this.card = new Card("EXAMPLE CARD");
    }

    @AfterEach
    void afterEach(){
        this.cardService.removeAllCards();
    }

    @Test
    void testGetSpecificCardById(){
        Card expectedCard = this.cardService.createCard(this.card);

        Card resultCard = this.cardService.getSpecificCardById(expectedCard.getId());

        assertThat(resultCard).isEqualTo(expectedCard);
    }

    @Test
    void testNotFoundSpecificCardById(){
        Card resultCard = this.cardService.getSpecificCardById(12345L);

        assertThat(resultCard).isNull();
    }

    @Test
    void testCreateCard(){
        Card expectedCard = this.cardService.createCard(this.card);

        assertThat(expectedCard.getTitle()).isEqualTo(this.card.getTitle());
        assertThat(expectedCard.getDescription()).isEqualTo(this.card.getDescription());
        assertThat(expectedCard.getColor()).isEqualTo(this.card.getColor());
        assertThat(expectedCard.getPosition()).isEqualTo(this.card.getPosition());
        assertThat(expectedCard.getDeadLine()).isEqualTo(this.card.getDeadLine());
        assertThat(expectedCard.getAttachedFiles()).isEqualTo(this.card.getAttachedFiles());
        assertThat(expectedCard.getChecks()).isEqualTo(this.card.getChecks());
        assertThat(expectedCard.getTagList()).isEqualTo(this.card.getTagList());
    }

    @Test
    void testCreateInvalidCard(){
        IllegalArgumentException illegalArgumentException = assertThrows(IllegalArgumentException.class, () -> this.cardService.createCard(null));

        assertThat(illegalArgumentException.getMessage()).isEqualTo(INVALID_CARD_CREATION_ERROR + null);
    }

    @Test
    void testDeleteCard(){
        Card expectedCard = this.cardService.createCard(this.card);

        this.cardService.removeCardById(expectedCard.getId());

        Card resultCard = this.cardService.getSpecificCardById(expectedCard.getId());
        assertThat(resultCard).isNull();
    }

    @Test
    void testGetAllLocalTemplateCards(){
        Card localTemplateCard1 = new Card("TEMPLATE 1", CardType.LOCAL_TEMPLATE);
        Card localTemplateCard2 = new Card("TEMPLATE 2", CardType.LOCAL_TEMPLATE);
        Card localTemplateCard3 = new Card("TEMPLATE 3", CardType.LOCAL_TEMPLATE);

        this.cardService.createCard(localTemplateCard1);
        this.cardService.createCard(localTemplateCard2);
        this.cardService.createCard(localTemplateCard3);

        List<Card> localTemplateCardList = this.cardService.getAllLocalTemplateCards();

        assertThat(localTemplateCardList).hasSize(3);
        assertThat(localTemplateCardList.get(0)).isEqualTo(localTemplateCard1);
        assertThat(localTemplateCardList.get(1)).isEqualTo(localTemplateCard2);
        assertThat(localTemplateCardList.get(2)).isEqualTo(localTemplateCard3);
    }

    @Test
    void testGetAllGlobalTemplateCards(){
        Card globalTemplateCard1 = new Card("TEMPLATE 1", CardType.GLOBAL_TEMPLATE);
        Card globalTemplateCard2 = new Card("TEMPLATE 2", CardType.GLOBAL_TEMPLATE);
        Card globalTemplateCard3 = new Card("TEMPLATE 3", CardType.GLOBAL_TEMPLATE);

        this.cardService.createCard(globalTemplateCard1);
        this.cardService.createCard(globalTemplateCard2);
        this.cardService.createCard(globalTemplateCard3);

        List<Card> localTemplateCardList = this.cardService.getAllGlobalTemplateCards();

        assertThat(localTemplateCardList).hasSize(3);
        assertThat(localTemplateCardList.get(0)).isEqualTo(globalTemplateCard1);
        assertThat(localTemplateCardList.get(1)).isEqualTo(globalTemplateCard2);
        assertThat(localTemplateCardList.get(2)).isEqualTo(globalTemplateCard3);
    }

    @Test
    void testCreateTemplateCard(){
        Card localTemplateCard1 = new Card("TEMPLATE 1", CardType.LOCAL_TEMPLATE);
        Card globalTemplateCard2 = new Card("TEMPLATE 2", CardType.GLOBAL_TEMPLATE);

        Card result1 = this.cardService.createCard(localTemplateCard1);
        Card result2 = this.cardService.createCard(globalTemplateCard2);

        List<Card> allGlobalTemplateCards = this.cardService.getAllGlobalTemplateCards();
        assertThat(allGlobalTemplateCards).hasSize(1);
        assertThat(allGlobalTemplateCards.getFirst()).isEqualTo(result2);

        List<Card> allLocalTemplateCards = this.cardService.getAllLocalTemplateCards();
        assertThat(allLocalTemplateCards).hasSize(1);
        assertThat(allLocalTemplateCards.getFirst()).isEqualTo(result1);
    }
    
    @Test
    void testDeleteTemplateCard(){
        Card localTemplateCard1 = new Card("TEMPLATE 1", CardType.LOCAL_TEMPLATE);
        Card globalTemplateCard2 = new Card("TEMPLATE 2", CardType.GLOBAL_TEMPLATE);

        Card result1 = this.cardService.createCard(localTemplateCard1);
        Card result2 = this.cardService.createCard(globalTemplateCard2);

        this.cardService.removeCardById(result1.getId());
        this.cardService.removeCardById(result2.getId());

        List<Card> allLocalTemplateCards = this.cardService.getAllLocalTemplateCards();
        assertThat(allLocalTemplateCards).isEmpty();

        List<Card> allGlobalTemplateCards = this.cardService.getAllGlobalTemplateCards();
        assertThat(allGlobalTemplateCards).isEmpty();
    }

    @Test
    void testUpdateTitleCard(){
        Card exampleCard = this.cardService.createCard(this.card);
        String newTitle = "New Title";

        this.cardService.updateTitleCard(exampleCard.getId(), newTitle);

        Card resultCard = this.cardService.getSpecificCardById(exampleCard.getId());
        assertThat(resultCard.getTitle()).isEqualTo(newTitle);
        assertThat(resultCard).isEqualTo(exampleCard);
    }

    @Test
    void testUpdateTitleCardWithInvalidId(){
        Long invalidId = 12345L;
        String newTitle = "New Title";

        NoSuchElementException exception = assertThrows(NoSuchElementException.class, () -> {
            this.cardService.updateTitleCard(invalidId, newTitle);
        });

        assertThat(exception.getMessage()).isEqualTo(NOT_FOUND_ID_ERROR + invalidId);
    }

    @Test
    void testUpdateTitleCardWithNullId(){
        String newTitle = "New Title";

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            this.cardService.updateTitleCard(null, newTitle);
        });

        assertThat(exception.getMessage()).isEqualTo(INVALID_ID_ERROR + null);
    }

    @Test
    void testUpdateDescriptionCard(){
        Card exampleCard = this.cardService.createCard(this.card);
        String newDescription = "New Description";

        this.cardService.updateDescriptionCard(exampleCard.getId(), newDescription);

        Card resultCard = this.cardService.getSpecificCardById(exampleCard.getId());
        assertThat(resultCard.getDescription()).isEqualTo(newDescription);
        assertThat(resultCard).isEqualTo(exampleCard);
    }

    @Test
    void testUpdateDescriptionCardWithInvalidId(){
        Long invalidId = 12345L;
        String newDescription = "New Description";

        NoSuchElementException exception = assertThrows(NoSuchElementException.class, () -> {
            this.cardService.updateDescriptionCard(invalidId, newDescription);
        });

        assertThat(exception.getMessage()).isEqualTo(NOT_FOUND_ID_ERROR + invalidId);
    }

    @Test
    void testUpdateDescriptionCardWithNullId(){
        String newDescription = "New Description";

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            this.cardService.updateDescriptionCard(null, newDescription);
        });

        assertThat(exception.getMessage()).isEqualTo(INVALID_ID_ERROR + null);
    }

    @Test
    void testUpdateColorCard(){
        Card exampleCard = this.cardService.createCard(this.card);
        Color newColor = Color.PURPLE;

        this.cardService.updateColorCard(exampleCard.getId(), newColor);

        Card resultCard = this.cardService.getSpecificCardById(exampleCard.getId());
        assertThat(resultCard.getColor()).isEqualTo(newColor);
        assertThat(resultCard).isEqualTo(exampleCard);
    }

    @Test
    void testUpdateColorCardWithInvalidId(){
        Long invalidId = 12345L;
        Color newColor = Color.PURPLE;

        NoSuchElementException exception = assertThrows(NoSuchElementException.class, () -> {
            this.cardService.updateColorCard(invalidId, newColor);
        });

        assertThat(exception.getMessage()).isEqualTo(NOT_FOUND_ID_ERROR + invalidId);
    }

    @Test
    void testUpdateColorCardWithNullId(){
        Color newColor = Color.PURPLE;

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            this.cardService.updateColorCard(null, newColor);
        });

        assertThat(exception.getMessage()).isEqualTo(INVALID_ID_ERROR + null);
    }

    @Test
    void testUpdateDeadLineCard(){
        Card exampleCard = this.cardService.createCard(this.card);
        Date newDeadLine = new Date();

        this.cardService.updateDeadLineCard(exampleCard.getId(), newDeadLine);

        Card resultCard = this.cardService.getSpecificCardById(exampleCard.getId());
        assertThat(resultCard.getDeadLine().getTime()).isEqualTo(newDeadLine.getTime());
        assertThat(resultCard).isEqualTo(exampleCard);
    }

    @Test
    void testUpdateDeadLineCardWithInvalidId(){
        Long invalidId = 12345L;
        Date newDeadLine = new Date();

        NoSuchElementException exception = assertThrows(NoSuchElementException.class, () -> {
            this.cardService.updateDeadLineCard(invalidId, newDeadLine);
        });

        assertThat(exception.getMessage()).isEqualTo(NOT_FOUND_ID_ERROR + invalidId);
    }

    @Test
    void testUpdateDeadLineCardWithNullId(){
        Date newDeadLine = new Date();

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            this.cardService.updateDeadLineCard(null, newDeadLine);
        });

        assertThat(exception.getMessage()).isEqualTo(INVALID_ID_ERROR + null);
    }

    @Test
    @Transactional
    void testAddNewFileToCard(){
        Card exampleCard = this.cardService.createCard(this.card);
        AttachedFile newAttachedFile = new AttachedFile("example.pdf", "/");

        this.cardService.attachFileToCard(exampleCard.getId(), newAttachedFile);
        Card resultCard = this.cardService.getSpecificCardById(exampleCard.getId());

        assertThat(resultCard.getAttachedFiles()).hasSize(1);
        assertThat(resultCard.getAttachedFiles().getFirst()).isEqualTo(newAttachedFile);
    }

    @Test
    void testAddNewFileToCardWithInvalidId(){
        Long invalidId = 12345L;
        AttachedFile newAttachedFile = new AttachedFile("example.pdf", "/");

        NoSuchElementException exception = assertThrows(NoSuchElementException.class, () -> {
            this.cardService.attachFileToCard(invalidId, newAttachedFile);
        });

        assertThat(exception.getMessage()).isEqualTo(NOT_FOUND_ID_ERROR + invalidId);
    }

    @Test
    void testAddNewFileToCardWithNullId(){
        AttachedFile newAttachedFile = new AttachedFile("example.pdf", "/");

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            this.cardService.attachFileToCard(null, newAttachedFile);
        });

        assertThat(exception.getMessage()).isEqualTo(INVALID_ID_ERROR + null);
    }

    @Test
    void testAddNewFileToCardWithNullValue(){
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            this.cardService.attachFileToCard(1234L, null);
        });

        assertThat(exception.getMessage()).contains(INVALID_ELEMENT_ERROR);
    }

    @Test
    @Transactional
    void testRemoveFileFromCard(){
        Card exampleCard = this.cardService.createCard(this.card);
        AttachedFile newAttachedFile = new AttachedFile("example.pdf", "/");
        Long currentId = exampleCard.getId();

        this.cardService.attachFileToCard(currentId, newAttachedFile);
        Card resultCard = this.cardService.getSpecificCardById(currentId);

        assertThat(resultCard.getAttachedFiles()).hasSize(1);

        this.cardService.detachFileToCard(currentId, newAttachedFile);
        resultCard = this.cardService.getSpecificCardById(currentId);

        assertThat(resultCard.getAttachedFiles()).hasSize(0);
    }

    @Test
    void testRemoveFileFromCardWithInvalidId(){
        Long invalidId = 12345L;
        AttachedFile newAttachedFile = new AttachedFile("example.pdf", "/");

        NoSuchElementException exception = assertThrows(NoSuchElementException.class, () -> {
            this.cardService.detachFileToCard(invalidId, newAttachedFile);
        });

        assertThat(exception.getMessage()).isEqualTo(NOT_FOUND_ID_ERROR + invalidId);
    }

    @Test
    void testRemoveFileFromCardWithNullId(){
        AttachedFile newAttachedFile = new AttachedFile("example.pdf", "/");

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            this.cardService.detachFileToCard(null, newAttachedFile);
        });

        assertThat(exception.getMessage()).isEqualTo(INVALID_ID_ERROR + null);
    }

    @Test
    void testRemoveFileFromCardWithNullValue(){
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            this.cardService.detachFileToCard(1234L, null);
        });

        assertThat(exception.getMessage()).contains(INVALID_ELEMENT_ERROR);
    }

    @Test
    @Transactional
    void testRemoveFileFromCardWithInvalidFile(){
        Card exampleCard = this.cardService.createCard(this.card);
        AttachedFile newAttachedFile = new AttachedFile("example.pdf", "/");

        NoSuchElementException exception = assertThrows(NoSuchElementException.class, () -> {
            this.cardService.detachFileToCard(exampleCard.getId(), newAttachedFile);
        });

        assertThat(exception.getMessage()).contains(NOT_FOUND_ELEMENT_ERROR);
    }

    @Test
    @Transactional
    void testAddNewCheckToCard(){
        Card exampleCard = this.cardService.createCard(this.card);
        Check newCheck = new Check("EXAMPLE CHECK", false);

        this.cardService.addCheckToCard(exampleCard.getId(), newCheck);
        Card resultCard = this.cardService.getSpecificCardById(exampleCard.getId());

        assertThat(resultCard.getChecks()).hasSize(1);
        assertThat(resultCard.getChecks().getFirst()).isEqualTo(newCheck);
    }

    @Test
    void testAddNewCheckToCardWithInvalidId(){
        Long invalidId = 12345L;
        Check newCheck = new Check("EXAMPLE CHECK", false);

        NoSuchElementException exception = assertThrows(NoSuchElementException.class, () -> {
            this.cardService.addCheckToCard(invalidId, newCheck);
        });

        assertThat(exception.getMessage()).isEqualTo(NOT_FOUND_ID_ERROR + invalidId);
    }

    @Test
    void testAddNewCheckToCardWithNullId(){
        Check newCheck = new Check("EXAMPLE CHECK", false);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            this.cardService.addCheckToCard(null, newCheck);
        });

        assertThat(exception.getMessage()).isEqualTo(INVALID_ID_ERROR + null);
    }

    @Test
    void testAddNewCheckToCardWithNullValue(){
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            this.cardService.addCheckToCard(1234L, null);
        });

        assertThat(exception.getMessage()).contains(INVALID_ELEMENT_ERROR);
    }

    @Test
    @Transactional
    void testRemoveCheckFromCard(){
        Card exampleCard = this.cardService.createCard(this.card);
        Check newCheck = new Check("EXAMPLE CHECK", false);
        Long currentId = exampleCard.getId();

        this.cardService.addCheckToCard(currentId, newCheck);
        Card resultCard = this.cardService.getSpecificCardById(currentId);

        assertThat(resultCard.getChecks()).hasSize(1);

        this.cardService.removeCheckFromCard(currentId, newCheck);
        resultCard = this.cardService.getSpecificCardById(currentId);

        assertThat(resultCard.getChecks()).hasSize(0);
    }

    @Test
    void testRemoveCheckFromCardWithInvalidId(){
        Long invalidId = 12345L;
        Check newCheck = new Check("EXAMPLE CHECK", false);

        NoSuchElementException exception = assertThrows(NoSuchElementException.class, () -> {
            this.cardService.removeCheckFromCard(invalidId, newCheck);
        });

        assertThat(exception.getMessage()).isEqualTo(NOT_FOUND_ID_ERROR + invalidId);
    }

    @Test
    void testRemoveCheckFromCardWithNullId(){
        Check newCheck = new Check("EXAMPLE CHECK", false);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            this.cardService.removeCheckFromCard(null, newCheck);
        });

        assertThat(exception.getMessage()).isEqualTo(INVALID_ID_ERROR + null);
    }

    @Test
    void testRemoveCheckFromCardWithNullValue(){
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            this.cardService.removeCheckFromCard(1234L, null);
        });

        assertThat(exception.getMessage()).contains(INVALID_ELEMENT_ERROR);
    }

    @Test
    @Transactional
    void testRemoveCheckFromCardWithInvalidCheck(){
        Card exampleCard = this.cardService.createCard(this.card);
        Check newCheck = new Check("EXAMPLE CHECK", false);

        NoSuchElementException exception = assertThrows(NoSuchElementException.class, () -> {
            this.cardService.removeCheckFromCard(exampleCard.getId(), newCheck);
        });

        assertThat(exception.getMessage()).contains(NOT_FOUND_ELEMENT_ERROR);
    }

    @Test
    @Transactional
    void testUpdateCheckFromCard(){
        Card exampleCard = this.cardService.createCard(this.card);
        Check newCheck = new Check("EXAMPLE CHECK", false);

        this.cardService.addCheckToCard(exampleCard.getId(), newCheck);

        newCheck.setChecked(true);
        this.cardService.updateCheckFromCard(exampleCard.getId(), newCheck);
        Card resultCard = this.cardService.getSpecificCardById(exampleCard.getId());

        assertThat(resultCard.getChecks()).hasSize(1);
        assertThat(resultCard.getChecks().getFirst()).isEqualTo(newCheck);
    }

    @Test
    void testUpdateCheckFromCardWithInvalidId(){
        Long invalidId = 12345L;
        Check newCheck = new Check("EXAMPLE CHECK", false);

        NoSuchElementException exception = assertThrows(NoSuchElementException.class, () -> {
            this.cardService.updateCheckFromCard(invalidId, newCheck);
        });

        assertThat(exception.getMessage()).isEqualTo(NOT_FOUND_ID_ERROR + invalidId);
    }

    @Test
    void testUpdateCheckFromCardWithNullId(){
        Check newCheck = new Check("EXAMPLE CHECK", false);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            this.cardService.updateCheckFromCard(null, newCheck);
        });

        assertThat(exception.getMessage()).isEqualTo(INVALID_ID_ERROR + null);
    }

    @Test
    void testUpdateCheckFromCardWithNullValue(){
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            this.cardService.updateCheckFromCard(1234L, null);
        });

        assertThat(exception.getMessage()).contains(INVALID_ELEMENT_ERROR);
    }

    @Test
    @Transactional
    void testUpdateCheckFromCardWithInvalidCheck(){
        Card exampleCard = this.cardService.createCard(this.card);
        Check newCheck = new Check("EXAMPLE CHECK", false);

        NoSuchElementException exception = assertThrows(NoSuchElementException.class, () -> {
            this.cardService.updateCheckFromCard(exampleCard.getId(), newCheck);
        });

        assertThat(exception.getMessage()).contains(NOT_FOUND_CHECK_ERROR);
    }

    // TODO Add tests related with Entity Tag
}
