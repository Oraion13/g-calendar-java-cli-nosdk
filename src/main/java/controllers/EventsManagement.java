package controllers;

import java.io.IOException;
import java.nio.file.Path;
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

    /**
     * Print events in map
     *
     * @param events Events in a Map<Integer, Event>
     */
    public void printMapEvents(Map<Integer, TypeEvent> events) {
        System.out.println();
        for (Map.Entry<Integer, TypeEvent> event : events.entrySet()) {
            System.out.println(
                    event.getKey() + " : " + event.getValue().getSummary() + " || ");
            System.out.println(event.getValue().getStart().getDateTime() != null ?
                    event.getValue().getStart().getDateTime() : event.getValue().getStart().getDate());
        }
    }

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
    public List<TypeEvent> getEventsBetween(String from, String to, int limit) {
        String requestURL;
        Event event = new Event();

        try{
            if (to.equals("0")) {
                requestURL = "events?maxResults=" + limit
                        + "&timeMin=" + setDateTime(from, "00:00:00");
            } else {
                requestURL = "events?maxResults=" + limit + "&timeMax=" + setDateTime(to, "00-00-00")
                        + "&timeMin=" + setDateTime(from, "00:00:00");
            }

            service.setRequestUrl(requestURL);
            String responseBody = service.sendRequest("GET", null);

            event.storeResponseBody(responseBody);
        }catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

        return event.getListOfEvents();
    }

    /**
     * GET events between two dates in map
     *
     * @param from  Events Starting date
     * @param to    Events Ending date
     * @param limit N - events
     * @return a list of events Map<Integer, Event>
     */
    public Map<Integer, TypeEvent> getEventsInMap(String from, String to, int limit) {
        Map<Integer, TypeEvent> map = new HashMap<>();
        int counter = 0;

        // get a list of events
        List<TypeEvent> events = getEventsBetween(from, to, 10);

        // put all events with an associated ID to delete
        for (TypeEvent event : events) {
            map.put(++counter, event);
        }

        return map;
    }

    /**
     * POST an event
     *
     * @param typeEvent New Event to insert
     */
    public void postEvent(TypeEvent typeEvent) {
        try{
            String requestURL = "events";
            Event event = new Event();

            service.setRequestUrl(requestURL);
            event.storeJSONRequestBody(event.setupRequestBody(typeEvent, "POST"));
            String responseBody = service.sendRequest("POST", Path.of(Event.REQUEST_BODY_FILE));
            event.storeResponseBody(responseBody);
        }catch (IOException | InterruptedException e){
            e.printStackTrace();
        }
    }

    /**
     * DELETE an event
     *
     * @param eventID an Event ID
     */
    public void deleteEvent(String eventID) {
        try{
            String requestURL = "events/" + eventID;

            Event event = new Event();
            service.setRequestUrl(requestURL);
            String responseBody = service.sendRequest("DELETE", null);
            event.storeResponseBody(responseBody);
        }catch (IOException | InterruptedException e){
            e.printStackTrace();
        }
    }

    /**
     * PUT (UPDATE) an event
     *
     * @param typeEvent An Event to update
     */
    public void updateEvent(TypeEvent typeEvent, String updateId) {
        try{
            String requestURL = "events/" + updateId;
            Event event = new Event();

            service.setRequestUrl(requestURL);
            event.storeJSONRequestBody(event.setupRequestBody(typeEvent, "PUT"));
            String responseBody = service.sendRequest("PUT", Path.of(Event.REQUEST_BODY_FILE));
            event.storeResponseBody(responseBody);
        }catch (IOException | InterruptedException e){
            e.printStackTrace();
        }
    }
}
