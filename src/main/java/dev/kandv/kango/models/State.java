package dev.kandv.kango.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class State {
    private FontSize fontSize = FontSize.MEDIUM;
    private Language language = Language.ENGLISH;
    private ColorBlind colorBlind = ColorBlind.NONE;
    private List<Dashboard> dashboardList = new ArrayList<>();
    private List<Tag> tagList = new LinkedList<>();
    private List<Automation> automationList = new LinkedList<>();

    public enum FontSize {
        SMALL, MEDIUM, LARGE
    }

    public enum Language {
        SPANISH, ENGLISH
    }

    public enum ColorBlind {
        NONE, ANY
    }

    public void addDashboard(Dashboard dashboard) {
        this.dashboardList.add(dashboard);
    }

    public void removeDashboard(Dashboard dashboard) {
        this.dashboardList.remove(dashboard);
    }

    public void addTagToTagList(Tag tag) {
        this.tagList.add(tag);
    }

    public void removeTagFromTagList(Tag tag) {
        this.tagList.remove(tag);
    }

    public void addAutomationToAutomationList(Automation automation) {
        this.automationList.add(automation);
    }

    public void removeAutomationFromAutomation(Automation automation) {
        this.automationList.remove(automation);
    }
}
