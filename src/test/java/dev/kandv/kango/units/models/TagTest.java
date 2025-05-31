package dev.kandv.kango.units.models;

import dev.kandv.kango.models.Tag;
import dev.kandv.kango.models.enums.Color;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class TagTest {
    private Tag tag;

    @BeforeEach
    public void beforeEach(){
        this.tag = new Tag("Example", Color.PURPLE);
    }

    @Test
    public void testSetAndGetLabel(){
        String expected = "Example Label";
        this.tag.setLabel(expected);
        String actual = this.tag.getLabel();
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void testSetAndGetColor(){
        Color expected = Color.GREEN;
        this.tag.setColor(expected);
        Color actual = this.tag.getColor();
        assertThat(actual).isEqualTo(expected);
    }
}
