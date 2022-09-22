//package controllers;
//
//import java.io.IOException;
//import java.util.Map;
//
//import utils.ValidIOHandlers;
//
//public class DeleteEvents {
//    EventsManagement eventsManagement = null;
//
//    public DeleteEvents(EventsManagement eventsManagement) {
//        this.eventsManagement = eventsManagement;
//    }
//
//    // main caller
//    public void deleteEvents() throws IOException {
//        while (true) {
//            System.out.print("\033[H\033[2J");
//            System.out.println("\n------------------------------------------");
//            System.out.println("\t\tDelete Events");
//            System.out.println("------------------------------------------\n");
//
//            System.out.println("Get events From - To...\nEnter '0' to exit...\n");
//
//            // get events start and ending date
//            String from = ValidIOHandlers.getDate("Enter the Start date [YYYY-MM-DD / 0]: ", 0);
//            if (from.equals("0"))
//                break;
//            String to = ValidIOHandlers.getDate("Enter the date 'To', '0' for no ending date [YYYY-MM-DD / 0]: ", 0);
//
//            // events limit
//            int limit = ValidIOHandlers.getChoice("Enter the number of events to get [Number]: ");
//
//            deleteEvents(from, to, limit);
//        }
//    }
//
//    /**
//     * Display and delete events
//     *
//     * @param from  Event starting date
//     * @param to    Event ending date
//     * @param limit Events limit
//     * @throws IOException
//     */
//    private void deleteEvents(String from, String to, int limit) throws IOException {
//        Map<Integer, Event> toDelete = eventsManagement.getEventsInMap(from, to, limit);
//        // display the events
//        System.out.println("Delete ID :   Event\n-----------------------------------");
//        eventsManagement.printMapEvents(toDelete);
//
//        System.out.print("Enter a Delete ID to delete an event [Number / 0]: ");
//        int deleteID = Integer.parseInt(System.console().readLine());
//
//        if (deleteID == 0)
//            return;
//
//        eventsManagement.deleteEvent(toDelete.get(deleteID).getId());
//        System.out.println("Event Deleted Successfully!\n----------------------------------");
//    }
//}
