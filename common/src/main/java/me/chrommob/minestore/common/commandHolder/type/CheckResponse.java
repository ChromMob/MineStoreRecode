package me.chrommob.minestore.common.commandHolder.type;

import java.util.List;

public class CheckResponse {
    /**
     * Response used when the validation request itself failed, so the store never judged the commands.
     */
    public static CheckResponse failed() {
        CheckResponse response = new CheckResponse(false, null);
        response.requestFailed = true;
        return response;
    }

    public CheckResponse(boolean status, List<CheckResponses> results) {
        this.status = status;
        this.results = results;
    }

    private boolean status;
    private List<CheckResponses> results;
    private String error;
    private transient boolean requestFailed;
    public static class CheckResponses {
        private int cmd_id;
        private boolean status;
        private String error;

        public int cmd_id() {
            return cmd_id;
        }

        public boolean status() {
            return status;
        }

        public String error() {
            return error;
        }
    }

    public boolean status() {
        return status;
    }

    public List<CheckResponses> results() {
        return results;
    }

    public String error() {
        return error;
    }

    public boolean isRequestFailed() {
        return requestFailed;
    }
}