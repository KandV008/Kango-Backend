package dev.kandv.kango.integrations.services;

import dev.kandv.kango.KangoApplication;
import dev.kandv.kango.models.Card;
import dev.kandv.kango.models.Table;
import dev.kandv.kango.models.enums.CardListSort;
import dev.kandv.kango.services.CardService;
import dev.kandv.kango.services.TableService;
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

import java.util.List;
import java.util.NoSuchElementException;

import static dev.kandv.kango.services.ErrorMessagesServices.*;
import static dev.kandv.kango.services.TableService.INVALID_TABLE_CREATION_ERROR;
import static dev.kandv.kango.services.TableService.NOT_FOUND_CARD_IN_THE_TABLE_ERROR;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Testcontainers
@SpringBootTest(classes = KangoApplication.class)
@ExtendWith(SpringExtension.class)
class TableServiceTest {

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
    private TableService tableService;

    @Autowired
    private CardService cardService;

    Table table;
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
        this.table = new Table("EXAMPLE TABLE", 0);
        this.card = new Card("EXAMPLE CARD", 0);
    }

    @AfterEach
    void afterEach(){
        this.tableService.removeAllTables();
        this.cardService.removeAllCards();
    }

    @Test
    void testGetSpecificTableById(){
        Table expectedTable = this.tableService.createTable(this.table);

        Table resultTable = this.tableService.getSpecificTableById(expectedTable.getId());

        assertThat(resultTable).isEqualTo(expectedTable);
    }

    @Test
    void testNotFoundSpecificTableById(){
        Table resultTable = this.tableService.getSpecificTableById(12345L);

        assertThat(resultTable).isNull();
    }

    @Test
    void testCreateTable(){
        int position = 0;
        String tableName = "EXAMPLE TABLE";
        Table newTable = new Table(tableName, position);

        Table result = this.tableService.createTable(newTable);

        assertThat(result.getName()).isEqualTo(newTable.getName());
        assertThat(result.getPosition()).isEqualTo(newTable.getPosition());
        assertThat(result.getId()).isNotNull();
    }

    @Test
    void testCreateTableWithNullTable(){
        IllegalArgumentException illegalArgumentException = assertThrows(IllegalArgumentException.class, () -> this.tableService.createTable(null));

        assertThat(illegalArgumentException.getMessage()).contains(INVALID_TABLE_CREATION_ERROR);
    }

    @Test
    void testRemoveTableById(){
        Table expectedTable = this.tableService.createTable(this.table);

        Table resultTable = this.tableService.getSpecificTableById(expectedTable.getId());
        assertThat(resultTable).isNotNull();

        this.tableService.removeTableById(resultTable.getId());
        resultTable = this.tableService.getSpecificTableById(resultTable.getId());
        assertThat(resultTable).isNull();
    }

    @Test
    void testUpdateTableName(){
        String tableName = "New Name";
        Table expectedTable = this.tableService.createTable(this.table);

        this.tableService.updateNameTable(expectedTable.getId(), tableName);

        Table resultTable = this.tableService.getSpecificTableById(expectedTable.getId());
        assertThat(resultTable.getName()).isEqualTo(tableName);
    }

    @Test
    void testUpdateTableNameWithInvalidId(){
        String tableName = "New Name";

        NoSuchElementException noSuchElementException = assertThrows(NoSuchElementException.class, () ->  this.tableService.updateNameTable(12345L, tableName));

        assertThat(noSuchElementException.getMessage()).contains(NOT_FOUND_TABLE_WITH_ID_ERROR);
    }

    @Test
    void testUpdateTableNameWithNullId(){
        String tableName = "New Name";

        IllegalArgumentException noSuchElementException = assertThrows(IllegalArgumentException.class, () ->  this.tableService.updateNameTable(null, tableName));

        assertThat(noSuchElementException.getMessage()).contains(INVALID_ID_ERROR);
    }

    @Test
    void testUpdateTableNameWithNullName(){
        IllegalArgumentException noSuchElementException = assertThrows(IllegalArgumentException.class, () ->  this.tableService.updateNameTable(12345L, null));

        assertThat(noSuchElementException.getMessage()).contains(INVALID_ELEMENT_ERROR);
    }

    @Test
    @Transactional
    void testAddCardToTable(){
        Table expectedTable = this.tableService.createTable(this.table);
        long tableId = expectedTable.getId();
        Card expectedCard = this.cardService.createCard(this.card);
        long cardId = expectedCard.getId();


        this.tableService.addCardToTable(tableId, cardId);

        Table result = this.tableService.getSpecificTableById(expectedTable.getId());
        assertThat(result.getCardList()).hasSize(1);
        assertThat(result.getCardList()).contains(expectedCard);
    }

    @Test
    void testAddCardToTableWithInvalidTableId(){
        Long tableId = 12345L;
        Card expectedCard = this.cardService.createCard(this.card);
        long cardId = expectedCard.getId();

        NoSuchElementException exception = assertThrows(NoSuchElementException.class, () -> this.tableService.addCardToTable(tableId, cardId));

        assertThat(exception.getMessage()).contains(NOT_FOUND_TABLE_WITH_ID_ERROR);
    }

    @Test
    void testAddCardToTableWithNullTableId(){
        Card expectedCard = this.cardService.createCard(this.card);
        long cardId = expectedCard.getId();

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> this.tableService.addCardToTable(null, cardId));

        assertThat(exception.getMessage()).contains(INVALID_ID_ERROR);
    }

    @Test
    void testAddCardToTableWithInvalidCardId(){
        Table expectedTable = this.tableService.createTable(this.table);
        long tableId = expectedTable.getId();
        Long cardId = 12345L;

        NoSuchElementException exception = assertThrows(NoSuchElementException.class, () -> this.tableService.addCardToTable(tableId, cardId));

        assertThat(exception.getMessage()).contains(NOT_FOUND_CARD_WITH_ID_ERROR);
    }

    @Test
    void testAddCardToTableWithNullCardId(){
        Table expectedTable = this.tableService.createTable(this.table);
        long tableId = expectedTable.getId();

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> this.tableService.addCardToTable(tableId, null));

        assertThat(exception.getMessage()).contains(INVALID_ELEMENT_ERROR);
    }

    @Test
    @Transactional
    void testRemoveCardFromTable(){
        Table expectedTable = this.tableService.createTable(this.table);
        Card expectedCard = this.cardService.createCard(this.card);

        this.tableService.addCardToTable(expectedTable.getId(), expectedCard.getId());
        Table result = this.tableService.getSpecificTableById(expectedTable.getId());
        assertThat(result.getCardList()).hasSize(1);

        this.tableService.removeCardFromTable(expectedTable.getId(), expectedCard.getId());
        result = this.tableService.getSpecificTableById(expectedTable.getId());
        assertThat(result.getCardList()).isEmpty();
    }

    @Test
    void testRemoveCardFromTableWithInvalidTableId(){
        Long tableId = 12345L;
        Card expectedCard = this.cardService.createCard(this.card);
        long cardId = expectedCard.getId();

        NoSuchElementException exception = assertThrows(NoSuchElementException.class, () -> this.tableService.removeCardFromTable(tableId, cardId));

        assertThat(exception.getMessage()).contains(NOT_FOUND_TABLE_WITH_ID_ERROR);
    }

    @Test
    void testRemoveCardFromTableWithNullTableId(){
        Card expectedCard = this.cardService.createCard(this.card);
        long cardId = expectedCard.getId();

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> this.tableService.removeCardFromTable(null, cardId));

        assertThat(exception.getMessage()).contains(INVALID_ID_ERROR);
    }

    @Test
    void testRemoveCardFromTableWithInvalidCardId(){
        Table expectedTable = this.tableService.createTable(this.table);
        long tableId = expectedTable.getId();
        Long cardId = 12345L;

        NoSuchElementException exception = assertThrows(NoSuchElementException.class, () -> this.tableService.removeCardFromTable(tableId, cardId));

        assertThat(exception.getMessage()).contains(NOT_FOUND_CARD_WITH_ID_ERROR);
    }

    @Test
    void testRemoveCardFromTableWithNullCardId(){
        Table expectedTable = this.tableService.createTable(this.table);
        long tableId = expectedTable.getId();

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> this.tableService.removeCardFromTable(tableId, null));

        assertThat(exception.getMessage()).contains(INVALID_ELEMENT_ERROR);
    }

    @Test
    @Transactional
    void testRemoveCardFromTableWithNoCards(){
        Table expectedTable = this.tableService.createTable(this.table);
        long tableId = expectedTable.getId();
        Card expectedCard = this.cardService.createCard(this.card);
        long cardId = expectedCard.getId();

        NoSuchElementException exception = assertThrows(NoSuchElementException.class, () -> this.tableService.removeCardFromTable(tableId, cardId));

        assertThat(exception.getMessage()).contains(NOT_FOUND_CARD_IN_THE_TABLE_ERROR);
    }

    @Test
    @Transactional
    void testSortCardListFromTable(){
        Table expectedTable = this.tableService.createTable(this.table);
        Card newCard1 = new Card("Card 1");
        Card expectedCard1 = this.cardService.createCard(newCard1);
        Card newCard2 = new Card("Card 2");
        Card expectedCard2 = this.cardService.createCard(newCard2);

        this.tableService.addCardToTable(expectedTable.getId(), expectedCard1.getId());
        this.tableService.addCardToTable(expectedTable.getId(), expectedCard2.getId());

        Table resultTable = this.tableService.getSpecificTableById(expectedTable.getId());

        assertThat(resultTable.getCardList()).hasSize(2);
        List<Card> cardList = resultTable.getCardList();
        assertThat(cardList.get(0)).isEqualTo(expectedCard1);
        assertThat(cardList.get(1)).isEqualTo(expectedCard2);

        CardListSort typeSort = CardListSort.BY_ID_REVERSE;
        this.tableService.sortCardListFromTable(resultTable.getId(), typeSort);

        resultTable = this.tableService.getSpecificTableById(expectedTable.getId());

        assertThat(resultTable.getCardList()).hasSize(2);
        cardList = resultTable.getCardList();
        assertThat(cardList.get(0)).isEqualTo(expectedCard2);
        assertThat(cardList.get(1)).isEqualTo(expectedCard1);
    }

    @Test
    void testSortCardListFromTableWithInvalidTableId(){
        Long tableId = 12345L;
        CardListSort typeSort = CardListSort.BY_ID_REVERSE;

        NoSuchElementException exception = assertThrows(NoSuchElementException.class, () -> this.tableService.sortCardListFromTable(tableId, typeSort));

        assertThat(exception.getMessage()).contains(NOT_FOUND_TABLE_WITH_ID_ERROR);
    }

    @Test
    void testSortCardListFromTableWithNullTableId(){
        CardListSort typeSort = CardListSort.BY_ID_REVERSE;

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> this.tableService.sortCardListFromTable(null, typeSort));

        assertThat(exception.getMessage()).contains(INVALID_ID_ERROR);
    }

    @Test
    void testSortCardListFromTableWithNullCardListSort(){
        Long tableId = 12345L;

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> this.tableService.sortCardListFromTable(tableId, null));

        assertThat(exception.getMessage()).contains(INVALID_ELEMENT_ERROR);
    }

    @Test
    @Transactional
    void testUpdateCardPositionFromTable(){
        Table expectedTable = this.tableService.createTable(this.table);
        Card newCard1 = new Card("Card 1");
        Card expectedCard1 = this.cardService.createCard(newCard1);
        Card newCard2 = new Card("Card 2");
        Card expectedCard2 = this.cardService.createCard(newCard2);

        this.tableService.addCardToTable(expectedTable.getId(), expectedCard1.getId());
        this.tableService.addCardToTable(expectedTable.getId(), expectedCard2.getId());

        Table resultTable = this.tableService.getSpecificTableById(expectedTable.getId());
        assertThat(resultTable.getCardList()).hasSize(2);

        this.tableService.updateCardPositionFromTable(expectedTable.getId(), expectedCard1.getId(), 1);

        resultTable = this.tableService.getSpecificTableById(expectedTable.getId());
        List<Card> cardList = resultTable.getCardList();
        assertThat(cardList).hasSize(2);
        assertThat(cardList.get(0)).isEqualTo(expectedCard2);
        assertThat(cardList.get(1)).isEqualTo(expectedCard1);
    }

    @Test
    void testUpdateCardPositionFromTableWithInvalidTableId(){
        Long tableId = 12345L;
        Card expectedCard = this.cardService.createCard(this.card);
        long cardId = expectedCard.getId();

        NoSuchElementException exception = assertThrows(NoSuchElementException.class, () -> this.tableService.updateCardPositionFromTable(tableId, cardId, 0));

        assertThat(exception.getMessage()).contains(NOT_FOUND_TABLE_WITH_ID_ERROR);
    }

    @Test
    void testUpdateCardPositionFromTableWithNullTableId(){
        Card expectedCard = this.cardService.createCard(this.card);
        long cardId = expectedCard.getId();

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> this.tableService.updateCardPositionFromTable(null, cardId, 0));

        assertThat(exception.getMessage()).contains(INVALID_ID_ERROR);
    }

    @Test
    void testUpdateCardPositionFromTableWithInvalidCardId(){
        Table expectedTable = this.tableService.createTable(this.table);
        long tableId = expectedTable.getId();
        Long cardId = 12345L;

        NoSuchElementException exception = assertThrows(NoSuchElementException.class, () -> this.tableService.updateCardPositionFromTable(tableId, cardId, 0));

        assertThat(exception.getMessage()).contains(NOT_FOUND_CARD_WITH_ID_ERROR);
    }

    @Test
    void testUpdateCardPositionFromTableWithNullCardId(){
        Table expectedTable = this.tableService.createTable(this.table);
        long tableId = expectedTable.getId();

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> this.tableService.updateCardPositionFromTable(tableId, null, 0));

        assertThat(exception.getMessage()).contains(INVALID_ELEMENT_ERROR);
    }

    @Test
    @Transactional
    void testUpdateCardPositionFromTableWithNoCards(){
        Table expectedTable = this.tableService.createTable(this.table);
        long tableId = expectedTable.getId();
        Card expectedCard = this.cardService.createCard(this.card);
        long cardId = expectedCard.getId();

        NoSuchElementException exception = assertThrows(NoSuchElementException.class, () -> this.tableService.updateCardPositionFromTable(tableId, cardId, 0));

        assertThat(exception.getMessage()).contains(NOT_FOUND_CARD_IN_THE_TABLE_ERROR);
    }

    @Test
    @Transactional
    void testMoveCardFromTableToAnotherTable(){
        Table newTable1 = new Table("New Table 1");
        Table table1 = this.tableService.createTable(newTable1);
        Table newTable2 = new Table("New Table 2");
        Table table2 = this.tableService.createTable(newTable2);

        Card expectedCard = this.cardService.createCard(this.card);

        this.tableService.addCardToTable(table1.getId(), expectedCard.getId());
        Table resultTable1 = this.tableService.getSpecificTableById(table1.getId());
        Table resultTable2 = this.tableService.getSpecificTableById(table2.getId());
        assertThat(resultTable1.getCardList()).hasSize(1);
        assertThat(resultTable2.getCardList()).isEmpty();

        this.tableService.moveCardFromTableToAnotherTable(table1.getId(), expectedCard.getId(), table2.getId(), 0);

        resultTable1 = this.tableService.getSpecificTableById(table1.getId());
        resultTable2 = this.tableService.getSpecificTableById(table2.getId());
        assertThat(resultTable1.getCardList()).isEmpty();
        assertThat(resultTable2.getCardList()).hasSize(1);
    }

    @Test
    void testMoveCardFromTableToAnotherTableWithInvalidOriginTableId(){
        Long originTableId = 12345L;
        Card expectedCard = this.cardService.createCard(this.card);
        long cardId = expectedCard.getId();
        Table destinedTable = this.tableService.createTable(this.table);
        long destinedTableId = destinedTable.getId();

        NoSuchElementException exception = assertThrows(NoSuchElementException.class, () -> this.tableService.moveCardFromTableToAnotherTable(originTableId, cardId, destinedTableId, 0));

        assertThat(exception.getMessage()).contains(NOT_FOUND_TABLE_WITH_ID_ERROR);
    }

    @Test
    void testMoveCardFromTableToAnotherTableWithNullOriginTableId(){
        Card expectedCard = this.cardService.createCard(this.card);
        long cardId = expectedCard.getId();
        Table destinedTable = this.tableService.createTable(this.table);
        long destinedTableId = destinedTable.getId();

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> this.tableService.moveCardFromTableToAnotherTable(null, cardId, destinedTableId, 0));

        assertThat(exception.getMessage()).contains(INVALID_ID_ERROR);
    }

    @Test
    void testMoveCardFromTableToAnotherTableWithInvalidDestinyTableId(){
        Table originTable = this.tableService.createTable(this.table);
        long originTableId = originTable.getId();
        Card expectedCard = this.cardService.createCard(this.card);
        long cardId = expectedCard.getId();
        Long destinedTableId = 12345L;

        NoSuchElementException exception = assertThrows(NoSuchElementException.class, () -> this.tableService.moveCardFromTableToAnotherTable(originTableId, cardId, destinedTableId, 0));

        assertThat(exception.getMessage()).contains(NOT_FOUND_TABLE_WITH_ID_ERROR);
    }

    @Test
    void testMoveCardFromTableToAnotherTableWithNullDestinyTableId(){
        Table originTable = this.tableService.createTable(this.table);
        long originTableId = originTable.getId();
        Card expectedCard = this.cardService.createCard(this.card);
        long cardId = expectedCard.getId();

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> this.tableService.moveCardFromTableToAnotherTable(originTableId, cardId, null, 0));

        assertThat(exception.getMessage()).contains(INVALID_ELEMENT_ERROR);
    }

    @Test
    void testMoveCardFromTableToAnotherTableWithInvalidCardId(){
        Table originTable = this.tableService.createTable(this.table);
        long originTableId = originTable.getId();
        Long cardId = 12345L;
        Table destinedTable = this.tableService.createTable(this.table);
        long destinedTableId = destinedTable.getId();

        NoSuchElementException exception = assertThrows(NoSuchElementException.class, () -> this.tableService.moveCardFromTableToAnotherTable(originTableId, cardId, destinedTableId,0));

        assertThat(exception.getMessage()).contains(NOT_FOUND_CARD_WITH_ID_ERROR);
    }

    @Test
    void testMoveCardFromTableToAnotherTableWithNullCardId(){
        Table originTable = this.tableService.createTable(this.table);
        long originTableId = originTable.getId();
        Table destinedTable = this.tableService.createTable(this.table);
        long destinedTableId = destinedTable.getId();

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> this.tableService.moveCardFromTableToAnotherTable(originTableId, null, destinedTableId, 0));

        assertThat(exception.getMessage()).contains(INVALID_ELEMENT_ERROR);
    }

    @Test
    @Transactional
    void testMoveCardFromTableToAnotherTableWithNoCards(){
        Table newTable1 = new Table("New Table 1");
        Table originTable = this.tableService.createTable(newTable1);
        long originTableId = originTable.getId();
        Table newTable2 = new Table("New Table 2");
        Table destinyTable = this.tableService.createTable(newTable2);
        long destinedTableId = destinyTable.getId();

        Card expectedCard = this.cardService.createCard(this.card);
        long cardId = expectedCard.getId();

        NoSuchElementException exception = assertThrows(NoSuchElementException.class, () -> this.tableService.moveCardFromTableToAnotherTable(originTableId, cardId, destinedTableId, 0));

        assertThat(exception.getMessage()).contains(NOT_FOUND_CARD_IN_THE_TABLE_ERROR);
    }

    @Test
    @Transactional
    void testMoveCardListFromTableToAnotherTable(){
        Table newTable1 = new Table("New Table 1");
        Table table1 = this.tableService.createTable(newTable1);
        Table newTable2 = new Table("New Table 2");
        Table table2 = this.tableService.createTable(newTable2);

        Card newCard1 = new Card("New Card 1");
        Card expectedCard1 = this.cardService.createCard(newCard1);
        Card newCard2 = new Card("New Card 2");
        Card expectedCard2 = this.cardService.createCard(newCard2);

        this.tableService.addCardToTable(table1.getId(), expectedCard1.getId());
        this.tableService.addCardToTable(table1.getId(), expectedCard2.getId());

        Table resultTable1 = this.tableService.getSpecificTableById(table1.getId());
        Table resultTable2 = this.tableService.getSpecificTableById(table2.getId());
        assertThat(resultTable1.getCardList()).hasSize(2);
        assertThat(resultTable2.getCardList()).isEmpty();

        this.tableService.moveCardListFromTableToAnotherTable(table1.getId(), table2.getId());

        resultTable1 = this.tableService.getSpecificTableById(table1.getId());
        resultTable2 = this.tableService.getSpecificTableById(table2.getId());
        assertThat(resultTable1.getCardList()).isEmpty();
        assertThat(resultTable2.getCardList()).hasSize(2);
    }

    @Test
    @Transactional
    void testMoveCardListFromTableToAnotherTableWithCards(){
        Table newTable1 = new Table("New Table 1");
        Table table1 = this.tableService.createTable(newTable1);
        Table newTable2 = new Table("New Table 2");
        Table table2 = this.tableService.createTable(newTable2);

        Card newCard1 = new Card("New Card 1");
        Card expectedCard1 = this.cardService.createCard(newCard1);
        Card newCard2 = new Card("New Card 2");
        Card expectedCard2 = this.cardService.createCard(newCard2);

        this.tableService.addCardToTable(table1.getId(), expectedCard1.getId());
        this.tableService.addCardToTable(table2.getId(), expectedCard2.getId());

        Table resultTable1 = this.tableService.getSpecificTableById(table1.getId());
        Table resultTable2 = this.tableService.getSpecificTableById(table2.getId());
        assertThat(resultTable1.getCardList()).hasSize(1);
        assertThat(resultTable2.getCardList()).hasSize(1);

        this.tableService.moveCardListFromTableToAnotherTable(table1.getId(), table2.getId());

        resultTable1 = this.tableService.getSpecificTableById(table1.getId());
        resultTable2 = this.tableService.getSpecificTableById(table2.getId());
        assertThat(resultTable1.getCardList()).isEmpty();
        assertThat(resultTable2.getCardList()).hasSize(2);
    }

    @Test
    void testMoveCardListFromTableToAnotherTableWithInvalidOriginTableId(){
        Long originTableId = 12345L;
        Table destinedTable = this.tableService.createTable(this.table);
        long destinedTableId = destinedTable.getId();

        NoSuchElementException exception = assertThrows(NoSuchElementException.class, () -> this.tableService.moveCardListFromTableToAnotherTable(originTableId, destinedTableId));

        assertThat(exception.getMessage()).contains(NOT_FOUND_TABLE_WITH_ID_ERROR);
    }

    @Test
    void testMoveCardListFromTableToAnotherTableWithNullOriginTableId(){
        Table destinedTable = this.tableService.createTable(this.table);
        long destinedTableId = destinedTable.getId();

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> this.tableService.moveCardListFromTableToAnotherTable(null, destinedTableId));

        assertThat(exception.getMessage()).contains(INVALID_ID_ERROR);
    }

    @Test
    void testMoveCardListFromTableToAnotherTableWithInvalidDestinyTableId(){
        Table originTable = this.tableService.createTable(this.table);
        long originTableId = originTable.getId();
        Long destinedTableId = 12345L;

        NoSuchElementException exception = assertThrows(NoSuchElementException.class, () -> this.tableService.moveCardListFromTableToAnotherTable(originTableId, destinedTableId));

        assertThat(exception.getMessage()).contains(NOT_FOUND_TABLE_WITH_ID_ERROR);
    }

    @Test
    void testMoveCardListFromTableToAnotherTableWithNullDestinyTableId(){
        Table originTable = this.tableService.createTable(this.table);
        long originTableId = originTable.getId();

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> this.tableService.moveCardListFromTableToAnotherTable(originTableId, null));

        assertThat(exception.getMessage()).contains(INVALID_ELEMENT_ERROR);
    }

    @Test
    @Transactional
    void testMoveCardListFromTableToAnotherTableWithNoCards(){
        Table newTable1 = new Table("New Table 1");
        Table table1 = this.tableService.createTable(newTable1);
        Table newTable2 = new Table("New Table 2");
        Table table2 = this.tableService.createTable(newTable2);

        Table resultTable1 = this.tableService.getSpecificTableById(table1.getId());
        Table resultTable2 = this.tableService.getSpecificTableById(table2.getId());
        assertThat(resultTable1.getCardList()).isEmpty();
        assertThat(resultTable2.getCardList()).isEmpty();

        this.tableService.moveCardListFromTableToAnotherTable(table1.getId(), table2.getId());

        resultTable1 = this.tableService.getSpecificTableById(table1.getId());
        resultTable2 = this.tableService.getSpecificTableById(table2.getId());
        assertThat(resultTable1.getCardList()).isEmpty();
        assertThat(resultTable2.getCardList()).isEmpty();
    }

    @Test
    @Transactional
    void testCopyCardListFromTableToAnotherTable(){
        Table newTable1 = new Table("New Table 1");
        Table table1 = this.tableService.createTable(newTable1);
        Table newTable2 = new Table("New Table 2");
        Table table2 = this.tableService.createTable(newTable2);

        Card newCard1 = new Card("New Card 1");
        Card expectedCard1 = this.cardService.createCard(newCard1);
        Card newCard2 = new Card("New Card 2");
        Card expectedCard2 = this.cardService.createCard(newCard2);

        this.tableService.addCardToTable(table1.getId(), expectedCard1.getId());
        this.tableService.addCardToTable(table1.getId(), expectedCard2.getId());

        Table resultTable1 = this.tableService.getSpecificTableById(table1.getId());
        Table resultTable2 = this.tableService.getSpecificTableById(table2.getId());
        assertThat(resultTable1.getCardList()).hasSize(2);
        assertThat(resultTable2.getCardList()).isEmpty();

        this.tableService.copyCardListFromTableToAnotherTable(table1.getId(), table2.getId());

        resultTable1 = this.tableService.getSpecificTableById(table1.getId());
        resultTable2 = this.tableService.getSpecificTableById(table2.getId());
        assertThat(resultTable1.getCardList()).hasSize(2);
        assertThat(resultTable2.getCardList()).hasSize(2);
    }

    @Test
    @Transactional
    void testCopyCardListFromTableToAnotherTableWithCards(){
        Table newTable1 = new Table("New Table 1");
        Table table1 = this.tableService.createTable(newTable1);
        Table newTable2 = new Table("New Table 2");
        Table table2 = this.tableService.createTable(newTable2);

        Card newCard1 = new Card("New Card 1");
        Card expectedCard1 = this.cardService.createCard(newCard1);
        Card newCard2 = new Card("New Card 2");
        Card expectedCard2 = this.cardService.createCard(newCard2);

        this.tableService.addCardToTable(table1.getId(), expectedCard1.getId());
        this.tableService.addCardToTable(table2.getId(), expectedCard2.getId());

        Table resultTable1 = this.tableService.getSpecificTableById(table1.getId());
        Table resultTable2 = this.tableService.getSpecificTableById(table2.getId());
        assertThat(resultTable1.getCardList()).hasSize(1);
        assertThat(resultTable2.getCardList()).hasSize(1);

        this.tableService.copyCardListFromTableToAnotherTable(table1.getId(), table2.getId());

        resultTable1 = this.tableService.getSpecificTableById(table1.getId());
        resultTable2 = this.tableService.getSpecificTableById(table2.getId());
        assertThat(resultTable1.getCardList()).hasSize(1);
        assertThat(resultTable2.getCardList()).hasSize(2);
    }

    @Test
    void testCopyCardListFromTableToAnotherTableWithInvalidOriginTableId(){
        Long originTableId = 12345L;
        Table destinedTable = this.tableService.createTable(this.table);
        long destinedTableId = destinedTable.getId();

        NoSuchElementException exception = assertThrows(NoSuchElementException.class, () -> this.tableService.copyCardListFromTableToAnotherTable(originTableId, destinedTableId));

        assertThat(exception.getMessage()).contains(NOT_FOUND_TABLE_WITH_ID_ERROR);
    }

    @Test
    void testCopyCardListFromTableToAnotherTableWithNullOriginTableId(){
        Table destinedTable = this.tableService.createTable(this.table);
        long destinedTableId = destinedTable.getId();

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> this.tableService.copyCardListFromTableToAnotherTable(null, destinedTableId));

        assertThat(exception.getMessage()).contains(INVALID_ID_ERROR);
    }

    @Test
    void testCopyCardListFromTableToAnotherTableWithInvalidDestinyTableId(){
        Table originTable = this.tableService.createTable(this.table);
        long originTableId = originTable.getId();
        Long destinedTableId = 12345L;

        NoSuchElementException exception = assertThrows(NoSuchElementException.class, () -> this.tableService.copyCardListFromTableToAnotherTable(originTableId, destinedTableId));

        assertThat(exception.getMessage()).contains(NOT_FOUND_TABLE_WITH_ID_ERROR);
    }

    @Test
    void testCopyCardListFromTableToAnotherTableWithNullDestinyTableId(){
        Table originTable = this.tableService.createTable(this.table);
        long originTableId = originTable.getId();

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> this.tableService.copyCardListFromTableToAnotherTable(originTableId, null));

        assertThat(exception.getMessage()).contains(INVALID_ELEMENT_ERROR);
    }

    @Test
    @Transactional
    void testCopyCardListFromTableToAnotherTableWithNoCards(){
        Table newTable1 = new Table("New Table 1");
        Table table1 = this.tableService.createTable(newTable1);
        Table newTable2 = new Table("New Table 2");
        Table table2 = this.tableService.createTable(newTable2);

        Table resultTable1 = this.tableService.getSpecificTableById(table1.getId());
        Table resultTable2 = this.tableService.getSpecificTableById(table2.getId());
        assertThat(resultTable1.getCardList()).isEmpty();
        assertThat(resultTable2.getCardList()).isEmpty();

        this.tableService.copyCardListFromTableToAnotherTable(table1.getId(), table2.getId());

        resultTable1 = this.tableService.getSpecificTableById(table1.getId());
        resultTable2 = this.tableService.getSpecificTableById(table2.getId());
        assertThat(resultTable1.getCardList()).isEmpty();
        assertThat(resultTable2.getCardList()).isEmpty();
    }
}
