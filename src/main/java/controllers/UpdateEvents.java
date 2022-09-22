//package controllers;
//
//import java.io.IOException;
//import java.util.Map;
//
//import com.google.api.services.calendar.model.Event;
//
//import utils.ValidIOHandlers;
//
//public class UpdateEvents extends PostEvents {
//    public UpdateEvents(EventsManagement eventsManagement) {
//        super(eventsManagement);
//    }
//
//    // main caller
//    public void updateEvent() throws IOException {
//        while (true) {
//            System.out.print("\033[H\033[2J");
//            System.out.println("\n------------------------------------------");
//            System.out.println("\t\tUpdate Events");
//            System.out.println("------------------------------------------\n");
//
//            System.out.println("Get events From - To...\nEnter '0' to exit...\n");
//
//            String from = ValidIOHandlers.getDate("Enter the Start date [YYYY-MM-DD / 0]: ", 0);
//            // 0 - break the loop
//            if (from.equals("0"))
//                break;
//            String to = ValidIOHandlers.getDate("Enter the date 'To', '0' for no ending date [YYYY-MM-DD / 0]: ", 0);
//
//            // events limit
//            int limit = ValidIOHandlers.getChoice("Enter the number of events to get [Number]: ");
//
//            System.out.println("\n!! If you press 'n' for every update, the values will not be changed !!\n");
//
//            updateEvents(from, to, limit);
//        }
//    }
//
//    // display events to be updated
//    private void updateEvents(String from, String to, int limit) throws IOException {
//        Map<Integer, Event> toUpdate = eventsManagement.getEventsInMap(from, to, limit);
//
//        // display the events
//        System.out.println("Update ID   :   Event\n-----------------------------------");
//        eventsManagement.printMapEvents(toUpdate);
//
//        int updateID = ValidIOHandlers.getChoice("Enter a Update ID to update an event [Number / 0]: ");
//        if (updateID == 0)
//            return;
//
//        Event event = toUpdate.get(updateID);
//
//        // set default values to create an event
//        setDefaultValues(event);
//
//        // add recurence rules?
//        if (ValidIOHandlers.getYorN("\nIs a resucrsive event? [Y/n]: ")) {
//            setReurrenceRules(event);
//        }
//
//        // set attendees
//        if (ValidIOHandlers.getYorN("\nInvite attendees to event? [Y/n]: ")) {
//            setAttendees(event);
//        }
//
//        // set remainders
//        if (ValidIOHandlers.getYorN("\nSet remainders or use default? [Y/n]: ")) {
//            setRemainder(event);
//        }
//
//        eventsManagement.updateEvent(event);
//
//        System.out.println("Event Updated Successfully!\n----------------------------------");
//    }
//}
