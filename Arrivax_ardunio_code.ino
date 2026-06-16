#include <Arduino.h>
#include <WiFi.h>
#include <HTTPClient.h>
#include <WiFiClient.h>
#include <Firebase_ESP_Client.h>
#include "time.h"         // Server time
#include "addons/TokenHelper.h"  // For Firebase token generation
#include "addons/RTDBHelper.h"   // Helps read/write Firebase data
#include <Wire.h>
#include <LiquidCrystal_I2C.h>

// ----------- CONFIGURATION -----------
#define WIFI_SSID "M iPhone"
#define WIFI_PASSWORD "mOLa@925"
#define API_KEY "AIzaSyAYUkpiDmBFChPo956zSoq9mpj1Qiumozc"
#define USER_EMAIL "user@gmail.com"  
#define USER_PASSWORD "User@123"
#define DATABASE_URL "https://arrivax-a7884-default-rtdb.firebaseio.com"

// ----------- FIREBASE OBJECTS -----------
FirebaseData fbdo;
FirebaseAuth auth;
FirebaseConfig config;
FirebaseJson json;

String uid;
String History = "/history";
String DB_history;

// ----------- TIME & TIMERS -----------
const char* ntpServer = "pool.ntp.org"; 
struct tm timeinfo;
String timestamp;

unsigned long lastTime = 0;
unsigned long timerDelay = 3000;    // Update live feed every 3 seconds

unsigned long lastTime2 = 0;
unsigned long timerDelay2 = 60000;  // Update history every 60 seconds

unsigned long lastTime3 = 0;
unsigned long timerDelay3 = 5000;   // Fetch delay info from Firebase every 5 seconds

// ----------- HARDWARE INIT -----------
// I2C LCDs (Address, Columns, Rows)
LiquidCrystal_I2C lcd1(0x23, 16, 2);
LiquidCrystal_I2C lcd2(0x24, 16, 2);
LiquidCrystal_I2C lcd3(0x25, 16, 2);
LiquidCrystal_I2C lcd4(0x26, 16, 2);

// ----------- PIN DEFINITIONS -----------

// Slot 1
#define TRIG1 32
#define ECHO1 33

// Slot 2
#define TRIG2 25
#define ECHO2 26

// Slot 3
#define TRIG3 27
#define ECHO3 14

// Slot 4
#define TRIG4 18
#define ECHO4 19

// ----------- SENSOR VARIABLES -----------
const float OCCUPIED_DISTANCE = 10.0;

// Current Distances
float slot1_distance = 0;
float slot2_distance = 0;
float slot3_distance = 0;
float slot4_distance = 0;

// Current Statuses
String slot1_status = "FREE";
String slot2_status = "FREE";
String slot3_status = "FREE";
String slot4_status = "FREE";

// Previous Statuses (to detect changes)
String prev_slot1_status = "FREE";
String prev_slot2_status = "FREE";
String prev_slot3_status = "FREE";
String prev_slot4_status = "FREE";

// Timestamps for status changes
String slot1_time = "1773576444000";
String slot2_time = "1773576444000";
String slot3_time = "1773576444000";
String slot4_time = "1773576444000";

// Delay Statuses from Firebase
int slot1_delay = 0;
int slot2_delay = 0;
int slot3_delay = 0;
int slot4_delay = 0;

// ----------- SETUP -----------
void setup() {
  Serial.begin(115200);

  // Initialize Ultrasonic Pins
  pinMode(TRIG1, OUTPUT); pinMode(ECHO1, INPUT);
  pinMode(TRIG2, OUTPUT); pinMode(ECHO2, INPUT);
  pinMode(TRIG3, OUTPUT); pinMode(ECHO3, INPUT);
  pinMode(TRIG4, OUTPUT); pinMode(ECHO4, INPUT);

  // Initialize I2C and LCDs
  Wire.begin(21, 22);
  lcd1.begin(); lcd1.backlight();
  lcd2.begin(); lcd2.backlight();
  lcd3.begin(); lcd3.backlight();
  lcd4.begin(); lcd4.backlight();

  // Connect to WiFi and Time Server
  initWiFi();
  configTime(19800, 0, ntpServer); // 19800 seconds = +5:30 offset

  // Configure Firebase
  config.api_key = API_KEY;
  auth.user.email = USER_EMAIL;
  auth.user.password = USER_PASSWORD;
  config.database_url = DATABASE_URL;

  Firebase.reconnectWiFi(true);
  fbdo.setResponseSize(4096);
  config.token_status_callback = tokenStatusCallback; 
  config.max_token_generation_retry = 5;

  Firebase.begin(&config, &auth);

  Serial.println("Getting User UID");
  while ((auth.token.uid) == "") {
    Serial.print('.');
    delay(1000);
  }
  uid = auth.token.uid.c_str();
  Serial.print("\nUser UID: ");
  Serial.println(uid);
}

