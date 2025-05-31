package dev.kandv.kango.units.models;

import dev.kandv.kango.models.*;
import dev.kandv.kango.models.enums.Color;
import dev.kandv.kango.models.utils.AttachedFile;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class DashboardTest {
    private Dashboard dashboard;
    private List<Table> tableList;

    @BeforeEach
    void beforeEach() {
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
    void testSetName(){
        String expectedName = "Set Up Name";
        this.dashboard.setName(expectedName);

        String actualName = this.dashboard.getName();
        assertThat(actualName).isEqualTo(expectedName);
    }

    @Test
    void testGetTables(){
        Dashboard exampleDashboard = new Dashboard("Example Dashboard", this.tableList);
        List<Table> result = exampleDashboard.getTableList();

        assertThat(result).hasSize(2);
        assertThat(result).isEqualTo(this.tableList);
    }

    @Test
    void testSetTables(){
        this.dashboard.setTableList(this.tableList);
        List<Table> result = dashboard.getTableList();

        assertThat(result).hasSize(2);
        assertThat(result).isEqualTo(this.tableList);
    }

    @Test
    void testAttachFile() {
        AttachedFile expectedAttachedFile = new AttachedFile("example.png", "example");

        this.dashboard.attachFile(expectedAttachedFile);
        List<AttachedFile> attachedFiles = this.dashboard.getAttachedAttachedFiles();

        assertThat(attachedFiles).hasSize(1);
        AttachedFile attachedFile = attachedFiles.getFirst();
        assertThat(attachedFile).isEqualTo(expectedAttachedFile);
    }

    @Test
    void testDetachFile() {
        AttachedFile expectedAttachedFile = new AttachedFile("example.png", "example");

        this.dashboard.attachFile(expectedAttachedFile);
        List<AttachedFile> attachedFiles = this.dashboard.getAttachedAttachedFiles();

        assertThat(attachedFiles).hasSize(1);
        this.dashboard.detachFile(expectedAttachedFile);

        assertThat(attachedFiles).isEmpty();
    }

    @Test
    void testAddTable() {
        Table expectedTable = new Table("Example Table 3");
        this.dashboard.addTable(expectedTable);

        List<Table> tables = this.dashboard.getTableList();
        Table resultTable = tables.getLast();

        assertThat(tables).hasSize(1);
        assertThat(resultTable).isEqualTo(expectedTable);
    }

    @Test
    void testRemoveTable() {
        Table expectedTable = new Table("Example Table 3");
        this.dashboard.addTable(expectedTable);

        List<Table> tables = this.dashboard.getTableList();
        Table resultTable = tables.getLast();

        assertThat(tables).hasSize(1);

        this.dashboard.removeTable(resultTable);
        assertThat(tables).isEmpty();
    }

    @Test
    void testGetAndSetTemplateCardList(){
        List<Card> cardList = new ArrayList<>();
        Card card1 = new Card("Example Card 1");
        Card card2 = new Card("Example Card 2");
        cardList.add(card1);
        cardList.add(card2);

        this.dashboard.setTemplateCardList(cardList);
        List<Card> templateCardList = this.dashboard.getTemplateCardList();

        assertThat(templateCardList)
                .hasSize(2)
                .isEqualTo(cardList);
    }

    @Test
    void testAddTemplateCard(){
        Card expectedCard = new Card("Example Card 1");
        this.dashboard.addTemplateCard(expectedCard);

        List<Card> templateCardList = this.dashboard.getTemplateCardList();
        assertThat(templateCardList).hasSize(1);
        assertThat(templateCardList.getFirst()).isEqualTo(expectedCard);
    }

    @Test
    void testRemoveTemplateCard(){
        Card expectedCard = new Card("Example Card 1");
        this.dashboard.addTemplateCard(expectedCard);

        List<Card> templateCardList = this.dashboard.getTemplateCardList();
        assertThat(templateCardList).hasSize(1);

        this.dashboard.removeTemplateCard(expectedCard);
        templateCardList = this.dashboard.getTemplateCardList();
        assertThat(templateCardList).isEmpty();
    }

    @Test
    void testAddTagToTagList() {
        Tag newTag = new Tag("example", Color.PURPLE);

        this.dashboard.addTagToTagList(newTag);
        List<Tag> tags = this.dashboard.getTagList();
        assertThat(tags).hasSize(1);
    }

    @Test
    void testRemoveTagFromTagList() {
        Tag newTag = new Tag("example", Color.PURPLE);

        this.dashboard.addTagToTagList(newTag);
        List<Tag> tags = this.dashboard.getTagList();
        assertThat(tags).hasSize(1);

        this.dashboard.removeTagFromTagList(newTag);
        tags = this.dashboard.getTagList();
        assertThat(tags).isEmpty();
    }

    @Test
    void testAddAutomationToAutomationList() {
        Automation newAutomation = new Automation();

        this.dashboard.addAutomationToAutomationList(newAutomation);
        List<Automation> automations = this.dashboard.getAutomationList();
        assertThat(automations).hasSize(1);
    }

    @Test
    void testRemoveAutomationToAutomationList() {
        Automation newAutomation = new Automation();

        this.dashboard.addAutomationToAutomationList(newAutomation);
        List<Automation> automations = this.dashboard.getAutomationList();
        assertThat(automations).hasSize(1);

        this.dashboard.removeAutomationFromAutomation(newAutomation);
        automations = this.dashboard.getAutomationList();
        assertThat(automations).isEmpty();
    }
}
