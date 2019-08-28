import EventBus from 'vertx3-eventbus-client';
import { useEffect } from 'react';

let eb = new EventBus("/api/v1/eventbus")
eb.enableReconnect(true);

let callbacks = [];
let espCallbacks = [];


eb.onopen = function () {
    console.log("Connect eventbus");
    registerEvent("mesh.node.updated", callbacks);
    registerEvent("mesh.node.created", callbacks);
    registerEvent("mesh.node.deleted", callbacks);
    registerEvent("custom.event", espCallbacks);
}

function registerEvent(eventName, callbacks) {
    eb.registerHandler(eventName, function (error, message) {
        callbacks.forEach(cb => cb());
        console.log('Received a message: ' + JSON.stringify(message));
    });
}

export function useWebsocketESPBridge(callback) {
    useEffect(() => {
        espCallbacks.push(callback);
        console.log("Mount ESP");
        return () => {
            var index = espCallbacks.indexOf(callback);
            if (index > -1) {
                espCallbacks.splice(index, 1);
            }
            console.log("Unmount ESP");
        }
    }, []);
}

export default function useWebsocketBridge(callback) {
    useEffect(() => {
        callbacks.push(callback);
        console.log("Mount");
        return () => {
            var index = callbacks.indexOf(callback);
            if (index > -1) {
                callbacks.splice(index, 1);
            }
            console.log("Unmount");
        }
    }, []);
}