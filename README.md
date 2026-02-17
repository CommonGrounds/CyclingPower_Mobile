# Cycle Power

**Cycle Power** is a high-performance, open-source cycling computer designed specifically for mobile devices (Android & iOS). Built with JavaFX and compiled natively, it provides real-time data tracking for cyclists who want precision and a modern interface.


> [!IMPORTANT]
> **Status:** Project is in active development.
> Bluetooth sensor integration (Power/Cadence) is currently being implemented.
> 
[![GluonFX](https://img.shields.io/badge/GluonFX-Native-blue.svg)](https://github.com/gluonhq/gluonfx)
[![Java](https://img.shields.io/badge/Java-17+-orange?logo=openjdk&logoColor=white)](https://openjdk.org/)
[![JavaFX](https://img.shields.io/badge/JavaFX-21+-blue?logo=java&logoColor=white)](https://openjfx.io)
[![AtlantaFX](https://img.shields.io/badge/AtlantaFX-2.x-4C8BF5?logoColor=white)](https://github.com/mkpaz/atlantafx)
[![GitHub License](https://img.shields.io/github/license/CommonGrounds/CyclingPower_Mobile)](https://github.com/CommonGrounds/CyclingPower_Mobile)
[![GitHub stars](https://img.shields.io/github/stars/CommonGrounds/CyclingPower_Mobile?style=flat-square&color=gold)](https://github.com/CommonGrounds/CyclingPower_Mobile/stargazers)
[![GitHub last commit](https://img.shields.io/github/last-commit/CommonGrounds/CyclingPower_Mobile?style=flat-square)](https://github.com/CommonGrounds/CyclingPower_Mobile/commits/main)
![GitHub top language](https://img.shields.io/github/languages/top/CommonGrounds/CyclingPower_Mobile?style=flat-square)

## 🚀 Features

* **Real-time Metrics:** Track speed (current/average), altitude, slope, and distance.
* **Advanced Environmental Data:** Stay safe with integrated **Air Quality** monitoring, **UV Index**, and weather conditions (Fog, Temperature).
* **Wind Awareness:** Unique wind direction and speed indicator to help you manage your effort against headwinds.
* **Power & Cadence:** Support for external sensors to track power output (Watts) and pedaling cadence (RPM).
* **Live Mapping:** Integrated OpenStreetMap view to track your route in real-time.
* **GPS Precision:** Detailed location data including city, suburb, and street-level addressing.

---

## 📸 Screenshots
<p align="center">
  <img src=".screenshots/CyclePower_1.jpg" width="350" title="Dashboard View">
  <img src=".screenshots/CyclePower_2.jpg" width="350" title="Map View">
</p>

## 🛠 Tech Stack

* **[Java 17+]():** The core programming language.
* **[JavaFX 21+]():** For the cross-platform UI layer.
* **[GluonFX]():** Used to compile the Java bytecode into native ARM code for Android and iOS, ensuring high performance.
* **[AtlantaFX]():** A modern CSS theme library that gives the app its sleek, "native-feel" dark mode interface.

---

## 🏗 Getting Started

### Prerequisites

* GraalVM with `native-image` installed.
* Configured environment for Android/iOS development (Android SDK/Xcode).

### Build & Run

Ensure you have **GraalVM** and **Maven** configured.

Clone the repo:
```
git clone https://github.com/CommonGrounds/CyclingPower_Mobile.git
```

To run the application in development mode:

```bash
mvn gluonfx:run
```

To build a native mobile package:

```bash
mvn clean -Pandroid gluonfx:build gluonfx:package
```

---

## 📄 License

Distributed under the Apache 2.0 License. See `LICENSE` for more information.
