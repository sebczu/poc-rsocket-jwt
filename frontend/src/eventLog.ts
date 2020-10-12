export class EventLog {

    public add(log: string) {
        var eventLog = document.getElementById("eventLog");
        var eventLogLi = document.createElement("li");
        eventLogLi.appendChild(document.createTextNode(log));
        eventLog.appendChild(eventLogLi);
    }

}

