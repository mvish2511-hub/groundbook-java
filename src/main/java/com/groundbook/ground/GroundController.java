package com.groundbook.ground;

import java.time.LocalDate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** The grounds, the event types and a ground's day. Parses the request and delegates; no rules live here. */
@RestController
@RequestMapping("/venues/{venue}")
public class GroundController {

    private final GroundService grounds;

    public GroundController(GroundService grounds) {
        this.grounds = grounds;
    }

    @GetMapping("/grounds")
    public GroundsView grounds(@PathVariable String venue) {
        var items = grounds.grounds(venue).stream()
                .map(row -> new GroundView(
                        row.code(),
                        row.name(),
                        row.opens(),
                        row.closes(),
                        row.sundayOpens(),
                        row.sundayCloses()))
                .toList();
        return new GroundsView(venue, items);
    }

    @GetMapping("/event-types")
    public EventTypesView eventTypes(@PathVariable String venue) {
        var items = grounds.eventTypes(venue).stream()
                .map(row -> new EventTypeView(
                        row.code(),
                        row.name(),
                        row.setupMinutes(),
                        row.teardownMinutes()))
                .toList();
        return new EventTypesView(items);
    }

    @GetMapping("/grounds/{ground}/day")
    public DayView day(
            @PathVariable String venue,
            @PathVariable String ground,
            @RequestParam(required = false) LocalDate date) {
        return grounds.day(venue, ground, date);
    }
}
