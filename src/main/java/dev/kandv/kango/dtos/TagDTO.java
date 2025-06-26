package dev.kandv.kango.dtos;

import dev.kandv.kango.models.enums.Color;
import dev.kandv.kango.models.enums.Visibility;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TagDTO {
    private String label;
    private Color color;
    private Visibility visibility;
}
