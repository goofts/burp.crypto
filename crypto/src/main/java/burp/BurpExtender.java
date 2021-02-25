package burp;

import burp.aes.AesUIHandler;
import burp.des.DesUIHandler;
import burp.execjs.JsUIHandler;
import burp.rsa.RsaUIHandler;
import burp.utils.BurpCryptoMenuFactory;
import burp.utils.BurpStateListener;
import burp.utils.DictLogManager;
import org.iq80.leveldb.DB;
import org.iq80.leveldb.Options;

import javax.script.ScriptException;
import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;

import static org.iq80.leveldb.impl.Iq80DBFactory.factory;

public class BurpExtender implements IBurpExtender, ITab {

    public IExtensionHelpers helpers;
    public IBurpExtenderCallbacks callbacks;
    public PrintWriter stdout;
    public PrintWriter stderr;
    public DB store;
    public DictLogManager dict;
    private static String extensionName = "crypto";
    private static String version ="1.1.1";
    public HashMap<String, IIntruderPayloadProcessor> IPProcessors = new HashMap<>();

    public JTabbedPane mainPanel;

    public JPanel aesPanel;
    public AesUIHandler AesUI;


    public JPanel rsaPanel;
    public RsaUIHandler RsaUI;

    public JPanel desPanel;
    public DesUIHandler DesUI;

    public JPanel execJsPanel;
    public JsUIHandler JsUI;
    
    public boolean RegIPProcessor(String extName, IIntruderPayloadProcessor processor) {
        if (IPProcessors.containsKey(extName)) {
            JOptionPane.showMessageDialog(mainPanel, "This name already exist!");
            return false;
        }
        callbacks.registerIntruderPayloadProcessor(processor);
        IPProcessors.put(extName, processor);
        return true;
    }

    public void RemoveIPProcessor(String extName) {
        if (IPProcessors.containsKey(extName)) {
            IIntruderPayloadProcessor processor = IPProcessors.get(extName);
            callbacks.removeIntruderPayloadProcessor(processor);
            IPProcessors.remove(extName);
        }
    }

    @Override
    public void registerExtenderCallbacks(IBurpExtenderCallbacks callbacks) throws FileNotFoundException, ScriptException, NoSuchMethodException {
        this.callbacks = callbacks;
        this.helpers = callbacks.getHelpers();
        this.stdout = new PrintWriter(callbacks.getStdout(), true);
        this.stderr = new PrintWriter(callbacks.getStderr(), true);
        callbacks.setExtensionName(extensionName);
        callbacks.registerExtensionStateListener(new BurpStateListener(this));
        callbacks.registerContextMenuFactory(new BurpCryptoMenuFactory(this));
        Options options = new Options();
        options.createIfMissing(true);
        try {
            this.store = factory.open(new File("BurpCrypto.ldb"), options);
            this.dict = new DictLogManager(this);
            callbacks.issueAlert("LevelDb init success!");
        } catch (IOException e) {
            callbacks.issueAlert("LevelDb init failed! error message: " + e.getMessage());
        }

        stdout.println(getBanner());
        InitUi();
    }

    private void InitUi() {
        this.AesUI = new AesUIHandler(this);
        this.RsaUI = new RsaUIHandler(this);
        this.JsUI = new JsUIHandler(this);
        this.DesUI = new DesUIHandler(this);
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                BurpExtender bthis = BurpExtender.this;
                bthis.mainPanel = new JTabbedPane();
                bthis.aesPanel = AesUI.getPanel();
                bthis.rsaPanel = RsaUI.getPanel();
                bthis.desPanel = DesUI.getPanel();
                bthis.execJsPanel = JsUI.getPanel();
                bthis.mainPanel.addTab("AES", bthis.aesPanel);
                bthis.mainPanel.addTab("RSA", bthis.rsaPanel);
                bthis.mainPanel.addTab("DES", bthis.desPanel);
                bthis.mainPanel.addTab("Exec Js", bthis.execJsPanel);
                bthis.callbacks.addSuiteTab(bthis);
            }
        });
    }


    @Override
    public String getTabCaption() {
        return "crypto";
    }

    @Override
    public Component getUiComponent() {
        return this.mainPanel;
    }

    /**
     * 插件Banner信息
     * @return
     */
    public static String getBanner(){
        String bannerInfo =
                "[+] " + extensionName + " is loaded\n"
                        + "[+]\n"
                        + "[+] ###########################################################\n"
                        + "[+]    " + extensionName + " v" + version +"\n"
                        + "[+]    anthor:   Whwlsfb\n"
                        + "[+]    email:    whwlsfb@wanghw.cn\n"
                        + "[+]    github:   https://github.com/whwlsfb/BurpCrypto\n"
                        + "[+]    modifier: goofts\n"
                        + "[+]    date:     2021/1/14\n"
                        + "[+] ###########################################################\n"
                        + "[+] Please enjoy it";
        return bannerInfo;
    }
}