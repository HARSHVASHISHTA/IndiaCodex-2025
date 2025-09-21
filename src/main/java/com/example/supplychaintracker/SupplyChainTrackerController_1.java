package com.example.supplychaintracker;

import com.bloxbean.cardano.client.account.Account;
import com.bloxbean.cardano.client.api.model.Result;
import com.bloxbean.cardano.client.common.model.Networks;
import com.bloxbean.cardano.client.function.helper.SignerProviders;
import com.bloxbean.cardano.client.metadata.MetadataBuilder;
import com.bloxbean.cardano.client.quicktx.QuickTxBuilder;
import com.bloxbean.cardano.client.quicktx.Tx;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.bloxbean.cardano.client.backend.api.BackendService;
import com.bloxbean.cardano.client.backend.blockfrost.service.BFBackendService;
import com.bloxbean.cardano.client.api.model.Amount;
import com.bloxbean.cardano.client.metadata.Metadata;
import com.bloxbean.cardano.client.metadata.MetadataMap;

import java.math.BigInteger;

@RestController
public class SupplyChainTrackerController_1 {

    private final BackendService backendService;
    //private final String senderAddress = "addr_test1qrw8...q8t62tqf2g8x8"; // Placeholder
    private final String receiverAddress = "addr_test1qrmypje2ydmd3vufk6nkar83wsujxnw8dfdq532htanmd5z5k8lcqhvsxyhq3c82rupqufz0w6wmfq6cu8qv9z5lce8qjg68e5"; // Placeholder
    private final String blockfrostProjectId = "preprod6eW5dEni2Dum561WsxUXWVi8RpwIrrC0"; // Replace with your actual Blockfrost project ID
    String mnemonic = "cereal guitar fame narrow balance wing blood orient bundle mouse dawn artefact";
    private final Account senderAccount = new Account(Networks.preprod(), mnemonic);;
    private final String senderAddress = "addr_test1qr7zj7c7s5889p2a5xzs4atflm6d53lzgtw5x74au7nccgejydp93l3wcydj4fr8ymfw9anv7g8g9y6ck6ssshu39yzsle74q8";
    String receiver1 = new Account(Networks.testnet(), mnemonic).baseAddress();

    public SupplyChainTrackerController_1() {
        // Initialize Blockfrost service for the testnet.
        this.backendService = new BFBackendService("https://cardano-preprod.blockfrost.io/api/v0/", blockfrostProjectId);
    }

    @PostMapping("/api/track_1")
    public ResponseEntity<String> trackProduct(@RequestBody SupplyChainEvent event) {
        System.out.println("==================================================");
        System.out.println("Initialized Sender Address: " + this.senderAddress);
        System.out.println("==================================================");

        System.out.println("Received event from frontend: " + event.toString());


        try {
            // Build the transaction metadata.
            MetadataMap metadataMap = MetadataBuilder.createMap()
                    .put("productId", event.getProductId())
                    .put("status", event.getStatus())
                    .put("timestamp", new java.util.Date().toString());


            Metadata metadata = MetadataBuilder.createMetadata()
            .put(BigInteger.valueOf(1), metadataMap); // Metadata label can be any integer

            QuickTxBuilder quickTxBuilder = new QuickTxBuilder(backendService);

            Tx tx = new Tx()
                    .payToAddress(receiverAddress, Amount.ada(1L))
                    .attachMetadata(metadata)
                    .from(senderAddress);

            Result<String> result = quickTxBuilder.compose(tx)
                    .withSigner(SignerProviders.signerFrom(senderAccount))
                    .complete();
            if (result.isSuccessful()) {
                // This is a mock submission as we don't have a signing key
                String transactionHash = result.getValue();
                System.out.println("Simulated transaction with metadata. Tx Hash: " + transactionHash);
                return ResponseEntity.ok(transactionHash);
            } else {
                System.err.println("Error processing transaction: " + result.toString());
                return ResponseEntity.status(500).body("Error creating transaction.");
            }
//            Transaction transaction = quickTxUtil.createTransferTransaction(senderAddress, receiverAddress, Amount.ada(2000000L), metadata);
//
//            // Create a mock payment transaction. This is a simplified example.
//            PaymentTransaction paymentTransaction = PaymentTransaction.builder()
//                    .sender(senderAddress)
//                    .receiver(receiverAddress)
//                    .amount(Amount.ada(2000000L)) // 2 ADA for demonstration
//                    .auxiliaryData(AuxiliaryData.builder().metadata(metadata).build())
//                    .build();
//
//            // Simulate the transaction submission to the blockchain.
            //String transactionHash = UUID.randomUUID().toString().replace("-", "");

            //System.out.println("Simulated transaction with metadata: " + JsonUtil.getPrettyJson(paymentTransaction.getAuxiliaryData().getMetadata()));

            //return ResponseEntity.ok(transactionHash);

        }catch (Exception e) {
            System.err.println("Error processing transaction: " + e.getMessage());
            return ResponseEntity.status(500).body("Error creating transaction.");
        }
    }
}
