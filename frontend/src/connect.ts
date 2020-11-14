import { RSocketClient,
  JsonSerializer,
  IdentitySerializer, 
  encodeAndAddCustomMetadata,
  BufferEncoders,
  MESSAGE_RSOCKET_COMPOSITE_METADATA,
  encodeAndAddWellKnownMetadata,
  MESSAGE_RSOCKET_ROUTING} from "rsocket-core";
import { Flowable } from 'rsocket-flowable';
import { Encodable, ReactiveSocket, Payload } from 'rsocket-types';
import RSocketWebSocketClient from "rsocket-websocket-client";
import { EventLog } from "./eventLog";

let clientId = Math.floor((Math.random() * 10000) + 1);
let keepAlive = 60000;
let lifetime = 70000;

let client: RSocketClient<any, Encodable>;
let activeSocket: ReactiveSocket<any, Encodable>;

const eventLog = new EventLog();

function getDataMimeType() : string {
  let isCheckedDataMimeTypeText = (document.getElementById("dataMimeTypeText") as HTMLInputElement).checked;
  if (isCheckedDataMimeTypeText) {
    return 'text/plain';
  }
  return 'application/json';
}

function createClient() {
  let tokenJWT = (document.getElementById("tokenJWT") as HTMLInputElement).value;
  let host = (document.getElementById("host") as HTMLInputElement).value;
  let route = (document.getElementById("routeSetup") as HTMLInputElement).value;
  let message = (document.getElementById("messageSetup") as HTMLInputElement).value;
  let dataMimeType = getDataMimeType();

  client = new RSocketClient({
    setup: {
      payload: {
        data: Buffer.from(message),
        metadata: encodeAndAddWellKnownMetadata(
          encodeAndAddCustomMetadata(
            Buffer.alloc(0),
            "message/x.rsocket.authentication.bearer.v0",
            Buffer.from(tokenJWT),
          ),
          MESSAGE_RSOCKET_ROUTING,
          Buffer.from(String.fromCharCode(route.length) + route),
        )
      },
      keepAlive: keepAlive,
      lifetime: lifetime,
      dataMimeType: dataMimeType,
      metadataMimeType: MESSAGE_RSOCKET_COMPOSITE_METADATA.string,
    },
    transport: new RSocketWebSocketClient({url: host}, BufferEncoders),
    errorHandler: (error: Error) => eventLog.add("rsocket client error: " + error.message)
  });
}

function connect() {
  eventLog.add("connection: click");
  tryConnect();
}

function disconnect() {
  eventLog.add("disconnect: click");
  client.close();
}

function tryConnect() {
  createClient();

  client.connect().subscribe({
    onComplete: socket => {
      eventLog.add("connection: on complete");
      activeSocket = socket;

      socket.connectionStatus().subscribe(connectionStatus => {
        if (connectionStatus.kind == 'ERROR') {
          eventLog.add("connection status: status " + connectionStatus.kind + " error: " + connectionStatus.error);
        } else {
          eventLog.add("connection status: status " + connectionStatus.kind);
        }

        manageButtons(connectionStatus);
      });

    },
    onError: error => {
      eventLog.add("connection: error " + error);
    },
    onSubscribe: cancel => {
      eventLog.add("connection: on subscribe");
    }
  });
}

function sendFireAndForget() {
  let message = (document.getElementById("messageFireAndForget") as HTMLInputElement).value;
  let route = (document.getElementById("routeFireAndForget") as HTMLInputElement).value;

  activeSocket.fireAndForget({
    data: Buffer.from(message),
    metadata: encodeAndAddWellKnownMetadata(
      Buffer.alloc(0),
      MESSAGE_RSOCKET_ROUTING,
      Buffer.from(String.fromCharCode(route.length) + route),
    )
  });
}

function sendRequestResponse() {
  let message = (document.getElementById("messageRequestResponse") as HTMLInputElement).value;
  let route = (document.getElementById("routeRequestResponse") as HTMLInputElement).value;

  activeSocket.requestResponse({
    data: Buffer.from(message),
    metadata: encodeAndAddWellKnownMetadata(
      Buffer.alloc(0),
      MESSAGE_RSOCKET_ROUTING,
      Buffer.from(String.fromCharCode(route.length) + route),
    )
  }).subscribe({
    onComplete: payload => {
      eventLog.add("request: on complete data: " + payload.data + ", metadata: " + payload.metadata);
    },
    onError: error => {
      eventLog.add("request: error " + error);
    },
    onSubscribe: cancel => {
      eventLog.add("request: on subscribe");
    },
  });
}

