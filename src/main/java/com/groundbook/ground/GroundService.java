package com.groundbook.ground;

import java.time.Clock;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;

import org.springframework.stereotype.Service;

import com.groundbook.booking.BookingRepository;
import com.groundbook.error.NoSuchGround;
import com.groundbook.error.NoSuchVenue;

@Service
public class GroundService {

    private final VenueRepository venues;
    private final GroundRepository grounds;
    private final EventTypeRepository eventTypes;
    private final BookingRepository bookings;
    private final Clock clock;

    public GroundService(VenueRepository venues, GroundRepository grounds, EventTypeRepository eventTypes,
                         BookingRepository bookings, Clock clock) {
        this.venues = venues;
        this.grounds = grounds;
        this.eventTypes = eventTypes;
        this.bookings = bookings;
        this.clock = clock;
    }

    /**
     * A ground's day - by default today by the clock - with that weekday's hours and its bookings in from order,
     * each with its shadow worked out now from its event type.
     */
    public DayView day(String venue, String ground, LocalDate day) {
        long venueId = venueId(venue);
        GroundRepository.Row row = grounds.one(venueId, ground)
                .orElseThrow(() -> new NoSuchGround(venue, ground));
        LocalDate date = day == null ? LocalDate.now(clock) : day;
        LocalTime[] hours = hours(row, date.getDayOfWeek());

        return new DayView(venue, ground, date, hours[0], hours[1],
                bookings.onDay(venueId, ground, date).stream()
                        .map(b -> {
                            Shadow held = shadow(b.from(), b.to(), b.setupMinutes(), b.teardownMinutes());
                            return new DayBooking(b.bookingId(), b.event(), b.from(), b.to(), held.from(), held.to(),
                                    b.state());
                        }).toList());
    }

    /** The shadow: the slot plus its event type's setup before and teardown after. Never stored. */
    public Shadow shadow(Instant from, Instant to, int setupMinutes, int teardownMinutes) {
        return new Shadow(from.minus(Duration.ofMinutes(setupMinutes)),
                to.plus(Duration.ofMinutes(teardownMinutes)));
    }

    public static LocalTime[] hours(GroundRepository.Row row, DayOfWeek day) {
        if (day == DayOfWeek.SUNDAY) {
            return new LocalTime[]{row.sundayOpens(), row.sundayCloses()};
        }

        return new LocalTime[]{row.opens(), row.closes()};
    }

    private long venueId(String venue) {
    return venues.idOf(venue).orElseThrow(() -> new NoSuchVenue(venue));
}


public java.util.List<GroundRepository.Row> grounds(String venue) {
    return grounds.all(venueId(venue));
}

public java.util.List<EventTypeRepository.Row> eventTypes(String venue) {
    return eventTypes.all(venueId(venue));
}


}
