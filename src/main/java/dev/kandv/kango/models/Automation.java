package dev.kandv.kango.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Automation {
    private String name;
    private Domain domain;
    private Object subject;

    public enum Domain{
        CARD, TABLE
    }
}
