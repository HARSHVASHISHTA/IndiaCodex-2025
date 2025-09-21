Decentralized Product Passport: A Supply Chain Tracker on Cardano 🚀
A hackathon project demonstrating a transparent and tamper-proof supply chain solution. This system tracks a product's journey and mints its entire history as a unique "Digital Passport" NFT on the Cardano blockchain, verifiable by anyone with a QR code.

The Problem 🤔
For small and medium-sized businesses—like a specialty coffee grower in Araku Valley or a pearl artisan here in Hyderabad—proving the authenticity of their products to a global audience is a massive challenge.

Lack of Trust: How can a customer be sure a product is genuine?

Counterfeiting: High-value goods are easily copied, damaging brand reputation.

High Costs: Traditional supply chain solutions are too expensive and complex for small businesses.

Our Solution ✅
Our project creates a "Digital Passport" for physical products using the power of the Cardano blockchain.

It's a simple two-phase system:

Off-Chain Tracking: Businesses log each step of the product's journey (e.g., "Materials Sourced," "Shipped") via a simple API. This is fast and free.

On-Chain Finalization: Once the journey is complete, the system takes the entire history and mints a unique Cardano NFT. The journey is embedded forever into the NFT's metadata, creating an unforgeable certificate of authenticity.

Key Features ✨
Track Product Journeys: Log unlimited status updates for any product via a REST API.

Mint a Digital Passport: Finalize a product's history by minting a unique Cardano NFT with the journey embedded in its metadata (following the CIP-25 standard).

Instant Verification via QR Code: Automatically generate a scannable QR code that links directly to the transaction on a public blockchain explorer.

Built with a robust and scalable Java (Spring Boot) backend.

Tech Stack 🛠️
Backend: Java 17, Spring Boot 3

Blockchain: Cardano (Preprod Testnet)

Cardano Integration: cardano-client-lib

Frontend: HTML, Tailwind CSS, Vanilla JavaScript

Build Tool: Maven

Live Demo Flow 🎬
This project is designed for a live demonstration.

Track Steps: Using the web UI, add several status updates for a new productId (e.g., "HYD-COFFEE-001"). The UI will log these intermediate steps.

Finalize & Mint: Click the "Finalize & Mint NFT" button. This calls the backend to create, sign, and submit the minting transaction to the Cardano blockchain.

Get Proof: A transaction hash is returned upon success.

Scan the QR Code: The UI fetches and displays a QR code. When scanned, it opens the Cardanoscan explorer page, showing the newly minted NFT and its metadata—the product's complete, permanent, and verifiable history.

API Endpoints 🔌
POST /api/track

Body: {"productId": "string", "status": "string", "timestamp": "ISO_DATE_STRING"}

Description: Adds a status update to the product's in-memory journey.

POST /api/finalize/{productId}

Description: Takes all tracked steps for the given productId, mints an NFT with the history in its metadata, and returns the transaction hash.

GET /api/qr/{txHash}

Description: Returns a PNG image of a QR code that links to the specified transaction hash on the Preprod Cardanoscan explorer.

How to Run Locally ⚙️
Clone the repository:

Bash

git clone https://github.com/HARSHVASHISHTA/IndiaCodex-2025
Prerequisites:

Java 17 or higher

Maven

Configuration:

You will need a free Blockfrost API key for the Preprod network.

You will need a 24-word recovery phrase for a Preprod testnet wallet.

Fund the wallet address using the official Cardano Faucet.

Start the application:

Bash

mvn spring-boot:run
Open the index.html file in your browser to interact with the frontend.

Presentation Slide: https://docs.google.com/presentation/d/1MYq5Fx9dZMO6AiWRpgcmG4nlYWn60Qqbu3gd3zpMWj8/edit?usp=sharing