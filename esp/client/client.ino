#include <Arduino.h>
#include <ESP8266WiFi.h>
#include <ESP8266WiFiMulti.h>
#include <WebSocketsClient.h>
#include <Hash.h>

ESP8266WiFiMulti WiFiMulti;
WebSocketsClient webSocket;

#define USE_SERIAL Serial1

const char* ssid     = "Demo";
const char* password = "12345678";
const byte interruptPin = 13;
volatile byte interruptCounter = 0;
int numberOfInterrupts = 0;

void setup() {
  Serial.begin(115200);
  WiFi.mode(WIFI_STA);
  WiFi.begin(ssid, password);

  Serial.print("Connecting");
  while (WiFi.status() != WL_CONNECTED) {
    delay(500);
    Serial.print(".");
  }

  Serial.println("");
  Serial.println("WiFi connected");
  Serial.println("IP address: ");
  Serial.println(WiFi.localIP());

	webSocket.begin("138.201.65.159", 8080, "/api/v1/eventbus/websocket");
	webSocket.setReconnectInterval(2000);

  pinMode(interruptPin, INPUT_PULLUP);
  attachInterrupt(digitalPinToInterrupt(interruptPin), handleInterrupt, RISING);
}

void handleInterrupt() {
  interruptCounter++;
}

void loop() {
	webSocket.loop();
  if (interruptCounter > 0) {
    Serial.println("Detected interrupt");
    webSocket.sendTXT("{\"type\":\"publish\",\"address\":\"custom.event\",\"body\":\"someText\"}");
    interruptCounter = 0;
  }
  delay(100);
  webSocket.sendTXT("{\"type\":\"ping\"}");
  String p = "Ping";
  delay(100);
  webSocket.sendPing(p);
}
