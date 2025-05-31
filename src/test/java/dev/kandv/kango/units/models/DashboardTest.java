package dev.kandv.kango.units.models;

import dev.kandv.kango.models.Card;
import dev.kandv.kango.models.Dashboard;
import dev.kandv.kango.models.Table;
import dev.kandv.kango.models.utils.AttachedFile;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class DashboardTest {
    private Dashboard dashboard;
    private List<Table> tableList;

    @BeforeEach
    public void beforeEach() {
        String name = "Example Dashboard";
        this.dashboard = new Dashboard(name);
        this.tableList = new LinkedList<>();

        Table table1 = new Table("Example Table 1");
        table1.setPosition(0);
        this.tableList.add(table1);

        Table table2 = new Table("Example Table 2");
        table2.setPosition(1);
        this.tableList.add(table2);
    }

    @Test
    public void testSetName(){
        String expectedName = "Set Up Name";
        this.dashboard.setName(expectedName);

        String actualName = this.dashboard.getName();
        assertThat(actualName).isEqualTo(expectedName);
    }

    @Test
    public void testGetTables(){
        Dashboard dashboard = new Dashboard("Example Dashboard", this.tableList);
        List<Table> result = dashboard.getTableList();

        assertThat(result.size()).isEqualTo(2);
        assertThat(result).isEqualTo(this.tableList);
    }

    @Test
    public void testSetTables(){
        this.dashboard.setTableList(this.tableList);
        List<Table> result = dashboard.getTableList();

        assertThat(result.size()).isEqualTo(2);
        assertThat(result).isEqualTo(this.tableList);
    }

    @Test
    public void testAttachFile() {
        AttachedFile expectedAttachedFile = new AttachedFile("example.png", "example");

        this.dashboard.attachFile(expectedAttachedFile);
        List<AttachedFile> attachedFiles = this.dashboard.getAttachedAttachedFiles();

        Assertions.assertThat(attachedFiles).hasSize(1);
        AttachedFile attachedFile = attachedFiles.getFirst();
        Assertions.assertThat(attachedFile).isEqualTo(expectedAttachedFile);
    }

    @Test
    public void testDetachFile() {
        AttachedFile expectedAttachedFile = new AttachedFile("example.png", "example");

        this.dashboard.attachFile(expectedAttachedFile);
        List<AttachedFile> attachedFiles = this.dashboard.getAttachedAttachedFiles();

        Assertions.assertThat(attachedFiles).hasSize(1);
        this.dashboard.detachFile(expectedAttachedFile);

        Assertions.assertThat(attachedFiles).isEmpty();
    }

    @Test
    public void testAddTable() {
        Table expectedTable = new Table("Example Table 3");
        this.dashboard.addTable(expectedTable);

        List<Table> tables = this.dashboard.getTableList();
        Table resultTable = tables.getLast();

        Assertions.assertThat(tables).hasSize(1);
        Assertions.assertThat(resultTable).isEqualTo(expectedTable);
    }

    @Test
    public void testRemoveTable() {
        Table expectedTable = new Table("Example Table 3");
        this.dashboard.addTable(expectedTable);

        List<Table> tables = this.dashboard.getTableList();
        Table resultTable = tables.getLast();

        Assertions.assertThat(tables).hasSize(1);

        this.dashboard.removeTable(resultTable);
        Assertions.assertThat(tables).isEmpty();
    }

    @Test
    public void testGetAndSetTemplateCardList(){
        List<Card> cardList = new ArrayList<>();
        Card card1 = new Card("Example Card 1");
        Card card2 = new Card("Example Card 2");
        cardList.add(card1);
        cardList.add(card2);

        this.dashboard.setTemplateCardList(cardList);
        List<Card> templateCardList = this.dashboard.getTemplateCardList();

        Assertions.assertThat(templateCardList).hasSize(2);
        Assertions.assertThat(templateCardList).isEqualTo(cardList);
    }

    @Test
    public void testAddTemplateCard(){
        Card expectedCard = new Card("Example Card 1");
        this.dashboard.addTemplateCard(expectedCard);

        List<Card> templateCardList = this.dashboard.getTemplateCardList();
        Assertions.assertThat(templateCardList).hasSize(1);
        Assertions.assertThat(templateCardList.getFirst()).isEqualTo(expectedCard);
    }

    @Test
    public void testRemoveTemplateCard(){
        Card expectedCard = new Card("Example Card 1");
        this.dashboard.addTemplateCard(expectedCard);

        List<Card> templateCardList = this.dashboard.getTemplateCardList();
        Assertions.assertThat(templateCardList).hasSize(1);

        this.dashboard.removeTemplateCard(expectedCard);
        templateCardList = this.dashboard.getTemplateCardList();
        Assertions.assertThat(templateCardList).isEmpty();
    }
}
