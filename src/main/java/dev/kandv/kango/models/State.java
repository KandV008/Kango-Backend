package dev.kandv.kango.models;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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
@Entity
public class State {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private FontSize fontSize = FontSize.MEDIUM;
    private Language language = Language.ENGLISH;
    private ColorBlind colorBlind = ColorBlind.NONE;
    //private List<Automation> automationList = new LinkedList<>(); TODO Decide what to do

    public enum FontSize {
        SMALL, MEDIUM, LARGE
    }

    public enum Language {
        SPANISH, ENGLISH
    }

    public enum ColorBlind {
        NONE, ANY
    }

    //public void addAutomationToAutomationList(Automation automation) {
    //    this.automationList.add(automation);
    //}
//
    //public void removeAutomationFromAutomation(Automation automation) {
    //    this.automationList.remove(automation);
    //}

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        State state = (State) obj;
        return this.id != null && this.id.equals(state.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

}
