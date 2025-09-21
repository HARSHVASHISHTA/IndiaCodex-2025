package com.example.supplychaintracker;

import com.bloxbean.cardano.client.account.Account;
import com.bloxbean.cardano.client.api.model.Amount;
import com.bloxbean.cardano.client.api.model.Result;
import com.bloxbean.cardano.client.api.util.PolicyUtil;
import com.bloxbean.cardano.client.common.model.Networks;
import com.bloxbean.cardano.client.crypto.KeyGenUtil;
import com.bloxbean.cardano.client.crypto.Keys;
import com.bloxbean.cardano.client.crypto.SecretKey;
import com.bloxbean.cardano.client.function.helper.SignerProviders;
import com.bloxbean.cardano.client.metadata.Metadata;
import com.bloxbean.cardano.client.metadata.cbor.CBORMetadata;
import com.bloxbean.cardano.client.metadata.cbor.CBORMetadataList;
import com.bloxbean.cardano.client.metadata.cbor.CBORMetadataMap;
import com.bloxbean.cardano.client.plutus.spec.PlutusScript;
import com.bloxbean.cardano.client.quicktx.QuickTxBuilder;
import com.bloxbean.cardano.client.quicktx.Tx;
import com.bloxbean.cardano.client.transaction.spec.*;
import com.bloxbean.cardano.client.transaction.spec.script.NativeScript;
import com.bloxbean.cardano.client.transaction.spec.script.ScriptPubkey;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.bloxbean.cardano.client.backend.api.BackendService;
import com.bloxbean.cardano.client.backend.blockfrost.service.BFBackendService;

import java.io.ByteArrayOutputStream;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RestController
public class SupplyChainTrackerController {

    private final BackendService backendService;
    private final Account senderAccount;
    private final String senderAddress;
    private final String receiverAddress = "addr_test1qrmypje2ydmd3vufk6nkar83wsujxnw8dfdq532htanmd5z5k8lcqhvsxyhq3c82rupqufz0w6wmfq6cu8qv9z5lce8qjg68e5";

    // FEATURE 1: In-Memory database to track product journeys before they are finalized.
    // This satisfies the "transaction only at last state" requirement.
    private final Map<String, List<SupplyChainEvent>> productJourneys = new ConcurrentHashMap<>();

    public SupplyChainTrackerController() {
        // IMPORTANT: In a real app, load these from a secure config/environment file!
        String blockfrostProjectId = "preprod6eW5dEni2Dum561WsxUXWVi8RpwIrrC0";
        String mnemonic = "cereal guitar fame narrow balance wing blood orient bundle mouse dawn artefact";

        this.backendService = new BFBackendService("https://cardano-preprod.blockfrost.io/api/v0/", blockfrostProjectId);
        this.senderAccount = new Account(Networks.preprod(), mnemonic);
        this.senderAddress = senderAccount.baseAddress();

        System.out.println("==================================================");
        System.out.println("Controller Initialized. Sender Address: " + this.senderAddress);
        System.out.println("==================================================");
    }

    /**
     * ENDPOINT 1 (Modified): Track an intermediate step.
     * This now saves the event in memory instead of creating a transaction.
     */
    @PostMapping("/api/track")
    public ResponseEntity<String> trackProduct(@RequestBody SupplyChainEvent event) {
        System.out.println("Received tracking event: " + event);

        // Get the existing journey for this product or create a new one
        List<SupplyChainEvent> journey = productJourneys.computeIfAbsent(event.getProductId(), k -> new ArrayList<>());
        journey.add(event);

        System.out.println("Current journey for product " + event.getProductId() + ": " + journey.size() + " steps.");
        return ResponseEntity.ok("Status for product '" + event.getProductId() + "' updated successfully.");
    }

    /**
     * ENDPOINT 2 (NEW): Finalize a product's journey and mint an NFT.
     * This is the only endpoint that creates a blockchain transaction.
     */
    @PostMapping("/api/finalize/{productId}")
    public ResponseEntity<String> finalizeAndMintNFT(@PathVariable String productId) {
        System.out.println("Attempting to finalize journey for product: " + productId);

        List<SupplyChainEvent> journey = productJourneys.get(productId);
        if (journey == null || journey.isEmpty()) {
            return ResponseEntity.status(404).body("No journey found for product: " + productId);
        }

        try {

            Policy policy = PolicyUtil.createMultiSigScriptAtLeastPolicy("test_policy", 1, 1);
            BigInteger qty = BigInteger.valueOf(200);
            String assetName = productId;
            Asset asset = new Asset(assetName, BigInteger.ONE);
            String policyId = policy.getPolicyId();
            CBORMetadataList journeyList = new CBORMetadataList();
            for (SupplyChainEvent event : journey) {
                journeyList.add(new CBORMetadataMap()
                        .put("status", event.getStatus())
                        .put("timestamp", new java.util.Date().toString())); // Assume event has a getTimestamp()
            }

            Metadata metadata = new CBORMetadata()
                    .put(new BigInteger("721"), new CBORMetadataMap() // CIP-25 NFT Standard
                            .put(policyId, new CBORMetadataMap()
                                    .put(assetName, new CBORMetadataMap()
                                            .put("name", "Digital Passport: " + productId)
                                            .put("image", "ipfs://Qm...") // Placeholder for an image
                                            .put("journey", journeyList)
                                    )
                            )
                    );

            // 4. Build and submit the minting transaction
            Tx tx = new Tx()
                    .payToAddress(receiverAddress, Amount.ada(1L))
                    .mintAssets(policy.getPolicyScript(), asset, senderAddress)
                    .attachMetadata(metadata)
                    .from(senderAddress);
            QuickTxBuilder quickTxBuilder = new QuickTxBuilder(backendService);
            Result<String> result = quickTxBuilder.compose(tx)
                    .withSigner(SignerProviders.signerFrom(senderAccount))
                    .withSigner(SignerProviders.signerFrom(policy))
                    .completeAndWait(System.out::println);



            // Compose, sign with both payment key and policy key, and submit



            if (result.isSuccessful()) {
                String txHash = result.getValue();
                System.out.println("✅ NFT Minted Successfully! Tx Hash: " + txHash);
                // Clean up the in-memory journey after successful minting
                productJourneys.remove(productId);
                return ResponseEntity.ok(txHash);
            } else {
                System.err.println("❌ NFT Minting Failed: " + result.getResponse());
                return ResponseEntity.status(500).body("Error minting NFT: " + result.getResponse());
            }

        } catch (Exception e) {
            System.err.println("An exception occurred during minting: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error minting NFT: " + e.getMessage());
        }
    }

    /**
     * ENDPOINT 3 (NEW): Generate a QR code for a given transaction hash.
     * This creates a link to the Cardanoscan explorer.
     */
    @GetMapping(value = "/api/qr/{txHash}", produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<byte[]> getQRCode(@PathVariable String txHash) {
        String url = "https://preprod.cardanoscan.io/transaction/" + txHash;
        try {
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(url, BarcodeFormat.QR_CODE, 250, 250);

            ByteArrayOutputStream pngOutputStream = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", pngOutputStream);
            byte[] pngData = pngOutputStream.toByteArray();
            return ResponseEntity.ok(pngData);
        } catch (Exception e) {
            System.err.println("Could not generate QR code: " + e.getMessage());
            return ResponseEntity.status(500).build();
        }
    }
}