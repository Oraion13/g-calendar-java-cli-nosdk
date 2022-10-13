import authentication.LoginSignup;
import authentication.OTPGenerator;
import controllers.DeleteEvents;
import controllers.EventsManagement;
import controllers.GetEvents;
import controllers.PostEvents;
import controllers.UpdateEvents;
import utils.ValidIOHandlers;

import calendar.Calendar;

public class Operations {
    EventsManagement eventsManagement = null;

    // main caller
    public Operations(Calendar service) {
        this.eventsManagement = new EventsManagement(service, "primary");
        while (true) {
            // print the options
            preMainOptions();
            int choice = preMainOperations();

            // exit point
            if (choice != 0) {
                continue;
            }

            break;
        }
    }

    private void callMainOps(){
        while (true) {
            // print the options
            mainOptions();
            int choice = mainOperations();

            // exit point
            if (choice != 0) {
                continue;
            }

            break;
        }
    }

    // ----------------------------- Main Operations ---------------------------- //

    /**
     * Prints the main options
     */
    private void mainOptions() {
        System.out.print("\033[H\033[2J");
        System.out.println("\n------------------------------------------");
        System.out.println("\t\tGoogle Calander");
        System.out.println("------------------------------------------\n");
        System.out.println("\n------------------------------------------");
        System.out.println("Choices: ");
        System.out.println("0 - Exit");
        System.out.println("1 - Display events");
        System.out.println("2 - Create events");
        System.out.println("3 - Delete events");
        System.out.println("4 - Update events");
        System.out.println("------------------------------------------\n");
    }

    /**
     * Get a main operation choice
     * 
     * 0 - Exit
     * 1 - Display events
     * 2 - Create events
     * 3 - Delete events
     * 4 - Update events
     * 
     * @return an intger value (main option)
     */
    private int mainOperations() {
        int choice = 0;
        try {
            choice = ValidIOHandlers.getChoice("Enter a choice [0 - 4]: ");

            switch (choice) {
                // display events
                case 1:
                    new GetEvents(eventsManagement).getEvents();
                    break;

                // create events
                case 2:
                    new PostEvents(eventsManagement).postEvents();
                    break;

//                // delete events
                case 3:
                    new DeleteEvents(eventsManagement).deleteEvents();
                    break;

                // update events
                case 4:
                    new UpdateEvents(eventsManagement).updateEvent();
                    break;

                default:
                    return choice;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return choice;
    }

    private void preMainOptions() {
        System.out.print("\033[H\033[2J");
        System.out.println("\n------------------------------------------");
        System.out.println("\t\tGoogle Calander");
        System.out.println("------------------------------------------\n");
        System.out.println("\n------------------------------------------");
        System.out.println("Choices: ");
        System.out.println("0 - Exit");
        System.out.println("1 - Login");
        System.out.println("2 - Sign up");
        System.out.println("------------------------------------------\n");
    }

    private int preMainOperations() {
        int choice = 0;
        try {
            choice = ValidIOHandlers.getChoice("Enter a choice [0 - 4]: ");

            switch (choice) {
                // Login
                case 1:
                    boolean isLoggedIn = new LoginSignup().userLogin();
                    // Do operations for login and redirect to main operations
                    if(isLoggedIn){
                        callMainOps();
                    }
                    break;

                // Signup
                case 2:
                    new LoginSignup().signup();
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
