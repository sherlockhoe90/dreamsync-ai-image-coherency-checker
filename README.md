# **DreamSync — AI Scene Consistency Checker**  
*Keeping AI-generated images visually coherent.*

DreamSync is a lightweight and creator-friendly tool that analyzes **AI-generated images** for visual consistency across scenes. When generating storyboards, character shots, or sequential frames, artists often struggle with unpredictable color shifts, identity drift, inconsistent lighting, or sudden changes in style. DreamSync automatically detects these mismatches and provides **clear, human-readable explanations** so creators can quickly refine their outputs.

This project was built for the **PixelRiot Hackathon**, embracing the idea that AI should empower creators—not replace them.

---

## ⭐ Features

### 🔍 Image Consistency Analysis  
DreamSync compares scenes pairwise using:
- **Perceptual hashing (aHash + Hamming distance)**  
- **Average color & palette-shift metrics**  
- **Lighting & brightness difference detection**  
- **Simple structural drift indicators**  
- **Natural-language explanations** (e.g., “background became cooler”, “lighting darker”, “clothing color changed”)

Results include per-pair metrics, summaries, and detailed reasoning.

### 📂 Project-Based Workflow  
- Create projects  
- Add multiple scenes  
- Upload images to each scene  
- Run consistency checks with a single click  

### 🖥 Simple, Clean UI  
A lightweight Bootstrap UI (no frameworks required) is included:
- Upload scenes  
- Trigger analysis  
- See clean summaries + expandable detailed reports  

### 🚀 Fast & Lightweight  
- Built with **Spring Boot (Java 8)**  
- Uses custom image utilities (no ML frameworks)  
- Fast enough for real-time creative workflows  
- Deployable locally or publicly via **ngrok**  

### 🎬 Future Expansion: Video Consistency  
While the current version analyzes **images**, DreamSync is designed to expand into **video coherency checking**.  
Upcoming planned features:
- Frame extraction via `ffmpeg`  
- Per-frame similarity scoring  
- Drift detection for lighting, palette, character identity  
- Video-to-video consistency reports  

---

## 🛠 Tech Stack

**Backend:**  
- Java 8  
- Spring Boot  
- ImageIO  
- Custom hashing & color analysis utilities  

**Frontend:**  
- HTML  
- Bootstrap  
- Vanilla JavaScript  

**Hosting / Access:**  
- Local deployment  
- ngrok for public demo links  

---

## 🔧 Running the Project

### 1. Build the project
```bash
mvn clean package -DskipTests

