package com.group1.swp.pizzario_swp391.dto.websocket;

public class TableReleaseRequest {
    private int tableId;
    private String sessionId;

    public TableReleaseRequest() {
    }

    public TableReleaseRequest(int tableId, String sessionId) {
        this.tableId = tableId;
        this.sessionId = sessionId;
    }

    public int getTableId() {
        return tableId;
    }

    public void setTableId(int tableId) {
        this.tableId = tableId;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }
}