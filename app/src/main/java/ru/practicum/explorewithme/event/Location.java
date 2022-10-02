package ru.practicum.explorewithme.event;

import lombok.*;

import javax.persistence.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Embeddable
@AttributeOverrides({
        @AttributeOverride(name = "lat", column = @Column(name = "location_lat")),
        @AttributeOverride(name = "lon", column = @Column(name = "location_lon"))
})
public class Location {

    @Column(name = "lat", nullable = false)
    private Double lat;

    @Column(name = "lon", nullable = false)
    private Double lon;

}
