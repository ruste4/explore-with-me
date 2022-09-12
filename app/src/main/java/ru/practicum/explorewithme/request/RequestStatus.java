package ru.practicum.explorewithme.request;

public enum RequestStatus {

    PENDING("PENDING"),

    CONFIRMED("CONFIRMED"),

    CANCELED("CANCELED");

    private final String val;

    RequestStatus(String val) {
        this.val = val;
    }

    public String getVal() {
        return val;
    }

    public static RequestStatus findByName(String name) {
        for (RequestStatus status : values()) {
            if (status.name().equalsIgnoreCase(name)) {
                return status;
            }
        }

        throw new IllegalArgumentException(String.format("RequestStatus with name:%s not exist", name));
    }
}
