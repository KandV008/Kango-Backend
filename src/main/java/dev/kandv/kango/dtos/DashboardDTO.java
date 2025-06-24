package dev.kandv.kango.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.LinkedList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class DashboardDTO {
    private String name;
    private List<TableDTO> tableList;
    private List<CardDTO> templateCardList;
    private List<TagDTO> tagList;

    public DashboardDTO(String name){
        this.name = name;
        this.tableList = new LinkedList<>();
        this.templateCardList = new LinkedList<>();
        this.tagList = new LinkedList<>();
    }

}
