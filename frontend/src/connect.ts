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
let tryToConnect = true;
let client: RSocketClient<any, Encodable>;

const eventLog = new EventLog();

function createClient() {
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
          Buffer.from('eyJhbGciOiJSUzI1NiJ9.eyJleHAiOjE2MDMzNzk2NjEsInN1YiI6ImV4YW1wbGUifQ.lDBznz1-GdrLYzEWfY1iFoOwerRfKV5AV8vkWtyLHjEJkoSeac6MywDP4zVM94GaAPhzLzIgcxT18DeFyLgCJSudbAiXciWmcE59jqTt2KoLt8FURoNDpDIT53WHVt3AvsbECut0hzTJxXPlxaxMF97mPN0ARA3lmkBkmajhfcIRf3RIdH5zQJIGNv8G4OpxkSRQGDBEaBck-wkkb0-YJwk0IU8G8Y-McwgvGNtuK43b9-vGg0SjZ_wwO--XDDAopouaMreYp2JSYI2iRksNmEpGCrK8JuBLPm_bzZ6KfzD275FjA8peFmU-61qa-lysUzRh6ybs7zeylAwnSzhM4Q')
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
  tryToConnect = true;
  tryConnect();
}

function disconnect() {
  eventLog.add("disconnect: click");
  tryToConnect = false;
  if (client != null) {
    client.close();
  }
}

function tryConnect() {
  if (!tryToConnect) {
    eventLog.add("disconnect");
    return;
  }

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

        if (connectionStatus.kind == 'CLOSED' || connectionStatus.kind == 'ERROR') {
          tryConnectTimeout();
        }
      });

    },
    onError: error => {
      eventLog.add("connection: error " + error);
      tryConnectTimeout();
    },
    onSubscribe: cancel => {
      eventLog.add("connection: on subscribe");
    }
  });
}

function tryConnectTimeout() {
  setTimeout(() => { 
    tryConnect(); 
  }, 1000);
}

document.getElementById("connect").addEventListener('click', (e:Event) => connect());
document.getElementById("disconnect").addEventListener('click', (e:Event) => disconnect());