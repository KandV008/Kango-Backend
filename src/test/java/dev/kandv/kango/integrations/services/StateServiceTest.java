package dev.kandv.kango.integrations.services;

import dev.kandv.kango.KangoApplication;
import dev.kandv.kango.models.State;
import dev.kandv.kango.services.StateService;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.NoSuchElementException;

import static dev.kandv.kango.services.ErrorMessagesServices.INVALID_ELEMENT_ERROR;
import static dev.kandv.kango.services.ErrorMessagesServices.NOT_FOUND_STATE_ERROR;
import static dev.kandv.kango.services.StateService.INVALID_STATE_CREATION_ERROR;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Testcontainers
@SpringBootTest(classes = KangoApplication.class)
@ExtendWith(SpringExtension.class)
class StateServiceTest {

    @Container
    static PostgreSQLContainer<?> postgreSQLContainer =
            new PostgreSQLContainer<>("postgres:16-alpine")
                    .withDatabaseName("testdb")
                    .withUsername("test")
                    .withPassword("test");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgreSQLContainer::getJdbcUrl);
        registry.add("spring.datasource.username", postgreSQLContainer::getUsername);
        registry.add("spring.datasource.password", postgreSQLContainer::getPassword);
    }

    @Autowired
    private StateService stateService;

    private State state;

    @BeforeAll
    static void beforeAll(){
        postgreSQLContainer.start();
    }

    @AfterAll
    static void afterAll(){
        postgreSQLContainer.stop();
    }

    @BeforeEach
    void beforeEach(){
        this.state = new State();
    }

    @AfterEach
    void afterEach(){
        this.stateService.removeState();
    }

    @Test
    void testCreateState(){
        State expectedState = this.stateService.createState(this.state);

        assertThat(expectedState.getColorBlind()).isEqualTo(this.state.getColorBlind());
        assertThat(expectedState.getLanguage()).isEqualTo(this.state.getLanguage());
        assertThat(expectedState.getFontSize()).isEqualTo(this.state.getFontSize());
    }

    @Test
    void testCreateInvalidState(){
        IllegalArgumentException illegalArgumentException = assertThrows(IllegalArgumentException.class, () -> this.stateService.createState(null));

        assertThat(illegalArgumentException.getMessage()).isEqualTo(INVALID_STATE_CREATION_ERROR + null);
    }

    @Test
    void testGetState(){
        State expectedState = this.stateService.createState(this.state);

        State resultState = this.stateService.getState();

        assertThat(resultState).isEqualTo(expectedState);
    }

    @Test
    void testNotFoundState(){
        NoSuchElementException exception = assertThrows(NoSuchElementException.class, () -> this.stateService.getState());

        assertThat(exception.getMessage()).contains(NOT_FOUND_STATE_ERROR);
    }

    @Test
    void testDeleteState(){
        this.stateService.createState(this.state);

        this.stateService.removeState();

        assertThrows(IllegalArgumentException.class, () -> this.stateService.createState(null));
    }

    @Test
    void testCheckStateWithTrue(){
        this.stateService.createState(this.state);

        boolean checkState = this.stateService.checkStatus();

        assertThat(checkState).isTrue();
    }

    @Test
    void testCheckStateWithFalse(){
        boolean checkState = this.stateService.checkStatus();

        assertThat(checkState).isFalse();
    }

    @Test
    void testUpdateFontSizeState(){
        State expectedState = this.stateService.createState(this.state);
        State.FontSize newFontSize = State.FontSize.SMALL;

        this.stateService.updateFontSize(newFontSize);

        State resultState = this.stateService.getState();
        assertThat(resultState.getFontSize()).isEqualTo(newFontSize);
        assertThat(resultState).isEqualTo(expectedState);
    }

    @Test
    void testUpdateFontSizeStateWithInvalidFontSize(){
        this.stateService.createState(this.state);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () ->
                        this.stateService.updateFontSize(null)
        );

        assertThat(exception.getMessage()).contains(INVALID_ELEMENT_ERROR);
    }

    @Test
    void testUpdateFontSizeStateWithNoState(){
        State.FontSize newFontSize = State.FontSize.SMALL;

        NoSuchElementException exception = assertThrows(
                NoSuchElementException.class,
                () ->
                        this.stateService.updateFontSize(newFontSize)
        );

        assertThat(exception.getMessage()).isEqualTo(NOT_FOUND_STATE_ERROR);
    }

    @Test
    void testUpdateLanguageState(){
        State expectedState = this.stateService.createState(this.state);
        State.Language newLanguage = State.Language.ENGLISH;

        this.stateService.updateLanguage(newLanguage);

        State resultState = this.stateService.getState();
        assertThat(resultState.getLanguage()).isEqualTo(newLanguage);
        assertThat(resultState).isEqualTo(expectedState);
    }

    @Test
    void testUpdateLanguageStateWithInvalidLanguage(){
        this.stateService.createState(this.state);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () ->
                        this.stateService.updateLanguage(null)
        );

        assertThat(exception.getMessage()).contains(INVALID_ELEMENT_ERROR);
    }

    @Test
    void testUpdateLanguageStateWithNoState(){
        State.Language newLanguage = State.Language.ENGLISH;

        NoSuchElementException exception = assertThrows(
                NoSuchElementException.class,
                () ->
                        this.stateService.updateLanguage(newLanguage)
        );

        assertThat(exception.getMessage()).isEqualTo(NOT_FOUND_STATE_ERROR);
    }

    @Test
    void testUpdateColorBlindState(){
        State expectedState = this.stateService.createState(this.state);
        State.ColorBlind newColorBlind = State.ColorBlind.NONE;

        this.stateService.updateColorBlind(newColorBlind);

        State resultState = this.stateService.getState();
        assertThat(resultState.getColorBlind()).isEqualTo(newColorBlind);
        assertThat(resultState).isEqualTo(expectedState);
    }

    @Test
    void testUpdateColorBlindStateWithInvalidColorBlind(){
        this.stateService.createState(this.state);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () ->
                        this.stateService.updateColorBlind(null)
        );

        assertThat(exception.getMessage()).contains(INVALID_ELEMENT_ERROR);
    }

    @Test
    void testUpdateColorBlindStateWithNoState(){
        State.ColorBlind newColorBlind = State.ColorBlind.NONE;

        NoSuchElementException exception = assertThrows(
                NoSuchElementException.class,
                () ->
                        this.stateService.updateColorBlind(newColorBlind)
        );

        assertThat(exception.getMessage()).isEqualTo(NOT_FOUND_STATE_ERROR);
    }
}
