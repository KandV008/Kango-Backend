package dev.kandv.kango.models.utils;

import jakarta.persistence.Embeddable;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class Check {
    @NonNull
    private String label;
    private boolean checked;
    private int position = -1;

    public Check(@NonNull String label, boolean checked) {
        this.label = label;
        this.checked = checked;
    }

    public Check(Check other) {
        this.label = other.label;
        this.checked = other.checked;
        this.position = other.position;
    }


    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Check )) {
            return false;
        }

        boolean checkCheckLabel = this.label.equals(((Check) obj).label);
        boolean checkCheckedCheck = this.checked == (((Check) obj).checked);
        return checkCheckLabel && checkCheckedCheck;
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
