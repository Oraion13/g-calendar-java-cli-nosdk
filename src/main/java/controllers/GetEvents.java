package controllers;

import utils.ValidIOHandlers;

public class GetEvents {
    EventsManagement eventsManagement = null;

    public GetEvents(EventsManagement eventsManagement) {
        this.eventsManagement = eventsManagement;

    }

    // main caller
    public void getEvents() {
        System.out.print("\033[H\033[2J");
        while (true) {
            displayEventOptions();
            int choice = displayEventOperations();

            if (choice != 0) {
                continue;
            }

            break;
        }
    }

    // -------------------------- Event Operations --------------------------- //

    /**
     * Prints the event options
     * 1 - Display first 10 events
     * 2 - Get Events Between dates
     */
    public void displayEventOptions() {
        System.out.println("\n------------------------------------------");
        System.out.println("\t\tGet Events");
        System.out.println("------------------------------------------\n");
        System.out.println("\n------------------------------------------");
        System.out.println("Choices: ");
        System.out.println("0 - Exit");
        System.out.println("1 - Display first 10 events");
        System.out.println("2 - Get Events Between dates");
        System.out.println("------------------------------------------\n");
    }

    /**
     * Get a main operation choice
     * 
     * @return an intger value (main option)
     */
    public int displayEventOperations() {
        int choice = 0;
        try {
            choice = ValidIOHandlers.getChoice("Enter a choice [0 - 2]: ");

            switch (choice) {
                // display first 10 events
                case 1:
                    eventsManagement.printEvents(
                            eventsManagement.getFirstNEvents(10)
                );
                    break;

                // print events between
                case 2:
                    eventsManagement.printEvents(
                            eventsManagement.getEventsBetween(ValidIOHandlers
                                    .getDate("Enter a Starting date [YYYY-MM-DD]: ", 1),
                                    ValidIOHandlers
                                            .getDate("Enter a Ending date [YYYY-MM-DD / 0]: ", 0),
                                    ValidIOHandlers.getChoice("Enter events limit [Number]:")));
                    break;

                default:
                    return choice;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return choice;
    }
}
