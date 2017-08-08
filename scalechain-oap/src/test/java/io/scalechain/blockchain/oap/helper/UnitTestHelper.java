package io.scalechain.blockchain.oap.helper;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import io.scalechain.blockchain.GeneralException;
import io.scalechain.blockchain.chain.Blockchain;
import io.scalechain.blockchain.oap.exception.OapException;
import io.scalechain.blockchain.proto.*;
import io.scalechain.blockchain.script.ScriptParser;
import io.scalechain.blockchain.transaction.CoinAddress;
import io.scalechain.blockchain.transaction.OutputOwnership;
import io.scalechain.blockchain.transaction.ParsedPubKeyScript;
import io.scalechain.util.Bytes;
import io.scalechain.util.HexUtil;
import io.scalechain.wallet.Wallet;

import java.io.File;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Iterator;

/**
 * provides sample address data to some test cases.
 *
 * Created by shannon on 16. 12. 26.
 */
public class  UnitTestHelper {

  protected static HashMap<String, JsonObject> TEST_ADDRESSES = new HashMap<String, JsonObject>();
  protected static JsonArray TEST_ADDRESSES_ARRAY;


  public static String ACCOUNT_INTERNAL           = "_FOR_TEST_ONLY";
  public static String ACCOUNT_SEND_MANY_SENDER   = "SEND_MANY_SENDER";
  public static String ACCOUNT_SEND_MANY_RECEIVER = "SEND_MANY_RECEIVER";
  public static String ACCOUNT_GET_ADDRESSES      = ACCOUNT_SEND_MANY_RECEIVER;
  public static int[] SEND_MANY_RECEIVER_ADDRESS_INDEXES = {5, 6};
  public static int   SEND_MANY_SEND_ADDRESS_INDEX = 4;

  public static void init(){
    // READ TEST ADDRESSES DATA INTO "TEST_ADDRESSES"
    //   BITCOIN ADDRESS, PRIVATE KEY, PUBLIC KEY HASH, ASSET ADDRESS, ASSET ID, ASSET DEFINITION FILE
    String network = System.getenv("NETWORK");
    network = (network == null)? "testnet" : network;
    File f = new File("addresses." + network);
    JsonParser parser = new JsonParser();
    try {
      TEST_ADDRESSES_ARRAY = parser.parse(new JsonReader(
        new InputStreamReader(UnitTestHelper.class.getClassLoader().getResourceAsStream("addresses." + network))
      )).getAsJsonArray();
      for(JsonElement e : TEST_ADDRESSES_ARRAY) {
        JsonObject address = e.getAsJsonObject();
        TEST_ADDRESSES.put(address.get("bitcoinAddress").getAsString(), address);
      }
    } catch (Exception e) {
      // BUGBUG : Looks like Shannons codes are not checking types of exceptions.
      // Also some exceptions are ignored.
      e.printStackTrace();
    }
  }

  public static String getBitcoinAddressAt(int i) {
    return TEST_ADDRESSES_ARRAY.get(i).getAsJsonObject().get("bitcoinAddress").getAsString();
  }

  public static String getAssetId(String bitcoinAddress) {
    return TEST_ADDRESSES.get(bitcoinAddress).get("assetId").getAsString();
  }

  public static String getAssetAddress(String bitcoinAddress) {
    return TEST_ADDRESSES.get(bitcoinAddress).get("assetAddress").getAsString();
  }

  public static JsonObject getAssetDefinition(String bitcoinAddress) {
    return TEST_ADDRESSES.get(bitcoinAddress).get("asset_definition").getAsJsonObject();
  }

  public static String getAssetDefinitionHash(String bitcoinAddress) {
    return TEST_ADDRESSES.get(bitcoinAddress).get("asset_definition_hash").getAsString();
  }

  public static String getPublicKeyHash(String bitcoinAddress) {
    return TEST_ADDRESSES.get(bitcoinAddress).get("publicKeyHashHex").getAsString();
  }

  public static String getPrivateKey(String bitcoinAddress) {
    return TEST_ADDRESSES.get(bitcoinAddress).get("privateKey").getAsString();
  }

  public static void importAddress(String account, String scriptOrAddress) throws OapException {
    OutputOwnership ownership;
    try {
      ownership = CoinAddress.from(scriptOrAddress);
    } catch(Exception e) {
      if (e instanceof GeneralException) {
        try {
          ownership = new ParsedPubKeyScript(
            ScriptParser.parse(new LockingScript(new Bytes(HexUtil.bytes(scriptOrAddress))))
          );
        } catch(Exception ex) {
            throw new OapException(OapException.INTERNAL_ERROR, "Cannot parse script", ex);
          }
        } else {
          throw new OapException(OapException.INTERNAL_ERROR, "Internal Error", e);
        }
      }
      System.out.println("importing" + ownership);
      Wallet.get().importOutputOwnership(Blockchain.get().getDb(), Blockchain.get(), account, ownership, false);
  }

  // Compare 2 transactions.
  // 2 transactions should have
  //   same tx inputs and same tx outputs
  public static boolean compareTx(Transaction tx, Transaction ex, long fees) throws OapException {
    if (tx.getVersion() != ex.getVersion()) return false;
    if (tx.getOutputs().size() != ex.getOutputs().size()) return false;
    if (tx.getInputs().size() != ex.getInputs().size()) return false;
    Iterator<TransactionOutput> txOutIter = tx.getOutputs().iterator();
    Iterator<TransactionOutput> exOutIter = ex.getOutputs().iterator();
    while(true) {
      if (!txOutIter.hasNext() || !exOutIter.hasNext()) break;
      TransactionOutput txOut = txOutIter.next();
      TransactionOutput exOut = exOutIter.next();
      if (txOut.getValue() != exOut.getValue()) return false;
      return txOut.getLockingScript().equals(exOut.getLockingScript());
    }
    if (txOutIter.hasNext() || exOutIter.hasNext()) return false;

    Iterator<TransactionInput> txInIter = tx.getInputs().iterator();
    Iterator<TransactionInput> exInIter = ex.getInputs().iterator();
    while(true) {
      if (!txInIter.hasNext() || !exInIter.hasNext()) break;
      TransactionInput txIn = txInIter.next();
      TransactionInput exIn = exInIter.next();
      if (!txIn.getOutPoint().equals(exIn.getOutPoint())) return false;
    }
    if (txInIter.hasNext() || exInIter.hasNext()) return false;
    return true;
  }

  public static Transaction getTransaction(String txId) throws OapException {
    Transaction tx = Blockchain.get().getTransaction(Blockchain.get().getDb(), new Hash( new Bytes(HexUtil.bytes(txId))));
    return tx;
  }
}
