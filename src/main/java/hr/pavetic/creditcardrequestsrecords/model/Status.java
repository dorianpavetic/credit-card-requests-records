package hr.pavetic.creditcardrequestsrecords.model;

public enum Status {
    ACTIVE,
    INACTIVE;

    public static Status parse(String statusString) {
        if (ACTIVE.name().equals(statusString))
            return ACTIVE;
        else if (INACTIVE.name().equals(statusString))
            return INACTIVE;
        else
            throw new IllegalArgumentException("Invalid status to parse: " + statusString);
    }
}
