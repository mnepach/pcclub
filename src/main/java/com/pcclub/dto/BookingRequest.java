package com.pcclub.dto;

import java.time.LocalDateTime;

public class BookingRequest {
    private Long workStationId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String status;

    public Long getWorkStationId() { return workStationId; }
    public void setWorkStationId(Long workStationId) { this.workStationId = workStationId; }
    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }
    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}