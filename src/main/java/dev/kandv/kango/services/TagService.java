package dev.kandv.kango.services;

import dev.kandv.kango.models.Tag;
import dev.kandv.kango.models.enums.Visibility;
import dev.kandv.kango.repositories.TagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TagService {

    public static final String INVALID_TAG_CREATION_ERROR = "ERROR: Invalid Tag. Value: ";
    public static final String INVALID_ID_ERROR = "ERROR: The ID value is invalid. ID: ";
    public static final String NOT_FOUND_ID_ERROR = "ERROR: There is no Tag with such ID. ID: ";
    public static final String NULL_TAG_ERROR = "ERROR: The Tag is null.";
    public static final String INVALID_TAG_ERROR = "ERROR: The Tag with the updated data is not valid. Case: ";

    private final TagRepository tagRepository;

    public Tag getSpecificTagById(Long id) {
        Optional<Tag> tagById = this.tagRepository.findById(id);
        return tagById.orElse(null);
    }

    public Tag createTag(Tag tag) {
        try{
            return this.tagRepository.save(tag);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(INVALID_TAG_CREATION_ERROR + tag);
        }
    }

    public void removeAllTags() {
        this.tagRepository.deleteAll();
    }

    public void removeTagById(Long id) {
        this.tagRepository.deleteById(id);
    }

    public List<Tag> getAllGlobalTags() {
        return this.tagRepository.findAllByVisibilityEquals(Visibility.GLOBAL);
    }

    private void checkId(Long id) {
        if (id == null) {
            throw new IllegalArgumentException(INVALID_ID_ERROR + null);
        }
    }

    private void checkTagWithUpdate(Tag tagWithUpdate) {
        if (tagWithUpdate == null) {
            throw new IllegalArgumentException(NULL_TAG_ERROR);
        }
    }

    private Tag checkDatabaseResult(Long id, Optional<Tag> result) {
        if (result.isEmpty()) {
            throw new NoSuchElementException(NOT_FOUND_ID_ERROR + id);
        }

        return result.get();
    }

    public void updateTag(Long id, Tag updatedTag) {
        this.checkId(id);
        this.checkTagWithUpdate(updatedTag);

        Optional<Tag> tagById = this.tagRepository.findById(id);

        Tag currentTag = this.checkDatabaseResult(id, tagById);
        currentTag.setLabel(updatedTag.getLabel());
        currentTag.setColor(updatedTag.getColor());

        this.tagRepository.save(currentTag);
    }
}
