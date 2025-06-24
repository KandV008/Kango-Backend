package dev.kandv.kango.models.enums;

import lombok.Getter;

import static dev.kandv.kango.models.enums.EnumConfig.*;

@Getter
public enum Color {
    PURPLE(PURPLE_HEX_CODE, PURPLE_LABEL),
    PINK(PINK_HEX_CODE, PINK_LABEL),
    YELLOW(YELLOW_HEX_CODE, YELLOW_LABEL),
    BLUE(BLUE_HEX_CODE, BLUE_LABEL),
    RED(RED_HEX_CODE, RED_LABEL),
    GREEN(GREEN_HEX_CODE, GREEN_LABEL),
    ORANGE(ORANGE_HEX_CODE, ORANGE_LABEL),
    BLACK(BLACK_HEX_CODE, BLACK_LABEL);

    private final String hexCode;
    private final String label;

    Color(String hexCode, String label) {
        this.hexCode = hexCode;
        this.label = label;
    }

}
