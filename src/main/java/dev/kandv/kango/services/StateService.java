package dev.kandv.kango.services;

import dev.kandv.kango.models.State;
import dev.kandv.kango.repositories.StateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

import static dev.kandv.kango.services.ErrorMessagesServices.INVALID_ELEMENT_ERROR;
import static dev.kandv.kango.services.ErrorMessagesServices.NOT_FOUND_STATE_ERROR;

@Service
@RequiredArgsConstructor
public class StateService {
    public static final String INVALID_STATE_CREATION_ERROR = "ERROR: Invalid State. Value: ";

    private final StateRepository stateRepository;

    public State createState(State state) {
        try{
            return this.stateRepository.save(state);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(INVALID_STATE_CREATION_ERROR + state);
        }
    }

    public State getState() {
        List<State> states = this.stateRepository.findAll();

        if (states.isEmpty()) {
            throw new NoSuchElementException(NOT_FOUND_STATE_ERROR);
        }

        return states.getFirst();
    }

    public void removeState() {
        this.stateRepository.deleteAll();
    }

    public boolean checkStatus() {
        List<State> states = this.stateRepository.findAll();

        return !states.isEmpty();
    }

    private void checkElementToUpdate(Object newObject, String element) {
        if (newObject == null) {
            throw new IllegalArgumentException(INVALID_ELEMENT_ERROR + element);
        }
    }

    public void updateFontSize(State.FontSize newFontSize) {
        this.checkElementToUpdate(newFontSize, "fontSize");

        State state = this.getState();
        state.setFontSize(newFontSize);

        this.stateRepository.save(state);
    }

    public void updateLanguage(State.Language newLanguage) {
        this.checkElementToUpdate(newLanguage, "language");

        State state = this.getState();
        state.setLanguage(newLanguage);

        this.stateRepository.save(state);
    }

    public void updateColorBlind(State.ColorBlind newColorBlind) {
        this.checkElementToUpdate(newColorBlind, "colorBlind");

        State state = this.getState();
        state.setColorBlind(newColorBlind);

        this.stateRepository.save(state);
    }
}
