package controllers;

import calendar.Event;
import utils.ValidIOHandlers;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.EnumSet;
import java.util.List;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import calendar.Event.TypeEvent;

public class PostEvents {
    EventsManagement eventsManagement = null;
    // flag to check if the timeformat are same
    boolean timeFlag;

    // FREQ in RRULE
    enum Frequency {
        YEARLY, MONTHLY, WEEKLY, DAILY, HOURLY, MINUTELY, SECONDLY;

        // display frequency
        public static void printFrequency() {
            int count = 1;
            for (Frequency freq : EnumSet.allOf(Frequency.class)) {
                System.out.println(count++ + " : " + freq);
            }
        }

        // get frequency
        public static Frequency getFrequency(int n) {
            int count = 1;
            for (Frequency freq : EnumSet.allOf(Frequency.class)) {
                if (n == count++)
                    return freq;
            }

            return Frequency.YEARLY;
        }
    }

    // BYDAY in RRULE
    enum Days {
        MO, TU, WE, TH, FR, SA, SU;

        // display Days
        public static void printDays() {
            int count = 1;
            for (Days freq : EnumSet.allOf(Days.class)) {
                System.out.println(count++ + " : " + freq);
            }
        }

        // get Days
        public static Days getDays(int n) {
            int count = 1;
            for (Days freq : EnumSet.allOf(Days.class)) {
                if (n == count++)
                    return freq;
            }

            return Days.MO;
        }
    }

    // Post constructor
    public PostEvents(EventsManagement eventsManagement) {
        this.eventsManagement = eventsManagement;
    }

    // main caller
    public void postEvents() throws IOException {
        while (true) {
            System.out.print("\033[H\033[2J");
            System.out.println("\n------------------------------------------");
            System.out.println("\t\tPost Events");
            System.out.println("------------------------------------------\n");

            Event eventParent = new Event();
            Event.TypeEvent event = eventParent.new TypeEvent();

            // set default values to create an event
            setDefaultValues(event);

            // add recurence rules?
            if (ValidIOHandlers.getYorN("\nIs a resucrsive event? [Y/n]: ")) {
                setReurrenceRules(event);
            }

            // set attendees
            if (ValidIOHandlers.getYorN("\nInvite attendees to event? [Y/n]: ")) {
                setAttendees(event);
            }

            // set remainders
            if (ValidIOHandlers.getYorN("\nSet remainders or use default? [Y/n]: ")) {
                setRemainder(event);
            }

            eventsManagement.postEvent(event);
            System.out.println("Event created!!");

            if (!ValidIOHandlers.getYorN("Continue adding events? [Y/n]: ")) {
                break;
            }

            // reset timeflag
            timeFlag = false;
        }
    }

//    /**
//     * Get time for start and end event
//     *
//     * @param ld LocalDate - YYYY-MM-DD
//     * @return a DateTime vale in RFC3339 format
//     * @throws IOException
//     */
//    private String setStartAndEndTime(String ld) throws IOException {
//        int hour = ValidIOHandlers.getChoice("Enter hour(HH)[0 - 23]: ");
//        int minute = ValidIOHandlers.getChoice("Enter minute(MM)[0 - 59]: ");
//
//        return EventsManagement.setDateTime(ld, hour + ":" + minute + ":00");
//    }

    /**
     * Default values for an event
     * Summary
     * Description
     * Start and End DateTime values
     *
     * @param event New event to set default values
     * @throws IOException
     */
    protected void setDefaultValues(TypeEvent event) throws IOException {
        // get summary
        String summary = ValidIOHandlers.getString("Event summary: ");

        // get description
        String description = ValidIOHandlers.getString("Enter Description: ");

        // Event starting time
        String start = ValidIOHandlers.getDate("Enter Start date [YYYY-MM-DD]: ", 1);
        Event.TypeEvent.Start sTime = event.new Start();
        if (ValidIOHandlers.getYorN("Add start and end time? [Y/n]: ")) {
            timeFlag = true;
            sTime.setDateTime(setDateAndTime(start));
        } else {
            sTime.setDate(EventsManagement.setDateTime(start, null));
        }
        sTime.setTimeZone(TimeZone.getDefault().getID());

        // Event starting time
        String end = ValidIOHandlers.getDate("Enter End date [YYYY-MM-DD]: ", 1);
        Event.TypeEvent.End eTime = event.new End();
        if (timeFlag) {
            eTime.setDateTime(setDateAndTime(end));
        } else {
            eTime.setDate(EventsManagement.setDateTime(end, null));
        }

        // set default values
        event.setSummary(summary);
        event.setDescription(description);
        event.setStart(sTime);
        event.setEnd(eTime);
    }

    /**
     * Set RDATE and EXDATE
     *
     * @param recurrence list of String to store recurrence rule
     * @param name       can be R / EX DATE
     * @throws IOException
     */
    private void setRdateEXdate(List<String> recurrence, String name) throws IOException {
        // a temporary value to check if no changes occured
        StringBuilder forComp = new StringBuilder(name + "DATE;VALUE=DATE:");
        StringBuilder DATE = new StringBuilder(name + "DATE;VALUE=DATE:");
        System.out.println("Enter 0 to exit...");
        while (true) {
            String recur = ValidIOHandlers.getDate("Enter date [YYYY-MM-DD / 0]: ", 0)
                    .replaceAll("-", "\u0000");

            if (recur.equals("0"))
                break;

            // everything in same format - Start, End, UNTIL, RDATE, EXDATE
            if (timeFlag) {
                recur = setDateAndTime(recur);
            }

            DATE.append(recur).append(",");
        }

        if (DATE.compareTo(forComp) != 0) {
            recurrence.add(DATE.deleteCharAt(DATE.length() - 1).toString());
        }
    }