// ----------- MAIN LOOP -----------
void loop() {
  // Get current timestamp in milliseconds
  timestamp = getTime();
  timestamp = timestamp + "000";

  // 1. Timer: Send Live Feed to Firebase
  if ((millis() - lastTime) > timerDelay) {
    liveFeed();
    lastTime = millis();
  }
  
  // 2. Timer: Log History to Firebase
  if ((millis() - lastTime2) > timerDelay2) {
    historyData();
    lastTime2 = millis();
  }

  // 3. Timer: Fetch Delay info from Firebase
  if ((millis() - lastTime3) > timerDelay3) {
    getDelayInfo();
    lastTime3 = millis();
  }

  // Read all ultrasonic sensors
  readSlot1();
  readSlot2();
  readSlot3();
  readSlot4();

  // Update LCD panels
  displaySlots();

  // Print diagnostics to Serial Monitor
  Serial.println("----- BUS SLOT STATUS -----");
  Serial.printf("Slot 1 | Dist: %.2f cm | Stat: %s | Time: %s | Delay: %d\n", slot1_distance, slot1_status.c_str(), slot1_time.c_str(), slot1_delay);
  Serial.printf("Slot 2 | Dist: %.2f cm | Stat: %s | Time: %s | Delay: %d\n", slot2_distance, slot2_status.c_str(), slot2_time.c_str(), slot2_delay);
  Serial.printf("Slot 3 | Dist: %.2f cm | Stat: %s | Time: %s | Delay: %d\n", slot3_distance, slot3_status.c_str(), slot3_time.c_str(), slot3_delay);
  Serial.printf("Slot 4 | Dist: %.2f cm | Stat: %s | Time: %s | Delay: %d\n", slot4_distance, slot4_status.c_str(), slot4_time.c_str(), slot4_delay);
  Serial.println("-----------------------------");
}

// ----------- HELPER FUNCTIONS -----------

void initWiFi() {
  WiFi.begin(WIFI_SSID, WIFI_PASSWORD);
  Serial.print("Connecting to WiFi ..");
  while (WiFi.status() != WL_CONNECTED) {
    Serial.print('.');
    delay(1000);
  }
  Serial.println();
  Serial.print("Connected! IP: ");
  Serial.println(WiFi.localIP());
}

unsigned long getTime() {
  time_t now;
  if (!getLocalTime(&timeinfo)) {
    return (0);
  }
  time(&now);
  return now;
}

// ----------- SENSOR READING LOGIC -----------
// Reads distance, updates status, and logs time if status changes.
void readSlot1() {
  digitalWrite(TRIG1, LOW); delayMicroseconds(2);
  digitalWrite(TRIG1, HIGH); delayMicroseconds(10);
  digitalWrite(TRIG1, LOW);
  long duration = pulseIn(ECHO1, HIGH);
  slot1_distance = duration * 0.034 / 2;

  slot1_status = (slot1_distance < OCCUPIED_DISTANCE) ? "OCCUPIED" : "FREE";

  if (slot1_status != prev_slot1_status) {
    slot1_time = timestamp;
    prev_slot1_status = slot1_status;
    lcd1.clear(); // Clear LCD only on status change to prevent flickering
  }
}

void readSlot2() {
  digitalWrite(TRIG2, LOW); delayMicroseconds(2);
  digitalWrite(TRIG2, HIGH); delayMicroseconds(10);
  digitalWrite(TRIG2, LOW);
  long duration = pulseIn(ECHO2, HIGH);
  slot2_distance = duration * 0.034 / 2;

  slot2_status = (slot2_distance < OCCUPIED_DISTANCE) ? "OCCUPIED" : "FREE";

  if (slot2_status != prev_slot2_status) {
    slot2_time = timestamp;
    prev_slot2_status = slot2_status;
    lcd2.clear();
  }
}

void readSlot3() {
  digitalWrite(TRIG3, LOW); delayMicroseconds(2);
  digitalWrite(TRIG3, HIGH); delayMicroseconds(10);
  digitalWrite(TRIG3, LOW);
  long duration = pulseIn(ECHO3, HIGH);
  slot3_distance = duration * 0.034 / 2;

  slot3_status = (slot3_distance < OCCUPIED_DISTANCE) ? "OCCUPIED" : "FREE";

  if (slot3_status != prev_slot3_status) {
    slot3_time = timestamp;
    prev_slot3_status = slot3_status;
    lcd3.clear();
  }
}

void readSlot4() {
  digitalWrite(TRIG4, LOW); delayMicroseconds(2);
  digitalWrite(TRIG4, HIGH); delayMicroseconds(10);
  digitalWrite(TRIG4, LOW);
  long duration = pulseIn(ECHO4, HIGH);
  slot4_distance = duration * 0.034 / 2;

  slot4_status = (slot4_distance < OCCUPIED_DISTANCE) ? "OCCUPIED" : "FREE";

  if (slot4_status != prev_slot4_status) {
    slot4_time = timestamp;
    prev_slot4_status = slot4_status;
    lcd4.clear();
  }
}

