package calendar;

import org.json.JSONArray;
import org.json.JSONObject;

import javax.xml.crypto.Data;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class Event {

    private static final String RESPONSE_BODY_FILE = new File("bin/main/calendar/responseBody.json").getAbsolutePath();

    public void storeResponseBody(String responseBody){
        try {
            Path path = Path.of(RESPONSE_BODY_FILE);

            Files.writeString(path, responseBody);
        }catch (IOException e){
            e.printStackTrace();
        }

    }

    public List<TypeEvent> getListOfEvents(){
        List<TypeEvent> typeEvents = new ArrayList<>();
        try{
            if(!(new File(RESPONSE_BODY_FILE).exists())) return null;
            JSONObject responseBody = new JSONObject(Files.readString(Path.of(RESPONSE_BODY_FILE)));
            if(responseBody.isNull("items")) return null;

            JSONArray listOfEventsJson = responseBody
                    .getJSONArray("items");

            for(int i = 0; i < listOfEventsJson.length(); i++){
                typeEvents.add(setupEventObject(listOfEventsJson.getJSONObject(i)));
            }
        }catch (IOException e){
            e.printStackTrace();
        }

        return typeEvents;
    }

    public TypeEvent setupEventObject(JSONObject eventObject){
        TypeEvent event = new TypeEvent();

        if (!eventObject.isNull("id")){
            event.id = eventObject.getString("id");
        }

        if (!eventObject.isNull("status")){
            event.status = eventObject.getString("status");
        }

        if (!eventObject.isNull("summary")){
            event.summary = eventObject.getString("summary");
        }

        if (!eventObject.isNull("description")){
            event.description = eventObject.getString("description");
        }

        if (!eventObject.isNull("start")){
            TypeEvent.Start start = event.new Start();

            JSONObject startObject = eventObject.getJSONObject("start");
            if (!startObject.isNull("date")){
                start.date = startObject.getString("date");
            }
            if (!startObject.isNull("dateTime")){
                start.dateTime = startObject.getString("dateTime");
            }
            if (!startObject.isNull("timeZone")){
                start.timeZone = startObject.getString("timeZone");
            }

            event.start = start;
        }

        if (!eventObject.isNull("end")){
            TypeEvent.End end = event.new End();
            JSONObject endObject = eventObject.getJSONObject("end");
            if (!endObject.isNull("date")){
                end.date = endObject.getString("date");
            }
            if (!endObject.isNull("dateTime")){
                end.dateTime = endObject.getString("dateTime");
            }
            if (!endObject.isNull("timeZone")){
                end.timeZone = endObject.getString("timeZone");
            }

            event.end = end;
        }

        if(!eventObject.isNull("recurrence")){
            JSONArray recurrenceList = eventObject.getJSONArray("recurrence");

            for(int i = 0; i < recurrenceList.length(); i++){
                event.recurrence.add((String) recurrenceList.get(i));
            }
        }

        if(!eventObject.isNull("attendees")){
            JSONArray attendeesList = eventObject.getJSONArray("attendees");

            for(int i = 0; i < attendeesList.length(); i++){
                TypeEvent.Attendees attendee = event.new Attendees();
                JSONObject attendeeObject = (JSONObject) attendeesList.get(0);

                if(!attendeeObject.isNull("id")){
                    attendee.id = attendeeObject.getString("id");
                }
                if(!attendeeObject.isNull("email")){
                    attendee.email = attendeeObject.getString("email");
                }

                event.attendees.add(attendee);
            }
        }

        if(!eventObject.isNull("remainders")){
            TypeEvent.Remainders remainders = event.new Remainders();
            JSONObject remaindersObject = eventObject.getJSONObject("remainders");

            if(!remaindersObject.isNull("useDefault")){
                remainders.useDefault = remaindersObject.getBoolean("useDefault");
            }

            if(!remaindersObject.isNull("overrides")){
                JSONArray overridesList = remaindersObject.getJSONArray("overrides");

                for(int i = 0; i < overridesList.length(); i++){
                    JSONObject overridesObject = (JSONObject) overridesList.get(i);
                    TypeEvent.Remainders.Overrides override = remainders.new Overrides();

                    if(!overridesObject.isNull("method")){
                        override.method = overridesObject.getString("method");
                    }
                    if(!overridesObject.isNull("minutes")){
                        override.minutes = (long) overridesObject.get("minutes");
                    }

                    remainders.overrides.add(override);
                }
            }

            event.remainders = remainders;
        }

        if(!eventObject.isNull("eventType")){
            event.eventType = eventObject.getString("eventType");
        }

        return event;
    }

    public JSONObject setupResponseBody(TypeEvent event){
        JSONObject eventObject = new JSONObject();

        if(event.getId() != null){
            eventObject.put("id", event.getId());
        }

        if(event.getStatus() != null){
            eventObject.put("status", event.getStatus());
        }

        if(event.getSummary() != null){
            eventObject.put("summary", event.getSummary());
        }

        if(event.getDescription() != null){
            eventObject.put("description", event.getDescription());
        }

        if(event.getStart() != null){
            JSONObject startObject = new JSONObject();

            if(event.getStart().getDate() != null){
                startObject.put("date", event.getStart().getDate());
            }

            if(event.getStart().getDateTime() != null){
                startObject.put("dateTime", event.getStart().getDateTime());
            }

            if(event.getStart().getTimeZone() != null){
                startObject.put("timeZone", event.getStart().getTimeZone());
            }
        }

        if(event.getEnd() != null){
            JSONObject endObject = new JSONObject();

            if(event.getEnd().getDate() != null){
                endObject.put("date", event.getEnd().getDate());
            }

            if(event.getStart().getDateTime() != null){
                endObject.put("dateTime", event.getEnd().getDateTime());
            }

            if(event.getStart().getTimeZone() != null){
                endObject.put("timeZone", event.getEnd().getTimeZone());
            }
        }

        return eventObject;

    }

    public class TypeEvent{
        public void setId(String id) {
            this.id = id;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public void setSummary(String summary) {
            this.summary = summary;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public void setStart(Start start) {
            this.start = start;
        }

        public void setEnd(End end) {
            this.end = end;
        }

        public void setRecurrence(List<String> recurrence) {
            this.recurrence = recurrence;
        }

        public void setAttendees(List<Attendees> attendees) {
            this.attendees = attendees;
        }

        public void setRemainders(Remainders remainders) {
            this.remainders = remainders;
        }

        public void setEventType(String eventType) {
            this.eventType = eventType;
        }

        public String getId() {
            return id;
        }

        public String getStatus() {
            return status;
        }

        public String getSummary() {
            return summary;
        }

        public String getDescription() {
            return description;
        }

        public Start getStart() {
            return start;
        }

        public End getEnd() {
            return end;
        }

        public List<String> getRecurrence() {
            return recurrence;
        }

        public List<Attendees> getAttendees() {
            return attendees;
        }

        public Remainders getRemainders() {
            return remainders;
        }

        public String getEventType() {
            return eventType;
        }

        String id = null;
        String status = null;
        String summary = null;
        String description = null;

        public class Start{
            public String getDate() {
                return date;
            }

            public void setDate(String date) {
                this.date = date;
            }

            public void setDateTime(String dateTime) {
                this.dateTime = dateTime;
            }

            public void setTimeZone(String timeZone) {
                this.timeZone = timeZone;
            }

            public String getDateTime() {
                return dateTime;
            }

            public String getTimeZone() {
                return timeZone;
            }

            String date = null;
            String dateTime = null;
            String timeZone = null;
        }
        Start start = null;

        public class End{
            public String getDate() {
                return date;
            }

            public void setDate(String date) {
                this.date = date;
            }

            public void setDateTime(String dateTime) {
                this.dateTime = dateTime;
            }

            public void setTimeZone(String timeZone) {
                this.timeZone = timeZone;
            }

            public String getDateTime() {
                return dateTime;
            }

            public String getTimeZone() {
                return timeZone;
            }

            String date = null;
            String dateTime = null;
            String timeZone = null;
        }
        End end = null;

        List<String> recurrence = new ArrayList<>();

        public class Attendees{
            public String getId() {
                return id;
            }

            public void setId(String id) {
                this.id = id;
            }

            public void setEmail(String email) {
                this.email = email;
            }

            public String getEmail() {
                return email;
            }

            String id = null;
            String email = null;
        }
        List<Attendees> attendees = new ArrayList<>();

        public class Remainders{
            public boolean isUseDefault() {
                return useDefault;
            }

            public void setUseDefault(boolean useDefault) {
                this.useDefault = useDefault;
            }

            public void setOverrides(List<Overrides> overrides) {
                this.overrides = overrides;
            }

            public List<Overrides> getOverrides() {
                return overrides;
            }

            boolean useDefault = true;

            public class Overrides{
                public String getMethod() {
                    return method;
                }

                public void setMethod(String method) {
                    this.method = method;
                }

                public void setMinutes(long minutes) {
                    this.minutes = minutes;
                }

                public long getMinutes() {
                    return minutes;
                }

                String method = null;
                long minutes = 0;
            }

            List<Overrides> overrides = new ArrayList<>();
        }
        Remainders remainders = null;

        String eventType = "default";

    }





}
