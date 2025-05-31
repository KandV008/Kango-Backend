package dev.kandv.kango.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
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
    // TODO Add Tag List
    // TODO Add Automation List

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
}
