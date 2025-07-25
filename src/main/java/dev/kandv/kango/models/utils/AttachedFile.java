package dev.kandv.kango.models.utils;

import jakarta.persistence.Embeddable;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class AttachedFile {
    @NonNull
    private String fileName;
    @NonNull
    private String fileUrl;

    public AttachedFile(AttachedFile other) {
        this.fileName = other.fileName;
        this.fileUrl = other.fileUrl;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof AttachedFile )) {
            return false;
        }

        boolean checkFileName = this.fileName.equals(((AttachedFile) obj).fileName);
        boolean checkFileUrl = this.fileUrl.equals(((AttachedFile) obj).fileUrl);
        return checkFileName && checkFileUrl;
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
