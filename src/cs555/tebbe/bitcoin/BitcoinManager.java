package cs555.tebbe.bitcoin;

import cs555.tebbe.node.PubSubNode;
import org.bitcoinj.core.*;
import org.bitcoinj.kits.WalletAppKit;
import org.bitcoinj.params.TestNet3Params;
import org.bitcoinj.store.BlockStore;
import org.bitcoinj.store.BlockStoreException;
import org.bitcoinj.store.MemoryBlockStore;
import org.bitcoinj.store.UnreadableWalletException;

import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by caleb.tebbe
 */
public class BitcoinManager {

    NetworkParameters params;
    WalletAppKit walletKit;
    PubSubNode node;
    List<String> txHashBuffer = new ArrayList<>();

    public BitcoinManager(PubSubNode node, String walletName) {
        params = TestNet3Params.get();
        walletKit = new WalletAppKit(params, new File("./wallets"), walletName);
        walletKit.startAsync();
        walletKit.awaitRunning();
        walletKit.wallet().addEventListener(new WalletListener());
        this.node = node;
    }

    public boolean didReceiveTransactionHash(String queryHash) {
        return txHashBuffer.remove(queryHash);
    }

    /*
        returns the transaction hash after sending to the specified addr for amount
     */
    public String sendPayment(String addr, double amount) throws AddressFormatException, InsufficientMoneyException {
        Address address = new Address(params, addr);
        Wallet.SendResult result = walletKit.wallet().sendCoins(walletKit.peerGroup(), address, Coin.parseCoin(String.valueOf(amount)));
        return result.tx.getHashAsString();
    }

    public String getBalance() {
        return walletKit.wallet().getBalance().toFriendlyString();
    }

    public String getFreshAddress() {
        return walletKit.wallet().freshReceiveAddress().toString();
    }

    public String getCurrentAddress() {
        return walletKit.wallet().currentReceiveAddress().toString();
    }

    class WalletListener extends AbstractWalletEventListener {

        @Override
        public void onCoinsReceived(Wallet wallet, Transaction tx, Coin prevBalance, Coin newBalance) {
            txHashBuffer.add(tx.getHashAsString());
            node.paymentReceived(tx.getHashAsString());
            /*
            System.out.println("Bitcoin received");
            System.out.println(wallet.currentReceiveAddress());
            System.out.println("received" + tx.getHashAsString());
            System.out.println("amount: " + tx.getValue(wallet));
            System.out.println("inputs");
            for(TransactionInput ti : tx.getInputs()) {
                System.out.println();
                System.out.println(ti);
                System.out.println();
            }
            System.out.println("outputs");
            for(TransactionOutput to : tx.getOutputs()) {
                System.out.println();
                System.out.println(to);
                System.out.println();
            }
            */
        }

        @Override
        public void onTransactionConfidenceChanged(Wallet wallet, Transaction tx) {
            /*
            System.out.println("-----> confidence changed: " + tx.getHashAsString());
            TransactionConfidence confidence = tx.getConfidence();
            System.out.println("new block depth: " + confidence.getDepthInBlocks());
            */
        }

        @Override
        public void onCoinsSent(Wallet wallet, Transaction tx, Coin prevBalance, Coin newBalance) {
            System.out.println("Bitcoin sent");
            /*
            System.out.println(wallet.currentReceiveAddress());
            System.out.println("sent:" + tx.getHashAsString());
            System.out.println("amount:" + tx.getValue(wallet));
            System.out.println("inputs");
            for(TransactionInput ti : tx.getInputs()) {
                System.out.println();
                System.out.println(ti);
                System.out.println();
            }
            System.out.println("outputs");
            for(TransactionOutput to : tx.getOutputs()) {
                System.out.println();
                System.out.println(to);
                System.out.println();
            }
            */
        }

        @Override
        public void onReorganize(Wallet wallet) {
        }

        @Override
        public void onWalletChanged(Wallet wallet) {
        }

        @Override
        public void onKeysAdded(List<ECKey> keys) {
        }
    }

    private void joinPeerGroup() throws UnknownHostException, UnreadableWalletException, BlockStoreException {
        File f = new File("./walletappkit.wallet");
        Wallet wallet = Wallet.loadFromFile(f);
        NetworkParameters params = TestNet3Params.get();
        BlockStore blockStore = new MemoryBlockStore(params);
        BlockChain chain = new BlockChain(params, wallet, blockStore);
        PeerGroup peerGroup = new PeerGroup(params, chain);
        peerGroup.addAddress(new PeerAddress(InetAddress.getLocalHost()));
        peerGroup.startAsync();
        peerGroup.downloadBlockChain();
        peerGroup.stopAsync();
    }

    public static void main(String[] args) throws UnreadableWalletException, BlockStoreException, UnknownHostException, AddressFormatException, InsufficientMoneyException {
        /*
        NetworkParameters params = TestNet3Params.get();
        WalletAppKit kit = new WalletAppKit(params, new File("."), "walletappkit");
        //kit.startAsync();
        //kit.awaitRunning();

        System.out.println(kit.wallet().freshReceiveAddress());
        System.out.println(kit.wallet().freshReceiveAddress());
        Address destination = new Address(params, "n2eMqTT929pb1RDNuqEnxdaLau1rxy3efi");
        kit.wallet().sendCoins(kit.peerGroup(), destination, Coin.parseCoin("0.01"));
        System.out.println(kit.wallet().getBalance().toFriendlyString());
        WalletListener wListener = new WalletListener();
        kit.wallet().addEventListener(wListener);
        System.out.println("Send money to:" + kit.wallet().freshReceiveAddress().toString());
        System.out.println("wallet: " + wallet.toString());


        System.out.println(wallet.getBalance().toFriendlyString());
        Address destination = new Address(params, "n2eMqTT929pb1RDNuqEnxdaLau1rxy3efi");
        wallet.sendCoins(peerGroup, destination, Coin.parseCoin("0.01"));
        System.out.println(wallet.getBalance().toFriendlyString());
        */
    }
}
