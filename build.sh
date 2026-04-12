#!/bin/bash
# ==============================================================
#  build.sh  –  Compile + package the Multiplayer Quiz System
#  Run:  chmod +x build.sh && ./build.sh
#  Output: QuizServer.jar  and  QuizClient.jar
# ==============================================================

set -e   # stop on any error

echo ""
echo " ╔══════════════════════════════════════╗"
echo " ║   Multiplayer Quiz  –  Build Tool    ║"
echo " ╚══════════════════════════════════════╝"
echo ""

# ── 1. Compile ────────────────────────────────────────────────────────────────
echo "[1/4] Compiling sources..."
rm -rf out && mkdir out

javac -cp "lib/mysql-connector-j-9.6.0.jar" -d out \
    src/model/*.java \
    src/server/*.java \
    src/client/*.java

echo "  Done."

# ── 2. Extract MySQL driver ───────────────────────────────────────────────────
echo "[2/4] Extracting MySQL driver..."
cd out
jar xf ../lib/mysql-connector-j-9.6.0.jar
rm -f module-info.class
cd ..
echo "  Done."

# ── 3. Package QuizServer.jar ─────────────────────────────────────────────────
echo "[3/4] Packaging QuizServer.jar..."
jar cfm QuizServer.jar launchers/server-manifest.txt -C out .
echo "  Done."

# ── 4. Package QuizClient.jar ─────────────────────────────────────────────────
echo "[4/4] Packaging QuizClient.jar..."
jar cfm QuizClient.jar launchers/client-manifest.txt -C out .
echo "  Done."

echo ""
echo " ✅ Build complete!"
echo "    QuizServer.jar  –  run on the host machine"
echo "    QuizClient.jar  –  share with players"
echo ""
