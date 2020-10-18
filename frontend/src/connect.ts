import { RSocketClient,
  JsonSerializer,
  IdentitySerializer, 
  encodeAndAddCustomMetadata,
  BufferEncoders,
  MESSAGE_RSOCKET_COMPOSITE_METADATA} from "rsocket-core";
import { Encodable } from 'rsocket-types';
import RSocketWebSocketClient from "rsocket-websocket-client";
import { EventLog } from "./eventLog";

let clientId = Math.floor((Math.random() * 10000) + 1);
let keepAlive = 60000;
let lifetime = 70000;
let client: RSocketClient<any, Encodable>;

const eventLog = new EventLog();

function createClient() {
  let tokenJWT = (document.getElementById("tokenJWT") as HTMLInputElement).value;

  client = new RSocketClient({
    // serializers: {
    //   data: JsonSerializer,
    //   metadata: IdentitySerializer
    // },
    setup: {
      payload: {
        // data: "clientId-" + clientId,
        // metadata: String.fromCharCode("setup".length) + "setup",
        metadata: encodeAndAddCustomMetadata(
          Buffer.alloc(0),
          "message/x.rsocket.authentication.bearer.v0",
          Buffer.from(tokenJWT)
        )
      },
      keepAlive: keepAlive,
      lifetime: lifetime,
      dataMimeType: 'application/json',
      // metadataMimeType: 'message/x.rsocket.routing.v0',
      metadataMimeType: MESSAGE_RSOCKET_COMPOSITE_METADATA.string,
    },
    transport: new RSocketWebSocketClient({url: 'ws://localhost:7000'}, BufferEncoders),
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

      socket.connectionStatus().subscribe(connectionStatus => {
        if (connectionStatus.kind == 'ERROR') {
          eventLog.add("connection status: status " + connectionStatus.kind + " error: " + connectionStatus.error);
        } else {
          eventLog.add("connection status: status " + connectionStatus.kind);
        }
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


document.getElementById("connect").addEventListener('click', (e:Event) => connect());
document.getElementById("disconnect").addEventListener('click', (e:Event) => disconnect());