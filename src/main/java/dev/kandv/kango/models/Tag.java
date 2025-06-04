package dev.kandv.kango.models;

import dev.kandv.kango.models.enums.Color;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Tag {
    private String label;
    private Color color;
}
