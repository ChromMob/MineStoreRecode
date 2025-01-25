package me.chrommob.minestore.common.verification;

import java.util.List;

public class VerificationResult {
    private final boolean isValid;
    private final List<String> messages;
    private final TYPE type;

    public enum TYPE {
        STORE_URL,
        API_KEY,
        SECRET_KEY,
        DATABASE,
        WEBSTORE,
        SUPPORT
    }

    public VerificationResult(boolean isValid, List<String> messages, TYPE type) {
        this.type = type;
        this.isValid = isValid;
        this.messages = messages;
    }

    private static final VerificationResult valid = new VerificationResult(true, null, null);
    public static VerificationResult valid() {
        return valid;
    }

    public boolean isValid() {
        return isValid;
    }

    public List<String> messages() {
        return messages;
    }

    public TYPE type() {
        return type;
    }
}