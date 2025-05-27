package dev.kandv.kango.units.models;

import dev.kandv.kango.models.Card;
import dev.kandv.kango.models.enums.Color;
import org.assertj.core.api.AssertionsForClassTypes;
import org.junit.Test;

import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;


public class CardTest {

    @Test
    public void testSetTitle() {
        String expectedTitle = "Example Title";
        Card card = new Card();

        card.setTitle(expectedTitle);

        String resultTitle = card.getTitle();
        assertThat(resultTitle).isEqualTo(expectedTitle);
    }

    @Test
    public void testSetDescription() {
        String expectedDescription = "Example Description";
        Card card = new Card();

        card.setDescription(expectedDescription);

        String resultDescription = card.getDescription();
        assertThat(resultDescription).isEqualTo(expectedDescription);
    }

    @Test
    public void testSetColor() {
        Color expectedColor = Color.RED;
        Card card = new Card();

        card.setColor(expectedColor);

        Color resultColor = card.getColor();
        assertThat(resultColor).isEqualTo(expectedColor);
    }

    @Test
    public void testAttachFile() {
        Card.AttachedFile expectedAttachedFile = new Card.AttachedFile("example.png", "example");
        Card card = new Card();

        card.attachFile(expectedAttachedFile);
        List<Card.AttachedFile> attachedFiles = card.getAttachedAttachedFiles();

        assertThat(attachedFiles).hasSize(1);
        Card.AttachedFile attachedFile = attachedFiles.getFirst();
        assertThat(attachedFile).isEqualTo(expectedAttachedFile);
    }

    @Test
    public void testDetachFile() {
        Card.AttachedFile expectedAttachedFile = new Card.AttachedFile("example.png", "example");
        Card card = new Card();

        card.attachFile(expectedAttachedFile);
        List<Card.AttachedFile> attachedFiles = card.getAttachedAttachedFiles();

        assertThat(attachedFiles).hasSize(1);
        card.detachFile(expectedAttachedFile);

        assertThat(attachedFiles).isEmpty();
    }

    @Test
    public void testEndDate() {
        Date expectedDate = new Date();
        Card card = new Card();

        card.setDeadLine(expectedDate);
        Date resultDate = card.getDeadLine();

        assertThat(resultDate).isEqualTo(expectedDate);
    }

    @Test
    public void testAddCheckToCheckList() {
        Card card = new Card();
        Card.Check check = new Card.Check("Example", false);

        card.addCheckToCheckList(check);
        List<Card.Check> checks = card.getChecks();
        assertThat(checks).hasSize(1);
    }

    @Test
    public void testRemoveCheckFromCheckList() {
        Card card = new Card();
        Card.Check check = new Card.Check("Example", false);
        card.addCheckToCheckList(check);
        List<Card.Check> checks = card.getChecks();
        assertThat(checks).hasSize(1);

        card.removeCheckFromCheckList(check);
        checks = card.getChecks();
        assertThat(checks).isEmpty();
    }

    @Test
    public void testUpdateCheckFromCheckList() {
        Card card = new Card();
        Card.Check check = new Card.Check("Example", false);
        card.addCheckToCheckList(check);

        check.setChecked(true);
        card.updateCheckFromCheckList(check);

        List<Card.Check> checks = card.getChecks();
        assertThat(checks).hasSize(1);
        assertThat(checks.getFirst()).isEqualTo(check);
    }

    @Test
    public void testSetPosition(){
        String name = "example";
        int expectedPosition = 1;
        Card newCard = new Card(name);

        newCard.setPosition(expectedPosition);
        int result = newCard.getPosition();

        AssertionsForClassTypes.assertThat(result).isEqualTo(expectedPosition);
    }

    @Test
    public void testGetPosition(){
        String name = "example";
        int expectedPosition = 1;
        Card newCard = new Card(name, expectedPosition);

        int result = newCard.getPosition();

        AssertionsForClassTypes.assertThat(result).isEqualTo(expectedPosition);
    }
}
