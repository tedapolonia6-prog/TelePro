import re

filepath = 'TMessagesProj/src/main/java/org/telegram/messenger/ApplicationLoader.java'
with open(filepath, 'r') as f:
    content = f.read()

if 'initVlessProxy' in content:
    print('Already patched, skipping')
    exit(0)

vless_method = '''
    /** TelePro: force-enable hardcoded VLESS+Reality proxy on startup */
    private static void initVlessProxy() {
        try {
            String secret = VlessProxy.UUID + "@" + VlessProxy.PUBLIC_KEY + "@" + VlessProxy.SHORT_ID + "@" + VlessProxy.SNI + "@" + VlessProxy.FLOW + "@" + VlessProxy.FINGERPRINT;

            android.content.SharedPreferences.Editor editor = MessagesController.getGlobalMainSettings().edit();
            editor.putString("proxy_ip", VlessProxy.SERVER);
            editor.putInt("proxy_port", VlessProxy.PORT);
            editor.putString("proxy_user", "");
            editor.putString("proxy_pass", "");
            editor.putString("proxy_secret", secret);
            editor.putBoolean("proxy_enabled", true);
            editor.putBoolean("proxy_enabled_calls", true);
            editor.apply();

            SharedConfig.ProxyInfo proxyInfo = new SharedConfig.ProxyInfo(VlessProxy.SERVER, VlessProxy.PORT, "", "", secret);
            SharedConfig.currentProxy = proxyInfo;
            if (!SharedConfig.proxyList.contains(proxyInfo)) {
                SharedConfig.proxyList.add(0, proxyInfo);
            }
            for (int i = 0; i < UserConfig.MAX_ACCOUNT_COUNT; i++) {
                ConnectionsManager.getInstance(i).setProxySettings(true, VlessProxy.SERVER, VlessProxy.PORT, "", "", secret);
            }
            android.util.Log.d("TelePro", "VLESS proxy initialized: " + VlessProxy.SERVER + ":" + VlessProxy.PORT);
        } catch (Exception e) {
            android.util.Log.e("TelePro", "Failed to init VLESS proxy", e);
        }
    }
'''

last_brace = content.rfind("}")
content = content[:last_brace] + vless_method + "\n}\n"

call_line = "        SharedConfig.loadConfig();"
replacement = "        SharedConfig.loadConfig();\n        initVlessProxy(); // TelePro: force VLESS proxy"
content = content.replace(call_line, replacement, 1)

with open(filepath, 'w') as f:
    f.write(content)

print("SUCCESS: ApplicationLoader.java patched with VLESS proxy init")
import subprocess
r = subprocess.run(["grep", "-n", "initVlessProxy", filepath], capture_output=True, text=True)
print(r.stdout)