// ----------- FIREBASE COMMUNICATION -----------

void liveFeed() {
  json.clear();
  json.set("slot1", slot1_status);
  json.set("slot1_time", slot1_time);
  json.set("slot2", slot2_status);
  json.set("slot2_time", slot2_time);
  json.set("slot3", slot3_status);
  json.set("slot3_time", slot3_time);
  json.set("slot4", slot4_status);
  json.set("slot4_time", slot4_time);

  if (Firebase.RTDB.setJSON(&fbdo, "slots", &json)) {
    Serial.println("Live data sent successfully");
  } else {
    Serial.println("Firebase Error (Live): " + fbdo.errorReason());
  }
}

void historyData() {
  DB_history = History + "/" + String(timestamp);
  json.clear();
  json.set("/slot1", slot1_status);
  json.set("/slot1_time", String(slot1_time));
  json.set("/slot2", slot2_status);
  json.set("/slot2_time", String(slot2_time));
  json.set("/slot3", slot3_status);
  json.set("/slot3_time", String(slot3_time));
  json.set("/slot4", slot4_status);
  json.set("/slot4_time", String(slot4_time));
  json.set("/timestamp", String(timestamp));

  if (Firebase.RTDB.setJSON(&fbdo, DB_history.c_str(), &json)) {
    Serial.println("History data logged successfully");
  } else {
    Serial.println("Firebase Error (History): " + fbdo.errorReason());
  }
}

// Retrieves the 'delay' integer from the slots_info node for each slot
void getDelayInfo() {
  Serial.println("--- Fetching Delay Info from Firebase ---");

  // Check Slot 1
  if (Firebase.RTDB.getInt(&fbdo, "/slots_info/slot1/delay")) {
    slot1_delay = fbdo.intData();
    Serial.print("Slot 1 Delay fetched: ");
    Serial.println(slot1_delay);
  } else {
    Serial.print("Slot 1 Read Error: ");
    Serial.println(fbdo.errorReason());
  }

  // Check Slot 2
  if (Firebase.RTDB.getInt(&fbdo, "/slots_info/slot2/delay")) {
    slot2_delay = fbdo.intData();
  } else {
    Serial.print("Slot 2 Read Error: ");
    Serial.println(fbdo.errorReason());
  }

  // Check Slot 3
  if (Firebase.RTDB.getInt(&fbdo, "/slots_info/slot3/delay")) {
    slot3_delay = fbdo.intData();
  } else {
    Serial.print("Slot 3 Read Error: ");
    Serial.println(fbdo.errorReason());
  }

  // Check Slot 4
  if (Firebase.RTDB.getInt(&fbdo, "/slots_info/slot4/delay")) {
    slot4_delay = fbdo.intData();
  } else {
    Serial.print("Slot 4 Read Error: ");
    Serial.println(fbdo.errorReason());
  }
}

// ----------- DISPLAY & FORMATTING -----------

void displaySlots() {
  // LCD 1
  lcd1.setCursor(0, 0);
  lcd1.print("Slot 1:"); lcd1.print(slot1_status); lcd1.print("   ");
  lcd1.setCursor(0, 1);
  if (slot1_delay == -1) {
    lcd1.print("Delayed         "); // Padded to overwrite old characters
  } else {
    lcd1.print(formatDateTime(slot1_time)); lcd1.print("    ");
  }

  // LCD 2
  lcd2.setCursor(0, 0);
  lcd2.print("Slot 2:"); lcd2.print(slot2_status); lcd2.print("   ");
  lcd2.setCursor(0, 1);
  if (slot2_delay == -1) {
    lcd2.print("Delayed         ");
  } else {
    lcd2.print(formatDateTime(slot2_time)); lcd2.print("    ");
  }

  // LCD 3
  lcd3.setCursor(0, 0);
  lcd3.print("Slot 3:"); lcd3.print(slot3_status); lcd3.print("   ");
  lcd3.setCursor(0, 1);
  if (slot3_delay == -1) {
    lcd3.print("Delayed         ");
  } else {
    lcd3.print(formatDateTime(slot3_time)); lcd3.print("    ");
  }

  // LCD 4
  lcd4.setCursor(0, 0);
  lcd4.print("Slot 4:"); lcd4.print(slot4_status); lcd4.print("   ");
  lcd4.setCursor(0, 1);
  if (slot4_delay == -1) {
    lcd4.print("Delayed         ");
  } else {
    lcd4.print(formatDateTime(slot4_time)); lcd4.print("    ");
  }
}

// Converts Unix timestamp string to a readable format (DD/MM HH:MM)
String formatDateTime(String ts) {
  int64_t raw = atoll(ts.c_str());   
  raw = raw / 1000;                  
  struct tm *ti = localtime((time_t*)&raw);

  char buffer[20];
  sprintf(buffer, "%02d/%02d %02d:%02d",
          ti->tm_mday,
          ti->tm_mon + 1,
          ti->tm_hour,
          ti->tm_min);

  return String(buffer);
}