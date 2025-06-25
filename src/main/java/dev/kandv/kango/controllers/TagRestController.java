package dev.kandv.kango.controllers;

import dev.kandv.kango.dtos.TagDTO;
import dev.kandv.kango.models.Tag;
import dev.kandv.kango.services.TagService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static dev.kandv.kango.controllers.ErrorMessagesRestControllers.INTERNAL_SERVER_ERROR;
import static dev.kandv.kango.controllers.RestControllerUtils.checkTag;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class TagRestController {

    public static final String INVALID_LABEL = "ERROR: Invalid Tag Label. Value: ";
    public static final String INVALID_COLOR = "ERROR: Invalid Tag Color. Value: ";
    public static final String INVALID_VISIBILITY = "ERROR: Invalid Tag Visibility. Value: ";

    private final TagService tagService;

    @PostMapping("/tags")
    public ResponseEntity<Tag> createTag(@RequestBody TagDTO tagDTO) {
        Tag newTag = this.checkTagDTO(tagDTO);

        Tag createdTag = this.tagService.createTag(newTag);

        return ResponseEntity.status(201).body(createdTag);
    }

    @GetMapping("/tags/{id}")
    public ResponseEntity<Tag> getTag(@PathVariable Long id) {
        Tag currentTag = this.tagService.getSpecificTagById(id);

        checkTag(id, currentTag);

        return ResponseEntity.status(200).body(currentTag);
    }

    @GetMapping("/global-tags")
    public ResponseEntity<List<Tag>> getGlobalTags() {
        List<Tag> allGlobalTemplateCards = this.tagService.getAllGlobalTags();

        return ResponseEntity.status(200).body(allGlobalTemplateCards);
    }

    @DeleteMapping("/tags/{id}")
    public ResponseEntity<TagDTO> deleteTag(@PathVariable Long id) {
        Tag currentTag = this.tagService.getSpecificTagById(id);

        checkTag(id, currentTag);

        this.tagService.removeTagById(id);
        Tag nullTag = this.tagService.getSpecificTagById(id);

        if (nullTag != null) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, INTERNAL_SERVER_ERROR);
        }

        TagDTO tagDTO = new TagDTO(currentTag.getLabel(), currentTag.getColor(), currentTag.getVisibility());
        return new ResponseEntity<>(tagDTO, HttpStatus.OK);
    }

    @PutMapping("/tags/{id}")
    public ResponseEntity<Tag> updateTag(@PathVariable Long id, @RequestBody TagDTO tagDTO) {
        this.checkTagDTO(tagDTO);

        Tag currentTag = this.tagService.getSpecificTagById(id);
        checkTag(id, currentTag);

        currentTag.setLabel(tagDTO.getLabel());
        currentTag.setColor(tagDTO.getColor());

        this.tagService.updateTag(id, currentTag);
        Tag updatedTag = this.tagService.getSpecificTagById(id);
        return new ResponseEntity<>(updatedTag, HttpStatus.OK);
    }

    private Tag checkTagDTO(TagDTO tagDTO) {
        if (tagDTO.getLabel() == null ){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, INVALID_LABEL + null);
        }

        if (tagDTO.getColor() == null ){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, INVALID_COLOR + null);
        }

        if (tagDTO.getVisibility() == null ){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, INVALID_VISIBILITY + null);
        }

        return new Tag(tagDTO.getLabel(), tagDTO.getColor(), tagDTO.getVisibility());
    }

}
