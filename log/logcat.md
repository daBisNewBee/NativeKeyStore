> 以下为 `ExampleInstrumentedTest`下的`useAppContext`运行结果。

## 首次运行
```
V/test    (11246): Searching key in KS:
V/test    (11246):
V/test    (11246):
V/test    (11246): starting to generate/import: [import_key_alias] in KS
V/test    (11246): find alias = 1ff07be213cfd3135f6a4724b774f1f0_ca540932-b674-4b4a-adb1-ae9d0da886e1
D/test    (11246): getKeyStoreInNative. alias:USRPKEY_import_key_alias
D/test    (11246): starting to load engine keystore...........
D/test    (11246): END load engine keystore...........
D/test    (11246): listing available engine types
D/test    (11246): engine 0, id = "dynamic", name = "Dynamic engine loading support"
D/test    (11246): engine 1, id = "keystore", name = "Android keystore engine"
D/test    (11246): end of list
D/test    (11246): ENGINE_load_private_key:USRPKEY_import_key_alias success. addr:0x54d80680
D/test    (11246): ENGINE_load_public_key:USRPKEY_import_key_alias success. addr:54d80500
D/test    (11246): OpenSSL_Sign success.
D/test    (11246): OpenSSL_Sign. ret:0 sigLen:128
D/test    (11246): OpenSSL_Verify success.
D/test    (11246): OpenSSL_Verify. ret:0
V/test    (11246): done:import_key_alias
V/test    (11246):
V/test    (11246): starting to generate/import: [orig_key_alias] in KS
V/test    (11246): genKeyPairInKsTest done.
D/test    (11246): getKeyStoreInNative. alias:USRPKEY_orig_key_alias
D/test    (11246): use default. already find keystore engine.
D/test    (11246): listing available engine types
D/test    (11246): engine 0, id = "dynamic", name = "Dynamic engine loading support"
D/test    (11246): engine 1, id = "keystore", name = "Android keystore engine"
D/test    (11246): end of list
D/test    (11246): ENGINE_load_private_key:USRPKEY_orig_key_alias success. addr:0x54d82f50
D/test    (11246): ENGINE_load_public_key:USRPKEY_orig_key_alias success. addr:54d82cd0
D/test    (11246): OpenSSL_Sign success.
D/test    (11246): OpenSSL_Sign. ret:0 sigLen:256
D/test    (11246): OpenSSL_Verify success.
D/test    (11246): OpenSSL_Verify. ret:0
V/test    (11246): done:orig_key_alias
```

## 第二次运行
```
V/test    (11317): Searching key in KS:
V/test    (11317): import_key_alias
V/test    (11317): orig_key_alias
V/test    (11317):
V/test    (11317):
V/test    (11317): already find: [import_key_alias] in "KS"
D/test    (11317): getKeyStoreInNative. alias:USRPKEY_import_key_alias
D/test    (11317): starting to load engine keystore...........
D/test    (11317): END load engine keystore...........
D/test    (11317): listing available engine types
D/test    (11317): engine 0, id = "dynamic", name = "Dynamic engine loading support"
D/test    (11317): engine 1, id = "keystore", name = "Android keystore engine"
D/test    (11317): end of list
D/test    (11317): ENGINE_load_private_key:USRPKEY_import_key_alias success. addr:0x54d80048
D/test    (11317): ENGINE_load_public_key:USRPKEY_import_key_alias success. addr:54d7fec8
D/test    (11317): OpenSSL_Sign success.
D/test    (11317): OpenSSL_Sign. ret:0 sigLen:128
D/test    (11317): OpenSSL_Verify success.
D/test    (11317): OpenSSL_Verify. ret:0
V/test    (11317): done:import_key_alias
V/test    (11317):
V/test    (11317): already find: [orig_key_alias] in "KS"
D/test    (11317): getKeyStoreInNative. alias:USRPKEY_orig_key_alias
D/test    (11317): use default. already find keystore engine.
D/test    (11317): listing available engine types
D/test    (11317): engine 0, id = "dynamic", name = "Dynamic engine loading support"
D/test    (11317): engine 1, id = "keystore", name = "Android keystore engine"
D/test    (11317): end of list
D/test    (11317): ENGINE_load_private_key:USRPKEY_orig_key_alias success. addr:0x54d807c8
D/test    (11317): ENGINE_load_public_key:USRPKEY_orig_key_alias success. addr:54d80520
D/test    (11317): OpenSSL_Sign success.
D/test    (11317): OpenSSL_Sign. ret:0 sigLen:256
D/test    (11317): OpenSSL_Verify success.
D/test    (11317): OpenSSL_Verify. ret:0
V/test    (11317): done:orig_key_alias

```
