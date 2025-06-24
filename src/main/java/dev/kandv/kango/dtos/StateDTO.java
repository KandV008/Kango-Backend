package dev.kandv.kango.dtos;

import dev.kandv.kango.models.State;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class StateDTO {
    private State.FontSize fontSize;
    private State.Language language;
    private State.ColorBlind colorBlind;
}
