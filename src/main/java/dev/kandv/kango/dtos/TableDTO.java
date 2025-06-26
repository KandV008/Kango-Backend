package dev.kandv.kango.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TableDTO {
    private String name;
    private int position;
    private List<CardDTO> cardList;

    public TableDTO(String name, List<CardDTO> cards) {
        this.name = name;
        this.cardList = cards;
    }
}
