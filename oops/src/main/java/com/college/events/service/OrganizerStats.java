package com.college.events.service;

/**
 * Aggregated numbers shown on the organizer dashboard overview.
 * A plain class (rather than a record) so Thymeleaf property access works uniformly.
 */
public class OrganizerStats {

    private final long totalEvents;
    private final long upcomingEvents;
    private final long completedEvents;
    private final long cancelledEvents;
    private final long draftEvents;
    private final long totalRegistrations;
    private final long totalParticipants;

    public OrganizerStats(long totalEvents, long upcomingEvents, long completedEvents,
                          long cancelledEvents, long draftEvents,
                          long totalRegistrations, long totalParticipants) {
        this.totalEvents = totalEvents;
        this.upcomingEvents = upcomingEvents;
        this.completedEvents = completedEvents;
        this.cancelledEvents = cancelledEvents;
        this.draftEvents = draftEvents;
        this.totalRegistrations = totalRegistrations;
        this.totalParticipants = totalParticipants;
    }

    public long getTotalEvents() {
        return totalEvents;
    }

    public long getUpcomingEvents() {
        return upcomingEvents;
    }

    public long getCompletedEvents() {
        return completedEvents;
    }

    public long getCancelledEvents() {
        return cancelledEvents;
    }

    public long getDraftEvents() {
        return draftEvents;
    }

    public long getTotalRegistrations() {
        return totalRegistrations;
    }

    public long getTotalParticipants() {
        return totalParticipants;
    }
}
