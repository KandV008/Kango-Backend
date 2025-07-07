package dev.kandv.kango.controllers;

public class ErrorMessagesRestControllers {
    public static final String INTERNAL_SERVER_ERROR = "ERROR: Something gone wrong at server. It is not you fault.";

    public static final String NULL_ATTACHED_FILE = "ERROR: Attached File is null";
    public static final String INVALID_ATTACHED_FILE = "ERROR: Some or all attributes from Attached File are invalid";
    public static final String NULL_CHECK = "ERROR: Check is null";
    public static final String INVALID_CHECK = "ERROR: Some or all attributes from Check are invalid";

    public static final String CARD_NOT_FOUND = "ERROR: Card Not Found with that ID. ID: ";
    public static final String DASHBOARD_NOT_FOUND = "ERROR: Dashboard Not Found with that ID. ID: ";
    public static final String TABLE_NOT_FOUND = "ERROR: Table Not Found with that ID. ID: ";
    public static final String TAG_NOT_FOUND = "ERROR: Tag Not Found with that ID. ID: ";

    private ErrorMessagesRestControllers() {
        throw new IllegalStateException("Utility class");
    }
}