    /**
     * Set time for dates - UNTIL, RDATE, EXDATE
     *
     * @param date - UNTIL, RDATE, EXDATE
     * @return a String with Date and Time combined
     * @throws IOException
     */
    private String setDateAndTime(String date) throws IOException {
        String hour = ValidIOHandlers.getMinHour("Enter hour(HH)[0 - 23]: ", false);
        String minute = ValidIOHandlers.getMinHour("Enter minute(MM)[0 - 59]: ", true);

        return date + "T" + (hour.length() == 2 ? hour : ("0" + hour))
                + (minute.length() == 2 ? minute : ("0" + minute));
    }

    /**
     * Get RRULE, RDATE, EXDATE
     *
     * @param event new event
     * @throws IOException
     */
    protected void setReurrenceRules(TypeEvent event) throws IOException {
        List<String> recurrence = new ArrayList<>();
        // a temporary value to check if no changes occured
        StringBuilder forComp = new StringBuilder("RRULE:");
        StringBuilder RRULE = new StringBuilder("RRULE:");

        // set frequency - DAILY, MONTHLY ...
        Frequency.printFrequency();
        int freqn = ValidIOHandlers.getChoice("Enter Frequency value(default = YEARLY) [1 - 7]: ");
        Frequency freq = Frequency.getFrequency(freqn);
        RRULE.append("FREQ=").append(freq);

        // set interval - 1, 2, 3 ...
        if (ValidIOHandlers.getYorN("Set Interval? [Y/n]: ")) {
            int interval = ValidIOHandlers.getChoice("Enter Interval [Number]: ");
            RRULE.append(";").append("INTERVAL=").append(interval);
        }

        // set either count or until - 1, 2, 3 ...
        if (ValidIOHandlers.getYorN("Set Count? [Y/n]: ")) {
            int count = ValidIOHandlers.getChoice("Enter Count [Number]: ");
            RRULE.append(";").append("COUNT=").append(count);
        } else {
            // set until
            if (ValidIOHandlers.getYorN("Set Until by day? [Y/n]: ")) {
                String until = ValidIOHandlers.getDate("Enter Until [YYYY-MM-DD]: ", 1)
                        .replaceAll("-", "\u0000");

                // everything in same format - Start, End, UNTIL, RDATE, EXDATE
                if (timeFlag) {
                    until = setDateAndTime(until);
                }

                RRULE.append(";").append("UNTIL=").append(until);
            }
        }

        // set day?
        if (ValidIOHandlers.getYorN("Set by week days? [Y/n]: ")) {
            Days.printDays();
            int dayn = ValidIOHandlers.getChoice("Enter Days value(default = MO) [Number]: ");
            Days day = Days.getDays(dayn);
            RRULE.append(";").append("BYDAY=").append(day);
        }

        // set RRULE
        if (RRULE.compareTo(forComp) != 0) {
            recurrence.add(RRULE.toString());
        }

        // set RDATE
        if (ValidIOHandlers.getYorN("\nSet Recurring Dates? [Y/n]: ")) {
            setRdateEXdate(recurrence, "R");
        }

        // set RDATE
        if (ValidIOHandlers.getYorN("\nSet Exception Dates? [Y/n]: ")) {
            setRdateEXdate(recurrence, "EX");
        }

        event.setRecurrence(recurrence);
    }

    /**
     * Invite attendees for event
     *
     * @param event new event
     */
    protected void setAttendees(TypeEvent event) {
        List<TypeEvent.Attendees> attendees = new ArrayList<>();

        System.out.println("Enter 0 to exit...");
        while (true) {
            String email = ValidIOHandlers.getString("Enter an email to invite: ");
            if (email.equals("0"))
                break;

            // check for valid email
            String regex = "^(.+)@(.+)$";
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(email);

            if (!matcher.matches()) {
                System.out.println("Enter a valid email!");
                continue;
            }

            TypeEvent.Attendees attendee = event.new Attendees();
            attendee.setEmail(email);
            attendees.add(attendee);
        }

        // if an attendee is added
        if (attendees.size() > 0) {
            event.setAttendees(attendees);
        }
    }

    private void setRemainderEvents(TypeEvent.Remainders remainders, List<TypeEvent.Remainders.Overrides> overrides, String method) {
        System.out.println("Enter remainder time: \n");

        if (ValidIOHandlers.getYorN("In Hours? [Y/n]: ")) {
            System.out.print("Enter Hour before actual event: ");
            int hour = Integer.parseInt(System.console().readLine());
            TypeEvent.Remainders.Overrides override = remainders.new Overrides();
            override.setMethod(method);
            override.setMinutes(hour * 60L);
            overrides.add(override);
        } else {
            System.out.print("Enter Minutes before actual event: ");
            int minutes = Integer.parseInt(System.console().readLine());
            TypeEvent.Remainders.Overrides override = remainders.new Overrides();
            override.setMethod(method);
            override.setMinutes(minutes);
            overrides.add(override);
        }
    }

    /**
     * Set email and popu remainder
     *
     * @param event new event
     */
    protected void setRemainder(TypeEvent event) {
        TypeEvent.Remainders remainders = event.new Remainders();
        List<TypeEvent.Remainders.Overrides> overrides = new ArrayList<>();

        // Email remainder
        if (ValidIOHandlers.getYorN("Set Email remainder? [Y/n]: ")) {
            setRemainderEvents(remainders, overrides, "email");
        }

        // Popup remainder
        if (ValidIOHandlers.getYorN("Set Popup remainder? [Y/n]: ")) {
            setRemainderEvents(remainders, overrides, "popup");
        }

        event.setRemainders(remainders);
    }
}