function sendRequestStream() {
  let message = (document.getElementById("messageRequestStream") as HTMLInputElement).value;
  let route = (document.getElementById("routeRequestStream") as HTMLInputElement).value;

  activeSocket.requestStream({
    data: Buffer.from(message),
    metadata: encodeAndAddWellKnownMetadata(
      Buffer.alloc(0),
      MESSAGE_RSOCKET_ROUTING,
      Buffer.from(String.fromCharCode(route.length) + route),
    )
  }).subscribe({
    onComplete: () => {
      eventLog.add("request: on complete");
    },
    onError: error => {
      eventLog.add("request: error " + error);
    },
    onNext: payload => {
      eventLog.add("request: on next data: " + payload.data + ", metadata: " + payload.metadata);
    },
    onSubscribe: subscription => {
      subscription.request(1000);
      eventLog.add("request: on subscribe");
    },
  });
}

function sendRequestChannel() {
  let messages = (document.getElementById("messageRequestChannel") as HTMLInputElement).value;
  let route = (document.getElementById("routeRequestChannel") as HTMLInputElement).value;

  let payload = messages.split(",")
    .map(message => <Payload<Buffer, Encodable>> {
        data: Buffer.from(message),
        metadata: encodeAndAddWellKnownMetadata(
          Buffer.alloc(0),
          MESSAGE_RSOCKET_ROUTING,
          Buffer.from(String.fromCharCode(route.length) + route),
        )
    });

  activeSocket.requestChannel(
    Flowable.just(... payload)
    ).subscribe({
    onComplete: () => {
      eventLog.add("request: on complete");
    },
    onError: error => {
      eventLog.add("request: error " + error);
    },
    onNext: payload => {
      eventLog.add("request: on next data: " + payload.data + ", metadata: " + payload.metadata);
    },
    onSubscribe: subscription => {
      subscription.request(1000);
      eventLog.add("request: on subscribe");
    },
  });
}

function manageButtons(connectionStatus) {
  if (connectionStatus.kind == 'CONNECTED') {
    (document.getElementById("connect") as HTMLInputElement).disabled = true;
    (document.getElementById("disconnect") as HTMLInputElement).disabled = false;
    (document.getElementById("sendFireAndForget") as HTMLInputElement).disabled = false;
    (document.getElementById("sendRequestResponse") as HTMLInputElement).disabled = false;
    (document.getElementById("sendRequestStream") as HTMLInputElement).disabled = false;
    (document.getElementById("sendRequestChannel") as HTMLInputElement).disabled = false;
  } else {
    (document.getElementById("connect") as HTMLInputElement).disabled = false;
    (document.getElementById("disconnect") as HTMLInputElement).disabled = true;
    (document.getElementById("sendFireAndForget") as HTMLInputElement).disabled = true;
    (document.getElementById("sendRequestResponse") as HTMLInputElement).disabled = true;
    (document.getElementById("sendRequestStream") as HTMLInputElement).disabled = true;
    (document.getElementById("sendRequestChannel") as HTMLInputElement).disabled = true;
  }
}

function clearEventLog() {
  (document.getElementById("eventLog") as HTMLInputElement).innerHTML = "";
}

document.getElementById("connect").addEventListener('click', (e:Event) => connect());
document.getElementById("disconnect").addEventListener('click', (e:Event) => disconnect());
document.getElementById("sendFireAndForget").addEventListener('click', (e:Event) => sendFireAndForget());
document.getElementById("sendRequestResponse").addEventListener('click', (e:Event) => sendRequestResponse());
document.getElementById("sendRequestStream").addEventListener('click', (e:Event) => sendRequestStream());
document.getElementById("sendRequestChannel").addEventListener('click', (e:Event) => sendRequestChannel());
document.getElementById("clearEventLog").addEventListener('click', (e:Event) => clearEventLog());