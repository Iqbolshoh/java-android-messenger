# 💬 Java Android Messenger

**Java Android Messenger** (app name: **Social Chat**) is an **AI-powered chat app for Android**, built with **Java**. Every message you send is forwarded to **Google's Gemini API**, and the reply is shown as a chat bubble — with the full conversation history persisted locally in a **SQLite** database.

<p align="left">
  <img src="https://img.shields.io/badge/Java-%23ED8B00.svg?style=for-the-badge&logo=openjdk&logoColor=white" alt="Java">
  <img src="https://img.shields.io/badge/Android-%233DDC84.svg?style=for-the-badge&logo=android&logoColor=white" alt="Android">
  <img src="https://img.shields.io/badge/SQLite-%23003B57.svg?style=for-the-badge&logo=sqlite&logoColor=white" alt="SQLite">
  <img src="https://img.shields.io/badge/Gemini%20API-%234285F4.svg?style=for-the-badge&logo=google&logoColor=white" alt="Gemini API">
  <img src="https://img.shields.io/badge/License-MIT-green.svg?style=for-the-badge" alt="License">
</p>

## 📚 Table of Contents

- [Features](#-features)
- [Preview](#-preview)
- [Project Structure](#-project-structure)
- [Installation Guide](#️-installation-guide)
- [Technologies Used](#-technologies-used)
- [License](#-license)
- [Contributing](#-contributing)
- [Connect with Me](#-connect-with-me)

## ✨ Features

✅ **Gemini AI replies:** Every message is sent to Google's Gemini API and answered in real time.
✅ **Persistent history:** All messages are stored locally in a SQLite database and reloaded on launch.
✅ **Chat-style UI:** User and bot messages are rendered as distinct, rounded message bubbles.
✅ **Clear conversation:** Wipe the entire chat history with a single confirmation dialog.
✅ **Smooth animations:** Input field and send button use subtle scale/overshoot animations, and new messages fade and slide in.
✅ **Automatic retries:** Failed requests due to rate limiting are retried automatically before surfacing an error.

## 👀 Preview

> 📸 Screenshots of the running app aren't available in this repository yet — build and run the project in Android Studio to see it in action (see [Installation Guide](#️-installation-guide) below).

## 📂 Project Structure

```
java-android-messenger/
├── app/
│   └── src/main/
│       ├── java/uz/iqbolshoh/socialchat/
│       │   ├── MainActivity.java              # Chat screen: input handling, message list, animations
│       │   ├── ApiService.java                # Gemini API requests, retries, and response parsing
│       │   ├── Message.java                   # Message data model
│       │   └── MessageDatabaseHelper.java     # SQLite schema and CRUD operations
│       ├── res/
│       │   ├── layout/activity_main.xml       # Chat screen layout
│       │   ├── drawable/                       # Message bubble, button, and background styles
│       │   └── values/                         # App name, colors, and theme
│       └── AndroidManifest.xml
├── build.gradle
└── README.md
```

## ⚙️ Installation Guide 🛠️

### 1️⃣ Clone the Repository 📥
```bash
git clone https://github.com/Iqbolshoh/java-android-messenger.git
```

### 2️⃣ Open in Android Studio 📂
Open the `java-android-messenger` folder as a project and let Gradle sync.

### 3️⃣ Add Your Gemini API Key 🔑
Open `app/src/main/java/uz/iqbolshoh/socialchat/ApiService.java` and replace the placeholder with your own key:
```java
private static final String API_KEY = "API_KEY";
```
> Get a free key from [Google AI Studio](https://aistudio.google.com/).

### 4️⃣ Run the App 🚀
Select an emulator or connect a physical device, then click **Run**.

## 🖥 Technologies Used
![Java](https://img.shields.io/badge/Java-%23ED8B00.svg?style=for-the-badge&logo=openjdk&logoColor=white)
![Android](https://img.shields.io/badge/Android-%233DDC84.svg?style=for-the-badge&logo=android&logoColor=white)
![SQLite](https://img.shields.io/badge/SQLite-%23003B57.svg?style=for-the-badge&logo=sqlite&logoColor=white)
![Gemini API](https://img.shields.io/badge/Gemini%20API-%234285F4.svg?style=for-the-badge&logo=google&logoColor=white)

## 📜 License
This project is open-source and available under the [MIT License](./LICENSE).

## 🤝 Contributing
🎯 Contributions are welcome! If you have suggestions or want to enhance the project, feel free to fork the repository and submit a pull request.

## 📬 Connect with Me
💬 I love meeting new people and discussing tech, business, and creative ideas. Let's connect! You can reach me on these platforms:

<div align="center">

[![Website](https://img.shields.io/badge/Website-4285F4?style=for-the-badge&logo=googlechrome&logoColor=white)](https://iqbolshoh.uz)
[![Gmail](https://img.shields.io/badge/Gmail-EA4335?style=for-the-badge&logo=gmail&logoColor=white)](mailto:iilhomjonov777@gmail.com)
[![GitHub](https://img.shields.io/badge/GitHub-181717?style=for-the-badge&logo=github&logoColor=white)](https://github.com/iqbolshoh)
[![Telegram](https://img.shields.io/badge/Telegram-26A5E4?style=for-the-badge&logo=telegram&logoColor=white)](https://t.me/templates_uz_support)
[![WhatsApp](https://img.shields.io/badge/WhatsApp-25D366?style=for-the-badge&logo=whatsapp&logoColor=white)](https://wa.me/998776030033)
[![Instagram](https://img.shields.io/badge/Instagram-E4405F?style=for-the-badge&logo=instagram&logoColor=white)](https://instagram.com/iqbolshoh.dev)
[![YouTube](https://img.shields.io/badge/YouTube-FF0000?style=for-the-badge&logo=youtube&logoColor=white)](https://www.youtube.com/@Iqbolshoh_dev)

</div>
