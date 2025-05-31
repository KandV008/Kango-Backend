package dev.kandv.kango.models;

import dev.kandv.kango.models.enums.Color;
import dev.kandv.kango.models.utils.AttachedFile;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Card {
    private String title;
    private String description;
    private Color color;
    private List<AttachedFile> attachedAttachedFiles = new LinkedList<>();
    private Date deadLine;
    private List<Check> checks = new LinkedList<>();
    private int position;
    private List<Tag> tagList = new LinkedList<>();

    @Getter
    @Setter
    public static class Check {
        private String label;
        private boolean checked;
        private int position;

        public Check(String label, boolean checked) {
            this.label = label;
            this.checked = checked;
        }
    }

    public Card(String title){
        this.title = title;
    }

    public Card(String title, int position){
        this.title = title;
        this.position = position;
    }

    public void attachFile(AttachedFile attachedFile){
        this.attachedAttachedFiles.add(attachedFile);
    }

    public void detachFile(AttachedFile attachedFile){
        this.attachedAttachedFiles.remove(attachedFile);
    }

    public void addCheckToCheckList(Check check){
        check.setPosition(this.checks.size());
        this.checks.add(check);
    }

    public void removeCheckFromCheckList(Check check) {
        this.checks.remove(check);
        for (int i = 0; i < this.checks.size(); i++) {
            this.checks.get(i).setPosition(i);
        }
    }

    public void updateCheckFromCheckList(Check check){
        this.checks.set(check.position, check);
    }

    public void addTagToTagList(Tag tag) {
        this.tagList.add(tag);
    }

    public void removeTagFromTagList(Tag tag) {
        this.tagList.remove(tag);
    }
}
