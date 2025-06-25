package dev.kandv.kango.controllers;

import dev.kandv.kango.dtos.StateDTO;
import dev.kandv.kango.models.State;
import dev.kandv.kango.services.StateService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.NoSuchElementException;

import static dev.kandv.kango.controllers.ErrorMessagesRestControllers.INTERNAL_SERVER_ERROR;
import static dev.kandv.kango.controllers.ErrorMessagesRestControllers.STATE_NOT_FOUND;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class StateRestController {
    public static final String INVALID_FONT_SIZE_ERROR = "ERROR: Invalid State Font Size. Value: ";
    public static final String INVALID_LANGUAGE_ERROR = "ERROR: Invalid State Language. Value: ";
    public static final String INVALID_COLOR_BLIND_ERROR = "ERROR: Invalid State Color Blind. Value: ";

    private final StateService stateService;

    private void checkStateFontSize(State.FontSize fontSize) {
        if (fontSize == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, INVALID_FONT_SIZE_ERROR + null);
        }
    }

    private void checkStateLanguage(State.Language language) {
        if (language == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, INVALID_LANGUAGE_ERROR + null);
        }
    }

    private void checkStateColorBlind(State.ColorBlind colorBlind) {
        if (colorBlind == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, INVALID_COLOR_BLIND_ERROR + null);
        }
    }

    @PostMapping("/state")
    public ResponseEntity<State> createState() {
        State newState = new State();
        State createdState = this.stateService.createState(newState);

        return ResponseEntity.status(201).body(createdState);
    }

    @GetMapping("/state")
    public ResponseEntity<State> getState() {
        try {
            State currentState = this.stateService.getState();

            return ResponseEntity.status(200).body(currentState);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, STATE_NOT_FOUND);
        }
    }

    @DeleteMapping("/state")
    public ResponseEntity<StateDTO> deleteState() {
        boolean exists = this.stateService.checkStatus();

        if (!exists) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, STATE_NOT_FOUND);
        }

        State currentState = this.stateService.getState();

        this.stateService.removeState();
        boolean isFail = this.stateService.checkStatus();

        if (isFail) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, INTERNAL_SERVER_ERROR);
        }

        StateDTO stateDTO = new StateDTO(currentState.getFontSize(), currentState.getLanguage(), currentState.getColorBlind());
        return new ResponseEntity<>(stateDTO, HttpStatus.OK);
    }

    @GetMapping("/state/check")
    public ResponseEntity<Void> checkState() {
        boolean exist = this.stateService.checkStatus();

        if (!exist) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, STATE_NOT_FOUND);
        }

        return new ResponseEntity<>(null, HttpStatus.NO_CONTENT);
    }

    @PutMapping("/state/font-size")
    public ResponseEntity<State> updateFontSizeState(@RequestBody StateDTO stateDTO) {
        State.FontSize fontSize = stateDTO.getFontSize();
        this.checkStateFontSize(fontSize);

        try{
            this.stateService.updateFontSize(fontSize);
            State state = this.stateService.getState();

            return ResponseEntity.status(200).body(state);
        } catch (NoSuchElementException e){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, STATE_NOT_FOUND);
        }
    }

    @PutMapping("/state/language")
    public ResponseEntity<State> updateLanguageState(@RequestBody StateDTO stateDTO) {
        State.Language language = stateDTO.getLanguage();
        this.checkStateLanguage(language);

        try{
            this.stateService.updateLanguage(language);
            State state = this.stateService.getState();

            return ResponseEntity.status(200).body(state);
        } catch (NoSuchElementException e){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, STATE_NOT_FOUND);
        }
    }

    @PutMapping("/state/color-blind")
    public ResponseEntity<State> updateColorBlindState(@RequestBody StateDTO stateDTO) {
        State.ColorBlind colorBlind = stateDTO.getColorBlind();
        this.checkStateColorBlind(colorBlind);

        try{
            this.stateService.updateColorBlind(colorBlind);
            State state = this.stateService.getState();

            return ResponseEntity.status(200).body(state);
        } catch (NoSuchElementException e){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, STATE_NOT_FOUND);
        }
    }

}
