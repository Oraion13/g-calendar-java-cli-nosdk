package controllers;

import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

import calendar.Event;
import calendar.Event.TypeEvent;
import calendar.Calendar;

public class EventsManagement {
    Calendar service = null;
    String calanderId = null;

    // set the service credentials to access the API
    public EventsManagement(Calendar service, String calanderId) {
        this.service = service;
        this.calanderId = calanderId;
    }

    // ------------------------- Print Events ------------------------- //

    /**
     * Print events
     *
     * @param events Events in a List<Event>
     */
    public void printEvents(List<TypeEvent> events) {
        System.out.println();
        for (TypeEvent event : events) {
            System.out.print(event.getSummary() + " || ");
            System.out.println(event.getStart().getDateTime() != null ?
                    event.getStart().getDateTime() : event.getStart().getDate());
        }
    }

//    /**
//     * Print events in map
//     *
//     * @param events Events in a Map<Integer, Event>
//     */
//    public void printMapEvents(Map<Integer, Event> events) {
//        System.out.println();
//        for (Map.Entry<Integer, Event> event : events.entrySet()) {
//            System.out.println(
//                    event.getKey() + " : " + event.getValue().getSummary() + " || "
//                            + event.getValue().getStart().getDateTime());
//        }
//    }

    // ----------------------------- Get Events ----------------------------- //

    /**
     * GET First N events
     * 
     * @param limit N - events
     * @return a list of events List<Event>
     * @throws IOException
     */
    public List<Event.TypeEvent> getFirstNEvents(int limit) throws IOException, InterruptedException {
        String requestURL = "events?maxResults=" + limit;
        service.setRequestUrl(requestURL);
        String responseBody = service.sendRequest("GET", null);

        Event event = new Event();
        event.storeResponseBody(responseBody);

        return event.getListOfEvents();
    }

    /**
     * setup a rfc3339 DateTime
     *
     * @param date YYYY-MM-DD
     * @param time HH-MM-SS
     * @return YYYY-MM-DDTHH-MM-SSZ
     */
    public static String setDateTime(String date, String time){
        if(time == null) return date + "T" + "00:00:00Z";

        return date + "T" + time + "Z";
    }


    /**
     * GET events between two dates
     *
     * @param from  Events Starting date
     * @param to    Events Ending date
     * @param limit N - events
     * @return a list of events List<Event>
     * @throws IOException
     */
    public List<TypeEvent> getEventsBetween(String from, String to, int limit) throws IOException, InterruptedException {
        String requestURL;

        if(to.equals("0")){
            requestURL = "events?maxResults=" + limit
                    + "&timeMin=" + setDateTime(from, "00:00:00");
        }else{
            requestURL = "events?maxResults=" + limit + "&timeMax=" + setDateTime(to, "00-00-00")
                    + "&timeMin=" + setDateTime(from, "00:00:00");
        }

        service.setRequestUrl(requestURL);
        String responseBody = service.sendRequest("GET", null);

        Event event = new Event();
        event.storeResponseBody(responseBody);

        return event.getListOfEvents();
    }

//    /**
//     * GET events between two dates
//     *
//     * @param from  Events Starting date
//     * @param to    Events Ending date
//     * @param limit N - events
//     * @return a list of events Map<Integer, Event>
//     * @throws IOException
//     */
//    public Map<Integer, Event> getEventsInMap(String from, String to, int limit) throws IOException {
//        Map<Integer, Event> map = new HashMap<>();
//        int counter = 0;
//
//        // get a list of events
//        List<Event> events = getEventsBetween(from, to, 10);
//
//        // put all events with an associated ID to delete
//        for (Event event : events) {
//            map.put(++counter, event);
//        }
//
//        return map;
//    }

    /**
     * POST an event
     *
     * @param event New Event to insert
     * @throws IOException
     */
    public void postEvent(TypeEvent event) throws IOException {
//        event = service.events().insert(calanderId, event).execute();
    }

//    /**
//     * DELETE an event
//     *
//     * @param eventID an Event ID
//     * @throws IOException
//     */
//    public void deleteEvent(String eventID) throws IOException {
//        service.events().delete(calanderId, eventID).execute();
//    }
//
//    /**
//     * PUT (UPDATE) an event
//     *
//     * @param event An Event to update
//     * @throws IOException
//     */
//    public void updateEvent(Event event) throws IOException {
//        service.events().update(calanderId, event.getId(), event).execute();
//    }
}
