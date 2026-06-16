# 🚌 ArrivaX – Smart Bus Stand Management System

An IoT-based smart transportation system that improves bus scheduling, reduces delays, and enhances passenger convenience using real-time monitoring, cloud synchronization, and mobile integration.

---

## 📌 Project Overview

ArrivaX is a **Smart Bus Stand Management System** that integrates:

* ESP32 microcontrollers
* Ultrasonic sensors
* Firebase Realtime Database
* Android mobile application

The system enables real-time monitoring of bus arrivals, slot occupancy, delay detection, and transportation analytics.

Passengers and administrators can access live updates, manage schedules, and monitor system performance through a mobile app and admin dashboard.

---

## 🎯 Objectives

* Provide real-time bus tracking and arrival updates
* Reduce passenger waiting uncertainty
* Improve public transport efficiency
* Enable intelligent bus slot management
* Detect early, on-time, and delayed buses
* Centralize transportation data using cloud technology
* Support smart city transportation systems

---

## ⭐ Key Features

* ✔️ Real-time bus tracking and status updates
* ✔️ Automatic delay calculation (Scheduled vs Current time)
* ✔️ Smart slot management (A-01, A-02, A-03...)
* ✔️ Firebase cloud synchronization
* ✔️ IoT-based LCD passenger information display
* ✔️ Admin dashboard for full system control
* ✔️ Conductor and staff management
* ✔️ Live analytics and performance monitoring
* ✔️ Manual delay override system
* ✔️ Search and filter bus schedules
* ✔️ Scalable smart transportation architecture

---

## 🏗️ System Architecture

* ESP32 IoT Controllers
* Ultrasonic Distance Sensors
* LCD Display Modules
* Firebase Cloud Database
* Android Mobile Application
* Admin Dashboard System

---

## 🔄 System Workflow

1. Ultrasonic sensors detect incoming buses
2. ESP32 processes distance data
3. System calculates ETA and delay status
4. Bus slot occupancy is updated
5. Firebase synchronizes real-time data
6. LCD displays bus status information
7. Android app receives live updates
8. Admin dashboard monitors system analytics

---

## 🔧 Hardware Components

* ESP32 Microcontroller
* Ultrasonic Sensors
* LCD Display Module
* Wi-Fi Module (Built-in ESP32)
* Power Supply Unit

---

## 💻 Software Technologies

### 📱 Mobile App

* Kotlin
* Android Studio
* MVVM Architecture
* Firebase Authentication
* Firebase Realtime Database
* Firebase Firestore

### ⚙️ Embedded System

* Arduino IDE
* C / C++
* Firebase ESP Client Library
* Wi-Fi Communication Protocol
* NTP (Network Time Protocol)

---

## 🧩 Functional Modules

### 🚍 Smart ETA Prediction Module

* Calculates bus arrival time
* Detects delays and early arrivals
* Monitors real-time movement

---

### 🅿️ Smart Slot Management Module

* Detects slot occupancy using ultrasonic sensors
* Displays FREE / OCCUPIED status
* Supports multiple bus lanes
* Updates LCD in real-time
* Syncs with Firebase instantly

---

### 📱 Android Application Module

* User registration and login
* Real-time bus tracking
* Schedule and slot management
* Live updates via Firebase
* Delay monitoring system
* Search and filtering features
* Admin controls for updates

---

### 🧑‍💼 Admin Dashboard

* Manage schedules and bus slots
* Manage conductors and staff
* Monitor delays and performance
* View analytics dashboard

---

### ☁️ Cloud Synchronization Module

* Firebase Realtime Database integration
* Multi-device synchronization
* Live updates across system

---

## ⚙️ Installation & Setup

### 🔌 Hardware Setup

* Connect ultrasonic sensors to ESP32
* Connect LCD display module
* Configure Wi-Fi connection
* Power the device
* Mount bus slot system

---

### 💻 Embedded Software Setup

1. Install Arduino IDE

2. Install required libraries:

   * WiFi Library
   * Firebase ESP Client Library
   * LiquidCrystal Library

3. Configure:

   * Wi-Fi credentials
   * Firebase API key
   * Database URL

4. Upload code to ESP32

---

## 🚍 Sample Use Case

When a bus approaches the smart stand:

* Sensor detects distance
* ESP32 calculates ETA and delay
* System identifies:

  * On Time
  * Delayed
  * Early Arrival
* Firebase updates instantly
* LCD displays live status
* Android app reflects real-time updates

---

## 🎯 Advantages

* Reduces passenger waiting time uncertainty
* Improves transport reliability
* Provides real-time monitoring
* Supports smart city infrastructure
* Low-cost IoT implementation
* Scalable cloud system

---

## 🚀 Future Enhancements

* GPS-based live tracking
* AI delay prediction system
* Passenger notification alerts
* QR-based ticket system
* Voice announcement system
* Solar-powered bus stands
* Advanced analytics dashboard

---

## 👨‍💻 Team Members

* Mihili De Silva
* Thamasha Nethmini
* Gothami Dikmadugoda

---

## 🎥 Demo Video

<video src="https://github.com/user-attachments/assets/9aedaba2-6c31-4359-aa07-de2c5c468a7c" controls="controls" style="max-width: 100%;"></video>

---

## 📧 Contact

For inquiries or collaboration:
📩 Email: mihililahiruka925@gmail.com

---

## 📜 License

This project is licensed under the MIT License and is available for educational and non-commercial use.

---

