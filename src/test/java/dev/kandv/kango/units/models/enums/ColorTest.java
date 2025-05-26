package dev.kandv.kango.units.models.enums;

import dev.kandv.kango.models.enums.Color;
import org.junit.Test;

import static dev.kandv.kango.models.enums.EnumConfig.PURPLE_HEX_CODE;
import static dev.kandv.kango.models.enums.EnumConfig.PURPLE_LABEL;
import static org.assertj.core.api.Assertions.assertThat;

public class ColorTest {

    @Test
    public void testGetHexColor(){
        Color color = Color.PURPLE;

        String resultHexColor = color.getHexCode();

        assertThat(resultHexColor).isEqualTo(PURPLE_HEX_CODE);
    }

    @Test
    public void testGetLabel(){
        Color color = Color.PURPLE;

        String resultLabel = color.getLabel();

        assertThat(resultLabel).isEqualTo(PURPLE_LABEL);
    }
}
