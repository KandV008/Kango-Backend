package dev.kandv.kango.controllers;

import dev.kandv.kango.models.Card;
import dev.kandv.kango.models.Dashboard;
import dev.kandv.kango.models.Table;
import dev.kandv.kango.models.Tag;
import dev.kandv.kango.models.utils.AttachedFile;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import static dev.kandv.kango.controllers.ErrorMessagesRestControllers.*;
import static dev.kandv.kango.controllers.ErrorMessagesRestControllers.TAG_NOT_FOUND;

public class RestControllerUtils {

    private RestControllerUtils() {
        throw new IllegalStateException("Utility class");
    }

    public static void checkAttachedFile(AttachedFile attachedFile) {
        if (attachedFile == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, NULL_ATTACHED_FILE);
        }

        String fileName = attachedFile.getFileName();
        String fileUrl = attachedFile.getFileUrl();

        if (fileName.isEmpty() || fileUrl.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, INVALID_ATTACHED_FILE);
        }
    }

    public static void checkCard(Long id, Card currentCard) {
        if (currentCard == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, CARD_NOT_FOUND + id);
        }
    }

    public static void checkTag(Long id, Tag currentTag) {
        if (currentTag == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, TAG_NOT_FOUND + id);
        }
    }

    public static void checkDashboard(Long id, Dashboard currentDashboard) {
        if (currentDashboard == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, DASHBOARD_NOT_FOUND + id);
        }
    }

    public static void checkTable(Long id, Table currentTable) {
        if (currentTable == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, TABLE_NOT_FOUND + id);
        }
    }
}
