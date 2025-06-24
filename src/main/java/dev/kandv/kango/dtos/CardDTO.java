package dev.kandv.kango.dtos;

import dev.kandv.kango.models.enums.CardType;
import dev.kandv.kango.models.enums.Color;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CardDTO {
    private String title;
    private String description;
    private CardType cardType;
    private Color color;
    private Date deadLine;

    public CardDTO(String title, CardType cardType) {
        this.title = title;
        this.cardType = cardType;
    }

}
