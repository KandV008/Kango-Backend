package dev.kandv.kango.units.models;

import dev.kandv.kango.models.State;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class StateTest {
    private State state;

    @BeforeEach
    void beforeEach() {
        this.state = new State();
    }

    @Test
    void testGetAndSetFontSize(){
        State.FontSize expected = State.FontSize.LARGE;
        this.state.setFontSize(expected);
        State.FontSize result = this.state.getFontSize();

        assertThat(result).isEqualTo(expected);
    }

    @Test
    void testGetAndSetLanguage(){
        State.Language expected = State.Language.SPANISH;
        this.state.setLanguage(expected);
        State.Language result = this.state.getLanguage();

        assertThat(result).isEqualTo(expected);
    }

    @Test
    void testGetAndSetColorBlind(){
        State.ColorBlind expected = State.ColorBlind.ANY;
        this.state.setColorBlind(expected);
        State.ColorBlind result = this.state.getColorBlind();

        assertThat(result).isEqualTo(expected);
    }
}
