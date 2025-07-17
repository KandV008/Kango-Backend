package dev.kandv.kango.dtos;

import dev.kandv.kango.models.Tag;
import dev.kandv.kango.models.enums.CardType;
import dev.kandv.kango.models.enums.Color;
import dev.kandv.kango.models.utils.AttachedFile;
import dev.kandv.kango.models.utils.Check;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TemplateCardDTO {
        private Long id;
        private String title;
        private String description;
        private CardType cardType;
        private Color color;
        private List<AttachedFile> attachedFiles;
        private Date deadLine;
        private List<Check> checks;
        private int position;
        private List<Tag> tagList;
}


