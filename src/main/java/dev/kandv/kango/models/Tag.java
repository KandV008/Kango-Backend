package dev.kandv.kango.models;

import dev.kandv.kango.models.enums.Color;
import dev.kandv.kango.models.enums.Visibility;
import jakarta.persistence.*;
import jakarta.persistence.Table;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "tags")
public class Tag {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @NonNull
    private String label;
    @NonNull
    private Color color;
    private Visibility visibility = Visibility.LOCAL;
    @ManyToOne
    @JoinColumn(name = "dashboard_id")
    private Dashboard dashboard;

    public Tag(@NonNull String label, @NonNull Color color) {
        this.label = label;
        this.color = color;
    }

    public Tag(@NonNull String label, @NonNull Color color, Visibility visibility) {
        this.label = label;
        this.color = color;
        this.visibility = visibility;
    }

    public Tag(@NonNull String label, Visibility visibility){
        this.label = label;
        this.visibility = visibility;
    }

    public Tag(@NonNull Color color, Visibility visibility){
        this.color = color;
        this.visibility = visibility;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        Tag tag = (Tag) obj;
        return this.id != null && this.id.equals(tag.id);
    }
}
