package cs555.tebbe.bitcoin;

import org.bitcoinj.core.*;
import org.bitcoinj.kits.WalletAppKit;
import org.bitcoinj.params.TestNet3Params;
import org.bitcoinj.script.Script;
import org.bitcoinj.store.BlockStore;
import org.bitcoinj.store.BlockStoreException;
import org.bitcoinj.store.MemoryBlockStore;
import org.bitcoinj.store.UnreadableWalletException;

import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;

/**
 * Created by caleb.tebbe
 */
public class BitcoinManager {

    WalletAppKit walletKit;

    public BitcoinManager(String walletName) {
        NetworkParameters params = TestNet3Params.get();
        walletKit = new WalletAppKit(params, new File("./wallets"), walletName);
        walletKit.wallet().addEventListener(new WalletListener());
        walletKit.startAsync();
        walletKit.awaitRunning();
    }
    public static void main(String[] args) throws UnreadableWalletException, BlockStoreException, UnknownHostException, AddressFormatException, InsufficientMoneyException {
        NetworkParameters params = TestNet3Params.get();
        WalletAppKit kit = new WalletAppKit(params, new File("."), "walletappkit");
        kit.startAsync();
        kit.awaitRunning();

        System.out.println(kit.wallet().freshReceiveAddress());
        System.out.println(kit.wallet().freshReceiveAddress());
        /*
        Address destination = new Address(params, "n2eMqTT929pb1RDNuqEnxdaLau1rxy3efi");
        kit.wallet().sendCoins(kit.peerGroup(), destination, Coin.parseCoin("0.01"));
        System.out.println(kit.wallet().getBalance().toFriendlyString());
        WalletListener wListener = new WalletListener();
        kit.wallet().addEventListener(wListener);
        System.out.println("Send money to:" + kit.wallet().freshReceiveAddress().toString());
        File f = new File("./walletappkit.wallet");
        Wallet wallet = Wallet.loadFromFile(f);
        System.out.println("wallet: " + wallet.toString());

        NetworkParameters params = TestNet3Params.get();
        BlockStore blockStore = new MemoryBlockStore(params);
        BlockChain chain = new BlockChain(params, wallet, blockStore);
        PeerGroup peerGroup = new PeerGroup(params, chain);
        peerGroup.addAddress(new PeerAddress(InetAddress.getLocalHost()));
        peerGroup.startAsync();
        peerGroup.downloadBlockChain();
        peerGroup.stopAsync();

        System.out.println(wallet.getBalance().toFriendlyString());
        Address destination = new Address(params, "n2eMqTT929pb1RDNuqEnxdaLau1rxy3efi");
        wallet.sendCoins(peerGroup, destination, Coin.parseCoin("0.01"));
        System.out.println(wallet.getBalance().toFriendlyString());
        */
    }

    static class WalletListener extends AbstractWalletEventListener {

        @Override
        public void onCoinsReceived(Wallet wallet, Transaction tx, Coin prevBalance, Coin newBalance) {
            System.out.println("-----> coins received: " + tx.getHashAsString());
            System.out.println("received: " + tx.getValue(wallet));
        }

        @Override
        public void onTransactionConfidenceChanged(Wallet wallet, Transaction tx) {
            System.out.println("-----> confidence changed: " + tx.getHashAsString());
            TransactionConfidence confidence = tx.getConfidence();
            System.out.println("new block depth: " + confidence.getDepthInBlocks());
        }

        @Override
        public void onCoinsSent(Wallet wallet, Transaction tx, Coin prevBalance, Coin newBalance) {
            System.out.println("coins sent");
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
}
